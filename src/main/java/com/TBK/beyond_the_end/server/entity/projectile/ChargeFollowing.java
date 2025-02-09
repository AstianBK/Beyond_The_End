package com.TBK.beyond_the_end.server.entity.projectile;

import com.TBK.beyond_the_end.common.registry.BKEntityType;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class ChargeFollowing extends NormalProjectile{
    @Nullable
    private Entity finalTarget;
    @Nullable
    private Direction currentMoveDirection;
    private int flightSteps;
    private double targetDeltaX=0.0D;
    private double targetDeltaY=0.0D;
    private double targetDeltaZ=0.0D;
    @Nullable
    private UUID targetId;

    public ChargeFollowing(EntityType<? extends ThrowableProjectile> p_37466_, Level p_37467_) {
        super(p_37466_, p_37467_);
    }

    public ChargeFollowing(Level level,LivingEntity owner, LivingEntity target){
        this(BKEntityType.CHARGE_FOLLOWING.get(),level);
        this.setOwner(owner);
        this.targetId=target.getUUID();
        this.setPos(owner.getEyePosition());
        this.finalTarget=target;
    }

    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    protected void addAdditionalSaveData(CompoundTag p_37357_) {
        super.addAdditionalSaveData(p_37357_);
        if (this.finalTarget != null) {
            p_37357_.putUUID("Target", this.finalTarget.getUUID());
        }

        if (this.currentMoveDirection != null) {
            p_37357_.putInt("Dir", this.currentMoveDirection.get3DDataValue());
        }

        p_37357_.putInt("Steps", this.flightSteps);
        p_37357_.putDouble("TXD", this.targetDeltaX);
        p_37357_.putDouble("TYD", this.targetDeltaY);
        p_37357_.putDouble("TZD", this.targetDeltaZ);
    }

    protected void readAdditionalSaveData(CompoundTag p_37353_) {
        super.readAdditionalSaveData(p_37353_);
        this.flightSteps = p_37353_.getInt("Steps");
        this.targetDeltaX = p_37353_.getDouble("TXD");
        this.targetDeltaY = p_37353_.getDouble("TYD");
        this.targetDeltaZ = p_37353_.getDouble("TZD");
        if (p_37353_.contains("Dir", 99)) {
            this.currentMoveDirection = Direction.from3DDataValue(p_37353_.getInt("Dir"));
        }

        if (p_37353_.hasUUID("Target")) {
            this.targetId = p_37353_.getUUID("Target");
        }

    }

    @Override
    protected boolean canHitEntity(Entity p_37250_) {
        return super.canHitEntity(p_37250_) && this.finalTarget!=null && p_37250_==this.finalTarget;
    }

    @Override
    public void tick() {
        super.tick();
        if(this.targetDeltaX==0.0D && this.targetDeltaY==0.0D && this.targetDeltaZ==0.0D){
            this.setDeltaMovement(this.position().subtract(this.targetDeltaX,this.targetDeltaY,this.targetDeltaZ).normalize().scale(0.25F));
        }
        if(this.targetId!=null){
            LivingEntity target = this.level.getPlayerByUUID(this.targetId);
            if(this.tickCount%10==0 && target!=null){
                this.targetDeltaX=target.getX();
                this.targetDeltaY=target.getY();
                this.targetDeltaZ=target.getZ();
            }
        }
    }

    @Override
    protected void onHit(HitResult p_37260_) {
        super.onHit(p_37260_);
        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult p_37259_) {
        if(p_37259_.getEntity() instanceof LivingEntity living){
            living.hurt(DamageSource.LIGHTNING_BOLT,9.0F);
        }
    }
}
