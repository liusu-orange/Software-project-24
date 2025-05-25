package view.Impl;

import controller.Impl.ImportControllerImpl;
import controller.Impl.SettingControllerImpl;
import controller.Impl.UserControllerImpl;
import controller.Impl.showException;
import model.UserModel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
/**
 * UI implementation for application settings, including financial data directory management and user profile editing.
 *
 * @author Boliang Chen
 * @version 1.0.0
 * @since v1.0.0
 */
public class SettingUiImpl {

    private final JPanel contentPanel;
    private JTextField pathField;
    private JButton chooseButton;
    private JButton saveButton;
    private JPasswordField passwordField;
    private JRadioButton maleRadio;
    private JRadioButton femaleRadio;
    private JComboBox<Integer> ageComboBox;
    private final ButtonGroup genderGroup = new ButtonGroup();

    private SettingControllerImpl controller;
    private UserControllerImpl userController;
    /**
     * Initializes the settings UI with user controller and configuration logic.
     * @param contentPanel Parent container panel
     * @param userController User controller instance for authentication
     */
    public SettingUiImpl(JPanel contentPanel, UserControllerImpl userController) {
        this.contentPanel = contentPanel;
        this.userController = userController;
        this.controller = new SettingControllerImpl(userController);
    }
    /**
     * Displays the settings window with directory selection and user profile fields.
     */
    public void SettingWindow() {
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());

        JPanel settingPanel = createMainSettingPanel();
        contentPanel.add(settingPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();

        setupButtonListeners();
    }

    private JPanel createMainSettingPanel() {
        JPanel settingPanel = new JPanel(new BorderLayout());
        settingPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 顶部：文件路径面板
        settingPanel.add(createFilePathPanel(), BorderLayout.NORTH);

        // 中间：将用户设置面板放入左上角
        JPanel centerWrapper = new JPanel(new BorderLayout());
        JPanel leftTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftTopPanel.add(createUserSettingPanel());
        centerWrapper.add(leftTopPanel, BorderLayout.NORTH); // 把面板贴在左上角

        settingPanel.add(centerWrapper, BorderLayout.CENTER);

        // 右下角：保存按钮面板
        JPanel southWrapper = new JPanel(new BorderLayout());
        southWrapper.add(Box.createHorizontalGlue(), BorderLayout.CENTER);
        southWrapper.add(createSaveButtonPanel(), BorderLayout.EAST);
        settingPanel.add(southWrapper, BorderLayout.SOUTH);

        return settingPanel;
    }


    /**
     * Creates the financial data directory selection panel.
     * @return Configured directory path panel
     */
    // 创建文件路径面板
    private JPanel createFilePathPanel() {
        JPanel pathPanel = new JPanel(new BorderLayout(1, 1));
        pathField = new JTextField(SettingControllerImpl.getCurrentFinanceDirectory());
        pathField.setEditable(false);
        chooseButton = new JButton("Select the new directory");

        JPanel labelFieldPanel = new JPanel(new BorderLayout(1, 1));
        labelFieldPanel.add(new JLabel("The current financial data storage directory:"), BorderLayout.WEST);
        labelFieldPanel.add(pathField, BorderLayout.CENTER);

        pathPanel.add(labelFieldPanel, BorderLayout.CENTER);
        pathPanel.add(chooseButton, BorderLayout.EAST);

        return pathPanel;
    }
    /**
     * Creates the user profile settings panel (password, gender, age).
     * @return Configured user info panel
     */
    // 创建用户信息设置面板（密码、性别、年龄）
    private JPanel createUserSettingPanel() {
        JPanel userPanel = new JPanel(new GridBagLayout());
        userPanel.setBorder(BorderFactory.createTitledBorder("UserInfo Settings")); // 添加边框标题

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // 每个组件之间的间距
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        UserModel currentUser = userController.getCurrentUser();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(contentPanel, "No user logged in.", "Error", JOptionPane.ERROR_MESSAGE);
            return userPanel;
        }

        // 行 1：密码标签 + 密码输入框
        gbc.gridx = 0;
        gbc.gridy = 0;
        userPanel.add(new JLabel("New Password (leave blank to keep unchanged):"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        userPanel.add(passwordField, gbc);

        // 行 2：性别标签 + 性别选择
        gbc.gridx = 0;
        gbc.gridy = 1;
        userPanel.add(new JLabel("Gender:"), gbc);

        gbc.gridx = 1;
        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        maleRadio = new JRadioButton("Male");
        femaleRadio = new JRadioButton("Female");
        genderGroup.add(maleRadio);
        genderGroup.add(femaleRadio);
        if (currentUser.isGender()) {
            maleRadio.setSelected(true);
        } else {
            femaleRadio.setSelected(true);
        }
        genderPanel.add(maleRadio);
        genderPanel.add(femaleRadio);
        userPanel.add(genderPanel, gbc);

        // 行 3：年龄标签 + 年龄下拉框
        gbc.gridx = 0;
        gbc.gridy = 2;
        userPanel.add(new JLabel("Age:"), gbc);

        gbc.gridx = 1;
        ageComboBox = new JComboBox<>();
        for (int i = 1; i <= 120; i++) {
            ageComboBox.addItem(i);
        }
        ageComboBox.setSelectedItem(currentUser.getAge());
        userPanel.add(ageComboBox, gbc);

        return userPanel;
    }

    /**
     * Creates the save button panel for applying settings.
     * @return Panel containing the save button
     */
    private JPanel createSaveButtonPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Save settings");
        bottomPanel.add(saveButton);
        return bottomPanel;
    }
    /**
     * Sets up action listeners for directory selection and save operations.
     */
    private void setupButtonListeners() {
        chooseButton.addActionListener(e -> {
            JFileChooser dirChooser = new JFileChooser();
            dirChooser.setDialogTitle("Select the financial data storage directory");
            dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            File currentDir = new File(SettingControllerImpl.getCurrentFinanceDirectory());
            if (currentDir.exists()) {
                dirChooser.setCurrentDirectory(currentDir);
            }

            int result = dirChooser.showOpenDialog(contentPanel);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedDir = dirChooser.getSelectedFile();
                pathField.setText(selectedDir.getAbsolutePath());
            }
        });

        saveButton.addActionListener(e -> {
            String newPath = pathField.getText();
            UserModel currentUser = userController.getCurrentUser();
            if (currentUser == null) {
                JOptionPane.showMessageDialog(contentPanel, "No user logged in.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // 保存目录设置
                SettingControllerImpl.setFinanceFileDirectory(newPath);

                // 修改用户信息
                String username = currentUser.getUsername();
                String newPassword = new String(passwordField.getPassword()).trim();
                boolean newGender = maleRadio.isSelected();
                int newAge = (Integer) ageComboBox.getSelectedItem();

                if (!newPassword.isEmpty()) {
                    userController.updateUserPassword(username, newPassword);
                }
                userController.updateUserGender(username, newGender);
                userController.updateUserAge(username, newAge);

                JOptionPane.showMessageDialog(contentPanel,
                        "The settings have been saved successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (showException ex) {
                JOptionPane.showMessageDialog(contentPanel,
                        "Failed to save directory settings: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                pathField.setText(SettingControllerImpl.getCurrentFinanceDirectory());
            }
        });
    }
}
