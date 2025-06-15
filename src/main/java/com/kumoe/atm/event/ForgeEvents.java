package com.kumoe.atm.event;

import com.kumoe.atm.AtmMod;
import com.kumoe.atm.block.AtmBlock;
import com.kumoe.atm.block.AtmBlockEntity;
import com.kumoe.atm.block.AtmPart;
import com.kumoe.atm.config.AtmConfig;
import com.kumoe.atm.item.Coin;
import com.kumoe.atm.network.NetworkHandler;
import com.kumoe.atm.network.packet.PlayerBalancePacket;
import com.kumoe.atm.uitls.PluginUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mod.EventBusSubscriber(modid = AtmMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {
	public static boolean hasBukkit = false;

	@SubscribeEvent
	public static void onPlayerJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
		var player = event.getEntity();
		var level = player.level();

		if (!level.isClientSide()) {

			if (PluginUtils.checkBukkitInstalled()) {
				double balance = PluginUtils.getBalance(player);
				hasBukkit = true;
				NetworkHandler.sendToPlayer(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new PlayerBalancePacket(player.getUUID(), balance));
			}
		}
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (hasBukkit) {
			var tickCount = event.getServer().getTickCount();
			var players = event.getServer().getPlayerList().getPlayers();
			var isServer = event.side.isServer();
			if (isServer && tickCount % (20 * AtmConfig.updateInterval) == 0) {
				players.forEach(serverPlayer -> {
					if (PluginUtils.checkBukkitInstalled()) {
						double cmiBalance = PluginUtils.getBalance(serverPlayer);
						double cachedBalance = PlayerBalancePacket.getBalance(serverPlayer).orElse(-1d);

						// if balance different or no cached balance, update balance to client
						if (cmiBalance != cachedBalance || cachedBalance < 0) {
							PlayerBalancePacket.updateServerCache(serverPlayer, cmiBalance);
							AtmMod.LOGGER.debug("Balance: {} ;\nPre Cached Balance: {};\n Currently cached balance {}", cmiBalance, cachedBalance, PlayerBalancePacket.getBalance(serverPlayer).orElse(cmiBalance));
							AtmMod.LOGGER.debug("Sending update client money packet to {}", serverPlayer.getName().getString());
							NetworkHandler.sendToPlayer(
									PacketDistributor.PLAYER.with(() -> serverPlayer),
									new PlayerBalancePacket(serverPlayer, cmiBalance)
							);
						}
					}
				});
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerBreak(BlockEvent.BreakEvent event) {
		var player = event.getPlayer();
		if (player.isCreative() || player.hasPermissions(2)) return;
		var block = event.getState().getBlock();
		if (block instanceof AtmBlock) {
			// 获取blockState，如果是upper则代表可以正确获取block entity
			// 如果是lower则检查upper的blockentity是不是null或者是不是属于 atmblockentity
			// 再根据owner结果判断是否取消玩家的挖掘操作
			var isUpper = event.getState().getValue(AtmBlock.PART).equals(AtmPart.UPPER);
			var blockEntity = player.level().getBlockEntity(isUpper ? event.getPos() : event.getPos().above());
			if (blockEntity instanceof AtmBlockEntity atmEntity) {
				if (player.getUUID() != atmEntity.getOwnerUuid()) {
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void onTooltip(final ItemTooltipEvent event) {
		var player = event.getEntity();
		if (player != null) {
			if (event.getItemStack().getItem() instanceof Coin coin) {
				if (event.getItemStack().getCount() > 1) {
					event.getToolTip().add(Component.translatable("tooltip.atm_mod.total_value").append("" + BigDecimal.valueOf(coin.getAmount() * event.getItemStack().getCount()).setScale(2, RoundingMode.HALF_UP).doubleValue()));
				}
				event.getToolTip().add(Component.translatable("tooltip.atm_mod.value").append("" + BigDecimal.valueOf(coin.getAmount()).setScale(2, RoundingMode.HALF_UP).doubleValue()));
			}
		}
	}
}
