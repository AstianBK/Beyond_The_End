package com.TBK.beyondtheend.server;

import com.TBK.beyondtheend.common.registry.BKEntityType;
import com.TBK.beyondtheend.server.entity.FallenDragonEntity;
import com.TBK.beyondtheend.server.entity.JellyfishEntity;
import com.TBK.beyondtheend.server.entity.JellyfishMinionEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class AttributeEvent {
    @SubscribeEvent
    public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
        event.put(BKEntityType.JELLYFISH.get(), JellyfishEntity.createAttributes().build());
        event.put(BKEntityType.JELLYFISH_MINION.get(), JellyfishMinionEntity.createAttributes().build());
        event.put(BKEntityType.FALLEN_DRAGON.get(), FallenDragonEntity.createAttributes().build());
        event.put(BKEntityType.FALLEN_DRAGON_FAKE.get(), Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED,0.25D)
                .add(Attributes.KNOCKBACK_RESISTANCE,1.0F).build());
    }

}