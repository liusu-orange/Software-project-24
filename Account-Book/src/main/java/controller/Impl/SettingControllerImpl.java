package controller.Impl;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/**
 * Settings controller for managing application configurations and user finance data paths.
 *
 * @author Boliang Chen
 * @version 1.0.0
 * @since v1.0.0
 */
public class SettingControllerImpl {
    private static final String CONFIG_DIR = "config";
    private static final String CONFIG_FILE = CONFIG_DIR + File.separator + "config.properties";
    private static final String FINANCE_FILE_DIR_KEY = "finance_file_directory";
    private static final String DEFAULT_FINANCE_DIR = "user_finance_data";
    private static String currentFinanceFileDirectory;

    private final UserControllerImpl userController;
    private String CSV_FILE;

    /**
     * Initializes the settings controller with user context.
     * @param userController User controller instance
     */
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

    /**
     * Initializes application settings (config directory, files, and defaults).
     * @throws showException For configuration initialization failures
     */
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

    /**
     * Creates default configuration file with predefined settings.
     * @param configFile Target configuration file
     * @throws showException For I/O errors during creation
     */
    private static void createDefaultConfig(File configFile) throws showException {
        try (OutputStream out = new FileOutputStream(configFile)) {
            Properties props = new Properties();
            props.setProperty(FINANCE_FILE_DIR_KEY, DEFAULT_FINANCE_DIR);
            props.store(out, "Auto-generated default config");
        } catch (IOException e) {
            throw new showException("无法创建默认配置文件: " + e.getMessage(), e);
        }
    }

    /**
     * Loads settings from the configuration file.
     * @throws showException For errors reading the config file
     */
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

    /**
     * Saves current settings to the configuration file.
     * @throws showException For errors writing to the config file
     */
    private static void saveSettings() throws showException {
        Properties props = new Properties();
        props.setProperty(FINANCE_FILE_DIR_KEY, currentFinanceFileDirectory);

        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "应用程序设置");
        } catch (IOException e) {
            throw new showException("保存设置时出错: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the file path for a user's finance data.
     * @param username User identifier
     * @return Path to user's finance CSV file (e.g., "user_finance_data/username_finance.csv")
     */
    public static String getFinanceFilePath(String username) {
        return currentFinanceFileDirectory + File.separator + username + "_finance.csv";
    }

    /**
     * Sets the finance data directory and migrates existing files.
     * @param newDirectory New directory path for finance data
     * @throws showException For directory creation or file migration failures
     */
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

    /**
     * Gets the current finance data directory.
     * @return Current directory path for finance data
     */
    public static String getCurrentFinanceDirectory() {
        return currentFinanceFileDirectory;
    }

    /**
     * Creates a new finance data file for a user with default headers.
     * @param username User identifier
     * @throws showException For file creation or header writing failures
     */
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