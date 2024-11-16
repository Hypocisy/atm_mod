package com.kumoe.atm.block;

import com.kumoe.atm.item.Coin;
import com.kumoe.atm.network.DepositPacket;
import com.kumoe.atm.registry.AtmRegistries;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AtmBlockEntity extends BaseContainerBlockEntity {
    private static final String OWN_KEY = "ownEntity.playerUuid";
    private final NonNullList<ItemStack> items = NonNullList.withSize(6, ItemStack.EMPTY);
    private final ItemStackHandler itemStackHandler = new ItemStackHandler(items);
    private UUID ownerUuid;

    public AtmBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(AtmRegistries.ATM_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    @Override
    protected Component getDefaultName() {
        return getName();
    }

    @Override
    protected AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory) {
        return AtmMenu.factory(pContainerId, pInventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(this.worldPosition).writeUUID(this.ownerUuid));
    }

    @Override
    public int getContainerSize() {
        return itemStackHandler.getSlots();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public @NotNull ItemStack getItem(int pSlot) {
        return itemStackHandler.getStackInSlot(pSlot);
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        return ContainerHelper.takeItem(this.items, pSlot);
    }

    @Override
    public ItemStack removeItemNoUpdate(int pSlot) {
        return items.remove(pSlot);
    }

    @Override
    public void setItem(int pSlot, ItemStack pStack) {
        itemStackHandler.setStackInSlot(pSlot, pStack);
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return Container.stillValidBlockEntity(this, pPlayer);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        ContainerHelper.loadAllItems(pTag, this.items);
        if (!pTag.isEmpty() && pTag.contains(OWN_KEY)) {
            ownerUuid = pTag.getUUID(OWN_KEY);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        ContainerHelper.saveAllItems(pTag, this.items);
        if (!pTag.isEmpty() && ownerUuid != null) {
            pTag.putUUID(OWN_KEY, ownerUuid);
        }

    }

    public UUID getOwnerUuid() {
        return this.ownerUuid;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public ItemStackHandler getItemStackHandler() {
        return itemStackHandler;
    }

    public List<DepositPacket.SlotData> getSlotDataList(ItemStackHandler itemStackHandler) {
        List<DepositPacket.SlotData> slotDataList = new ArrayList<>();
        for (var i = 0; i < itemStackHandler.getSlots(); i++) {
            var stack = itemStackHandler.getStackInSlot(i);
            if (stack.getItem() instanceof Coin coin) {
                slotDataList.add(new DepositPacket.SlotData(coin.getType(), stack.getCount()));
            }
        }
        return slotDataList;
    }
    public List<DepositPacket.SlotData> getSlotDataList(){
        return getSlotDataList(itemStackHandler);
    }
}
