package com.hbm_devtools.features.json_editor;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI экран для редактирования трансформаций предметов
 */
@OnlyIn(Dist.CLIENT)
public class JsonTransformEditorScreen extends Screen {
    private static final Logger LOGGER = LogManager.getLogger();
    
    private final String itemId;
    private JsonTransformData currentData;
    private TransformHistory history;
    private String currentMode = "gui";
    
    // Слайдеры
    private TransformSlider rotationXSlider, rotationYSlider, rotationZSlider;
    private TransformSlider translationXSlider, translationYSlider, translationZSlider;
    private TransformSlider scaleXSlider, scaleYSlider, scaleZSlider;
    
    // Поля ввода (альтернатива слайдерам)
    private EditBox rotationXField, rotationYField, rotationZField;
    private EditBox translationXField, translationYField, translationZField;
    private EditBox scaleXField, scaleYField, scaleZField;
    
    // Кнопки режимов
    private List<Button> modeButtons = new ArrayList<>();
    
    // Кнопки управления
    private Button saveButton, resetButton, undoButton, redoButton;
    private Button copyButton, pasteButton;
    
    private boolean isDirty = false;
    
    // Предпросмотр
    private int previewX = 550;
    private int previewY = 60;
    private int previewWidth = 200;
    private int previewHeight = 200;
    
    public JsonTransformEditorScreen(String itemId) {
        super(Component.literal("Transform Editor: " + itemId));
        this.itemId = itemId;
        this.history = new TransformHistory();
        
        // Загружаем данные
        loadData();
    }
    
    private void loadData() {
        // Пытаемся загрузить из конфига
        currentData = JsonTransformSaver.loadFromConfig(itemId);
        
        // Если нет в конфиге, загружаем из оригинального JSON
        if (currentData == null) {
            currentData = JsonTransformSaver.loadFromModelJson(itemId);
        }
        
        if (currentData == null) {
            currentData = new JsonTransformData(itemId);
        }
        
        history.push(currentData.copy());
    }
    
    @Override
    protected void init() {
        super.init();
        
        int buttonY = 20;
        int buttonWidth = 80;
        int buttonHeight = 20;
        int spacing = 5;
        
        // Кнопки переключения режимов
        String[] modes = {"gui", "ground", "fixed", "thirdperson", "firstperson"};
        for (int i = 0; i < modes.length; i++) {
            String mode = modes[i];
            Button button = Button.builder(
                Component.literal(mode.substring(0, 1).toUpperCase() + mode.substring(1)),
                b -> setMode(mode)
            ).bounds(10 + i * (buttonWidth + spacing), buttonY, buttonWidth, buttonHeight).build();
            addRenderableWidget(button);
            modeButtons.add(button);
        }
        
        // Слайдеры для трансформаций
        int sliderY = 60;
        int sliderWidth = 200;
        int sliderHeight = 20;
        
        TransformData currentTransform = currentData.getTransformForMode(currentMode);
        
        // Rotation слайдеры
        rotationXSlider = new TransformSlider(10, sliderY, sliderWidth, sliderHeight, "Rotation X",
            -360, 360, currentTransform.rotationX, v -> updateRotationX(v));
        rotationYSlider = new TransformSlider(10, sliderY + 25, sliderWidth, sliderHeight, "Rotation Y",
            -360, 360, currentTransform.rotationY, v -> updateRotationY(v));
        rotationZSlider = new TransformSlider(10, sliderY + 50, sliderWidth, sliderHeight, "Rotation Z",
            -360, 360, currentTransform.rotationZ, v -> updateRotationZ(v));
        
        // Translation слайдеры
        translationXSlider = new TransformSlider(10, sliderY + 85, sliderWidth, sliderHeight, "Translation X",
            -50, 50, currentTransform.translationX, v -> updateTranslationX(v));
        translationYSlider = new TransformSlider(10, sliderY + 110, sliderWidth, sliderHeight, "Translation Y",
            -50, 50, currentTransform.translationY, v -> updateTranslationY(v));
        translationZSlider = new TransformSlider(10, sliderY + 135, sliderWidth, sliderHeight, "Translation Z",
            -50, 50, currentTransform.translationZ, v -> updateTranslationZ(v));
        
        // Scale слайдеры
        scaleXSlider = new TransformSlider(10, sliderY + 170, sliderWidth, sliderHeight, "Scale X",
            0.001f, 1.0f, currentTransform.scaleX, v -> updateScaleX(v));
        scaleYSlider = new TransformSlider(10, sliderY + 195, sliderWidth, sliderHeight, "Scale Y",
            0.001f, 1.0f, currentTransform.scaleY, v -> updateScaleY(v));
        scaleZSlider = new TransformSlider(10, sliderY + 220, sliderWidth, sliderHeight, "Scale Z",
            0.001f, 1.0f, currentTransform.scaleZ, v -> updateScaleZ(v));
        
        addRenderableWidget(rotationXSlider);
        addRenderableWidget(rotationYSlider);
        addRenderableWidget(rotationZSlider);
        addRenderableWidget(translationXSlider);
        addRenderableWidget(translationYSlider);
        addRenderableWidget(translationZSlider);
        addRenderableWidget(scaleXSlider);
        addRenderableWidget(scaleYSlider);
        addRenderableWidget(scaleZSlider);
        
        // Поля ввода (справа от слайдеров)
        int fieldX = 220;
        int fieldWidth = 80;
        
        rotationXField = new EditBox(font, fieldX, sliderY, fieldWidth, sliderHeight, Component.literal("RX"));
        rotationYField = new EditBox(font, fieldX, sliderY + 25, fieldWidth, sliderHeight, Component.literal("RY"));
        rotationZField = new EditBox(font, fieldX, sliderY + 50, fieldWidth, sliderHeight, Component.literal("RZ"));
        
        translationXField = new EditBox(font, fieldX, sliderY + 85, fieldWidth, sliderHeight, Component.literal("TX"));
        translationYField = new EditBox(font, fieldX, sliderY + 110, fieldWidth, sliderHeight, Component.literal("TY"));
        translationZField = new EditBox(font, fieldX, sliderY + 135, fieldWidth, sliderHeight, Component.literal("TZ"));
        
        scaleXField = new EditBox(font, fieldX, sliderY + 170, fieldWidth, sliderHeight, Component.literal("SX"));
        scaleYField = new EditBox(font, fieldX, sliderY + 195, fieldWidth, sliderHeight, Component.literal("SY"));
        scaleZField = new EditBox(font, fieldX, sliderY + 220, fieldWidth, sliderHeight, Component.literal("SZ"));
        
        updateFieldsFromSliders();
        
        addRenderableWidget(rotationXField);
        addRenderableWidget(rotationYField);
        addRenderableWidget(rotationZField);
        addRenderableWidget(translationXField);
        addRenderableWidget(translationYField);
        addRenderableWidget(translationZField);
        addRenderableWidget(scaleXField);
        addRenderableWidget(scaleYField);
        addRenderableWidget(scaleZField);
        
        // Кнопки управления
        int controlY = height - 40;
        saveButton = Button.builder(Component.literal("Save"), b -> saveData())
            .bounds(10, controlY, 80, 20).build();
        resetButton = Button.builder(Component.literal("Reset"), b -> resetCurrentMode())
            .bounds(100, controlY, 80, 20).build();
        undoButton = Button.builder(Component.literal("Undo"), b -> undo())
            .bounds(190, controlY, 80, 20).build();
        redoButton = Button.builder(Component.literal("Redo"), b -> redo())
            .bounds(280, controlY, 80, 20).build();
        copyButton = Button.builder(Component.literal("Copy"), b -> copyFromCurrent())
            .bounds(370, controlY, 80, 20).build();
        pasteButton = Button.builder(Component.literal("Paste"), b -> pasteToCurrent())
            .bounds(460, controlY, 80, 20).build();
        
        addRenderableWidget(saveButton);
        addRenderableWidget(resetButton);
        addRenderableWidget(undoButton);
        addRenderableWidget(redoButton);
        addRenderableWidget(copyButton);
        addRenderableWidget(pasteButton);
        
        updateButtonStates();
    }
    
    private void setMode(String mode) {
        saveCurrentValues();
        currentMode = mode;
        updateSlidersFromData();
        updateFieldsFromSliders();
    }
    
    private void updateSlidersFromData() {
        TransformData transform = currentData.getTransformForMode(currentMode);
        rotationXSlider.setValue(transform.rotationX);
        rotationYSlider.setValue(transform.rotationY);
        rotationZSlider.setValue(transform.rotationZ);
        translationXSlider.setValue(transform.translationX);
        translationYSlider.setValue(transform.translationY);
        translationZSlider.setValue(transform.translationZ);
        scaleXSlider.setValue(transform.scaleX);
        scaleYSlider.setValue(transform.scaleY);
        scaleZSlider.setValue(transform.scaleZ);
    }
    
    private void updateFieldsFromSliders() {
        rotationXField.setValue(String.format("%.3f", rotationXSlider.getValue()));
        rotationYField.setValue(String.format("%.3f", rotationYSlider.getValue()));
        rotationZField.setValue(String.format("%.3f", rotationZSlider.getValue()));
        translationXField.setValue(String.format("%.3f", translationXSlider.getValue()));
        translationYField.setValue(String.format("%.3f", translationYSlider.getValue()));
        translationZField.setValue(String.format("%.3f", translationZSlider.getValue()));
        scaleXField.setValue(String.format("%.3f", scaleXSlider.getValue()));
        scaleYField.setValue(String.format("%.3f", scaleYSlider.getValue()));
        scaleZField.setValue(String.format("%.3f", scaleZSlider.getValue()));
    }
    
    private void saveCurrentValues() {
        TransformData transform = currentData.getTransformForMode(currentMode);
        transform.rotationX = rotationXSlider.getValue();
        transform.rotationY = rotationYSlider.getValue();
        transform.rotationZ = rotationZSlider.getValue();
        transform.translationX = translationXSlider.getValue();
        transform.translationY = translationYSlider.getValue();
        transform.translationZ = translationZSlider.getValue();
        transform.scaleX = scaleXSlider.getValue();
        transform.scaleY = scaleYSlider.getValue();
        transform.scaleZ = scaleZSlider.getValue();
        isDirty = true;
    }
    
    // Методы обновления значений
    private void updateRotationX(float value) {
        currentData.getTransformForMode(currentMode).rotationX = value;
        rotationXField.setValue(String.format("%.3f", value));
        isDirty = true;
    }
    
    private void updateRotationY(float value) {
        currentData.getTransformForMode(currentMode).rotationY = value;
        rotationYField.setValue(String.format("%.3f", value));
        isDirty = true;
    }
    
    private void updateRotationZ(float value) {
        currentData.getTransformForMode(currentMode).rotationZ = value;
        rotationZField.setValue(String.format("%.3f", value));
        isDirty = true;
    }
    
    private void updateTranslationX(float value) {
        currentData.getTransformForMode(currentMode).translationX = value;
        translationXField.setValue(String.format("%.3f", value));
        isDirty = true;
    }
    
    private void updateTranslationY(float value) {
        currentData.getTransformForMode(currentMode).translationY = value;
        translationYField.setValue(String.format("%.3f", value));
        isDirty = true;
    }
    
    private void updateTranslationZ(float value) {
        currentData.getTransformForMode(currentMode).translationZ = value;
        translationZField.setValue(String.format("%.3f", value));
        isDirty = true;
    }
    
    private void updateScaleX(float value) {
        currentData.getTransformForMode(currentMode).scaleX = value;
        scaleXField.setValue(String.format("%.3f", value));
        isDirty = true;
    }
    
    private void updateScaleY(float value) {
        currentData.getTransformForMode(currentMode).scaleY = value;
        scaleYField.setValue(String.format("%.3f", value));
        isDirty = true;
    }
    
    private void updateScaleZ(float value) {
        currentData.getTransformForMode(currentMode).scaleZ = value;
        scaleZField.setValue(String.format("%.3f", value));
        isDirty = true;
    }
    
    private void saveData() {
        saveCurrentValues();
        JsonTransformSaver.saveToConfig(currentData);
        JsonTransformSaver.saveToModelJson(currentData);
        history.push(currentData.copy());
        isDirty = false;
        updateButtonStates();
        LOGGER.info("Saved transforms for {}", itemId);
    }
    
    private void resetCurrentMode() {
        JsonTransformData original = JsonTransformSaver.loadFromModelJson(itemId);
        if (original != null) {
            TransformData originalTransform = original.getTransformForMode(currentMode);
            TransformData currentTransform = currentData.getTransformForMode(currentMode);
            
            currentTransform.rotationX = originalTransform.rotationX;
            currentTransform.rotationY = originalTransform.rotationY;
            currentTransform.rotationZ = originalTransform.rotationZ;
            currentTransform.translationX = originalTransform.translationX;
            currentTransform.translationY = originalTransform.translationY;
            currentTransform.translationZ = originalTransform.translationZ;
            currentTransform.scaleX = originalTransform.scaleX;
            currentTransform.scaleY = originalTransform.scaleY;
            currentTransform.scaleZ = originalTransform.scaleZ;
            
            updateSlidersFromData();
            updateFieldsFromSliders();
            isDirty = true;
        }
    }
    
    private TransformData copiedTransform = null;
    
    private void copyFromCurrent() {
        copiedTransform = currentData.getTransformForMode(currentMode).copy();
    }
    
    private void pasteToCurrent() {
        if (copiedTransform != null) {
            TransformData currentTransform = currentData.getTransformForMode(currentMode);
            currentTransform.rotationX = copiedTransform.rotationX;
            currentTransform.rotationY = copiedTransform.rotationY;
            currentTransform.rotationZ = copiedTransform.rotationZ;
            currentTransform.translationX = copiedTransform.translationX;
            currentTransform.translationY = copiedTransform.translationY;
            currentTransform.translationZ = copiedTransform.translationZ;
            currentTransform.scaleX = copiedTransform.scaleX;
            currentTransform.scaleY = copiedTransform.scaleY;
            currentTransform.scaleZ = copiedTransform.scaleZ;
            
            updateSlidersFromData();
            updateFieldsFromSliders();
            isDirty = true;
        }
    }
    
    private void undo() {
        saveCurrentValues();
        JsonTransformData previous = history.undo(currentData);
        if (previous != null) {
            currentData = previous;
            updateSlidersFromData();
            updateFieldsFromSliders();
            updateButtonStates();
        }
    }
    
    private void redo() {
        saveCurrentValues();
        JsonTransformData next = history.redo(currentData);
        if (next != null) {
            currentData = next;
            updateSlidersFromData();
            updateFieldsFromSliders();
            updateButtonStates();
        }
    }
    
    private void updateButtonStates() {
        undoButton.active = history.canUndo();
        redoButton.active = history.canRedo();
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        guiGraphics.drawString(font, "Editing: " + itemId, 10, 5, 0xFFFFFF);
        guiGraphics.drawString(font, "Mode: " + currentMode, 10, 45, 0xFFFFFF);
        if (isDirty) {
            guiGraphics.drawString(font, "* Unsaved changes", 10, height - 20, 0xFF0000);
        }
        
        // Рендерим предпросмотр
        renderPreview(guiGraphics);
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    
    private void renderPreview(GuiGraphics guiGraphics) {
        // Рамка предпросмотра
        guiGraphics.fill(previewX - 1, previewY - 1, 
                        previewX + previewWidth + 1, previewY + previewHeight + 1, 
                        0xFF000000);
        guiGraphics.fill(previewX, previewY, 
                        previewX + previewWidth, previewY + previewHeight, 
                        0xFF1A1A1A);
        
        guiGraphics.drawString(font, "Preview (" + currentMode + ")", previewX, previewY - 12, 0xFFFFFF);
        
        // Рендерим предмет с текущими трансформациями
        try {
            net.minecraft.world.item.ItemStack itemStack = getItemStackForPreview();
            if (itemStack != null && !itemStack.isEmpty()) {
                // Рендерим предмет в центре области предпросмотра
                int centerX = previewX + previewWidth / 2;
                int centerY = previewY + previewHeight / 2;
                
                // Применяем трансформации для текущего режима
                TransformData transform = currentData.getTransformForMode(currentMode);
                
                // Рендерим предмет с учетом трансформаций
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(centerX, centerY, 100);
                guiGraphics.pose().scale(transform.scaleX * 100, transform.scaleY * 100, transform.scaleZ * 100);
                guiGraphics.pose().translate(transform.translationX * 10, transform.translationY * 10, transform.translationZ * 10);
                
                // Поворот
                guiGraphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(transform.rotationZ));
                guiGraphics.pose().mulPose(com.mojang.math.Axis.YP.rotationDegrees(transform.rotationY));
                guiGraphics.pose().mulPose(com.mojang.math.Axis.XP.rotationDegrees(transform.rotationX));
                
                // Рендерим предмет
                net.minecraft.client.renderer.entity.ItemRenderer itemRenderer = 
                    net.minecraft.client.Minecraft.getInstance().getItemRenderer();
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                if (mc.level != null) {
                    itemRenderer.renderStatic(itemStack, 
                        net.minecraft.world.item.ItemDisplayContext.GUI,
                        15728880, // packedLight
                        0, // packedOverlay
                        guiGraphics.pose(),
                        mc.renderBuffers().bufferSource(),
                        mc.level,
                        0 // seed
                    );
                }
                
                guiGraphics.pose().popPose();
            } else {
                guiGraphics.drawString(font, "Item not found", previewX + 10, previewY + previewHeight / 2, 0xFF888888);
            }
        } catch (Exception e) {
            guiGraphics.drawString(font, "Preview error", previewX + 10, previewY + previewHeight / 2, 0xFFFF0000);
        }
    }
    
    private net.minecraft.world.item.ItemStack getItemStackForPreview() {
        // Пытаемся найти предмет по ID
        try {
            net.minecraft.resources.ResourceLocation itemLocation = 
                net.minecraft.resources.ResourceLocation.parse("hbm_m:" + itemId);
            net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(itemLocation)
                .ifPresent(item -> {
                    // Предмет найден, можно использовать
                });
            
            // Альтернативный способ - через игрока
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player != null) {
                // Ищем предмет в инвентаре игрока
                for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
                    net.minecraft.world.item.ItemStack stack = mc.player.getInventory().getItem(i);
                    if (!stack.isEmpty() && stack.getItem().toString().contains(itemId)) {
                        return stack;
                    }
                }
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }
        
        return null;
    }
    
    @Override
    public void tick() {
        super.tick();
        // Автосохранение в конфиг при изменении (но не в файл модели)
        if (isDirty) {
            saveCurrentValues();
            JsonTransformSaver.saveToConfig(currentData);
        }
    }
    
    @Override
    public void onClose() {
        if (isDirty) {
            saveData();
        }
        super.onClose();
    }
}

