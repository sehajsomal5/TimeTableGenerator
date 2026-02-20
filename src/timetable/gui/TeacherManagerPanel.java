package timetable.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import timetable.Teacher;

public class TeacherManagerPanel extends JPanel {

    private TimetableGUI parent;
    private DefaultListModel<String> teacherListModel;
    private JList<String> teacherList;
    private DefaultTableModel teachesModel;
    private DefaultListModel<String> unavailabilityModel; // Model for the unavailability list
    private Teacher selectedTeacher;

    public TeacherManagerPanel(TimetableGUI parent) {
        this.parent = parent;
        setLayout(new BorderLayout(10, 10));

        // Header
        JLabel header = new JLabel("Step 3: Manage Teachers", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        add(header, BorderLayout.NORTH);

        // Split Pane
        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.3);

        // Left: Teacher List
        JPanel leftPanel = new JPanel(new BorderLayout());
        teacherListModel = new DefaultListModel<>();
        teacherList = new JList<>(teacherListModel);
        teacherList.addListSelectionListener(e -> updateRightPanel());
        leftPanel.add(new JScrollPane(teacherList), BorderLayout.CENTER);

        JButton addTeacherBtn = new JButton("Add Teacher");
        addTeacherBtn.addActionListener(e -> addTeacher());
        leftPanel.add(addTeacherBtn, BorderLayout.SOUTH);

        splitPane.setLeftComponent(leftPanel);

        // Right: Details (Teaches & Unavailability)
        JTabbedPane rightTabbedPane = new JTabbedPane();

        // Tab 1: Subjects Taught
        JPanel teachesPanel = new JPanel(new BorderLayout());
        String[] cols = { "Class", "Subject" };
        teachesModel = new DefaultTableModel(cols, 0);
        JTable teachesTable = new JTable(teachesModel);
        teachesPanel.add(new JScrollPane(teachesTable), BorderLayout.CENTER);

        JButton assignBtn = new JButton("Assign Subject");
        assignBtn.addActionListener(e -> assignSubject());
        teachesPanel.add(assignBtn, BorderLayout.SOUTH);

        rightTabbedPane.addTab("Subjects Taught", teachesPanel);

        // Tab 2: Unavailable Times
        JPanel naPanel = new JPanel(new BorderLayout());
        unavailabilityModel = new DefaultListModel<>();
        JList<String> unavailabilityList = new JList<>(unavailabilityModel);
        naPanel.add(new JScrollPane(unavailabilityList), BorderLayout.CENTER);

        JButton addNaBtn = new JButton("Add Unavailability");
        addNaBtn.addActionListener(e -> addUnavailability());
        naPanel.add(addNaBtn, BorderLayout.SOUTH);

        rightTabbedPane.addTab("Unavailable Periods", naPanel);

        splitPane.setRightComponent(rightTabbedPane);
        add(splitPane, BorderLayout.CENTER);

        // Navigation
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton backBtn = new JButton("< Back");
        backBtn.addActionListener(e -> parent.showCard("Classes"));

        JButton nextBtn = new JButton("Next: GENERATE TIMETABLE >");
        nextBtn.setBackground(new Color(100, 200, 255));
        nextBtn.setFont(new Font("Arial", Font.BOLD, 20));
        nextBtn.addActionListener(e -> parent.showCard("Results"));

        bottom.add(backBtn);
        bottom.add(nextBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    private void addTeacher() {
        String name = JOptionPane.showInputDialog(this, "Enter Teacher Name:");
        if (name != null && !name.trim().isEmpty()) {
            Teacher t = new Teacher(name.trim());
            parent.teachers.add(t);
            teacherListModel.addElement(t.name);
            teacherList.setSelectedValue(t.name, true);
        }
    }

    private void updateRightPanel() {
        int idx = teacherList.getSelectedIndex();
        if (idx == -1) {
            selectedTeacher = null;
            teachesModel.setRowCount(0);
            unavailabilityModel.clear();
            return;
        }
        selectedTeacher = parent.teachers.get(idx);

        // Update Subjects
        teachesModel.setRowCount(0);
        for (Map.Entry<String, Set<String>> entry : selectedTeacher.teaches.entrySet()) {
            String className = entry.getKey();
            for (String subject : entry.getValue()) {
                teachesModel.addRow(new Object[] { className, subject });
            }
        }

        // Update Unavailability
        unavailabilityModel.clear();
        for (Map.Entry<String, Set<Integer>> entry : selectedTeacher.notAvailable.entrySet()) {
            String day = entry.getKey();
            Set<Integer> periods = entry.getValue();
            unavailabilityModel.addElement(day + ": Periods " + periods.toString());
        }
    }

    private void assignSubject() {
        if (selectedTeacher == null)
            return;

        // ComboBox for Classes
        JComboBox<String> classBox = new JComboBox<>();
        for (timetable.ClassRoom c : parent.classes)
            classBox.addItem(c.name);

        JTextField subField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Select Class:"));
        panel.add(classBox);
        panel.add(new JLabel("Subject Name (must match exactly):"));
        panel.add(subField);

        int res = JOptionPane.showConfirmDialog(this, panel, "Assign Subject", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String cls = (String) classBox.getSelectedItem();
            String sub = subField.getText().trim();
            if (cls != null && !sub.isEmpty()) {
                selectedTeacher.addTeaches(cls, sub);
                updateRightPanel();
            }
        }
    }

    private void addUnavailability() {
        if (selectedTeacher == null) {
            JOptionPane.showMessageDialog(this, "Please select a teacher first!");
            return;
        }

        // Dialog Components
        JComboBox<String> dayBox = new JComboBox<>(parent.days.toArray(new String[0]));

        JPanel periodsPanel = new JPanel(new GridLayout(0, 4)); // 4 columns
        List<JCheckBox> boxes = new ArrayList<>();
        for (int i = 1; i <= parent.periodsPerDay; i++) {
            JCheckBox box = new JCheckBox("Period " + i);
            boxes.add(box);
            periodsPanel.add(box);
        }

        JPanel container = new JPanel(new BorderLayout());
        container.add(new JLabel("Select Day:"), BorderLayout.NORTH);
        container.add(dayBox, BorderLayout.CENTER);
        container.add(new JLabel("Select Unavailable Periods:"), BorderLayout.SOUTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(container, BorderLayout.NORTH);
        mainPanel.add(periodsPanel, BorderLayout.CENTER); // Will show boxes below

        int result = JOptionPane.showConfirmDialog(this, mainPanel, "Set Unavailability",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String selectedDay = (String) dayBox.getSelectedItem();
            List<Integer> periods = new ArrayList<>();
            for (int i = 0; i < boxes.size(); i++) {
                if (boxes.get(i).isSelected()) {
                    periods.add(i + 1); // 1-indexed
                }
            }

            if (!periods.isEmpty()) {
                selectedTeacher.addNotAvailable(selectedDay, periods);
                updateRightPanel();
            }
        }
    }
}
