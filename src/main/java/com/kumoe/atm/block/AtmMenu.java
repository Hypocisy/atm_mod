package com.kumoe.atm.block;

import com.kumoe.atm.registry.AtmRegistries;
import com.kumoe.atm.uitls.ModUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;

public class AtmMenu extends AbstractContainerMenu {

    protected final Container container;
    protected final Inventory playerInventory;
    protected final int containerRows;
    protected final int containerColumns = 1;
    private final int e;

    public AtmMenu(int windowId, Inventory pPlayerInventory, int pContainerRows, Container pContainer) {
        super(AtmRegistries.ATM_MENU.get(), windowId);
        this.container = pContainer;
        this.playerInventory = pPlayerInventory;
        this.containerRows = pContainerRows;
        this.e = (this.containerRows - 4) * 18;
        // add output slots
        for (int slot = 0; slot < 6; ++slot) { // 渲染一行6个槽, 在第三行开始渲染
            this.addSlot(new CustomSlot(((AtmBlockEntity) pContainer).getItemStackHandler(), slot, 8 + slot * 18, 18 * 3));
        }
        addPlayerSlots(pPlayerInventory, 1);
        this.container.startOpen(pPlayerInventory.player);
    }

    public AtmMenu(int windowId, Inventory pPlayerInventory, int pContainerRows, FriendlyByteBuf data) {
        this(windowId, pPlayerInventory, pContainerRows, getTileEntity(pPlayerInventory, data));
    }

    private static AtmBlockEntity getTileEntity(Inventory playerInventory, FriendlyByteBuf data) {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null");
        Objects.requireNonNull(data, "data cannot be null");
        BlockEntity tileAtPos = playerInventory.player.level().getBlockEntity(data.readBlockPos());
        if (tileAtPos instanceof AtmBlockEntity bin) {
            if (bin.getLevel().isClientSide()) {
                Minecraft.getInstance().submitAsync(() -> ModUtils.cachePlayerAvatar(bin.getOwnerUuid()));
            }
            bin.setOwnerUuid(data.readUUID());
            return bin;
        } else {
            throw new IllegalStateException("Tile entity is not correct! " + tileAtPos);
        }
    }

    public static AtmMenu factory(int containerId, Inventory inventory, int pContainerRows, FriendlyByteBuf data) {
        return new AtmMenu(containerId, inventory, pContainerRows, data);
    }

    public static AtmMenu factory(int containerId, Inventory inventory, FriendlyByteBuf data) {
        return factory(containerId, inventory, 3, data);
    }

    void addPlayerSlots(Inventory pPlayerInventory, int yOffset) {
        int row;
        int slot;
        // add player inv
        for (row = 0; row < 3; ++row) {
            for (slot = 0; slot < 9; ++slot) {
                this.addSlot(new Slot(pPlayerInventory, slot + row * 9 + 9, 8 + slot * 18, 103 + row * 18 + this.e - yOffset));
            }
        }

        for (row = 0; row < 9; ++row) {
            this.addSlot(new Slot(pPlayerInventory, row, 8 + row * 18, 160 + this.e - yOffset));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot.hasItem()) {
            ItemStack slotItem = slot.getItem();
            itemStack = slotItem.copy();
            if (pIndex < this.containerRows * this.containerColumns) {
                if (!this.moveItemStackTo(slotItem, this.containerRows * this.containerColumns, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotItem, 0, this.containerRows * this.containerColumns, false)) {
                return ItemStack.EMPTY;
            }

            if (slotItem.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemStack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return this.container.stillValid(pPlayer);
    }
}
