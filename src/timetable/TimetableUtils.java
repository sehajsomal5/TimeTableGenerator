package timetable;

import java.util.*;

public class TimetableUtils {

    private static final List<String> days = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");

    public static int getDayIndex(String day) {
        int index = days.indexOf(day);
        if (index == -1) throw new IllegalArgumentException("Invalid day: " + day);
        return index;
    }

    public static List<String> getDays() {
        return new ArrayList<>(days);
    }
}
