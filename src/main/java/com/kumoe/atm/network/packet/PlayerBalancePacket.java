package com.kumoe.atm.network.packet;

import com.kumoe.atm.AtmMod;
import com.kumoe.atm.network.NetworkHandler;
import com.kumoe.atm.uitls.PluginUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class PlayerBalancePacket {
    private static final Map<UUID, Double> cachedBalances = new HashMap<>();
    @Nullable
    private static BalanceUpdateListener balanceUpdateListener;
    private final UUID uuid;
    private final double balance;

    public PlayerBalancePacket(UUID uuid, double balance) {
        this.uuid = uuid;
        this.balance = balance;
    }

    public PlayerBalancePacket(ServerPlayer player, double balance) {
        this(player.getUUID(), balance);
    }

    public static void requestUpdateCache(UUID uuid) {
        NetworkHandler.sendToServer(new PlayerBalancePacket(uuid, 0d));
    }

    public static void setBalanceUpdateListener(@Nullable BalanceUpdateListener listener) {
        balanceUpdateListener = listener;
    }

    public static PlayerBalancePacket decode(FriendlyByteBuf buf) {
        return new PlayerBalancePacket(buf.readUUID(), buf.readDouble());
    }

    public static Optional<Double> getBalance(UUID playerUUID) {
        return Optional.ofNullable(cachedBalances.get(playerUUID));
    }

    public static Optional<Double> getBalance(ServerPlayer player) {
        return getBalance(player.getUUID());
    }

    public static void updateServerCache(ServerPlayer player, double toUpdate) {
        cachedBalances.put(player.getUUID(), toUpdate);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeDouble(balance);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            if (ctx.getDirection().getReceptionSide().isClient()) {
                handleClientSide();
            } else {
                handleServerSide(ctx.getSender());
            }
        });
        ctx.setPacketHandled(true);
    }

    private void handleClientSide() {
        cachedBalances.put(uuid, balance);
        AtmMod.LOGGER.info("Client received balance update for player {}: {}", uuid, balance);
        if (balanceUpdateListener != null) {
            balanceUpdateListener.onBalanceUpdate(uuid, balance);
        }
    }

    private void handleServerSide(@Nullable ServerPlayer player) {
        if (player == null) return;

        if (!PluginUtils.checkBukkitInstalled()) {
            AtmMod.LOGGER.error("Bukkit server is null!");
            return;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin("SeasonShop");
        if (plugin == null) {
            AtmMod.LOGGER.error("SeasonShop plugin not found!");
            return;
        }

        double currentBalance = PluginUtils.getBalance(player);

        updateServerCache(player, currentBalance);

        NetworkHandler.sendToPlayer(
                PacketDistributor.PLAYER.with(() -> player),
                new PlayerBalancePacket(uuid, currentBalance)
        );
    }

    @FunctionalInterface
    public interface BalanceUpdateListener {
        void onBalanceUpdate(UUID playerUUID, double newBalance);
    }
}