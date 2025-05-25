// AccountBookUiImplTest.java
package test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import javax.swing.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import controller.Impl.UserControllerImpl;
import view.Impl.AccountBookUiImpl;

class AccountBookUiImplTest {
    private AccountBookUiImpl ui;
    private JPanel contentPanel = new JPanel();

    @BeforeEach
    void setUp() throws Exception {
        UserControllerImpl userController = new UserControllerImpl();
        ui = new AccountBookUiImpl(contentPanel, userController);

        // 在EDT线程中初始化UI组件
        CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeAndWait(() -> {
            ui.AccountBookWindow();
            latch.countDown();
        });
        latch.await();
    }

    @Test
    void testDateSelectorInitialization() {
        assertNotNull(ui.startYearCombo);
        assertEquals(Calendar.getInstance().get(Calendar.YEAR),
                ui.startYearCombo.getSelectedItem());
    }

    @Test
    void testValidDateConversion() {
        SwingUtilities.invokeLater(() -> {
            ui.startYearCombo.setSelectedItem(2023);
            ui.startMonthCombo.setSelectedItem(12);
            ui.startDayCombo.setSelectedItem(31);
        });

        // 添加短暂延迟确保UI更新
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String date = ui.getSelectedDate(true);
        assertEquals("2023-12-31", date);
    }
}