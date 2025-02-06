package com.TBK.beyond_the_end.server.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public abstract class NormalProjectile extends ThrowableProjectile {

    protected NormalProjectile(EntityType<? extends ThrowableProjectile> p_37466_, Level p_37467_) {
        super(p_37466_, p_37467_);
    }

    @Override
    public void tick() {
        if (!this.level.isClientSide()) {
            HitResult result = ProjectileUtil.getHitResult(this, this::canHitEntity);
            if (result.getType() == HitResult.Type.MISS && this.isAlive()) {
                List<Entity> intersecting = this.level.getEntitiesOfClass(Entity.class, this.getBoundingBox(), this::canHitEntity);
                if (!intersecting.isEmpty())
                    this.onHit(new EntityHitResult(intersecting.get(0)));
            }
        }
        this.refreshDimensions();
        Vec3 vec3;
        vec3 = this.getDeltaMovement();
        double d5 = vec3.x;
        double d6 = vec3.y;
        double d1 = vec3.z;
        double d7 = this.getX() + d5;
        double d2 = this.getY() + d6;
        double d3 = this.getZ() + d1;
        double d4 = vec3.horizontalDistance();

        this.setXRot((float)(Mth.atan2(d6, d4) * (double)(180F / (float)Math.PI)));
        this.setXRot(lerpRotation(this.xRotO, this.getXRot()));
        this.setYRot(lerpRotation(this.yRotO, this.getYRot()));

        this.setDeltaMovement(vec3);

        this.setPos(d7, d2, d3);
        this.checkInsideBlocks();
    }

    @Override
    public void handleEntityEvent(byte p_19882_) {
        super.handleEntityEvent(p_19882_);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> p_20059_) {

        super.onSyncedDataUpdated(p_20059_);
    }


    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
    }

    @Override
    protected void defineSynchedData() {
    }
}
