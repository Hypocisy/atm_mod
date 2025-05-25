package com.kumoe.atm.config;

import com.kumoe.atm.item.CoinType;
import net.minecraftforge.common.ForgeConfigSpec;

public class Config {

    protected ForgeConfigSpec.DoubleValue sliverValue;
    protected ForgeConfigSpec.DoubleValue copperValue;
    protected ForgeConfigSpec.DoubleValue goldValue;
    protected ForgeConfigSpec.DoubleValue button1Tax;
    protected ForgeConfigSpec.DoubleValue button2Tax;
    protected ForgeConfigSpec.DoubleValue button3Tax;
    protected ForgeConfigSpec.BooleanValue enableSwap;
    protected ForgeConfigSpec.BooleanValue enableWayStoneCompat;
    protected ForgeConfigSpec.BooleanValue showHud;
    protected ForgeConfigSpec.IntValue updateInterval;
    protected ForgeConfigSpec.IntValue textOffset;
    protected ForgeConfigSpec.IntValue distanceToCost;
    protected ForgeConfigSpec.IntValue perDistanceCost;
    protected ForgeConfigSpec.IntValue dimensionalTeleportCost;
    protected ForgeConfigSpec.IntValue valueScale;
    protected ForgeConfigSpec.EnumValue<CoinType> consumeCoinType;
    protected ForgeConfigSpec.ConfigValue<String> apiUrl;
    protected ForgeConfigSpec.ConfigValue<String> apiParams;

    public Config(ForgeConfigSpec.Builder builder) {
        builder.push("General settings");
        {
            sliverValue = builder.comment("设置银币的价格").defineInRange("sliverValue", 10d, 0.1d, Double.MAX_VALUE);
            copperValue = builder.comment("设置铜币的价格").defineInRange("copperValue", 1d, 0.1d, Double.MAX_VALUE);
            goldValue = builder.comment("设置金币的价格").defineInRange("goldValue", 100d, 0.1d, Double.MAX_VALUE);
            button1Tax = builder.comment("设置第一个按钮收的税").defineInRange("button1Tax", 1d, 1d, Double.MAX_VALUE);
            button2Tax = builder.comment("设置第二个按钮收的税").defineInRange("button2Tax", 10d, 1d, Double.MAX_VALUE);
            button3Tax = builder.comment("设置第三个按钮收的税").defineInRange("button3Tax", 100d, 1d, Double.MAX_VALUE);
            updateInterval = builder.comment("设置hud更新间隔").defineInRange("updateInterval", 3, 1, Integer.MAX_VALUE);
            distanceToCost = builder.comment("设置传送时每X米消耗一次硬币的距离").defineInRange("distanceToCost", 500, 1, Integer.MAX_VALUE);
            perDistanceCost = builder.comment("设置distanceToCost传送消耗多少个银币").defineInRange("perDistanceCost", 3, 1, Integer.MAX_VALUE);
            dimensionalTeleportCost = builder.comment("设置跨次元传送消耗多少个银币").defineInRange("dimensionalTeleportCost", 3, 1, Integer.MAX_VALUE);
            valueScale = builder.comment("设置客户端显示保留多少小数点").defineInRange("valueScale", 2, 1, Integer.MAX_VALUE);
            textOffset = builder.comment("设置vault hud 文字的y轴位置").defineInRange("textOffset", 0, 0, 1000);
            consumeCoinType = builder.comment("设置每次传送消耗多少个物品").defineEnum("consumeCoinType", CoinType.COPPER, CoinType.values());
            enableSwap = builder.comment("设置是否开启shift转换功能").define("enableSwap", true);
            enableWayStoneCompat = builder.comment("设置是否开启wayStone兼容功能").define("enableWayStoneCompat", true);
            showHud = builder.comment("设置是否开启gui显示余额").define("showHud", true);
            apiUrl = builder.comment("设置当前请求头像使用的api").define("apiUrl", "https://crafatar.com/avatars/");
            apiParams = builder.comment("设置当前请求头像使用的api").define("apiParams", "?size=16");
        }
        builder.pop();
    }
}