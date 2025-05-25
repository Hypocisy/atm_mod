package com.kumoe.atm.compat;

import com.kumoe.atm.AtmMod;
import com.kumoe.atm.config.AtmConfig;
import com.kumoe.atm.item.Coin;
import net.blay09.mods.waystones.api.TeleportDestination;
import net.blay09.mods.waystones.api.WaystoneTeleportEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class WayStoneCompat {
    public static void onWayStoneTeleport(WaystoneTeleportEvent.Pre event) {
        if (!AtmConfig.enableWayStoneCompat) {
            return;
        }
        var teleportContext = event.getContext();

        var teleportEntity = teleportContext.getEntity();
        if (!(teleportEntity instanceof ServerPlayer serverPlayer)) {
            return;
        }
        var totalCost = destinationToCost(event.getDestination(), serverPlayer.position(), event.getContext().isDimensionalTeleport());
        AtmMod.LOGGER.info("Total cost: {}", totalCost);

        var inventory = serverPlayer.getInventory();
        // 先检查是否有足够数量的铜币
        for (var item : inventory.items) {
            if (item.getItem() instanceof Coin coin && coin.getType().equals(AtmConfig.consumeCoinType) && item.getCount() >= totalCost) {
                // 找到足够数量的铜币，消耗并允许传送
                item.setCount(item.getCount() - totalCost);
                serverPlayer.sendSystemMessage(Component.literal(
                        "消耗 %d 个 %s 进行传送，剩余 %d 个"
                                .formatted(totalCost, item.getDisplayName().getString(), item.getCount())
                ));
                AtmMod.LOGGER.info("玩家 {} 传送成功", serverPlayer.getName());
                return;
            }
        }

        // 如果没有找到足够的铜币，取消传送
        event.setCanceled(true);
        String itemName = new ItemStack(Coin.getCoinByType(AtmConfig.consumeCoinType)).getDisplayName().getString();
        serverPlayer.sendSystemMessage(Component.literal(
                "传送失败：需要 " + totalCost + " 个 " + itemName
        ));
        AtmMod.LOGGER.debug("玩家 {} 传送失败：{} 不足", serverPlayer.getName().getString(), itemName);
    }

    /**
     * Destination to coin cost
     *
     * @param destination         player's destination
     * @param location            player's location
     * @param dimensionalTeleport is player dimensional teleporting
     * @return the coin cost number
     */
    private static int destinationToCost(TeleportDestination destination, Vec3 location, boolean dimensionalTeleport) {
        // calculate distance
        int distance = (int) destination.getLocation().distanceTo(location);
        int distanceCost = Math.round((float) distance / AtmConfig.distanceToCost) * AtmConfig.perDistanceCost;
        if (distanceCost == 0) {
            distanceCost = AtmConfig.perDistanceCost;
        }
        return dimensionalTeleport ? AtmConfig.dimensionalTeleportCost + distanceCost : distanceCost;
    }
}
