package com.TBK.beyondtheend.server.entity.phase;

import com.TBK.beyondtheend.server.entity.FallenDragonEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class RespawnPhase extends AbstractDragonPhaseInstance {
    @Nullable
    private Vec3 targetLocation;
    private int time;

    public RespawnPhase(FallenDragonEntity p_31217_) {
        super(p_31217_);
    }

    public void doClientTick() {
        if(this.time++>=200){
            this.dragon.phaseManager.setPhase(FallenDragonPhase.HOLDING_PATTERN);
        }

    }

    public float porcentTimer(){
        return ((float) this.time)/200.0F;
    }

    public void doServerTick() {
        ++this.time;
        if(this.time<=200){
            this.dragon.setHealth(this.dragon.getMaxHealth()*this.porcentTimer());
        }else {
            this.dragon.phaseManager.setPhase(FallenDragonPhase.HOLDING_PATTERN);
        }

    }

    public void begin() {
        this.targetLocation = null;
        this.time = 0;
    }

    public float getFlySpeed() {
        return 3.0F;
    }

    @Nullable
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    @Override
    public FallenDragonPhase<? extends FallenDragonInstance> getPhase() {
        return FallenDragonPhase.RESPAWN;
    }
}
