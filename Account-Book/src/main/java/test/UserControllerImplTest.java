package test;

import controller.Impl.UserControllerImpl;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;

class UserControllerImplTest {
    private UserControllerImpl userController;
    private static final String TEST_USER = "testUser123";
    private static final String TEST_PWD = "password123";

    @BeforeEach
    void setUp() {
        new File("user.csv").delete();
        userController = new UserControllerImpl();
    }

    @Test
    void testRegisterAndLogin() {
        // 测试注册
        assertTrue(userController.register(TEST_USER, TEST_PWD, true, 25));

        // 测试重复注册
        assertFalse(userController.register(TEST_USER, "newPwd", false, 30));

        // 测试登录
        assertTrue(userController.login(TEST_USER, TEST_PWD));
        assertFalse(userController.login(TEST_USER, "wrongPwd"));
    }

    @Test
    void testFinanceFileCreation() {
        // 注册后显式登录
        userController.register(TEST_USER, TEST_PWD, true, 25);
        userController.login(TEST_USER, TEST_PWD); // 新增登录操作
        String path = userController.getCurrentUserFinanceFilePath();
        assertNotNull(path);
        assertTrue(new File(path).exists());
    }

}