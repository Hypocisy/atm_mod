package com.kumoe.atm.network;

import com.kumoe.atm.AtmMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class QueryPlayerBalance {

    private final UUID uuid;
    private final double balance;
    public static final Map<UUID, Double> SYNCED_DATA = new HashMap<>();

    public QueryPlayerBalance(UUID uuid, double balance) {
        this.uuid = uuid;
        this.balance = balance;
    }

    public static QueryPlayerBalance decode(FriendlyByteBuf buf) {
        return new QueryPlayerBalance(buf.readUUID(), buf.readDouble());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(this.uuid);
        buf.writeDouble(this.balance);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            if (ctx.getDirection().getReceptionSide().isClient()) {
                AtmMod.LOGGER.info("player {} {}", uuid, balance);
            }
        });
        ctx.enqueueWork(this::handlePacketOnMainThread);
        ctx.setPacketHandled(true);
    }
    private void handlePacketOnMainThread() {
        SYNCED_DATA.put(uuid, balance);
    }

    public double getBalance() {
        return balance;
    }
}
