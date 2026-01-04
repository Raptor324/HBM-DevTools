package com.hbm_devtools;

import com.hbm_devtools.core.registry.FeatureRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(HBMDevTools.MOD_ID)
public class HBMDevTools {
    public static final String MOD_ID = "hbm_devtools";
    public static final Logger LOGGER = LogManager.getLogger();

    public HBMDevTools(IEventBus modEventBus) {
        modEventBus.addListener(this::onClientSetup);
        
        // Проверка, что мы в dev-окружении
        if (!net.minecraftforge.fml.loading.FMLEnvironment.production) {
            LOGGER.info("HBM DevTools initialized in development environment");
        } else {
            LOGGER.warn("HBM DevTools should not be loaded in production! Disabling features.");
        }
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        if (!net.minecraftforge.fml.loading.FMLEnvironment.production) {
            event.enqueueWork(() -> {
                FeatureRegistry.initialize();
                LOGGER.info("HBM DevTools features registered");
            });
        }
    }
}

