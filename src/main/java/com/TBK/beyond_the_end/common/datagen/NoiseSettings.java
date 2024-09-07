package com.TBK.beyond_the_end.common.datagen;

import com.TBK.beyond_the_end.BeyondTheEnd;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public class NoiseSettings {
    public static final ResourceKey<NoiseGeneratorSettings> SKYLANDS = createKey("boss_room");

    private static ResourceKey<NoiseGeneratorSettings> createKey(String name) {
        return ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation(BeyondTheEnd.MODID, name));
    }
}
