package view.Impl;

import controller.Impl.ImportControllerImpl;
import controller.Impl.UserControllerImpl;
import view.ImportUi;
import model.Entry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * UI implementation for importing/exporting financial data and managing entries in a table.
 *
 * @author Guanren Huang Yi Zhong
 * @version 1.2.0
 * @since v1.0.0
 */
public class ImportUiImpl implements ImportUi {
    private JPanel contentPanel;
    private JTable table;
    private DefaultTableModel model;
    private JTextField dateField, amountField, categoryField, descriptionField;

    private ImportControllerImpl controller;
    private UserControllerImpl userController;

    /**
     * Initializes the import UI with user controller and data model.
     * @param contentPanel Parent container panel
     * @param userController User controller instance for authentication
     */
    public ImportUiImpl(JPanel contentPanel, UserControllerImpl userController) {
        this.contentPanel = contentPanel;
        this.userController = userController; // 初始化UserController
        this.controller = new ImportControllerImpl(userController); // 传入UserController
        initializeModel();
    }

private void initializeModel() {
    if (sharedModel == null) {
        sharedModel = new DefaultTableModel(new Object[]{"Date", "Amount", "Category", "Description"}, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 1 ? Double.class : String.class;
            }

            @Override
            public Object getValueAt(int row, int column) {
                Object value = super.getValueAt(row, column);
                return value == null ? "" : value;
            }
        };

        // 仅加载CSV中的真实数据，不添加空行
        List<Entry> initialData = controller.loadEntries();
        for (Entry e : initialData) {
            sharedModel.addRow(new Object[]{
                    e.getDate(),
                    e.getAmount(),
                    e.getCategory(),
                    e.getDescription()
            });
        }
    }
    this.model = sharedModel;
}

    /**
     * Displays the import/export window with data table and input fields.
     */
    @Override
    public void ImportWindow() {
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());

        table = new JTable(model);
        table.setAutoCreateRowSorter(true);

        // 添加单元格编辑后的自动保存功能
        table.getModel().addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                autoSave();
            }
        });


        JScrollPane scrollPane = new JScrollPane(table);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1
                    && model.getRowCount() > 0) {
                int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                if (model.getValueAt(modelRow, 0) != null) {
                    dateField.setText(model.getValueAt(modelRow, 0).toString());
                    amountField.setText(model.getValueAt(modelRow, 1).toString());
                    categoryField.setText(model.getValueAt(modelRow, 2).toString());
                    descriptionField.setText(model.getValueAt(modelRow, 3).toString());
                }
            }
        });

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
    }

    /**
     * Saves table changes to CSV automatically when data is updated.
     */
    public void autoSave() {
        // 增加有效性检查
        if (model.getRowCount() > 0 && hasValidData()) {
            controller.rewriteCSV(model);
        }
    }

    private boolean hasValidData() {
        for (int i = 0; i < model.getRowCount(); i++) {
            Object date = model.getValueAt(i, 0);
            Object amount = model.getValueAt(i, 1);
            if (date != null && !date.toString().isEmpty()
                    && amount != null) {
                return true;
            }
        }
        return false;
    }


    /**
     * Creates action listeners for import/export and analysis buttons.
     * @param label Button label to determine action
     * @return ActionListener for the specified button
     */
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
                JOptionPane.showMessageDialog(contentPanel, "Operation failure: " + ex.getMessage());
            }
        };
    }

    /**
     * Adds a new financial entry from input fields to the table and CSV.
     */
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

    /**
     * Validates input fields before adding a new entry.
     * @return True if input is valid, false otherwise
     */
    private boolean validateInput() {
        if (dateField.getText().isEmpty() || amountField.getText().isEmpty()
                || descriptionField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(contentPanel, "The required fields cannot be blank");
            return false;
        }
        try {
            Double.parseDouble(amountField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(contentPanel, "The amount format is incorrect.");
            return false;
        }
        return true;
    }

    /**
     * Deletes selected entries from the table and updates the CSV.
     */
    private void deleteEntry() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            model.removeRow(selectedRow);
            controller.rewriteCSV(model);
        } else {
            JOptionPane.showMessageDialog(contentPanel, "Please select the rows to be deleted first");
        }
    }

    private DefaultTableModel sharedModel;

    /**
     * Clears all input fields after adding an entry.
     */
    private void clearFields() {
        dateField.setText("");
        amountField.setText("");
        categoryField.setText("");
        descriptionField.setText("");
    }
}
