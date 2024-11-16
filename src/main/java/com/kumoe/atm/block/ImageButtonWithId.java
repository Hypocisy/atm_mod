package com.kumoe.atm.block;

import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.resources.ResourceLocation;

public class ImageButtonWithId extends ImageButton {
    private final int id;

    public ImageButtonWithId(int id, int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, int pYDiffTex, ResourceLocation pResourceLocation, OnPress pOnPress) {
        super(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, pYDiffTex, pResourceLocation, pOnPress);
        this.id = id;
    }


    public int getId() {
        return id;
    }
}
