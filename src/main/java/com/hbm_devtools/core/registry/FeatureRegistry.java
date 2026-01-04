package com.hbm_devtools.core.registry;

import com.hbm_devtools.core.api.IDevToolFeature;
import com.hbm_devtools.features.json_editor.JsonTransformEditor;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Централизованный реестр всех функций DevTools.
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FeatureRegistry {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<String, IDevToolFeature> FEATURES = new ConcurrentHashMap<>();
    private static final Map<IDevToolFeature, KeyMapping> KEY_BINDINGS = new HashMap<>();
    private static boolean initialized = false;
    
    /**
     * Инициализация реестра и регистрация всех функций
     */
    public static void initialize() {
        if (initialized) {
            LOGGER.warn("FeatureRegistry already initialized!");
            return;
        }
        
        if (net.minecraftforge.fml.loading.FMLEnvironment.production) {
            LOGGER.warn("FeatureRegistry: Production environment detected, skipping feature registration");
            return;
        }
        
        // Регистрация функций
        registerFeature(new JsonTransformEditor());
        
        // Регистрация всех функций
        FEATURES.values().forEach(feature -> {
            if (feature.isAvailable()) {
                try {
                    feature.register();
                    LOGGER.debug("Registered feature: {}", feature.getId());
                } catch (Exception e) {
                    LOGGER.error("Failed to register feature: {}", feature.getId(), e);
                }
            }
        });
        
        initialized = true;
        LOGGER.info("FeatureRegistry initialized with {} features", FEATURES.size());
    }
    
    /**
     * Регистрация функции
     */
    public static void registerFeature(IDevToolFeature feature) {
        if (FEATURES.containsKey(feature.getId())) {
            LOGGER.warn("Feature with id '{}' already registered, skipping", feature.getId());
            return;
        }
        
        FEATURES.put(feature.getId(), feature);
        
        // Сохраняем KeyMapping для последующей регистрации
        KeyMapping keyBinding = feature.getKeyBinding();
        if (keyBinding != null) {
            KEY_BINDINGS.put(feature, keyBinding);
        }
        
        // Для JsonTransformEditor также регистрируем второй KeyMapping
        if (feature instanceof com.hbm_devtools.features.json_editor.JsonTransformEditor) {
            KeyMapping heldItemKey = ((com.hbm_devtools.features.json_editor.JsonTransformEditor) feature).getOpenEditorForHeldItemKey();
            if (heldItemKey != null) {
                // Сохраняем как дополнительный KeyMapping
                KEY_BINDINGS.put(feature, heldItemKey); // Перезаписываем, но это нормально для регистрации
            }
        }
    }
    
    /**
     * Получить функцию по ID
     */
    @SuppressWarnings("unchecked")
    public static <T extends IDevToolFeature> T getFeature(String id, Class<T> clazz) {
        IDevToolFeature feature = FEATURES.get(id);
        if (feature != null && clazz.isInstance(feature)) {
            return (T) feature;
        }
        return null;
    }
    
    /**
     * Получить все зарегистрированные функции
     */
    public static Collection<IDevToolFeature> getAllFeatures() {
        return Collections.unmodifiableCollection(FEATURES.values());
    }
    
    /**
     * Регистрация горячих клавиш
     */
    @SubscribeEvent
    public static void onRegisterKeyMappings(@NotNull RegisterKeyMappingsEvent event) {
        Set<KeyMapping> registered = new HashSet<>();
        for (Map.Entry<IDevToolFeature, KeyMapping> entry : KEY_BINDINGS.entrySet()) {
            KeyMapping keyMapping = entry.getValue();
            if (keyMapping != null && !registered.contains(keyMapping)) {
                event.register(keyMapping);
                registered.add(keyMapping);
            }
            
            // Для JsonTransformEditor регистрируем оба KeyMapping
            if (entry.getKey() instanceof com.hbm_devtools.features.json_editor.JsonTransformEditor) {
                com.hbm_devtools.features.json_editor.JsonTransformEditor editor = 
                    (com.hbm_devtools.features.json_editor.JsonTransformEditor) entry.getKey();
                KeyMapping heldItemKey = editor.getOpenEditorForHeldItemKey();
                if (heldItemKey != null && !registered.contains(heldItemKey)) {
                    event.register(heldItemKey);
                    registered.add(heldItemKey);
                }
            }
        }
        LOGGER.debug("Registered {} key bindings", registered.size());
    }
    
    /**
     * Очистка реестра (для тестирования)
     */
    public static void clear() {
        FEATURES.values().forEach(IDevToolFeature::unregister);
        FEATURES.clear();
        KEY_BINDINGS.clear();
        initialized = false;
    }
}

