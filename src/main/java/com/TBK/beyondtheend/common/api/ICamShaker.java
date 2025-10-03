package com.TBK.beyondtheend.common.api;

import net.minecraft.world.entity.Entity;

public interface ICamShaker {
    float getShakeDistance();
    float getScreenShakeAmount(float partialTicks);
    boolean canShake(Entity entity);
    double getShakeDistanceForEntity(Entity entity);
}
