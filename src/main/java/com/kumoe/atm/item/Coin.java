package com.kumoe.atm.item;

import com.kumoe.atm.config.AtmConfig;
import com.kumoe.atm.registry.AtmRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemHandlerHelper;

public class Coin extends Item {
    private final CoinType type;

    public Coin(Properties pProperties, CoinType type) {
        super(pProperties.setNoRepair());
        this.type = type;
    }

    /**
     * Returns the coin type based on current coin type and shift status.
     * If not holding shift, it returns the higher denomination, otherwise the lower.
     */
    public static Coin getCoinByType(CoinType type, boolean isShiftKeyDown) {
        return switch (type) {
            case COPPER -> AtmRegistries.SILVER.get();
            case SILVER -> isShiftKeyDown ? AtmRegistries.COPPER.get() : AtmRegistries.GOLD.get();
            case GOLD -> AtmRegistries.SILVER.get();
        };
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (AtmConfig.enableSwap) {
            ItemStack stackInHand = pPlayer.getItemInHand(pUsedHand);
            int count = stackInHand.getCount();
            boolean isShiftKeyDown = pPlayer.isShiftKeyDown();

            // Define conversion logic based on whether the player is holding shift
            if (!isShiftKeyDown && count >= 10) {
                // Convert to higher denomination (e.g., 10 copper -> 1 silver)
                stackInHand.shrink(10);
                ItemHandlerHelper.giveItemToPlayer(pPlayer, new ItemStack(getCoinByType(type, false), 1));
            } else if (isShiftKeyDown && getType() != CoinType.COPPER) {
                // Convert to lower denomination (e.g., 1 gold -> 10 silver)
                stackInHand.shrink(1);
                ItemHandlerHelper.giveItemToPlayer(pPlayer, new ItemStack(getCoinByType(type, true), 10));
            }
        }

        return InteractionResultHolder.sidedSuccess(pPlayer.getItemInHand(pUsedHand), pLevel.isClientSide());
    }

    public CoinType getType() {
        return type;
    }

    public double getAmount() {
        return switch (getType()) {
            case GOLD -> AtmConfig.goldValue;
            case SILVER -> AtmConfig.sliverValue;
            default -> AtmConfig.copperValue;
        };
    }

}
