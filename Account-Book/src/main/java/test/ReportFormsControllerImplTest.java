package test;

import static org.junit.jupiter.api.Assertions.*;
import controller.Impl.ReportFormsControllerImpl;
import controller.Impl.UserControllerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jfree.data.category.CategoryDataset;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;

class ReportFormsControllerImplTest {
    private ReportFormsControllerImpl controller;
    private UserControllerImpl userController;

    @BeforeEach
    void setUp() throws Exception {
        userController = new UserControllerImpl();
        userController.register("reportUser", "pwd", true, 30);
        userController.login("reportUser", "pwd");

        // 创建测试数据
        String csvPath = userController.getCurrentUserFinanceFilePath();
        try (var writer = new FileWriter(csvPath)) {
            writer.write("Date,Amount,Category,Description\n");
            writer.write("2024-03-01,100.0,Food,Lunch\n");
            writer.write("2024-03-02,200.0,Transportation,Taxi\n");
        }

        controller = new ReportFormsControllerImpl(userController);
    }

    @Test
    void testDatasetCreation() throws IOException, ParseException {
        String CSV_FILE = userController.getCurrentUserFinanceFilePath();
        CategoryDataset dataset = null;
        try {
            dataset = controller.createCategoryDataset(controller.groupRecordsByDate(controller.readCSV(CSV_FILE)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        assertEquals(2, dataset.getColumnCount());
        assertEquals(100.0, dataset.getValue("Total Expenses", "2024-03-01"));
    }
}