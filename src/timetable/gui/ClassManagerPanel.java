package timetable.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import timetable.ClassRoom;

public class ClassManagerPanel extends JPanel {

    private TimetableGUI parent;
    private DefaultListModel<String> classListModel;
    private JList<String> classList;
    private DefaultTableModel subjectTableModel;
    private JTable subjectTable;
    private ClassRoom selectedClass;

    public ClassManagerPanel(TimetableGUI parent) {
        this.parent = parent;
        setLayout(new BorderLayout(10, 10));

        // Heading
        JLabel header = new JLabel("Step 2: Manage Classes", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        add(header, BorderLayout.NORTH);

        // Split Pane
        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.3);

        // Left: Class List
        JPanel leftPanel = new JPanel(new BorderLayout());
        classListModel = new DefaultListModel<>();
        classList = new JList<>(classListModel);
        classList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        classList.addListSelectionListener(e -> updateRightPanel());
        leftPanel.add(new JScrollPane(classList), BorderLayout.CENTER);

        JButton addClassBtn = new JButton("Add Class");
        addClassBtn.addActionListener(e -> addClass());
        leftPanel.add(addClassBtn, BorderLayout.SOUTH);

        splitPane.setLeftComponent(leftPanel);

        // Right: Class Details
        JPanel rightPanel = new JPanel(new BorderLayout());
        String[] columns = { "Subject", "Lectures/Week", "Combined With" };
        subjectTableModel = new DefaultTableModel(columns, 0);
        subjectTable = new JTable(subjectTableModel);

        rightPanel.add(new JScrollPane(subjectTable), BorderLayout.CENTER);

        JButton addSubjectBtn = new JButton("Add Subject to Selected Class");
        addSubjectBtn.addActionListener(e -> addSubject());
        rightPanel.add(addSubjectBtn, BorderLayout.SOUTH);

        splitPane.setRightComponent(rightPanel);
        add(splitPane, BorderLayout.CENTER);

        // Navigation
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton backBtn = new JButton("< Back");
        backBtn.addActionListener(e -> parent.showCard("Setup"));

        JButton nextBtn = new JButton("Next: Teachers >");
        nextBtn.setBackground(new Color(100, 200, 100));
        nextBtn.setFont(new Font("Arial", Font.BOLD, 20)); // Make it obvious
        nextBtn.addActionListener(e -> parent.showCard("Teachers"));

        bottom.add(backBtn);
        bottom.add(nextBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    private void addClass() {
        String name = JOptionPane.showInputDialog(this, "Enter Class Name (e.g. 10A):");
        if (name != null && !name.trim().isEmpty()) {
            ClassRoom c = new ClassRoom(name.trim());
            parent.classes.add(c);
            classListModel.addElement(c.name);
            classList.setSelectedValue(c.name, true);
        }
    }

    private void updateRightPanel() {
        int idx = classList.getSelectedIndex();
        if (idx == -1) {
            selectedClass = null;
            subjectTableModel.setRowCount(0);
            return;
        }

        selectedClass = parent.classes.get(idx);
        subjectTableModel.setRowCount(0);

        for (Map.Entry<String, Integer> entry : selectedClass.subjectsPerWeek.entrySet()) {
            String subName = entry.getKey();
            int lectures = entry.getValue();
            java.util.List<String> combined = selectedClass.combinedSubjects.get(subName);
            String combinedStr = (combined == null || combined.isEmpty()) ? "None" : String.join(", ", combined);

            subjectTableModel.addRow(new Object[] { subName, lectures, combinedStr });
        }
    }

    private void addSubject() {
        if (selectedClass == null) {
            JOptionPane.showMessageDialog(this, "Please select a class first!");
            return;
        }

        // Custom Dialog for Subject Input
        JTextField subNameField = new JTextField();
        JSpinner lecSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 10, 1));
        JTextField combinedField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Subject Name:"));
        panel.add(subNameField);
        panel.add(new JLabel("Lectures per Week:"));
        panel.add(lecSpinner);
        panel.add(new JLabel("Combined with (comma separated class names, optional):"));
        panel.add(combinedField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Subject",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String sub = subNameField.getText().trim();
            if (sub.isEmpty())
                return;

            int lectures = (int) lecSpinner.getValue();
            String combinedStr = combinedField.getText().trim();
            java.util.List<String> combinedList = new ArrayList<>();

            if (!combinedStr.isEmpty()) {
                for (String part : combinedStr.split(",")) {
                    combinedList.add(part.trim());
                }
            }

            selectedClass.addSubject(sub, lectures, combinedList);
            updateRightPanel();
        }
    }
}
