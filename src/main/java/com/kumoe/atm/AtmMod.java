package com.kumoe.atm;

import com.kumoe.atm.block.AtmBlockStateProvider;
import com.kumoe.atm.block.AtmScreen;
import com.kumoe.atm.config.AtmConfig;
import com.kumoe.atm.config.Config;
import com.kumoe.atm.item.ModItemModelProvider;
import com.kumoe.atm.network.NetworkHandler;
import com.kumoe.atm.registry.AtmRegistries;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(AtmMod.MODID)
public class AtmMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "atm_mod";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    private static AtmMod instance;
    final Pair<Config, ForgeConfigSpec> configured = (new ForgeConfigSpec.Builder()).configure(Config::new);

    public AtmMod(FMLJavaModLoadingContext context) {
        instance = this;
        // add register method
        AtmRegistries.register(context.getModEventBus());
        context.getModEventBus().addListener(this::onClientSetup);
        context.getModEventBus().addListener(this::onGatherData);
        context.getModEventBus().addListener(this::onModConfigLoad);
        context.getModEventBus().addListener(this::onCommonSetup);
        context.registerConfig(ModConfig.Type.SERVER, configured.getRight());
    }

    public static AtmMod getInstance() {
        return instance;
    }

    private void onModConfigLoad(ModConfigEvent event) {
        ModConfig config = event.getConfig();
        if (config.getSpec() == AtmMod.getInstance().getConfigSpec()) {
            AtmMod.LOGGER.debug("Loading " + AtmMod.MODID + " config");
            AtmConfig.bake();
        }
    }

    private void onGatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var existing = event.getExistingFileHelper();
        var packOutput = generator.getPackOutput();
        generator.addProvider(event.includeClient(), new AtmBlockStateProvider(packOutput, MODID, existing));
        generator.addProvider(event.includeClient(), new ModItemModelProvider(packOutput, MODID, existing));
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        MenuScreens.register(AtmRegistries.ATM_MENU.get(), AtmScreen::new);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        NetworkHandler.register();
    }

    public Config getConfig() {
        return this.configured.getLeft();
    }

    public ForgeConfigSpec getConfigSpec() {
        return configured.getRight();
    }
}
