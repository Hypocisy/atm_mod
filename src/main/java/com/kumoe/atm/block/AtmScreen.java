package com.kumoe.atm.block;

import com.kumoe.atm.AtmMod;
import com.kumoe.atm.network.NetworkHandler;
import com.kumoe.atm.network.packet.DepositPacket;
import com.kumoe.atm.network.packet.QueryPlayerBalance;
import com.kumoe.atm.network.packet.WithdrawPacket;
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
            var packet = new QueryPlayerBalance(uuid, 0, false);
            network.sendToServer(packet);
            var cachedBalance = QueryPlayerBalance.cachedBalance;
            var itemStackHandler = atmBlockEntity.getItemStackHandler();

            switch (id) {
                case 0 -> {
                    // Withdraw coin
                    if (cachedBalance > 0d) {
                        network.sendToServer(WithdrawPacket.create(uuid, 10, blockPos));
                    } else {
                        player.closeContainer();
                        player.sendSystemMessage(Component.literal("you don't have enough money to withdraw"));
                    }
                }
                case 1 -> {
                    // Withdraw coin
                    if (cachedBalance > 0d) {
                        network.sendToServer(WithdrawPacket.create(uuid, 100, blockPos));
                    } else {
                        player.closeContainer();
                        player.sendSystemMessage(Component.literal("you don't have enough money to withdraw"));
                    }
                }
                case 2 -> {
                    // Withdraw coin
                    if (cachedBalance > 0d) {
                        network.sendToServer(WithdrawPacket.create(uuid, 1000, blockPos));
                    } else {
                        player.closeContainer();
                        player.sendSystemMessage(Component.literal("you don't have enough money to withdraw"));
                    }
                }
                case 3 -> {
                    // Deposit coin
                    if (!atmBlockEntity.getSlotDataList().isEmpty()) {
                        network.sendToServer(DepositPacket.create(uuid, atmBlockEntity.getSlotDataList(itemStackHandler), blockPos));
                    }
                }
            }
        }
    }

    double depositIdToPrice(int id) {
        return switch (id) {
            case 0 -> 10;
            case 1 -> 100;
            default -> 1000;
        };
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
