package com.kumoe.atm.client;

import com.kumoe.atm.AtmMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = AtmMod.MODID)
public class ClientEventHandler {

    @SubscribeEvent
    public static void registerGuiOverlayEvent(final RegisterGuiOverlaysEvent evt) {
        evt.registerBelowAll(AtmMod.MODID + "_vault_overlay", VaultGui.INSTANCE);
    }
}
