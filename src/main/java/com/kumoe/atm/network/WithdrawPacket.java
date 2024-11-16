package com.kumoe.atm.network;

import com.kumoe.atm.AtmMod;
import com.kumoe.atm.block.AtmBlockEntity;
import com.kumoe.atm.item.Coin;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkEvent;

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
                if (ctx.getSender().level().getBlockEntity(pos) instanceof AtmBlockEntity bin) {
                    AtmMod.LOGGER.debug("price {}", price);
                    bin.clearContent();
                    bin.setChanged();
                }
            }
        });
        ctx.setPacketHandled(true);
    }

}
