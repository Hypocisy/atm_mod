package com.kumoe.atm.network;

import com.kumoe.atm.AtmMod;
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
        INSTANCE.registerMessage(0, DepositPacket.class, DepositPacket::encode, DepositPacket::decode, DepositPacket::handle);
        INSTANCE.registerMessage(1, WithdrawPacket.class, WithdrawPacket::encode, WithdrawPacket::decode, WithdrawPacket::handle);
        INSTANCE.registerMessage(2, QueryPlayerBalance.class, QueryPlayerBalance::encode, QueryPlayerBalance::decode, QueryPlayerBalance::handle);

    }

    public static SimpleChannel getInstance() {
        return INSTANCE;
    }

    public <MSG> void sendToServer(MSG msg) {
        INSTANCE.sendToServer(msg);
    }

    public <MSG> void sendToPlayer(PacketDistributor.PacketTarget with, MSG packet) {
        INSTANCE.send(with, packet);
    }
}