package com.TBK.beyond_the_end.server.entity.projectile;

import com.TBK.beyond_the_end.common.registry.BKEntityType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ChargeFlash extends NormalProjectile{
    public int tickDiscard = 0;
    public ChargeFlash(EntityType<? extends ThrowableProjectile> p_37466_, Level p_37467_) {
        super(p_37466_, p_37467_);
    }
    public ChargeFlash(Level level,LivingEntity living){
        this(BKEntityType.CHARGE_FLASH.get(),level);
        this.setOwner(living);
    }

    @Override
    protected boolean canHitEntity(Entity p_37250_) {
        return super.canHitEntity(p_37250_);
    }

    @Override
    public void tick() {
        super.tick();
        if(this.tickDiscard++>300){
            this.discard();
        }
    }



    @Override
    protected void onHit(HitResult p_37260_) {
        super.onHit(p_37260_);
        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult p_37259_) {
        if(p_37259_.getEntity() instanceof LivingEntity living ){
            living.hurt(DamageSource.LIGHTNING_BOLT,16.0F);
            if(this.level.isClientSide){
                living.level.playLocalSound(living.getX(),living.getY(),living.getZ(), SoundEvents.THORNS_HIT, SoundSource.HOSTILE,3.0F,1.0F,false);
            }
        }
    }
}
