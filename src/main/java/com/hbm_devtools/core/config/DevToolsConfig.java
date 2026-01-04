package com.hbm_devtools.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Основной класс для работы с конфигурацией DevTools
 */
public class DevToolsConfig {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path configDir;
    private static Path transformsConfigPath;
    
    /**
     * Инициализация путей конфигурации
     */
    public static void initialize() {
        if (Minecraft.getInstance() != null && Minecraft.getInstance().gameDirectory != null) {
            configDir = Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config")
                .resolve("hbm_devtools");
            
            transformsConfigPath = configDir.resolve("json_transforms.json");
            
            try {
                Files.createDirectories(configDir);
            } catch (IOException e) {
                LOGGER.error("Failed to create config directory", e);
            }
        }
    }
    
    /**
     * Получить путь к директории конфигурации
     */
    public static Path getConfigDir() {
        if (configDir == null) {
            initialize();
        }
        return configDir;
    }
    
    /**
     * Получить путь к файлу трансформаций
     */
    public static Path getTransformsConfigPath() {
        if (transformsConfigPath == null) {
            initialize();
        }
        return transformsConfigPath;
    }
    
    /**
     * Загрузить JSON из файла
     */
    public static JsonObject loadJson(Path path) {
        if (!Files.exists(path)) {
            return new JsonObject();
        }
        
        try {
            String content = Files.readString(path);
            if (content.isEmpty()) {
                return new JsonObject();
            }
            return JsonParser.parseString(content).getAsJsonObject();
        } catch (IOException e) {
            LOGGER.error("Failed to load JSON from {}", path, e);
            return new JsonObject();
        }
    }
    
    /**
     * Сохранить JSON в файл
     */
    public static void saveJson(Path path, JsonObject json) {
        try {
            Files.createDirectories(path.getParent());
            String content = GSON.toJson(json);
            Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            LOGGER.error("Failed to save JSON to {}", path, e);
        }
    }
}

