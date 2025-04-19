package controller.Impl;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class AccountBookControllerImpl {
    //注意此文件路径需要修改！！！！！！
    private static final String CSV_FILE = "D:\\StudySoftware\\java practice\\Integration\\Account-Book\\src\\main\\java\\org\\example\\finance_data.csv";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    // 初始化日期格式配置
    public AccountBookControllerImpl() {
        initializeDateFormat();
    }

    public void initializeDateFormat() {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        DATE_FORMAT.setLenient(false);
    }

    // 核心业务方法：搜索记录
    public Map<Date, List<Record>> searchRecords(String startInput, String endInput)
            throws ParseException, IOException, IllegalArgumentException {
        ProcessedDates dates = processInputDates(startInput, endInput);
        List<Record> records = CSVUtils.readCSV(CSV_FILE);
        return filterRecords(records, dates.start(), dates.end());
    }

    // 日期处理管道
    private ProcessedDates processInputDates(String start, String end) throws ParseException {
        String sanitizedStart = sanitizeDate(start);
        String sanitizedEnd = sanitizeDate(end);
        validateDateFormat(sanitizedStart, sanitizedEnd);

        Date startDate = DATE_FORMAT.parse(sanitizedStart);
        Date endDate = DATE_FORMAT.parse(sanitizedEnd);
        validateDateOrder(startDate, endDate);

        return new ProcessedDates(startDate, endDate);
    }

    // 数据过滤逻辑
    private Map<Date, List<Record>> filterRecords(List<Record> records, Date start, Date end) {
        Map<Date, List<Record>> result = new TreeMap<>(Collections.reverseOrder());
        Date normStart = normalizeDate(start, true);
        Date normEnd = normalizeDate(end, false);

        records.stream()
                .filter(record -> isDateInRange(record.date(), normStart, normEnd))
                .forEach(record -> {
                    Date recordDate = normalizeDate(record.date(), true);
                    result.computeIfAbsent(recordDate, k -> new ArrayList<>()).add(record);
                });

        return result;
    }

    // 工具方法组
    private String sanitizeDate(String input) {
        return input.trim()
                .replaceAll("[^\\d-]", "")
                .replaceAll("-(?=.)", "-");
    }

    private void validateDateFormat(String start, String end) throws ParseException {
        if (!start.matches("\\d{4}-\\d{2}-\\d{2}") || !end.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new ParseException("日期格式必须为YYYY-MM-DD", 0);
        }
    }

    private void validateDateOrder(Date start, Date end) {
        if (start.after(end)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }
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

    private boolean isDateInRange(Date date, Date start, Date end) {
        return !date.before(start) && !date.after(end);
    }

    // 对外暴露的日期格式化方法
    public String formatDate(Date date) {
        return DATE_FORMAT.format(date);
    }

    public SimpleDateFormat getDateFormat() {
        return DATE_FORMAT;
    }

    // 数据模型定义
    public record ProcessedDates(Date start, Date end) {}
    public record Record(Date date, double amount, String category, String description) {}

    // CSV数据处理工具类
    static class CSVUtils {
        static List<Record> readCSV(String path) throws IOException {
            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                return br.lines()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .map(CSVUtils::parseLine)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
        }

        private static Record parseLine(String line) {
            String[] values = line.split(",", -1);
            if (values.length < 4) return null;

            try {
                Date date = DATE_FORMAT.parse(values[0].trim());
                double amount = parseAmount(values[1]);
                return new Record(date, amount, values[2].trim(), values[3].trim());
            } catch (ParseException | NumberFormatException ex) {
                System.err.println("CSV解析错误 - 跳过无效记录: " + line);
                return null;
            }
        }

        private static double parseAmount(String value) {
            return Double.parseDouble(value.replaceAll("[^\\d.]", ""));
        }
    }
}
