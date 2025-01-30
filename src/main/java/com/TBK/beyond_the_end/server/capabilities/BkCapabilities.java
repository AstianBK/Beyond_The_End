package com.TBK.beyond_the_end.server.capabilities;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.common.registry.BkDimension;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


public class BkCapabilities {
    public static final Capability<PortalPlayerCapability> PORTAL_PLAYER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() { });

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(PortalPlayer.class);
    }
    @SuppressWarnings("unchecked")
    public static <T extends PortalPlayerCapability> T getEntityPatch(Entity entity, Class<T> type) {
        if (entity != null) {
            PortalPlayerCapability entitypatch = entity.getCapability(BkCapabilities.PORTAL_PLAYER_CAPABILITY).orElse(null);

            if (entitypatch != null && type.isAssignableFrom(entitypatch.getClass())) {
                return (T)entitypatch;
            }
        }

        return null;
    }

    @Mod.EventBusSubscriber(modid = BeyondTheEnd.MODID)
    public static class Registration {

        @SubscribeEvent
        public static void attachWorldCapabilities(AttachCapabilitiesEvent<Level> event) {
            if (event.getObject().dimensionType().effectsLocation().equals(BkDimension.BEYOND_END_TYPE.location())) {
                //event.addCapability(new ResourceLocation(BeyondTheEnd.MODID, "aether_time"), new CapabilityProvider(AetherCapabilities.AETHER_TIME_CAPABILITY, new AetherTimeCapability(event.getObject())));
            }
        }
    }
}
