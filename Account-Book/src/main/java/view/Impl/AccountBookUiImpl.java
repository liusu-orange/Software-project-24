package view.Impl;

import javax.swing.*;
import java.awt.*;
// import java.awt.event.ActionEvent;
// import java.awt.event.ActionListener;
public class AccountBookUiImpl {
    private JPanel contentPanel;
    public AccountBookUiImpl(JPanel contentPanel) {
        this.contentPanel = contentPanel;
    }
    public void AccountBookWindow() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("这里是 Account Book 界面"));
        contentPanel.add(panel, BorderLayout.CENTER);
    }
}
