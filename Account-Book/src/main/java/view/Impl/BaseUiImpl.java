package view.Impl;

import controller.Impl.UserControllerImpl;
import view.BaseUi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
/**
 * Base UI implementation for the account book application, providing core navigation and authentication views.
 *
 * @author Boliang Chen Haoming Lei
 * @version 1.2.0
 * @since v1.0.0
 */
public class BaseUiImpl implements BaseUi {
    private JFrame frame;
    private JPanel sidebar;
    private JPanel contentPanel;
    private final Map<String, Runnable> viewMap = new HashMap<>();
    private UserControllerImpl userController;
    private JLabel userStatusLabel;
    /**
     * Initializes the base UI with user authentication logic.
     */
    public BaseUiImpl() {
        userController = new UserControllerImpl();
    }
    /**
     * Displays the main application window with login, registration, and sidebar navigation.
     */
    public void BaseWindow() {
        frame = new JFrame("AccountBook");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // 初始化映射关系
        initViewMap();

        // 创建顶部状态栏
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userStatusLabel = new JLabel("Not logged in");
        statusBar.add(userStatusLabel);
        frame.add(statusBar, BorderLayout.NORTH);

        // 创建侧边栏和主内容区
        createSidebar();

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);

        // 默认显示登录界面
        showLoginView();

        frame.add(sidebar, BorderLayout.WEST);
        frame.add(contentPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }
    /**
     * Shows the login view with username/password input and authentication logic.
     */
private void showLoginView() {
    contentPanel.removeAll();

    JPanel loginPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    int row = 0; // 用于管理布局的行数

    // 添加图片
    gbc.gridx = 0;
    gbc.gridy = row++;
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.CENTER;

    // 加载图片（替换为你的图片路径）
    ImageIcon imageIcon = new ImageIcon("Account-Book\\src\\main\\resources\\AccountBook.png");
    Image image = imageIcon.getImage().getScaledInstance(200, 100, Image.SCALE_AREA_AVERAGING);
    JLabel label = new JLabel(new ImageIcon(image));

    loginPanel.add(label, gbc);

    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridwidth = 1;

    // 用户名
    gbc.gridx = 0;
    gbc.gridy = row;
    loginPanel.add(new JLabel("User name:"), gbc);

    gbc.gridx = 1;
    JTextField usernameField = new JTextField(15);
    loginPanel.add(usernameField, gbc);

    // 密码
    gbc.gridx = 0;
    gbc.gridy = ++row;
    loginPanel.add(new JLabel("Password:"), gbc);

    gbc.gridx = 1;
    JPasswordField passwordField = new JPasswordField(15);
    loginPanel.add(passwordField, gbc);

    // 登录按钮
    gbc.gridx = 0;
    gbc.gridy = ++row;
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.CENTER;
    JButton loginButton = new JButton("Log in");
    loginButton.addActionListener(e -> {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Username and password cannot be empty",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (userController.login(username, password)) {
            userStatusLabel.setText("Logged in: " + username);
            contentPanel.removeAll();

            // 加载并缩放图片（使用 SCALE_AREA_AVERAGING 提高质量）
            Image image1 = imageIcon.getImage().getScaledInstance(260, 100, Image.SCALE_AREA_AVERAGING);
            JLabel imageLabel = new JLabel(new ImageIcon(image1));


// 创建欢迎文字
            JLabel textLabel = new JLabel("Welcome to use the accounting book system");
            textLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
            textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

// 创建垂直排列的容器
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setOpaque(false);
            panel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // 添加图片和文字
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(imageLabel);
            panel.add(Box.createVerticalStrut(10)); // 图片和文字间距
            panel.add(textLabel);

            // 用外层容器让整体居中
            JPanel wrapper = new JPanel(new GridBagLayout());
            wrapper.add(panel);

            contentPanel.add(wrapper, BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();

        } else {
            JOptionPane.showMessageDialog(null,
                    "Invalid username or password",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    });
    loginPanel.add(loginButton, gbc);

    // 注册按钮
    gbc.gridy = ++row;
    JButton registerButton = new JButton("Register a new user");
    registerButton.addActionListener(e -> showRegisterView());
    loginPanel.add(registerButton, gbc);

    contentPanel.add(loginPanel, BorderLayout.CENTER);
    contentPanel.revalidate();
    contentPanel.repaint();
}
    /**
     * Shows the user registration view with form validation and account creation.
     */
    private void showRegisterView() {
        contentPanel.removeAll();

        JPanel registerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 用户名
        gbc.gridx = 0;
        gbc.gridy = 0;
        registerPanel.add(new JLabel("User name:"), gbc);

        gbc.gridx = 1;
        JTextField usernameField = new JTextField(15);
        registerPanel.add(usernameField, gbc);

        // 密码
        gbc.gridx = 0;
        gbc.gridy = 1;
        registerPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField(15);
        registerPanel.add(passwordField, gbc);

        // 性别
        gbc.gridx = 0;
        gbc.gridy = 2;
        registerPanel.add(new JLabel("Gender:"), gbc);

        gbc.gridx = 1;
        JComboBox<String> genderCombo = new JComboBox<>(new String[]{"man", "woman"});
        registerPanel.add(genderCombo, gbc);

        // 年龄
        gbc.gridx = 0;
        gbc.gridy = 3;
        registerPanel.add(new JLabel("Age:"), gbc);

        gbc.gridx = 1;
        JSpinner ageSpinner = new JSpinner(new SpinnerNumberModel(18, 1, 120, 1));
        registerPanel.add(ageSpinner, gbc);

        // 注册按钮
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            boolean gender = genderCombo.getSelectedIndex() == 0; // 0=男, 1=女
            int age = (Integer) ageSpinner.getValue();

            if (userController.register(username, password, gender, age)) {
                JOptionPane.showMessageDialog(frame, "Registration successful. Please log in", "Registration successful", JOptionPane.INFORMATION_MESSAGE);
                showLoginView();
            } else {
                JOptionPane.showMessageDialog(frame, "The username already exists", "Registration failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        registerPanel.add(registerButton, gbc);

        // 返回登录按钮
        gbc.gridy = 5;
        JButton backButton = new JButton("Return to login");
        backButton.addActionListener(e -> showLoginView());
        registerPanel.add(backButton, gbc);

        contentPanel.add(registerPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    /**
     * Creates the sidebar navigation panel with view buttons (Account Book, Reports, etc.).
     */
    private void createSidebar() {
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.LIGHT_GRAY);

        // 按钮尺寸统一设置
        Dimension buttonSize = new Dimension(120, 40);

        // 创建侧边栏按钮
        String[] buttonLabels = {"Account Book", "Report Forms", "Import", "AI Analyze", "Setting"};
        for (String label : buttonLabels) {
            String htmlLabel = label;
            if (label.length() > 20) {
                int mid = label.length() / 2;
                htmlLabel = label.substring(0, mid) + "<br>" + label.substring(mid);
            }

            JButton button = new JButton("<html>" + htmlLabel + "</html>");
            button.setAlignmentX(Component.CENTER_ALIGNMENT);

            // 设置统一的尺寸
            button.setPreferredSize(buttonSize);
            button.setMaximumSize(buttonSize);
            button.setMinimumSize(buttonSize);

            // 设置文字居中
            button.setHorizontalTextPosition(SwingConstants.CENTER);
            button.setVerticalTextPosition(SwingConstants.CENTER);

            // 添加监听器
            button.addActionListener(new SidebarButtonListener(label));

            sidebar.add(button);
        }

        // 添加垂直胶水填充底部空间
        sidebar.add(Box.createVerticalGlue());

        // 添加登出按钮
        JButton logoutButton = new JButton("Log out");
        logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutButton.setPreferredSize(buttonSize);
        logoutButton.setMaximumSize(buttonSize);
        logoutButton.setMinimumSize(buttonSize);
        logoutButton.setHorizontalTextPosition(SwingConstants.CENTER);
        logoutButton.setVerticalTextPosition(SwingConstants.CENTER);
        logoutButton.setBackground(Color.PINK);
        logoutButton.addActionListener(new SidebarButtonListener("Logout")); // 使用统一监听器

        sidebar.add(logoutButton);
    }
    /**
     * Initializes view mappings for sidebar buttons to corresponding UI components.
     */
    private void initViewMap() {
        viewMap.put("Account Book", () -> {
            if (!userController.isLoggedIn()) {
                JOptionPane.showMessageDialog(frame, "Please log in first.", "Not logged in", JOptionPane.WARNING_MESSAGE);
                showLoginView();
                return;
            }
            AccountBookUiImpl accountUi = new AccountBookUiImpl(contentPanel, userController);
            accountUi.AccountBookWindow();
        });
        viewMap.put("Report Forms", () -> {
            if (!userController.isLoggedIn()) {
                JOptionPane.showMessageDialog(frame, "Please log in first.", "Not logged in", JOptionPane.WARNING_MESSAGE);
                showLoginView();
                return;
            }
            ReportFormsUiImpl reportUi = new ReportFormsUiImpl(contentPanel,userController);
            reportUi.ReportFormsWindow();
        });
        viewMap.put("Import", () -> {
            if (!userController.isLoggedIn()) {
                JOptionPane.showMessageDialog(frame, "Please log in first.", "Not logged in", JOptionPane.WARNING_MESSAGE);
                showLoginView();
                return;
            }
            ImportUiImpl importUi = new ImportUiImpl(contentPanel, userController);
            importUi.ImportWindow();
        });
        viewMap.put("Setting", () -> {
            if (!userController.isLoggedIn()) {
                JOptionPane.showMessageDialog(frame, "Please log in first.", "Not logged in", JOptionPane.WARNING_MESSAGE);
                showLoginView();
                return;
            }
            SettingUiImpl settingUi = new SettingUiImpl(contentPanel,userController);
            settingUi.SettingWindow();
        });
        viewMap.put("AI Analyze", () -> {
            if (!userController.isLoggedIn()) {
                JOptionPane.showMessageDialog(frame, "Please log in first.", "Not logged in", JOptionPane.WARNING_MESSAGE);
                showLoginView();
                return;
            }
            AIAnalyzeUiImpl aiUi = new AIAnalyzeUiImpl(contentPanel, userController);
            aiUi.AIAnalyzeWindow();
        });
        viewMap.put("Logout", () -> {
            if (userController.isLoggedIn()) {
                userController.logout();
                userStatusLabel.setText("Not logged in");
                JOptionPane.showMessageDialog(frame, "Successfully logged out", "Log out", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Not logged in currently", "Hint", JOptionPane.WARNING_MESSAGE);
            }
            showLoginView();
        });
    }
    /**
     * Listener for sidebar buttons to switch between different application views.
     */
    private class SidebarButtonListener implements ActionListener {
        private final String buttonLabel;

        public SidebarButtonListener(String label) {
            this.buttonLabel = label;
        }
        /**
         * Handles view switching based on the clicked sidebar button.
         * @param e Action event triggered by button click
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            contentPanel.removeAll();

            Runnable viewFunc = viewMap.get(buttonLabel);
            if (viewFunc != null) {
                viewFunc.run();
            } else {
                contentPanel.add(new JLabel("Unknown option: " + buttonLabel, SwingConstants.CENTER), BorderLayout.CENTER);
            }

            contentPanel.revalidate();
            contentPanel.repaint();
        }
    }
}
