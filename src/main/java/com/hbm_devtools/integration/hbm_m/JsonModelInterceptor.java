package com.hbm_devtools.integration.hbm_m;

import com.hbm_devtools.features.json_editor.JsonTransformSaver;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Перехватчик моделей для применения динамических трансформаций
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class JsonModelInterceptor {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<net.minecraft.resources.ResourceLocation, String> ITEM_ID_CACHE = new HashMap<>();
    private static final Pattern ITEM_MODEL_PATTERN = Pattern.compile("hbm_m:models/item/([^/]+)\\.json");
    
    /**
     * Перехватывает результат бейкинга моделей и оборачивает нужные модели
     */
    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        if (net.minecraftforge.fml.loading.FMLEnvironment.production) {
            return;
        }
        
        Map<net.minecraft.resources.ResourceLocation, BakedModel> models = event.getModels();
        Map<net.minecraft.resources.ResourceLocation, BakedModel> modifiedModels = new HashMap<>();
        
        for (Map.Entry<net.minecraft.resources.ResourceLocation, BakedModel> entry : models.entrySet()) {
            net.minecraft.resources.ResourceLocation location = entry.getKey();
            BakedModel model = entry.getValue();
            
            // Проверяем, является ли это моделью предмета HBM
            if (location instanceof ModelResourceLocation) {
                ModelResourceLocation modelLocation = (ModelResourceLocation) location;
                String itemId = extractItemId(modelLocation);
                if (itemId != null && JsonTransformSaver.loadFromConfig(itemId) != null) {
                    // Обертываем модель для применения динамических трансформаций
                    DynamicTransformsWrapper wrapper = new DynamicTransformsWrapper(model, itemId);
                    modifiedModels.put(location, wrapper);
                    LOGGER.debug("Wrapped model {} with dynamic transforms", location);
                }
            }
        }
        
        // Применяем изменения
        for (Map.Entry<net.minecraft.resources.ResourceLocation, BakedModel> entry : modifiedModels.entrySet()) {
            models.put(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Извлекает ID предмета из ModelResourceLocation
     */
    private static String extractItemId(ModelResourceLocation location) {
        net.minecraft.resources.ResourceLocation key = location;
        // Кэшируем результаты
        if (ITEM_ID_CACHE.containsKey(key)) {
            return ITEM_ID_CACHE.get(key);
        }
        
        String path = location.getPath();
        Matcher matcher = ITEM_MODEL_PATTERN.matcher(path);
        
        if (matcher.find()) {
            String itemId = matcher.group(1);
            ITEM_ID_CACHE.put(key, itemId);
            return itemId;
        }
        
        // Пытаемся извлечь из namespace:path формата
        if (location.getNamespace().equals("hbm_m") && path.startsWith("item/")) {
            String itemId = path.substring(5); // Убираем "item/"
            ITEM_ID_CACHE.put(key, itemId);
            return itemId;
        }
        
        return null;
    }
    
    /**
     * Инвалидирует кэш (вызывается при изменении конфигурации)
     */
    public static void invalidateCache() {
        ITEM_ID_CACHE.clear();
    }
}

