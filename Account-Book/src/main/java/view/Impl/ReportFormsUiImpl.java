package view.Impl;

import javax.swing.*;
import java.awt.*;
// import java.awt.event.ActionEvent;
// import java.awt.event.ActionListener;
public class ReportFormsUiImpl {
    private JPanel contentPanel;
    public ReportFormsUiImpl(JPanel contentPanel) {
        this.contentPanel = contentPanel;
    }
    public void ReportFormsWindow() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("这里是 Report Forms 界面"));
        contentPanel.add(panel, BorderLayout.CENTER);
    }
}
