package com.kumoe.atm.block;

import com.kumoe.atm.item.Coin;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CustomSlot extends Slot {
    public CustomSlot(Container pContainer, int pSlot, int pX, int pY) {
        super(pContainer, pSlot, pX, pY);
    }

    @Override
    public boolean mayPickup(Player pPlayer) {
        return super.mayPickup(pPlayer);
    }

    @Override
    public boolean mayPlace(ItemStack pStack) {
        return pStack.getItem() instanceof Coin;
    }
}
