package view.Impl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SettingUiImpl {
    private JPanel contentPanel;
    private final ImageIcon accountIcon = new ImageIcon("account_icon.png"); // 替换为实际图标路径
    private final ImageIcon accountingIcon = new ImageIcon("accounting_icon.png"); // 替换为实际图标路径
    private final ImageIcon personalizationIcon = new ImageIcon("personalization_icon.png"); // 替换为实际图标路径
    private final ImageIcon arrowIcon = new ImageIcon("arrow_icon.png"); // 替换为实际图标路径

    public SettingUiImpl(JPanel contentPanel) {
        this.contentPanel = contentPanel;
    }

    public void SettingWindow() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 1)); // 改为5行1列布局
        panel.setBackground(Color.WHITE);

        // 创建三个类似列表项的面板
        createListItem(panel, "Account Settings", accountIcon);
        createListItem(panel, "Accounting Settings", accountingIcon);
        createListItem(panel, "Personalization", personalizationIcon);

        // 添加两个空白的JPanel来占据最后两行
        panel.add(new JPanel());
        panel.add(new JPanel());

        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void createListItem(JPanel parentPanel, String label, ImageIcon icon) {
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BorderLayout());
        itemPanel.setBackground(Color.WHITE);
        itemPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY)); // 底部边框

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.add(new JLabel(icon));
        leftPanel.add(new JLabel(label));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.add(new JLabel(arrowIcon));

        itemPanel.add(leftPanel, BorderLayout.WEST);
        itemPanel.add(rightPanel, BorderLayout.EAST);

        itemPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                contentPanel.removeAll();
                if (label.equals("Account Settings")) {
                    showSubContent("这是 Account Settings 界面");
                } else if (label.equals("Accounting Settings")) {
                    showSubContent("这是 Accounting Settings 界面");
                } else if (label.equals("Personalization")) {
                    showSubContent("这是 Personalization 界面");
                }
                contentPanel.revalidate();
                contentPanel.repaint();
            }
        });

        parentPanel.add(itemPanel);
    }

    private void showSubContent(String message) {
        JPanel subPanel = new JPanel();
        subPanel.add(new JLabel(message));
        // 添加返回按钮
        JButton backButton = new JButton("返回");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                contentPanel.removeAll();
                SettingWindow();
                contentPanel.revalidate();
                contentPanel.repaint();
            }
        });
        subPanel.add(backButton);
        contentPanel.add(subPanel, BorderLayout.CENTER);
    }
}