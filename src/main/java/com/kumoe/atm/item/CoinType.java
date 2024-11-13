package com.kumoe.atm.item;

import net.minecraft.util.StringRepresentable;

public enum CoinType implements StringRepresentable {
    GOLD("Gold"),
    COPPER("Copper"),
    SILVER("Silver");
    private final String name;
    CoinType(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getSerializedName() {
        return "CoinType: " + this;
    }
}
