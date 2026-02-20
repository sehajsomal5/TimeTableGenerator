package timetable;

public class FixedPeriod {
    public String day;
    public int period;
    public String subject;
    public String teacherName;

    public FixedPeriod(String day, int period, String subject, String teacherName) {
        this.day = day;
        this.period = period;
        this.subject = subject;
        this.teacherName = teacherName;
    }
}
