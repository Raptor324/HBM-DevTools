package com.hbm_devtools.features.json_editor;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * История изменений для Undo/Redo функциональности
 */
public class TransformHistory {
    private static final int MAX_HISTORY_SIZE = 50;
    
    private final Deque<JsonTransformData> undoStack = new ArrayDeque<>();
    private final Deque<JsonTransformData> redoStack = new ArrayDeque<>();
    
    /**
     * Сохранить состояние в историю
     */
    public void push(JsonTransformData state) {
        undoStack.push(state.copy());
        if (undoStack.size() > MAX_HISTORY_SIZE) {
            // Удаляем самый старый элемент
            ArrayDeque<JsonTransformData> temp = new ArrayDeque<>();
            while (undoStack.size() > MAX_HISTORY_SIZE - 1) {
                temp.push(undoStack.pop());
            }
            undoStack.clear();
            undoStack.addAll(temp);
        }
        // Очищаем redo при новом действии
        redoStack.clear();
    }
    
    /**
     * Отменить последнее действие
     */
    public JsonTransformData undo(JsonTransformData current) {
        if (undoStack.isEmpty()) {
            return null;
        }
        
        redoStack.push(current.copy());
        return undoStack.pop().copy();
    }
    
    /**
     * Повторить отмененное действие
     */
    public JsonTransformData redo(JsonTransformData current) {
        if (redoStack.isEmpty()) {
            return null;
        }
        
        undoStack.push(current.copy());
        return redoStack.pop().copy();
    }
    
    /**
     * Проверить, можно ли отменить
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }
    
    /**
     * Проверить, можно ли повторить
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
    
    /**
     * Очистить историю
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}

