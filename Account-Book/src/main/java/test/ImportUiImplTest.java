package test;// ImportUiImplTest.java
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import controller.Impl.UserControllerImpl;
import view.Impl.ImportUiImpl;

class ImportUiImplTest {
    private ImportUiImpl importUi;
    private DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Date", "Amount", "Category", "Description"}, 0
    );

    @BeforeEach
    void setUp() {
        JPanel panel = new JPanel();
        UserControllerImpl userController = new UserControllerImpl();
        importUi = new ImportUiImpl(panel, userController);
        importUi.model = model; // 直接注入测试用的model
    }

    @Test
    void testAddValidEntry() {
        importUi.dateField.setText("2023-10-01");
        importUi.amountField.setText("99.99");
        importUi.categoryField.setText("Food");
        importUi.descriptionField.setText("Dinner");

        importUi.addEntry();

        assertEquals(1, model.getRowCount());
        assertEquals("2023-10-01", model.getValueAt(0, 0));
    }

    @Test
    void testInvalidAmountInput() {
        importUi.dateField.setText("2023-10-01");
        importUi.amountField.setText("invalid");

        assertThrows(NumberFormatException.class, () -> {
            importUi.addEntry();
        });
    }
}