package timetable;
import java.util.*;

public class ClassRoom {
    String name;
    Map<String, Integer> subjectsPerWeek;

    public ClassRoom(String name) {
        this.name = name;
        subjectsPerWeek = new HashMap<>();
    }

    public void addSubject(String subject, int lecturesPerWeek) {
        subjectsPerWeek.put(subject, lecturesPerWeek);
    }
}
