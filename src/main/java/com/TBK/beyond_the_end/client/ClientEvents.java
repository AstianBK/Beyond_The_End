package com.TBK.beyond_the_end.client;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.common.api.ICamShaker;
import com.TBK.beyond_the_end.server.world.biome.BKBiome;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
                //event.setCanceled(true);
                //event.setFarPlaneDistance(end * farness);
                //event.setNearPlaneDistance(defaultNearPlaneDistance * nearness);
            }
        }
    }

    @SubscribeEvent
    public void computeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Entity player = Minecraft.getInstance().getCameraEntity();
        float partialTick = Minecraft.getInstance().getPartialTick();
        float tremorAmount =  0F;
        if (player != null) {
            double shakeDistanceScale = 64;
            double distance = Double.MAX_VALUE;
            if (tremorAmount == 0) {
                AABB aabb = player.getBoundingBox().inflate(shakeDistanceScale);
                for (Mob screenShaker : Minecraft.getInstance().level.getEntitiesOfClass(Mob.class, aabb, (mob -> mob instanceof ICamShaker forge && forge.getScreenShakeAmount(partialTick)>0.0F))) {
                    ICamShaker shakesScreen = (ICamShaker) screenShaker;
                    if (player.isOnGround() && ((ICamShaker) screenShaker).canShake(player)) {
                        distance = ((ICamShaker) screenShaker).getShakeDistanceForEntity(player);
                        tremorAmount = Math.min((1F - (float) Math.min(1, distance / shakesScreen.getShakeDistance())) * Math.max(shakesScreen.getScreenShakeAmount(partialTick), 0F), 2.0F);

                    }
                }
                if(tremorAmount==0){
                    if(BeyondTheEnd.jellyfishFightEvent!=null && BeyondTheEnd.jellyfishFightEvent.screenShakeAmount>0.0F){
                        tremorAmount = Math.min((1F - Math.max(BeyondTheEnd.jellyfishFightEvent.getScreenShakeAmount(partialTick), 0F)), 2.0F);

                    }
                }
            }
            if (tremorAmount > 0) {
                if (ClientProxy.lastTremorTick != player.tickCount) {
                    RandomSource rng = player.level.random;
                    ClientProxy.randomTremorOffsets[0] = rng.nextFloat();
                    ClientProxy.randomTremorOffsets[1] = rng.nextFloat();
                    ClientProxy.randomTremorOffsets[2] = rng.nextFloat();
                    ClientProxy.lastTremorTick = player.tickCount;
                }
                double intensity = tremorAmount * Minecraft.getInstance().options.screenEffectScale().get();
                event.getCamera().move(ClientProxy.randomTremorOffsets[0] * 0.8F * intensity, ClientProxy.randomTremorOffsets[1] * 0.0F, ClientProxy.randomTremorOffsets[2] * 08F * intensity);
            }
        }
    }


}
