package com.kumoe.atm.config;

import com.kumoe.atm.AtmMod;

public class AtmConfig {
    public static boolean enableSwap;
    public static double sliverValue;
    public static double copperValue;
    public static double goldValue;
    public static String apiUrl;
    public static String apiParams;

    public static Config config = AtmMod.getInstance().getConfig();

    public static void bake() {
        // general settings
        initGeneralSettings();
    }

    private static void initGeneralSettings() {
        try {
            sliverValue = config.sliverValue.get();
            copperValue = config.copperValue.get();
            goldValue = config.goldValue.get();
            enableSwap = config.enableSwap.get();
            apiUrl = config.apiUrl.get();
            apiParams = config.apiParams.get();
        } catch (Exception var) {
            AtmMod.LOGGER.trace("An exception was caused trying to load the config for GeneralSettings.\n%s".formatted(var));
        }
    }
}
