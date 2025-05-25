package com.kumoe.atm.registry;

import com.kumoe.atm.AtmMod;
import com.kumoe.atm.block.AtmBlock;
import com.kumoe.atm.block.AtmBlockEntity;
import com.kumoe.atm.block.AtmMenu;
import com.kumoe.atm.item.Coin;
import com.kumoe.atm.item.CoinType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class AtmRegistries {

    public static final RegistryObject<Block> ATM_BLOCK;
    public static final RegistryObject<Coin> COPPER;
    public static final RegistryObject<Coin> SILVER;
    public static final RegistryObject<Coin> GOLD;
    public static final RegistryObject<BlockEntityType<AtmBlockEntity>> ATM_BLOCK_ENTITY;
    public static final RegistryObject<MenuType<AtmMenu>> ATM_MENU;
    public static final RegistryObject<CreativeModeTab> ATM_CREATIVE_MODE_TAB;
    private static final DeferredRegister<Block> BLOCK;
    private static final DeferredRegister<Item> ITEM;
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE;
    private static final DeferredRegister<MenuType<?>> MENU;
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB;

    static {
        BLOCK = DeferredRegister.create(Registries.BLOCK, AtmMod.MODID);
        ITEM = DeferredRegister.create(Registries.ITEM, AtmMod.MODID);
        MENU = DeferredRegister.create(Registries.MENU, AtmMod.MODID);
        CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AtmMod.MODID);
        ATM_BLOCK = registerBlockItems("atm_block", () -> new AtmBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
        COPPER = ITEM.register("copper_coin", () -> new Coin(new Item.Properties(), CoinType.COPPER));
        SILVER = ITEM.register("sliver_coin", () -> new Coin(new Item.Properties(), CoinType.SILVER));
        GOLD = ITEM.register("gold_coin", () -> new Coin(new Item.Properties(), CoinType.GOLD));
        BLOCK_ENTITY_TYPE = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AtmMod.MODID);
        ATM_BLOCK_ENTITY = BLOCK_ENTITY_TYPE.register("atm_block_entity", () -> BlockEntityType.Builder.of(AtmBlockEntity::new, ATM_BLOCK.get()).build(null));
        ATM_MENU = MENU.register("atm_menu", () -> IForgeMenuType.create(AtmMenu::factory));
        ATM_CREATIVE_MODE_TAB = CREATIVE_MODE_TAB.register("atm_mod", () -> CreativeModeTab.builder().icon(() -> new ItemStack(GOLD.get()))
                .title(Component.translatable("menu.atm_mod.title"))
                .displayItems((pParameters, pOutput) -> ITEM.getEntries().forEach(itemRegistryObject -> pOutput.accept(itemRegistryObject.get())))
                .noScrollBar()
                .build()
        );
    }

    public static <T extends Block> RegistryObject<T> registerBlockItems(String name, Supplier<T> block) {
        RegistryObject<T> blockItem = BLOCK.register(name, block);
        var registerBlockItem = registerBlockItem(name, blockItem);
        AtmMod.LOGGER.info("Registered block item: {}", registerBlockItem);
        return blockItem;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ITEM.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        ITEM.register(eventBus);
        BLOCK.register(eventBus);
        BLOCK_ENTITY_TYPE.register(eventBus);
        MENU.register(eventBus);
        CREATIVE_MODE_TAB.register(eventBus);
    }


}
