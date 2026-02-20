package timetable;

public class TimetableEntry {
    public String className;
    public String subject;
    public String teacherName;

    public TimetableEntry(String className, String subject, String teacherName) {
        this.className = className;
        this.subject = subject;
        this.teacherName = teacherName;
    }
}
