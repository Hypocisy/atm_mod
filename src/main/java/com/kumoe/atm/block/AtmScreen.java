package com.kumoe.atm.block;

import com.kumoe.atm.AtmMod;
import com.kumoe.atm.network.NetworkHandler;
import com.kumoe.atm.network.packet.DepositPacket;
import com.kumoe.atm.network.packet.PlayerBalancePacket;
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
import java.util.UUID;

public class AtmScreen extends AbstractContainerScreen<AtmMenu> implements PlayerBalancePacket.BalanceUpdateListener {
    private static final ResourceLocation BG = new ResourceLocation(AtmMod.MODID, "textures/gui/atm.png");
    private final Container container;
    private final AtmBlockEntity atmBlockEntity;
    private final Player player;
    private double currentBalance;

    public AtmScreen(AtmMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.player = pMenu.playerInventory.player;
        this.container = pMenu.container;
        this.atmBlockEntity = (AtmBlockEntity) this.container;
        this.currentBalance = PlayerBalancePacket.getBalance(player.getUUID()).orElse(0.0);
        PlayerBalancePacket.setBalanceUpdateListener(this);
    }

    @Override
    protected void init() {
        super.init();
        initButtons();
    }

    private void initButtons() {

        addDepositButton(0, 7, 10);
        addDepositButton(1, 36, 100);
        addDepositButton(2, 66, 1000);
        addWithdrawButton(133, 59);
    }

    private void addDepositButton(int id, int x, int amount) {
        int depositWidth = 25;
        int depositHeight = 14;

        var button = new ImageButtonID(
                id,
                leftPos + x,
                topPos + 21,
                depositWidth,
                depositHeight,
                176 + (id * 25),
                0,
                14,
                BG,
                this::onPress
        );
        button.setTooltip(Tooltip.create(Component.translatable("screen.atm_mod.deposit", amount)));
        addRenderableWidget(button);
    }

    private void addWithdrawButton(int x, int y) {
        int withdrawWidth = 18;
        int withdrawHeight = 12;
        var button = new ImageButtonID(
                3,
                leftPos + x,
                topPos + y,
                withdrawWidth,
                withdrawHeight,
                176,
                28,
                12,
                BG,
                this::onPress
        );
        button.setTooltip(Tooltip.create(Component.translatable("screen.atm_mod.withdraw")));
        addRenderableWidget(button);
    }

    private void onPress(Button button) {
        if (!(button instanceof ImageButtonID buttonWithId)) return;

        var id = buttonWithId.getId();
        var blockPos = atmBlockEntity.getBlockPos();
        var userUuid = player.getUUID();
        var ownerUuid = atmBlockEntity.getOwnerUuid();
        var itemStackHandler = atmBlockEntity.getItemStackHandler();

        switch (id) {
            case 0, 1, 2 -> handleDeposit(id, userUuid, ownerUuid, blockPos);
            case 3 -> handleWithdraw(userUuid, ownerUuid, blockPos, itemStackHandler);
            default -> throw new IllegalStateException("Unexpected case id: " + id);
        }
    }

    private void handleDeposit(int id, UUID userUuid, UUID ownerUuid, net.minecraft.core.BlockPos blockPos) {
        int amount = switch (id) {
            case 0 -> 10;
            case 1 -> 100;
            case 2 -> 1000;
            default -> throw new IllegalStateException("Unexpected value: " + id);
        };

        if (currentBalance > 0d) {
            NetworkHandler.sendToServer(WithdrawPacket.create(ownerUuid, userUuid, amount, blockPos));
            PlayerBalancePacket.requestUpdateCache(userUuid);
        } else {
            player.closeContainer();
            player.sendSystemMessage(Component.translatable("screen.atm_mod.not_enough_money"));
        }
    }

    private void handleWithdraw(UUID userUuid, UUID ownerUuid, net.minecraft.core.BlockPos blockPos,
                                net.minecraftforge.items.ItemStackHandler itemStackHandler) {
        if (!atmBlockEntity.getSlotDataList().isEmpty()) {
            NetworkHandler.sendToServer(DepositPacket.create(ownerUuid,
                    atmBlockEntity.getSlotDataList(itemStackHandler), blockPos));
            PlayerBalancePacket.requestUpdateCache(userUuid);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.pose().pushPose();
        graphics.blit(BG, leftPos, topPos, 0, 0, 176, 166);
        renderOwnerAvatar(graphics);
        graphics.pose().popPose();
    }

    private void renderOwnerAvatar(GuiGraphics graphics) {
        if (!(getBlockEntity() instanceof AtmBlockEntity atmBlockEntity)) return;

        var ownerUuid = atmBlockEntity.getOwnerUuid();
        File avatarFile = ModUtils.getAvatarFile(ownerUuid);

        if (avatarFile.exists()) {
            ResourceLocation avatarLocation = ModUtils.loadPlayerAvatar(avatarFile, ownerUuid);
            if (avatarLocation != null) {
                graphics.blit(avatarLocation, leftPos + 135, topPos + 22, 0, 0, 16, 16, 16, 16);
            }
        }
    }

    @Override
    public void onBalanceUpdate(UUID playerUUID, double newBalance) {
        if (playerUUID.equals(player.getUUID())) {
            this.currentBalance = newBalance;
        }
    }

    public Container getBlockEntity() {
        return container;
    }
}