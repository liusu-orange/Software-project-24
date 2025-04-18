package view.Impl;

import controller.Impl.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * 视图类：设置面板UI，
 * 实现文件存储路径显示、选择、保存
 */
public class SettingUiImpl extends JPanel {
    private JPanel contentPanel;
    private JTextField pathField;
    private JButton browseButton;
    private JButton saveButton;

    private SettingControllerImpl controller;

    public SettingUiImpl(JPanel contentPanel){
        this.contentPanel = contentPanel;
        this.controller = new SettingControllerImpl();
    }

    public void SettingWindow(){
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout(10, 10));

        pathField = new JTextField(controller.getCurrentCsvPath());
        pathField.setEditable(false);

        browseButton = new JButton("浏览...");
        saveButton = new JButton("保存路径");

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.add(pathField, BorderLayout.CENTER);
        centerPanel.add(browseButton, BorderLayout.EAST);

        add(new JLabel("当前CSV路径:"), BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(saveButton, BorderLayout.SOUTH);

        browseButton.addActionListener(e -> chooseFile());
        saveButton.addActionListener(e -> savePath());
    }

//    public SettingUiImpl() {
//        controller = new SettingControllerImpl();
//
//        setLayout(new BorderLayout(10, 10));
//
//        pathField = new JTextField(controller.getCurrentCsvPath());
//        pathField.setEditable(false);
//
//        browseButton = new JButton("浏览...");
//        saveButton = new JButton("保存路径");
//
//        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
//        centerPanel.add(pathField, BorderLayout.CENTER);
//        centerPanel.add(browseButton, BorderLayout.EAST);
//
//        add(new JLabel("当前CSV路径:"), BorderLayout.NORTH);
//        add(centerPanel, BorderLayout.CENTER);
//        add(saveButton, BorderLayout.SOUTH);
//
//        browseButton.addActionListener(e -> chooseFile());
//        saveButton.addActionListener(e -> savePath());
//    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            pathField.setText(chooser.getSelectedFile().getPath());
        }
    }

    private void savePath() {
        String newPath = pathField.getText();
        try {
            controller.updateCsvPath(newPath);

            // 新增：路径保存成功后，立刻刷新 Import 模块（如果已加载）
            JOptionPane.showMessageDialog(this, "路径保存成功，已立刻生效！");

            /*
            1.将之前的csv文件与新建的csv文件合并
            2.（现在的记录显示是直接从文件读取，还是存到类中）
                重新加载现在的csv
             */
            //notifyImportModule();

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "保存失败: " + ex.getMessage());
        }
    }

}
