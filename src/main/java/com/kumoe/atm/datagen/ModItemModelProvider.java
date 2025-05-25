package com.kumoe.atm.datagen;

import com.kumoe.atm.registry.AtmRegistries;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(AtmRegistries.COPPER.get());
        basicItem(AtmRegistries.GOLD.get());
        basicItem(AtmRegistries.SILVER.get());
    }
}
