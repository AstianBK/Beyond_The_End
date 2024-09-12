package com.TBK.beyond_the_end.common.registry;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.common.blocks.GlowingEnergyBlock;
import com.TBK.beyond_the_end.common.blocks.PortalBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.checkerframework.checker.units.qual.C;

import java.util.function.Supplier;
import java.util.function.ToIntFunction;

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
