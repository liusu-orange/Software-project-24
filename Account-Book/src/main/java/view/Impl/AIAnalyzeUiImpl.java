package view.Impl;

import controller.Impl.AIAnalyzeControllerImpl;
import controller.Impl.UserControllerImpl;
import view.AIAnalyzeUi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AIAnalyzeUiImpl implements AIAnalyzeUi {
    private JPanel contentPanel;
    private UserControllerImpl userController;
    private JTextArea resultTextArea; // 显示分析结果的文本域
    private JLabel statusLabel;       // 状态提示标签
    private AIAnalyzeControllerImpl aiController; // 分析控制器

    public AIAnalyzeUiImpl(JPanel contentPanel, UserControllerImpl userController) {
        this.contentPanel = contentPanel;
        this.userController = userController;
        // 初始化分析控制器（需传入 userController）
        this.aiController = new AIAnalyzeControllerImpl(userController);
    }

    @Override
    public void AIAnalyzeWindow() {
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout(20, 20));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 标题
        JLabel titleLabel = new JLabel("AI financial analysis ", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 22));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 结果显示区域（带滚动条）
        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setFont(new Font("Consolas", Font.PLAIN, 14)); // 等宽字体便于阅读
        resultTextArea.setLineWrap(true);
        resultTextArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(resultTextArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 状态提示栏
        statusLabel = new JLabel("Click the button to start generating the analysis report", SwingConstants.CENTER);
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setForeground(Color.GRAY);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        // 分析按钮
        JButton analyzeButton = new JButton("AI Analyze");
        analyzeButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        analyzeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 异步执行分析，避免阻塞 UI
                new Thread(() -> {
                    try {
                        statusLabel.setText("Analysis in progress ... please wait");
                        // 调用分析方法（使用 controller 中的 CSV 文件路径）
                        String result = aiController.analyzeBudgetWithDeepSeek(aiController.CSV_FILE);

                        // 在 UI 线程更新结果
                        SwingUtilities.invokeLater(() -> {
                            resultTextArea.setText(result);
                            statusLabel.setText("Analysis complete");
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        SwingUtilities.invokeLater(() -> {
                            resultTextArea.setText("Analysis failed：" + ex.getMessage());
                            statusLabel.setText("Error: please check the file path or network connection");
                        });
                    }
                }).start();
            }
        });

        // 添加按钮到右侧
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(analyzeButton);
        mainPanel.add(buttonPanel, BorderLayout.EAST);

        contentPanel.add(mainPanel);
        contentPanel.revalidate(); // 重新验证布局
        contentPanel.repaint();    // 重新绘制界面
    }
}