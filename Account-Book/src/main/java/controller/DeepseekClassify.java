package controller;


import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class DeepseekClassify {

    private static final String DEEPSEEK_API_URL_COMPLETIONS = "https://api.deepseek.com/chat/completions";
    private static final String DEEPSEEK_API_KEY = "sk-941ce5cde31c41cfaf9c5b7f4789b044";
    private static final String CSV_FILE_PATH = "E:\\JAVA\\Software-project_4\\18\\Account-Book\\src\\main\\java\\model\\finance_data.csv";//自己的路径

    public static void main(String[] args) {
        DeepseekClassify test = new DeepseekClassify();
        try {
            test.processCSV(CSV_FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processCSV(String csvFilePath) throws IOException {
        File csvFile = new File(csvFilePath);
        File tempFile = new File(csvFilePath + ".tmp");

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile));
             BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    bw.write(line);
                    isFirstLine = false;
                } else {
                    String[] columns = line.split(",");
                    String description = columns[3];
                    String category = classifyDescription(description);
                    columns[2] = category; // 更新Category字段
                    bw.write(String.join(",", columns));
                }
                bw.newLine();
            }
        }

        // 删除原文件，将临时文件重命名为原文件名
        if (csvFile.delete()) {
            tempFile.renameTo(csvFile);
        } else {
            throw new IOException("Failed to delete the original CSV file.");
        }
    }

    public String classifyDescription(String description) throws IOException {
        String result = null;
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

        String requestData = "{ \"messages\": "
                + "[ "
                + " { \"content\": \"你是一个财务分类的专家，请根据描述返回一个具体的分类名称要用英文，不要返回其他内容。\", \"role\": \"system\" , \"name\": \"muyunfei\" }, "
                + " { \"content\": \"根据描述分类: " + description + "\", \"role\": \"user\" , \"name\": \"路人甲\"} "
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
        result = item.getString("content");

        return result;
    }

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

    private TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[] {};
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
    } };

    private HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
}
