package com.TBK.beyond_the_end.server.world.biome;

import com.TBK.beyond_the_end.BeyondTheEnd;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.*;

public class BKBiome {
    public static final ResourceKey<Biome> BOSS_FIGHT = ResourceKey.create(Registry.BIOME_REGISTRY,
            new ResourceLocation(BeyondTheEnd.MODID, "boss_fight"));
}