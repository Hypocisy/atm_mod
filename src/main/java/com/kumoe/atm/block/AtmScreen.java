package com.kumoe.atm.block;

import com.kumoe.atm.AtmMod;
import com.kumoe.atm.uitls.ModUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;

import java.io.File;

public class AtmScreen extends AbstractContainerScreen<AtmMenu> {
    private static final ResourceLocation BG = new ResourceLocation(AtmMod.MODID, "textures/gui/atm.png");
    private final Container container;

    public AtmScreen(AtmMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.container = pMenu.container;
    }

    @Override
    protected void init() {
        super.init();
        var deposit_width = 25;
        var deposit_height = 14;
        var withdraw_width = 18;
        var withdraw_height = 12;
        var deposit_1 = new ImageButton(leftPos + 7, topPos + 21, deposit_width, deposit_height, 176, 0, deposit_height, BG, this::onPress);
        var deposit_2 = new ImageButton(leftPos + 36, topPos + 21, deposit_width, deposit_height, 176 + deposit_width, 0, deposit_height, BG, this::onPress);
        var deposit_3 = new ImageButton(leftPos + 66, topPos + 21, deposit_width, deposit_height, 176 + deposit_width * 2, 0, deposit_height, BG, this::onPress);
        var withdraw = new ImageButton(leftPos + 133, topPos + 59, withdraw_width, withdraw_height, 176, deposit_height * 2, withdraw_height, BG, this::onPress);
        deposit_1.setTooltip(Tooltip.create(Component.translatable("screen.atm_mod.deposit", 10)));
        deposit_2.setTooltip(Tooltip.create(Component.translatable("screen.atm_mod.deposit", 100)));
        deposit_3.setTooltip(Tooltip.create(Component.translatable("screen.atm_mod.deposit", 1000)));
        withdraw.setTooltip(Tooltip.create(Component.translatable("screen.atm_mod.withdraw")));
        addRenderableWidget(deposit_1);
        addRenderableWidget(deposit_2);
        addRenderableWidget(deposit_3);
        addRenderableWidget(withdraw);

    }

    private void onPress(Button button) {

    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);

    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.blit(BG, leftPos, topPos, 0, 0, 176, 166);
        // todo render owner's icon and more info

        if (getBlockEntity() instanceof AtmBlockEntity atmBlockEntity) {
            var ownerUuid = atmBlockEntity.getOwnerUuid();
            File avatarFile = ModUtils.getAvatarFile(ownerUuid);
            if (avatarFile.exists()) {
                // render player avatar
                ResourceLocation avatarLocation = ModUtils.loadPlayerAvatar(avatarFile, ownerUuid);
                if (avatarLocation != null) {
                    pGuiGraphics.blit(avatarLocation, leftPos + 135, topPos + 22, 0, 0, 16, 16, 16, 16);
                }
            }
        }
        pGuiGraphics.pose().popPose();
    }

    public Container getBlockEntity() {
        return container;
    }
}
