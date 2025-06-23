package com.TBK.beyond_the_end.server.entity;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.server.entity.phase.FallenDragonPhase;
import com.TBK.beyond_the_end.server.network.PacketHandler;
import com.TBK.beyond_the_end.server.network.message.PacketNextActionJellyfish;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.GeckoLib;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class FallenDragonFakeEntity extends PathfinderMob implements IAnimatable {
    public final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    public FallenDragonFakeEntity(EntityType<? extends PathfinderMob> p_21683_, Level p_21684_) {
        super(p_21683_, p_21684_);
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this,"controller",10, state -> {
            state.getController().setAnimationSpeed(1.0D);
            state.getController().setAnimation(new AnimationBuilder().loop("death"));
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    @Override
    protected int calculateFallDamage(float p_21237_, float p_21238_) {
        return 0;
    }
    protected PathNavigation createNavigation(Level pLevel) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, pLevel);
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(true);
        flyingpathnavigation.setCanPassDoors(true);
        return flyingpathnavigation;
    }
    @Override
    public void tick() {
        this.noPhysics = true;
        this.setNoGravity(true);
        JellyfishEntity jellyfish = this.level.getNearestEntity(JellyfishEntity.class, TargetingConditions.forNonCombat(),this,this.getX(),this.getY(),this.getZ(),this.getBoundingBox().inflate(1000));
        if(jellyfish!=null){
            Vec3 vec32 = this.position().subtract(jellyfish.getEyePosition());
            this.setDeltaMovement(vec32.multiply(-1,-1,-1).normalize().scale(1.0D));
            jellyfish.dragonDeath=this.position();
            jellyfish.setTarget(this);

            double f5 = -Math.toDegrees(Math.atan2(vec32.y,Math.sqrt(vec32.x*vec32.x + vec32.z*vec32.z)));
            double f6 = Math.toDegrees(Math.atan2(vec32.z, vec32.x)) - 90.0F;
            this.yHeadRot=(float)f6;
            this.setYHeadRot((float) f6);
            this.yBodyRot= (float) (f6);
            this.setYRot((float) f6);
            this.setXRot((float) f5);
            this.setRot(this.getYRot(),this.getXRot());
            if(!this.level.getEntities(this,this.getBoundingBox(),e->e==jellyfish).isEmpty()) {
                this.discard();
                jellyfish.shootLaser.stop();
                jellyfish.setActionForID(4);
                jellyfish.positionLastGroundPos=0;
                if(!this.level.isClientSide){
                    PacketHandler.sendToAllTracking(new PacketNextActionJellyfish(jellyfish.getId(),0,4),this);
                }
            }
        }
        super.tick();
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
