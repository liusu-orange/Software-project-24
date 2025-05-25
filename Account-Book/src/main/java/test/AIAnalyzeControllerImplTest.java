package test;

import controller.Impl.AIAnalyzeControllerImpl;
import controller.Impl.UserControllerImpl;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.FileWriter;

class AIAnalyzeControllerImplTest {
    private AIAnalyzeControllerImpl controller;
    private UserControllerImpl userController;

    @BeforeEach
    void setUp() throws Exception {
        userController = new UserControllerImpl();
        userController.register("aiUser", "pwd", true, 30);
        userController.login("aiUser", "pwd");
        controller = new AIAnalyzeControllerImpl(userController);

        // 创建测试CSV
        String csvPath = userController.getCurrentUserFinanceFilePath();
        try (var writer = new FileWriter(csvPath)) {
            writer.write("Date,Amount,Category,Description\n");
            writer.write("2024-03-01,300.0,Food,Groceries\n");
            writer.write("2024-03-05,500.0,Entertainment,Movie\n");
        }
    }

    @Test
    void testBudgetAnalysis() throws Exception {
        String analysis = controller.analyzeBudgetWithDeepSeek(controller.CSV_FILE);
        assertTrue(analysis.contains("Budget Analysis"));
        assertTrue(analysis.contains("Food"));
        assertTrue(analysis.contains("Entertainment"));
    }
}