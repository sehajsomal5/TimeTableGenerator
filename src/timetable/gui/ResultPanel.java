package timetable.gui;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import timetable.TimetableGenerator;
import timetable.CsvExporter;
import timetable.TimetableEntry;

public class ResultPanel extends JPanel {

    private TimetableGUI parent;
    private JTextArea resultArea;
    private JButton generateBtn;
    private JButton exportBtn;
    private TimetableGenerator generator;

    public ResultPanel(TimetableGUI parent) {
        this.parent = parent;
        setLayout(new BorderLayout(10, 10));

        // Heading
        JLabel header = new JLabel("Step 4: Generate Timetable", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        add(header, BorderLayout.NORTH);

        // Center: Result Display
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        // Bottom: Action Buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20)); // Spacing

        generateBtn = new JButton("GENERATE TIMETABLE");
        generateBtn.setBackground(new Color(50, 150, 50));
        generateBtn.setForeground(Color.WHITE);
        generateBtn.setFont(new Font("Arial", Font.BOLD, 24));
        generateBtn.addActionListener(e -> generateTimetable());

        exportBtn = new JButton("Export to CSV");
        exportBtn.setFont(new Font("Arial", Font.BOLD, 18));
        exportBtn.setEnabled(false);
        exportBtn.addActionListener(e -> exportToCsv());

        bottom.add(generateBtn);
        bottom.add(exportBtn);

        add(bottom, BorderLayout.SOUTH);

        // Navigation (Restart)
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton restartBtn = new JButton("Start Over");
        restartBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure? This will clear all data.", "Restart",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                parent.classes.clear();
                parent.teachers.clear();
                // Reset panels if needed (implementation omitted for brevity in "easy" version)
                parent.showCard("Setup");
            }
        });
        navPanel.add(restartBtn);
        add(navPanel, BorderLayout.WEST); // Add to specific area or integrated
    }

    private void generateTimetable() {
        if (parent.classes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No classes added! Go back to Step 2.");
            return;
        }

        resultArea.setText("Generating... please wait...");
        exportBtn.setEnabled(false);

        // Run in background thread to keep UI responsive
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                generator = new TimetableGenerator(parent.days, parent.periodsPerDay, parent.teachers, parent.classes);
                return generator.generate();
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        displayResults();
                        exportBtn.setEnabled(true);
                        JOptionPane.showMessageDialog(ResultPanel.this, "Success! Timetable generated.");
                    } else {
                        resultArea.setText("Failed to generate a valid timetable with the given constraints.\n" +
                                "Try increasing periods per day or adding more teachers.");
                        JOptionPane.showMessageDialog(ResultPanel.this, "Generation Failed!", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    resultArea.setText("Error occurred: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void displayResults() {
        StringBuilder sb = new StringBuilder();
        for (timetable.ClassRoom cls : parent.classes) {
            sb.append("Timetable for ").append(cls.name).append(":\n");
            for (int d = 0; d < parent.days.size(); d++) {
                String day = parent.days.get(d);
                sb.append(String.format("%-10s", day)).append(": ");
                for (int p = 1; p <= parent.periodsPerDay; p++) {
                    int key = p + d * 100;
                    TimetableEntry entry = generator.classSchedule.get(cls.name).get(key);
                    if (entry != null) {
                        sb.append("[").append(entry.subject).append(" (").append(entry.teacherName).append(")] ");
                    } else {
                        sb.append("[ FREE ] ");
                    }
                }
                sb.append("\n");
            }
            sb.append("--------------------------------------------------\n");
        }
        resultArea.setText(sb.toString());
        resultArea.setCaretPosition(0);
    }

    private void exportToCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Timetable CSV");
        fileChooser.setSelectedFile(new File("timetable.csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                CsvExporter.export(generator, fileToSave.getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Saved to " + fileToSave.getAbsolutePath());
                // Open file
                Desktop.getDesktop().open(fileToSave);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage());
            } catch (Exception e) {
                // ignore open error
                JOptionPane.showMessageDialog(this, "Saved, but could not open file automatically.");
            }
        }
    }
}
