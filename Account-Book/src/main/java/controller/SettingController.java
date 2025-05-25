package controller;

import controller.Impl.SettingControllerImpl;

/**
 * 设置控制器接口（适配器模式，兼容静态方法）
 */
public interface SettingController {

    /**
     * 获取指定用户的财务文件路径
     * @param username 用户名
     * @return 该用户的财务文件完整路径
     */
    default String getFinanceFilePath(String username) {
        return SettingControllerImpl.getFinanceFilePath(username);
    }

    /**
     * 设置财务文件存储目录
     * @param newDirectory 新的目录路径
     * @throws Exception 当目录创建失败或文件移动失败时抛出异常
     */
    default void setFinanceFileDirectory(String newDirectory) throws Exception {
        SettingControllerImpl.setFinanceFileDirectory(newDirectory);
    }

    /**
     * 获取当前财务文件存储目录
     * @return 当前财务文件存储目录路径
     */
    default String getCurrentFinanceDirectory() {
        return SettingControllerImpl.getCurrentFinanceDirectory();
    }

    /**
     * 创建用户财务文件
     * @param username 用户名
     * @throws Exception 当文件创建失败时抛出异常
     */
    default void createUserFinanceFile(String username) throws Exception {
        SettingControllerImpl.createUserFinanceFile(username);
    }
}