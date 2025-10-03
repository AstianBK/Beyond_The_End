package com.TBK.beyondtheend.common.registry;

import com.TBK.beyondtheend.BeyondTheEnd;
import com.TBK.beyondtheend.common.blocks.GlowingEnergyBlock;
import com.TBK.beyondtheend.common.blocks.PortalBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class BkCommonRegistry {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, BeyondTheEnd.MODID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, BeyondTheEnd.MODID);

    public static final RegistryObject<Block> PORTAL = registerBlock("portal",
            () -> new PortalBlock(BlockBehaviour.Properties.copy(Blocks.END_PORTAL))
    );
    public static final RegistryObject<Block> COBBLED_DIABASE = registerBlock("cobbled_diabase",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.END_STONE))
    );
    public static final RegistryObject<Block> COBBLED_TEPHRITE = registerBlock("cobbled_tephrite",
            () -> new  Block(BlockBehaviour.Properties.copy(Blocks.END_STONE))
    );
    public static final RegistryObject<Block> CRATERED_DIABASE = registerBlock("cratered_diabase",
            () -> new  Block(BlockBehaviour.Properties.copy(Blocks.END_STONE))
    );
    public static final RegistryObject<Block> SMOOTH_DIABASE = registerBlock("smooth_diabase",
            () -> new  Block(BlockBehaviour.Properties.copy(Blocks.END_STONE))
    );
    public static final RegistryObject<Block> SMOOTH_TEPHRITE = registerBlock("smooth_tephrite",
            () -> new  Block(BlockBehaviour.Properties.copy(Blocks.END_STONE))
    );
    public static final RegistryObject<Block> GLOWING_ENERGY_STREAK = registerBlock("glowing_energy_streak",
            () -> new GlowingEnergyBlock(BlockBehaviour.Properties.copy(Blocks.GLOWSTONE))
    );

    public static final RegistryObject<Item> FALLEN_DRAGON_SPAWN_EGG = ITEMS.register("fallen_dragon_spawn_egg",
            () -> new ForgeSpawnEggItem(BKEntityType.FALLEN_DRAGON,0xf7fafa, 0xc6e2f5,
                    new Item.Properties()));
    public static final RegistryObject<Block> GLOWING_ENERGY = registerBlock("glowing_energy",
            () -> new  Block(BlockBehaviour.Properties.copy(Blocks.GLOWSTONE))
    );

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block){
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends  Block> void registerBlockItem(String name, RegistryObject<T> block) {
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(BKCreativeModeTab.BK_TAB)));
    }



}
