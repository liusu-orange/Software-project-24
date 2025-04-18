package controller.Impl;

import util.ConfigLoader;

import java.io.*;
import java.util.Properties;

/**
 * 控制器：处理配置路径的读取和写入操作
 */
public class SettingControllerImpl {

    //配置文件路径
    private static final String CONFIG_PATH = "src/main/resources/config.properties";

    public String getCurrentCsvPath() {
        return ConfigLoader.get("csv.file.path");
    }

    public void updateCsvPath(String newPath) throws IOException {
        File configFile = new File(CONFIG_PATH);
        Properties props = new Properties();

        try (InputStream in = new FileInputStream(configFile)) {
            props.load(in);
        }

        props.setProperty("csv.file.path", newPath);

        try (OutputStream out = new FileOutputStream(configFile)) {
            props.store(out, "Updated CSV file path");
        }
    }



}
