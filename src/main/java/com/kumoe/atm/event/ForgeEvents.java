package com.kumoe.atm.event;

import com.kumoe.atm.AtmMod;
import com.kumoe.atm.item.Coin;
import com.kumoe.atm.network.NetworkHandler;
import com.kumoe.atm.network.packet.QueryPlayerBalance;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mod.EventBusSubscriber(modid = AtmMod.MODID)
public class ForgeEvents {

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

    @SubscribeEvent
    public static void onPlayerJoin(final PlayerEvent.PlayerLoggedInEvent event) {
        var packet = new QueryPlayerBalance(event.getEntity().getUUID(), 0, false);
        NetworkHandler.getInstance().send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getEntity()), packet);
    }
}
