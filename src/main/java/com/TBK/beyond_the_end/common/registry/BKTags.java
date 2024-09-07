package com.TBK.beyond_the_end.common.registry;

import com.TBK.beyond_the_end.BeyondTheEnd;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;

public class BKTags {
    public static final TagKey<Biome> IS_DESERT= tag("is_boss_fight");

    private static TagKey<Biome> tag(String name)
    {
        return TagKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(BeyondTheEnd.MODID, name));
    }
}
