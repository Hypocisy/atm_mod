package com.kumoe.atm.client;

import com.kumoe.atm.AtmMod;
import com.kumoe.atm.config.AtmConfig;
import com.kumoe.atm.network.packet.PlayerBalancePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class VaultGui implements IGuiOverlay {
    public static final VaultGui INSTANCE = new VaultGui();
    private static final ResourceLocation COPPER = new ResourceLocation(AtmMod.MODID, "textures/gui/icon.png");

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (AtmConfig.showHud && gui.getMinecraft().screen == null) {
            var x = screenWidth - 16;
            var y = 16;
            var player = Objects.requireNonNull(gui.getMinecraft().player);
            var currently = BigDecimal.valueOf(PlayerBalancePacket.getBalance(player.getUUID()).orElse(0d)).setScale(2, RoundingMode.HALF_UP).doubleValue();
            var message = Component.translatable("gui.atm_mod.currently_money", currently);
            var messageLength = gui.getFont().width(message) + 2;
            guiGraphics.blit(COPPER, x, y, 0, 0, 0, 8, 8, 8, 8);
            guiGraphics.drawString(gui.getFont(), message, x - messageLength, 16+AtmConfig.textOffset, 0xffffff, false);
        }
    }
}
