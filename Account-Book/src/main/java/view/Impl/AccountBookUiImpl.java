package view.Impl;

import controller.Impl.AccountBookControllerImpl;
import controller.Impl.UserControllerImpl;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * 记账本界面实现类，负责用户界面构建和交互逻辑
 */
public class AccountBookUiImpl {
    private JPanel contentPanel;
    private JScrollPane resultScrollPane;
    private JPanel resultPanel;

    private JPanel totalPanel;
    private JLabel totalLabel;

    private AccountBookControllerImpl controller;
    private UserControllerImpl userController;

    // 日期选择组件
    private JComboBox<Integer> startYearCombo, startMonthCombo, startDayCombo;
    private JComboBox<Integer> endYearCombo, endMonthCombo, endDayCombo;

    /**
     * 构造函数初始化核心组件
     * @param contentPanel 父容器面板
     */
    public AccountBookUiImpl(JPanel contentPanel, UserControllerImpl userController) {
        this.contentPanel = contentPanel;
        this.userController = userController;
        this.controller = new AccountBookControllerImpl(userController);
        initializeDateFormat();
    }

    /**
     * 创建主界面窗口，包含控制面板和结果展示区域
     */
    public void AccountBookWindow() {
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());

        JPanel controlPanel = createControlPanel();
        initializeResultPanel();

        contentPanel.add(controlPanel, BorderLayout.NORTH);
        contentPanel.add(resultScrollPane, BorderLayout.CENTER);

        autoLoadInitialData();
        contentPanel.revalidate();
    }

    /**
     * 创建包含日期选择器和搜索按钮的控制面板
     * @return 组装完成的面板组件
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JPanel startDatePanel = createDateSelectorPanel(true);
        JPanel endDatePanel = createDateSelectorPanel(false);
        JButton searchButton = new JButton("Search");

        addComponent(panel, new JLabel("Start date: "), gbc, 0, 0);
        addComponent(panel, startDatePanel, gbc, 1, 0);
        addComponent(panel, new JLabel("End date: "), gbc, 2, 0);
        addComponent(panel, endDatePanel, gbc, 3, 0);
        addComponent(panel, searchButton, gbc, 4, 0);

        searchButton.addActionListener(e -> handleSearch(
                getSelectedDate(true),
                getSelectedDate(false)
        ));

        totalPanel = new JPanel();
        totalLabel = new JLabel("Total consumption this month：--");
        totalPanel.add(totalLabel);
        totalPanel.setBackground(new Color(240, 240, 240));

        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.add(panel, BorderLayout.NORTH);
        containerPanel.add(totalPanel, BorderLayout.CENTER);

        return containerPanel;
    }

    /**
     * 创建日期选择器面板（支持动态日期更新）
     * @param isStartDate 标识是否为起始日期选择器
     * @return 包含年月日选择的下拉面板
     */
    private JPanel createDateSelectorPanel(boolean isStartDate) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        // 年份选择（当前年前后各5年）
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        JComboBox<Integer> yearCombo = new JComboBox<>(
                IntStream.range(currentYear - 5, currentYear + 6)
                        .boxed().toArray(Integer[]::new));

        // 月份选择
        JComboBox<Integer> monthCombo = new JComboBox<>(
                IntStream.rangeClosed(1, 12).boxed().toArray(Integer[]::new));

        // 日期选择（动态生成）
        JComboBox<Integer> dayCombo = new JComboBox<>();

        // 设置初始值
        Calendar cal = Calendar.getInstance();
        yearCombo.setSelectedItem(cal.get(Calendar.YEAR));
        monthCombo.setSelectedItem(cal.get(Calendar.MONTH) + 1);
        updateDayCombo(dayCombo, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
        dayCombo.setSelectedItem(cal.get(Calendar.DAY_OF_MONTH));

        // 添加联动监听
        yearCombo.addActionListener(e -> updateDayCombo(dayCombo,
                (Integer)yearCombo.getSelectedItem(),
                (Integer)monthCombo.getSelectedItem()));

        monthCombo.addActionListener(e -> updateDayCombo(dayCombo,
                (Integer)yearCombo.getSelectedItem(),
                (Integer)monthCombo.getSelectedItem()));

        // 保存组件引用
        if(isStartDate) {
            startYearCombo = yearCombo;
            startMonthCombo = monthCombo;
            startDayCombo = dayCombo;
        } else {
            endYearCombo = yearCombo;
            endMonthCombo = monthCombo;
            endDayCombo = dayCombo;
        }

        panel.add(yearCombo);
        panel.add(new JLabel("Year"));
        panel.add(monthCombo);
        panel.add(new JLabel("Month"));
        panel.add(dayCombo);
        panel.add(new JLabel("Day"));

        return panel;
    }

    /**
     * 更新日期下拉框的天数（根据年月动态调整）
     * @param dayCombo 目标下拉框组件
     * @param year 当前选择的年份
     * @param month 当前选择的月份（1-12）
     */
    private void updateDayCombo(JComboBox<Integer> dayCombo, int year, int month) {
        dayCombo.removeAllItems();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DATE, 1);

        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        IntStream.rangeClosed(1, maxDay).forEach(dayCombo::addItem);
    }

    /**
     * 获取格式化日期字符串（智能识别起始/结束日期组件）
     * @param isStart 日期类型标识 (Date type flag)
     * @return 标准化的日期字符串（格式：yyyy-MM-dd）
     */
    private String getSelectedDate(boolean isStart) {
        int year = isStart ? (Integer)startYearCombo.getSelectedItem()
                : (Integer)endYearCombo.getSelectedItem();
        int month = isStart ? (Integer)startMonthCombo.getSelectedItem()
                : (Integer)endMonthCombo.getSelectedItem();
        int day = isStart ? (Integer)startDayCombo.getSelectedItem()
                : (Integer)endDayCombo.getSelectedItem();

        return String.format("%04d-%02d-%02d", year, month, day);
    }

    /**
     * 设置日期选择器组件值
     * @param yearCombo 年份下拉框
     * @param monthCombo 月份下拉框
     * @param dayCombo 日期下拉框
     * @param date 要设置的日期对象
     */
    private void setDateComponents(JComboBox<Integer> yearCombo,
                                   JComboBox<Integer> monthCombo,
                                   JComboBox<Integer> dayCombo,
                                   Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        yearCombo.setSelectedItem(cal.get(Calendar.YEAR));
        monthCombo.setSelectedItem(cal.get(Calendar.MONTH) + 1);
        updateDayCombo(dayCombo, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
        dayCombo.setSelectedItem(cal.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * 处理搜索操作（包含异常处理）
     * @param startInput 起始日期字符串（yyyy-MM-dd）
     * @param endInput 结束日期字符串（yyyy-MM-dd）
     */
    private void handleSearch(String startInput, String endInput) {
        try {
            if (startInput.isEmpty() || endInput.isEmpty()) {
                throw new ParseException("The date cannot be empty", 0);
            }

            Map<Date, List<AccountBookControllerImpl.Record>> filteredData =
                    controller.searchRecords(startInput, endInput);
            updateResultPanel(filteredData);
        } catch (IOException ex) {
            showError("<html><b>File reading failed:</b>" + ex.getMessage()
                    + "<br>Please check the file path:</html>");
        } catch (RuntimeException ex) { // 捕获新增的运行时异常
            showError("<html><b>Data parsing error:</b>"
                    + ex.getCause().getMessage() + "</html>");
        } catch (Exception ex) {
            showError("<html><b>Search error:</b>"
                    + ex.getMessage().replace("\n", "<br>") + "</html>");
        }
    }

    /**
     * 更新结果展示面板（含数据空状态处理）
     * @param data 过滤后的数据集
     */
    private void updateResultPanel(Map<Date, List<AccountBookControllerImpl.Record>> data) {
        resultPanel.removeAll();
        if (data.isEmpty()) {
            resultPanel.add(new JLabel("No relevant records were found"));
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

    /**
     * 创建带日期标题的记录展示面板
     * @param date 面板对应的日期
     * @param records 当日的消费记录列表
     * @return 组装完成的日期面板
     */
    private JPanel createDatePanel(Date date, List<AccountBookControllerImpl.Record> records) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        // ================= 动态尺寸计算 =================
        int rowHeight = 28; // 行高（根据字体大小可调节）
        int headerHeight = 30; // 表头高度
        int maxVisibleRows = 8; // 最大可见行数

        // 动态计算理想高度
        int idealHeight = headerHeight + (Math.min(records.size(), maxVisibleRows) * rowHeight);
        int minHeight = 30; // 最小面板高度

        // ===== 新增标题面板 =====
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(245, 245, 245));

        // 左侧日期标签
        String dateText = String.format("<html><div style='padding:3px;'>"
                        + "<b>%s</b> <font color='#666666' size='2'>(%d 条)</font></div></html>",
                controller.formatDate(date), records.size());
        JLabel dateLabel = new JLabel(dateText);
        dateLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));

        // 右侧总计标签
        double dailyTotal = records.stream()
                .mapToDouble(AccountBookControllerImpl.Record::amount)
                .sum();
        String totalText = String.format("<html><div style='padding:3px;'>"
                        + "<font color='#D32F2F'>total：</font>"
                        + "<font color='black'>¥%.2f</font></div></html>",
                dailyTotal);
        JLabel totalLabel = new JLabel(totalText);
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        // 组装标题面板
        titlePanel.add(dateLabel, BorderLayout.WEST);
        titlePanel.add(totalLabel, BorderLayout.EAST);
        titlePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));

        // ================= 智能表格创建 =================
        JTable table = createSmartTable(records);
        JScrollPane scrollPane = new JScrollPane(table);

        // 滚动条策略配置（根据行数动态调整）
        if (records.size() > maxVisibleRows) {
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            idealHeight = headerHeight + (maxVisibleRows * rowHeight) + 5; // 包含滚动条补偿
        } else {
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        }

        // ================= 布局优化 =================
        scrollPane.setPreferredSize(new Dimension(scrollPane.getWidth(), Math.max(idealHeight, minHeight)));

        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 创建智能表格（支持列宽自适应和自动换行）
     * @param records 要展示的记录数据
     * @return 配置完成的表格组件
     */
    private JTable createSmartTable(List<AccountBookControllerImpl.Record> records) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Amount", "Category", "Description"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);

        // ================= 列宽自适应配置 =================
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // 关闭自动调整
        TableColumnModel colModel = table.getColumnModel();

        // 金额列（固定宽度）
        TableColumn amountCol = colModel.getColumn(0);
        amountCol.setPreferredWidth(90);
        amountCol.setMaxWidth(120);

        // 分类列（弹性宽度）
        TableColumn categoryCol = colModel.getColumn(1);
        categoryCol.setPreferredWidth(120);
        categoryCol.setMinWidth(100);

        // 描述列（最大弹性宽度）
        TableColumn descCol = colModel.getColumn(2);
        descCol.setPreferredWidth(300);
        descCol.setMinWidth(150);

        // ================= 视觉优化 =================
        table.setRowHeight(28);
        table.setShowVerticalLines(true);
        table.setGridColor(new Color(220, 220, 220));
        table.getTableHeader().setBackground(new Color(245, 245, 245));

        DefaultTableCellRenderer wrapRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JTextArea textArea = new JTextArea(value != null ? value.toString() : "");
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true); // 按单词换行
                textArea.setFont(table.getFont());
                textArea.setBorder(UIManager.getBorder("Table.cellBorder"));

                // 动态计算最佳高度
                int width = table.getColumnModel().getColumn(column).getWidth();
                textArea.setSize(new Dimension(width, Integer.MAX_VALUE));
                int preferredHeight = textArea.getPreferredSize().height + 4; // 加边距

                // 设置行高（但不超过120px）
                if (table.getRowHeight(row) != Math.min(preferredHeight, 120)) {
                    table.setRowHeight(row, Math.min(preferredHeight, 120));
                }
                return textArea;
            }
        };
        // 应用渲染器到描述列
        descCol.setCellRenderer(wrapRenderer);

        // ================= 调整自动调整策略 =================
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); // 启用自动调整
        table.doLayout(); // 立即应用布局

        // 数据填充
        records.forEach(r -> model.addRow(new Object[]{
                String.format("¥%.2f", r.amount()),
                r.category(),
                r.description()
        }));

        return table;

    }

    /**
     * 自动加载初始数据（默认当月数据）
     */
    private void autoLoadInitialData() {
        try {
            // 设置开始日期为当月第一天
            Calendar firstCal = Calendar.getInstance();
            firstCal.set(Calendar.DAY_OF_MONTH, 1);
            setDateComponents(startYearCombo, startMonthCombo, startDayCombo, firstCal.getTime());

            // 设置结束日期为当天
            setDateComponents(endYearCombo, endMonthCombo, endDayCombo, new Date());

            handleSearch(getSelectedDate(true), getSelectedDate(false));
        } catch (Exception e) {
            showError("Automatic loading failed: " + e.getMessage());
        }
    }

    private void initializeResultPanel() {
        resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultScrollPane = new JScrollPane(resultPanel);
        resultScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    private void addComponent(JPanel panel, Component comp,
                              GridBagConstraints gbc, int x, int y) {
        gbc.gridx = x;
        gbc.gridy = y;
        panel.add(comp, gbc);
    }

    /**
     * 更新总额显示面板（新增）
     * @param total 计算得到的总金额
     */
    private void updateTotalDisplay(double total) {
        String formattedTotal = String.format("¥%.2f", total);
        String htmlText = "<html><b style='color:#D32F2F; font-size:14px;'>"
                + "Total consumption：</b><span style='font-size:16px;'>%s</span></html>";
        totalLabel.setText(String.format(htmlText, formattedTotal));

        // 添加动态边框效果
        totalPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(224, 224, 224)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    /**
     * 创建当日总计标签（新增）
     * @param records 当日记录列表
     * @return 带样式的JLabel组件
     */
    private JLabel createTotalLabel(List<AccountBookControllerImpl.Record> records) {
        double total = records.stream()
                .mapToDouble(AccountBookControllerImpl.Record::amount)
                .sum();

        JLabel label = new JLabel("Total for the day: " + String.format("¥%.2f", total));
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(3, 5, 3, 10)
        ));
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        return label;
    }


    private void showError(String message) {
        JOptionPane.showMessageDialog(contentPanel, message,
                "System prompt", JOptionPane.ERROR_MESSAGE);
    }

    private void initializeDateFormat() {
        controller.initializeDateFormat();
    }
}
