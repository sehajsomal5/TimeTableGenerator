package timetable;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        List<String> days = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");

        System.out.print("Enter number of periods per day: ");
        int periodsPerDay = sc.nextInt();
        sc.nextLine();

        // Input Classes and Subjects
        System.out.print("Enter number of classes: ");
        int numClasses = sc.nextInt();
        sc.nextLine();

        List<ClassRoom> classes = new ArrayList<>();

        for (int i = 0; i < numClasses; i++) {
            System.out.print("Enter class name: ");
            String className = sc.nextLine();
            ClassRoom cls = new ClassRoom(className);

            System.out.print("Enter number of subjects for this class: ");
            int numSubjects = sc.nextInt();
            sc.nextLine();

            for (int j = 0; j < numSubjects; j++) {
                System.out.print("Enter subject name: ");
                String subject = sc.nextLine();
                System.out.print("Enter lectures per week for " + subject + ": ");
                int lectures = sc.nextInt();
                sc.nextLine();
                cls.addSubject(subject, lectures);
            }
            classes.add(cls);
        }

        // Input Teachers
        System.out.print("Enter number of teachers: ");
        int numTeachers = sc.nextInt();
        sc.nextLine();

        List<Teacher> teachers = new ArrayList<>();

        for (int i = 0; i < numTeachers; i++) {
            System.out.print("Enter teacher name: ");
            String tName = sc.nextLine();
            Teacher t = new Teacher(tName);

            System.out.print("Enter number of subjects this teacher teaches: ");
            int ts = sc.nextInt();
            sc.nextLine();

            for (int j = 0; j < ts; j++) {
                System.out.print("Enter class name for this subject: ");
                String clsName = sc.nextLine();
                System.out.print("Enter subject name: ");
                String subName = sc.nextLine();
                t.addTeaches(clsName, subName);
            }

            System.out.print("Enter number of non-available slots: ");
            int na = sc.nextInt();
            sc.nextLine();

            for (int j = 0; j < na; j++) {
                System.out.print("Enter day (Monday-Friday): ");
                String day = sc.nextLine();
                System.out.print("Enter number of periods not available on " + day + ": ");
                int numPeriods = sc.nextInt();
                sc.nextLine();
                List<Integer> periods = new ArrayList<>();
                for (int k = 0; k < numPeriods; k++) {
                    System.out.print("Enter period number: ");
                    periods.add(sc.nextInt());
                }
                sc.nextLine();
                t.addNotAvailable(day, periods);
            }
            teachers.add(t);
        }

        TimetableGenerator tg = new TimetableGenerator(days, periodsPerDay, teachers, classes);

        if (tg.generate()) tg.printTimetable();
        else System.out.println("No valid timetable possible with given constraints.");

        sc.close();
    }
}
