package test;

import controller.Impl.ImportControllerImpl;
import controller.Impl.UserControllerImpl;
import model.Entry;
import org.junit.jupiter.api.*;

import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ImportControllerImplTest {

    private ImportControllerImpl importController;
    private static final String TEST_CSV_PATH = "testUser_finance.csv";

    static class MockUserControllerImpl extends UserControllerImpl {
        @Override
        public String getCurrentUserFinanceFilePath() {
            return TEST_CSV_PATH;
        }
    }

    @BeforeAll
    static void setupCSV() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TEST_CSV_PATH))) {
            writer.write("Date,Amount,Category,Description\n");
            writer.write("2024-04-01,1000.00,Salary,April salary\n");
            writer.write("2024-04-02,-50.00,Food,Lunch\n");
        }
    }

    @BeforeEach
    void setupController() {
        importController = new ImportControllerImpl(new MockUserControllerImpl());
    }

    @AfterAll
    static void cleanUp() {
        new File(TEST_CSV_PATH).delete();
    }

    @Test
    @DisplayName("测试加载CSV条目")
    void testLoadEntries() {
        List<Entry> entries = importController.loadEntries();
        assertEquals(2, entries.size(), "应该加载2条记录");

        Entry entry = entries.get(0);
        assertEquals("2024-04-01", entry.getDate());
        assertEquals(1000.0, entry.getAmount());
        assertEquals("Salary", entry.getCategory());
    }

    @Test
    @DisplayName("测试追加Entry记录")
    void testAddEntry() throws IOException {
        Entry newEntry = new Entry("2024-04-03", 120.5, "Transport", "Taxi ride");
        importController.addEntry(newEntry);

        List<Entry> entries = importController.loadEntries();
        boolean found = entries.stream()
                .anyMatch(e -> e.getDate().equals("2024-04-03")
                        && e.getCategory().equals("Transport")
                        && e.getDescription().equals("Taxi ride"));
        assertTrue(found, "新记录应该被成功追加");
    }

    @Test
    @DisplayName("测试重写CSV文件（基于List<Entry>）")
    void testRewriteCSVWithEntries() throws IOException {
        List<Entry> testEntries = List.of(
                new Entry("2024-01-01", 500.0, "Gift", "New Year gift"),
                new Entry("2024-01-02", -30.0, "Food", "Snack")
        );
        importController.rewriteCSV(testEntries);

        List<Entry> loaded = importController.loadEntries();
        assertEquals(2, loaded.size(), "CSV应只包含重写后的2条记录");
        assertEquals("Gift", loaded.get(0).getCategory());
    }

    @Test
    @DisplayName("测试重写CSV文件（基于TableModel）")
    void testRewriteCSVWithModel() throws IOException {
        DefaultTableModel model = new DefaultTableModel(
                new Object[][]{
                        {"2024-05-01", 800.0, "Bonus", "Performance bonus"},
                        {"2024-05-02", -100.0, "Entertainment", "Movie"}
                },
                new String[]{"Date", "Amount", "Category", "Description"}
        );
        importController.rewriteCSV(model);

        List<Entry> entries = importController.loadEntries();
        assertEquals(2, entries.size());
        assertEquals("Bonus", entries.get(0).getCategory());
    }
}

