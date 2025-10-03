package com.TBK.beyondtheend.server.entity.phase;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public interface FallenDragonInstance {
    boolean isSitting();

    void doClientTick();

    void doServerTick();

    void onCrystalDestroyed(EndCrystal p_31315_, BlockPos p_31316_, DamageSource p_31317_, @Nullable Player p_31318_);

    void begin();

    void end();

    float getFlySpeed();

    float getTurnSpeed();

    FallenDragonPhase<? extends FallenDragonInstance> getPhase();

    @Nullable
    Vec3 getFlyTargetLocation();

    float onHurt(DamageSource p_31313_, float p_31314_);

}
