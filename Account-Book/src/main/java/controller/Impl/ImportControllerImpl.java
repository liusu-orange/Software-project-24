package controller.Impl;

import model.CSVImporter;
import model.Entry;
import util.DeepseekClassify;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ImportControllerImpl {

    private final UserControllerImpl userController;
    private String CSV_FILE;

    public ImportControllerImpl(UserControllerImpl userController) {
        this.userController = userController;
        updateCsvFilePath();
    }

    private void updateCsvFilePath() {
        CSV_FILE = userController.getCurrentUserFinanceFilePath();
    }

//    public List<Entry> loadEntries() {
//        List<Entry> entries = new ArrayList<>();
//
//        updateCsvFilePath();
//        File file = new File(CSV_FILE);
//        if (!file.exists()) return entries;
//
//        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
//            br.readLine(); // 跳过表头
//            String line;
//            while ((line = br.readLine()) != null) {
//                String[] fields = line.split(",");
//                if (fields.length == 4) {
//                    entries.add(new Entry(fields[0], Double.parseDouble(fields[1]), fields[2], fields[3]));
//                }
//            }
//        } catch (IOException ignored) {}
//        return entries;
//    }
public List<Entry> loadEntries() {
    List<Entry> entries = new ArrayList<>();
    updateCsvFilePath();
    File file = new File(CSV_FILE);

    if (!file.exists() || file.length() == 0) {
        return entries; // 空文件直接返回空列表
    }

    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        br.readLine(); // 跳过表头
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] fields = line.split(",");
            if (fields.length == 4) {
                try {
                    entries.add(new Entry(
                            fields[0],
                            Double.parseDouble(fields[1]),
                            fields[2],
                            fields[3]
                    ));
                } catch (NumberFormatException e) {
                    System.err.println("跳过无效数据行: " + line);
                }
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
    return entries;
}

    public void addEntry(Entry entry) {

        updateCsvFilePath();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE, true))) {
            writer.write(String.format("%s,%.2f,%s,%s\n",
                    entry.getDate(),
                    entry.getAmount(),
                    entry.getCategory(),
                    entry.getDescription()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Entry> importCSV(JPanel parent) {
        JFileChooser chooser = new JFileChooser();
        List<Entry> entries = new ArrayList<>();
        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            CSVImporter importer = new CSVImporter();
            entries = importer.importCSV(chooser.getSelectedFile().getPath());
            rewriteCSV(entries);
        }
        return entries;
    }

    public void exportCSV(DefaultTableModel model, JPanel parent) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
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
                JOptionPane.showMessageDialog(parent, "导出失败");
            }
        }
    }

    public void analyzeWithAI(DefaultTableModel model, JPanel parent) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    updateCsvFilePath();
                    DeepseekClassify ai = new DeepseekClassify(userController);
                    for (int i = 0; i < model.getRowCount(); i++) {
                        String desc = (String) model.getValueAt(i, 3);
                        String category = ai.classifyDescription(desc);
                        model.setValueAt(category, i, 2);
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(parent, "AI分析失败: " + ex.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                rewriteCSV(model);
            }
        }.execute();
    }

//    public void rewriteCSV(DefaultTableModel model) {
//
//        updateCsvFilePath();
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE))) {
//            writer.write("Date,Amount,Category,Description\n");
//
//            for (int i = 0; i < model.getRowCount(); i++) {
//                String line = String.join(",",
//                        model.getValueAt(i, 0).toString(),
//                        model.getValueAt(i, 1).toString(),
//                        model.getValueAt(i, 2).toString(),
//                        model.getValueAt(i, 3).toString()
//                );
//                writer.write(line + "\n");
//            }
//        } catch (IOException ex) {
//            JOptionPane.showMessageDialog(null, "CSV更新失败: " + ex.getMessage());
//        }
//    }
public void rewriteCSV(DefaultTableModel model) {
    updateCsvFilePath();
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE))) {
        writer.write("Date,Amount,Category,Description\n");

        for (int i = 0; i < model.getRowCount(); i++) {
            writer.write(String.format("%s,%.2f,%s,%s\n",
                    model.getValueAt(i, 0),
                    model.getValueAt(i, 1),
                    model.getValueAt(i, 2),
                    model.getValueAt(i, 3)
            ));
        }
    } catch (IOException ex) {
        JOptionPane.showMessageDialog(null, "CSV保存失败: " + ex.getMessage());
    }
}


    public void rewriteCSV(List<Entry> entries) {

        updateCsvFilePath();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE))) {
            writer.write("Date,Amount,Category,Description\n");
            for (Entry entry : entries) {
                writer.write(String.format("%s,%.2f,%s,%s\n",
                        entry.getDate(),
                        entry.getAmount(),
                        entry.getCategory(),
                        entry.getDescription()));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}