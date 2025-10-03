package com.TBK.beyondtheend.common.registry;

import com.TBK.beyondtheend.BeyondTheEnd;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class BKTags {
    public static final TagKey<Biome> IS_DESERT= tag("is_boss_fight");

    private static TagKey<Biome> tag(String name)
    {
        return TagKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(BeyondTheEnd.MODID, name));
    }
}
