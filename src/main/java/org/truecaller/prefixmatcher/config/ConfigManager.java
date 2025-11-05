package org.truecaller.prefixmatcher.config;

import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Singleton class responsible for loading and managing the application's configuration.
 * It uses the 'Initialization-on-demand holder' idiom for thread-safe, lazy instantiation.
 * Configuration data is loaded once from 'application.yml' upon first access.
 */
@Slf4j
public class ConfigManager {

    /** The immutable loaded application configuration data. */
    private final AppConfig config;

    /**
     * Private constructor to prevent direct instantiation.
     * This method executes the critical configuration loading steps.
     */
    private ConfigManager() {
        // Defines the expected configuration file name
        final String configFileName = "application.yml";
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(configFileName)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Configuration file not found: " + configFileName);
            }
            // Use SnakeYAML library to deserialize the YAML stream into the AppConfig POJO
            Yaml yaml = new Yaml();
            this.config = yaml.loadAs(inputStream, AppConfig.class);

        } catch (Exception e) {
            // Logs the error using Slf4j and throws an unchecked exception, halting initialization
            log.error("Error loading configuration: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize configuration", e);
        }
    }

    /**
     * Static inner class to hold the Singleton instance.
     * This class ensures thread-safe, lazy initialization.
     */
    private static class ConfigManagerHolder {
        // The instance is only created when ConfigManagerHolder is first accessed via getInstance()
        private static final ConfigManager INSTANCE = new ConfigManager();
    }

    /**
     * Public static method to get the single instance of ConfigManager.
     * Accessing this method triggers the configuration loading via ConfigManagerHolder.
     * @return The singleton ConfigManager instance.
     */
    public static ConfigManager getInstance() {
        return ConfigManagerHolder.INSTANCE;
    }

    /**
     * Public access method to retrieve the loaded configuration data object.
     * @return The fully initialized AppConfig data object.
     */
    public AppConfig getConfig() {
        return config;
    }
}
