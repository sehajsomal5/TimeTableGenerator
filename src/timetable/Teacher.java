package timetable;

import java.util.*;

public class Teacher {
    public String name;
    public Map<String, Set<String>> teaches = new LinkedHashMap<>();
    public Map<String, Set<Integer>> notAvailable = new LinkedHashMap<>();

    public Teacher(String name) {
        this.name = name;
    }

    public void addTeaches(String className, String subject) {
        teaches.putIfAbsent(className, new HashSet<>());
        teaches.get(className).add(subject);
    }

    public void addNotAvailable(String day, List<Integer> periods) {
        notAvailable.putIfAbsent(day, new HashSet<>());
        notAvailable.get(day).addAll(periods);
    }

    public boolean canTeach(String className, String subject) {
        String clsLower = className.trim().toLowerCase();
        String subLower = subject.trim().toLowerCase();

        for (String c : teaches.keySet()) {
            if (c.trim().toLowerCase().equals(clsLower)) {
                for (String s : teaches.get(c)) {
                    if (s.trim().toLowerCase().equals(subLower)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isAvailable(String day, int period, int key, Map<String, Map<Integer, String>> teacherSchedule) {
        if (notAvailable.containsKey(day) && notAvailable.get(day).contains(period))
            return false;
        Map<Integer, String> schedule = teacherSchedule.get(name);
        return !schedule.containsKey(key);
    }
}
