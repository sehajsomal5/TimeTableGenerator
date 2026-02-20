package timetable.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import timetable.*;

public class TimetableGUI extends JFrame {

    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);

    // Shared Data
    public int periodsPerDay = 8;
    public java.util.List<String> days = new ArrayList<>();
    public java.util.List<ClassRoom> classes = new ArrayList<>();
    public java.util.List<Teacher> teachers = new ArrayList<>();

    private SetupPanel setupPanel;
    private ClassManagerPanel classPanel;
    private TeacherManagerPanel teacherPanel;
    private ResultPanel resultPanel;

    public TimetableGUI() {
        super("Easy Timetable Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        // Initialize Panels
        setupPanel = new SetupPanel(this);
        classPanel = new ClassManagerPanel(this);
        teacherPanel = new TeacherManagerPanel(this);
        resultPanel = new ResultPanel(this);

        mainPanel.add(setupPanel, "Setup");
        mainPanel.add(classPanel, "Classes");
        mainPanel.add(teacherPanel, "Teachers");
        mainPanel.add(resultPanel, "Results");

        add(mainPanel);

        // Default Days
        days.add("Monday");
        days.add("Tuesday");
        days.add("Wednesday");
        days.add("Thursday");
        days.add("Friday");
    }

    public void showCard(String name) {
        cardLayout.show(mainPanel, name);
    }

    public static void setLargeFont(Component comp) {
        comp.setFont(new Font("Arial", Font.PLAIN, 18));
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                setLargeFont(child);
            }
        }
    }

    public static void main(String[] args) {
        // Set Look and Feel for better visuals
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // Global larger font
            java.util.Enumeration keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);
                if (value instanceof javax.swing.plaf.FontUIResource) {
                    UIManager.put(key, new javax.swing.plaf.FontUIResource("Arial", Font.PLAIN, 18));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new TimetableGUI().setVisible(true);
        });
    }
}
