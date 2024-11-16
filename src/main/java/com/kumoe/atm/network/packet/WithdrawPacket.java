package com.kumoe.atm.network.packet;

import com.kumoe.atm.AtmMod;
import com.kumoe.atm.block.AtmBlockEntity;
import com.kumoe.atm.config.AtmConfig;
import com.kumoe.atm.item.Coin;
import com.kumoe.atm.network.NetworkHandler;
import com.kumoe.atm.registry.AtmRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

public class WithdrawPacket {

    private final UUID playerUuid;
    private final double price;
    private final BlockPos pos;

    WithdrawPacket(UUID playerUuid, double price, BlockPos pos) {
        this.playerUuid = playerUuid;
        this.price = price;
        this.pos = pos;
    }

    public static WithdrawPacket decode(FriendlyByteBuf byteBuf) {
        var playerUuid = byteBuf.readUUID();
        var price = byteBuf.readDouble();
        var blockPos = byteBuf.readBlockPos();
        return WithdrawPacket.create(playerUuid, price, blockPos);
    }

    public static WithdrawPacket create(UUID playerUuid, double price, BlockPos pos) {
        return new WithdrawPacket(playerUuid, price, pos);
    }

    public static double getPrice(ItemStackHandler stack) {
        var price = 0.0d;
        for (int i = 0; i < stack.getSlots(); i++) {
            var itemstack = stack.getStackInSlot(i);
            if (itemstack.getItem() instanceof Coin coin) {
                price += coin.getAmount() * itemstack.getCount();
            }
        }
        return price;
    }

    public void encode(FriendlyByteBuf byteBuf) {
        byteBuf.writeUUID(this.playerUuid);
        byteBuf.writeDouble(this.price);
        byteBuf.writeBlockPos(this.pos);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            // remove sold items
            if (ctx.getDirection().getReceptionSide().isServer()) {
                var player = ctx.getSender();
                if (player != null && player.level().getBlockEntity(pos) instanceof AtmBlockEntity bin) {
                    AtmMod.LOGGER.debug("price {}", price);
                    var playerUUID = player.getUUID();

                    if (QueryPlayerBalance.cachedBalance < price) {
                        return;
                    }
                    ItemStack stackToAdd = ItemStack.EMPTY;

                    if (price == AtmConfig.sliverValue) {
                        stackToAdd = new ItemStack(AtmRegistries.SILVER.get());
                    } else if (price == AtmConfig.goldValue) {
                        stackToAdd = new ItemStack(AtmRegistries.GOLD.get(), 1);
                    } else if (price == AtmConfig.goldValue * 10) {
                        stackToAdd = new ItemStack(AtmRegistries.GOLD.get(), 10);
                    }

                    // insert a stack to right slot, or give it to player slot
                    var stackHandler = bin.getItemStackHandler();
                    for (int i = 0; i < stackHandler.getSlots(); i++) {
                        var stackInSlot = stackHandler.getStackInSlot(i);
                        if (stackInSlot.getCount() < 64 && stackToAdd.getCount() + stackInSlot.getCount() <= 64) {
                            stackHandler.insertItem(i, stackToAdd, false);
                            break;
                        } else if (i == stackHandler.getSlots() - 1) {
                            ItemHandlerHelper.giveItemToPlayer(player, stackToAdd);
                            break;
                        }
                    }

                    bin.setChanged();
                }
            }
        });
        ctx.setPacketHandled(true);
    }

}
