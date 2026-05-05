package com.TBK.beyondtheend.client;

import com.TBK.beyondtheend.BeyondTheEnd;
import com.TBK.beyondtheend.client.sounds.BossMusicSoundInstance;
import com.TBK.beyondtheend.common.api.ICamShaker;
import com.TBK.beyondtheend.common.registry.BTESounds;
import com.TBK.beyondtheend.server.entity.FallenDragonEntity;
import com.TBK.beyondtheend.server.entity.JellyfishEntity;
import com.TBK.beyondtheend.server.world.biome.BKBiome;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class ClientEvents {

    private BossMusicSoundInstance dragonMusic   = null;
    private BossMusicSoundInstance jellyfishMusic = null;

    private boolean dragonAliveNearby(Minecraft mc) {
        if (mc.level == null || mc.player == null) return false;
        List<FallenDragonEntity> list = mc.level.getEntitiesOfClass(
                FallenDragonEntity.class, mc.player.getBoundingBox().inflate(512));
        return list.stream().anyMatch(e -> e.isAlive() && !e.isDeadOrDying());
    }

    private boolean jellyfishAliveNearby(Minecraft mc) {
        if (mc.level == null || mc.player == null) return false;
        List<JellyfishEntity> list = mc.level.getEntitiesOfClass(
                JellyfishEntity.class, mc.player.getBoundingBox().inflate(512));
        return list.stream().anyMatch(e -> e.isAlive() && !e.isDeadOrDying());
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) { stopAllMusic(mc); return; }

        if (dragonAliveNearby(mc)) {
            if (dragonMusic == null || dragonMusic.isStopped()) {
                dragonMusic = new BossMusicSoundInstance(BTESounds.FALLEN_DRAGON_MUSIC.get());
                mc.getSoundManager().play(dragonMusic);
            }
        } else if (dragonMusic != null && !dragonMusic.isStopped()) {
            dragonMusic.stopMusic(); mc.getSoundManager().stop(dragonMusic); dragonMusic = null;
        }

        if (jellyfishAliveNearby(mc)) {
            if (jellyfishMusic == null || jellyfishMusic.isStopped()) {
                jellyfishMusic = new BossMusicSoundInstance(BTESounds.JELLYFISH_MUSIC.get());
                mc.getSoundManager().play(jellyfishMusic);
            }
        } else if (jellyfishMusic != null && !jellyfishMusic.isStopped()) {
            jellyfishMusic.stopMusic(); mc.getSoundManager().stop(jellyfishMusic); jellyfishMusic = null;
        }
    }

    private void stopAllMusic(Minecraft mc) {
        if (dragonMusic != null && !dragonMusic.isStopped()) {
            dragonMusic.stopMusic(); mc.getSoundManager().stop(dragonMusic); dragonMusic = null;
        }
        if (jellyfishMusic != null && !jellyfishMusic.isStopped()) {
            jellyfishMusic.stopMusic(); mc.getSoundManager().stop(jellyfishMusic); jellyfishMusic = null;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void fogRender(ViewportEvent.RenderFog event) {
        if (event.isCanceled()) return;
        Entity player = Minecraft.getInstance().getCameraEntity();
        if (player != null && player.level.getBiome(player.getOnPos()).is(BKBiome.BOSS_FIGHT)) { }
    }

    @SubscribeEvent
    public void computeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Entity player = Minecraft.getInstance().getCameraEntity();
        float partialTick = Minecraft.getInstance().getPartialTick();
        float amount = 0F;
        if (player != null) {
            AABB aabb = player.getBoundingBox().inflate(64);
            for (Mob m : Minecraft.getInstance().level.getEntitiesOfClass(Mob.class, aabb,
                    mob -> mob instanceof ICamShaker s && s.getScreenShakeAmount(partialTick) > 0F)) {
                ICamShaker s = (ICamShaker) m;
                if (player.isOnGround() && s.canShake(player)) {
                    double d = s.getShakeDistanceForEntity(player);
                    amount = Math.min((1F-(float)Math.min(1,d/s.getShakeDistance()))*Math.max(s.getScreenShakeAmount(partialTick),0F),2F);
                }
            }
            if (amount == 0 && BeyondTheEnd.jellyfishFightEvent != null && BeyondTheEnd.jellyfishFightEvent.screenShakeAmount > 0F)
                amount = Math.min((1F-Math.max(BeyondTheEnd.jellyfishFightEvent.getScreenShakeAmount(partialTick),0F)),2F);

            if (amount > 0) {
                if (ClientProxy.lastTick != player.tickCount) {
                    RandomSource rng = player.level.random;
                    ClientProxy.randomOffsets[0] = rng.nextFloat();
                    ClientProxy.randomOffsets[1] = rng.nextFloat();
                    ClientProxy.randomOffsets[2] = rng.nextFloat();
                    ClientProxy.lastTick = player.tickCount;
                }
                double intensity = amount * Minecraft.getInstance().options.screenEffectScale().get();
                event.getCamera().move(ClientProxy.randomOffsets[0]*0.8F*intensity, 0, ClientProxy.randomOffsets[2]*0.8F*intensity);
            }
        }
    }
}
