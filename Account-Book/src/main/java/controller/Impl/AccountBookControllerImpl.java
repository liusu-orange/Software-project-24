package controller.Impl;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
/**
 *The ledger controller implementation class handles the search, filtering and date processing logic of financial records.
 * <p>
 *This class obtains the CSV data file path of the current user through the user controller, and provides the following core functions:
 * <ul>
 *<li>date format validation and standardization</li>
 *<li>csv data reading and record filtering</li>
 *<li>search financial records by date range</li>
 * </ul>
 *@author Guanren Huang
 * @version 1.1.0
 * @since v1.0.0
 */
public class AccountBookControllerImpl {

    private final UserControllerImpl userController;
    private String CSV_FILE;
    /**
     * Constructs the account book controller.
     * @param userController Instance of UserController to retrieve the user's financial data file path
     */
    public AccountBookControllerImpl(UserControllerImpl userController) {
        this.userController = userController;
        updateCsvFilePath();
    }
    /**
     * Updates the CSV file path to the current user's financial data path.
     */
    private void updateCsvFilePath() {
        CSV_FILE = userController.getCurrentUserFinanceFilePath();
    }

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    /**
     * Initializes the date format (time zone: GMT+8, strict parsing mode).
     */
    // 初始化日期格式配置
    public void initializeDateFormat() {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        DATE_FORMAT.setLenient(false);
    }
    /**
     * Searches financial records by date range.
     *
     * @param startInput Start date (format: YYYY-MM-DD)
     * @param endInput End date (format: YYYY-MM-DD)
     * @return Mapped records grouped by date (sorted in reverse order)
     * @throws ParseException For invalid date formats
     * @throws IOException For file read failures
     * @throws IllegalArgumentException If start date is after end date
     */
    // 核心业务方法：搜索记录
    public Map<Date, List<Record>> searchRecords(String startInput, String endInput)
            throws ParseException, IOException, IllegalArgumentException {
        updateCsvFilePath();
        ProcessedDates dates = processInputDates(startInput, endInput);
        List<Record> records = CSVUtils.readCSV(CSV_FILE);
        return filterRecords(records, dates.start(), dates.end());
    }
    /**
     * Cleans and validates input dates.
     * @param start Start date string
     * @param end End date string
     * @return Processed date range
     * @throws ParseException For invalid date formats
     */
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
    /**
     * Filters and groups records by date range.
     * @param records List of raw financial records
     * @param start Start date (inclusive)
     * @param end End date (inclusive)
     * @return Mapped records grouped by date
     */
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
    /**
     * Cleans a date string by removing invalid characters.
     * @param input Raw date string
     * @return Sanitized date string
     */
    // 工具方法组
    private String sanitizeDate(String input) {
        return input.trim()
                .replaceAll("[^\\d-]", "")
                .replaceAll("-(?=.)", "-");
    }
    /**
     * Validates if dates are in YYYY-MM-DD format.
     * @param start Start date string
     * @param end End date string
     * @throws ParseException If date format does not match YYYY-MM-DD
     */
    private void validateDateFormat(String start, String end) throws ParseException {
        if (!start.matches("\\d{4}-\\d{2}-\\d{2}") || !end.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new ParseException("日期格式必须为YYYY-MM-DD", 0);
        }
    }
    /**
     * Validates if the start date is before the end date.
     * @param start Start date object
     * @param end End date object
     * @throws IllegalArgumentException If start date is after end date
     */
    private void validateDateOrder(Date start, Date end) {
        if (start.after(end)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }
    }
    /**
     * Normalizes a date to the start or end of the day.
     * @param date Original date
     * @param isStart True for start of day (00:00:00), false for end of day (23:59:59)
     * @return Normalized date object
     */
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
    /**
     * Checks if a date falls within a specified range.
     * @param date Date to check
     * @param start Start date (inclusive)
     * @param end End date (inclusive)
     * @return True if the date is within the range
     */
    private boolean isDateInRange(Date date, Date start, Date end) {
        return !date.before(start) && !date.after(end);
    }
    /**
     * Formats a date to a string (YYYY-MM-DD).
     * @param date Date object
     * @return Formatted date string
     */
    // 对外暴露的日期格式化方法
    public String formatDate(Date date) {
        return DATE_FORMAT.format(date);
    }
    /**
     * Gets the date formatter instance.
     * @return Date formatter (YYYY-MM-DD, GMT+8)
     */
    public SimpleDateFormat getDateFormat() {
        return DATE_FORMAT;
    }
    /**
     * Record for processed date range.
     * @param start Start date of the range
     * @param end End date of the range
     */
    // 数据模型定义
    public record ProcessedDates(Date start, Date end) {}
    public record Record(Date date, double amount, String category, String description) {}
    /**
     *CSV file processing tool class.
     */
    public static class CSVUtils {
        /**
         * Reads and parses a CSV file into a list of records.
         * @param path Path to the CSV file
         * @return List of financial records
         * @throws IOException For CSV file read errors
         */
        public static List<Record> readCSV(String path) throws IOException {

            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                return br.lines()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .map(CSVUtils::parseLine)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
        }
        /**
         * Parses a CSV line into a Record object.
         * @param line CSV line string
         * @return Parsed Record, or null on failure
         */
        private static Record parseLine(String line) {
            String[] values = line.split(",", -1);
            if (values.length < 4) return null;
            try {
                Date date = DATE_FORMAT.parse(values[0].trim());
                double amount = parseAmount(values[1]);
                return new Record(date, amount, values[2].trim(), values[3].trim());
            } catch (ParseException | NumberFormatException ex) {
                System.err.println("跳过无效记录: " + line);
                return null;
            }
        }
        /**
         * Parses an amount string into a numeric value.
         * @param value Amount string (e.g., "¥1,234.56")
         * @return Parsed numeric amount
         */
        private static double parseAmount(String value) {
            return Double.parseDouble(value.replaceAll("[^\\d.]", ""));
        }
    }
}
