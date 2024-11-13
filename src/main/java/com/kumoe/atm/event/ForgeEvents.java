package com.kumoe.atm.event;

import com.kumoe.atm.AtmMod;
import com.kumoe.atm.item.Coin;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
}
