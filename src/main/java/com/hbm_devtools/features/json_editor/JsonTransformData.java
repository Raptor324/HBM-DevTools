package com.hbm_devtools.features.json_editor;

import com.google.gson.JsonObject;

/**
 * Полные данные трансформаций для одного предмета (все режимы отображения)
 */
public class JsonTransformData {
    public final String itemId;
    
    public final TransformData gui = new TransformData();
    public final TransformData ground = new TransformData();
    public final TransformData fixed = new TransformData();
    public final TransformData thirdperson = new TransformData();
    public final TransformData firstperson = new TransformData();
    
    public JsonTransformData(String itemId) {
        this.itemId = itemId;
    }
    
    public TransformData getTransformForMode(String mode) {
        return switch (mode.toLowerCase()) {
            case "gui" -> gui;
            case "ground" -> ground;
            case "fixed" -> fixed;
            case "thirdperson_righthand", "thirdperson" -> thirdperson;
            case "firstperson_righthand", "firstperson" -> firstperson;
            default -> gui;
        };
    }
    
    public JsonTransformData copy() {
        JsonTransformData copy = new JsonTransformData(itemId);
        copy.gui.rotationX = gui.rotationX;
        copy.gui.rotationY = gui.rotationY;
        copy.gui.rotationZ = gui.rotationZ;
        copy.gui.translationX = gui.translationX;
        copy.gui.translationY = gui.translationY;
        copy.gui.translationZ = gui.translationZ;
        copy.gui.scaleX = gui.scaleX;
        copy.gui.scaleY = gui.scaleY;
        copy.gui.scaleZ = gui.scaleZ;
        
        // Аналогично для остальных режимов...
        copyFrom(gui, copy.gui);
        copyFrom(ground, copy.ground);
        copyFrom(fixed, copy.fixed);
        copyFrom(thirdperson, copy.thirdperson);
        copyFrom(firstperson, copy.firstperson);
        
        return copy;
    }
    
    private void copyFrom(TransformData from, TransformData to) {
        to.rotationX = from.rotationX;
        to.rotationY = from.rotationY;
        to.rotationZ = from.rotationZ;
        to.translationX = from.translationX;
        to.translationY = from.translationY;
        to.translationZ = from.translationZ;
        to.scaleX = from.scaleX;
        to.scaleY = from.scaleY;
        to.scaleZ = from.scaleZ;
    }
    
    public void fromJson(JsonObject json) {
        if (json.has("gui")) {
            gui.fromJson(json.getAsJsonObject("gui"));
        }
        if (json.has("ground")) {
            ground.fromJson(json.getAsJsonObject("ground"));
        }
        if (json.has("fixed")) {
            fixed.fromJson(json.getAsJsonObject("fixed"));
        }
        if (json.has("thirdperson_righthand")) {
            thirdperson.fromJson(json.getAsJsonObject("thirdperson_righthand"));
        }
        if (json.has("firstperson_righthand")) {
            firstperson.fromJson(json.getAsJsonObject("firstperson_righthand"));
        }
    }
    
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.add("gui", gui.toJson());
        json.add("ground", ground.toJson());
        json.add("fixed", fixed.toJson());
        json.add("thirdperson_righthand", thirdperson.toJson());
        json.add("firstperson_righthand", firstperson.toJson());
        return json;
    }
}

