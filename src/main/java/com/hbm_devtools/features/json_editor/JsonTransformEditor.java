package com.hbm_devtools.features.json_editor;

import com.hbm_devtools.core.api.IDevToolFeature;
import com.hbm_devtools.core.config.DevToolsConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

/**
 * Основная функция редактора трансформаций JSON
 */
@OnlyIn(Dist.CLIENT)
public class JsonTransformEditor implements IDevToolFeature {
    private static final Logger LOGGER = LogManager.getLogger();
    
    private static final String ID = "json_transform_editor";
    private static final String NAME = "JSON Transform Editor";
    
    private KeyMapping openEditorKey;
    private KeyMapping openEditorForHeldItemKey;
    
    public JsonTransformEditor() {
        createKeyBindings();
    }
    
    @Override
    public String getId() {
        return ID;
    }
    
    @Override
    public String getName() {
        return NAME;
    }
    
    @Override
    public Component getDescription() {
        return Component.literal("Edit item model transforms in real-time");
    }
    
    @Override
    public void register() {
        DevToolsConfig.initialize();
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("JsonTransformEditor registered");
    }
    
    @Override
    public void unregister() {
        MinecraftForge.EVENT_BUS.unregister(this);
        LOGGER.info("JsonTransformEditor unregistered");
    }
    
    @Override
    @Nullable
    public Screen createScreen() {
        return new JsonTransformEditorScreen("t51_boots"); // По умолчанию
    }
    
    @Override
    @Nullable
    public KeyMapping getKeyBinding() {
        return openEditorKey;
    }
    
    private void createKeyBindings() {
        openEditorKey = new KeyMapping(
            "key.hbm_devtools.open_editor",
            GLFW.GLFW_KEY_F8,
            "key.categories.hbm_devtools"
        );
        
        openEditorForHeldItemKey = new KeyMapping(
            "key.hbm_devtools.open_editor_held",
            GLFW.GLFW_KEY_F9,
            "key.categories.hbm_devtools"
        );
    }
    
    public KeyMapping getOpenEditorForHeldItemKey() {
        return openEditorForHeldItemKey;
    }
    
    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) {
            return; // Не обрабатываем, если открыт другой экран
        }
        
        if (openEditorKey != null && openEditorKey.isDown()) {
            openEditor();
        }
        
        if (openEditorForHeldItemKey != null && openEditorForHeldItemKey.isDown()) {
            openEditorForHeldItem();
        }
    }
    
    private void openEditor() {
        // Открываем редактор с выбором предмета
        // Пока просто открываем для t51_boots как пример
        Minecraft.getInstance().setScreen(new JsonTransformEditorScreen("t51_boots"));
    }
    
    private void openEditorForHeldItem() {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty()) {
            heldItem = player.getOffhandItem();
        }
        
        if (heldItem.isEmpty()) {
            LOGGER.warn("No item in hand to edit");
            return;
        }
        
        // Получаем ID предмета из его registry name
        String itemId = heldItem.getItem().toString();
        // Убираем префикс мода, если есть
        if (itemId.contains(":")) {
            itemId = itemId.substring(itemId.indexOf(':') + 1);
        }
        
        // Убираем префикс "item." если есть
        if (itemId.startsWith("item.")) {
            itemId = itemId.substring(5);
        }
        
        LOGGER.info("Opening editor for item: {}", itemId);
        Minecraft.getInstance().setScreen(new JsonTransformEditorScreen(itemId));
    }
    
}

