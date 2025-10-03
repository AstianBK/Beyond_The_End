package com.TBK.beyondtheend.server.capabilities;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;


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
}
