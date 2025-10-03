package com.TBK.beyondtheend.server.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.network.NetworkHooks;

public class JellyfishPartEntity extends net.minecraftforge.entity.PartEntity<JellyfishEntity>{
    public static final EntityDataAccessor<Boolean> HITBOX_ACTIVE =  SynchedEntityData.defineId(JellyfishPartEntity.class, EntityDataSerializers.BOOLEAN);

    public final JellyfishEntity parentMob;
    public final String name;
    private EntityDimensions size;
    public final EntityDimensions size0;
    public JellyfishPartEntity(JellyfishEntity p_31014_, String p_31015_, float p_31016_, float p_31017_) {
        super(p_31014_);
        this.setSize(EntityDimensions.scalable(p_31016_, p_31017_));
        this.size0 = EntityDimensions.scalable(p_31016_, p_31017_);
        this.refreshDimensions();
        this.parentMob = p_31014_;
        this.name = p_31015_;
    }
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (accessor == HITBOX_ACTIVE) {
            this.setSize(this.getDimensions(Pose.STANDING));
            if (this.parentMob!=null && this.parentMob.actuallyPhase == JellyfishEntity.PhaseAttack.GROUND)
                this.activate();
            else
                this.deactivate();
        }
    }
    protected void setSize(EntityDimensions size) {
        this.size = size;
        this.refreshDimensions();
    }
    public void activate() {
        this.dimensions = this.size;
        this.refreshDimensions();
    }
    public void deactivate() {
        this.dimensions = EntityDimensions.scalable(0, 0);
        this.refreshDimensions();
    }
    protected void defineSynchedData() {
        this.entityData.define(HITBOX_ACTIVE,true);
    }

    protected void readAdditionalSaveData(CompoundTag p_31025_) {
    }

    protected void addAdditionalSaveData(CompoundTag p_31028_) {
    }

    public boolean isPickable() {
        return true;
    }


    public boolean hurt(DamageSource p_31020_, float p_31021_) {
        return !this.isInvulnerableTo(p_31020_) && this.parentMob.hurt(p_31020_, p_31021_);
    }

    public boolean is(Entity p_31031_) {
        return this == p_31031_ || this.parentMob == p_31031_;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public boolean shouldBeSaved() {
        return false;
    }
    public EntityDimensions getDimensions(Pose p_19975_) {
        return this.size;
    }
}
