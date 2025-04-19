package view.Impl;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import controller.Impl.AccountBookControllerImpl;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AccountBookUiImpl {
    private JPanel contentPanel;
    private JScrollPane resultScrollPane;
    private JPanel resultPanel;
    private AccountBookControllerImpl controller;

    public AccountBookUiImpl(JPanel contentPanel) {
        this.contentPanel = contentPanel;
        this.controller = new AccountBookControllerImpl();
        initializeDateFormat();
    }

    public void AccountBookWindow() {
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());

        JPanel controlPanel = createControlPanel();
        initializeResultPanel();

        contentPanel.add(controlPanel, BorderLayout.NORTH);
        contentPanel.add(resultScrollPane, BorderLayout.CENTER);
        contentPanel.revalidate();
    }

    // 控制面板构建
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JFormattedTextField startDateField = createDateField(150);
        JFormattedTextField endDateField = createDateField(150);
        JButton searchButton = new JButton("Search");

        addComponent(panel, new JLabel("Start Date:"), gbc, 0, 0);
        addComponent(panel, startDateField, gbc, 1, 0);
        addComponent(panel, new JLabel("End Date:"), gbc, 2, 0);
        addComponent(panel, endDateField, gbc, 3, 0);
        addComponent(panel, searchButton, gbc, 4, 0);

        searchButton.addActionListener(e -> handleSearch(
                startDateField.getText(),
                endDateField.getText()
        ));

        return panel;
    }

    // 结果面板初始化
    private void initializeResultPanel() {
        resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultScrollPane = new JScrollPane(resultPanel);
        resultScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    // 日期输入框构建
    private JFormattedTextField createDateField(int width) {
        JFormattedTextField field = new JFormattedTextField(controller.getDateFormat());
        configureTextField(field, width);
        return field;
    }

    private void configureTextField(JFormattedTextField field, int width) {
        field.setColumns(10);
        field.setPreferredSize(new Dimension(width, 30));
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setToolTipText("<html>格式：<b>YYYY-MM-DD</b><br>示例：2025-04-18</html>");
    }

    private void addComponent(JPanel panel, Component comp, GridBagConstraints gbc, int x, int y) {
        gbc.gridx = x;
        gbc.gridy = y;
        panel.add(comp, gbc);
    }

    // 核心业务调用
    private void handleSearch(String startInput, String endInput) {
        try {
            Map<Date, List<AccountBookControllerImpl.Record>> filteredData =
                    controller.searchRecords(startInput, endInput);
            updateResultPanel(filteredData);
        } catch (ParseException ex) {
            showError("日期格式错误：" + ex.getMessage());
        } catch (IllegalArgumentException | IOException ex) {
            showError(ex.getMessage());
        }
    }

    // 结果展示
    private void updateResultPanel(Map<Date, List<AccountBookControllerImpl.Record>> data) {
        resultPanel.removeAll();
        if (data.isEmpty()) {
            resultPanel.add(new JLabel("未找到相关记录"));
        } else {
            data.forEach((date, records) -> resultPanel.add(createDatePanel(date, records)));
        }
        resultPanel.revalidate();
        resultScrollPane.repaint();
    }

    private JPanel createDatePanel(Date date, List<AccountBookControllerImpl.Record> records) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(controller.formatDate(date)));

        double total = records.stream()
                .mapToDouble(AccountBookControllerImpl.Record::amount)
                .sum();
        panel.add(new JLabel(String.format("总计：¥%.2f", total)));

        DefaultTableModel model = new DefaultTableModel(new Object[]{"金额", "分类", "描述"}, 0);
        records.forEach(r -> model.addRow(new Object[]{r.amount(), r.category(), r.description()}));
        panel.add(new JScrollPane(new JTable(model)));

        return panel;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(contentPanel,
                "<html><b>错误：</b>" + message + "</html>",
                "系统提示",
                JOptionPane.ERROR_MESSAGE);
    }

    private void initializeDateFormat() {
        // 从Controller获取日期格式配置
        controller.initializeDateFormat();
    }
}
