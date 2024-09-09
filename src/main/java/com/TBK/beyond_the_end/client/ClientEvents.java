package com.TBK.beyond_the_end.client;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.server.world.biome.BKBiome;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import javax.annotation.Nullable;

public class ClientEvents {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void fogRender(ViewportEvent.RenderFog event) {
        if (event.isCanceled()) {
            //another mod has cancelled fog rendering.
            return;
        }
        float end=RenderSystem.getShaderFogEnd();
        float defaultNearPlaneDistance = RenderSystem.getShaderFogStart();

        Entity player = Minecraft.getInstance().getCameraEntity();
        if (player.level.getBiome(player.getOnPos()).is(BKBiome.BOSS_FIGHT)) {
            float nearness = -0.2F;
            boolean flag = Math.abs(nearness) - 1.0F < 0.01F;
            float farness = 0.3F;

            if (flag) {
                event.setCanceled(true);
                event.setFarPlaneDistance(end * farness);
                event.setNearPlaneDistance(defaultNearPlaneDistance * nearness);
            }
        }
    }



}
