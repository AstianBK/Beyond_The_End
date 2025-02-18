package com.TBK.beyond_the_end.client.sounds;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.common.Util;
import com.TBK.beyond_the_end.common.registry.BTESounds;
import com.TBK.beyond_the_end.server.entity.JellyfishEntity;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LaserSoundInstance extends EntityBoundSoundInstance {
    public Vec3 end;
    public Vec3 start;
    private final Entity entity;
    private final JellyfishEntity sourceLaser;

    public LaserSoundInstance(Player player, JellyfishEntity jellyfish) {
        super(BTESounds.JELLYFISH_LASERLOOP.get(),SoundSource.HOSTILE, 10.0F, 1.0F, player, 100L);
        this.end = jellyfish.directionBlock;
        this.start = jellyfish.position();
        this.entity=player;
        this.sourceLaser = jellyfish;
    }

    @Override
    public boolean canPlaySound() {
        return super.canPlaySound();
    }

    public float getDistanceFinal(){
        Vec3 positionEntity = this.entity.position();
        Vec3 initial = this.end;
        Vec3 direction = this.start.subtract(this.end).normalize();
        float distance = (float) this.end.distanceTo(positionEntity);
        while (initial.subtract(this.start).length()>1.0F){
            if(distance<20){
                return (float) Mth.clamp((20.0D-distance)/20.0D,0.0F,1.0F);
            }
            initial=initial.add(direction);
            distance = (float) initial.distanceTo(positionEntity);
        }
        return 0.0F;
    }
    @Override
    public void tick() {
        BeyondTheEnd.LOGGER.debug("SoundFuncionando  volumenn actual :"+this.volume);
        if(this.sourceLaser.lazerTimer<=0){
            this.stop();
        }
        this.volume=10.0F * getDistanceFinal();
        super.tick();
    }
}
