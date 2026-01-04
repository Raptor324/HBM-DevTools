package com.hbm_devtools.features.json_editor;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * Кастомный слайдер для редактирования значений трансформаций
 */
public class TransformSlider extends AbstractSliderButton {
    private final String label;
    private final float minValue;
    private final float maxValue;
    private final Consumer<Float> onValueChanged;
    private float currentValue;
    
    public TransformSlider(int x, int y, int width, int height, String label, 
                          float minValue, float maxValue, float initialValue,
                          Consumer<Float> onValueChanged) {
        super(x, y, width, height, Component.literal(label + ": " + String.format("%.3f", initialValue)), 
              (initialValue - minValue) / (maxValue - minValue));
        this.label = label;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.currentValue = initialValue;
        this.onValueChanged = onValueChanged;
    }
    
    @Override
    protected void updateMessage() {
        setMessage(Component.literal(label + ": " + String.format("%.3f", currentValue)));
    }
    
    @Override
    protected void applyValue() {
        currentValue = minValue + (float) value * (maxValue - minValue);
        if (onValueChanged != null) {
            onValueChanged.accept(currentValue);
        }
    }
    
    public void setValue(float value) {
        this.currentValue = value;
        this.value = (value - minValue) / (maxValue - minValue);
        updateMessage();
    }
    
    public float getValue() {
        return currentValue;
    }
}

