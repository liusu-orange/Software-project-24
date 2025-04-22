package controller;

import model.UserModel;

/**
 * 用户控制器接口，定义用户注册、登录、注销及用户信息相关操作。
 */
public interface UserController {

    /**
     * 注册新用户。
     *
     * @param username 用户名
     * @param password 密码
     * @param gender   性别（true 表示男，false 表示女）
     * @param age      年龄
     * @return 注册是否成功，如果用户名已存在或创建财务文件失败，则返回 false
     */
    boolean register(String username, String password, boolean gender, int age);

    /**
     * 登录验证。
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录是否成功
     */
    boolean login(String username, String password);

    /**
     * 注销当前用户。
     */
    void logout();

    /**
     * 获取当前登录用户。
     *
     * @return 当前用户对象，如果未登录返回 null
     */
    UserModel getCurrentUser();

    /**
     * 检查是否有用户处于登录状态。
     *
     * @return 如果当前已有用户登录，返回 true；否则返回 false
     */
    boolean isLoggedIn();

    /**
     * 获取当前登录用户的用户名。
     *
     * @return 用户名字符串，如果未登录返回 null
     */
    String getCurrentUserUsername();

    /**
     * 获取当前用户对应的财务数据文件路径。
     *
     * @return 财务文件的路径字符串，如果未登录返回 null
     */
    String getCurrentUserFinanceFilePath();
}
