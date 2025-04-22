package view.Impl;

import controller.Impl.SettingControllerImpl;
import controller.Impl.showException;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class SettingUiImpl {

    private final JPanel contentPanel;
    private JTextField pathField;
    private JButton chooseButton;
    private JButton saveButton;


    public SettingUiImpl(JPanel contentPanel) {
        this.contentPanel = contentPanel;
    }

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

        //顶部
        settingPanel.add(createFilePathPanel(), BorderLayout.NORTH);
        settingPanel.add(createSaveButtonPanel(), BorderLayout.SOUTH);

        return settingPanel;
    }

    private JPanel createFilePathPanel() {
        JPanel pathPanel = new JPanel(new BorderLayout(1, 1));
        pathField = new JTextField(SettingControllerImpl.getCurrentFinanceDirectory());
        pathField.setEditable(false);
        chooseButton = new JButton("选择新目录");

        JPanel labelFieldPanel = new JPanel(new BorderLayout(1, 1));
        labelFieldPanel.add(new JLabel("当前财务数据存储目录:"), BorderLayout.WEST);
        labelFieldPanel.add(pathField, BorderLayout.CENTER);

        pathPanel.add(labelFieldPanel, BorderLayout.CENTER);
        pathPanel.add(chooseButton, BorderLayout.EAST);

        return pathPanel;
    }

    private JPanel createSaveButtonPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("保存设置");
        bottomPanel.add(saveButton);
        return bottomPanel;
    }

    private void setupButtonListeners() {
        chooseButton.addActionListener(e -> {
            JFileChooser dirChooser = new JFileChooser();
            dirChooser.setDialogTitle("选择财务数据存储目录");
            dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            // Set current directory as the starting point
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
            try {
                SettingControllerImpl.setFinanceFileDirectory(newPath);
                JOptionPane.showMessageDialog(contentPanel,
                        "目录设置保存成功！",
                        "成功",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (showException ex) {
                JOptionPane.showMessageDialog(contentPanel,
                        "目录设置保存失败: " + ex.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
                // Reset to current directory if save fails
                pathField.setText(SettingControllerImpl.getCurrentFinanceDirectory());
            }
        });
    }
}