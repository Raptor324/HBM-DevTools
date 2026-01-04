package com.hbm_devtools.features.json_editor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hbm_devtools.core.config.DevToolsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

/**
 * Класс для сохранения и загрузки трансформаций из JSON файлов моделей
 */
public class JsonTransformSaver {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    /**
     * Загрузить трансформации из оригинального JSON файла модели
     */
    public static JsonTransformData loadFromModelJson(String itemId) {
        ResourceLocation modelLocation = ResourceLocation.parse("hbm_m:models/item/" + itemId + ".json");
        
        try {
            Optional<Resource> resourceOpt = Minecraft.getInstance().getResourceManager().getResource(modelLocation);
            if (resourceOpt.isPresent()) {
                Resource resource = resourceOpt.get();
                String content = new String(resource.open().readAllBytes());
                JsonObject json = JsonParser.parseString(content).getAsJsonObject();
                
                if (json.has("display")) {
                    JsonTransformData data = new JsonTransformData(itemId);
                    data.fromJson(json.getAsJsonObject("display"));
                    return data;
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to load model JSON for {}: {}", itemId, e.getMessage());
        }
        
        // Возвращаем данные по умолчанию
        return new JsonTransformData(itemId);
    }
    
    /**
     * Сохранить трансформации в оригинальный JSON файл модели
     */
    public static boolean saveToModelJson(JsonTransformData data) {
        ResourceLocation modelLocation = ResourceLocation.parse("hbm_m:models/item/" + data.itemId + ".json");
        
        // Находим физический путь к файлу в исходниках
        Path sourcePath = findSourceModelPath(data.itemId);
        if (sourcePath == null || !Files.exists(sourcePath)) {
            LOGGER.warn("Source model file not found for: {}", data.itemId);
            return false;
        }
        
        try {
            // Создаем резервную копию
            Path backupPath = sourcePath.getParent().resolve(data.itemId + ".json.backup");
            Files.copy(sourcePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            
            // Загружаем оригинальный JSON
            String content = Files.readString(sourcePath);
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();
            
            // Обновляем секцию display
            JsonObject display = data.toJson();
            json.add("display", display);
            
            // Сохраняем обратно
            String newContent = GSON.toJson(json);
            Files.writeString(sourcePath, newContent, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            
            LOGGER.info("Saved transforms for {} to model JSON", data.itemId);
            return true;
        } catch (IOException e) {
            LOGGER.error("Failed to save model JSON for {}", data.itemId, e);
            return false;
        }
    }
    
    /**
     * Найти физический путь к исходному файлу модели
     */
    private static Path findSourceModelPath(String itemId) {
        // Пытаемся найти в различных возможных местах
        Path[] possiblePaths = {
            // В исходниках HBM-Modernized
            Path.of("../HBM-Modernized/src/main/resources/assets/hbm_m/models/item/" + itemId + ".json"),
            Path.of("../../HBM-Modernized/src/main/resources/assets/hbm_m/models/item/" + itemId + ".json"),
            // В рабочей директории
            Path.of("run/assets/hbm_m/models/item/" + itemId + ".json"),
        };
        
        for (Path path : possiblePaths) {
            if (Files.exists(path)) {
                return path.toAbsolutePath();
            }
        }
        
        // Если не нашли, пытаемся найти через game directory
        if (Minecraft.getInstance() != null && Minecraft.getInstance().gameDirectory != null) {
            Path gameDir = Minecraft.getInstance().gameDirectory.toPath();
            Path[] gameDirPaths = {
                gameDir.resolve("../../HBM-Modernized/src/main/resources/assets/hbm_m/models/item/" + itemId + ".json"),
                gameDir.resolve("../HBM-Modernized/src/main/resources/assets/hbm_m/models/item/" + itemId + ".json"),
            };
            
            for (Path path : gameDirPaths) {
                if (Files.exists(path)) {
                    return path.toAbsolutePath();
                }
            }
        }
        
        return null;
    }
    
    /**
     * Загрузить трансформации из конфигурационного файла DevTools
     */
    public static JsonTransformData loadFromConfig(String itemId) {
        Path configPath = DevToolsConfig.getTransformsConfigPath();
        if (!Files.exists(configPath)) {
            return null;
        }
        
        try {
            JsonObject config = DevToolsConfig.loadJson(configPath);
            if (config.has(itemId)) {
                JsonTransformData data = new JsonTransformData(itemId);
                data.fromJson(config.getAsJsonObject(itemId));
                return data;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load transforms from config for {}", itemId, e);
        }
        
        return null;
    }
    
    /**
     * Сохранить трансформации в конфигурационный файл DevTools
     */
    public static void saveToConfig(JsonTransformData data) {
        Path configPath = DevToolsConfig.getTransformsConfigPath();
        
        JsonObject config = DevToolsConfig.loadJson(configPath);
        config.add(data.itemId, data.toJson());
        
        DevToolsConfig.saveJson(configPath, config);
        LOGGER.debug("Saved transforms for {} to config", data.itemId);
    }
    
    /**
     * Загрузить все трансформации из конфигурации
     */
    public static java.util.Map<String, JsonTransformData> loadAllFromConfig() {
        java.util.Map<String, JsonTransformData> result = new java.util.HashMap<>();
        Path configPath = DevToolsConfig.getTransformsConfigPath();
        
        if (!Files.exists(configPath)) {
            return result;
        }
        
        try {
            JsonObject config = DevToolsConfig.loadJson(configPath);
            for (String key : config.keySet()) {
                JsonTransformData data = new JsonTransformData(key);
                data.fromJson(config.getAsJsonObject(key));
                result.put(key, data);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load all transforms from config", e);
        }
        
        return result;
    }
}

