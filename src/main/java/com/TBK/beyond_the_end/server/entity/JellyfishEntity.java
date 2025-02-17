package com.TBK.beyond_the_end.server.entity;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.client.particle.BKParticles;
import com.TBK.beyond_the_end.common.Util;
import com.TBK.beyond_the_end.common.api.ICamShaker;
import com.TBK.beyond_the_end.common.registry.BKEntityType;
import com.TBK.beyond_the_end.common.registry.BTESounds;
import com.TBK.beyond_the_end.server.capabilities.PortalPlayer;
import com.TBK.beyond_the_end.server.network.PacketHandler;
import com.TBK.beyond_the_end.server.network.message.PacketFlameParticles;
import com.TBK.beyond_the_end.server.network.message.PacketNextActionJellyfish;
import com.TBK.beyond_the_end.server.network.message.PacketSync;
import com.TBK.beyond_the_end.server.network.message.PacketTargetAttack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
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
import net.minecraft.world.entity.monster.warden.AngerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.*;
import net.minecraftforge.client.event.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public class JellyfishEntity extends PathfinderMob implements ICamShaker {
    public int idleTimer = 0;
    public int lazerTimer = 0;
    public int attackTimer = 0;
    public int startLazerTimer = 0;
    public int startSummoningTimer = 0;
    public int summoningTimer = 0;
    public int shootLaserTimer = 0;
    public int progressMine = 0;
    public int deathTimer = 0;
    private final double speed = 1.0F;
    private final double circleRadius = 300.0D;
    private double circlingAngle = 0.0F;
    private Vec3 circlingPosition;
    public boolean rot=false;
    public double offsetX=0.0D;
    public double offsetZ=0.0D;
    public double heightOffset = 0.0D;

    public BlockPos lastBlockPos=BlockPos.ZERO;
    public Vec3 directionBlock=Vec3.ZERO;

    public PhaseAttack actuallyPhase=PhaseAttack.SPIN_AROUND;
    public AnimationState idle = new AnimationState();
    public AnimationState spinning = new AnimationState();
    public AnimationState startSummoning = new AnimationState();
    public AnimationState summoning = new AnimationState();
    public AnimationState startLazer = new AnimationState();
    public AnimationState shootLaser = new AnimationState();
    public int timerSpawn=0;
    public int nextTimer=0;
    public int maxNextTimer=300;
    private int pulsingAnimation;

    private int pulsingAnimationO;
    private float prevScreenShakeAmount;
    private float screenShakeAmount;

    public JellyfishEntity(EntityType<? extends PathfinderMob> p_21368_, Level p_21369_) {
        super(p_21368_, p_21369_);
        this.moveControl = new FlyingMoveControl(this, 90, true);
        this.lookControl = new SmoothSwimmingLookControl(this,10);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0D)
                .add(Attributes.FOLLOW_RANGE,100.0D)
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
        boolean wasHurt = false;
        float damageOriginal=damage;
        if(this.lazerTimer<=0){
            if(source.getEntity() instanceof Player player && PortalPlayer.get(player).isPresent()){
                damage = PortalPlayer.get(player).orElse(null).damageFinal(this,damage);
            }
            if(!this.level.isClientSide){
                if(BeyondTheEnd.jellyfishFightEvent!=null){
                    BeyondTheEnd.jellyfishFightEvent.updateJellyfish(this);
                }
            }
        }
        wasHurt=damageOriginal==damage;

        if(wasHurt && this.level.isClientSide){
            this.particleHurt();
        }

        return super.hurt(source,damage);
    }

    public void particleHurt() {
        Random random = new Random();
        double box=this.getBbWidth();

        double xp=this.getX() + random.nextDouble(-box,box);
        double yp=this.getY() + random.nextDouble(0.0d,this.getBbHeight());
        double zp=this.getZ() + random.nextDouble(-box,box);

        this.level.addAlwaysVisibleParticle(ParticleTypes.DRAGON_BREATH, xp, yp, zp, 0.0F, 0.1F, 0.0F);
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
    public void die(DamageSource p_21014_) {
        super.die(p_21014_);
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);
        if(this.lazerTimer>0){
            this.lazerTimer--;

            if(!this.level.isClientSide){
                List<HitResult> hitResults = Util.internalRaycastForAllEntity(this.level,this,this.getEyePosition(),this.directionBlock,true,4.0F);
                for (HitResult hitResult : hitResults){
                    if(hitResult.getType() == HitResult.Type.BLOCK){
                        Level level = this.level;
                        BlockPos blockpos = ((BlockHitResult)hitResult).getBlockPos();
                        BlockState blockstate = level.getBlockState(blockpos);
                        BlockState oldBlockState = level.getBlockState(this.lastBlockPos);
                        if (blockstate.isFlammable(this.level,blockpos,((BlockHitResult) hitResult).getDirection()) && !CampfireBlock.canLight(blockstate) && !CandleBlock.canLight(blockstate) && !CandleCakeBlock.canLight(blockstate)) {
                            BlockPos blockpos1 = blockpos.relative(((BlockHitResult) hitResult).getDirection());
                            if (BaseFireBlock.canBePlacedAt(level, blockpos1, ((BlockHitResult) hitResult).getDirection())) {
                                level.playSound(null, blockpos1, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                                BlockState blockstate1 = BaseFireBlock.getState(level, blockpos1);
                                level.setBlock(blockpos1, blockstate1, 11);
                                level.gameEvent(this, GameEvent.BLOCK_PLACE, blockpos);
                            }
                        }
                        if(oldBlockState==blockstate){
                            int hardness = (int) (Math.max(blockstate.getDestroySpeed(this.level, blockpos), 0.2F) * 30F);
                            int i = (int) ((float) this.progressMine / hardness * 10.0F);

                            if(hardness<75){
                                this.level.destroyBlockProgress(this.getId(),blockpos,i);

                                if (this.progressMine++ > hardness) {
                                    this.level.destroyBlock(blockpos, true);
                                    this.progressMine = 0;
                                }
                            }
                        }else {
                            this.lastBlockPos=blockpos;
                            this.progressMine=0;
                        }
                    }else if(hitResult.getType() == HitResult.Type.ENTITY){
                        Entity entity = ((EntityHitResult)hitResult).getEntity();
                        if(entity instanceof LivingEntity living){
                            living.hurt(DamageSource.GENERIC,4.0F);
                            living.hurt(DamageSource.mobAttack(this),8.0F);
                        }
                    }
                }
            }else {
                if(this.screenShakeAmount<=0.0F){
                    this.screenShakeAmount=0.5F;
                    //this.level.broadcastEntityEvent(this,(byte) 65);
                }
            }

            if(this.lazerTimer==0){
                this.directionBlock=Vec3.ZERO;
                this.actuallyPhase=PhaseAttack.SPIN_AROUND;
                if(!this.level.isClientSide){
                    this.level.broadcastEntityEvent(this,(byte) 8);
                    PacketHandler.sendToAllTracking(new PacketNextActionJellyfish(this.getId(),0,0),this);
                }else {
                    this.shootLaser.stop();
                }
            }
        }

        if(this.actuallyPhase==PhaseAttack.SPIN_AROUND){
            this.nextTimer++;
        }

        if(!this.level.isClientSide){
            if(this.nextTimer>this.maxNextTimer && this.actuallyPhase==PhaseAttack.SPIN_AROUND && this.getTarget()!=null){
                int time=300 + this.level.random.nextInt(0,5)*this.level.random.nextInt(0,5);
                this.maxNextTimer=time;
                this.nextTimer=0;
                int nextAction=this.level.random.nextInt(0,4);
                if(nextAction==3){
                    if(!this.canSummonMinions()){
                        nextAction=2;
                    }
                }
                this.setActionForID(nextAction);
                PacketHandler.sendToAllTracking(new PacketNextActionJellyfish(this.getId(),time,nextAction),this);
            }
        }

        if(this.attackTimer>0){
            this.attackTimer--;
            if(this.attackTimer==0){
                this.actuallyPhase=PhaseAttack.SPIN_AROUND;
                if(!this.level.isClientSide){
                    PacketHandler.sendToAllTracking(new PacketNextActionJellyfish(this.getId(),0,0),this);
                }else {
                    this.spinning.stop();
                }
            }
        }

        if(this.startLazerTimer>0){
            this.startLazerTimer--;
            if(this.getTarget()!=null){
                Vec3 vec3=new Vec3(this.getTarget().getBlockX(),this.getTarget().getBlockY(),this.getTarget().getBlockZ());
                this.directionBlock=vec3;
                if(!this.level.isClientSide){
                    PacketHandler.sendToAllTracking(new PacketTargetAttack(this.getId(), (int) vec3.x, (int) vec3.y, (int) vec3.z),this);
                }
            }
            if(this.startLazerTimer==0){
                this.lazerTimer=180;
                if(!this.level.isClientSide){
                    this.level.broadcastEntityEvent(this,(byte) 32);
                }else{
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
            this.summoningTimer--;
            if(this.summoningTimer>45){
                if((45+this.summoningTimer)%75==0 || this.summoningTimer==46){
                    JellyfishMinionEntity minion = new JellyfishMinionEntity(BKEntityType.JELLYFISH_MINION.get(), this.level);
                    minion.setPos(this.position().add(0.0F,-6.0D,0.0D));
                    minion.actuallyPhase= JellyfishMinionEntity.PhaseAttack.SPAWN;
                    minion.jellyfish = this;
                    minion.heightOffset = this.level.random.nextDouble() * 40.0D * this.level.random.nextInt(-1,1);
                    this.level.addFreshEntity(minion);
                    if(this.getTarget()!=null){
                        minion.setTarget(this.getTarget());
                    }
                }
            }

            if(this.level.isClientSide){
                double x = this.getX();
                double y = this.getY();
                double z = this.getZ();
                for (int i=0;i<10;i++){
                    Random random1 = new Random();
                    this.level.addParticle(BKParticles.MIST_PARTICLE.get(),x + random.nextInt(-2,2),y,z + random.nextInt(-2,2),random1.nextFloat(-0.5F,0.5F),-0.6F,random1.nextFloat(-0.5F,0.5F));
                }
            }

            if(this.summoningTimer==0){
                this.actuallyPhase=PhaseAttack.SPIN_AROUND;
                if(!this.level.isClientSide){
                    PacketHandler.sendToAllTracking(new PacketNextActionJellyfish(this.getId(),0,0),this);
                }else {
                    this.summoning.stop();
                }
            }
        }


        if(this.level.isClientSide){
            this.clientAnim();
        }

        if(!this.level.isClientSide){
            if(BeyondTheEnd.jellyfishFightEvent!=null){
                BeyondTheEnd.jellyfishFightEvent.updateJellyfish(this);
            }
        }

        this.refreshDimensions();
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
    
    @Override
    public void aiStep() {
        super.aiStep();
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
        this.timerSpawn++;
        if(this.timerSpawn>90){
            this.actuallyPhase=PhaseAttack.SPIN_AROUND;
            if(!this.level.isClientSide){
                PacketHandler.sendToAllTracking(new PacketNextActionJellyfish(this.getId(),0,0),this);
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
        }else {
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



    public void clientAnim(){
        if(this.idleTimer<=0 && this.attackTimer<=0){
            this.idleTimer = 100;
            this.idle.start(this.tickCount);
            this.attackTimer = 0;
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
    }

    private int getPulsingBeatDelay() {
        return 50;
    }

    @Override
    public void handleEntityEvent(byte p_21375_) {
        if(p_21375_==4){
            this.attackTimer=300;
            this.spinning.start(this.tickCount);
            this.idle.stop();
            this.idleTimer = 300;
        }else if(p_21375_==8){
            this.shootLaser.stop();
        }else if(p_21375_==16){
            this.startLazerTimer=110;
            this.startLazer.start(this.tickCount);
            this.idle.stop();
            this.idleTimer=110;

            this.level.playSound(null,this, BTESounds.ARQUEBUS_SHOOT.get(),SoundSource.HOSTILE,10.0F,1.0F);
        }else if(p_21375_==32){
            this.lazerTimer=180;
            this.shootLaser.start(this.tickCount);
            this.startLazer.stop();
            this.idle.stop();
            this.idleTimer=300;
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
            this.idleTimer=101;
        }else if(p_21375_==65){
            this.screenShakeAmount=1.0F;
        }
        super.handleEntityEvent(p_21375_);
    }

    public void setActionForID(int idAction) {
        switch (idAction){
            case 0 ->{
                this.actuallyPhase=PhaseAttack.SPIN_AROUND;
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
        }
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
        List<? extends JellyfishMinionEntity> list = ((ServerLevel)this.level).getEntities(BKEntityType.JELLYFISH_MINION.get(), LivingEntity::isAlive);
        return list.isEmpty();
    }

    @Override
    public double getShakeDistanceForEntity(Entity entity) {
        if(this.directionBlock.distanceTo(entity.position())<20.0F){
            return this.directionBlock.distanceTo(entity.position());
        }
        return this.distanceTo(entity);
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
                    this.mob.setDeltaMovement(new Vec3(this.pathedTargetX-this.mob.position().x,this.pathedTargetY-this.mob.position().y,this.pathedTargetZ-this.mob.position().z).normalize().scale(1.5D));


                    this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);
                }

                if(livingentity.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) < 30.0D){
                    this.upgradePosTarget(livingentity);
                }
                if(this.mob.tickCount%5==0){
                    this.checkAndPerformAttack();
                }
            }
        }

        private void upgradePosTarget(LivingEntity livingentity) {
            this.pathedTargetX = livingentity.getX();
            this.pathedTargetY = livingentity.getY();
            this.pathedTargetZ = livingentity.getZ();

        }

        protected void checkAndPerformAttack() {
            this.mob.level.getEntitiesOfClass(LivingEntity.class,this.mob.getBoundingBox().inflate(25.0D,12.0F,25.0F),e->e!=this.mob).forEach(e->{
                if(e.hurt(DamageSource.mobAttack(this.mob),10.0F)){
                    double dX=e.getX()-this.mob.getX();
                    double dZ=e.getZ()-this.mob.getZ();
                    double d0=dX*dX+dZ*dZ;
                    if(e.isOnGround()){
                        e.push(dX/d0,2.4F,dZ/d0);
                    }
                }
            });
        }

        protected void resetAttackCooldown() {
            if(this.mob.attackTimer<=0){
                this.mob.attackTimer=300;
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
        DEATH;
    }
}
