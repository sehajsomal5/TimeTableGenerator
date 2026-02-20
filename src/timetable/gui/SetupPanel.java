package timetable.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class SetupPanel extends JPanel {
    private TimetableGUI parent;
    private JSpinner periodsSpinner;
    private JCheckBox mon, tue, wed, thu, fri, sat, sun;

    public SetupPanel(TimetableGUI parent) {
        this.parent = parent;
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Header
        JLabel header = new JLabel("Step 1: Basic Setup", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        add(header, BorderLayout.NORTH);

        // Center Content
        JPanel center = new JPanel(new GridLayout(0, 1, 10, 10));

        // Periods
        JPanel pPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pPanel.add(new JLabel("Periods per Day:"));
        periodsSpinner = new JSpinner(new SpinnerNumberModel(8, 1, 15, 1));
        pPanel.add(periodsSpinner);
        center.add(pPanel);

        // Days
        JPanel dPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        center.add(new JLabel("Select Working Days:", SwingConstants.CENTER));

        mon = new JCheckBox("Mon", true);
        tue = new JCheckBox("Tue", true);
        wed = new JCheckBox("Wed", true);
        thu = new JCheckBox("Thu", true);
        fri = new JCheckBox("Fri", true);
        sat = new JCheckBox("Sat", false);
        sun = new JCheckBox("Sun", false);

        dPanel.add(mon);
        dPanel.add(tue);
        dPanel.add(wed);
        dPanel.add(thu);
        dPanel.add(fri);
        dPanel.add(sat);
        dPanel.add(sun);
        center.add(dPanel);

        add(center, BorderLayout.CENTER);

        // Navigation
        JButton nextBtn = new JButton("Next: Add Classes >");
        nextBtn.setFont(new Font("Arial", Font.BOLD, 20));
        nextBtn.setBackground(new Color(100, 200, 100));
        nextBtn.addActionListener((ActionEvent e) -> {
            saveData();
            parent.showCard("Classes");
        });

        JPanel bottom = new JPanel();
        bottom.add(nextBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    private void saveData() {
        parent.periodsPerDay = (int) periodsSpinner.getValue();
        parent.days.clear();
        if (mon.isSelected())
            parent.days.add("Monday");
        if (tue.isSelected())
            parent.days.add("Tuesday");
        if (wed.isSelected())
            parent.days.add("Wednesday");
        if (thu.isSelected())
            parent.days.add("Thursday");
        if (fri.isSelected())
            parent.days.add("Friday");
        if (sat.isSelected())
            parent.days.add("Saturday");
        if (sun.isSelected())
            parent.days.add("Sunday");
    }
}
