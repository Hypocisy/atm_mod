package com.kumoe.atm.network.packet;

import com.kumoe.atm.block.AtmBlockEntity;
import com.kumoe.atm.item.CoinType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class DepositPacket {
    private final UUID playerUuid;
    private final BlockPos pos;
    private final List<SlotData> slotDataList;

    public DepositPacket(UUID playerUuid, List<SlotData> slotDataList, BlockPos pos) {
        this.playerUuid = playerUuid;
        this.slotDataList = slotDataList;
        this.pos = pos;
    }

    public static DepositPacket decode(FriendlyByteBuf byteBuf) {
        var playerUuid = byteBuf.readUUID();

        var size = byteBuf.readInt();
        List<SlotData> slotDataList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            slotDataList.add(SlotData.decode(byteBuf));
        }
        var pos = byteBuf.readBlockPos();
        return DepositPacket.create(playerUuid, slotDataList, pos);
    }

    public static DepositPacket create(UUID playerUuid, List<SlotData> slotDataList, BlockPos pos) {
        return new DepositPacket(playerUuid, slotDataList, pos);
    }

    public void encode(FriendlyByteBuf byteBuf) {
        byteBuf.writeUUID(playerUuid);
        byteBuf.writeInt(slotDataList.size());
        for (SlotData slotData : slotDataList) {
            slotData.encode(byteBuf);
        }
        byteBuf.writeBlockPos(pos);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            // remove sold items
            var player = ctx.getSender();
            if (player != null) {
                var level = player.level();
                if (!level.isClientSide()) {
                    if (level.getBlockEntity(pos) instanceof AtmBlockEntity bin) {
//                    bin.getSlotDataList().forEach(slotData -> AtmMod.LOGGER.info(slotData.toString()));
                        bin.clearContent();
                        bin.setChanged();
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
    }

    public record SlotData(CoinType coinType, int count) {

        public static SlotData decode(FriendlyByteBuf buf) {
            return new SlotData(buf.readEnum(CoinType.class), buf.readInt());
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeEnum(coinType);
            buf.writeInt(count);
        }
    }
}