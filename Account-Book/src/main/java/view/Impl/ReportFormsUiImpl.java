package view.Impl;

import view.ReportFormsUi;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.*;
import java.awt.*;

public class ReportFormsUiImpl implements ReportFormsUi {
    private JPanel contentPanel;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public ReportFormsUiImpl(JPanel contentPanel) {
        this.contentPanel = contentPanel;
    }

    public void ReportFormsWindow() {
        try {
            // 读取 CSV 文件数据，这里记得改自己电脑的绝对路径
            List<Record> records = readCSV("D:\\mycode\\Software-project\\Account-Book\\src\\main\\resources\\finance_data.csv");

            // 按日期分组数据
            Map<Date, List<Record>> recordsByDate = groupRecordsByDate(records);

            // 创建数据集
            DefaultCategoryDataset barDataset = createBarDataset(recordsByDate);
            DefaultCategoryDataset lineDataset = createLineDataset(recordsByDate);
            DefaultPieDataset pieDataset = createPieDataset(records);

            // 创建图表
            JFreeChart barChart = createBarChart(barDataset);
            JFreeChart lineChart = createLineChart(lineDataset);
            JFreeChart pieChart = createPieChart(pieDataset);

            // 创建图表面板
            ChartPanel barChartPanel = new ChartPanel(barChart);
            ChartPanel lineChartPanel = new ChartPanel(lineChart);
            ChartPanel pieChartPanel = new ChartPanel(pieChart);

            // 创建主面板
            JPanel mainPanel = new JPanel(new GridLayout(3, 1));
            mainPanel.add(barChartPanel);
            mainPanel.add(lineChartPanel);
            mainPanel.add(pieChartPanel);

            contentPanel.add(mainPanel, BorderLayout.CENTER);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    // 读取 CSV 文件
    private List<Record> readCSV(String filePath) throws IOException, ParseException {
        List<Record> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] values = line.split(",");
                Date date = DATE_FORMAT.parse(values[0].trim());
                double amount = Double.parseDouble(values[1].trim());
                String category = values[2].trim();
                records.add(new Record(date, amount, category));
            }
        }
        return records;
    }

    // 按日期分组数据
    private Map<Date, List<Record>> groupRecordsByDate(List<Record> records) {
        Map<Date, List<Record>> recordsByDate = new TreeMap<>();
        for (Record record : records) {
            recordsByDate.computeIfAbsent(record.date, k -> new ArrayList<>()).add(record);
        }
        return recordsByDate;
    }

    // 创建柱状图数据集
    private DefaultCategoryDataset createBarDataset(Map<Date, List<Record>> recordsByDate) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<Date, List<Record>> entry : recordsByDate.entrySet()) {
            Date date = entry.getKey();
            List<Record> records = entry.getValue();
            double totalAmount = records.stream().mapToDouble(r -> r.amount).sum();
            dataset.addValue(totalAmount, "Total Expenses", DATE_FORMAT.format(date));
        }
        return dataset;
    }

    // 创建折线图数据集
    private DefaultCategoryDataset createLineDataset(Map<Date, List<Record>> recordsByDate) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<Date, List<Record>> entry : recordsByDate.entrySet()) {
            Date date = entry.getKey();
            List<Record> records = entry.getValue();
            double totalAmount = records.stream().mapToDouble(r -> r.amount).sum();
            dataset.addValue(totalAmount, "Total Expenses", DATE_FORMAT.format(date));
        }
        return dataset;
    }

    // 创建饼图数据集
    private DefaultPieDataset createPieDataset(List<Record> records) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        Map<String, Double> categoryAmountMap = new HashMap<>();
        for (Record record : records) {
            String category = record.category;
            double amount = record.amount;
            categoryAmountMap.put(category, categoryAmountMap.getOrDefault(category, 0.0) + amount);
        }
        for (Map.Entry<String, Double> entry : categoryAmountMap.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }
        return dataset;
    }

    // 创建柱状图
    private JFreeChart createBarChart(DefaultCategoryDataset dataset) {
        return ChartFactory.createBarChart(
                "Bar Chart of Daily Expenses",
                "Date",
                "Total Expenses",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);
    }

    // 创建折线图
    private JFreeChart createLineChart(DefaultCategoryDataset dataset) {
        return ChartFactory.createLineChart(
                "Line Chart of Daily Expenses",
                "Date",
                "Total Expenses",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);
    }

    // 创建饼图
    private JFreeChart createPieChart(DefaultPieDataset dataset) {
        return ChartFactory.createPieChart(
                "Pie Chart of Expense Categories",
                dataset,
                true, true, false);
    }

    // 记录类
    static class Record {
        Date date;
        double amount;
        String category;

        Record(Date date, double amount, String category) {
            this.date = date;
            this.amount = amount;
            this.category = category;
        }
    }
}    