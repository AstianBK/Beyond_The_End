package com.TBK.beyond_the_end.server;

import com.TBK.beyond_the_end.common.registry.BKEntityType;
import com.TBK.beyond_the_end.server.entity.FallenDragonEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class AttributeEvent {
    @SubscribeEvent
    public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
        event.put(BKEntityType.FALLEN_DRAGON.get(), FallenDragonEntity.createAttributes().build());
    }

}