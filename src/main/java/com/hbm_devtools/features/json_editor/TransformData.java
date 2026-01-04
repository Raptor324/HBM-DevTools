package com.hbm_devtools.features.json_editor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Данные трансформаций для одного режима отображения
 */
public class TransformData {
    public float rotationX = 0;
    public float rotationY = 0;
    public float rotationZ = 0;
    public float translationX = 0;
    public float translationY = 0;
    public float translationZ = 0;
    public float scaleX = 1;
    public float scaleY = 1;
    public float scaleZ = 1;
    
    public TransformData() {}
    
    public TransformData(float rx, float ry, float rz, float tx, float ty, float tz, float sx, float sy, float sz) {
        this.rotationX = rx;
        this.rotationY = ry;
        this.rotationZ = rz;
        this.translationX = tx;
        this.translationY = ty;
        this.translationZ = tz;
        this.scaleX = sx;
        this.scaleY = sy;
        this.scaleZ = sz;
    }
    
    public TransformData copy() {
        return new TransformData(rotationX, rotationY, rotationZ, 
                                translationX, translationY, translationZ,
                                scaleX, scaleY, scaleZ);
    }
    
    public void fromJson(JsonObject json) {
        if (json.has("rotation")) {
            JsonArray rot = json.getAsJsonArray("rotation");
            if (rot.size() >= 3) {
                rotationX = rot.get(0).getAsFloat();
                rotationY = rot.get(1).getAsFloat();
                rotationZ = rot.get(2).getAsFloat();
            }
        }
        if (json.has("translation")) {
            JsonArray trans = json.getAsJsonArray("translation");
            if (trans.size() >= 3) {
                translationX = trans.get(0).getAsFloat();
                translationY = trans.get(1).getAsFloat();
                translationZ = trans.get(2).getAsFloat();
            }
        }
        if (json.has("scale")) {
            JsonArray scale = json.getAsJsonArray("scale");
            if (scale.size() >= 3) {
                scaleX = scale.get(0).getAsFloat();
                scaleY = scale.get(1).getAsFloat();
                scaleZ = scale.get(2).getAsFloat();
            }
        }
    }
    
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        
        JsonArray rotation = new JsonArray();
        rotation.add(rotationX);
        rotation.add(rotationY);
        rotation.add(rotationZ);
        json.add("rotation", rotation);
        
        JsonArray translation = new JsonArray();
        translation.add(translationX);
        translation.add(translationY);
        translation.add(translationZ);
        json.add("translation", translation);
        
        JsonArray scale = new JsonArray();
        scale.add(scaleX);
        scale.add(scaleY);
        scale.add(scaleZ);
        json.add("scale", scale);
        
        return json;
    }
}

