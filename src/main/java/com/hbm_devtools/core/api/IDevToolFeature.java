package com.hbm_devtools.core.api;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Интерфейс для функций DevTools.
 * Каждая функция должна реализовывать этот интерфейс для регистрации в системе.
 */
public interface IDevToolFeature {
    /**
     * Уникальный идентификатор функции (например, "armor_editor")
     */
    String getId();
    
    /**
     * Отображаемое имя функции
     */
    String getName();
    
    /**
     * Описание функции
     */
    Component getDescription();
    
    /**
     * Регистрация функции (обработчики событий, команды и т.д.)
     */
    void register();
    
    /**
     * Отмена регистрации функции
     */
    void unregister();
    
    /**
     * Создает экран GUI для функции (если требуется)
     * @return Screen или null, если GUI не требуется
     */
    @Nullable
    Screen createScreen();
    
    /**
     * Возвращает горячую клавишу для функции (если требуется)
     * @return KeyMapping или null, если горячая клавиша не требуется
     */
    @Nullable
    KeyMapping getKeyBinding();
    
    /**
     * Проверяет, доступна ли функция в текущем окружении
     */
    default boolean isAvailable() {
        return !net.minecraftforge.fml.loading.FMLEnvironment.production;
    }
}

