package controller.Impl;
import java.io.*;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;

import controller.Impl.UserControllerImpl;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
/**
 * AI Financial Analysis Controller that generates budget reports using the DeepSeek API.
 * @author Yi Zhong
 * @version 1.0.0
 * @since v1.0.0
 */
public class AIAnalyzeControllerImpl {
    private static final String DEEPSEEK_API_URL_COMPLETIONS = "https://api.deepseek.com/chat/completions";
    private static final String DEEPSEEK_API_KEY = "sk-941ce5cde31c41cfaf9c5b7f4789b044"; // 替换为你的API Key

    private final UserControllerImpl userController;
    public String CSV_FILE;
    /**
     * Initializes the AI analysis controller with the user's financial data path.
     * @param userController Instance of UserController to retrieve the CSV file path
     */
    public AIAnalyzeControllerImpl(UserControllerImpl userController) {
        this.userController = userController;
        updateCsvFilePath();
    }
    /**
     * Updates the user's financial data file path.
     */
    private void updateCsvFilePath() {
        CSV_FILE = userController.getCurrentUserFinanceFilePath();
    }
    /**
     * Analyzes CSV financial data and generates AI budget suggestions.
     * @param csvFilePath Path to the CSV file (format: date,amount,category[,description])
     * @return String containing monthly expense summaries and AI-generated suggestions
     * @throws IOException Thrown on file read or network request failures
     */
    public String analyzeBudgetWithDeepSeek(String csvFilePath) throws IOException {
        StringBuilder result = new StringBuilder();
        // 使用 TreeMap 确保按月份顺序排序
        Map<String, Map<String, Double>> monthlyExpenses = new TreeMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy/MM");

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }

                String[] columns = line.split(",");
                if (columns.length < 3) continue;

                String dateStr = columns[0];
                double amount = Double.parseDouble(columns[1]);
                String category = columns[2];

                try {
                    Date date = dateFormat.parse(dateStr);
                    String monthKey = monthFormat.format(date);

                    monthlyExpenses.putIfAbsent(monthKey, new HashMap<>());
                    Map<String, Double> categoryExpenses = monthlyExpenses.get(monthKey);

                    categoryExpenses.put(category, categoryExpenses.getOrDefault(category, 0.0) + amount);
                } catch (ParseException e) {
                    System.err.println("Invalid date format: " + dateStr);
                }
            }
        }

        // Generate budget suggestions for each month using DeepSeek
        for (Map.Entry<String, Map<String, Double>> entry : monthlyExpenses.entrySet()) {
            String month = entry.getKey();
            Map<String, Double> categoryExpenses = entry.getValue();

            result.append("\n--- Budget Analysis for ").append(month).append(" ---\n");
            result.append("Total Expenses: $").append(getTotalExpenses(categoryExpenses)).append("\n");

            // Append category-wise expenses
            result.append("\nCategory-wise Expenses:\n");
            for (Map.Entry<String, Double> categoryEntry : categoryExpenses.entrySet()) {
                result.append(String.format("%-15s: $%.2f%n", categoryEntry.getKey(), categoryEntry.getValue()));
            }

            // Append budget suggestions from DeepSeek
            String suggestions = getBudgetSuggestionsFromDeepSeek(categoryExpenses, month);
            result.append("\nBudget Suggestions:\n").append(suggestions).append("\n");
        }

        return result.toString();
    }
    /**
     * Calculates the total expenses for given category expenses.
     * @param categoryExpenses Map of category to expense amounts
     * @return Total expense amount
     */
    private double getTotalExpenses(Map<String, Double> categoryExpenses) {
        return categoryExpenses.values().stream().mapToDouble(Double::doubleValue).sum();
    }
    /**
     * Calls the DeepSeek API to get budget suggestions.
     * @param categoryExpenses Category-wise expense data
     * @param month Month in "yyyy/MM" format
     * @return English text of AI-generated budget suggestions
     * @throws IOException Thrown on API request failures
     */
    private String getBudgetSuggestionsFromDeepSeek(Map<String, Double> categoryExpenses, String month) throws IOException {
        StringBuilder description = new StringBuilder();

        // 检查月份是否为2月（中国新年）或9月（中秋节）
        if (month.endsWith("/02")) {
            description.append("Note: Adjust budget reasonably for Chinese New Year. ");
        } else if (month.endsWith("/09")) {
            description.append("Note: Adjust budget reasonably for Mid-Autumn Festival. ");
        }

        for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
            description.append(entry.getKey()).append(": $").append(entry.getValue()).append(", ");
        }

        String requestData = "{ \"messages\": "
                + "[ "
                + " { \"content\": \"你是一个财务分析专家，请根据以下消费数据，给出预算建议和需要削减的消费种类。请用英文回答，并给出具体理由。注意：不要提出任何问题或询问是否需要更多信息，只需提供建议。\", \"role\": \"system\" }, "
                + " { \"content\": \"消费数据: " + description.toString() + "\", \"role\": \"user\"} "
                + "],"
                + " \"model\": \"deepseek-chat\","
                + " \"frequency_penalty\": 0,"
                + " \"max_tokens\": 2048,"
                + " \"presence_penalty\": 0,"
                + " \"response_format\": {\n \"type\": \"text\"\n },"
                + " \"stop\": null,"
                + " \"stream\": false,"
                + " \"stream_options\": null,"
                + " \"temperature\": 1,"
                + " \"top_p\": 1,"
                + " \"tools\": null,"
                + " \"tool_choice\": \"none\","
                + " \"logprobs\": false,"
                + " \"top_logprobs\": null}";

        URL url_req = new URL(DEEPSEEK_API_URL_COMPLETIONS);
        HttpsURLConnection connection = (HttpsURLConnection) url_req.openConnection();

        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setConnectTimeout(60000);
        connection.setReadTimeout(60000);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Charset", "UTF-8");
        connection.addRequestProperty("Authorization", "Bearer " + DEEPSEEK_API_KEY);

        HttpsURLConnection https = (HttpsURLConnection) connection;
        SSLSocketFactory oldSocketFactory = trustAllHosts(https);
        https.setHostnameVerifier(DO_NOT_VERIFY);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestData.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        InputStream in = connection.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(in);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int c;
        while (-1 != (c = bis.read())) {
            baos.write(c);
        }
        bis.close();
        in.close();
        baos.flush();

        byte[] data = baos.toByteArray();
        String responseMsg = new String(data);
        JSONObject jsonObject = JSONObject.fromObject(responseMsg);
        JSONArray choices = JSONArray.fromObject(jsonObject.get("choices"));
        JSONObject item = JSONObject.fromObject(JSONObject.fromObject(choices.get(0)).get("message"));
        return item.getString("content");
    }
    /**
     * Trusts all SSL certificates (for bypassing certificate validation).
     * @param connection HTTPS connection instance
     * @return Original SSLSocketFactory
     */
    private SSLSocketFactory trustAllHosts(HttpsURLConnection connection) {
        SSLSocketFactory oldFactory = connection.getSSLSocketFactory();
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory newFactory = sc.getSocketFactory();
            connection.setSSLSocketFactory(newFactory);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return oldFactory;
    }
    /**
     * Custom trust manager for ignoring certificate validation.
     */
    private TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
    }};
    /**
     * Hostname verifier that always returns true.
     */
    private HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
}

