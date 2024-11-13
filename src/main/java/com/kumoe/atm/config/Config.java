package com.kumoe.atm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {

    protected ForgeConfigSpec.DoubleValue sliverValue;
    protected ForgeConfigSpec.DoubleValue copperValue;
    protected ForgeConfigSpec.DoubleValue goldValue;
    protected ForgeConfigSpec.BooleanValue enableSwap;
    protected ForgeConfigSpec.ConfigValue<String> apiUrl;
    protected ForgeConfigSpec.ConfigValue<String> apiParams;

    public Config(ForgeConfigSpec.Builder builder) {
        builder.push("General settings");
        {
            sliverValue = builder.comment("设置银币的价格").defineInRange("sliverValue", 1d, 0.1d, Double.MAX_VALUE);
            copperValue = builder.comment("设置铜币的价格").defineInRange("copperValue", 10d, 0.1d, Double.MAX_VALUE);
            goldValue = builder.comment("设置金币的价格").defineInRange("goldValue", 100d, 0.1d, Double.MAX_VALUE);
            enableSwap = builder.comment("设置是否开启shift转换功能").define("enableSwap", true);
            apiUrl = builder.comment("设置当前请求头像使用的api").define("apiUrl", "https://crafatar.com/avatars/");
            apiParams = builder.comment("设置当前请求头像使用的api").define("apiParams", "?size=16");
        }
        builder.pop();
    }
}