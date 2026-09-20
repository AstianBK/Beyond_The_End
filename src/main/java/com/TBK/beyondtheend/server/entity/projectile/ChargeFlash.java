package com.TBK.beyondtheend.server.entity.projectile;

import com.TBK.beyondtheend.common.registry.BKEntityType;
import com.TBK.beyondtheend.server.entity.JellyfishEntity;
import com.TBK.beyondtheend.server.entity.JellyfishMinionEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;

public class ChargeFlash extends NormalProjectile {
    private static final float DIRECT_DAMAGE = 16.0F;
    private static final float SPLASH_DAMAGE = 8.0F;
    private static final double SPLASH_RADIUS = 3.0D;

    public int tickDiscard = 0;

    public ChargeFlash(EntityType<? extends ThrowableProjectile> p_37466_, Level p_37467_) {
        super(p_37466_, p_37467_);
    }

    public ChargeFlash(Level level, LivingEntity living) {
        this(BKEntityType.CHARGE_FLASH.get(), level);
        this.setOwner(living);
    }

    @Override
    protected boolean canHitEntity(Entity p_37250_) {
        if (p_37250_ instanceof JellyfishMinionEntity || p_37250_ instanceof JellyfishEntity) {
            return false;
        }
        if (p_37250_ instanceof PartEntity<?> part && part.getParent() instanceof JellyfishEntity) {
            return false;
        }
        return super.canHitEntity(p_37250_);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickDiscard++ > 300) {
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
        if (p_37259_.getEntity() instanceof LivingEntity living) {
            this.applyHit(living, DIRECT_DAMAGE);

            if (this.level.isClientSide) {
                living.level.playLocalSound(
                    living.getX(),
                    living.getY(),
                    living.getZ(),
                    SoundEvents.THORNS_HIT,
                    SoundSource.HOSTILE,
                    3.0F,
                    1.0F,
                    false
                );
            }
        }
        this.explode(p_37259_.getEntity());
    }

    @Override
    protected void onHitBlock(BlockHitResult p_37258_) {
        super.onHitBlock(p_37258_);
        this.explode(null);
    }

    private void applyHit(LivingEntity living, float damage) {
        living.hurt(DamageSource.LIGHTNING_BOLT, damage * this.damageScale);

        // Apply Slowness II 2s to the player
        if (living instanceof Player player && player.isEffectiveAi()) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
        }
    }

    // Explosion en area sin romper bloques ni provocar fuego: solo dano a entidades.
    private void explode(Entity directlyHit) {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 center = this.position();
        AABB area = new AABB(center, center).inflate(SPLASH_RADIUS);
        for (LivingEntity living : serverLevel.getEntitiesOfClass(LivingEntity.class, area, e -> e != directlyHit && this.canHitEntity(e))) {
            if (living.getBoundingBox().getCenter().distanceToSqr(center) <= SPLASH_RADIUS * SPLASH_RADIUS) {
                this.applyHit(living, SPLASH_DAMAGE);
            }
        }

        serverLevel.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z, 0, 0.5D, 0.0D, 0.0D, 1.0D);
        serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, 24, 0.6D, 0.6D, 0.6D, 0.4D);
        serverLevel.playSound(null, center.x, center.y, center.z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.HOSTILE, 1.0F, 1.6F);
    }
}
