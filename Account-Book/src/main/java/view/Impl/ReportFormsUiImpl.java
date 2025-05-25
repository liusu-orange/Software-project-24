package view.Impl;

import controller.Impl.UserControllerImpl;
import view.ReportFormsUi;
import controller.ReportFormsController;
import controller.Impl.ReportFormsControllerImpl;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import org.jfree.chart.ChartPanel;
/**
 * UI implementation for generating and displaying financial reports with charts (bar, line, pie).
 *
 * @author Haoming Lei
 * @version 1.0.0
 * @since v1.0.0
 */
public class ReportFormsUiImpl implements ReportFormsUi {
    private JPanel contentPanel;
    private ReportFormsController controller;
    private UserControllerImpl userController;
    /**
     * Initializes the report forms UI with user controller and chart generation logic.
     * @param contentPanel Parent container panel
     * @param userController User controller instance for data access
     */
    public ReportFormsUiImpl(JPanel contentPanel,UserControllerImpl userController) {
        this.contentPanel = contentPanel;
        this.userController = userController; // 初始化UserController
        this.controller = new ReportFormsControllerImpl(userController); // 传入UserController
    }
    /**
     * Displays the report forms window with generated financial charts.
     */
    public void ReportFormsWindow() {
        String path = "Account-Book/src/main/resources/finance_data.csv"; // 或从配置中获取
        List<ChartPanel> panels = controller.generateCharts();

        JPanel mainPanel = new JPanel(new GridLayout(3, 1));
        for (ChartPanel panel : panels) {
            mainPanel.add(panel);
        }

        contentPanel.add(mainPanel, BorderLayout.CENTER);
    }
}
