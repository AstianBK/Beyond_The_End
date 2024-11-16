package com.TBK.beyond_the_end.common.registry;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.server.entity.FallenDragonEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BKEntityType {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, BeyondTheEnd.MODID);

    public static final RegistryObject<EntityType<FallenDragonEntity>> FALLEN_DRAGON =
            ENTITY_TYPES.register("fallen_dragon",
                    () -> EntityType.Builder.of(FallenDragonEntity::new, MobCategory.MONSTER)
                            .fireImmune().sized(4.0F, 8.0F)
                            .clientTrackingRange(10)
                            .build(new ResourceLocation(BeyondTheEnd.MODID, "fallen_dragon").toString()));

}
