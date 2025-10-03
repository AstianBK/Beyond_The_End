package com.TBK.beyondtheend.common.registry;

import com.TBK.beyondtheend.BeyondTheEnd;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;


public class BkDimension{
    public final static ResourceLocation BEYOND_END_ID = new ResourceLocation(BeyondTheEnd.MODID,"the_new_end");

    public static final ResourceKey<Level> BEYOND_END_LEVEL = ResourceKey.create(Registry.DIMENSION_REGISTRY, BEYOND_END_ID);
    public static final ResourceKey<DimensionType> BEYOND_END_TYPE = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, BEYOND_END_ID);
    public static final int BEYOND_END_TICKS_PER_DAY = (24000) * 3; // too scared to call Level.TICKS_PER_DAY because of static init problems, but just know this is Level.TICKS_PER_DAY * 3
    public static final ResourceKey<LevelStem> BEYOND_END_LEVEL_STEM = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, BEYOND_END_ID);


}
