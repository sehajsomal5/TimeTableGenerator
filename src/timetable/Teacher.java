package timetable;

import java.util.*;

public class Teacher {
    public String name;
    public Map<String, Set<String>> teaches = new HashMap<>();
    public Map<String, Set<Integer>> notAvailable = new HashMap<>();

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
        return teaches.containsKey(className) && teaches.get(className).contains(subject);
    }

    public boolean isAvailable(String day, int period, Map<String, Map<Integer, String>> teacherSchedule) {
        if (notAvailable.containsKey(day) && notAvailable.get(day).contains(period)) return false;
        Map<Integer, String> schedule = teacherSchedule.get(name);
        int key = period + (TimetableUtils.getDayIndex(day) * 100);
        return !schedule.containsKey(key);
    }
}
