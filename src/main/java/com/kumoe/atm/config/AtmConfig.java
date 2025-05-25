package com.kumoe.atm.config;

import com.kumoe.atm.AtmMod;
import com.kumoe.atm.item.CoinType;

public class AtmConfig {
    public static boolean showHud;
    public static boolean enableSwap;
    public static double sliverValue;
    public static double copperValue;
    public static double goldValue;
    public static double button1Tax;
    public static double button2Tax;
    public static double button3Tax;
    public static int updateInterval;
    public static int distanceToCost;
    public static int perDistanceCost;
    public static int dimensionalTeleportCost;
    public static int textOffset;
    public static int valueScale;
    public static String apiUrl;
    public static String apiParams;

    public static Config config = AtmMod.getInstance().getConfig();
    public static boolean enableWayStoneCompat;
    public static CoinType consumeCoinType;

    public static void bake() {
        // general settings
        initGeneralSettings();
    }

    private static void initGeneralSettings() {
        try {
            sliverValue = config.sliverValue.get();
            copperValue = config.copperValue.get();
            goldValue = config.goldValue.get();
            button1Tax = config.button1Tax.get();
            button2Tax = config.button2Tax.get();
            button3Tax = config.button3Tax.get();
            updateInterval = config.updateInterval.get();
            consumeCoinType = config.consumeCoinType.get();
            distanceToCost = config.distanceToCost.get();
            perDistanceCost = config.perDistanceCost.get();
            dimensionalTeleportCost = config.dimensionalTeleportCost.get();
            textOffset = config.textOffset.get();
            enableSwap = config.enableSwap.get();
            enableWayStoneCompat = config.enableWayStoneCompat.get();
            showHud = config.showHud.get();
            apiUrl = config.apiUrl.get();
            apiParams = config.apiParams.get();
            valueScale = config.valueScale.get();
        } catch (Exception var) {
            AtmMod.LOGGER.trace("An exception was caused trying to load the config for GeneralSettings.\n%s".formatted(var));
        }
    }
}
