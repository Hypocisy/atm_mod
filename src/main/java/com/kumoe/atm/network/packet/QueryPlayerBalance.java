package com.kumoe.atm.network.packet;

import com.kumoe.atm.AtmMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class QueryPlayerBalance {

    public static double cachedBalance;
    private static Consumer<Double> callback;
    private final UUID uuid;
    private final double balance;
    private final boolean isPlugin;

    public QueryPlayerBalance(UUID uuid, double balance, boolean isPlugin) {
        this.uuid = uuid;
        this.balance = balance;
        this.isPlugin = isPlugin;
    }

    public static QueryPlayerBalance decode(FriendlyByteBuf buf) {
        return new QueryPlayerBalance(buf.readUUID(), buf.readDouble(), buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(this.uuid);
        buf.writeDouble(this.balance);
        buf.writeBoolean(false);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            if (ctx.getDirection().getReceptionSide().isClient()) {
                AtmMod.LOGGER.info("[Server->Client]player {} cached balance: {}", uuid, cachedBalance);
                AtmMod.LOGGER.info("[Server->Client] is plugin {}", isPlugin);
                cachedBalance = balance;
            } else if (ctx.getDirection().getReceptionSide().isServer()) {
                AtmMod.LOGGER.info("[Client->Server]player {} cached balance: {}", uuid, cachedBalance);
                AtmMod.LOGGER.info("[Client->Server] is plugin {}", isPlugin);
            }
        });
        ctx.setPacketHandled(true);
    }
}
