package com.kumoe.atm.network;

import com.kumoe.atm.AtmMod;
import com.kumoe.atm.network.packet.DepositPacket;
import com.kumoe.atm.network.packet.PlayerBalancePacket;
import com.kumoe.atm.network.packet.WithdrawPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static SimpleChannel INSTANCE;
    private static int id = 0;

    public static int getId() {
        return id++;
    }

    public static void register() {
        INSTANCE = NetworkRegistry.ChannelBuilder.named(
                        new ResourceLocation(AtmMod.MODID, "main")).networkProtocolVersion(() -> PROTOCOL_VERSION)
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();
        INSTANCE.registerMessage(getId(), DepositPacket.class, DepositPacket::encode, DepositPacket::decode, DepositPacket::handle);
        INSTANCE.registerMessage(getId(), WithdrawPacket.class, WithdrawPacket::encode, WithdrawPacket::decode, WithdrawPacket::handle);
        INSTANCE.registerMessage(getId(), PlayerBalancePacket.class, PlayerBalancePacket::encode, PlayerBalancePacket::decode, PlayerBalancePacket::handle);
    }

    public static SimpleChannel getInstance() {
        return INSTANCE;
    }

    public static <MSG> void sendToServer(MSG msg) {
        INSTANCE.sendToServer(msg);
    }

    public static <MSG> void sendToPlayer(PacketDistributor.PacketTarget with, MSG packet) {
        INSTANCE.send(with, packet);
    }
}