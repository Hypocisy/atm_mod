package com.kumoe.atm.network.packet;

import com.kumoe.atm.block.AtmBlockEntity;
import com.kumoe.atm.config.AtmConfig;
import com.kumoe.atm.registry.AtmRegistries;
import com.kumoe.atm.uitls.PluginUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class WithdrawPacket {

    private final UUID ownerUuid;
    private final UUID userUuid;
    private final double price;
    private final BlockPos pos;

    WithdrawPacket(UUID ownerUuid, UUID userUuid, double price, BlockPos pos) {
        this.ownerUuid = ownerUuid;
        this.userUuid = userUuid;
        this.price = price;
        this.pos = pos;
    }

    public static WithdrawPacket decode(FriendlyByteBuf byteBuf) {
        var ownerUuid = byteBuf.readUUID();
        var playerUuid = byteBuf.readUUID();
        var price = byteBuf.readDouble();
        var blockPos = byteBuf.readBlockPos();
        return WithdrawPacket.create(ownerUuid, playerUuid, price, blockPos);
    }

    public static WithdrawPacket create(UUID ownerUuid, UUID playerUuid, double price, BlockPos blockPos) {
        return new WithdrawPacket(ownerUuid, playerUuid, price, blockPos);
    }

    public void encode(FriendlyByteBuf byteBuf) {
        byteBuf.writeUUID(this.ownerUuid);
        byteBuf.writeUUID(this.userUuid);
        byteBuf.writeDouble(this.price);
        byteBuf.writeBlockPos(this.pos);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            if (ctx.getDirection().getReceptionSide().isServer()) {
                var player = ctx.getSender();
                if (player != null && !player.level().isClientSide()) {
                    if (player.level().getBlockEntity(pos) instanceof AtmBlockEntity bin && PluginUtils.checkBukkitInstalled()) {
                        // get plugin value
                        double balance;
                        double tax = calculateTax(price);
                        balance = PluginUtils.getBalance(player);

                        if (balance < price + tax) {
                            player.sendSystemMessage(Component.translatable("screen.atm_mod.not_enough_money"));
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
                        handleItemInsertion(stackHandler, stackToAdd, player);
                        bin.setChanged();

                        // 执行存钱操作
                        // 将tax支付给银行主
                        PluginUtils.depositPlayer(ownerUuid, tax);
                        PluginUtils.withdrawPlayer(userUuid, price, tax);

                        // todo 通知银行主

                    }
                }
            }
        });
        ctx.setPacketHandled(true);
    }

    private double calculateTax(double price) {
        if (userUuid != ownerUuid) {
            if (price == AtmConfig.sliverValue) {
                return AtmConfig.button1Tax;
            } else if (price == AtmConfig.goldValue) {
                return AtmConfig.button2Tax;
            } else if (price == AtmConfig.goldValue * 10) {
                return AtmConfig.button3Tax;
            }
        }
        return 0;
    }

    private void handleItemInsertion(IItemHandler stackHandler, ItemStack stackToAdd, Player player) {
        boolean inserted = false;

        // 首先尝试找到相同类型的物品堆叠
        for (int i = 0; i < stackHandler.getSlots(); i++) {
            ItemStack existingStack = stackHandler.getStackInSlot(i);
            if (ItemStack.isSameItemSameTags(existingStack, stackToAdd)) {
                ItemStack remaining = stackHandler.insertItem(i, stackToAdd, false);
                if (remaining.isEmpty()) {
                    inserted = true;
                    break;
                }
                stackToAdd = remaining;
            }
        }

        // 如果没有找到相同物品或无法完全插入，寻找空槽位
        if (!inserted) {
            for (int i = 0; i < stackHandler.getSlots(); i++) {
                if (stackHandler.getStackInSlot(i).isEmpty()) {
                    stackHandler.insertItem(i, stackToAdd, false);
                    inserted = true;
                    break;
                }
            }
        }

        // 如果仍然无法插入，给予玩家
        if (!inserted) {
            ItemHandlerHelper.giveItemToPlayer(player, stackToAdd);
        }
    }
}
