package view.Impl;

import controller.Impl.AccountBookControllerImpl;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DateFormatter;
import java.awt.*;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.Map;

public class AccountBookUiImpl {
    private JPanel contentPanel;
    private JScrollPane resultScrollPane;
    private JPanel resultPanel;
    private AccountBookControllerImpl controller;
    // 添加总额显示面板
    private JPanel totalPanel;
    private JLabel totalLabel;

    // ⚡ 声明为成员变量
    private JFormattedTextField startDateField;
    private JFormattedTextField endDateField;

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

        // ⚡ 调整初始化顺序
        autoLoadInitialData();
        contentPanel.revalidate();
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // ⚡ 使用带默认值的创建方法
        startDateField = createDateFieldWithDefault(150,
                controller.getDefaultDateRange()[0]);
        endDateField = createDateFieldWithDefault(150,
                controller.getDefaultDateRange()[1]);

        JButton searchButton = new JButton("Search");

        addComponent(panel, new JLabel("Start Date:"), gbc, 0, 0);
        addComponent(panel, startDateField, gbc, 1, 0);
        addComponent(panel, new JLabel("End Date:"), gbc, 2, 0);
        addComponent(panel, endDateField, gbc, 3, 0);
        addComponent(panel, searchButton, gbc, 4, 0);

        searchButton.addActionListener(e -> handleSearch(
                startDateField.getText().trim(),
                endDateField.getText().trim()
        ));

        totalPanel = new JPanel();
        totalLabel = new JLabel("本月消费总额：--");
        totalPanel.add(totalLabel);
        totalPanel.setBackground(new Color(240, 240, 240));

        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.add(panel, BorderLayout.NORTH);
        containerPanel.add(totalPanel, BorderLayout.CENTER);

        return containerPanel;
    }

    // ⚡ 增强的日期输入框创建
    private JFormattedTextField createDateFieldWithDefault(int width, String defaultValue) {
        JFormattedTextField field = new JFormattedTextField(
                new SafeDateFormatter(controller.getDateFormat()));

        configureTextField(field, width);
        try {
            field.setValue(controller.getDateFormat().parse(defaultValue));
        } catch (ParseException e) {
            field.setValue(new Date());
        }
        return field;
    }

    private static class SafeDateFormatter extends DateFormatter {
        public SafeDateFormatter(SimpleDateFormat format) {
            super(format);
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            if (value == null) {
                return this.getFormat().format(new Date());
            }
            if (!(value instanceof Date)) {
                throw new ParseException("Invalid date type: " +
                        value.getClass().getName(), 0);
            }
            return super.valueToString(value);
        }
    }

    private void autoLoadInitialData() {
        try {
            // ⚡ 空值安全处理
            Date startDate = Optional.ofNullable(controller.getFirstDayOfMonth())
                    .orElse(new Date());
            Date endDate = new Date();

            startDateField.setValue(startDate);
            endDateField.setValue(endDate);

            handleSearch(
                    controller.formatDate(startDate),
                    controller.formatDate(endDate)
            );
        } catch (IllegalArgumentException e) {
            showError("自动加载失败: " + e.getMessage());
        }
    }

    private void handleSearch(String startInput, String endInput) {
        try {
            if (startInput.isEmpty() || endInput.isEmpty()) {
                throw new ParseException("日期不能为空", 0);
            }

            Map<Date, List<AccountBookControllerImpl.Record>> filteredData =
                    controller.searchRecords(startInput, endInput);
            updateResultPanel(filteredData);
        } catch (IOException ex) {
            showError("<html><b>文件读取失败：</b>" + ex.getMessage()
                    + "<br>请检查文件路径：</html>");
        } catch (RuntimeException ex) { // 捕获新增的运行时异常
            showError("<html><b>数据解析错误：</b>"
                    + ex.getCause().getMessage() + "</html>");
        } catch (Exception ex) {
            showError("<html><b>搜索错误：</b>"
                    + ex.getMessage().replace("\n", "<br>") + "</html>");
        }
    }


    // 其余保持不变的方法
    private void initializeResultPanel() {
        resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultScrollPane = new JScrollPane(resultPanel);
        resultScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    private void configureTextField(JFormattedTextField field, int width) {
        field.setColumns(10);
        field.setPreferredSize(new Dimension(width, 30));
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setToolTipText("<html>格式：<b>YYYY-MM-DD</b><br>示例：2025-04-18</html>");
    }

    private void addComponent(JPanel panel, Component comp,
                              GridBagConstraints gbc, int x, int y) {
        gbc.gridx = x;
        gbc.gridy = y;
        panel.add(comp, gbc);
    }

    private void updateResultPanel(Map<Date, List<AccountBookControllerImpl.Record>> data) {
        resultPanel.removeAll();
        if (data.isEmpty()) {
            resultPanel.add(new JLabel("未找到相关记录"));
        } else {
            data.forEach((date, records) ->
                    resultPanel.add(createDatePanel(date, records)));
        }
        // 计算总金额
        double total = data.values().stream()
                .flatMap(List::stream)
                .mapToDouble(AccountBookControllerImpl.Record::amount)
                .sum();

        updateTotalDisplay(total);  // 更新总额显示

        resultPanel.revalidate();
        resultScrollPane.repaint();
    }

    // 添加总额更新方法
    private void updateTotalDisplay(double total) {
        String formattedTotal = String.format("¥%.2f", total);
        String htmlText = "<html><b style='color:#D32F2F; font-size:14px;'>"
                + "消费总额：</b><span style='font-size:16px;'>%s</span></html>";
        totalLabel.setText(String.format(htmlText, formattedTotal));

        // 添加动态效果
        totalPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(224, 224, 224)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private JPanel createDatePanel(Date date, List<AccountBookControllerImpl.Record> records) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(controller.formatDate(date)));

        double total = records.stream()
                .mapToDouble(AccountBookControllerImpl.Record::amount)
                .sum();
        panel.add(new JLabel(String.format("总计：¥%.2f", total)));

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"金额", "分类", "描述"}, 0);
        records.forEach(r -> model.addRow(
                new Object[]{r.amount(), r.category(), r.description()}));
        panel.add(new JScrollPane(new JTable(model)));

        return panel;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(contentPanel, message,
                "系统提示", JOptionPane.ERROR_MESSAGE);
    }

    private void initializeDateFormat() {
        controller.initializeDateFormat();
    }
}
