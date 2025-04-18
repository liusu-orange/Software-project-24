package view.Impl;

import controller.Impl.ImportControllerImpl;
import view.ImportUi;
import model.Entry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class ImportUiImpl implements ImportUi {
    private JPanel contentPanel;
    private JTable table;
    private DefaultTableModel model;
    private JTextField dateField, amountField, categoryField, descriptionField;

    private ImportControllerImpl controller;

    public ImportUiImpl(JPanel contentPanel) {
        this.contentPanel = contentPanel;
        this.controller = new ImportControllerImpl();
        initializeModel();
    }

    private void initializeModel() {
        model = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 1 ? Double.class : String.class;
            }
        };
        model.addColumn("Date");
        model.addColumn("Amount");
        model.addColumn("Category");
        model.addColumn("Description");
    }

    @Override
    public void ImportWindow() {
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());

        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        dateField = new JTextField();
        amountField = new JTextField();
        categoryField = new JTextField();
        descriptionField = new JTextField();

        inputPanel.add(new JLabel("Date:"));
        inputPanel.add(dateField);
        inputPanel.add(new JLabel("Amount:"));
        inputPanel.add(amountField);
        inputPanel.add(new JLabel("Category:"));
        inputPanel.add(categoryField);
        inputPanel.add(new JLabel("Description:"));
        inputPanel.add(descriptionField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        String[] btnLabels = {"Add Entry", "Delete Entry", "Export CSV", "Import CSV", "AI Analyze All"};
        for (String label : btnLabels) {
            JButton btn = new JButton(label);
            btn.addActionListener(createActionListener(label));
            buttonPanel.add(btn);
        }

        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(inputPanel, BorderLayout.NORTH);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        List<Entry> initialData = controller.loadEntries();
        for (Entry e : initialData) {
            model.addRow(new Object[]{e.getDate(), e.getAmount(), e.getCategory(), e.getDescription()});
        }
    }

    private ActionListener createActionListener(String label) {
        return e -> {
            try {
                switch (label) {
                    case "Add Entry" -> addEntry();
                    case "Delete Entry" -> deleteEntry();
                    case "Export CSV" -> controller.exportCSV(model, contentPanel);
                    case "Import CSV" -> {
                        List<Entry> entries = controller.importCSV(contentPanel);
                        for (Entry entry : entries) {
                            model.addRow(new Object[]{entry.getDate(), entry.getAmount(), entry.getCategory(), entry.getDescription()});
                        }
                    }
                    case "AI Analyze All" -> {
                        controller.analyzeWithAI(model, contentPanel);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(contentPanel, "操作失败: " + ex.getMessage());
            }
        };
    }

    private void addEntry() {
        if (validateInput()) {
            Entry entry = new Entry(
                    dateField.getText(),
                    Double.parseDouble(amountField.getText()),
                    categoryField.getText(),
                    descriptionField.getText()
            );
            model.addRow(new Object[]{entry.getDate(), entry.getAmount(), entry.getCategory(), entry.getDescription()});
            controller.addEntry(entry);
            clearFields();
        }
    }

    private boolean validateInput() {
        if (dateField.getText().isEmpty() || amountField.getText().isEmpty()
                || descriptionField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(contentPanel, "必填字段不能为空");
            return false;
        }
        try {
            Double.parseDouble(amountField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(contentPanel, "金额格式错误");
            return false;
        }
        return true;
    }

    private void deleteEntry() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            model.removeRow(selectedRow);
            controller.rewriteCSV(model);
        } else {
            JOptionPane.showMessageDialog(contentPanel, "请先选择要删除的行");
        }
    }

    private void clearFields() {
        dateField.setText("");
        amountField.setText("");
        categoryField.setText("");
        descriptionField.setText("");
    }
}
