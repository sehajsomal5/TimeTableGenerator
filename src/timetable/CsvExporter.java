package timetable;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CsvExporter {

    public static void export(TimetableGenerator tg, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            // 1. Class-wise Timetable
            writer.append("=== CLASS-WISE TIMETABLE ===\n");
            writer.append("Period,");
            for (ClassRoom cls : tg.classes) {
                writer.append(cls.name).append(",");
            }
            writer.append("\n");

            for (int p = 1; p <= tg.periodsPerDay; p++) {
                writer.append("Period ").append(String.valueOf(p)).append(",");
                for (ClassRoom cls : tg.classes) {
                    writer.append("\"");
                    // Collect all days for this period
                    List<String> entries = new ArrayList<>();
                    for (int d = 0; d < tg.days.size(); d++) {
                        int key = p + d * 100;
                        TimetableEntry entry = tg.classSchedule.get(cls.name).get(key);
                        if (entry != null) {
                            String dayShort = tg.days.get(d).substring(0, 3);
                            entries.add(dayShort + ": " + entry.subject + " (" + entry.teacherName + ")");
                        }
                    }
                    writer.append(String.join("\n", entries));
                    writer.append("\",");
                }
                writer.append("\n");
            }

            writer.append("\n\n");

            // 2. Teacher-wise Timetable
            writer.append("=== TEACHER-WISE TIMETABLE ===\n");
            writer.append("Period,");
            for (Teacher t : tg.teachers) {
                writer.append(t.name).append(",");
            }
            writer.append("\n");

            for (int p = 1; p <= tg.periodsPerDay; p++) {
                writer.append("Period ").append(String.valueOf(p)).append(",");
                for (Teacher t : tg.teachers) {
                    writer.append("\"");
                    List<String> entries = new ArrayList<>();
                    for (int d = 0; d < tg.days.size(); d++) {
                        int key = p + d * 100;
                        String className = tg.teacherSchedule.get(t.name).get(key);
                        if (className != null) {
                            String dayShort = tg.days.get(d).substring(0, 3);
                            // We need subject name, but teacherSchedule only has className.
                            // We can look it up in classSchedule, but we need to know WHICH class if it was
                            // a combined one.
                            // The teacherSchedule stores "ClassA+ClassB", so we can just pick the first one
                            // to find the subject
                            String lookupClass = className.split("\\+")[0];
                            TimetableEntry entry = tg.classSchedule.get(lookupClass).get(key);
                            String subj = (entry != null) ? entry.subject : "???";

                            entries.add(dayShort + ": " + subj + " (" + className + ")");
                        }
                    }
                    writer.append(String.join("\n", entries));
                    writer.append("\",");
                }
                writer.append("\n");
            }

            System.out.println("Successfully exported to " + filename);

        } catch (IOException e) {
            System.err.println("Error writing to CSV: " + e.getMessage());
        }
    }
}
