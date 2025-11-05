package org.truecaller.prefixmatcher.config;

import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.InputStream;

@Slf4j
public class ConfigManager {

    private final AppConfig config;

    /**
     * Private constructor to prevent direct instantiation.
     * The config loading happens here.
     */
    private ConfigManager() {
        // Load the YAML file from the classpath
        String configFileName = "application.yml";
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(configFileName)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Configuration file not found: " + configFileName);
            }
            // Use SnakeYAML to load the YAML into the AppConfig POJO
            Yaml yaml = new Yaml();
            this.config = yaml.loadAs(inputStream, AppConfig.class);

        } catch (Exception e) {
            // In a real application, log the error and decide on shutdown/default config
            log.error("Error loading configuration: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize configuration", e);
        }
    }

    /**
     * Static inner class for Singleton implementation.
     * The INSTANCE is only created when ConfigManagerHolder is first accessed,
     * which happens when getInstance() is called (lazy initialization).
     * This is inherently thread-safe without needing synchronized keywords.
     */
    private static class ConfigManagerHolder {
        private static final ConfigManager INSTANCE = new ConfigManager();
    }

    /**
     * Public static method to get the single instance of ConfigManager.
     * @return The singleton ConfigManager instance.
     */
    public static ConfigManager getInstance() {
        return ConfigManagerHolder.INSTANCE;
    }

    /**
     * Public access method to retrieve the loaded configuration data.
     * @return The AppConfig data object.
     */
    public AppConfig getConfig() {
        return config;
    }
}