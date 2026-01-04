package com.hbm_devtools.integration.hbm_m;

import com.hbm_devtools.features.json_editor.JsonTransformData;
import com.hbm_devtools.features.json_editor.JsonTransformSaver;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.BakedModelWrapper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Wrapper модель, которая применяет динамические трансформации вместо статических из JSON
 */
@OnlyIn(Dist.CLIENT)
public class DynamicTransformsWrapper extends BakedModelWrapper<BakedModel> {
    private final String itemId;
    private ItemTransforms cachedTransforms;
    private long lastUpdateTime = 0;
    private static final long CACHE_TTL = 100; // Кэш на 100мс
    
    public DynamicTransformsWrapper(BakedModel originalModel, String itemId) {
        super(originalModel);
        this.itemId = itemId;
    }
    
    @Override
    public ItemTransforms getTransforms() {
        // Проверяем кэш
        long currentTime = System.currentTimeMillis();
        if (cachedTransforms != null && (currentTime - lastUpdateTime) < CACHE_TTL) {
            return cachedTransforms;
        }
        
        // Пытаемся загрузить кастомные трансформации
        JsonTransformData customData = JsonTransformSaver.loadFromConfig(itemId);
        
        if (customData != null) {
            // Создаем ItemTransforms из кастомных данных
            cachedTransforms = createItemTransforms(customData);
            lastUpdateTime = currentTime;
            return cachedTransforms;
        }
        
        // Используем оригинальные трансформации
        cachedTransforms = originalModel.getTransforms();
        lastUpdateTime = currentTime;
        return cachedTransforms;
    }
    
    /**
     * Создает ItemTransforms из JsonTransformData
     */
    private ItemTransforms createItemTransforms(JsonTransformData data) {
        ItemTransform gui = createTransform(data.gui);
        ItemTransform ground = createTransform(data.ground);
        ItemTransform fixed = createTransform(data.fixed);
        ItemTransform thirdperson = createTransform(data.thirdperson);
        ItemTransform firstperson = createTransform(data.firstperson);
        
        return new ItemTransforms(thirdperson, thirdperson, firstperson, firstperson, gui, ground, fixed, fixed);
    }
    
    /**
     * Создает ItemTransform из TransformData
     */
    private ItemTransform createTransform(com.hbm_devtools.features.json_editor.TransformData data) {
        // Используем правильный способ создания ItemTransform
        // В Minecraft 1.20.1+ ItemTransform создается через Transformation
        Vector3f translation = new Vector3f(data.translationX, data.translationY, data.translationZ);
        Vector3f scale = new Vector3f(data.scaleX, data.scaleY, data.scaleZ);
        
        // Создаем кватернион для вращения
        Quaternionf rotation = new Quaternionf()
            .rotateZ(Mth.DEG_TO_RAD * data.rotationZ)
            .rotateY(Mth.DEG_TO_RAD * data.rotationY)
            .rotateX(Mth.DEG_TO_RAD * data.rotationX);
        
        // Создаем Transformation
        Transformation transformation = new Transformation(translation, rotation, scale, null);
        
        // Используем правильный способ создания ItemTransform
        // В Minecraft 1.20.1+ ItemTransform создается через конструктор с Transformation
        // Используем рефлексию для доступа к конструктору, если он недоступен напрямую
        try {
            java.lang.reflect.Constructor<ItemTransform> constructor = ItemTransform.class.getDeclaredConstructor(Transformation.class);
            constructor.setAccessible(true);
            return constructor.newInstance(transformation);
        } catch (Exception e) {
            // Если рефлексия не работает, используем ItemTransform.NO_TRANSFORM как fallback
            org.apache.logging.log4j.LogManager.getLogger().warn("Failed to create ItemTransform, using NO_TRANSFORM", e);
            return ItemTransform.NO_TRANSFORM;
        }
    }
    
    /**
     * Инвалидирует кэш трансформаций (вызывается при изменении данных)
     */
    public void invalidateCache() {
        cachedTransforms = null;
        lastUpdateTime = 0;
    }
}

