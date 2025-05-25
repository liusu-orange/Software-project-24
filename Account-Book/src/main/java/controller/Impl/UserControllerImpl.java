package controller.Impl;

import model.UserModel;
import util.MD5Util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
/**
 * User controller for managing user authentication, registration, and profile settings.
 *
 * @author Boliang Chen
 * @version 1.0.0
 * @since v1.0.0
 */
public class UserControllerImpl {
    private static final String USER_FILE = "user.csv";
    private static final String DEFAULT_USERNAME = "testUser";
    private static final String DEFAULT_PASSWORD = MD5Util.encrypt("testUser");
    private static final String DEFAULT_FINANCE_FILE = "/testUser_finance.csv";
    private List<UserModel> users;
    private UserModel currentUser;
    /**
     * Initializes the user controller and loads user data from file.
     * Creates default admin user if no users exist.
     */
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
    /**
     * Registers a new user with encrypted password and creates their finance file.
     * @param username Unique user identifier
     * @param password Clear-text password (auto-encrypted via MD5)
     * @param gender User's gender
     * @param age User's age
     * @return True if registration succeeds, false if username exists or file creation fails
     */
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
            System.err.println("The migration of financial documents failed: " + e.getMessage());
            // 回滚用户注册
            users.removeIf(u -> u.getUsername().equals(username));
            saveUsersToFile();
            return false;
        }
        return true;
    }
    /**
     * Logs in a user with username and password.
     * @param username User identifier
     * @param password Clear-text password (auto-encrypted for validation)
     * @return True if credentials are valid, false otherwise
     */
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
    /**
     * Logs out the current user.
     */
    // 用户注销
    public void logout() {
        currentUser = null;
    }
    /**
     * Gets the currently logged-in user.
     * @return UserModel instance or null if not logged in
     */
    // 获取当前用户
    public UserModel getCurrentUser() {
        return currentUser;
    }
    /**
     * Checks if a user is currently logged in.
     * @return True if logged in, false otherwise
     */
    // 检查是否已登录
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    /**
     * Gets the current user's username.
     * @return Username string or null if not logged in
     */
    // 获取用户名
    public String getCurrentUserUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }
    /**
     * Gets the file path for the current user's finance data.
     * @return Path to user's finance CSV file (e.g., "user_finance_data/username_finance.csv")
     */
    // 获取当前用户的财务文件路径
    public String getCurrentUserFinanceFilePath() {
        if (currentUser == null) {
            return null;
        }
        return SettingControllerImpl.getFinanceFilePath(currentUser.getUsername());
    }
    /**
     * Updates a user's password (auto-encrypted via MD5).
     * @param username User identifier
     * @param newPassword New clear-text password
     * @return True if update succeeds, false if user not found
     */
    // 修改用户密码
    public boolean updateUserPassword(String username, String newPassword) {
        for (UserModel user : users) {
            if (user.getUsername().equals(username)) {
                user.setPassword(MD5Util.encrypt(newPassword));
                saveUsersToFile();
                return true;
            }
        }
        return false;
    }
    /**
     * Updates a user's gender.
     * @param username User identifier
     * @param newGender New gender value
     * @return True if update succeeds, false if user not found
     */
    // 修改用户性别
    public boolean updateUserGender(String username, boolean newGender) {
        for (UserModel user : users) {
            if (user.getUsername().equals(username)) {
                user.setGender(newGender);
                saveUsersToFile();
                return true;
            }
        }
        return false;
    }
    /**
     * Updates a user's age.
     * @param username User identifier
     * @param newAge New age value
     * @return True if update succeeds, false if user not found
     */
    // 修改用户年龄
    public boolean updateUserAge(String username, int newAge) {
        for (UserModel user : users) {
            if (user.getUsername().equals(username)) {
                user.setAge(newAge);
                saveUsersToFile();
                return true;
            }
        }
        return false;
    }
    /**
     * Deletes a user by username and removes their finance file.
     * @param username User identifier to delete
     * @return True if deletion succeeds, false if user not found
     */
    // 删除指定用户名的用户
    public boolean deleteUser(String username) {
        boolean removed = users.removeIf(user -> user.getUsername().equals(username));
        if (removed) {
            saveUsersToFile();
        }
        return removed;
    }

}