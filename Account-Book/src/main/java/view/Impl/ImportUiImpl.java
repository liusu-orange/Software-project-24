package view.Impl;

import javax.swing.*;
import java.awt.*;
// import java.awt.event.ActionEvent;
// import java.awt.event.ActionListener;
public class ImportUiImpl {
    private JPanel contentPanel;
    public ImportUiImpl(JPanel contentPanel) {
        this.contentPanel = contentPanel;
    }
    public void ImportWindow() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("这里是 Import 界面"));
        contentPanel.add(panel, BorderLayout.CENTER);
    }
}
