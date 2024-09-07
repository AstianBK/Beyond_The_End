package com.TBK.beyond_the_end.server.network;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.server.world.featured.SpikeBossFeatured;
import com.TBK.beyond_the_end.server.world.featured.SpikesBossConfiguration;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;

public class ConfiguresFeatures {
    public static ResourceKey<Feature<?>> SPIKE=registerKey("spike_boss");

    public static ResourceKey<Feature<?>> registerKey(String name) {
        return ResourceKey.create(Registry.FEATURE_REGISTRY, new ResourceLocation(BeyondTheEnd.MODID, name));
    }


}
