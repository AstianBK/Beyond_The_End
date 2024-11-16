package com.TBK.beyond_the_end.server.entity.phase;

import com.TBK.beyond_the_end.server.entity.FallenDragonEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class FallenDragonHoverPhase extends AbstractDragonPhaseInstance{
    @Nullable
    private Vec3 targetLocation;

    public FallenDragonHoverPhase(FallenDragonEntity p_31246_) {
        super(p_31246_);
    }

    public void doServerTick() {
        if (this.targetLocation == null) {
            this.targetLocation = this.dragon.position();
        }
    }

    public boolean isSitting() {
        return true;
    }

    public void begin() {
        this.targetLocation = null;
    }

    public float getFlySpeed() {
        return 1.0F;
    }

    @Nullable
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    public FallenDragonPhase<? extends FallenDragonInstance> getPhase() {
        return FallenDragonPhase.HOVERING;
    }

}
