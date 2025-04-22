package view.Impl;

import view.BaseUi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class BaseUiImpl implements BaseUi {

    private JPanel sidebar;
    private JPanel contentPanel;
    private final Map<String, Runnable> viewMap = new HashMap<>();
    private Object currentView; // 新增成员变量
    private final Map<String, Object> viewCache = new HashMap<>(); // 视图缓存

    public void BaseWindow() {
        JFrame frame = new JFrame("AccountBook");
        frame.setSize(900, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // 初始化映射关系
        initViewMap();

        // 创建侧边栏和主内容区
        createSidebar();

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(new JLabel("默认内容", SwingConstants.CENTER), BorderLayout.CENTER);

        frame.add(sidebar, BorderLayout.WEST);
        frame.add(contentPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private void createSidebar() {
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.LIGHT_GRAY);

        // 按钮尺寸统一设置
        Dimension buttonSize = new Dimension(120, 40);

        // 创建侧边栏按钮
        String[] buttonLabels = {"Account Book", "Report Forms", "Import", "Setting"};
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
    }

    private void initViewMap() {
        viewMap.put("Account Book", () -> {
            AccountBookUiImpl accountUi = new AccountBookUiImpl(contentPanel);
            accountUi.AccountBookWindow();
        });
        viewMap.put("Report Forms", () -> {
            ReportFormsUiImpl reportUi = new ReportFormsUiImpl(contentPanel);
            reportUi.ReportFormsWindow();
        });
        viewMap.put("Import", () -> {
            // 使用 computeIfAbsent 保证单例
            ImportUiImpl importUi = (ImportUiImpl) viewCache.computeIfAbsent(
                    "Import",
                    k -> new ImportUiImpl(contentPanel)
            );
            this.currentView = importUi; // 更新当前视图
            importUi.ImportWindow();
        });
        viewMap.put("Setting", () -> {
            SettingUiImpl settingUi = new SettingUiImpl(contentPanel);
            settingUi.SettingWindow();
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

            if(currentView instanceof ImportUiImpl) {
                ((ImportUiImpl)currentView).autoSave();
            }

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