package com.TBK.beyondtheend.server.world;

import com.TBK.beyondtheend.BeyondTheEnd;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.ForgeRegistries;

public class BKBiomeModifiers {
    public static final ResourceKey<BiomeModifier> ADD_SPIKE = registerKey("add_spike");


    private static ResourceKey<BiomeModifier> registerKey(String name) {
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, new ResourceLocation(BeyondTheEnd.MODID, name));
    }

}
