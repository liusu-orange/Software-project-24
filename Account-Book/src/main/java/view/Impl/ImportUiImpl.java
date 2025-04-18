package view.Impl;

import view.ImportUi;

import org.example.CSVImporter;
import org.example.DeepseekTestMain;
import org.example.Entry;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;

public class ImportUiImpl implements ImportUi{
    private JPanel contentPanel;
    private JTable table;
    private DefaultTableModel model;

    //WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING 以下路径需调整为个人路径 或新建一个csv文件
    private static final String CSV_FILE = "D:\\StudySoftware\\java practice\\Integration\\Account-Book\\src\\main\\java\\org\\example\\finance_data.csv";
    private JTextField dateField, amountField, categoryField, descriptionField;


    public ImportUiImpl(JPanel contentPanel) {
        this.contentPanel = contentPanel;
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

    public void ImportWindow() {
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());

        // 表格初始化
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(table);

        // 输入面板
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

        // 功能按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        String[] btnLabels = {"Add Entry", "Import CSV", "Export CSV", "Delete Entry", "AI Analyze All"};
        for (String label : btnLabels) {
            JButton btn = new JButton(label);
            btn.addActionListener(createActionListener(label));
            buttonPanel.add(btn);
        }

        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(inputPanel, BorderLayout.NORTH);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        loadCSV();
        //contentPanel.revalidate();
        //contentPanel.repaint();
    }

    private ActionListener createActionListener(String label) {
        return e -> {
            try {
                switch (label) {
                    case "Add Entry":
                        addEntry();
                        break;
                    case "Import CSV":
                        importCSV();
                        break;
                    case "Export CSV":
                        exportCSV();
                        break;
                    case "Delete Entry":
                        deleteEntry();
                        break;
                    case "AI Analyze All":
                        aiAnalyze();
                        break;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(contentPanel, "操作失败: " + ex.getMessage());
            }
        };
    }

    private void addEntry() {
        if (validateInput()) {
            model.addRow(new Object[]{
                    dateField.getText(),
                    Double.parseDouble(amountField.getText()),
                    categoryField.getText(),
                    descriptionField.getText()
            });
            String date = dateField.getText();
            double amount = Double.parseDouble(amountField.getText());
            String category = categoryField.getText();
            String description = descriptionField.getText();
            appendToCSV(date, amount, category, description);
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

    private void importCSV() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(contentPanel) == JFileChooser.APPROVE_OPTION) {
            CSVImporter importer = new CSVImporter();
            List<Entry> entries = importer.importCSV(chooser.getSelectedFile().getPath());

            entries.forEach(entry -> model.addRow(new Object[]{
                    entry.getDate(),
                    entry.getAmount(),
                    entry.getCategory(),
                    entry.getDescription()
            }));
            rewriteCSV();
        }
    }

    private void aiAnalyze() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    DeepseekTestMain ai = new DeepseekTestMain();
                    for (int i = 0; i < model.getRowCount(); i++) {
                        String desc = (String) model.getValueAt(i, 3);
                        String category = ai.classifyDescription(desc);
                        model.setValueAt(category, i, 2);
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(contentPanel, "AI分析失败: " + ex.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                rewriteCSV();
            }
        }.execute();
    }

    private void loadCSV() {
        File file = new File(CSV_FILE);

//        System.out.println("调试信息：");
//        System.out.println("绝对路径: " + file.getAbsolutePath());
//        System.out.println("是否存在: " + file.exists());
//        System.out.println("是文件吗: " + file.isFile());
//        System.out.println("是目录吗: " + file.isDirectory());
//        System.out.println("可读性: " + file.canRead());

        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                // Skip header
                br.readLine();
                while ((line = br.readLine()) != null) {
                    String[] fields = line.split(",");
                    if (fields.length == 4) {
                        String date = fields[0];
                        double amount = Double.parseDouble(fields[1]);
                        String category = fields[2];
                        String description = fields[3];
                        model.addRow(new Object[]{date, amount, category, description});
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(contentPanel, "加载数据失败");
            }
        }
    }

    private void appendToCSV(String date, double amount, String category, String description) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE, true))) {
            writer.write(String.format("%s,%.2f,%s,%s\n", date, amount, category, description));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(contentPanel, "\"Error appending data to CSV file.");
        }
    }

    private void rewriteCSV() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE))) {
            writer.write("Date,Amount,Category,Description\n");
            for (int i = 0; i < model.getRowCount(); i++) {
                writer.write(String.format("%s,%.2f,%s,%s\n",
                        model.getValueAt(i, 0),
                        (Double) model.getValueAt(i, 1),
                        model.getValueAt(i, 2),
                        model.getValueAt(i, 3)
                ));
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(contentPanel, "更新失败");
        }
    }

    private void exportCSV() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(contentPanel) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getAbsolutePath() + ".csv");
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("Date,Amount,Category,Description\n");
                for (int i = 0; i < model.getRowCount(); i++) {
                    writer.write(String.format("%s,%.2f,%s,%s\n",
                            model.getValueAt(i, 0),
                            (Double) model.getValueAt(i, 1),
                            model.getValueAt(i, 2),
                            model.getValueAt(i, 3)
                    ));
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(contentPanel, "导出失败");
            }
        }
    }

    private void deleteEntry() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            model.removeRow(selectedRow);
            rewriteCSV();
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
