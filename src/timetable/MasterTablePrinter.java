package timetable;

import java.util.*;

public class MasterTablePrinter {

    List<String> days;
    int periodsPerDay;
    List<ClassRoom> classes;
    List<Teacher> teachers;
    Map<String, Map<Integer, TimetableEntry>> classSchedule;
    Map<String, Map<Integer, String>> teacherSchedule;

    public MasterTablePrinter(TimetableGenerator tg) {
        this.days = tg.days;
        this.periodsPerDay = tg.periodsPerDay;
        this.classes = tg.classes;
        this.teachers = tg.teachers;
        this.classSchedule = tg.classSchedule;
        this.teacherSchedule = tg.teacherSchedule;
    }

    public void printMasterTableByClass() {
        System.out.println("=== Master Table 1: Period vs Class ===");
        System.out.print(String.format("%-10s", "Period"));
        for (ClassRoom cls : classes) {
            System.out.print(String.format("%-30s", cls.name));
        }
        System.out.println();

        for (int p = 1; p <= periodsPerDay; p++) {
            System.out.print(String.format("%-10s", "P" + p));
            for (ClassRoom cls : classes) {
                Map<String, List<String>> subjectDays = new LinkedHashMap<>();
                for (int d = 0; d < days.size(); d++) {
                    String day = days.get(d);
                    int key = p + d * 100;
                    TimetableEntry entry = classSchedule.get(cls.name).get(key);
                    if (entry != null) {
                        String subj = entry.subject + " (" + entry.teacherName + ")";
                        subjectDays.putIfAbsent(subj, new ArrayList<>());
                        subjectDays.get(subj).add(day.substring(0,3));
                    }
                }
                StringBuilder cell = new StringBuilder();
                for (Map.Entry<String, List<String>> sd : subjectDays.entrySet()) {
                    cell.append(sd.getKey()).append(" — ").append(String.join(", ", sd.getValue())).append("; ");
                }
                System.out.print(String.format("%-30s", cell.toString()));
            }
            System.out.println();
        }
        System.out.println();
    }

    public void printMasterTableByTeacher() {
        System.out.println("=== Master Table 2: Period vs Teacher ===");
        System.out.print(String.format("%-10s", "Period"));
        for (Teacher t : teachers) {
            System.out.print(String.format("%-30s", t.name));
        }
        System.out.println();

        for (int p = 1; p <= periodsPerDay; p++) {
            System.out.print(String.format("%-10s", "P" + p));
            for (Teacher t : teachers) {
                Map<String, List<String>> subjDays = new LinkedHashMap<>();
                for (int d = 0; d < days.size(); d++) {
                    String day = days.get(d);
                    int key = p + d * 100;
                    String clsName = teacherSchedule.get(t.name).get(key);
                    if (clsName != null) {
                        TimetableEntry entry = classSchedule.get(clsName).get(key);
                        if (entry != null) {
                            String subjCls = entry.subject + " (" + entry.className + ")";
                            subjDays.putIfAbsent(subjCls, new ArrayList<>());
                            subjDays.get(subjCls).add(day.substring(0,3));
                        }
                    }
                }
                StringBuilder cell = new StringBuilder();
                for (Map.Entry<String, List<String>> sd : subjDays.entrySet()) {
                    cell.append(sd.getKey()).append(" — ").append(String.join(", ", sd.getValue())).append("; ");
                }
                System.out.print(String.format("%-30s", cell.toString()));
            }
            System.out.println();
        }
        System.out.println();
    }
}
