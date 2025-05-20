import view.Impl.BaseUiImpl;

import java.io.IOException;

public class main extends javax.swing.JFrame {
    private static final String CSV_FILE_PATH = "D:\\mycode\\Software-project-24\\Account-Book\\src\\main\\resources\\testUser_finance.csv";
    public static void main(String[] args) {
        BaseUiImpl baseUi = new BaseUiImpl();
        baseUi.BaseWindow();
    }
}
