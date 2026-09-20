package com.TBK.beyondtheend.server.entity;

import com.TBK.beyondtheend.BeyondTheEnd;
import com.TBK.beyondtheend.Config;
import com.TBK.beyondtheend.client.particle.BKParticles;
import com.TBK.beyondtheend.common.Util;
import com.TBK.beyondtheend.common.api.ICamShaker;
import com.TBK.beyondtheend.common.registry.BKEntityType;
import com.TBK.beyondtheend.common.registry.BTESounds;
import com.TBK.beyondtheend.server.capabilities.PortalPlayer;
import com.TBK.beyondtheend.server.entity.projectile.ChargeFollowing;
import com.TBK.beyondtheend.server.network.PacketHandler;
import com.TBK.beyondtheend.server.network.message.PacketNextActionJellyfish;
import com.TBK.beyondtheend.server.network.message.PacketPlaySound;
import com.TBK.beyondtheend.server.network.message.PacketActionDragon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class JellyfishEntity extends PathfinderMob implements ICamShaker {

    public int lastAction = -1;

    public int idleTimer = 0;
    public int lazerTimer = 0;
    public int spinTimer = 0;
    public int attackHeadTimer = 0;
    public int startLazerTimer = 0;
    public int startSummoningTimer = 0;
    public int summoningTimer = 0;
    public int shootLaserTimer = 0;
    public int maxJumpCount = 0;
    public int jumpCount = 0;
    public int waitInGroundTime = 0;
    private int scaledPlayers = 1;
    private int lastShieldHitTick = 0;
    public int deathTimer = 0;
    // Bloques por tick a los que el punto de mira del rayo persigue al objetivo (0.25 = 5 b/s).
    // Caminando (~4.3 b/s) acaba alcanzando al jugador; corriendo (~5.6 b/s) se le escapa.
    // LASER_CHARGE_TRACK_SPEED se usa durante la carga y LASER_TRACK_SPEED durante el haz.
    private static final double LASER_CHARGE_TRACK_SPEED = 0.25D;
    private static final double LASER_TRACK_SPEED = 0.25D;
    // Fases segun la vida restante: 1 (>66 %), 2 (>33 %) y 3 (el resto).
    private static final float PHASE_2_HEALTH = 0.66F;
    private static final float PHASE_3_HEALTH = 0.33F;
    // Ticks de espera en SPIN_AROUND antes de elegir la siguiente accion, por fase.
    private static final int[] PHASE_ACTION_DELAY = {100, 60, 30};
    // Maximo de minions vivos para poder volver a invocar, por fase.
    private static final int[] PHASE_MAX_MINIONS_TO_SUMMON = {0, 2, 3};
    // Esferas seguidoras por descarga cuando el objetivo esta lejos del pisoton, por fase.
    private static final int[] PHASE_VOLLEY_SIZE = {2, 3, 4};
    // En la fase 3, durante el rayo lanza BEAM_VOLLEY_SIZE esferas cada BEAM_VOLLEY_INTERVAL ticks.
    private static final int BEAM_VOLLEY_INTERVAL = 60;
    private static final int BEAM_VOLLEY_SIZE = 2;
    // Mientras el escudo esta activo regenera solo esta fraccion de lo normal (1/8).
    private static final double SHIELDED_REGEN_FACTOR = 0.125D;
    private static final UUID SCALING_MODIFIER_ID = UUID.fromString("5d7c1c3e-6f0e-4a55-9c3b-2f0d9a4e8b11");
    private static final EntityDataAccessor<Boolean> DATA_SHIELDED = SynchedEntityData.defineId(JellyfishEntity.class, EntityDataSerializers.BOOLEAN);
    private final double speed = 1.0F;
    private final double circleRadius = 300.0D;
    private double circlingAngle = 0.0F;
    private Vec3 circlingPosition;
    public boolean rot=false;
    public double offsetX=0.0D;
    public double offsetZ=0.0D;
    public Vec3 directionBlock=Vec3.ZERO;
    public Vec3 dragonDeath = Vec3.ZERO;
    public PhaseAttack actuallyPhase=PhaseAttack.SPIN_AROUND;
    public AnimationState idle = new AnimationState();
    public AnimationState idleGround = new AnimationState();
    public AnimationState idleJump = new AnimationState();
    public AnimationState spinning = new AnimationState();
    public AnimationState startSummoning = new AnimationState();
    public AnimationState summoning = new AnimationState();
    public AnimationState startLazer = new AnimationState();
    public AnimationState shootLaser = new AnimationState();
    public AnimationState jump = new AnimationState();
    public AnimationState land = new AnimationState();
    public AnimationState attackGround = new AnimationState();
    public int prepareTimer = 0;
    public int timerSpawn=0;
    public int nextTimer=0;
    public int timerInAir = 0;
    public int maxTimerInAir = 0;
    public int maxNextTimer=100;
    private int pulsingAnimation;
    private int cooldownHeadAttack = 0;
    private int pulsingAnimationO;
    private float prevScreenShakeAmount;
    private float screenShakeAmount;
    private final JellyfishPartEntity leg0;
    private final JellyfishPartEntity leg1;
    private final JellyfishPartEntity leg2;
    private final JellyfishPartEntity leg3;
    private final JellyfishPartEntity[] legs;

    private final BlockPos[] groundPos = new BlockPos[]{
            new BlockPos(0,90,0),
            new BlockPos(19,90,30),
            new BlockPos(100,90,78),
            new BlockPos(7,90,116),
            new BlockPos(-76,90,84),
            new BlockPos(-102,90,35),
            new BlockPos(-73,90,-51),
            new BlockPos(-32,90,-114),
            new BlockPos(59,90,91),
            new BlockPos(0,120,0)
    };
    public int positionLastGroundPos = -1;
    public int positionNextPosIndex = -1;

    public JellyfishEntity(EntityType<? extends PathfinderMob> p_21368_, Level p_21369_) {
        super(p_21368_, p_21369_);
        this.moveControl = new FlyingMoveControl(this, 90, true);
        this.lookControl = new SmoothSwimmingLookControl(this,10);
        this.leg0 = new JellyfishPartEntity(this,"leg0",5.0F,20.0F);
        this.leg1 = new JellyfishPartEntity(this,"leg1",5.0F,20.0F);
        this.leg2 = new JellyfishPartEntity(this,"leg2",5.0F,20.0F);
        this.leg3 = new JellyfishPartEntity(this,"leg3",5.0F,20.0F);

        this.legs = new JellyfishPartEntity[]{this.leg0,this.leg1,this.leg2,this.leg3};
        this.setId(ENTITY_COUNTER.getAndAdd(this.legs.length + 1) + 1);
    }

    @Override
    public void setId(int p_20235_) {
        super.setId(p_20235_);
        for (int i = 0; i < this.legs.length; i++) // Forge: Fix MC-158205: Set part ids to successors of parent mob id
            this.legs[i].setId(p_20235_ + i + 1);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SHIELDED, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_21484_) {
        super.addAdditionalSaveData(p_21484_);
        p_21484_.putInt("ScaledPlayers", this.scaledPlayers);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_21450_) {
        super.readAdditionalSaveData(p_21450_);
        if(p_21450_.contains("ScaledPlayers")){
            this.scaledPlayers = Math.max(1, p_21450_.getInt("ScaledPlayers"));
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0D)
                .add(Attributes.FOLLOW_RANGE,1000.0D)
                .add(Attributes.FLYING_SPEED,0.35D)
                .add(Attributes.KNOCKBACK_RESISTANCE,1.0F);
    }

    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {

    }

    protected PathNavigation createNavigation(Level pLevel) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, pLevel);
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(true);
        flyingpathnavigation.setCanPassDoors(true);
        return flyingpathnavigation;
    }

    @Override
    public boolean hurt(DamageSource source,float damage) {
        if(this.isShielded() && !source.isBypassInvul() && !source.isCreativePlayer()){
            if(!this.level.isClientSide){
                this.onShieldHit(source);
            }
            return false;
        }

        float damageOriginal=damage;
        if(this.lazerTimer<=0 && this.actuallyPhase!=PhaseAttack.GROUND){
            if(source.getEntity() instanceof Player player && PortalPlayer.get(player).isPresent()){
                damage = PortalPlayer.get(player).orElse(null).damageFinal(this,damage);
            }
            if(!this.level.isClientSide){
                if(BeyondTheEnd.jellyfishFightEvent!=null){
                    BeyondTheEnd.jellyfishFightEvent.updateJellyfish(this);

                }
            }
        }


        if(this.level.isClientSide){
            if(damageOriginal>=damage){
                this.particleHurt();
            }
        }


        return super.hurt(source,damage);
    }


    @Override
    protected float getSoundVolume() {
        return 5.0F;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource p_21239_) {
        return BTESounds.JELLYFISH_HURT.get();
    }

    public void particleHurt() {
        Random random = new Random();
        double box=this.getBbWidth();

        for(int i = 0 ; i<45 ; i++){
            double xp=this.getX() + random.nextDouble(-box,box);
            double yp=this.getY() + random.nextDouble(0.0d,this.getBbHeight());
            double zp=this.getZ() + random.nextDouble(-box,box);

            this.level.addParticle(ParticleTypes.DRAGON_BREATH, xp, yp, zp, 0.0F, 0.1F, 0.0F);
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(3,new AttackGoal(this,1.5F,true));
        this.goalSelector.addGoal(2,new JellyfishSummoning(this));

        this.goalSelector.addGoal(1,new JellyfishLazer(this));
        this.targetSelector.addGoal(1,new HurtByTargetGoal(this));
        this.targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this, Zombie.class,false));
        this.targetSelector.addGoal(4,new NearestAttackableTargetGoal<>(this, Player.class,false));
    }



    @Override
    public void tick() {
        if(this.cooldownHeadAttack>0){
            this.cooldownHeadAttack--;
            if(this.cooldownHeadAttack == 0){
                if(this.actuallyPhase==PhaseAttack.GROUND){
                    this.actuallyPhase = PhaseAttack.PREPARE_JUMP;
                    this.prepareTimer = 10;
                    if(!this.level.isClientSide){
                        int randomPos = this.pickJumpTarget();
                        this.positionNextPosIndex = randomPos;
                        PacketHandler.sendToAllTracking(new PacketNextActionJellyfish(this.getId(),randomPos,6),this);
                    }
                }
            }
        }

        this.setNoGravity(this.actuallyPhase != PhaseAttack.JUMP && this.actuallyPhase != PhaseAttack.PREPARE_JUMP);
        super.tick();

        if(this.lazerTimer>0){
            this.lazerTimer--;

            if(!this.level.isClientSide){
                this.trackLaserTarget(LASER_TRACK_SPEED);
                if(this.getFightPhase() >= 3 && this.lazerTimer % BEAM_VOLLEY_INTERVAL == 0){
                    this.launchOrbVolley(BEAM_VOLLEY_SIZE);
                }
                List<HitResult> hitResults = Util.internalRaycastForAllEntity(this.level,this,this.getEyePosition(),this.directionBlock,true,4.0F);
                for (HitResult hitResult : hitResults){
                    if(hitResult.getType() == HitResult.Type.ENTITY){
                        Entity entity = ((EntityHitResult)hitResult).getEntity();
                        if(entity instanceof LivingEntity living){
                            //ChargeBeam
                            living.hurt(DamageSource.GENERIC,50.0F * this.getDamageScale());
                            living.hurt(DamageSource.mobAttack(this),25.0F * this.getDamageScale());
                        }
                    }
                }
            }else {
                if(this.screenShakeAmount<=0.0F){
                    this.screenShakeAmount=0.5F;
                }
            }

            if(this.lazerTimer==0){
                this.directionBlock=Vec3.ZERO;
                this.actuallyPhase=PhaseAttack.LAND;
                if(!this.level.isClientSide){
                    this.level.broadcastEntityEvent(this,(byte) 8);
                    this.positionLastGroundPos = this.random.nextInt(0,8);
                    this.beginGroundSequence();
                    PacketHandler.sendToAllTracking(new PacketNextActionJellyfish(this.getId(),this.positionLastGroundPos,4),this);
                }else {
                    this.shootLaser.stop();
                }
            }
        }

        if(this.actuallyPhase==PhaseAttack.SPIN_AROUND){
            this.nextTimer++;
        }

        if(!this.level.isClientSide){
            this.tickShield();
            this.tickPlayerScaling();
            this.tickRegeneration();

            if(this.nextTimer>this.getActionDelay() && this.actuallyPhase==PhaseAttack.SPIN_AROUND && this.getTarget()!=null){
                // Elige entre embestida (1), laser (2) e invocacion (3) sin repetir la anterior.
                // La invocacion solo entra en la tirada si quedan pocos minions vivos (segun la fase).
                List<Integer> candidates = new ArrayList<>();
                candidates.add(1);
                candidates.add(2);
                if(this.canSummonMinions()){
                    candidates.add(3);
                }
                candidates.remove(Integer.valueOf(this.lastAction));

                int nextAction = candidates.get(this.level.random.nextInt(candidates.size()));
                this.lastAction = nextAction;
                this.nextTimer = 0;
                this.setActionForID(nextAction);
                PacketHandler.sendToAllTracking(new PacketNextActionJellyfish(this.getId(),0,nextAction),this);
            }
        }

        if(this.attackHeadTimer>0){
            this.attackHeadTimer--;
            if(this.attackHeadTimer==8){
                if(!this.level.isClientSide){
                    this.level.broadcastEntityEvent(this,(byte) 3);
                }
                this.knockBack(this.level.getEntitiesOfClass(Entity.class,this.getBoundingBox().inflate(10.0D).move(0.0D,-30.0D,0.0D),e->e!=this && !this.isAlliedTo(e)));
            }else if(this.attackHeadTimer == 0){
                this.cooldownHeadAttack = 150;
                this.attackGround.stop();
            }
        }
        if(this.spinTimer > 0){
            this.spinTimer--;
            if(this.spinTimer == 0){
                this.actuallyPhase=PhaseAttack.LAND;
                if(!this.level.isClientSide){
                    this.positionLastGroundPos = this.random.nextInt(0,8);
                    this.beginGroundSequence();
                    PacketHandler.sendToAllTracking(new PacketNextActionJellyfish(this.getId(),this.positionLastGroundPos,4),this);
                }else {
                    this.spinning.stop();
                }
            }
        }

        if(this.startLazerTimer>0){
            if(this.startLazerTimer==110){
                this.level.playLocalSound(this.getX(), this.getY(), this.getZ(),BTESounds.JELLYFISH_CHARGE.get(), this.getSoundSource(), 5.0F, this.getVoicePitch(), false);
            }
            if(this.level.isClientSide){
                double box=this.getBbWidth();

                for (int i=0;i<3;i++){
                    Random random = new Random();
                    double xp=this.getX() + random.nextDouble(-box,box);
                    double yp=this.getY() + random.nextDouble(0.0d,this.getBbHeight());
                    double zp=this.getZ() + random.nextDouble(-box,box);

                    Vec3 delta = new Vec3(this.getX()-xp,this.getY()-yp,this.getZ()-zp).normalize().scale(0.6F);
                    this.level.addParticle(BKParticles.FLAME_PARTICLE.get(),xp + random.nextInt(-2,2),yp,zp + random.nextInt(-2,2),delta.x,delta.y,delta.z);
                }
            }
            this.startLazerTimer--;
            if(!this.level.isClientSide){
                this.trackLaserTarget(LASER_CHARGE_TRACK_SPEED);
            }
            if(this.startLazerTimer==0){
                this.lazerTimer=180;
                if(!this.level.isClientSide){
                    this.level.broadcastEntityEvent(this,(byte) 32);
                    PacketHandler.sendToAllTracking(new PacketPlaySound(this.getId()),this);
                }else{
                    this.level.playLocalSound(this.getX(), this.getY(), this.getZ(),BTESounds.JELLYFISH_EXPLOSION.get(), this.getSoundSource(), 10.0F, this.getVoicePitch(), false);
                    this.startLazer.stop();
                }
            }
        }

        if(this.startSummoningTimer>0){
            this.startSummoningTimer--;

            if(this.startSummoningTimer==0){
                this.summoningTimer=500;
                if(!this.level.isClientSide){
                    this.level.broadcastEntityEvent(this,(byte) 64);
                }
            }
        }

        if(this.summoningTimer>0){
            if(this.summoningTimer>45){
                if((45+this.summoningTimer)%75==0 || this.summoningTimer==46){
                    JellyfishMinionEntity minion = new JellyfishMinionEntity(BKEntityType.JELLYFISH_MINION.get(), this.level);
                    minion.setPos(this.position().add(0.0F,-6.0D,0.0D));
                    minion.actuallyPhase= JellyfishMinionEntity.PhaseAttack.SPAWN;
                    minion.jellyfish = this;
                    minion.rot=this.level.random.nextBoolean();
                    minion.heightOffset = this.level.random.nextDouble() * 40.0D * this.level.random.nextInt(-1,1);
                    this.level.addFreshEntity(minion);
                    if(this.getTarget()!=null){
                        minion.setTarget(this.getTarget());
                    }
                }
            }

            if(this.level.isClientSide){
                if(this.summoningTimer%40==0 || this.summoningTimer==500){
                    this.level.playLocalSound(this.getX(), this.getY(), this.getZ(),BTESounds.JELLYFISH_SUMMON.get(), this.getSoundSource(), 5.0F, this.getVoicePitch(), false);
                }
                if(this.summoningTimer%5 == 0 ){
                    double x = this.getX();
                    double y = this.getY();
                    double z = this.getZ();
                    for (int i=0;i<8;i++){
                        Random random1 = new Random();
                        this.level.addParticle(BKParticles.MIST_PARTICLE.get(),x + random.nextInt(-2,2),y,z + random.nextInt(-2,2),random1.nextFloat(-0.5F,0.5F),-0.6F,random1.nextFloat(-0.5F,0.5F));
                    }
                }
            }
            this.summoningTimer--;

            if(this.summoningTimer==0){
                this.actuallyPhase=PhaseAttack.LAND;
                if(!this.level.isClientSide){
                    this.positionLastGroundPos = this.random.nextInt(0,8);
                    this.beginGroundSequence();
                    PacketHandler.sendToAllTracking(new PacketNextActionJellyfish(this.getId(),this.positionLastGroundPos,4),this);
                }else {
                    this.summoning.stop();
                }
            }
        }


        if(this.level.isClientSide){
            this.clientAnim();
            this.tickShieldParticles();
        }

        if(!this.level.isClientSide){
            if(BeyondTheEnd.jellyfishFightEvent!=null){
                BeyondTheEnd.jellyfishFightEvent.updateJellyfish(this);
            }
        }

        this.refreshDimensions();
    }

    public int getFightPhase() {
        float health = this.getHealth() / this.getMaxHealth();
        if(health > PHASE_2_HEALTH){
            return 1;
        }
        return health > PHASE_3_HEALTH ? 2 : 3;
    }

    private int getActionDelay() {
        return PHASE_ACTION_DELAY[this.getFightPhase() - 1];
    }

    public float getDamageScale() {
        return 1.0F + (this.scaledPlayers - 1) * Config.JELLYFISH_DAMAGE_PER_EXTRA_PLAYER.get().floatValue();
    }

    public boolean isShielded() {
        return this.entityData.get(DATA_SHIELDED);
    }

    private int countAliveMinions() {
        return ((ServerLevel)this.level).getEntities(BKEntityType.JELLYFISH_MINION.get(), LivingEntity::isAlive).size();
    }

    private void tickShield() {
        if(this.tickCount % 5 != 0){
            return;
        }
        boolean shielded = Config.JELLYFISH_SHIELD_WITH_MINIONS.get() && !this.isDeadOrDying() && this.countAliveMinions() > 0;
        if(shielded != this.isShielded()){
            this.entityData.set(DATA_SHIELDED, shielded);
        }
    }

    private void onShieldHit(DamageSource source) {
        if(!(this.level instanceof ServerLevel serverLevel) || this.tickCount - this.lastShieldHitTick < 6){
            return;
        }
        this.lastShieldHitTick = this.tickCount;

        Vec3 center = this.position().add(0.0D, this.getBbHeight() * 0.5D, 0.0D);
        Vec3 from = source.getSourcePosition();
        if(from == null && source.getEntity() != null){
            from = source.getEntity().position();
        }
        Vec3 direction = from == null ? new Vec3(0.0D, 1.0D, 0.0D) : from.subtract(center);
        if(direction.lengthSqr() < 1.0E-4D){
            direction = new Vec3(0.0D, 1.0D, 0.0D);
        }
        Vec3 point = center.add(direction.normalize().multiply(this.getBbWidth() * 0.55D, this.getBbHeight() * 0.55D, this.getBbWidth() * 0.55D));

        serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z, 14, 0.8D, 0.8D, 0.8D, 0.25D);
        serverLevel.playSound(null, point.x, point.y, point.z, SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 1.5F, 0.5F);
    }

    private void tickShieldParticles() {
        if(!this.isShielded() || this.tickCount % 2 != 0){
            return;
        }
        double radiusXZ = this.getBbWidth() * 0.6D;
        double radiusY = this.getBbHeight() * 0.6D;
        Vec3 center = this.position().add(0.0D, this.getBbHeight() * 0.5D, 0.0D);

        for(int i = 0; i < 8; i++){
            double u = this.random.nextDouble() * 2.0D - 1.0D;
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double ring = Math.sqrt(1.0D - u * u);
            ParticleOptions particle = this.random.nextInt(4) == 0 ? ParticleTypes.END_ROD : ParticleTypes.ELECTRIC_SPARK;
            this.level.addAlwaysVisibleParticle(particle, center.x + Math.cos(angle) * ring * radiusXZ, center.y + u * radiusY, center.z + Math.sin(angle) * ring * radiusXZ, 0.0D, 0.0D, 0.0D);
        }
    }

    private void tickRegeneration() {
        if(this.tickCount % 20 != 0 || this.isDeadOrDying()){
            return;
        }
        double percent = Config.JELLYFISH_REGEN_PERCENT_PER_SECOND.get();
        if(this.isShielded()){
            percent *= SHIELDED_REGEN_FACTOR;
        }
        if(percent <= 0.0D || this.getHealth() >= this.getMaxHealth()){
            return;
        }
        // Solo regenera fuera de las ventanas de dano real (suelo y rayo).
        if(this.actuallyPhase == PhaseAttack.GROUND || this.lazerTimer > 0){
            return;
        }
        this.heal((float)(this.getMaxHealth() * percent / 100.0D));
    }

    private void tickPlayerScaling() {
        if(this.tickCount % 20 != 0 || this.isDeadOrDying()){
            return;
        }
        int players = 0;
        for(ServerPlayer player : ((ServerLevel)this.level).players()){
            if(player.isAlive() && !player.isSpectator() && player.distanceToSqr(0.0D, 128.0D, 0.0D) < 300.0D * 300.0D){
                players++;
            }
        }
        players = Mth.clamp(players, 1, Config.JELLYFISH_MAX_SCALED_PLAYERS.get());
        // Solo sube: que un jugador muera o se marche no encoge al jefe a mitad de combate.
        if(players > this.scaledPlayers){
            this.scaledPlayers = players;
            this.applyPlayerScaling();
        }
    }

    private void applyPlayerScaling() {
        AttributeInstance maxHealth = this.getAttribute(Attributes.MAX_HEALTH);
        if(maxHealth == null){
            return;
        }
        float ratio = this.getHealth() / this.getMaxHealth();
        maxHealth.removeModifier(SCALING_MODIFIER_ID);
        double bonus = (this.scaledPlayers - 1) * Config.JELLYFISH_HEALTH_PER_EXTRA_PLAYER.get();
        if(bonus > 0.0D){
            maxHealth.addPermanentModifier(new AttributeModifier(SCALING_MODIFIER_ID, "Player count scaling", bonus, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        this.setHealth(ratio * this.getMaxHealth());
    }

    private void launchOrbVolley(int count) {
        LivingEntity target = this.getTarget();
        if(!(target instanceof Player) || !target.isAlive()){
            return;
        }

        Vec3 origin = this.getEyePosition();
        for(int i = 0; i < count; i++){
            ChargeFollowing orb = new ChargeFollowing(this.level, this, target);
            orb.setPos(origin.x + (this.random.nextDouble() - 0.5D) * 8.0D, origin.y + (this.random.nextDouble() - 0.5D) * 4.0D, origin.z + (this.random.nextDouble() - 0.5D) * 8.0D);
            orb.setDamageScale(this.getDamageScale());
            this.level.addFreshEntity(orb);
        }
        this.level.playSound(null, this.getX(), this.getY(), this.getZ(), BTESounds.JELLYFISH_SHOOT2.get(), SoundSource.HOSTILE, 5.0F, 1.0F);
    }

    private int pickJumpTarget() {
        int pos = this.level.random.nextInt(0,8);
        if(pos == this.positionLastGroundPos){
            pos = (pos + 1) % 8;
        }
        return pos;
    }

    private void beginGroundSequence() {
        this.jumpCount = 0;
        this.maxJumpCount = this.positionLastGroundPos % 2 == 0 ? 1 : 3;
    }

    private void trackLaserTarget(double trackSpeed) {
        LivingEntity target = this.getTarget();
        if(target == null || !target.isAlive()){
            return;
        }

        if(this.directionBlock.equals(Vec3.ZERO)){
            this.directionBlock = target.position();
        }else {
            Vec3 toTarget = target.position().subtract(this.directionBlock);
            double distance = toTarget.length();
            if(distance > 1.0E-3D){
                this.directionBlock = this.directionBlock.add(toTarget.scale(Math.min(distance, trackSpeed) / distance));
            }
        }

        if(this.tickCount % 2 == 0){
            PacketHandler.sendToAllTracking(new PacketActionDragon(this.getId(), Mth.floor(this.directionBlock.x), Mth.floor(this.directionBlock.y), Mth.floor(this.directionBlock.z)),this);
        }
    }

    private void knockBack(List<Entity> p_31132_) {
        double d0 = (this.getBoundingBox().minX + this.getBoundingBox().maxX) / 2.0D;
        double d1 = (this.getBoundingBox().minZ + this.getBoundingBox().maxZ) / 2.0D;

        for(Entity entity : p_31132_) {
            if (entity instanceof LivingEntity) {
                double d2 = entity.getX() - d0;
                double d3 = entity.getZ() - d1;
                double d4 = Math.max(d2 * d2 + d3 * d3, 0.1D);
                entity.push(d2 / d4 * 16.0D,(double)0.2F, d3 / d4 * 16.0D);
                entity.hurt(DamageSource.mobAttack(this), 20.0F * this.getDamageScale());
            }
        }
    }

    private void particleChargedPoof() {
        ParticleOptions particleoptions = BKParticles.FLAME_PARTICLE.get();
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

    private void tickPart(JellyfishPartEntity p_31116_, double p_31117_, double p_31118_, double p_31119_) {
        p_31116_.setPos(this.getX() + p_31117_, this.getY() + p_31118_, this.getZ() + p_31119_);
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    public float getHeartAnimation(float p_219470_) {
        return Mth.lerp(p_219470_, (float)this.pulsingAnimationO, (float)this.pulsingAnimation) / 30.0F;
    }

    public float getLazerAnim(float p_219470_) {
        return this.lazerTimer>0 ? 1.0F  :(110.0F-Mth.lerp(p_219470_, (float)this.startLazerTimer, (float)this.startLazerTimer)) / 110.0F;
    }
    public Vec3 calculateJumpVelocity(BlockPos from, BlockPos to, int ticks) {
        double g = 0.08; // gravedad aproximada de Minecraft

        double dx = to.getX() + 0.5 - (from.getX() + 0.5); // centrado en el bloque
        double dy = to.getY() - from.getY();
        double dz = to.getZ() + 0.5 - (from.getZ() + 0.5);

        double vx = dx / ticks;
        double vz = dz / ticks;
        double vy = (dy + 0.5 * g * ticks * ticks) / ticks;

        return new Vec3(vx, vy, vz);
    }

    @Override
    public boolean shouldDiscardFriction() {
        return this.actuallyPhase == PhaseAttack.JUMP || this.actuallyPhase == PhaseAttack.PREPARE_JUMP;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if(this.actuallyPhase == PhaseAttack.GROUND){
            if(this.positionLastGroundPos!=-1){
                this.setPos(Vec3.atCenterOf(this.groundPos[this.positionLastGroundPos]).add(0,5,0));
                Vec3[] avec3 = new Vec3[this.legs.length];

                for(int j = 0; j < this.legs.length; ++j) {
                    avec3[j] = new Vec3(this.legs[j].getX(), this.legs[j].getY(), this.legs[j].getZ());
                }

                // Rotación enY ( horizontal)
                float yawRad = (float)Math.toRadians(this.getYRot());
                float sin = (float)Math.sin(yawRad);
                float cos = (float)Math.cos(yawRad);

                // Pierna 1: adelante derecha
                double leg0X =  ( 20 * cos) - ( 5 * sin);
                double leg0Z =  ( 20 * sin) + ( 5 * cos);

                // Pierna 2: atrás izquierda
                double leg1X =  ( -20 * cos) + ( 5 * sin);
                double leg1Z =  ( -20 * sin) - ( 5 * cos);

                // Pierna 3: atrás derecha
                double leg2X =  - ( 20 * cos) - ( 5 * sin);
                double leg2Z =  - ( 20 * sin) + ( 5 * cos);


                double leg3X =  - ( -20 * cos) + ( 5 * sin);
                double leg3Z =  - ( -20 * sin) - ( 5 * cos);

                this.tickPart(this.leg0,leg0X,-25,leg0Z);
                this.tickPart(this.leg1,leg1X,-25,leg1Z);
                this.tickPart(this.leg2,leg2X,-25,leg2Z);
                this.tickPart(this.leg3,leg3X,-25,leg3Z);


                for(int l = 0; l < this.legs.length; ++l) {
                    this.legs[l].xo = avec3[l].x;
                    this.legs[l].yo = avec3[l].y;
                    this.legs[l].zo = avec3[l].z;
                    this.legs[l].xOld = avec3[l].x;
                    this.legs[l].yOld = avec3[l].y;
                    this.legs[l].zOld = avec3[l].z;
                }
                if(this.waitInGroundTime++>200){
                    if(this.jumpCount<this.maxJumpCount){
                        this.prepareTimer = 10;
                        if(!this.level.isClientSide){
                            this.actuallyPhase = PhaseAttack.PREPARE_JUMP;
                            int randomPos = this.pickJumpTarget();
                            this.positionNextPosIndex = randomPos;
                            PacketHandler.sendToAllTracking(new PacketNextActionJellyfish(this.getId(),randomPos,6),this);
                        }
                        this.cooldownHeadAttack=0;
                        this.waitInGroundTime=0;
                        this.jumpCount++;
                    }else {
                        this.waitInGroundTime=0;
                        this.prepareTimer = 10;
                        this.positionNextPosIndex = 9;
                        if(!this.level.isClientSide){
                            this.actuallyPhase = PhaseAttack.PREPARE_JUMP;
                            PacketHandler.sendToAllTracking(new PacketNextActionJellyfish(this.getId(),9,6),this);
                        }
                        this.jumpCount = 0;
                    }
                }
            }
            if(this.getTarget()!=null && this.cooldownHeadAttack<=0 && this.attackHeadTimer <=0){
                double d0 = this.distanceTo(this.getTarget());
                if(d0<30){
                    if(!this.level.isClientSide){
                        this.attackHeadTimer = 34;
                        this.level.broadcastEntityEvent(this,(byte) 1);
                    }
                }else if(!this.level.isClientSide){
                    // Objetivo fuera del alcance del pisoton: descarga de esferas seguidoras.
                    this.launchOrbVolley(PHASE_VOLLEY_SIZE[this.getFightPhase() - 1]);
                    this.cooldownHeadAttack = 150;
                }
            }
        }

        if(this.actuallyPhase == PhaseAttack.PREPARE_JUMP){
            this.prepareTimer--;
            if(this.prepareTimer==2){
                if(this.positionNextPosIndex != -1){
                    BlockPos start = this.blockPosition();
                    BlockPos end = this.groundPos[this.positionNextPosIndex];
                    Vec3 velocity = this.calculateJumpVelocity(start, end, 70);

                    this.setDeltaMovement(velocity);
                }
            }
            if(this.prepareTimer==0){
                this.setActionForID(7);
                if (!this.level.isClientSide) {
                    PacketHandler.sendToAllTracking(new PacketNextActionJellyfish(this.getId(), 0, 7), this);
                }
            }

            if(this.prepareTimer>2){
                if(this.positionLastGroundPos != -1){
                    this.setPos(Vec3.atCenterOf(this.groundPos[this.positionLastGroundPos]).add(0,5,0));
                }
            }
        }

        if(this.actuallyPhase == PhaseAttack.JUMP){
            if(this.positionNextPosIndex!=-1){
                BlockPos nextPos = this.groundPos[this.positionNextPosIndex];
                if(nextPos.distSqr(this.blockPosition())<20 || this.onGround){
                    if(this.positionNextPosIndex==9){
                        this.setActionForID(0);
                        this.positionLastGroundPos = this.positionNextPosIndex;
                        if(!this.level.isClientSide){
                            PacketHandler.sendToAllTracking(new PacketNextActionJellyfish(this.getId(), (int) (40 + 100 * this.random.nextFloat()),0),this);
                        }
                    }else {
                        this.setActionForID(4);
                        this.positionLastGroundPos = this.positionNextPosIndex;
                        if(!this.level.isClientSide){
                            this.maxJumpCount = this.positionNextPosIndex % 2 == 0 ? 1 : 3;
                            PacketHandler.sendToAllTracking(new PacketNextActionJellyfish(this.getId(),this.positionNextPosIndex,4),this);
                        }
                    }
                }
            }
        }

        if(this.actuallyPhase == PhaseAttack.LAND && this.positionLastGroundPos!=-1){
            Vec3 direction = Vec3.atCenterOf(this.groundPos[this.positionLastGroundPos]).subtract(this.position()).normalize().scale(0.6F);
            this.setDeltaMovement(direction);
            if(this.distanceToSqr(Vec3.atCenterOf(this.groundPos[this.positionLastGroundPos]))<21){
                this.actuallyPhase = PhaseAttack.GROUND;
                this.setPos(Vec3.atCenterOf(this.groundPos[this.positionLastGroundPos]).add(0,10,0));
                this.setActionForID(5);
                if(!this.level.isClientSide){
                    PacketHandler.sendToAllTracking(new PacketNextActionJellyfish(this.getId(),0,5),this);
                }
            }
        }

        if(this.actuallyPhase == PhaseAttack.GROUND){
            for (JellyfishPartEntity leg : this.legs) {
                leg.tick();
                leg.setSize(leg.size0);
            }
        }else{
            for (JellyfishPartEntity leg : this.legs) {
                leg.tick();
                leg.setSize(EntityDimensions.scalable(0, 0));
            }
        }

        if(!this.level.isClientSide){
            if(this.actuallyPhase!=PhaseAttack.ATTACK  || this.getTarget()==null){
                this.spinAround();
            }
        }

        if(this.actuallyPhase==PhaseAttack.SPAWN){
            this.tickSpawn();
        }
    }

    public void tickSpawn(){
        if(this.timerSpawn<200){
            this.timerSpawn++;
        }
        this.setDeltaMovement(Vec3.atCenterOf(this.groundPos[0]).subtract(this.position()).normalize().scale(0.05));
        Vec3 vec32 = this.dragonDeath.subtract(this.getEyePosition());
        double f5 = -Math.toDegrees(Math.atan2(vec32.y,Math.sqrt(vec32.x*vec32.x + vec32.z*vec32.z)));
        double f6 = Math.toDegrees(Math.atan2(vec32.z, vec32.x)) - 90.0F;
        this.yHeadRot=(float)f6;
        this.setYHeadRot((float) f6);
        this.yBodyRot= (float) (f6);
        this.setYRot((float) f6);
        this.setXRot((float) f5);
        this.setRot(this.getYRot(),this.getXRot());
        if(!this.level.isClientSide){
            if(this.tickCount % 20 == 0){
                this.level.broadcastEntityEvent(this,(byte) 67);
            }
        }
    }

    public void tickDeath(){
        this.deathTimer++;
        if(!this.level.isClientSide && this.deathTimer>90){
            if(BeyondTheEnd.jellyfishFightEvent!=null){
                BeyondTheEnd.jellyfishFightEvent.setJellyFishKilled(this);
            }
            this.level.broadcastEntityEvent(this, (byte)60);
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    @Override
    public void checkDespawn() {

    }

    public void spinAround(){
        LivingEntity target = this.getTarget();
        Vec3 direction;
        if(this.actuallyPhase==PhaseAttack.SPIN_AROUND ){
            this.circlingAngle += this.rot ? 0.05F : -0.05F;
            offsetX = Math.cos(this.circlingAngle) * this.circleRadius;
            offsetZ = Math.sin(this.circlingAngle) * this.circleRadius;

            if (target != null) {
                double heightOffset = this.calculateHeightOffset(target);
                this.circlingPosition = new Vec3(target.getX() + offsetX, target.getY() + heightOffset, target.getZ() + offsetZ);
                direction = this.circlingPosition.subtract(this.position()).normalize();
                this.setDeltaMovement(direction.scale(this.speed));

                this.rotateTowardsTarget(target);
            }else {
                BlockPos pos = new BlockPos(0,this.level.getHeight(Heightmap.Types.WORLD_SURFACE,0,0),0);
                double heightOffset = this.calculateHeightOffset(pos);
                this.circlingPosition = new Vec3(pos.getX() + offsetX, pos.getY() + heightOffset, pos.getZ() + offsetZ);
                direction = this.circlingPosition.subtract(this.position()).normalize();
                this.setDeltaMovement(direction.scale(this.speed));

                this.rotateTowardsTarget(pos);
            }
        }else if(target!=null){
            this.setPos(this.position());
            var hit = Util.internalRaycastForEntity(this.level, this,this.getEyePosition(),this.directionBlock,true,2F);

            Vec3 vec32 = hit.getLocation().subtract(this.getEyePosition());
            double f5 = -Math.toDegrees(Math.atan2(vec32.y,Math.sqrt(vec32.x*vec32.x + vec32.z*vec32.z)));
            double f6 = Math.toDegrees(Math.atan2(vec32.z, vec32.x)) - 90.0F;
            this.yHeadRot=(float)f6;
            this.setYHeadRot((float) f6);
            this.yBodyRot= (float) (f6);
            this.setYRot((float) f6);
            this.setXRot((float) f5);
            this.setRot(this.getYRot(),this.getXRot());
        }
    }

    private void rotateTowardsTarget(LivingEntity target) {
        Vec3 targetPos = target.position();
        Vec3 harpyPos = this.position();
        double dx = targetPos.x - harpyPos.x;
        double dy = targetPos.y - harpyPos.y;
        double dz = targetPos.z - harpyPos.z;
        double targetYaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
        double pitch = -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        this.setYRot(this.lerpRotation(this.getYRot(), (float)targetYaw, 30.0F));
        this.setXRot((float)pitch);
        this.setRot(this.getYRot(),this.getXRot());
    }

    private void rotateTowardsTarget(BlockPos target) {
        Vec3 targetPos = Vec3.atCenterOf(target);
        Vec3 harpyPos = this.position();
        double dx = targetPos.x - harpyPos.x;
        double dy = targetPos.y - harpyPos.y;
        double dz = targetPos.z - harpyPos.z;
        double targetYaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
        double pitch = -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        this.setYRot((float) targetYaw);
        this.setXRot((float)pitch);
        this.setRot(this.getYRot(),this.getXRot());
        this.yBodyRot=this.getYRot();
        this.setYBodyRot(this.getYRot());
    }

    private float lerpRotation(float currentYaw, float targetYaw, float maxTurnSpeed) {
        float deltaYaw = Mth.floor(targetYaw - currentYaw);
        return currentYaw > -269 ? currentYaw + Mth.clamp(deltaYaw, -maxTurnSpeed, maxTurnSpeed) : currentYaw+360;
    }



    private double calculateHeightOffset(LivingEntity target) {
        double currentAltitude = this.getY();
        double targetAltitude = target.getY();
        double targetHeight = targetAltitude + (double)80.0F + Math.random();
        return targetHeight - currentAltitude;
    }

    private double calculateHeightOffset(BlockPos target) {
        double currentAltitude = this.getY();
        double targetAltitude = target.getY();
        double targetHeight = targetAltitude + (double)80.0F + Math.random();
        return targetHeight - currentAltitude;
    }


    @Override
    public net.minecraftforge.entity.PartEntity<?>[] getParts() {
        return this.legs;
    }

    public void clientAnim(){
        if(this.idleTimer<=0 && this.spinTimer <=0){
            if(this.actuallyPhase == PhaseAttack.GROUND){
                this.jump.stop();
                this.idleTimer = 80;
                this.idle.stop();
                this.idleJump.stop();
                this.idleGround.start(this.tickCount);
            }else if(this.actuallyPhase == PhaseAttack.JUMP){
                this.idleTimer = 20;
                this.idle.stop();
                this.idleGround.stop();
                this.idleJump.start(this.tickCount);
            }else {
                this.idleTimer = 100;
                this.idleJump.stop();
                this.idleGround.stop();
                this.idle.start(this.tickCount);
            }
            this.spinTimer = 0;
            this.lazerTimer = 0;
            this.shootLaserTimer = 0;
            this.startSummoningTimer = 0;
            this.startLazerTimer = 0;
        }else {
            this.idleTimer--;
        }

        if (this.tickCount % this.getPulsingBeatDelay() == 0) {
            this.pulsingAnimation = 30;
            if (!this.isSilent()) {
                this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.WARDEN_HEARTBEAT, this.getSoundSource(), 5.0F, this.getVoicePitch(), false);
            }
        }

        this.pulsingAnimationO = this.pulsingAnimation;
        if (this.pulsingAnimation > 0) {
            --this.pulsingAnimation;
        }

        prevScreenShakeAmount = screenShakeAmount;

        if (screenShakeAmount > 0) {
            screenShakeAmount = Math.max(0, screenShakeAmount - 0.03F);
        }

        if(this.actuallyPhase == PhaseAttack.SPAWN){
            int points = 36;
            double radius = 5.5;


            ParticleOptions particleoptions = ParticleTypes.POOF;

            Vec3 vec3 = this.dragonDeath.subtract(this.position());

            Vec3 direction = vec3.normalize(); // dirección del "tubo" de anillos
            int distance = Mth.ceil(vec3.length());
            Vec3 up = new Vec3(0, 1, 0);
            if (Math.abs(direction.dot(up)) > 0.99) {
                up = new Vec3(1, 0, 0); // si apunta hacia arriba, evitamos degeneración
            }
            Vec3 right = direction.cross(up).normalize();  // eje X local
            Vec3 forward = right.cross(direction).normalize(); // eje Y local
            int i2;
            float f1;
            i2 = 30;
            f1 = distance;


            for(int j = 0; j < i2; ++j) {
                float f2 = (float) (this.random.nextFloat() * (this.random.nextFloat()*Math.PI));
                float f3 = Mth.sqrt(this.random.nextFloat()) * f1;
                double d0 = this.getX() + (double)(Mth.cos(f2) * f3);
                double d4 = this.getZ() + (double)(Mth.sin(f2) * f3);
                double d2 = this.level.getHeight(Heightmap.Types.WORLD_SURFACE, (int) d0, (int) d4) + 1;

                Vec3 delta=this.position().subtract(new Vec3(d0,d2,d4)).normalize().scale(2.0D);

                double d5 = delta.x + (0.5D - this.random.nextDouble()) * 0.15D;
                double d6 = (double)0.01F;
                double d7 = delta.z + (0.5D - this.random.nextDouble()) * 0.15D;


                this.level.addAlwaysVisibleParticle(particleoptions, d0, d2, d4, d5, d6, d7);
            }
            int i = 0;
            for (int i1 = 0; i1 < distance; i1 += 1) {
                double px = this.getX() + direction.x * i1;
                double py = this.getY() + 1.0 + direction.y * i1;
                double pz = this.getZ() + direction.z * i1;

                double angle = 2 * Math.PI * i / points + 0.1744444F * i1;

                double x = radius * Math.cos(angle);
                double y = radius * Math.sin(angle);

                Vec3 offset = right.scale(x).add(forward.scale(y));

                double fx = px + offset.x;
                double fy = py + offset.y;
                double fz = pz + offset.z;
                double dx = -radius * Math.sin(angle);
                double dy = radius * Math.cos(angle);

                Vec3 motion = right.scale(dx).add(forward.scale(dy)).normalize().scale(0.3); // velocidad tangencial

                level.addParticle(this.random.nextBoolean() ? BKParticles.FLAME_PARTICLE.get() : ParticleTypes.SOUL_FIRE_FLAME, fx, fy, fz, motion.x, motion.y, motion.z);

                if(i++>35){
                    i=0;
                }
            }
        }
    }

    private int getPulsingBeatDelay() {
        return 50;
    }

    @Override
    public void handleEntityEvent(byte p_21375_) {
        if(p_21375_==4){
            this.spinTimer =300;
            this.spinning.start(this.tickCount);
            this.idle.stop();
            this.idleTimer = 300;
        }else if(p_21375_==1){
            this.attackHeadTimer =34;
            this.attackGround.start(this.tickCount);
            this.idleGround.stop();
            this.idle.stop();
            this.idleTimer = 34;
        }else if(p_21375_==3){
            this.particleChargedPoof();
            if(BeyondTheEnd.jellyfishFightEvent!=null){
                BeyondTheEnd.jellyfishFightEvent.screenShakeAmount=0.5F;
            }
        }else if(p_21375_==8){
            this.shootLaser.stop();
        }else if(p_21375_==16){
            this.startLazerTimer=110;
            this.startLazer.start(this.tickCount);
            this.idle.stop();
            this.idleTimer=110;
        }else if(p_21375_==32){
            this.lazerTimer=180;
            this.shootLaser.start(this.tickCount);
            this.startLazer.stop();
            this.idle.stop();
            this.idleTimer=180;
            if(BeyondTheEnd.jellyfishFightEvent!=null){
                BeyondTheEnd.jellyfishFightEvent.screenShakeAmount=1.0F;
            }
        }else if(p_21375_==48){
            this.startSummoningTimer=101;
            this.startSummoning.start(this.tickCount);
            this.idle.stop();
            this.idleTimer=101;
        }else if(p_21375_==64){
            this.summoningTimer=500;
            this.summoning.start(this.tickCount);
            this.startSummoning.stop();
            this.idle.stop();
            this.idleTimer=500;
        }else if(p_21375_ == 66){
            this.actuallyPhase = JellyfishEntity.PhaseAttack.SPAWN;
            this.shootLaser.start(this.tickCount);
        }else {
            super.handleEntityEvent(p_21375_);
        }
    }
    public void resetState(){
        this.idleGround.stop();
        this.idleTimer=0;
        this.jump.stop();
        this.shootLaser.stop();
        this.idleJump.stop();
    }

    public void setActionForID(int idAction) {

        switch (idAction){
            case 0 ->{
                if(this.level.isClientSide){
                    this.resetState();
                }
                this.actuallyPhase=PhaseAttack.SPIN_AROUND;
                this.jumpCount = 0;
                this.setTarget(null);
            }
            case 1 ->{
                this.actuallyPhase=PhaseAttack.ATTACK;
            }
            case 2 ->{
                this.actuallyPhase=PhaseAttack.LAZER;
            }
            case 3 ->{
                this.actuallyPhase=PhaseAttack.SUMMONING;
            }
            case 4 ->{
                this.actuallyPhase = PhaseAttack.LAND;
            }
            case 5 ->{
                this.actuallyPhase = PhaseAttack.GROUND;
            }
            case 6 ->{
                this.actuallyPhase = PhaseAttack.PREPARE_JUMP;
            }
            case 7 ->{
                this.actuallyPhase = PhaseAttack.JUMP;
            }
        }
    }

    public void recreateFromPacket(ClientboundAddEntityPacket p_218825_) {
        super.recreateFromPacket(p_218825_);
        if (true) return; // Forge: Fix MC-158205: Moved into setId()
        JellyfishPartEntity[] aenderdragonpart = this.legs;

        for(int i = 0; i < aenderdragonpart.length; ++i) {
            aenderdragonpart[i].setId(i + p_218825_.getId());
        }

    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public float getShakeDistance() {
        return 20;
    }

    @Override
    public float getScreenShakeAmount(float partialTicks) {
        return prevScreenShakeAmount + (screenShakeAmount - prevScreenShakeAmount) * partialTicks;
    }

    @Override
    public boolean canShake(Entity entity) {
        if(this.distanceTo(entity)>=20.0F){
            return this.directionBlock.distanceTo(entity.position())<20.0F;
        }
        return true;
    }

    private boolean canSummonMinions() {
        return this.countAliveMinions() <= PHASE_MAX_MINIONS_TO_SUMMON[this.getFightPhase() - 1];
    }

    @Override
    public double getShakeDistanceForEntity(Entity entity) {
        if(this.directionBlock.distanceTo(entity.position())<20.0F){
            return this.directionBlock.distanceTo(entity.position());
        }
        return this.distanceTo(entity);
    }

    @Override
    public boolean isAlliedTo(Entity p_20355_) {
        if(p_20355_ instanceof JellyfishMinionEntity){
            return true;
        }
        return super.isAlliedTo(p_20355_);
    }

    static class JellyfishSummoning extends Goal{
        private final JellyfishEntity harpy;
        public BlockPos pos = new BlockPos(0,0,0);

        public JellyfishSummoning(JellyfishEntity harpy) {
            this.harpy = harpy;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        public boolean canUse() {
            return this.harpy.actuallyPhase==PhaseAttack.SUMMONING;
        }

        @Override
        public void start() {
            this.harpy.startSummoningTimer=101;
            if(!this.harpy.level.isClientSide){
                this.harpy.level.broadcastEntityEvent(this.harpy,(byte) 48);
            }
        }
        public boolean requiresUpdateEveryTick() {
            return true;
        }

    }

    static class JellyfishLazer extends Goal{
        private final JellyfishEntity harpy;


        public JellyfishLazer(JellyfishEntity harpy) {
            this.harpy = harpy;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        public boolean canUse() {
            return this.harpy.actuallyPhase==PhaseAttack.LAZER;
        }

        @Override
        public void start() {
            this.harpy.startLazerTimer=110;
            if(!this.harpy.level.isClientSide){
                this.harpy.level.broadcastEntityEvent(this.harpy,(byte) 16);
            }
        }

        public void tick() {


        }
        public boolean requiresUpdateEveryTick() {
            return true;
        }

    }


    static class AttackGoal extends Goal {
        protected final JellyfishEntity mob;
        private final double speedModifier;
        private final boolean followingTargetEvenIfNotSeen;
        private double pathedTargetX;
        private double pathedTargetY;
        private double pathedTargetZ;
        private int ticksUntilNextPathRecalculation;
        private int ticksUntilNextAttack;
        private long lastCanUseCheck;
        private int failedPathFindingPenalty = 0;
        private boolean canPenalize = false;

        public AttackGoal(JellyfishEntity p_25552_, double p_25553_, boolean p_25554_) {
            this.mob = p_25552_;
            this.speedModifier = p_25553_;
            this.followingTargetEvenIfNotSeen = p_25554_;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        public boolean canUse() {
            return this.mob.actuallyPhase==PhaseAttack.ATTACK;
        }


        public void start() {
            this.mob.setDeltaMovement(Vec3.ZERO);
            this.mob.setAggressive(true);
            this.ticksUntilNextPathRecalculation = 0;
            this.ticksUntilNextAttack = 0;
            this.resetAttackCooldown();
            LivingEntity livingentity=this.mob.getTarget();
            if(livingentity!=null){
                this.upgradePosTarget(livingentity);
            }
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
                Vec3 vec3 = new Vec3(this.pathedTargetX,this.pathedTargetY,this.pathedTargetZ);
                double d0 = this.mob.distanceToSqr(vec3.x, vec3.y, vec3.z);
                this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
                if ((this.followingTargetEvenIfNotSeen || this.mob.getSensing().hasLineOfSight(livingentity)) && this.ticksUntilNextPathRecalculation <= 0 && (this.pathedTargetX == 0.0D && this.pathedTargetY == 0.0D && this.pathedTargetZ == 0.0D || livingentity.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= 1.0D || this.mob.getRandom().nextFloat() < 0.05F)) {
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
                    } else if (d0 > 256.0D) {
                        this.ticksUntilNextPathRecalculation += 5;
                    }
                    this.mob.setDeltaMovement(new Vec3(this.pathedTargetX-this.mob.position().x,this.pathedTargetY-this.mob.position().y,this.pathedTargetZ-this.mob.position().z).normalize().scale(0.85D));


                    this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);
                }

                if(this.mob.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) < 4.0D){
                    this.upgradePosTarget(livingentity);
                }
                if(this.mob.tickCount%10==0){
                    this.checkAndPerformAttack();
                }
            }
        }

        private void upgradePosTarget(LivingEntity livingentity) {
            Vec3 delta = livingentity.getDeltaMovement().scale(10.0D);
            this.pathedTargetX = livingentity.getX() + delta.x;
            this.pathedTargetY = livingentity.getY() + delta.y;
            this.pathedTargetZ = livingentity.getZ() + delta.z;

        }

        protected void checkAndPerformAttack() {
            this.mob.level.getEntitiesOfClass(LivingEntity.class,this.mob.getBoundingBox().inflate(25.0D,4.0F,25.0F),e->e!=this.mob && !this.mob.isAlliedTo(e)).forEach(e->{
                //Rotacion?
                if(e.hurt(DamageSource.mobAttack(this.mob),30.0F * this.mob.getDamageScale())){
                    double dX=e.getX()-this.mob.getX();
                    double dZ=e.getZ()-this.mob.getZ();
                    double d0=Math.sqrt(dX*dX+dZ*dZ);
                    if(e.isOnGround()){
                        e.push(dX/d0 * 2.0D,2.4F,dZ/d0 * 2.0D);
                    }
                }
            });
        }

        protected void resetAttackCooldown() {
            if(this.mob.spinTimer <=0){
                this.mob.spinTimer =300;
                this.mob.level.broadcastEntityEvent(this.mob,(byte) 4);
            }
        }


    }

    public enum PhaseAttack{
        SPAWN,
        ATTACK,
        SUMMONING,
        LAZER,
        SPIN_AROUND,
        LAND,
        GROUND,
        PREPARE_JUMP,
        JUMP,
        DEATH;
    }
}
