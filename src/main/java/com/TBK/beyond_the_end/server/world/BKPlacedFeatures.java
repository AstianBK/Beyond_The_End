package com.TBK.beyond_the_end.server.world;

import com.TBK.beyond_the_end.BeyondTheEnd;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class BKPlacedFeatures {
    public static final ResourceKey<PlacedFeature> SPIKE_KEY = registerKey();


    private static ResourceKey<PlacedFeature> registerKey() {
        return ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, new ResourceLocation(BeyondTheEnd.MODID, "spike_placed"));
    }
}
