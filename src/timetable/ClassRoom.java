package timetable;

import java.util.*;

public class ClassRoom {
    public String name;
    public Map<String, Integer> subjectsPerWeek;

    // Subject -> List of other Class Names that share this subject
    public Map<String, List<String>> combinedSubjects;

    // Fixed periods mandated by govt
    public List<FixedPeriod> fixedPeriods;

    public ClassRoom(String name) {
        this.name = name;
        subjectsPerWeek = new LinkedHashMap<>();
        combinedSubjects = new LinkedHashMap<>();
        fixedPeriods = new ArrayList<>();
    }

    public void addSubject(String subject, int lecturesPerWeek) {
        addSubject(subject, lecturesPerWeek, new ArrayList<>());
    }

    public void addSubject(String subject, int lecturesPerWeek, List<String> combinedWith) {
        subjectsPerWeek.put(subject, lecturesPerWeek);
        combinedSubjects.put(subject, combinedWith);
    }

    public void addFixedPeriod(String day, int period, String subject, String teacherName) {
        fixedPeriods.add(new FixedPeriod(day, period, subject, teacherName));
    }
}
