package view.Impl;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.List;

public class AccountBookUiImpl {
    // WARNING WARNING WARNING WARNING WARNING 此处CSV文件地址需调整为你的CSV文件所在的绝对地址
    private static final String CSV_FILE = "D:\\StudySoftware\\java practice\\Integration\\Account-Book\\src\\main\\java\\org\\example\\finance_data.csv";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private JPanel contentPanel;
    private JScrollPane resultScrollPane; //滚动面板
    private JPanel resultPanel; //结果面板

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        DATE_FORMAT.setLenient(false);
    }

    public AccountBookUiImpl(JPanel contentPanel) {
        this.contentPanel = contentPanel;
    }

    public void AccountBookWindow() {
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());

        // 控制面板
        JPanel controlPanel = createControlPanel();

        // 结果面板初始化
        resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultScrollPane = new JScrollPane(resultPanel);

        contentPanel.add(controlPanel, BorderLayout.NORTH);
        contentPanel.add(resultScrollPane, BorderLayout.CENTER);
        contentPanel.revalidate();
    }

    //创建控制面板
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JFormattedTextField startDateField = createDateField(150);
        JFormattedTextField endDateField = createDateField(150);
        JButton searchButton = new JButton("Search");

        addComponent(panel, new JLabel("Start Date:"), gbc, 0, 0);
        addComponent(panel, startDateField, gbc, 1, 0);
        addComponent(panel, new JLabel("End Date:"), gbc, 2, 0);
        addComponent(panel, endDateField, gbc, 3, 0);
        addComponent(panel, searchButton, gbc, 4, 0);

        searchButton.addActionListener(e -> handleSearch(
                startDateField.getText(),
                endDateField.getText()
        ));

        return panel;
    }

    //创建文本框
    private JFormattedTextField createDateField(int width) {
        JFormattedTextField field = new JFormattedTextField(DATE_FORMAT);
        configureTextField(field, width);
        return field;
    }

    //文本框提示
    private void configureTextField(JFormattedTextField field, int width) {
        field.setColumns(10);
        field.setPreferredSize(new Dimension(width, 30));
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setToolTipText("<html>格式：<b>YYYY-MM-DD</b><br>示例：2025-04-18</html>");
    }

    //添加组件
    private void addComponent(JPanel panel, Component comp, GridBagConstraints gbc, int x, int y) {
        gbc.gridx = x;
        gbc.gridy = y;
        panel.add(comp, gbc);
    }

    //搜索功能
    private void handleSearch(String startInput, String endInput) {
        try {
            ProcessedDates dates = processInputDates(startInput, endInput);
            List<Record> records = CSVUtils.readCSV(CSV_FILE);
            Map<Date, List<Record>> filteredData = filterRecords(records, dates.start(), dates.end());
            updateResultPanel(filteredData);
        } catch (ParseException ex) {
            showError("日期格式错误：" + ex.getMessage());
        } catch (IOException | IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private record ProcessedDates(Date start, Date end) {}


    private ProcessedDates processInputDates(String start, String end) throws ParseException {
        String sanitizedStart = sanitizeDate(start);
        String sanitizedEnd = sanitizeDate(end);
        validateDateFormat(sanitizedStart, sanitizedEnd);

        Date startDate = DATE_FORMAT.parse(sanitizedStart);
        Date endDate = DATE_FORMAT.parse(sanitizedEnd);
        validateDateOrder(startDate, endDate);

        return new ProcessedDates(startDate, endDate);
    }

    private String sanitizeDate(String input) {
        return input.trim()
                .replaceAll("[^\\d-]", "")
                .replaceAll("-(?=.)", "-");
    }

    private void validateDateFormat(String start, String end) throws ParseException {
        if (!start.matches("\\d{4}-\\d{2}-\\d{2}") || !end.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new ParseException("Invalid format", 0);
        }
    }

    private void validateDateOrder(Date start, Date end) {
        if (start.after(end)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }
    }

    private Map<Date, List<Record>> filterRecords(List<Record> records, Date start, Date end) {
        Map<Date, List<Record>> result = new TreeMap<>(Collections.reverseOrder());
        Calendar cal = Calendar.getInstance();

        Date normStart = normalizeDate(start, true);
        Date normEnd = normalizeDate(end, false);

        for (Record record : records) {
            Date recordDate = normalizeDate(record.date(), true);
            if (!recordDate.before(normStart) && !recordDate.after(normEnd)) {
                result.computeIfAbsent(recordDate, k -> new ArrayList<>()).add(record);
            }
        }
        return result;
    }

    private Date normalizeDate(Date date, boolean isStart) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if (isStart) {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
        } else {
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
        }
        return cal.getTime();
    }

    private void updateResultPanel(Map<Date, List<Record>> data) {
        resultPanel.removeAll();
        if (data.isEmpty()) {
            resultPanel.add(new JLabel("未找到相关记录"));
        } else {
            data.forEach((date, records) -> resultPanel.add(createDatePanel(date, records)));
        }
        resultPanel.revalidate();
        resultScrollPane.repaint();
    }

    private JPanel createDatePanel(Date date, List<Record> records) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(DATE_FORMAT.format(date)));

        double total = records.stream().mapToDouble(Record::amount).sum();
        panel.add(new JLabel(String.format("总计：¥%.2f", total)));

        DefaultTableModel model = new DefaultTableModel(new Object[]{"金额", "分类", "描述"}, 0);
        records.forEach(r -> model.addRow(new Object[]{r.amount(), r.category(), r.description()}));
        panel.add(new JScrollPane(new JTable(model)));

        return panel;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(contentPanel,
                "<html><b>错误：</b>" + message + "</html>",
                "系统提示",
                JOptionPane.ERROR_MESSAGE);
    }

    record Record(Date date, double amount, String category, String description) {}

    static class CSVUtils {
        static List<Record> readCSV(String path) throws IOException {
            List<Record> records = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                String line;
                while ((line = br.readLine()) != null) {
                    processCSVLine(line, records);
                }
            }
            return records;
        }

        private static void processCSVLine(String line, List<Record> records) {
            line = line.trim();
            if (line.isEmpty()) return;

            String[] values = line.split(",", -1);
            if (values.length < 4) return;

            try {
                Date date = DATE_FORMAT.parse(values[0].trim());
                double amount = parseAmount(values[1]);
                records.add(new Record(date, amount, values[2].trim(), values[3].trim()));
            } catch (ParseException | NumberFormatException ex) {
                System.err.println("跳过无效记录：" + line);
            }
        }

        private static double parseAmount(String value) {
            return Double.parseDouble(value.replaceAll("[^\\d.]", ""));
        }
    }
}
