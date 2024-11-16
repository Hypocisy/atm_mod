package com.kumoe.atm.block;

import com.kumoe.atm.item.Coin;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class CustomSlot extends SlotItemHandler {
    public CustomSlot(IItemHandler itemHandler, int pSlot, int pX, int pY) {
        super(itemHandler, pSlot, pX, pY);
    }

    @Override
    public boolean mayPlace(ItemStack pStack) {
        return pStack.getItem() instanceof Coin && super.mayPlace(pStack);
    }
}
