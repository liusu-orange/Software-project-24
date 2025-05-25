package controller.Impl;

import controller.ReportFormsController;
import org.jfree.chart.*;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.*;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.io.*;
import java.text.*;
import java.util.*;
/**
 * Controller for generating financial charts based on user expense data.
 *
 * @author Haoming Lei
 * @version 1.0.0
 * @since v1.0.0
 */
public class ReportFormsControllerImpl implements ReportFormsController {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final UserControllerImpl userController;
    private String CSV_FILE;
    /**
     * Initializes the controller with user financial data.
     * @param userController User controller to fetch CSV file path
     */
    public ReportFormsControllerImpl(UserControllerImpl userController) {
        this.userController = userController;
        updateCsvFilePath();
    }
    /**
     * Updates the CSV file path to the current user's data.
     */
    private void updateCsvFilePath() {
        CSV_FILE = userController.getCurrentUserFinanceFilePath();
    }
    /**
     * Generates financial charts (bar, line, pie) from user expense data.
     * @return List of ChartPanels containing bar, line, and pie charts
     */
    @Override
    public List<ChartPanel> generateCharts() {
        updateCsvFilePath(); // 确保始终获取最新路径
        List<ChartPanel> panels = new ArrayList<>();
        try {
            List<Record> records = readCSV(CSV_FILE);
            Map<Date, List<Record>> recordsByDate = groupRecordsByDate(records);

            DefaultCategoryDataset barDataset = createCategoryDataset(recordsByDate);
            DefaultCategoryDataset lineDataset = createCategoryDataset(recordsByDate);
            DefaultPieDataset pieDataset = createPieDataset(records);

            JFreeChart barChart = createBarChart(barDataset);
            JFreeChart lineChart = createLineChart(lineDataset);
            JFreeChart pieChart = createPieChart(pieDataset);

            PiePlot piePlot = (PiePlot) pieChart.getPlot();
            piePlot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {2}",
                    NumberFormat.getNumberInstance(), NumberFormat.getPercentInstance()));

            panels.add(new ChartPanel(barChart));
            panels.add(new ChartPanel(lineChart));
            panels.add(new ChartPanel(pieChart));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return panels;
    }

    /**
     * Reads financial records from a CSV file.
     * @param filePath Path to CSV file (format: date,amount,category)
     * @return List of parsed records
     * @throws IOException If file read fails
     * @throws ParseException If date parsing fails
     */
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
    /**
     * Groups records by date.
     * @param records List of financial records
     * @return Map of date to records
     */
    private Map<Date, List<Record>> groupRecordsByDate(List<Record> records) {
        Map<Date, List<Record>> map = new TreeMap<>();
        for (Record r : records) {
            map.computeIfAbsent(r.date, k -> new ArrayList<>()).add(r);
        }
        return map;
    }
    /**
     * Creates dataset for category-based charts (bar/line).
     * @param data Records grouped by date
     * @return Dataset with daily total expenses
     */
    private DefaultCategoryDataset createCategoryDataset(Map<Date, List<Record>> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<Date, List<Record>> entry : data.entrySet()) {
            double total = entry.getValue().stream().mapToDouble(r -> r.amount).sum();
            dataset.addValue(total, "Total Expenses", DATE_FORMAT.format(entry.getKey()));
        }
        return dataset;
    }
    /**
     * Creates dataset for pie chart (category distribution).
     * @param records List of financial records
     * @return Dataset with category-wise expense totals
     */
    private DefaultPieDataset createPieDataset(List<Record> records) {
        Map<String, Double> map = new HashMap<>();
        for (Record r : records) {
            map.put(r.category, map.getOrDefault(r.category, 0.0) + r.amount);
        }
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }
        return dataset;
    }
    /**
     * Creates bar chart for daily expenses.
     * @param dataset Dataset with daily expense totals
     * @return Configured bar chart
     */
    private JFreeChart createBarChart(DefaultCategoryDataset dataset) {
        return ChartFactory.createBarChart("Bar Chart of Daily Expenses", "Date", "Total Expenses",
                dataset, PlotOrientation.VERTICAL, true, true, false);
    }
    /**
     * Creates line chart for daily expenses.
     * @param dataset Dataset with daily expense totals
     * @return Configured line chart
     */
    private JFreeChart createLineChart(DefaultCategoryDataset dataset) {
        return ChartFactory.createLineChart("Line Chart of Daily Expenses", "Date", "Total Expenses",
                dataset, PlotOrientation.VERTICAL, true, true, false);
    }
    /**
     * Creates pie chart for expense categories.
     * @param dataset Dataset with category expense totals
     * @return Configured pie chart
     */
    private JFreeChart createPieChart(DefaultPieDataset dataset) {
        return ChartFactory.createPieChart("Pie Chart of Expense Categories", dataset, true, true, false);
    }
    /**
     * Data model for financial records.
     */
    private static class Record {
        Date date;
        double amount;
        String category;

        /**
         * Creates a financial record.
         * @param date Transaction date
         * @param amount Transaction amount
         * @param category Expense category
         */
        Record(Date date, double amount, String category) {
            this.date = date;
            this.amount = amount;
            this.category = category;
        }
    }
}

