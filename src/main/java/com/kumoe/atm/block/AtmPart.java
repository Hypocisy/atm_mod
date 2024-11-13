package com.kumoe.atm.block;

import net.minecraft.util.StringRepresentable;

public enum AtmPart implements StringRepresentable {
    UPPER("upper"), LOWER("lower");
    private final String name;

    AtmPart(String pName) {
        this.name = pName;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
