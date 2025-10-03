package com.TBK.beyondtheend.common.registry;

import com.TBK.beyondtheend.BeyondTheEnd;
import com.TBK.beyondtheend.server.entity.FallenDragonEntity;
import com.TBK.beyondtheend.server.entity.FallenDragonFakeEntity;
import com.TBK.beyondtheend.server.entity.JellyfishEntity;
import com.TBK.beyondtheend.server.entity.JellyfishMinionEntity;
import com.TBK.beyondtheend.server.entity.projectile.ChargeFlash;
import com.TBK.beyondtheend.server.entity.projectile.ChargeFollowing;
import com.TBK.beyondtheend.server.entity.projectile.FallenDragonFireball;
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
                            .fireImmune().sized(1.0F, 1.0F)
                            .clientTrackingRange(30)
                            .build(new ResourceLocation(BeyondTheEnd.MODID, "fallen_dragon").toString()));
    public static final RegistryObject<EntityType<FallenDragonFakeEntity>> FALLEN_DRAGON_FAKE =
            ENTITY_TYPES.register("fallen_dragon_fake",
                    () -> EntityType.Builder.of(FallenDragonFakeEntity::new, MobCategory.MONSTER)
                            .fireImmune().sized(1.0F, 1.0F)
                            .clientTrackingRange(30)
                            .build(new ResourceLocation(BeyondTheEnd.MODID, "fallen_dragon_fake").toString()));

    public static final RegistryObject<EntityType<JellyfishEntity>> JELLYFISH =
            ENTITY_TYPES.register("jellyfish",
                    () -> EntityType.Builder.of(JellyfishEntity::new, MobCategory.MONSTER)
                            .fireImmune().sized(16.0F, 12.0F)
                            .clientTrackingRange(100)
                            .build(new ResourceLocation(BeyondTheEnd.MODID, "jellyfish").toString()));


    public static final RegistryObject<EntityType<JellyfishMinionEntity>> JELLYFISH_MINION =
            ENTITY_TYPES.register("jellyfish_minion",
                    () -> EntityType.Builder.of(JellyfishMinionEntity::new, MobCategory.MONSTER)
                            .fireImmune().sized(2.0F, 2.0F)
                            .build(new ResourceLocation(BeyondTheEnd.MODID, "jellyfish_minion").toString()));

    public static final RegistryObject<EntityType<ChargeFollowing>> CHARGE_FOLLOWING = ENTITY_TYPES
            .register("charge_following", () -> EntityType.Builder.<ChargeFollowing>of(ChargeFollowing::new, MobCategory.MISC)
                    .fireImmune().sized(1F, 1F).build(BeyondTheEnd.MODID+ "charge_following"));

    public static final RegistryObject<EntityType<FallenDragonFireball>> FALLEN_DRAGON_FIREBALL = ENTITY_TYPES
            .register("fallen_dragon_fireball", () -> EntityType.Builder.<FallenDragonFireball>of(FallenDragonFireball::new, MobCategory.MISC)
                    .fireImmune().sized(1F, 1F).build(BeyondTheEnd.MODID+ "fallen_dragon_fireball"));

    public static final RegistryObject<EntityType<ChargeFlash>> CHARGE_FLASH = ENTITY_TYPES
            .register("charge_flash", () -> EntityType.Builder.<ChargeFlash>of(ChargeFlash::new, MobCategory.MISC)
                    .fireImmune().sized(0.2F, 0.2F).build(BeyondTheEnd.MODID+ "charge_flash"));

}
