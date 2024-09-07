package com.TBK.beyond_the_end.server.world;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.server.world.featured.SpikeBossFeatured;
import com.TBK.beyond_the_end.server.world.featured.SpikesBossConfiguration;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class BKConfigFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> SPIKE_CONG = registerKey("spike");
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, BeyondTheEnd.MODID);

    public static final RegistryObject<Feature<SpikesBossConfiguration>> SPIKE = FEATURES.register("spike_boss", () -> new SpikeBossFeatured(SpikesBossConfiguration.CODEC));

    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registry.CONFIGURED_FEATURE_REGISTRY, new ResourceLocation(BeyondTheEnd.MODID, name));
    }
}
