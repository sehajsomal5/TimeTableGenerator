package timetable;

import java.util.*;

public class TimetableGenerator {

    public List<String> days;
    public int periodsPerDay;
    public List<Teacher> teachers;
    public List<ClassRoom> classes;

    public Map<String, Map<Integer, TimetableEntry>> classSchedule = new HashMap<>();
    public Map<String, Map<Integer, String>> teacherSchedule = new HashMap<>();

    public TimetableGenerator(List<String> days, int periodsPerDay, List<Teacher> teachers, List<ClassRoom> classes) {
        this.days = days;
        this.periodsPerDay = periodsPerDay;
        this.teachers = teachers;
        this.classes = classes;

        for (ClassRoom c : classes) classSchedule.put(c.name, new HashMap<>());
        for (Teacher t : teachers) teacherSchedule.put(t.name, new HashMap<>());
    }

    // Public method to start timetable generation
    public boolean generate() {
        return backtrack(0, 0, 1); // classIndex, subjectIndex, period
    }

    // Backtracking method to assign subjects to periods
    private boolean backtrack(int classIndex, int subjectIndex, int period) {
        if (classIndex >= classes.size()) return true; // all classes done

        ClassRoom cls = classes.get(classIndex);
        List<String> subjects = new ArrayList<>(cls.subjectsPerWeek.keySet());
        if (subjectIndex >= subjects.size()) {
            // move to next class
            return backtrack(classIndex + 1, 0, 1);
        }

        String subject = subjects.get(subjectIndex);
        int lecturesRemaining = cls.subjectsPerWeek.get(subject);

        // Try to assign all lectures for this subject
        return assignLecture(cls, subject, lecturesRemaining, classIndex, subjectIndex, 0, 1);
    }

    private boolean assignLecture(ClassRoom cls, String subject, int remaining, int classIndex, int subjectIndex, int dayIndex, int periodIndex) {
        if (remaining == 0) {
            // Move to next subject
            return backtrack(classIndex, subjectIndex + 1, 1);
        }
        if (dayIndex >= days.size()) return false; // no more days to assign

        String day = days.get(dayIndex);
        if (periodIndex > periodsPerDay) {
            // Next day
            return assignLecture(cls, subject, remaining, classIndex, subjectIndex, dayIndex + 1, 1);
        }

        int key = periodIndex + dayIndex * 100;

        if (!classSchedule.get(cls.name).containsKey(key)) {
            // Try all teachers for this subject
            for (Teacher t : teachers) {
                if (!t.canTeach(cls.name, subject)) continue;
                if (!t.isAvailable(day, periodIndex, teacherSchedule)) continue;

                // Assign
                TimetableEntry entry = new TimetableEntry(cls.name, subject, t.name);
                classSchedule.get(cls.name).put(key, entry);
                teacherSchedule.get(t.name).put(key, cls.name);

                // Recurse to assign next lecture
                if (assignLecture(cls, subject, remaining - 1, classIndex, subjectIndex, dayIndex, periodIndex + 1)) return true;

                // Backtrack
                classSchedule.get(cls.name).remove(key);
                teacherSchedule.get(t.name).remove(key);
            }
        }

        // Try next period
        return assignLecture(cls, subject, remaining, classIndex, subjectIndex, dayIndex, periodIndex + 1);
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
                    if (entry != null) System.out.print(entry.subject + "(" + entry.teacherName + ") ");
                    else System.out.print("FREE ");
                }
                System.out.println();
            }
            System.out.println("-------------------------");
        }
    }
}
