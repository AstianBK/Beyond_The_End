package com.TBK.beyond_the_end.server.entity.phase;

import com.TBK.beyond_the_end.server.entity.FallenDragonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public abstract class AbstractDragonPhaseInstance implements FallenDragonInstance{
    protected final FallenDragonEntity dragon;

    public AbstractDragonPhaseInstance(FallenDragonEntity p_31178_) {
        this.dragon = p_31178_;
    }

    public boolean isSitting() {
        return false;
    }

    public void doClientTick() {
    }

    public void doServerTick() {
    }

    public void onCrystalDestroyed(EndCrystal p_31184_, BlockPos p_31185_, DamageSource p_31186_, @Nullable Player p_31187_) {
    }

    public void begin() {
    }

    public void end() {
    }

    public float getFlySpeed() {
        return 0.6F;
    }

    @Nullable
    public Vec3 getFlyTargetLocation() {
        return null;
    }

    public float onHurt(DamageSource p_31181_, float p_31182_) {
        return p_31182_;
    }

    public float getTurnSpeed() {
        float f = (float)this.dragon.getDeltaMovement().horizontalDistance() + 1.0F;
        float f1 = Math.min(f, 40.0F);
        return 0.7F / f1 / f;
    }
}
