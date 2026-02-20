package timetable;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        List<String> days = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");

        int periodsPerDay = getValidInt(sc, "Enter number of periods per day: ");

        // Input Classes and Subjects
        int numClasses = getValidInt(sc, "Enter number of classes: ");

        List<ClassRoom> classes = new ArrayList<>();

        for (int i = 0; i < numClasses; i++) {
            System.out.print("Enter class name: ");
            String className = sc.nextLine();
            ClassRoom cls = new ClassRoom(className);

            int numSubjects = getValidInt(sc, "Enter number of subjects for this class: ");

            for (int j = 0; j < numSubjects; j++) {
                System.out.print("Enter subject name: ");
                String subject = sc.nextLine();
                int lectures = getValidInt(sc, "Enter lectures per week for " + subject + ": ");

                System.out.print("Is " + subject + " combined with other classes? (y/n): ");
                String isCombined = sc.nextLine();
                List<String> combinedWith = new ArrayList<>();
                if (isCombined.equalsIgnoreCase("y")) {
                    System.out.print("Enter names of other classes (comma separated): ");
                    String others = sc.nextLine();
                    String[] parts = others.split(",");
                    for (String p : parts)
                        combinedWith.add(p.trim());
                }

                cls.addSubject(subject, lectures, combinedWith);
            }

            int numFixed = getValidInt(sc, "Enter number of fixed government periods for class " + className + ": ");
            for (int j = 0; j < numFixed; j++) {
                System.out.print("Enter day (Monday-Friday): ");
                String day = sc.nextLine();
                int period = getValidInt(sc, "Enter period number: ");
                System.out.print("Enter subject name: ");
                String subject = sc.nextLine();
                System.out.print("Enter teacher name: ");
                String teacher = sc.nextLine();
                cls.addFixedPeriod(day, period, subject, teacher);
            }
            classes.add(cls);
        }

        // Input Teachers
        int numTeachers = getValidInt(sc, "Enter number of teachers: ");

        List<Teacher> teachers = new ArrayList<>();

        for (int i = 0; i < numTeachers; i++) {
            System.out.print("Enter teacher name: ");
            String tName = sc.nextLine();
            Teacher t = new Teacher(tName);

            int ts = getValidInt(sc, "Enter number of subjects this teacher teaches: ");

            for (int j = 0; j < ts; j++) {
                System.out.print("Enter class name for this subject: ");
                String clsName = sc.nextLine();
                System.out.print("Enter subject name: ");
                String subName = sc.nextLine();
                t.addTeaches(clsName, subName);
            }

            int na = getValidInt(sc, "Enter number of non-available slots: ");

            for (int j = 0; j < na; j++) {
                System.out.print("Enter day (Monday-Friday): ");
                String day = sc.nextLine();
                int numPeriods = getValidInt(sc, "Enter number of periods not available on " + day + ": ");
                List<Integer> periods = new ArrayList<>();
                for (int k = 0; k < numPeriods; k++) {
                    periods.add(getValidInt(sc, "Enter period number: "));
                }
                t.addNotAvailable(day, periods);
            }
            teachers.add(t);
        }

        TimetableGenerator tg = new TimetableGenerator(days, periodsPerDay, teachers, classes);

        if (tg.generate()) {
            tg.printTimetable();

            System.out.print("Do you want to export to CSV? (y/n): ");
            String export = sc.nextLine();
            if (export.equalsIgnoreCase("y")) {
                CsvExporter.export(tg, "timetable.csv");
            }
        } else
            System.out.println("No valid timetable possible with given constraints.");

        sc.close();
    }

    private static int getValidInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine();
            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }
}
