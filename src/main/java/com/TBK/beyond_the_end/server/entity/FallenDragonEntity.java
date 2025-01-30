package com.TBK.beyond_the_end.server.entity;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.server.entity.phase.*;
import com.TBK.beyond_the_end.server.network.PacketHandler;
import com.TBK.beyond_the_end.server.network.message.PacketTargetAttack;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.BinaryHeap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.*;


public class FallenDragonEntity extends PathfinderMob implements IAnimatable {
    public static final EntityDataAccessor<Integer> DATA_PHASE = SynchedEntityData.defineId(FallenDragonEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> MODE_FLY=SynchedEntityData.defineId(FallenDragonEntity.class,EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> CHARGING=SynchedEntityData.defineId(FallenDragonEntity.class,EntityDataSerializers.BOOLEAN);

    public final double[][] positions = new double[64][3];
    public int posPointer = -1;
    private final AnimationFactory factory= GeckoLibUtil.createFactory(this);
    private static final Logger LOGGER = LogUtils.getLogger();
    private final FallenDragonFight dragonFight;
    public final FallenDragonPhaseManager phaseManager;
    public boolean isCharging = false;
    private int growlTime = 100;
    private final FallenPartEntity[] subEntities;
    public final FallenPartEntity head;
    private final FallenPartEntity neck;
    private final FallenPartEntity body;
    private final FallenPartEntity tail1;
    private final FallenPartEntity tail2;
    private final FallenPartEntity tail3;
    private final FallenPartEntity wing1;
    private final FallenPartEntity wing2;

    public final int[] posZ ={
            0,1,-1,0
    };
    public final int[] posX = {
            1, 0, 0, -1
    };
    public BlockPos targetPos=null;
    private int attackMelee=0;
    public float oFlapTime;
    public float flapTime;
    public boolean inWall;
    public int dragonDeathTime;
    public float yRotA;
    private final Node[] nodes = new Node[24];
    private final int[] nodeAdjacency = new int[24];
    private final BinaryHeap openSet = new BinaryHeap();
    private boolean isBackAttack = false;
    public int chargedAnim = 0;
    public boolean isFlame=false;

    public FallenDragonEntity(EntityType<? extends FallenDragonEntity> p_31096_, Level p_31097_) {
        super(p_31096_, p_31097_);
        this.head = new FallenPartEntity(this, "head", 1.0F, 1.0F);
        this.neck = new FallenPartEntity(this, "neck", 3.0F, 3.0F);
        this.body = new FallenPartEntity(this, "body", 5.0F, 3.0F);
        this.tail1 = new FallenPartEntity(this, "tail", 2.0F, 2.0F);
        this.tail2 = new FallenPartEntity(this, "tail", 2.0F, 2.0F);
        this.tail3 = new FallenPartEntity(this, "tail", 2.0F, 2.0F);
        this.wing1 = new FallenPartEntity(this, "wing", 4.0F, 2.0F);
        this.wing2 = new FallenPartEntity(this, "wing", 4.0F, 2.0F);

        this.noPhysics=false;
        if (p_31097_ instanceof ServerLevel) {
            this.dragonFight = BeyondTheEnd.bossFight;
        } else {
            this.dragonFight = null;
        }
        this.phaseManager=new FallenDragonPhaseManager(this);
        this.subEntities = new FallenPartEntity[]{this.head, this.neck, this.body, this.tail1, this.tail2, this.tail3, this.wing1, this.wing2};

        this.setId(ENTITY_COUNTER.getAndAdd(this.subEntities.length + 1) + 1);
    }

    @Override
    public void setId(int p_20235_) {
        super.setId(p_20235_);
        for (int i = 0; i < this.subEntities.length; i++) // Forge: Fix MC-158205: Set part ids to successors of parent mob id
            this.subEntities[i].setId(p_20235_ + i + 1);
    }

    @Override
    public float getStepHeight() {
        return 3.0F;
    }

    public boolean isFlapping() {
        float f = Mth.cos(this.flapTime * ((float)Math.PI * 2F));
        float f1 = Mth.cos(this.oFlapTime * ((float)Math.PI * 2F));
        return f1 <= -0.3F && f >= -0.3F;
    }

    public void onFlap() {
        if (this.level.isClientSide && !this.isSilent()) {
            this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENDER_DRAGON_FLAP, this.getSoundSource(), 5.0F, 0.8F + this.random.nextFloat() * 0.3F, false);
        }

    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 500.0D)
                .add(Attributes.FOLLOW_RANGE,100.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE,1.0F);
    }


    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(3,new HurtByTargetGoal(this));
        this.targetSelector.addGoal(9,new NearestAttackableTargetGoal<>(this, LivingEntity.class,false));
        this.targetSelector.addGoal(4,new AttackGoal(this,0.4,false));

    }

    public boolean canAttack(LivingEntity p_149576_) {
        return p_149576_.canBeSeenAsEnemy();
    }




    public boolean canFly(){
        return this.hasModeFly();
    }

    @Override
    public void tick() {
        super.tick();
        if(this.attackMelee>0){
            this.getNavigation().stop();
            this.attackMelee--;
            if(this.attackMelee==2){
                if(this.isBackAttack){
                    this.tailArea();
                    this.isBackAttack=false;
                }else {
                    this.particlePoof(8);
                    this.knockBack(this.level.getEntities(this, this.getBoundingBox().inflate(8.0D, 2.0D, 8.0D).move(0.0D, -2.0D, 0.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR),false);
                }
            }
        }

        if(this.chargedAnim>0){
            this.chargedAnim--;
            this.setDeltaMovement(Vec3.ZERO);
            this.setPos(this.getX(),this.getY(),this.getZ());
            if(this.targetPos!=null){
                this.getLookControl().setLookAt(Vec3.atBottomCenterOf(this.targetPos));
            }
            if(this.chargedAnim<=0){
                this.setNoGravity(false);
                this.setCharging(true);
                if(!this.level.isClientSide){
                    this.level.broadcastEntityEvent(this,(byte) 64);
                }
            }
        }


        if(this.isCharging()){
            if(this.level.isClientSide){
                for (int i=0;i<50;i++){
                    this.particleCharged();
                }
            }
            if(this.targetPos==null){
                this.targetPos=new BlockPos(0,70,0);
            }
            Vec3 delta=new Vec3(this.targetPos.getX(),this.targetPos.getY(),this.targetPos.getZ()).subtract(this.position()).normalize();
            this.getLookControl().setLookAt(Vec3.atBottomCenterOf(this.targetPos));
            this.yBodyRot=this.getYHeadRot();
            this.setDeltaMovement(delta.scale(2.0D));
            if(this.checkWalls(this.getBoundingBox().inflate(1.0F))){
                this.setDeltaMovement(Vec3.ZERO);
                this.setCharging(false);
                this.level.broadcastEntityEvent(this,(byte) 67);
                this.knockBack(this.level.getEntities(this, this.getBoundingBox().inflate(30.0D, 10.0D, 30.0D).move(0.0D, -10.0D, 0.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
                this.targetPos=null;
            }

            if(this.fallDistance>4026.0D){
                this.setDeltaMovement(Vec3.ZERO);
                this.fallDistance=0.0F;
                this.setCharging(false);
                this.initFall();
            }
        }
        this.refreshDimensions();
    }

    public void particleCharged() {
        Random random = new Random();
        double box=this.getBbWidth();
        double d5=-this.getDeltaMovement().x;
        double d7=-this.getDeltaMovement().z;
        double xp=this.getX() + random.nextDouble(-box,box);
        double yp=this.getY() + random.nextDouble(0.0d,this.getBbHeight());
        double zp=this.getZ() + random.nextDouble(-box,box);
        this.level.addAlwaysVisibleParticle(ParticleTypes.DRAGON_BREATH, xp, yp, zp, d5, 0.1F, d7);
    }

    @Override
    protected int calculateFallDamage(float p_21237_, float p_21238_) {
        return 0;
    }

    @Override
    public int getMaxHeadXRot() {
        return 360;
    }


    
    public void tailArea(){
        List<LivingEntity> targets = this.level.getEntitiesOfClass(LivingEntity.class,this.getBoundingBox().inflate(16,5,16), e -> e != this && this.distanceTo(e) <= 81 + e.getBbWidth() / 2f && e.getY() <= this.getY() + 3);
        for(LivingEntity living : targets){
            float entityHitAngle = (float) ((Math.atan2(living.getZ() - this.getZ(), living.getX() - this.getX()) * (180 / Math.PI) - 90) % 360);
            float entityAttackingAngle = (this.yBodyRot-180F) % 360;
            float arc = 180.0F;
            if (entityHitAngle < 0) {
                entityHitAngle += 360;
            }
            if (entityAttackingAngle < 0) {
                entityAttackingAngle += 360;
            }
            float entityRelativeAngle = entityHitAngle - entityAttackingAngle;
            float entityHitDistance = (float) Math.sqrt((living.getZ() - this.getZ()) * (living.getZ() - this.getZ()) + (living.getX() - this.getX()) * (living.getX() - this.getX())) - living.getBbWidth() / 2f;
            if (entityHitDistance <= 18 - 0.3 && (entityRelativeAngle <= arc / 2 && entityRelativeAngle >= -arc / 2) || (entityRelativeAngle >= 360 - arc / 2 || entityRelativeAngle <= -360 + arc / 2) ) {
                if(living.isBlocking() && living instanceof Player player){
                    player.disableShield(true);
                }
                living.hurt(DamageSource.mobAttack(this).bypassArmor(), 12.0F);
            }
        }
    }

    private void knockBack(List<Entity> p_31132_) {
        for(Entity entity : p_31132_) {
            if (entity instanceof LivingEntity) {
                Vec3 pushed=new Vec3(entity.getX()-this.getX(),0.0D,entity.getZ()-this.getZ()).normalize().scale(15.0D);
                entity.push(pushed.x, (double)0.8F, pushed.z);
                entity.hurt(DamageSource.mobAttack(this), 20.0F);
            }
        }
    }

    public void kill() {
        this.remove(Entity.RemovalReason.KILLED);
        this.gameEvent(GameEvent.ENTITY_DIE);
        if (this.dragonFight != null) {
            this.dragonFight.updateDragon(this);
            this.dragonFight.setDragonKilled(this);
        }

    }

    protected void tickDeath() {
        if (this.dragonFight != null) {
            this.dragonFight.updateDragon(this);
        }

        ++this.dragonDeathTime;
        if (this.dragonDeathTime >= 180 && this.dragonDeathTime <= 200) {
            float f = (this.random.nextFloat() - 0.5F) * 8.0F;
            float f1 = (this.random.nextFloat() - 0.5F) * 4.0F;
            float f2 = (this.random.nextFloat() - 0.5F) * 8.0F;
            this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.getX() + (double)f, this.getY() + 2.0D + (double)f1, this.getZ() + (double)f2, 0.0D, 0.0D, 0.0D);
        }

        boolean flag = this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT);
        int i = 500;
        if (this.dragonFight != null && !this.dragonFight.hasPreviouslyKilledDragon()) {
            i = 12000;
        }

        if (this.level instanceof ServerLevel) {
            if (this.dragonDeathTime > 150 && this.dragonDeathTime % 5 == 0 && flag) {
                int award =  Mth.floor((float)i * 0.08F);
                ExperienceOrb.award((ServerLevel)this.level, this.position(), award);
            }

            if (this.dragonDeathTime == 1 && !this.isSilent()) {
                this.level.globalLevelEvent(1028, this.blockPosition(), 0);
            }
        }

        this.move(MoverType.SELF, new Vec3(0.0D, (double)0.1F, 0.0D));
        this.setYRot(this.getYRot() + 1.0F);
        this.yBodyRot = this.getYRot();
        if (this.dragonDeathTime == 200 && this.level instanceof ServerLevel) {
            if (flag) {
                int award = Mth.floor((float)i * 0.2F);
                ExperienceOrb.award((ServerLevel)this.level, this.position(), award);
            }

            if (this.dragonFight != null) {
                this.dragonFight.setDragonKilled(this);
            }

            this.remove(Entity.RemovalReason.KILLED);
            this.gameEvent(GameEvent.ENTITY_DIE);
        }

    }


    private void knockBack(List<Entity> p_31132_, boolean up) {
        double d0 = (this.body.getBoundingBox().minX + this.body.getBoundingBox().maxX) / 2.0D;
        double d1 = (this.body.getBoundingBox().minZ + this.body.getBoundingBox().maxZ) / 2.0D;

        for(Entity entity : p_31132_) {
            if (entity instanceof LivingEntity) {
                double d2 = entity.getX() - d0;
                double d3 = entity.getZ() - d1;
                double d4 = Math.max(d2 * d2 + d3 * d3, 0.1D);
                entity.push(d2 / d4 * 16.0D,(double)0.2F, d3 / d4 * 16.0D);
                entity.hurt(DamageSource.mobAttack(this), 20.0F);
                this.doEnchantDamageEffects(this, entity);
            }
        }
    }

    public boolean isRespawn(){
        return this.phaseManager.getCurrentPhase()!=null && this.phaseManager.getCurrentPhase().getPhase().equals(FallenDragonPhase.RESPAWN);
    }


    public void aiStep() {
        this.processFlappingMovement();
        if (this.level.isClientSide) {
            this.setHealth(this.getHealth());
            if (!this.isSilent() && !this.phaseManager.getCurrentPhase().isSitting() && --this.growlTime < 0) {
                this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENDER_DRAGON_GROWL, this.getSoundSource(), 2.5F, 0.8F + this.random.nextFloat() * 0.3F, false);
                this.growlTime = 200 + this.random.nextInt(200);
            }
        }

        this.oFlapTime = this.flapTime;
        if (this.isRespawn() || this.dragonDeathTime>0) {
            FallenDragonInstance dragonphaseinstance = this.phaseManager.getCurrentPhase();

            if(dragonphaseinstance!=null){
                if(this.level.isClientSide){
                    this.phaseManager.getCurrentPhase().doClientTick();
                }else {
                    dragonphaseinstance.doServerTick();
                    if (this.phaseManager.getCurrentPhase() != dragonphaseinstance) {
                        dragonphaseinstance = this.phaseManager.getCurrentPhase();
                        dragonphaseinstance.doServerTick();
                    }
                }
            }
        } else {
            if(canFly()){
                this.noPhysics=true;
                this.getNavigation().stop();
                FallenDragonInstance dragonphaseinstance = this.phaseManager.getCurrentPhase();

                if(dragonphaseinstance!=null){
                    if(this.level.isClientSide){
                        this.phaseManager.getCurrentPhase().doClientTick();
                    }else {
                        dragonphaseinstance.doServerTick();
                        if (this.phaseManager.getCurrentPhase() != dragonphaseinstance) {
                            dragonphaseinstance = this.phaseManager.getCurrentPhase();
                            dragonphaseinstance.doServerTick();
                        }
                    }
                }
                this.yBodyRot = this.getYRot();
                Vec3 vec3 = this.getDeltaMovement();
                float f = 0.2F / ((float)vec3.horizontalDistance() * 10.0F + 1.0F);
                f *= (float)Math.pow(2.0D, vec3.y);
                if (this.phaseManager.getCurrentPhase().isSitting()) {
                    this.flapTime += 0.1F;
                } else if (this.inWall) {
                    this.flapTime += f * 0.5F;
                } else {
                    this.flapTime += f;
                }

                this.setYRot(Mth.wrapDegrees(this.getYRot()));
                if (this.isNoAi()) {
                    this.flapTime = 0.5F;
                } else {
                    if (this.posPointer < 0) {
                        for(int i = 0; i < this.positions.length; ++i) {
                            this.positions[i][0] = (double)this.getYRot();
                            this.positions[i][1] = this.getY();
                        }
                    }

                    if (++this.posPointer == this.positions.length) {
                        this.posPointer = 0;
                    }

                    this.positions[this.posPointer][0] = (double)this.getYRot();
                    this.positions[this.posPointer][1] = this.getY();
                    if (this.level.isClientSide) {
                        if (this.lerpSteps > 0) {
                            double d6 = this.getX() + (this.lerpX - this.getX()) / (double)this.lerpSteps;
                            double d0 = this.getY() + (this.lerpY - this.getY()) / (double)this.lerpSteps;
                            double d1 = this.getZ() + (this.lerpZ - this.getZ()) / (double)this.lerpSteps;
                            double d2 = Mth.wrapDegrees(this.lerpYRot - (double)this.getYRot());
                            this.setYRot(this.getYRot() + (float)d2 / (float)this.lerpSteps);
                            this.setXRot(this.getXRot() + (float)(this.lerpXRot - (double)this.getXRot()) / (float)this.lerpSteps);
                            --this.lerpSteps;
                            this.setPos(d6, d0, d1);
                            this.setRot(this.getYRot(), this.getXRot());
                        }
                    } else {
                        Vec3 vec31 = dragonphaseinstance.getFlyTargetLocation();
                        if (vec31 != null) {
                            double d7 = vec31.x - this.getX();
                            double d8 = vec31.y - this.getY();
                            double d9 = vec31.z - this.getZ();
                            double d3 = d7 * d7 + d8 * d8 + d9 * d9;
                            float f5 = dragonphaseinstance.getFlySpeed();
                            double d4 = Math.sqrt(d7 * d7 + d9 * d9);
                            if (d4 > 0.0D) {
                                d8 = Mth.clamp(d8 / d4, (double)(-f5), (double)f5);
                            }

                            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, d8 * 0.01D, 0.0D));
                            this.setYRot(Mth.wrapDegrees(this.getYRot()));
                            Vec3 vec32 = vec31.subtract(this.getX(), this.getY(), this.getZ()).normalize();
                            Vec3 vec33 = (new Vec3((double)Mth.sin(this.getYRot() * ((float)Math.PI / 180F)), this.getDeltaMovement().y, (double)(-Mth.cos(this.getYRot() * ((float)Math.PI / 180F))))).normalize();
                            float f6 = Math.max(((float)vec33.dot(vec32) + 0.5F) / 1.5F, 0.0F);
                            if (Math.abs(d7) > (double)1.0E-5F || Math.abs(d9) > (double)1.0E-5F) {
                                float f7 = Mth.clamp(Mth.wrapDegrees(180.0F - (float)Mth.atan2(d7, d9) * (180F / (float)Math.PI) - this.getYRot()), -50.0F, 50.0F);
                                this.yRotA *= 0.8F;
                                this.yRotA += f7 * dragonphaseinstance.getTurnSpeed();
                                this.setYRot(this.getYRot() + this.yRotA * 0.1F);
                            }

                            float f19 = (float)(2.0D / (d3 + 1.0D));
                            float f8 = 0.06F;
                            this.moveRelative(0.06F * (f6 * f19 + (1.0F - f19)), new Vec3(0.0D, 0.0D, -1.0D));
                            if (this.inWall) {
                                this.move(MoverType.SELF, this.getDeltaMovement().scale((double)0.8F));
                            } else {
                                this.move(MoverType.SELF, this.getDeltaMovement());
                            }

                            Vec3 vec34 = this.getDeltaMovement().normalize();
                            double d5 = 0.8D + 0.15D * (vec34.dot(vec33) + 1.0D) / 2.0D;
                            this.setDeltaMovement(this.getDeltaMovement().multiply(d5, (double)0.91F, d5));
                        }
                    }

                }
            }else {
                this.noPhysics=false;
                super.aiStep();
            }
        }
        if (!this.level.isClientSide) {
            if (this.dragonFight != null) {
                this.dragonFight.updateDragon(this);
            }
        }
        boolean flag=this.isOnGround() && this.phaseManager.getCurrentPhase().getPhase().equals(FallenDragonPhase.FLAME);

        this.yBodyRot = this.getYRot();
        Vec3[] avec3 = new Vec3[this.subEntities.length];

        for(int j = 0; j < this.subEntities.length; ++j) {
            avec3[j] = new Vec3(this.subEntities[j].getX(), this.subEntities[j].getY(), this.subEntities[j].getZ());
        }

        float f12 = (float)(this.getLatencyPos(5, 1.0F)[1] - this.getLatencyPos(10, 1.0F)[1]) * 10.0F * ((float)Math.PI / 180F);
        float f13 = Mth.cos(f12);
        float f1 = Mth.sin(f12);
        float f14 = this.getYRot() * ((float)Math.PI / 180F);
        float f2 = Mth.sin(f14);
        float f15 = Mth.cos(f14);

        this.tickPart(this.body, (double)(f2 * 0.5F), 0.0D, (double)(-f15 * 0.5F));
        this.tickPart(this.wing1, (double)(f15 * 4.5F), 2.0D, (double)(f2 * 4.5F));
        this.tickPart(this.wing2, (double)(f15 * -4.5F), 2.0D, (double)(f2 * -4.5F));
        if (!this.level.isClientSide && this.hurtTime == 0 && this.canFly()) {
            this.knockBack(this.level.getEntities(this, this.wing1.getBoundingBox().inflate(4.0D, 2.0D, 4.0D).move(0.0D, -2.0D, 0.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR),false);
            this.knockBack(this.level.getEntities(this, this.wing2.getBoundingBox().inflate(4.0D, 2.0D, 4.0D).move(0.0D, -2.0D, 0.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR),false);
            this.hurt(this.level.getEntities(this, this.head.getBoundingBox().inflate(1.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
            this.hurt(this.level.getEntities(this, this.neck.getBoundingBox().inflate(1.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
        }

        float f3 = Mth.sin(this.getYRot() * ((float)Math.PI / 180F) - this.yRotA * 0.01F);
        float f16 = Mth.cos(this.getYRot() * ((float)Math.PI / 180F) - this.yRotA * 0.01F);
        float f4 = this.getHeadYOffset();
        if(flag){
            this.tickPart(this.head, (double)-(f2 * 6.5F), (double)(f4 + f1 * 6.5F), (double)-(-f15 * 6.5F));
            this.tickPart(this.neck, (double)-(f2 * 5.5F), (double)(f4 + f1 * 5.5F), (double)-(-f15 * 5.5F));
        }else{
            this.tickPart(this.head, (double)(f3 * 6.5F * f13), (double)(f4 + f1 * 6.5F), (double)(-f16 * 6.5F * f13));
            this.tickPart(this.neck, (double)(f3 * 5.5F * f13), (double)(f4 + f1 * 5.5F), (double)(-f16 * 5.5F * f13));
        }

        double[] adouble = this.getLatencyPos(5, 1.0F);

        for(int k = 0; k < 3; ++k) {
            FallenPartEntity enderdragonpart = null;
            if (k == 0) {
                enderdragonpart = this.tail1;
            }

            if (k == 1) {
                enderdragonpart = this.tail2;
            }

            if (k == 2) {
                enderdragonpart = this.tail3;
            }

            double[] adouble1 = this.getLatencyPos(12 + k * 2, flag ? 0.0F :1.0F);
            float f17 = this.getYRot() * ((float)Math.PI / 180F) + this.rotWrap(adouble1[0] - adouble[0]) * ((float)Math.PI / 180F);
            float f18 = Mth.sin(f17);
            float f20 = Mth.cos(f17);
            float f21 = 1.5F;
            float f22 = (float)(k + 1) * 2.0F;
            if(flag){
                this.tickPart(enderdragonpart, (double)((f2 * 1.5F - f22)), adouble1[1] - adouble[1] - (double)((f22 + 1.5F) * f1) + 1.5D, (double)(-(f15 * 1.5F - f22)));
            }else {
                this.tickPart(enderdragonpart, (double)(-(f2 * 1.5F + f18 * f22) * f13), adouble1[1] - adouble[1] - (double)((f22 + 1.5F) * f1) + 1.5D, (double)((f15 * 1.5F + f20 * f22) * f13));
            }
        }

        for(int l = 0; l < this.subEntities.length; ++l) {
            this.subEntities[l].xo = avec3[l].x;
            this.subEntities[l].yo = avec3[l].y;
            this.subEntities[l].zo = avec3[l].z;
            this.subEntities[l].xOld = avec3[l].x;
            this.subEntities[l].yOld = avec3[l].y;
            this.subEntities[l].zOld = avec3[l].z;
        }


    }

    private void hurt(List<Entity> p_31142_) {
        for(Entity entity : p_31142_) {
            if (entity instanceof LivingEntity) {
                entity.hurt(DamageSource.mobAttack(this), this.isCharging ? 20.0F : 10.0F);
                this.doEnchantDamageEffects(this, entity);
            }
        }

    }

    @Override
    public void die(DamageSource p_21014_) {
        this.setModeFly(true);
        super.die(p_21014_);
    }

    private float rotWrap(double p_31165_) {
        return (float)Mth.wrapDegrees(p_31165_);
    }

    public boolean hurt(FallenPartEntity p_31121_, DamageSource p_31122_, float p_31123_) {
        if(this.isRespawn() || this.phaseManager.getCurrentPhase().getPhase() == FallenDragonPhase.DEATH){
            return false;
        }
        boolean damage=attackDetermination(p_31122_);
        if (!damage) {
            return false;
        } else {
            p_31123_ = this.phaseManager.getCurrentPhase().onHurt(p_31122_, p_31123_);
            if (p_31121_ != this.head) {
                p_31123_ = p_31123_ / 4.0F + Math.min(p_31123_, 1.0F);
            }

            if (p_31123_ < 0.01F) {
                return false;
            } else {
                if (p_31122_.getEntity() instanceof Player || p_31122_.isExplosion()) {
                    this.reallyHurt(p_31122_, p_31123_);
                    if (this.isDeadOrDying()) {
                        this.setHealth(1.0F);
                        this.phaseManager.setPhase(FallenDragonPhase.DEATH);
                    }
                }
                return true;
            }
        }
    }
    protected void reallyHurt(DamageSource p_31162_, float p_31163_) {
        super.hurt(p_31162_, p_31163_);
    }
    @Override
    public boolean hurt(DamageSource p_31113_, float p_31114_) {
        return !this.level.isClientSide && this.hurt(this.body, p_31113_, p_31114_);
    }

    public boolean attackDetermination(DamageSource p_21016_) {
        if(p_21016_.isMagic()){
            return false;
        }
        if(p_21016_.getEntity() !=null && this.random.nextFloat()<0.5F){
            LivingEntity living= (LivingEntity) p_21016_.getEntity();
            float entityHitAngle = (float) ((Math.atan2(living.getZ() - this.getZ(), living.getX() - this.getX()) * (180 / Math.PI) - 90) % 360);
            float entityAttackingAngle = (this.yBodyRot-180.0F) % 360;
            float arc = 50.0F;
            if (entityHitAngle < 0) {
                entityHitAngle += 360;
            }
            if (entityAttackingAngle < 0) {
                entityAttackingAngle += 360;
            }
            float entityRelativeAngle = entityHitAngle - entityAttackingAngle;
            float entityHitDistance = (float) Math.sqrt((living.getZ() - this.getZ()) * (living.getZ() - this.getZ()) + (living.getX() - this.getX()) * (living.getX() - this.getX())) - living.getBbWidth() / 2f;
            if (entityHitDistance <= 18 - 0.3 && (entityRelativeAngle <= arc / 2 && entityRelativeAngle >= -arc / 2) || (entityRelativeAngle >= 360 - arc / 2 || entityRelativeAngle <= -360 + arc / 2) ) {
                if(!this.isBackAttack){
                    this.isBackAttack=true;
                    this.attackMelee=23;
                    if(!this.level.isClientSide){
                        this.level.broadcastEntityEvent(this,(byte) 62);
                    }
                }
            }
        }
        if(this.phaseManager.getCurrentPhase().getPhase()!=FallenDragonPhase.FLAME && this.attackMelee<=0 && this.chargedAnim<=0 && !this.isCharging()){
            if(this.getHealth()<this.getMaxHealth()*0.6F){
                if(this.hasModeFly()){
                    this.initFall();
                }else{
                    if(this.random.nextFloat()<0.15F+0.3F*Math.min((1.4F-(this.getHealth()/this.getMaxHealth())),1.0F)){
                        boolean attack=this.random.nextFloat()<0.6F;
                        if(attack){
                            this.initFall();
                        }else {
                            this.initFlameAttack();
                        }
                    }
                }
            }
        }

        return true;

    }


    public void initFlameAttack(){
        if(!this.level.isClientSide){
            this.setNoGravity(true);
            Collection<ServerPlayer> collection=this.dragonFight.getPlayer();
            Optional<ServerPlayer> player = collection.stream().min((e, y) -> (int) e.distanceToSqr(new Vec3(0, 70, 0)));
            if(player.isPresent() && !player.get().isCreative()){
                int random = this.random.nextInt(0,4);
                int x= (int) ((player.get().getX())) + this.posX[random]*this.random.nextInt(32,64);
                int z= (int) ((player.get().getZ())) + this.posZ[random]*this.random.nextInt(32,64);
                if(this.teleport(x,90,z)){
                    this.phaseManager.setPhase(FallenDragonPhase.FLAME);
                    this.phaseManager.getPhase(FallenDragonPhase.FLAME).setTarget(player.get());
                    this.level.broadcastEntityEvent(this,(byte) 70);
                }
            }
            this.setModeFly(true);
        }
    }

    public void initFall() {
        if(!this.level.isClientSide){
            this.phaseManager.setPhase(FallenDragonPhase.HOLDING_PATTERN);
            Collection<ServerPlayer> collection=this.dragonFight.getPlayer();
            Optional<ServerPlayer> player = collection.stream().min((e, y) -> (int) e.distanceToSqr(new Vec3(0, 70, 0)));
            if(player.isPresent() && !player.get().isCreative()){
                if(this.teleportTowards(player.get())){
                    BlockPos pos=player.get().level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,player.get().getOnPos());
                    this.targetPos=pos;
                    PacketHandler.sendToAllTracking(new PacketTargetAttack(this.getId(),pos.getX(),pos.getY(),pos.getZ()),this);
                }
            }else{
                if(this.teleport(0,100,0)){
                    this.targetPos=this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,BlockPos.ZERO);
                    PacketHandler.sendToAllTracking(new PacketTargetAttack(this.getId(),0,50,0),this);
                }
            }
            this.isCharging=false;
            this.setNoGravity(true);
            this.setModeFly(false);
            this.chargedAnim=63;
            this.level.broadcastEntityEvent(this,(byte) 63);
        }
    }

    public boolean teleportTowards(Entity p_32501_) {

        Vec3 vec3 = new Vec3(this.getX() - p_32501_.getX(), this.getY(0.5D), this.getZ() - p_32501_.getZ());
        vec3 = vec3.normalize();
        double d1 = this.getX() + (this.random.nextDouble() - 0.5D) * this.level.random.nextInt(32,64) - vec3.x * 6.0D;
        double d2 = 110.0F;
        double d3 = this.getZ() + (this.random.nextDouble() - 0.5D) * this.level.random.nextInt(32,64) - vec3.z * 6.0D;
        return this.teleport(d1, d2, d3);
    }

    private boolean teleport(double p_32544_, double p_32545_, double p_32546_) {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(p_32544_, p_32545_, p_32546_);

        while(blockpos$mutableblockpos.getY() > this.level.getMinBuildHeight()) {
            blockpos$mutableblockpos.move(Direction.DOWN);
        }

        BlockState blockstate = this.level.getBlockState(blockpos$mutableblockpos);
        boolean flag = blockstate.isAir();
        boolean flag1 = blockstate.getFluidState().is(FluidTags.WATER);
        if (flag && !flag1) {
            net.minecraftforge.event.entity.EntityTeleportEvent.EnderEntity event = net.minecraftforge.event.ForgeEventFactory.onEnderTeleport(this, p_32544_, p_32545_, p_32546_);
            if (event.isCanceled()) return false;
            Vec3 vec3 = this.position();
            boolean flag2 = this.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true);
            if (flag2) {
                this.level.gameEvent(GameEvent.TELEPORT, vec3, GameEvent.Context.of(this));
                if (!this.isSilent()) {
                    this.level.playSound((Player)null, this.xo, this.yo, this.zo, SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
                    this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                }
            }

            return flag2;
        } else {
            return false;
        }
    }

    public boolean randomTeleport(double p_20985_, double p_20986_, double p_20987_, boolean p_20988_) {
        double d0 = this.getX();
        double d1 = this.getY();
        double d2 = this.getZ();
        double d3 = p_20986_;
        boolean flag = false;
        BlockPos blockpos = new BlockPos(p_20985_, p_20986_, p_20987_);
        Level level = this.level;
        if (level.hasChunkAt(blockpos)) {
            boolean flag1 = false;

            while(!flag1 && blockpos.getY() > level.getMinBuildHeight()) {
                BlockPos blockpos1 = blockpos.below();
                BlockState blockstate = level.getBlockState(blockpos1);
                if (!blockstate.getMaterial().blocksMotion()) {
                    flag1 = true;
                } else {
                    --d3;
                    blockpos = blockpos1;
                }
            }

            if (flag1) {
                this.teleportTo(p_20985_, d3, p_20987_);
                if (level.noCollision(this) && !level.containsAnyLiquid(this.getBoundingBox())) {
                    flag = true;
                }
            }
        }

        if (!flag) {
            this.teleportTo(d0, d1, d2);
            return false;
        } else {
            if (p_20988_) {
                level.broadcastEntityEvent(this, (byte)46);
            }

            if (this instanceof PathfinderMob) {
                ((PathfinderMob)this).getNavigation().stop();
            }

            return true;
        }
    }

    @Override
    public void teleportTo(double p_19887_, double p_19888_, double p_19889_) {
        super.teleportTo(p_19887_, p_19888_, p_19889_);
    }

    @Override
    public EntityDimensions getDimensions(Pose p_21047_) {
        return this.getType().getDimensions().scale(this.getScale(),this.canFly() ? 1.0F : 0.5F);
    }

    @Override
    public float getScale() {
        return this.canFly() ? 4.0F : super.getScale();
    }

    private void tickPart(FallenPartEntity p_31116_, double p_31117_, double p_31118_, double p_31119_) {
        p_31116_.setPos(this.getX() + p_31117_, this.getY() + p_31118_, this.getZ() + p_31119_);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().define(DATA_PHASE, EnderDragonPhase.HOVERING.getId());
        this.entityData.define(MODE_FLY,true);
        this.entityData.define(CHARGING,false);
    }

    public double[] getLatencyPos(int p_31102_, float p_31103_) {
        if (this.isDeadOrDying()) {
            p_31103_ = 0.0F;
        }

        p_31103_ = 1.0F - p_31103_;
        int i = this.posPointer - p_31102_ & 63;
        int j = this.posPointer - p_31102_ - 1 & 63;
        double[] adouble = new double[3];
        double d0 = this.positions[i][0];
        double d1 = Mth.wrapDegrees(this.positions[j][0] - d0);
        adouble[0] = d0 + d1 * (double)p_31103_;
        d0 = this.positions[i][1];
        d1 = this.positions[j][1] - d0;
        adouble[1] = d0 + d1 * (double)p_31103_;
        adouble[2] = Mth.lerp((double)p_31103_, this.positions[i][2], this.positions[j][2]);
        return adouble;
    }

    public float getHeadPartYOffset(int p_31109_, double[] p_31110_, double[] p_31111_) {
        double d0;
        BlockPos blockpos = this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,new BlockPos(0,60,0));
        double d1 = Math.max(Math.sqrt(blockpos.distToCenterSqr(this.position())) / 4.0D, 1.0D);
        d0 = (double)p_31109_ / d1;


        return (float)d0;
    }

    public Vec3 getHeadLookVector(float p_31175_) {
        Vec3 vec3;
        BlockPos blockpos = this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,new BlockPos(0,60,0));
        float f = Math.max((float)Math.sqrt(blockpos.distToCenterSqr(this.position())) / 4.0F, 1.0F);
        float f1 = 6.0F / f;
        float f2 = this.getXRot();
        float f3 = 1.5F;
        this.setXRot(-f1 * 1.5F * 5.0F);
        vec3 = this.getViewVector(p_31175_);
        this.setXRot(f2);


        return vec3;
    }

    @Override
    public void checkDespawn() {

    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void handleEntityEvent(byte p_21375_) {
        if(p_21375_==4){
            this.isFlame=true;
        }else if(p_21375_==8){
            this.attackMelee=45;
        }else if(p_21375_==62){
            this.attackMelee=23;
            this.isBackAttack=true;
        }else if(p_21375_==63){
            this.setNoGravity(true);
            this.setCharging(false);
            this.chargedAnim=63;
            this.setModeFly(false);
            this.phaseManager.setPhase(FallenDragonPhase.HOLDING_PATTERN);
        }else if(p_21375_==64){
            this.setCharging(true);
        }else if(p_21375_==65){
            this.isFlame=false;
        } else if (p_21375_==67) {
            this.particleChargedPoof();
            this.level.playSound(null,this,SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.HOSTILE,4.0F,1.0F);
        } else if (p_21375_==66) {
            this.phaseManager.setPhase(FallenDragonPhase.RESPAWN);
        } else if (p_21375_==68) {
            this.isCharging=true;
        } else if (p_21375_==69) {
            this.isCharging=false;
        } else if (p_21375_==70) {
            this.phaseManager.setPhase(FallenDragonPhase.FLAME);
        }  else{
            super.handleEntityEvent(p_21375_);
        }
    }

    private void particleChargedPoof() {
        ParticleOptions particleoptions = ParticleTypes.POOF;
        int i;
        float f1;
        i = Mth.ceil((float)Math.PI * 20 * 20);
        f1 = 20;

        double d2 = this.level.getHeight(Heightmap.Types.WORLD_SURFACE, (int) this.getX(), (int) this.getZ());

        for(int j = 0; j < i; ++j) {
            float f2 = this.random.nextFloat() * ((float)Math.PI * 2F);
            float f3 = Mth.sqrt(this.random.nextFloat()) * f1;
            double d0 = this.getX() + (double)(Mth.cos(f2) * f3);
            double d4 = this.getZ() + (double)(Mth.sin(f2) * f3);

            Vec3 delta=new Vec3(d0,d2,d4).subtract(this.position()).normalize().scale(2.0D);

            double d5 = delta.x + (0.5D - this.random.nextDouble()) * 0.15D;
            double d6 = (double)0.01F;
            double d7 = delta.z + (0.5D - this.random.nextDouble()) * 0.15D;


            this.level.addAlwaysVisibleParticle(particleoptions, d0, d2, d4, d5, d6, d7);
        }
    }

    public void particlePoof(int range){
        ParticleOptions particleoptions = ParticleTypes.POOF;
        int i;
        float f1;
        i = Mth.ceil((float)Math.PI * range * range);
        f1 = range;

        for(int j = 0; j < i; ++j) {
            float f2 = this.random.nextFloat() * ((float)Math.PI * 2F);
            float f3 = Mth.sqrt(this.random.nextFloat()) * f1;
            double d0 = this.getX() + (double)(Mth.cos(f2) * f3);
            double d2 = this.getY();
            double d4 = this.getZ() + (double)(Mth.sin(f2) * f3);
            double d5;
            double d6;
            double d7;
            if (particleoptions.getType() != ParticleTypes.ENTITY_EFFECT) {
                d5 = (0.5D - this.random.nextDouble()) * 0.15D;
                d6 = (double)0.01F;
                d7 = (0.5D - this.random.nextDouble()) * 0.15D;

            } else {
                int k = 16777215;
                d5 = (double)((float)(k >> 16 & 255) / 255.0F);
                d6 = (double)((float)(k >> 8 & 255) / 255.0F);
                d7 = (double)((float)(k & 255) / 255.0F);
            }

            this.level.addAlwaysVisibleParticle(particleoptions, d0, d2, d4, d5, d6, d7);
        }
    }

    private boolean checkWalls(AABB p_31140_) {
        int i = Mth.floor(p_31140_.minX);
        int j = Mth.floor(p_31140_.minY);
        int k = Mth.floor(p_31140_.minZ);
        int l = Mth.floor(p_31140_.maxX);
        int i1 = Mth.floor(p_31140_.maxY);
        int j1 = Mth.floor(p_31140_.maxZ);
        boolean flag = false;
        boolean flag1 = false;

        for(int k1 = i; k1 <= l; ++k1) {
            for(int l1 = j; l1 <= i1; ++l1) {
                for(int i2 = k; i2 <= j1; ++i2) {
                    BlockPos blockpos = new BlockPos(k1, l1, i2);
                    BlockState blockstate = this.level.getBlockState(blockpos);
                    if (!blockstate.isAir() && !blockstate.is(BlockTags.DRAGON_TRANSPARENT)) {
                        if (net.minecraftforge.common.ForgeHooks.canEntityDestroy(this.level, blockpos, this) && !blockstate.is(BlockTags.DRAGON_IMMUNE)) {
                            flag = true;
                        }
                    }
                }
            }
        }

        return flag;
    }

    private float getHeadYOffset() {
        if (this.phaseManager.getCurrentPhase().isSitting()) {
            return -1.0F;
        }if (this.phaseManager.getCurrentPhase().equals(FallenDragonPhase.FLAME)) {
            return 3.0F;
        } else {
            double[] adouble = this.getLatencyPos(5, 1.0F);
            double[] adouble1 = this.getLatencyPos(0, 1.0F);
            return (float)(adouble[1] - adouble1[1]);
        }
    }

    public int findClosestNode() {
        if (this.nodes[0] == null) {
            for(int i = 0; i < 24; ++i) {
                int j = 5;
                int l;
                int i1;
                if (i < 12) {
                    l = Mth.floor(60.0F * Mth.cos(2.0F * (-(float)Math.PI + 0.2617994F * (float)i)));
                    i1 = Mth.floor(60.0F * Mth.sin(2.0F * (-(float)Math.PI + 0.2617994F * (float)i)));
                } else if (i < 20) {
                    int $$2 = i - 12;
                    l = Mth.floor(40.0F * Mth.cos(2.0F * (-(float)Math.PI + ((float)Math.PI / 8F) * (float)$$2)));
                    i1 = Mth.floor(40.0F * Mth.sin(2.0F * (-(float)Math.PI + ((float)Math.PI / 8F) * (float)$$2)));
                    j += 10;
                } else {
                    int k1 = i - 20;
                    l = Mth.floor(20.0F * Mth.cos(2.0F * (-(float)Math.PI + ((float)Math.PI / 4F) * (float)k1)));
                    i1 = Mth.floor(20.0F * Mth.sin(2.0F * (-(float)Math.PI + ((float)Math.PI / 4F) * (float)k1)));
                }

                int j1 = Math.max(this.level.getSeaLevel() + 10, this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(l, 0, i1)).getY() + j);
                this.nodes[i] = new Node(l, j1, i1);
            }

            this.nodeAdjacency[0] = 6146;
            this.nodeAdjacency[1] = 8197;
            this.nodeAdjacency[2] = 8202;
            this.nodeAdjacency[3] = 16404;
            this.nodeAdjacency[4] = 32808;
            this.nodeAdjacency[5] = 32848;
            this.nodeAdjacency[6] = 65696;
            this.nodeAdjacency[7] = 131392;
            this.nodeAdjacency[8] = 131712;
            this.nodeAdjacency[9] = 263424;
            this.nodeAdjacency[10] = 526848;
            this.nodeAdjacency[11] = 525313;
            this.nodeAdjacency[12] = 1581057;
            this.nodeAdjacency[13] = 3166214;
            this.nodeAdjacency[14] = 2138120;
            this.nodeAdjacency[15] = 6373424;
            this.nodeAdjacency[16] = 4358208;
            this.nodeAdjacency[17] = 12910976;
            this.nodeAdjacency[18] = 9044480;
            this.nodeAdjacency[19] = 9706496;
            this.nodeAdjacency[20] = 15216640;
            this.nodeAdjacency[21] = 13688832;
            this.nodeAdjacency[22] = 11763712;
            this.nodeAdjacency[23] = 8257536;
        }

        return this.findClosestNode(this.getX(), this.getY(), this.getZ());
    }

    public int findClosestNode(double p_31171_, double p_31172_, double p_31173_) {
        float f = 10000.0F;
        int i = 0;
        Node node = new Node(Mth.floor(p_31171_), Mth.floor(p_31172_), Mth.floor(p_31173_));
        int j = 0;
        if (this.dragonFight == null) {
            j = 12;
        }

        for(int k = j; k < 24; ++k) {
            if (this.nodes[k] != null) {
                float f1 = this.nodes[k].distanceToSqr(node);
                if (f1 < f) {
                    f = f1;
                    i = k;
                }
            }
        }

        return i;
    }

    @Nullable
    public Path findPath(int p_31105_, int p_31106_, @Nullable Node p_31107_) {
        for(int i = 0; i < 24; ++i) {
            Node node = this.nodes[i];
            node.closed = false;
            node.f = 0.0F;
            node.g = 0.0F;
            node.h = 0.0F;
            node.cameFrom = null;
            node.heapIdx = -1;
        }

        Node node4 = this.nodes[p_31105_];
        Node node5 = this.nodes[p_31106_];
        node4.g = 0.0F;
        node4.h = node4.distanceTo(node5);
        node4.f = node4.h;
        this.openSet.clear();
        this.openSet.insert(node4);
        Node node1 = node4;
        int j = 0;
        if (this.dragonFight == null) {
            j = 12;
        }

        while(!this.openSet.isEmpty()) {
            Node node2 = this.openSet.pop();
            if (node2.equals(node5)) {
                if (p_31107_ != null) {
                    p_31107_.cameFrom = node5;
                    node5 = p_31107_;
                }

                return this.reconstructPath(node4, node5);
            }

            if (node2.distanceTo(node5) < node1.distanceTo(node5)) {
                node1 = node2;
            }

            node2.closed = true;
            int k = 0;

            for(int l = 0; l < 24; ++l) {
                if (this.nodes[l] == node2) {
                    k = l;
                    break;
                }
            }

            for(int i1 = j; i1 < 24; ++i1) {
                if ((this.nodeAdjacency[k] & 1 << i1) > 0) {
                    Node node3 = this.nodes[i1];
                    if (!node3.closed) {
                        float f = node2.g + node2.distanceTo(node3);
                        if (!node3.inOpenSet() || f < node3.g) {
                            node3.cameFrom = node2;
                            node3.g = f;
                            node3.h = node3.distanceTo(node5);
                            if (node3.inOpenSet()) {
                                this.openSet.changeCost(node3, node3.g + node3.h);
                            } else {
                                node3.f = node3.g + node3.h;
                                this.openSet.insert(node3);
                            }
                        }
                    }
                }
            }
        }

        if (node1 == node4) {
            return null;
        } else {
            LOGGER.debug("Failed to find path from {} to {}", p_31105_, p_31106_);
            if (p_31107_ != null) {
                p_31107_.cameFrom = node1;
                node1 = p_31107_;
            }

            return this.reconstructPath(node4, node1);
        }
    }

    public FallenDragonFight getDragonFight(){
        return this.dragonFight;
    }

    private Path reconstructPath(Node p_31129_, Node p_31130_) {
        List<Node> list = Lists.newArrayList();
        Node node = p_31130_;
        list.add(0, p_31130_);

        while(node.cameFrom != null) {
            node = node.cameFrom;
            list.add(0, node);
        }

        return new Path(list, new BlockPos(p_31130_.x, p_31130_.y, p_31130_.z), true);
    }

    public void addAdditionalSaveData(CompoundTag p_31144_) {
        super.addAdditionalSaveData(p_31144_);
        p_31144_.putInt("DragonPhase", this.phaseManager.getCurrentPhase().getPhase().getId());
        p_31144_.putInt("DragonDeathTime", this.dragonDeathTime);
        p_31144_.putBoolean("isCharging",this.isCharging());
        p_31144_.putBoolean("mode",this.hasModeFly());
    }

    public boolean isCharging() {
        return this.entityData.get(CHARGING);
    }

    private boolean hasModeFly() {
        return this.entityData.get(MODE_FLY);
    }

    public void readAdditionalSaveData(CompoundTag p_31134_) {
        super.readAdditionalSaveData(p_31134_);
        if (p_31134_.contains("DragonPhase")) {
            this.phaseManager.setPhase(FallenDragonPhase.getById(p_31134_.getInt("DragonPhase")));
        }
        this.setCharging(p_31134_.getBoolean("isCharging"));
        this.setModeFly(p_31134_.getBoolean("mode"));

        if (p_31134_.contains("DragonDeathTime")) {
            this.dragonDeathTime = p_31134_.getInt("DragonDeathTime");
        }
    }

    private void setCharging(boolean isCharging) {
        this.entityData.set(CHARGING,isCharging);
    }

    public void setModeFly(boolean mode) {
        this.entityData.set(MODE_FLY,mode);
    }


    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this,"controller",10, state -> {
            boolean isMove= !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F);
            if(!this.isOnGround() || this.phaseManager.getCurrentPhase().getPhase()==FallenDragonPhase.FLAME){
                if(this.phaseManager.getCurrentPhase().getPhase()==FallenDragonPhase.FLAME){
                    state.getController().setAnimationSpeed(1.0D);
                    state.getController().setAnimation(new AnimationBuilder().loop("flyingidlefire"));
                }else if(this.chargedAnim>0 ){
                    state.getController().setAnimationSpeed(1.0D);
                    state.getController().setAnimation(new AnimationBuilder().loop("flycharge"));
                }else if(this.isCharging() || this.isCharging){
                    state.getController().setAnimationSpeed(1.0D);
                    state.getController().setAnimation(new AnimationBuilder().loop("chargeidle"));
                }else if(this.attackMelee<=0){
                    state.getController().setAnimationSpeed(1.0D);
                    state.getController().setAnimation(new AnimationBuilder().loop("flying"));
                }
            }else{
                if(this.attackMelee>0){
                    state.getController().setAnimationSpeed(this.isBackAttack ? 2.0D : 1.0D);
                    state.getController().setAnimation(new AnimationBuilder().playOnce(this.isBackAttack ? "meleetail" : "meleeclaw"));
                }else {
                    state.getController().setAnimationSpeed(1.0D);
                    state.getController().setAnimation(new AnimationBuilder().loop(isMove ? "groundwalk" : "ground"));
                }
            }
            return PlayState.CONTINUE;
        }));
        data.addAnimationController(new AnimationController<>(this,"controller_wing",10, state -> {
            boolean isMove= !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F);
            if(this.phaseManager.getCurrentPhase()==null && this.phaseManager.getCurrentPhase().getPhase()!=FallenDragonPhase.SKYFALL){
                return PlayState.STOP;
            }
            state.getController().setAnimationSpeed(1.0D);
            if(isMove){
                //state.getController().setAnimationSpeed(2.0D );
                //state.getController().setAnimation(new AnimationBuilder().loop("fly"));
            }else {
                //state.getController().setAnimation(RawAnimation.begin().thenLoop(this.hasSnowball()? "moleman.move2":"moleman.idle"));
            }
            return PlayState.CONTINUE;
        }));
        data.addAnimationController(new AnimationController<>(this,"controller_head",10, state -> {
            if(this.phaseManager.getCurrentPhase()==null || this.isCharging() || this.phaseManager.getCurrentPhase().getPhase()!=FallenDragonPhase.SKYFALL){
                return PlayState.STOP;
            }
            if(this.phaseManager.getCurrentPhase()!=null && this.phaseManager.getCurrentPhase().getPhase()==FallenDragonPhase.SKYFALL && this.isFlame && this.phaseManager.getCurrentPhase().getFlyTargetLocation()!=null){
                state.getController().setAnimationSpeed(1.0D );
                state.getController().setAnimation(new AnimationBuilder().loop("flyingheadattack"));
            }else {
                state.getController().setAnimationSpeed(1.0D );
                state.getController().setAnimation(new AnimationBuilder().loop(!this.isOnGround() ? "flyinghead" : "groundhead"));
            }

            return PlayState.CONTINUE;
        }));
    }
    public void onSyncedDataUpdated(EntityDataAccessor<?> p_31136_) {
        if (DATA_PHASE.equals(p_31136_) && this.level.isClientSide) {
            this.phaseManager.setPhase(FallenDragonPhase.getById(this.getEntityData().get(DATA_PHASE)));
        }

        super.onSyncedDataUpdated(p_31136_);
    }


    public boolean addEffect(MobEffectInstance p_182394_, @Nullable Entity p_182395_) {
        return false;
    }

    protected boolean canRide(Entity p_31169_) {
        return false;
    }

    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public net.minecraftforge.entity.PartEntity<?>[] getParts() {
        return this.subEntities;
    }

    public void recreateFromPacket(ClientboundAddEntityPacket p_218825_) {
        super.recreateFromPacket(p_218825_);
        if (true) return; // Forge: Fix MC-158205: Moved into setId()
        FallenPartEntity[] aenderdragonpart = this.getSubEntities();

        for(int i = 0; i < aenderdragonpart.length; ++i) {
            aenderdragonpart[i].setId(i + p_218825_.getId());
        }

    }

    private FallenPartEntity[] getSubEntities() {
        return this.subEntities;
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }



    static class AttackGoal extends Goal {
        protected final FallenDragonEntity mob;
        private final double speedModifier;
        private final boolean followingTargetEvenIfNotSeen;
        private Path path;
        private double pathedTargetX;
        private double pathedTargetY;
        private double pathedTargetZ;
        private double probability;
        private int ticksUntilNextPathRecalculation;
        private int ticksUntilNextAttack;
        private final int attackInterval = 20;
        private long lastCanUseCheck;
        private static final long COOLDOWN_BETWEEN_CAN_USE_CHECKS = 20L;
        private int failedPathFindingPenalty = 0;
        private boolean canPenalize = false;

        public AttackGoal(FallenDragonEntity p_25552_, double p_25553_, boolean p_25554_) {
            this.mob = p_25552_;
            this.speedModifier = p_25553_;
            this.followingTargetEvenIfNotSeen = p_25554_;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        public boolean canUse() {
            long i = this.mob.level.getGameTime();
            if (i - this.lastCanUseCheck < 20L) {
                return false;
            } else {
                this.lastCanUseCheck = i;
                LivingEntity livingentity = this.mob.getTarget();
                if (livingentity == null) {
                    return false;
                } else if (!livingentity.isAlive()) {
                    return false;
                } else {
                    if (canPenalize) {
                        if (--this.ticksUntilNextPathRecalculation <= 0) {
                            this.path = this.mob.getNavigation().createPath(livingentity, 0);
                            this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
                            return this.path != null;
                        } else {
                            return true;
                        }
                    }
                    this.path = this.mob.getNavigation().createPath(livingentity, 0);
                    if (this.path != null) {
                        return true;
                    } else {
                        return this.getAttackReachSqr(livingentity) >= this.mob.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ()) && !this.mob.isCharging();
                    }
                }
            }
        }

        public boolean canContinueToUse() {
            LivingEntity livingentity = this.mob.getTarget();
            if (livingentity == null) {
                return false;
            } else if (!livingentity.isAlive()) {
                return false;
            } else if (!this.followingTargetEvenIfNotSeen) {
                return !this.mob.getNavigation().isDone();
            } else if (!this.mob.isWithinRestriction(livingentity.blockPosition())) {
                return false;
            } else {
                return !(livingentity instanceof Player) || !livingentity.isSpectator() && !((Player)livingentity).isCreative();
            }
        }

        public void start() {
            this.mob.getNavigation().moveTo(this.path, this.speedModifier);
            this.mob.setAggressive(true);
            this.ticksUntilNextPathRecalculation = 0;
            this.ticksUntilNextAttack = 0;
            this.probability=0.0F;
        }

        public void stop() {
            LivingEntity livingentity = this.mob.getTarget();
            if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingentity)) {
                this.mob.setTarget((LivingEntity)null);
            }

            this.mob.setAggressive(false);
            this.mob.getNavigation().stop();
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void tick() {
            LivingEntity livingentity = this.mob.getTarget();
            if (livingentity != null) {
                this.mob.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
                double d0 = this.mob.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());
                this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
                if ((this.followingTargetEvenIfNotSeen || this.mob.getSensing().hasLineOfSight(livingentity)) && this.ticksUntilNextPathRecalculation <= 0 && (this.pathedTargetX == 0.0D && this.pathedTargetY == 0.0D && this.pathedTargetZ == 0.0D || livingentity.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= 1.0D || this.mob.getRandom().nextFloat() < 0.05F)) {
                    this.pathedTargetX = livingentity.getX();
                    this.pathedTargetY = livingentity.getY();
                    this.pathedTargetZ = livingentity.getZ();
                    this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
                    if (this.canPenalize) {
                        this.ticksUntilNextPathRecalculation += failedPathFindingPenalty;
                        if (this.mob.getNavigation().getPath() != null) {
                            net.minecraft.world.level.pathfinder.Node finalPathPoint = this.mob.getNavigation().getPath().getEndNode();
                            if (finalPathPoint != null && livingentity.distanceToSqr(finalPathPoint.x, finalPathPoint.y, finalPathPoint.z) < 1)
                                failedPathFindingPenalty = 0;
                            else
                                failedPathFindingPenalty += 10;
                        } else {
                            failedPathFindingPenalty += 10;
                        }
                    }
                    if (d0 > 1024.0D) {
                        this.ticksUntilNextPathRecalculation += 10;
                        this.probability+=0.01F;
                    } else if (d0 > 256.0D) {
                        this.ticksUntilNextPathRecalculation += 5;
                        this.probability+=0.005F;
                    }

                    if (!this.mob.getNavigation().moveTo(livingentity, this.speedModifier)) {
                        this.ticksUntilNextPathRecalculation += 15;
                        this.probability+=0.05F;
                    }
                    if(this.probability>0.3F){
                        this.probability=0.3F;
                    }
                    if(this.ticksUntilNextPathRecalculation>10 && this.mob.random.nextFloat()<probability){
                        boolean attack=this.mob.random.nextFloat()<0.7F;
                        if(attack){
                            this.mob.initFall();
                        }else {
                            this.mob.initFlameAttack();
                        }
                    }

                    this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);
                }

                this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
                this.checkAndPerformAttack(livingentity, d0);
            }
        }

        protected void checkAndPerformAttack(LivingEntity p_25557_, double p_25558_) {
            double d0 = this.getAttackReachSqr(p_25557_);
            if (p_25558_ <= d0 && this.mob.attackMelee<=0) {
                this.mob.getLookControl().setLookAt(p_25557_, 30.0F, 30.0F);
                this.resetAttackCooldown();
                this.mob.swing(InteractionHand.MAIN_HAND);
            }

        }

        protected void resetAttackCooldown() {
            this.mob.attackMelee=45;
            this.mob.level.broadcastEntityEvent(this.mob,(byte) 8);
        }

        protected boolean isTimeToAttack() {
            return this.ticksUntilNextAttack <= 0;
        }

        protected int getTicksUntilNextAttack() {
            return this.ticksUntilNextAttack;
        }

        protected int getAttackInterval() {
            return this.adjustedTickDelay(20);
        }

        protected double getAttackReachSqr(LivingEntity p_25556_) {
            return (double)(this.mob.getBbWidth() * this.mob.getBbWidth() + p_25556_.getBbWidth()) + p_25556_.getBbWidth() * p_25556_.getBbWidth() * 2.0D;
        }

    }
}
