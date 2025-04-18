package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 工具类：每次动态读取配置文件中的参数，确保修改后立刻生效
 */
public class ConfigLoader {
    private static final String CONFIG_FILE = "config.properties";

    /**
     * 每次调用都重新加载配置文件，确保配置项是最新的
     */
    public static String get(String key) {
        Properties properties = new Properties();
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("无法加载配置文件：" + CONFIG_FILE);
            }
            properties.load(input);
            return properties.getProperty(key);
        } catch (IOException ex) {
            throw new RuntimeException("读取配置文件失败", ex);
        }
    }
}
