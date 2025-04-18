package controller;

import model.Entry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 * Author SunMinghao HuangGuanren ChenBoliang
 * Data 2025/4/18
 * ImportController 定义了导入控制器的业务逻辑方法。
 * 包括CSV读写、AI分析、添加和删除数据等核心操作。
 */
public interface ImportController {
    /**
     * 从本地CSV文件加载所有账目记录。
     * @return 账目记录列表
     */
    List<Entry> loadEntries();

    /**
     * 将新账目追加写入CSV文件。
     * @param entry 新账目信息
     */
    void addEntry(Entry entry);

    /**
     * 打开文件选择器导入CSV文件，并返回解析后的账目记录。
     * @param parent 父组件（用于显示对话框）
     * @return 导入的账目记录
     */
    List<Entry> importCSV(JPanel parent);

    /**
     * 将当前表格数据导出为CSV文件。
     * @param model 表格数据模型
     * @param parent 父组件（用于显示文件对话框）
     */
    void exportCSV(DefaultTableModel model, JPanel parent);

    /**
     * 调用AI模型对所有账目的描述进行自动分类。
     * @param model 表格数据模型
     * @param parent 父组件（用于弹窗提示）
     */
    void analyzeWithAI(DefaultTableModel model, JPanel parent);

    /**
     * 使用当前表格数据重写CSV文件。
     * @param model 表格数据模型
     */
    void rewriteCSV(DefaultTableModel model);

    /**
     * 使用账目列表重写CSV文件。
     * @param entries 要写入的账目记录列表
     */
    void rewriteCSV(List<Entry> entries);
}

