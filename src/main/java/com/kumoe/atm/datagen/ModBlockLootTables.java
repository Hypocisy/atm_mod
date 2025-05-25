package com.kumoe.atm.datagen;

import com.kumoe.atm.block.AtmBlock;
import com.kumoe.atm.block.AtmPart;
import com.kumoe.atm.registry.AtmRegistries;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;
import java.util.Set;

public class ModBlockLootTables extends BlockLootSubProvider {
    private static final Set<Item> EXPLOSION_RESISTANT = Set.of();

    public ModBlockLootTables() {
        super(EXPLOSION_RESISTANT, FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        // ATM方块的掉落物生成
        this.add(AtmRegistries.ATM_BLOCK.get(),
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(LootItem.lootTableItem(AtmRegistries.ATM_BLOCK.get())
                                        .when(LootItemBlockStatePropertyCondition
                                                .hasBlockStateProperties(AtmRegistries.ATM_BLOCK.get())
                                                .setProperties(StatePropertiesPredicate.Builder.properties()
                                                        .hasProperty(AtmBlock.PART, AtmPart.LOWER))))));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        // 返回所有需要生成loot table的方块
        return List.of(AtmRegistries.ATM_BLOCK.get());
    }
}