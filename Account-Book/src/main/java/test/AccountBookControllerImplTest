package test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.ParseException;

import controller.Impl.AccountBookControllerImpl;
import controller.Impl.UserControllerImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AccountBookControllerImplTest {

    private AccountBookControllerImpl controller;
    private UserControllerImpl mockUserController;

    // 测试用临时CSV文件路径
    private static final String TEST_CSV_PATH = "src/test/resources/test_finance.csv";

    @Before
    public void setUp() throws Exception {
        // 初始化模拟UserController
        mockUserController = mock(UserControllerImpl.class);
        when(mockUserController.getCurrentUserFinanceFilePath()).thenReturn(TEST_CSV_PATH);

        // 创建测试用CSV文件
        createTestCSV();

        // 初始化Controller并设置日期格式
        controller = new AccountBookControllerImpl(mockUserController);
        controller.initializeDateFormat();
    }

    /**
     * 测试无效日期格式的异常
     */
    @Test(expected = ParseException.class)
    public void testSearchRecords_InvalidDateFormat() throws Exception {
        controller.searchRecords("2023/01/01", "2023-01-03");
    }

    /**
     * 测试开始日期晚于结束日期的异常
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSearchRecords_InvalidDateOrder() throws Exception {
        controller.searchRecords("2023-01-03", "2023-01-01");
    }

    /**
     * 通过反射测试私有方法sanitizeDate
     */
    @Test
    public void testSanitizeDate() throws Exception {
        Method method = AccountBookControllerImpl.class
                .getDeclaredMethod("sanitizeDate", String.class);
        method.setAccessible(true);

        String input = " 2023-01-01 ";
        String output = (String) method.invoke(controller, input);
        assertEquals("2023-01-01", output);
    }

    /**
     * 测试CSV文件解析逻辑
     */
    @Test
    public void testCSVUtils_ParseValidLine() {
        String line = "2023-01-01, ¥100.50, Food, Lunch";
        Record record = AccountBookControllerImpl.CSVUtils.parseLine(line);

        assertNotNull(record);
        assertEquals("Food", ((AccountBookControllerImpl.Record) record).category());
        assertEquals(100.50, ((AccountBookControllerImpl.Record) record).amount(), 0.001);
    }

    /**
     * 测试CSV解析跳过无效记录
     */
    @Test
    public void testCSVUtils_SkipInvalidLine() {
        String line = "InvalidDate, ABC, Food, Lunch";
        Record record = AccountBookControllerImpl.CSVUtils.parseLine(line);
        assertNull(record);
    }

    //-------------------------------------------
    // 辅助方法
    //-------------------------------------------

    /**
     * 创建测试用CSV文件内容
     */
    private void createTestCSV() throws IOException {
        File file = new File(TEST_CSV_PATH);
        file.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("2023-01-01, 100.0, Food, Breakfast\n");
            writer.write("2023-01-01, 200.0, Transport, Taxi\n");
            writer.write("2023-01-03, 50.0, Food, Coffee\n");
            writer.write("InvalidLine\n"); // 测试跳过无效行
        }
    }
}
