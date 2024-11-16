package com.kumoe.atm.block;

import com.kumoe.atm.AtmMod;
import com.kumoe.atm.network.DepositPacket;
import com.kumoe.atm.network.NetworkHandler;
import com.kumoe.atm.network.QueryPlayerBalance;
import com.kumoe.atm.network.WithdrawPacket;
import com.kumoe.atm.uitls.ModUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import java.io.File;

public class AtmScreen extends AbstractContainerScreen<AtmMenu> {
    private static final ResourceLocation BG = new ResourceLocation(AtmMod.MODID, "textures/gui/atm.png");
    private final Container container;
    private final AtmBlockEntity atmBlockEntity;
    private final Player player;

    public AtmScreen(AtmMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.player = pMenu.playerInventory.player;
        this.container = pMenu.container;
        this.atmBlockEntity = (AtmBlockEntity) this.container;
    }

    @Override
    protected void init() {
        super.init();
        var deposit_width = 25;
        var deposit_height = 14;
        var withdraw_width = 18;
        var withdraw_height = 12;
        var deposit_1 = new ImageButtonWithId(0, leftPos + 7, topPos + 21, deposit_width, deposit_height, 176, 0, deposit_height, BG, this::onPress);
        var deposit_2 = new ImageButtonWithId(1, leftPos + 36, topPos + 21, deposit_width, deposit_height, 176 + deposit_width, 0, deposit_height, BG, this::onPress);
        var deposit_3 = new ImageButtonWithId(2, leftPos + 66, topPos + 21, deposit_width, deposit_height, 176 + deposit_width * 2, 0, deposit_height, BG, this::onPress);
        var withdraw = new ImageButtonWithId(3, leftPos + 133, topPos + 59, withdraw_width, withdraw_height, 176, deposit_height * 2, withdraw_height, BG, this::onPress);
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
        // 1000 -> 10 gold
        // 100 -> 1 gold
        // 10 -> 1 silver
        if (button instanceof ImageButtonWithId buttonWithId) {
            var id = buttonWithId.getId();
            var blockPos = this.atmBlockEntity.getBlockPos();
            var uuid = this.player.getUUID();
            var network = NetworkHandler.getInstance();
            switch (id) {
                case 0 ->
                    // get 1 silver
                        network.sendToServer(WithdrawPacket.create(uuid, 1, blockPos));
                case 1 ->
                    // get 1 gold
                        network.sendToServer(WithdrawPacket.create(uuid, 1, blockPos));
                case 2 ->
                    // get 10 gold
                {
                    network.sendToServer(new QueryPlayerBalance(uuid, 0d));
                    network.sendToServer(WithdrawPacket.create(uuid, 10, blockPos));
                }
                default -> {
                    // withdraw coin
                    if (!atmBlockEntity.getSlotDataList().isEmpty()) {
                        // Query player balance

                        network.sendToServer(DepositPacket.create(this.player.getUUID(), atmBlockEntity.getSlotDataList(atmBlockEntity.getItemStackHandler()), blockPos));
                    }
                }
            }
        }
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
