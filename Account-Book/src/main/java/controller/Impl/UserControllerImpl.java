package controller.Impl;

import model.UserModel;
import util.MD5Util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class UserControllerImpl {
    private static final String USER_FILE = "user.csv";
    private static final String DEFAULT_USERNAME = "testUser";
    private static final String DEFAULT_PASSWORD = MD5Util.encrypt("testUser");
    private static final String DEFAULT_FINANCE_FILE = "/testUser_finance.csv";
    private List<UserModel> users;
    private UserModel currentUser;

    public UserControllerImpl() {
        this.users = new ArrayList<>();
        this.currentUser = null;
        loadUsersFromFile();
    }

    // 从CSV文件加载用户数据
    private void loadUsersFromFile() {
        File file = new File(USER_FILE);
        if (!file.exists()) {
            // 如果文件不存在，创建默认管理员用户
            users.add(new UserModel(DEFAULT_USERNAME, DEFAULT_PASSWORD, true, 30));
            saveUsersToFile();
            initializeDefaultUserFinanceFile();
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String username = parts[0];
                    String password = parts[1];
                    boolean gender = Boolean.parseBoolean(parts[2]);
                    int age = Integer.parseInt(parts[3]);
                    users.add(new UserModel(username, password, gender, age));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // 如果读取失败，至少保证有一个管理员用户
            users.add(new UserModel(DEFAULT_USERNAME, DEFAULT_PASSWORD, true, 30));
            initializeDefaultUserFinanceFile();
        }
    }

    // 初始化默认用户的财务文件
    private void initializeDefaultUserFinanceFile() {
        try {
            // 从resources目录读取默认财务文件
            InputStream inputStream = getClass().getResourceAsStream(DEFAULT_FINANCE_FILE);
            if (inputStream == null) {
                throw new FileNotFoundException("Default finance file not found in resources: " + DEFAULT_FINANCE_FILE);
            }

            // 获取财务文件路径
            String filePath = SettingControllerImpl.getFinanceFilePath(DEFAULT_USERNAME);
            Path targetPath = Path.of(filePath);

            // 确保目录存在
            Files.createDirectories(targetPath.getParent());

            // 复制资源文件到目标位置
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed to initialize default user finance file: " + e.getMessage());
            // 如果复制失败，创建一个空的财务文件
            try {
                SettingControllerImpl.createUserFinanceFile(DEFAULT_USERNAME);
            } catch (Exception ex) {
                System.err.println("Failed to create empty finance file: " + ex.getMessage());
            }
        }
    }

    // 保存用户数据到CSV文件
    private void saveUsersToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE))) {
            for (UserModel user : users) {
                writer.write(user.getUsername() + "," +
                        user.getPassword() + "," +
                        user.isGender() + "," +
                        user.getAge());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 用户注册
    public boolean register(String username, String password, boolean gender, int age) {
        // 检查用户名是否已存在
        for (UserModel user : users) {
            if (user.getUsername().equals(username)) {
                return false;
            }
        }

        //调用MD5进行加密
        String encryptPassword = MD5Util.encrypt(password);

        users.add(new UserModel(username, encryptPassword, gender, age));
        saveUsersToFile();

        // 为新用户创建Finance文件
        try {
            SettingControllerImpl.createUserFinanceFile(username);
        } catch (Exception e) {
            System.err.println("财务文件创建失败: " + e.getMessage());
            // 回滚用户注册
            users.removeIf(u -> u.getUsername().equals(username));
            saveUsersToFile();
            return false;
        }
        return true;
    }

    // 用户登录
    public boolean login(String username, String password) {
        for (UserModel user : users) {
            String encryptPassword = MD5Util.encrypt(password);
            if (user.getUsername().equals(username) && user.getPassword().equals(encryptPassword)) {
                currentUser = user;
                return true;
            }
        }
        return false;
    }

    // 用户注销
    public void logout() {
        currentUser = null;
    }

    // 获取当前用户
    public UserModel getCurrentUser() {
        return currentUser;
    }

    // 检查是否已登录
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    // 获取用户名
    public String getCurrentUserUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }

    // 获取当前用户的财务文件路径
    public String getCurrentUserFinanceFilePath() {
        if (currentUser == null) {
            return null;
        }
        return SettingControllerImpl.getFinanceFilePath(currentUser.getUsername());
    }
}