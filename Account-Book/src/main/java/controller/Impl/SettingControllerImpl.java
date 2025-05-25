package controller.Impl;

import controller.SettingController;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public class SettingControllerImpl implements SettingController {
    private static final String CONFIG_DIR = "config";
    private static final String CONFIG_FILE = CONFIG_DIR + File.separator + "config.properties";
    private static final String FINANCE_FILE_DIR_KEY = "finance_file_directory";
    private static final String DEFAULT_FINANCE_DIR = "user_finance_data";
    private static String currentFinanceFileDirectory;

    private final UserControllerImpl userController;
    private String CSV_FILE;

    public SettingControllerImpl(UserControllerImpl userController) {
        this.userController = userController;
    }

    static {
        try {
            initializeApplication();
        } catch (showException e) {
            // 初始化失败，使用默认值
            currentFinanceFileDirectory = DEFAULT_FINANCE_DIR;
            System.err.println("初始化失败: " + e.getMessage());
        }
    }

    private static void initializeApplication() throws showException {
        // 1. 确保配置目录存在
        File configDir = new File(CONFIG_DIR);
        if (!configDir.exists() && !configDir.mkdirs()) {
            throw new showException("无法创建配置目录: " + CONFIG_DIR);
        }

        // 2. 初始化配置文件
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            createDefaultConfig(configFile);
        }

        // 3. 加载配置
        loadSettings();

        // 4. 确保财务数据目录存在
        new File(currentFinanceFileDirectory).mkdirs();
    }

    private static void createDefaultConfig(File configFile) throws showException {
        try (OutputStream out = new FileOutputStream(configFile)) {
            Properties props = new Properties();
            props.setProperty(FINANCE_FILE_DIR_KEY, DEFAULT_FINANCE_DIR);
            props.store(out, "Auto-generated default config");
        } catch (IOException e) {
            throw new showException("无法创建默认配置文件: " + e.getMessage(), e);
        }
    }

    private static void loadSettings() throws showException {
        Properties props = new Properties();
        File externalConfig = new File(CONFIG_FILE);

        if (externalConfig.exists()) {
            try (FileInputStream in = new FileInputStream(externalConfig)) {
                props.load(in);
                currentFinanceFileDirectory = props.getProperty(FINANCE_FILE_DIR_KEY, DEFAULT_FINANCE_DIR);
            } catch (IOException e) {
                throw new showException("加载外部配置时出错: " + e.getMessage(), e);
            }
        }

        if (currentFinanceFileDirectory == null) {
            currentFinanceFileDirectory = DEFAULT_FINANCE_DIR;
        }
    }

    private static void saveSettings() throws showException {
        Properties props = new Properties();
        props.setProperty(FINANCE_FILE_DIR_KEY, currentFinanceFileDirectory);

        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "应用程序设置");
        } catch (IOException e) {
            throw new showException("保存设置时出错: " + e.getMessage(), e);
        }
    }

    public static String getFinanceFilePath(String username) {
        return currentFinanceFileDirectory + File.separator + username + "_finance.csv";
    }

    public static void setFinanceFileDirectory(String newDirectory) throws showException {
        File dir = new File(newDirectory);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new showException("无法创建目标文件夹: " + newDirectory);
        }

        // 移动现有用户的财务文件
        File oldDir = new File(currentFinanceFileDirectory);
        if (oldDir.exists()) {
            File[] userFiles = oldDir.listFiles((d, name) -> name.endsWith("_finance.csv"));
            if (userFiles != null) {
                for (File file : userFiles) {
                    try {
                        Files.move(
                                file.toPath(),
                                new File(dir, file.getName()).toPath(),
                                StandardCopyOption.REPLACE_EXISTING
                        );
                    } catch (IOException e) {
                        throw new showException("移动用户财务文件失败: " + e.getMessage(), e);
                    }
                }
            }
        }

        currentFinanceFileDirectory = newDirectory;
        saveSettings();
    }

    public static String getCurrentFinanceDirectory() {
        return currentFinanceFileDirectory;
    }

    // 创建用户财务文件
    public static void createUserFinanceFile(String username) throws showException {
        String filePath = getFinanceFilePath(username);
        File file = new File(filePath);

        // 创建父目录（如果不存在）
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new showException("创建目录失败: " + parentDir.getAbsolutePath());
            }
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
                // 写入CSV表头
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("Date,Amount,Category,Description\n");
                }
            } catch (IOException e) {
                throw new showException("创建用户账目文件失败: " + e.getMessage(), e);
            }
        }
    }
}