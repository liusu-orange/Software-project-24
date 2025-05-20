package view.Impl;

import controller.Impl.UserControllerImpl;
import view.BaseUi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class BaseUiImpl implements BaseUi {
    private JFrame frame;
    private JPanel sidebar;
    private JPanel contentPanel;
    private final Map<String, Runnable> viewMap = new HashMap<>();
    private UserControllerImpl userController;
    private JLabel userStatusLabel;

    public BaseUiImpl() {
        userController = new UserControllerImpl();
    }

    public void BaseWindow() {
        frame = new JFrame("AccountBook");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // 初始化映射关系
        initViewMap();

        // 创建顶部状态栏
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userStatusLabel = new JLabel("未登录");
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

    private void showLoginView() {
        contentPanel.removeAll();

        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 用户名
        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(new JLabel("用户名:"), gbc);

        gbc.gridx = 1;
        JTextField usernameField = new JTextField(15);
        loginPanel.add(usernameField, gbc);

        // 密码
        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(new JLabel("密码:"), gbc);

        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField(15);
        loginPanel.add(passwordField, gbc);

        // 登录按钮
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        JButton loginButton = new JButton("登录");
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (userController.login(username, password)) {
                userStatusLabel.setText("已登录: " + username);
                contentPanel.removeAll();
                contentPanel.add(new JLabel("欢迎使用记账本系统", SwingConstants.CENTER), BorderLayout.CENTER);
                contentPanel.revalidate();
                contentPanel.repaint();
            } else {
                JOptionPane.showMessageDialog(frame, "用户名或密码错误", "登录失败", JOptionPane.ERROR_MESSAGE);
            }
        });
        loginPanel.add(loginButton, gbc);

        // 注册按钮
        gbc.gridy = 3;
        JButton registerButton = new JButton("注册新用户");
        registerButton.addActionListener(e -> showRegisterView());
        loginPanel.add(registerButton, gbc);

        contentPanel.add(loginPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showRegisterView() {
        contentPanel.removeAll();

        JPanel registerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 用户名
        gbc.gridx = 0;
        gbc.gridy = 0;
        registerPanel.add(new JLabel("用户名:"), gbc);

        gbc.gridx = 1;
        JTextField usernameField = new JTextField(15);
        registerPanel.add(usernameField, gbc);

        // 密码
        gbc.gridx = 0;
        gbc.gridy = 1;
        registerPanel.add(new JLabel("密码:"), gbc);

        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField(15);
        registerPanel.add(passwordField, gbc);

        // 性别
        gbc.gridx = 0;
        gbc.gridy = 2;
        registerPanel.add(new JLabel("性别:"), gbc);

        gbc.gridx = 1;
        JComboBox<String> genderCombo = new JComboBox<>(new String[]{"男", "女"});
        registerPanel.add(genderCombo, gbc);

        // 年龄
        gbc.gridx = 0;
        gbc.gridy = 3;
        registerPanel.add(new JLabel("年龄:"), gbc);

        gbc.gridx = 1;
        JSpinner ageSpinner = new JSpinner(new SpinnerNumberModel(18, 1, 120, 1));
        registerPanel.add(ageSpinner, gbc);

        // 注册按钮
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        JButton registerButton = new JButton("注册");
        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            boolean gender = genderCombo.getSelectedIndex() == 0; // 0=男, 1=女
            int age = (Integer) ageSpinner.getValue();

            if (userController.register(username, password, gender, age)) {
                JOptionPane.showMessageDialog(frame, "注册成功，请登录", "注册成功", JOptionPane.INFORMATION_MESSAGE);
                showLoginView();
            } else {
                JOptionPane.showMessageDialog(frame, "用户名已存在", "注册失败", JOptionPane.ERROR_MESSAGE);
            }
        });
        registerPanel.add(registerButton, gbc);

        // 返回登录按钮
        gbc.gridy = 5;
        JButton backButton = new JButton("返回登录");
        backButton.addActionListener(e -> showLoginView());
        registerPanel.add(backButton, gbc);

        contentPanel.add(registerPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    //侧边栏
    private void createSidebar() {
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.LIGHT_GRAY);

        // 按钮尺寸统一设置
        Dimension buttonSize = new Dimension(120, 40);

        // 创建侧边栏按钮
        String[] buttonLabels = {"Account Book", "Report Forms", "Import", "Setting","AI Analyze"};
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
        JButton logoutButton = new JButton("登出");
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

    private void initViewMap() {
        viewMap.put("Account Book", () -> {
            if (!userController.isLoggedIn()) {
                JOptionPane.showMessageDialog(frame, "请先登录", "未登录", JOptionPane.WARNING_MESSAGE);
                showLoginView();
                return;
            }
            AccountBookUiImpl accountUi = new AccountBookUiImpl(contentPanel, userController);
            accountUi.AccountBookWindow();
        });
        viewMap.put("Report Forms", () -> {
            if (!userController.isLoggedIn()) {
                JOptionPane.showMessageDialog(frame, "请先登录", "未登录", JOptionPane.WARNING_MESSAGE);
                showLoginView();
                return;
            }
            ReportFormsUiImpl reportUi = new ReportFormsUiImpl(contentPanel,userController);
            reportUi.ReportFormsWindow();
        });
        viewMap.put("Import", () -> {
            if (!userController.isLoggedIn()) {
                JOptionPane.showMessageDialog(frame, "请先登录", "未登录", JOptionPane.WARNING_MESSAGE);
                showLoginView();
                return;
            }
            ImportUiImpl importUi = new ImportUiImpl(contentPanel, userController);
            importUi.ImportWindow();
        });
        viewMap.put("Setting", () -> {
            if (!userController.isLoggedIn()) {
                JOptionPane.showMessageDialog(frame, "请先登录", "未登录", JOptionPane.WARNING_MESSAGE);
                showLoginView();
                return;
            }
            SettingUiImpl settingUi = new SettingUiImpl(contentPanel);
            settingUi.SettingWindow();
        });
        viewMap.put("AI Analyze", () -> {
            if (!userController.isLoggedIn()) {
                JOptionPane.showMessageDialog(frame, "请先登录", "未登录", JOptionPane.WARNING_MESSAGE);
                showLoginView();
                return;
            }
            AIAnalyzeUiImpl aiUi = new AIAnalyzeUiImpl(contentPanel, userController);
            aiUi.AIAnalyzeWindow();
        });
        viewMap.put("Logout", () -> {
            if (userController.isLoggedIn()) {
                userController.logout();
                userStatusLabel.setText("未登录");
                JOptionPane.showMessageDialog(frame, "已成功登出", "登出", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "当前未登录", "提示", JOptionPane.WARNING_MESSAGE);
            }
            showLoginView(); // 无论是否已登录，点击登出都回到登录界面
        });
    }

    private class SidebarButtonListener implements ActionListener {
        private final String buttonLabel;

        public SidebarButtonListener(String label) {
            this.buttonLabel = label;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            contentPanel.removeAll();

            Runnable viewFunc = viewMap.get(buttonLabel);
            if (viewFunc != null) {
                viewFunc.run();
            } else {
                contentPanel.add(new JLabel("未知选项: " + buttonLabel, SwingConstants.CENTER), BorderLayout.CENTER);
            }

            contentPanel.revalidate();
            contentPanel.repaint();
        }
    }
}