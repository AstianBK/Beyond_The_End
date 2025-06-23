package com.TBK.beyond_the_end.server.entity.phase;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.server.entity.FallenDragonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class DiyingPhase extends AbstractDragonPhaseInstance {
    @Nullable
    private Vec3 targetLocation;
    private int time;

    public DiyingPhase(FallenDragonEntity p_31217_) {
        super(p_31217_);
    }

    public void doClientTick() {
        if (this.time++ % 10 == 0) {
            float f = (this.dragon.getRandom().nextFloat() - 0.5F) * 8.0F;
            float f1 = (this.dragon.getRandom().nextFloat() - 0.5F) * 4.0F;
            float f2 = (this.dragon.getRandom().nextFloat() - 0.5F) * 8.0F;
            this.dragon.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.dragon.getX() + (double)f, this.dragon.getY() + 2.0D + (double)f1, this.dragon.getZ() + (double)f2, 0.0D, 0.0D, 0.0D);
        }

    }

    public void doServerTick() {
        BeyondTheEnd.LOGGER.debug("Esta muerto tiempo muerto :"+this.time);
        ++this.time;
        if (this.targetLocation == null) {
            this.targetLocation = new Vec3(0,100,0);
        }

        double d0 = this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if (!(d0 < 100.0D) && !(d0 > 22500.0D) && !this.dragon.horizontalCollision && !this.dragon.verticalCollision) {
            this.dragon.setHealth(1.0F);
        } else {
            this.dragon.setHealth(0.0F);
        }

    }

    public void begin() {
        this.targetLocation = null;
        this.time = 0;
    }

    public float getFlySpeed() {
        return 0.2F;
    }

    @Nullable
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    @Override
    public FallenDragonPhase<? extends FallenDragonInstance> getPhase() {
        return FallenDragonPhase.DEATH;
    }
}
