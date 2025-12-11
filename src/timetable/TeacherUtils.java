package timetable;
import java.util.*;

public class TeacherUtils {
    public static List<String> days = Arrays.asList("Monday","Tuesday","Wednesday","Thursday","Friday");

    public static int getDayIndex(String day) {
        return days.indexOf(day);
    }
}
