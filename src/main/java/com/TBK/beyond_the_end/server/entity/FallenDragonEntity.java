package com.TBK.beyond_the_end.server.entity;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.common.registry.BKEntityType;
import com.TBK.beyond_the_end.server.entity.phase.*;
import com.TBK.beyond_the_end.server.entity.projectile.FallenDragonFireball;
import com.TBK.beyond_the_end.server.network.PacketHandler;
import com.TBK.beyond_the_end.server.network.message.PacketTargetDragon;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
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

    public BlockPos targetPos=null;
    private int attackMelee=0;
    public int oFlapTime;
    public int flapTime;
    public boolean inWall;
    public int delayCharge = 0;
    public int dragonDeathTime;
    public float yRotA;
    private final Node[] nodes = new Node[24];
    private final int[] nodeAdjacency = new int[24];
    private final BinaryHeap openSet = new BinaryHeap();
    private boolean isBackAttack = false;
    public boolean clawAttack = false;
    public int chargedAnim = 0;
    public boolean isFlame=false;

    public int shootTime = 0;
    private boolean hasFlapped = false;

    public FallenDragonEntity(EntityType<? extends FallenDragonEntity> p_31096_, Level p_31097_) {
        super(p_31096_, p_31097_);
        this.navigation = new DragonGroundNavigation(this, this.level);
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
        this.moveControl = new MoveControl(this){

        };
        this.phaseManager=new FallenDragonPhaseManager(this);
        this.subEntities = new FallenPartEntity[]{this.head, this.neck, this.body, this.tail1, this.tail2, this.tail3, this.wing1, this.wing2};

        this.lookControl = new LookControl(this) {
            @Override
            public void tick() {
                // No hacer nada → ahorro de CPU
            }
        };
        this.setId(ENTITY_COUNTER.getAndAdd(this.subEntities.length + 1) + 1);
    }

    @Override
    public PathNavigation getNavigation() {
        return this.navigation;
    }

    @Override
    public void setId(int p_20235_) {
        super.setId(p_20235_);
        for (int i = 0; i < this.subEntities.length; i++) // Forge: Fix MC-158205: Set part ids to successors of parent mob id
            this.subEntities[i].setId(p_20235_ + i + 1);
    }

    @Override
    public float getStepHeight() {
        return 1.5F; // suficiente para terreno irregular, sin causar tanto escaneo
    }


    public boolean isFlapping() {
        return false;
    }

    public void onFlap() {
        if (this.level.isClientSide && !this.isSilent()) {
            this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENDER_DRAGON_FLAP, this.getSoundSource(), 5.0F, 0.8F + this.random.nextFloat() * 0.3F, false);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 500.0D)
                .add(Attributes.FOLLOW_RANGE,1000.0D)
                .add(Attributes.ATTACK_DAMAGE,10.0D)
                .add(Attributes.MOVEMENT_SPEED,0.23D)
                .add(Attributes.KNOCKBACK_RESISTANCE,1.0F);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(3,new HurtByTargetGoal(this));
        this.targetSelector.addGoal(9,new NearestAttackableTargetGoal<>(this, LivingEntity.class,false));
        this.goalSelector.addGoal(2,new AttackGoal(this,1.75F,false));

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
        if(this.shootTime>0){
            this.getNavigation().stop();
            this.shootTime--;
            if(this.getTarget()!=null){
                if(!this.level.isClientSide){
                    if(this.shootTime%6==0){
                        this.rotateToTarget(this.getTarget().getEyePosition());
                    }
                    if(this.shootTime==6){
                        float yawRad = (float)Math.toRadians(this.getYRot());
                        float sin = (float)Math.sin(yawRad);
                        float cos = (float)Math.cos(yawRad);
                        double d6 = this.getX() - 6.25D*sin;
                        double d7 = this.getY() + 1.5D;
                        double d8 = this.getZ() + 6.25D*cos;
                        double d9 = this.getTarget().getX() - d6;
                        double d10 = this.getTarget().getY() - d7;
                        double d11 = this.getTarget().getZ() - d8;
                        if (!this.isSilent()) {
                            this.level.levelEvent((Player)null, 1017, this.blockPosition(), 0);
                        }

                        FallenDragonFireball dragonfireball = new FallenDragonFireball(this.level, this, d9, d10 , d11);
                        dragonfireball.setOwner(this);
                        dragonfireball.moveTo(d6, d7, d8, 0.0F, 0.0F);
                        this.level.addFreshEntity(dragonfireball);
                    }
                }
            }

        }

        if(this.attackMelee>0){
            if(this.getTarget()!=null){
                this.rotateToTarget(this.getTarget().position());
            }
            this.getNavigation().stop();
            this.attackMelee--;
            if(this.attackMelee==2){
                if(this.isBackAttack){
                    this.tailArea();
                    this.isBackAttack=false;
                }else {
                    if(this.clawAttack){
                        this.particlePoof(8,this.blockPosition());
                        this.knockBack(this.level.getEntities(this, this.getBoundingBox().inflate(8.0D, 4.0D, 8.0D).move(0.0D, -2.0D, 0.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR),false);
                    }else {
                        if(this.getTarget()!=null && this.getTarget().distanceTo(this)<10){
                            this.clawAttack = true;
                            this.bite(this.level.getEntities(this, this.getTarget().getBoundingBox().inflate(3,2,3) , EntitySelector.NO_CREATIVE_OR_SPECTATOR));
                            this.particlePoof(3,this.getTarget().blockPosition());
                        }
                    }
                }
            }
            if(this.attackMelee==0){
                this.clawAttack=true;
            }
        }

        if(this.isCharging()){
            if(this.getTarget()!=null){
                if(this.delayCharge>20){
                    if(this.delayCharge%10==0){
                        LivingEntity target = this.getTarget();
                        this.getNavigation().moveTo(target,3.0D);
                    }
                }

                if(this.delayCharge%10==0){
                    this.rotateToTarget(this.getTarget().position());
                }
                double d0 = (this.body.getBoundingBox().minX + this.body.getBoundingBox().maxX) / 2.0D;
                double d1 = (this.body.getBoundingBox().minZ + this.body.getBoundingBox().maxZ) / 2.0D;
                this.delayCharge++;
                for (LivingEntity entity : this.level.getEntitiesOfClass(LivingEntity.class,this.getBoundingBox().inflate(7.5F))){
                    if(entity==this.getTarget()){
                        this.setCharging(false);
                        this.attackMelee = 45;
                        if(!this.level.isClientSide){
                            this.level.playSound(null,this.getTarget(),SoundEvents.ANVIL_HIT,SoundSource.HOSTILE,4.0F,1.0F);
                            this.level.broadcastEntityEvent(this, (byte) 8);
                            this.getTarget().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 4));
                            PacketHandler.sendToAllTracking(new PacketTargetDragon(this.getId(), -1, 1), this);
                        }
                        break;
                    }
                    if(!entity.is(entity)){
                        this.knockBack(entity,d0,d1);
                    }
                }
            }
        }
    }

    private void rotateToTarget(Vec3 pos) {
        if(!this.level.isClientSide ){
            Vec3 vec32 = pos.subtract(this.getEyePosition());
            double f5 = -Math.toDegrees(Math.atan2(vec32.y,Math.sqrt(vec32.x*vec32.x + vec32.z*vec32.z)));
            double f6 = Math.toDegrees(Math.atan2(vec32.z, vec32.x)) - 90.0F;
            float maxTurn = 20.0F; // velocidad máxima de giro por tick

            this.yBodyRot = Mth.approachDegrees(this.yBodyRot, (float) f6, maxTurn);
            this.setYRot(this.yBodyRot);
            this.setYHeadRot(this.yBodyRot);
            this.setXRot(Mth.approachDegrees(this.getXRot(), (float) f5, maxTurn));
        }
    }

    @Override
    protected int calculateFallDamage(float p_21237_, float p_21238_) {
        return 0;
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

    public void kill() {
        this.remove(Entity.RemovalReason.KILLED);
        this.gameEvent(GameEvent.ENTITY_DIE);
        if (this.dragonFight != null) {
            this.dragonFight.updateDragon(this);
            this.dragonFight.setDragonKilled(this);
        }
    }

    @Override
    protected void tryCheckInsideBlocks() {
        //super.tryCheckInsideBlocks();
    }

    @Override
    protected void checkInsideBlocks() {
        //super.checkInsideBlocks();
    }

    protected void tickDeath() {
        if (this.dragonFight != null) {
            this.dragonFight.updateDragon(this);
        }

        ++this.dragonDeathTime;

    }
    private void bite(List<Entity> p_31132_) {
        for(Entity entity : p_31132_) {
            if (entity instanceof LivingEntity) {
                entity.hurt(DamageSource.GENERIC, 20.0F);
                if(entity instanceof  Player player){
                    player.disableShield(true);
                }
            }
        }
    }
    private void knockBack(LivingEntity entity,double d0,double d1) {
        double d2 = entity.getX() - d0;
        double d3 = entity.getZ() - d1;
        double d4 = Math.max(d2 * d2 + d3 * d3, 0.1D);
        entity.push(d2 / d4 * 16.0D,(double)0.2F, d3 / d4 * 16.0D);
        entity.hurt(DamageSource.mobAttack(this), 20.0F);
        this.doEnchantDamageEffects(this, entity);

    }
    private void knockBack(List<Entity> p_31132_, boolean up) {
        double d0 = (this.getBoundingBox().minX + this.getBoundingBox().maxX) / 2.0D;
        double d1 = (this.getBoundingBox().minZ + this.getBoundingBox().maxZ) / 2.0D;

        for(Entity entity : p_31132_) {
            if (entity instanceof LivingEntity) {
                this.knockBack((LivingEntity) entity,d0,d1);
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
            if (this.canFly() && !this.isSilent() && !this.phaseManager.getCurrentPhase().isSitting() && --this.growlTime < 0) {
                this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENDER_DRAGON_GROWL, this.getSoundSource(), 2.5F, 0.8F + this.random.nextFloat() * 0.3F, false);
                this.growlTime = 200 + this.random.nextInt(200);
            }
        }

        if (this.isRespawn()) {
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
                if(this.flapTime==0){
                    this.flapTime=20;
                }
                if(this.flapTime>0){
                    this.flapTime--;
                }

                this.setYRot(Mth.wrapDegrees(this.getYRot()));
                if (this.isNoAi()) {
                    this.flapTime = 0;
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

        float f3 = this.hasModeFly() ? Mth.sin(this.getYRot() * ((float)Math.PI / 180F) - this.yRotA * 0.01F) : Mth.sin(180F * ((float)Math.PI / 180F) - this.yRotA * 0.01F) ;
        float f16 = this.hasModeFly() ? Mth.cos(this.getYRot() * ((float)Math.PI / 180F) - this.yRotA * 0.01F) : Mth.cos(180F * ((float)Math.PI / 180F) - this.yRotA * 0.01F);
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

            double[] adouble1 = this.getLatencyPos(12 + k * 2, 1.0F);
            float f17 = this.hasModeFly() ?  this.getYRot() * ((float)Math.PI / 180F) + this.rotWrap(adouble1[0] - adouble[0]) * ((float)Math.PI / 180F) : 180F * ((float)Math.PI / 180F) + this.rotWrap(adouble1[0] - adouble[0]) * ((float)Math.PI / 180F);;
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
        if (!this.level.isClientSide) {
            if (this.dragonFight != null) {
                this.dragonFight.updateDragon(this);
            }
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
        this.phaseManager.setPhase(FallenDragonPhase.DEATH);
        super.die(p_21014_);
    }

    private float rotWrap(double p_31165_) {
        return (float)Mth.wrapDegrees(p_31165_);
    }

    public boolean hurt(FallenPartEntity entity,DamageSource p_31122_, float p_31123_) {
        if(this.isRespawn() || this.phaseManager.getCurrentPhase().getPhase() == FallenDragonPhase.DEATH){
            return false;
        }
        boolean damage=attackDetermination(p_31122_);
        if(!this.level.isClientSide){
            if (!damage) {
                return false;
            } else {
                p_31123_ = this.phaseManager.getCurrentPhase().onHurt(p_31122_, p_31123_);

                if (p_31123_ < 0.01F) {
                    return false;
                } else {
                    if (p_31122_.getEntity() instanceof Player || p_31122_.isExplosion()) {
                        this.reallyHurt(p_31122_, p_31123_);
                        if(this.isDeadOrDying()){
                            FallenDragonFakeEntity fake = BKEntityType.FALLEN_DRAGON_FAKE.get().create(this.level);
                            fake.moveTo(this.position());
                            this.level.addFreshEntity(fake);
                            if (this.dragonFight != null) {
                                this.dragonFight.setDragonKilled(this);
                            }
                            this.remove(Entity.RemovalReason.KILLED);
                            this.gameEvent(GameEvent.ENTITY_DIE);
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }
    protected void reallyHurt(DamageSource p_31162_, float p_31163_) {
        super.hurt(p_31162_, p_31163_);
    }
    @Override
    public boolean hurt(DamageSource p_31113_, float p_31114_) {
        return this.hurt(this.body, p_31113_, p_31114_);
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
            if(this.getHealth()<this.getMaxHealth()*0.9F){
                if(this.hasModeFly()){
                    this.teleport(this.getX(),this.level.getHeight(Heightmap.Types.WORLD_SURFACE, (int) this.getX(), (int) this.getZ()),this.getZ());
                    this.setModeFly(false);
                    this.phaseManager.setPhase(FallenDragonPhase.HOLDING_PATTERN);
                    this.setDeltaMovement(Vec3.ZERO);
                    this.getNavigation().stop();
                }
            }
        }

        return true;

    }



    public boolean teleportTowards(Entity p_32501_) {

        Vec3 vec3 = new Vec3(this.getX() - p_32501_.getX(), this.getY(0.5D), this.getZ() - p_32501_.getZ());
        vec3 = vec3.normalize();
        double d1 = this.getX() + (this.random.nextDouble() - 0.5D) * this.level.random.nextInt(32,64) - vec3.x * 6.0D;
        double d2 = 85.0F;
        double d3 = this.getZ() + (this.random.nextDouble() - 0.5D) * this.level.random.nextInt(32,64) - vec3.z * 6.0D;

        return this.teleport(d1, d2, d3);
    }

    public boolean teleport(double p_32544_, double p_32545_, double p_32546_) {
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
    public void travel(Vec3 p_21280_) {

        super.travel(p_21280_);
    }

    @Override
    public void updateSwimming() {
        super.updateSwimming();
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
            this.setCharging(false);
            this.attackMelee=45;
        }else if(p_21375_==9){
            this.setCharging(true);
            this.attackMelee=45;
            this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENDER_DRAGON_GROWL, this.getSoundSource(), 2.5F, 0.8F + this.random.nextFloat() * 0.3F, false);
        }else if(p_21375_==62){
            this.attackMelee=23;
            this.isBackAttack=true;
        }else if(p_21375_==65){
            this.isFlame=false;
        } else if (p_21375_==67) {
            this.level.playSound(null,this,SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.HOSTILE,4.0F,1.0F);
        } else if (p_21375_==66) {
            this.phaseManager.setPhase(FallenDragonPhase.RESPAWN);
        } else if (p_21375_==70) {
            this.phaseManager.setPhase(FallenDragonPhase.FLAME);
        }  else{
            super.handleEntityEvent(p_21375_);
        }
    }

    public void particlePoof(int range,BlockPos center){
        ParticleOptions particleoptions = ParticleTypes.POOF;
        int i;
        float f1;
        i = Mth.ceil((float)Math.PI * range * range);
        f1 = range;

        for(int j = 0; j < i; ++j) {
            float f2 = this.random.nextFloat() * ((float)Math.PI * 2F);
            float f3 = Mth.sqrt(this.random.nextFloat()) * f1;
            double d0 = center.getX() + (double)(Mth.cos(f2) * f3);
            double d2 = center.getY();
            double d4 = center.getZ() + (double)(Mth.sin(f2) * f3);
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
        this.delayCharge = 0;
    }

    public void setModeFly(boolean mode) {
        this.entityData.set(MODE_FLY,mode);
    }


    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this,"controller",10, state -> {
            boolean isMove= !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F);
            if((!this.isOnGround() && !this.isCharging()) || this.phaseManager.getCurrentPhase().getPhase()==FallenDragonPhase.FLAME){
                float animTick = (float) (state.getAnimationTick() % 45); // se reinicia cada 2.25s
                float seconds = animTick / 20f;

                // 🔊 Flap en 0.5s y 1.75s (con margen de error)
                if ((Math.abs(seconds - 0.5f) < 0.05f || Math.abs(seconds - 1.75f) < 0.05f) && !hasFlapped) {
                    this.onFlap();
                    hasFlapped = true;
                } else if (seconds > 1.8f) {
                    hasFlapped = false;
                }
                if(this.phaseManager.getCurrentPhase().getPhase()==FallenDragonPhase.FLAME){
                    state.getController().setAnimationSpeed(1.0D);
                    state.getController().setAnimation(new AnimationBuilder().loop("flyingidlefire"));
                }else if(this.attackMelee<=0){

                    state.getController().setAnimationSpeed(1.0D);
                    state.getController().setAnimation(new AnimationBuilder().loop("flying"));
                }
            }else{
                if(this.isCharging() && this.attackMelee<=0 ){
                    state.getController().setAnimationSpeed(2.0D);
                    state.getController().setAnimation(new AnimationBuilder().loop("groundrun"));
                }else if(this.shootTime>0){
                    state.getController().setAnimationSpeed(this.isBackAttack ? 2.0D : 1.0D);
                    state.getController().setAnimation(new AnimationBuilder().playOnce("shootfirehead"));
                }else if(this.attackMelee>0){
                    state.getController().setAnimationSpeed(this.isBackAttack ? 2.0D : 1.0D);
                    state.getController().setAnimation(new AnimationBuilder().playOnce(this.isBackAttack ? "meleetail" : this.clawAttack ? "meleeclaw" : "meleebite"));
                }else {
                    state.getController().setAnimationSpeed(1.0D);
                    state.getController().setAnimation(new AnimationBuilder().loop(isMove ? "groundwalk" : "ground"));
                }
            }
            return PlayState.CONTINUE;
        }));
        data.addAnimationController(new AnimationController<>(this,"controller_head",10, state -> {
            if(this.phaseManager.getCurrentPhase()==null || this.attackMelee>0 || this.shootTime>0 || this.isCharging() || this.phaseManager.getCurrentPhase().getPhase()!=FallenDragonPhase.SKYFALL){
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

    public static class DragonGroundNavigation extends GroundPathNavigation {

        public final Level level;
        public DragonGroundNavigation(Mob mob, Level level) {
            super(mob, level);
            this.level = level;
        }

        @Override
        protected PathFinder createPathFinder(int maxVisitedNodes) {
            WalkNodeEvaluator nodeEvaluator = new WalkNodeEvaluator();
            nodeEvaluator.setCanPassDoors(true);        // Puede pasar por puertas
            nodeEvaluator.setCanOpenDoors(true);        // Puede abrirlas si tiene AI
            nodeEvaluator.setCanFloat(true);            // Puede pasar por agua poco profunda
            return new PathFinder(nodeEvaluator, maxVisitedNodes);
        }

        @Override
        protected boolean canUpdatePath() {
            // Evita recalcular cada tick
            return super.canUpdatePath();
        }

        @Override
        protected boolean canMoveDirectly(Vec3 p_186133_, Vec3 p_186134_) {
            return false;
        }

        @Override
        public boolean isStableDestination(BlockPos pos) {
            return this.level.getBlockState(pos.below()).getMaterial().isSolid();
        }

        @Override
        public void tick() {
            super.tick();
        }
    }


    static class AttackGoal extends Goal {
        public Strat strat = Strat.NORMAL;
        protected final FallenDragonEntity mob;
        private double speedModifier;
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
        public int timeSelectNewStrat=0;
        private int seeTime;
        private int countShoot;
        public AttackGoal(FallenDragonEntity p_25552_, double p_25553_, boolean p_25554_) {
            this.mob = p_25552_;
            this.speedModifier = p_25553_;
            this.followingTargetEvenIfNotSeen = p_25554_;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        public boolean canUse() {
            if(this.mob.isCharging()){
                return false;
            }
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
                        return this.getAttackReachSqr(livingentity) >= this.mob.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());
                    }
                }
            }
        }

        public boolean canContinueToUse() {
            LivingEntity livingentity = this.mob.getTarget();
            if (livingentity == null) {
                return false;
            }else if(this.mob.isCharging()){
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
            if (this.path != null) {
                this.mob.getNavigation().moveTo(this.path,this.speedModifier);
            }
            this.mob.setAggressive(true);
            this.ticksUntilNextPathRecalculation = 0;
            this.ticksUntilNextAttack = 0;
            this.probability = 0.0F;
        }

        public void stop() {
            LivingEntity livingentity = this.mob.getTarget();
            if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingentity)) {
                this.mob.setTarget((LivingEntity)null);
            }
            this.countShoot = 0;
            this.seeTime = 0;
            this.mob.setAggressive(false);
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void tick() {
            LivingEntity target = this.mob.getTarget();
            Strat current = this.strat;

            if (target != null) {
                this.timeSelectNewStrat++;
                double distSqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());

                if (this.strat == Strat.NORMAL) {
                    this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);

                    boolean shouldRepath = (this.pathedTargetX == 0.0D && this.pathedTargetY == 0.0D && this.pathedTargetZ == 0.0D)
                            || target.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= 4.0D;

                    if ((this.followingTargetEvenIfNotSeen || this.mob.getSensing().hasLineOfSight(target)) &&
                            this.ticksUntilNextPathRecalculation <= 0 &&
                            (shouldRepath || this.mob.getRandom().nextFloat() < 0.05F)) {

                        this.pathedTargetX = target.getX();
                        this.pathedTargetY = target.getY();
                        this.pathedTargetZ = target.getZ();

                        this.ticksUntilNextPathRecalculation = 20 + this.mob.getRandom().nextInt(20); // más estable

                        if (this.canPenalize) {
                            this.ticksUntilNextPathRecalculation += failedPathFindingPenalty;

                            if (this.mob.getNavigation().getPath() != null) {
                                var finalNode = this.mob.getNavigation().getPath().getEndNode();
                                if (finalNode != null && target.distanceToSqr(finalNode.x, finalNode.y, finalNode.z) < 1) {
                                    failedPathFindingPenalty = 0;
                                } else {
                                    failedPathFindingPenalty += 10;
                                }
                            } else {
                                failedPathFindingPenalty += 10;
                            }
                        }

                        if (distSqr > 1024.0D) {
                            this.ticksUntilNextPathRecalculation += 15;
                        } else if (distSqr > 256.0D) {
                            this.ticksUntilNextPathRecalculation += 5;
                        }


                        this.mob.getNavigation().moveTo(target,this.speedModifier);
                        this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);
                    }

                } else if (this.strat == Strat.SHOOT) {
                    this.tickShooting(target, distSqr);
                    if (this.mob.shootTime == 0) {
                        this.strat = Strat.NORMAL;
                    }

                } else if (this.strat == Strat.SHOOT_RUN) {
                    this.tickShooting(target, distSqr);
                    if (this.mob.shootTime == 0) {
                        this.strat = Strat.RUN;
                        this.mob.setCharging(true);
                    }
                }

                if (this.timeSelectNewStrat > 100) {
                    this.selectNextStrat(target, distSqr);
                    this.timeSelectNewStrat = 0;
                }

                if (this.strat == current) {
                    this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
                    this.checkAndPerformAttack(target, distSqr);
                }
            }
        }

        public void tickShooting(LivingEntity target, double distSqr) {
            boolean canSee = this.mob.getSensing().hasLineOfSight(target);
            boolean wasSeeing = this.seeTime > 0;

            if (canSee != wasSeeing) {
                this.seeTime = 0;
            }

            if (canSee) {
                ++this.seeTime;
            } else {
                --this.seeTime;
            }

            if (distSqr <= 1024 && this.seeTime >= 20) {
                this.mob.getNavigation().stop();
            } else if (!this.mob.getNavigation().isInProgress() && this.seeTime >= 0) {
                this.mob.getNavigation().moveTo(target,this.speedModifier);
            }

            if (this.mob.shootTime <= -1 && this.countShoot <= 0 && this.seeTime >= -60) {
                this.countShoot++;
                this.mob.shootTime = 33;
                PacketHandler.sendToAllTracking(new PacketTargetDragon(this.mob.getId(), -1, 3), this.mob);
            }
        }
        private void selectNextStrat(LivingEntity livingentity, double distance) {
            boolean hasShield = entityHasShield(livingentity);
            if(distance > 516.0D || this.mob.level.random.nextFloat()<0.4F){

                if(this.mob.level.random.nextFloat()<0.35F){
                    this.strat = Strat.SHOOT_FLY;
                    this.mob.setModeFly(true);
                    this.mob.teleportTowards(livingentity);
                    this.mob.phaseManager.setPhase(FallenDragonPhase.FLAME);
                    this.mob.phaseManager.getPhase(FallenDragonPhase.FLAME).setTarget(livingentity);
                    PacketHandler.sendToAllTracking(new PacketTargetDragon(this.mob.getId(),livingentity.getId(),4),this.mob);
                }else {
                    if(hasShield){
                        this.strat = Strat.RUN;
                        this.mob.setCharging(true);
                        if(!this.mob.level.isClientSide){
                            this.mob.level.broadcastEntityEvent(this.mob,(byte) 8);
                        }
                        this.mob.getNavigation().stop();
                    }else {
                        this.countShoot=0;
                        this.mob.shootTime=-1;
                        this.strat = this.mob.level.random.nextBoolean() ? Strat.SHOOT_RUN : Strat.SHOOT;
                    }
                }
            }else {
                this.strat = Strat.NORMAL;
            }
        }

        private boolean entityHasShield(LivingEntity living) {
            for (ItemStack stack : living.getHandSlots()) {
                if (stack.getItem() instanceof ShieldItem) {
                    return true;
                }
            }
            return false;
        }

        protected void checkAndPerformAttack(LivingEntity p_25557_, double p_25558_) {
            double d0 = this.getAttackReachSqr(p_25557_);
            if (p_25558_ <= d0 && this.mob.attackMelee<=0) {
                this.resetAttackCooldown();
            }

        }

        protected void resetAttackCooldown() {
            boolean isNormal = this.strat == Strat.NORMAL;
            if(!isNormal){
                this.strat=Strat.NORMAL;
                this.mob.setCharging(false);
            }
            this.mob.attackMelee=45;
            this.mob.level.broadcastEntityEvent(this.mob,(byte) 8);
            PacketHandler.sendToAllTracking(new PacketTargetDragon(this.mob.getId(),-1,isNormal ? 2 : 1),this.mob);
        }

        protected double getAttackReachSqr(LivingEntity p_25556_) {
            return (double)(this.mob.getBbWidth() * this.mob.getBbWidth() + p_25556_.getBbWidth()) + p_25556_.getBbWidth() * p_25556_.getBbWidth() * 4.0D;
        }

        enum Strat{
            SHOOT_RUN,
            SHOOT,
            RUN,
            SHOOT_FLY,
            NORMAL;
        }

    }
}
