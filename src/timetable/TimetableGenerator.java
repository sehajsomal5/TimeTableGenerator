package timetable;

import java.util.*;

public class TimetableGenerator {

    public List<String> days;
    public int periodsPerDay;
    public List<Teacher> teachers;
    public List<ClassRoom> classes;

    public Map<String, Map<Integer, TimetableEntry>> classSchedule = new LinkedHashMap<>();
    public Map<String, Map<Integer, String>> teacherSchedule = new LinkedHashMap<>();

    public TimetableGenerator(List<String> days, int periodsPerDay, List<Teacher> teachers, List<ClassRoom> classes) {
        this.days = days;
        this.periodsPerDay = periodsPerDay;
        this.teachers = teachers;
        this.classes = classes;

        for (ClassRoom c : classes)
            classSchedule.put(c.name, new LinkedHashMap<>());
        for (Teacher t : teachers)
            teacherSchedule.put(t.name, new LinkedHashMap<>());

        // Pre-fill Fixed Periods
        for (ClassRoom cls : classes) {
            for (FixedPeriod fp : cls.fixedPeriods) {
                int dayIndex = days.indexOf(fp.day);
                if (dayIndex == -1)
                    continue;
                int key = fp.period + dayIndex * 100;

                List<String> combinedClassNames = cls.combinedSubjects.getOrDefault(fp.subject, new ArrayList<>());
                List<String> allClasses = new ArrayList<>();
                allClasses.add(cls.name);
                allClasses.addAll(combinedClassNames);

                for (String cName : allClasses) {
                    if (classSchedule.containsKey(cName)) {
                        classSchedule.get(cName).put(key, new TimetableEntry(cName, fp.subject, fp.teacherName));
                    }
                }
                if (teacherSchedule.containsKey(fp.teacherName)) {
                    teacherSchedule.get(fp.teacherName).put(key,
                            cls.name + (combinedClassNames.isEmpty() ? ""
                                    : "+" + String.join("+", combinedClassNames)));
                }
            }
        }
    }

    // Public method to start timetable generation
    public boolean generate() {
        return backtrack(0, 0, 1); // classIndex, subjectIndex, period
    }

    private int maxConsecutivePeriods = 5;

    // Backtracking method to assign subjects to periods
    private boolean backtrack(int classIndex, int subjectIndex, int period) {
        if (classIndex >= classes.size())
            return true; // all classes done

        ClassRoom cls = classes.get(classIndex);

        // SORT SUBJECTS BY FREQUENCY (Constraint 6.1)
        List<String> subjects = new ArrayList<>(cls.subjectsPerWeek.keySet());
        subjects.sort((s1, s2) -> Integer.compare(cls.subjectsPerWeek.get(s2), cls.subjectsPerWeek.get(s1)));

        if (subjectIndex >= subjects.size()) {
            // move to next class
            return backtrack(classIndex + 1, 0, 1);
        }

        String subject = subjects.get(subjectIndex);
        String subjectLower = subject.trim().toLowerCase();

        long scheduledCount = classSchedule.get(cls.name).values().stream()
                .filter(e -> e.subject.trim().toLowerCase().equals(subjectLower))
                .count();
        int required = cls.subjectsPerWeek.get(subject);

        if (scheduledCount >= required) {
            return backtrack(classIndex, subjectIndex + 1, 1);
        }

        int lecturesRemaining = required - (int) scheduledCount;

        // Try to assign all lectures for this subject
        // Search starting from Monday P1 for the first lecture
        return assignLecture(cls, subject, lecturesRemaining, classIndex, subjectIndex, 0, 1);
    }

    private boolean assignLecture(ClassRoom cls, String subject, int remaining, int classIndex, int subjectIndex,
            int startDay, int startPeriod) {
        if (remaining == 0) {
            // Move to next subject
            return backtrack(classIndex, subjectIndex + 1, 1);
        }

        // Try every possible slot in the week, starting from (startDay, startPeriod)
        // This ensures the algorithm is complete (tries all valid combinations)
        int totalSlots = days.size() * periodsPerDay;
        int startAbsoluteSlot = startDay * periodsPerDay + (startPeriod - 1);

        for (int i = 0; i < totalSlots; i++) {
            int currentAbsoluteSlot = (startAbsoluteSlot + i) % totalSlots;
            int d = currentAbsoluteSlot / periodsPerDay;
            int p = (currentAbsoluteSlot % periodsPerDay) + 1;
            int key = p + d * 100;
            String day = days.get(d);

            // Implementation of Combined Classes
            List<String> combinedClassNames = cls.combinedSubjects.getOrDefault(subject, new ArrayList<>());
            List<String> allClassesToSchedule = new ArrayList<>();
            allClassesToSchedule.add(cls.name);
            allClassesToSchedule.addAll(combinedClassNames);

            // Check availability for ALL classes involved
            boolean allAvailable = true;
            for (String cName : allClassesToSchedule) {
                if (classSchedule.containsKey(cName) && classSchedule.get(cName).containsKey(key)) {
                    allAvailable = false;
                    break;
                }
            }

            if (allAvailable) {
                // Try all teachers for this subject
                for (Teacher t : teachers) {
                    if (classSchedule.containsKey(cls.name) && !t.canTeach(cls.name, subject))
                        continue;
                    if (!t.isAvailable(day, p, key, teacherSchedule))
                        continue;

                    // Teacher Overload Check
                    if (isTeacherOverloaded(t, d, p))
                        continue;

                    // Assign to ALL classes
                    for (String cName : allClassesToSchedule) {
                        TimetableEntry entry = new TimetableEntry(cName, subject, t.name);
                        classSchedule.get(cName).put(key, entry);
                    }
                    teacherSchedule.get(t.name).put(key,
                            cls.name + (combinedClassNames.isEmpty() ? ""
                                    : "+" + String.join("+", combinedClassNames)));

                    // Recurse to assign next lecture - PREFER NEXT DAY to spread lectures
                    if (assignLecture(cls, subject, remaining - 1, classIndex, subjectIndex, (d + 1) % days.size(), 1))
                        return true;

                    // Backtrack
                    for (String cName : allClassesToSchedule) {
                        classSchedule.get(cName).remove(key);
                    }
                    teacherSchedule.get(t.name).remove(key);
                }
            }
        }

        return false;
    }

    private boolean isTeacherOverloaded(Teacher t, int dayIndex, int periodIndex) {
        int consecutive = 0;
        // Check backwards
        for (int p = periodIndex - 1; p >= 1; p--) {
            int key = p + dayIndex * 100;
            if (teacherSchedule.get(t.name).containsKey(key)) {
                consecutive++;
            } else {
                break;
            }
        }
        return consecutive >= maxConsecutivePeriods;
    }

    // Optional: simple timetable print
    public void printTimetable() {
        for (ClassRoom cls : classes) {
            System.out.println("Timetable for " + cls.name + ":");
            for (int d = 0; d < days.size(); d++) {
                String day = days.get(d);
                System.out.print(day + ": ");
                for (int p = 1; p <= periodsPerDay; p++) {
                    TimetableEntry entry = classSchedule.get(cls.name).get(p + d * 100);
                    if (entry != null)
                        System.out.print(entry.subject + "(" + entry.teacherName + ") ");
                    else
                        System.out.print("FREE ");
                }
                System.out.println();
            }
            System.out.println("-------------------------");
        }
    }
}
