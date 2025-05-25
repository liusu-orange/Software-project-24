package test;

import controller.Impl.AccountBookControllerImpl;
import controller.Impl.UserControllerImpl;

import org.junit.jupiter.api.*;
import java.io.*;
import java.text.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AccountBookControllerImplTest {

    private AccountBookControllerImpl controller;
    private static final String TEST_CSV_PATH = "testUser_finance.csv";

    // 创建一个模拟的 UserControllerImpl 类
    static class MockUserControllerImpl extends UserControllerImpl {
        @Override
        public String getCurrentUserFinanceFilePath() {
            return TEST_CSV_PATH;
        }
    }

    @BeforeAll
    static void setupCSV() throws IOException {
        // 测试CSV文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TEST_CSV_PATH))) {
            writer.write("2024-04-01,1000,Salary,Monthly Salary\n");
            writer.write("2024-04-15,-200,Food,Lunch\n");
            writer.write("2024-05-01,500,Gift,Birthday Gift\n");
        }
    }

    @BeforeEach
    void setUp() {
        controller = new AccountBookControllerImpl(new MockUserControllerImpl());
        controller.initializeDateFormat();
    }

    @AfterAll
    static void cleanUp() {
        new File(TEST_CSV_PATH).delete();
    }

    @Test
    @DisplayName("测试搜索记录：正常日期范围")
    void testSearchRecordsValidRange() throws Exception {
        Map<Date, List<AccountBookControllerImpl.Record>> result =
                controller.searchRecords("2024-04-01", "2024-04-30");

        // 应该包含两个记录（4月1日和4月15日）
        assertEquals(2, result.values().stream().mapToInt(List::size).sum());
    }

    @Test
    @DisplayName("测试搜索记录：无匹配记录")
    void testSearchRecordsNoMatch() throws Exception {
        Map<Date, List<AccountBookControllerImpl.Record>> result =
                controller.searchRecords("2023-01-01", "2023-01-31");

        // 应该为空
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试非法日期格式")
    void testInvalidDateFormat() {
        assertThrows(ParseException.class, () -> {
            controller.searchRecords("2024/04/01", "2024/04/30");
        });
    }

    @Test
    @DisplayName("测试开始日期晚于结束日期")
    void testStartDateAfterEndDate() {
        assertThrows(IllegalArgumentException.class, () -> {
            controller.searchRecords("2024-05-01", "2024-04-01");
        });
    }

    @Test
    @DisplayName("测试日期格式化方法")
    void testFormatDate() throws ParseException {
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse("2024-04-01");
        String formatted = controller.formatDate(date);
        assertEquals("2024-04-01", formatted);
    }

    @Test
    @DisplayName("测试CSV解析失败行被跳过")
    void testInvalidCSVLineIgnored() throws IOException {
        // 加入一行无效数据
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TEST_CSV_PATH, true))) {
            writer.write("InvalidLine\n");
        }

        // 无异常，且仍然能解析原始三条有效记录
        var records = AccountBookControllerImpl.CSVUtils.readCSV(TEST_CSV_PATH);
        assertEquals(3, records.size());  // 依然只包含三条有效记录
    }
}
