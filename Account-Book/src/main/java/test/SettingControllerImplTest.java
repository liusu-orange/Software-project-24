package test;

import static org.junit.jupiter.api.Assertions.*;
import controller.Impl.SettingControllerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;

class SettingControllerImplTest {
    private static final String TEST_DIR = "test_config";

    @BeforeEach
    void cleanUp() {
        new File(TEST_DIR).delete();
    }

    @Test
    void testDirectoryMigration() throws Exception {
        // 初始目录设置
        SettingControllerImpl.setFinanceFileDirectory(TEST_DIR);
        assertEquals(TEST_DIR, SettingControllerImpl.getCurrentFinanceDirectory());

        // 验证目录创建
        assertTrue(new File(TEST_DIR).exists());
    }

    @Test
    void testUserFileCreation() throws Exception {
        SettingControllerImpl.createUserFinanceFile("testUser");
        String path = SettingControllerImpl.getFinanceFilePath("testUser");
        assertTrue(new File(path).exists());
        assertTrue(new File(path).length() > 0);
    }
}