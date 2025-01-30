package com.TBK.beyond_the_end.server.entity;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.server.network.PacketHandler;
import com.TBK.beyond_the_end.server.network.message.PacketNextActionJellyfish;
import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.*;
import net.minecraftforge.entity.PartEntity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public class JellyfishEntity extends Mob {
    private static final EntityDataAccessor<Boolean> SPINNING = SynchedEntityData.defineId(JellyfishEntity.class, EntityDataSerializers.BOOLEAN);
    public int idleTimer = 0;
    public int lazerTimer = 0;
    public int attackTimer = 0;
    public int startLazerTimer = 0;
    public int startSummoningTimer = 0;
    public int shootLaserTimer = 0;
    public int progressMine = 0;
    public BlockPos lastBlockPos=BlockPos.ZERO;

    public PhaseAttack actuallyPhase=PhaseAttack.SPIN_AROUND;
    public AnimationState idle = new AnimationState();
    public AnimationState spinning = new AnimationState();
    public AnimationState startSummoning = new AnimationState();
    public AnimationState summoning = new AnimationState();
    public AnimationState startLazer = new AnimationState();
    public AnimationState shootLaser = new AnimationState();
    public int nextTimer=0;
    public int maxNextTimer=300;
    public Vec3 circlePos= Vec3.ZERO;
    public JellyfishEntity(EntityType<? extends Mob> p_21368_, Level p_21369_) {
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
    public boolean hurt(DamageSource p_21016_, float p_21017_) {
        return super.hurt(p_21016_, p_21017_*0.1F);
    }

    public boolean hurt(DamageSource source,float damage,boolean isChargedAttack){
        if(isChargedAttack){
            if(!this.level.isClientSide){
                this.level.broadcastEntityEvent(this,(byte) 8);
            }
            this.actuallyHurt(source,damage);
            return true;
        }
        return false;
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
        this.goalSelector.addGoal(2,new JellyfishLazer(this));

        this.goalSelector.addGoal(1,new JellyfishSpinAround(this,0.5D,10.0D,20, 4, 10));
        this.targetSelector.addGoal(0,new NearestAttackableTargetGoal<>(this,LivingEntity.class,true));
    }

    @Override
    public void tick() {
        super.tick();
        if(this.lazerTimer>0){
            this.lazerTimer--;
            HitResult hitResult = raycastForEntity(this.level,this,50,true,0.5F);
            if(hitResult.getType() == HitResult.Type.BLOCK){
                Level level = this.level;
                BlockPos blockpos = ((BlockHitResult)hitResult).getBlockPos();
                BlockState blockstate = level.getBlockState(blockpos);
                BlockState oldBlockState = level.getBlockState(this.lastBlockPos);
                if(!this.level.isClientSide){
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
                }
            }else if(hitResult.getType() == HitResult.Type.ENTITY){
                Entity entity = ((EntityHitResult)hitResult).getEntity();
                if(entity instanceof LivingEntity living){
                    living.hurt(DamageSource.GENERIC,2.0F);
                    living.hurt(DamageSource.mobAttack(this),5.0F);
                }
            }
            if(this.lazerTimer==0){
                this.actuallyPhase=PhaseAttack.SPIN_AROUND;
                if(!this.level.isClientSide){
                    PacketHandler.sendToAllTracking(new PacketNextActionJellyfish(this.getId(),0,0),this);
                }
            }
        }

        if(this.actuallyPhase==PhaseAttack.SPIN_AROUND){
            this.nextTimer++;
        }

        if(!this.level.isClientSide){
            if(this.nextTimer>this.maxNextTimer && this.actuallyPhase==PhaseAttack.SPIN_AROUND){
                int time=300 + this.level.random.nextInt(0,5)*this.level.random.nextInt(0,5);
                this.maxNextTimer=time;
                this.nextTimer=0;
                this.setActionForID(2);
                PacketHandler.sendToAllTracking(new PacketNextActionJellyfish(this.getId(),time,2),this);
            }
        }

        if(this.attackTimer>0){
            this.attackTimer--;
        }

        if(this.startLazerTimer>0){
            this.startLazerTimer--;
            if(this.startLazerTimer==0){
                this.lazerTimer=300;
                if(!this.level.isClientSide){
                    this.level.broadcastEntityEvent(this,(byte) 32);
                }
            }
        }


        if(this.level.isClientSide){
            this.clientAnim();
        }
        this.refreshDimensions();
    }


    public static HitResult raycastForEntity(Level level, Entity originEntity, float distance, boolean checkForBlocks, float bbInflation) {
        Vec3 start = originEntity.getEyePosition();
        Vec3 end = originEntity.getLookAngle().normalize().scale(distance).add(start);

        return internalRaycastForEntity(level, originEntity, start, end, checkForBlocks, bbInflation);
    }

    private static HitResult internalRaycastForEntity(Level level, Entity originEntity, Vec3 start, Vec3 end, boolean checkForBlocks, float bbInflation) {
        BlockHitResult blockHitResult = null;
        if (checkForBlocks) {
            blockHitResult = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, originEntity));
            end = blockHitResult.getLocation();
        }
        AABB range = originEntity.getBoundingBox().expandTowards(end.subtract(start));



        List<HitResult> hits = new ArrayList<>();
        List<? extends Entity> entities = level.getEntities(originEntity, range, Entity::isAlive);
        for (Entity target : entities) {
            HitResult hit = checkEntityIntersecting(target, start, end, bbInflation);
            if (hit.getType() != HitResult.Type.MISS)
                hits.add(hit);
        }

        if (!hits.isEmpty()) {
            hits.sort((o1, o2) -> o1.getLocation().distanceToSqr(start) < o2.getLocation().distanceToSqr(start) ? -1 : 1);
            return hits.get(0);
        } else if (checkForBlocks) {
            return blockHitResult;
        }
        return BlockHitResult.miss(end, Direction.UP, new BlockPos(end));
    }

    public static HitResult checkEntityIntersecting(Entity entity, Vec3 start, Vec3 end, float bbInflation) {
        Vec3 hitPos = null;
        if (entity.isMultipartEntity()) {
            for (PartEntity p : entity.getParts()) {
                var hit = p.getBoundingBox().inflate(bbInflation).clip(start, end).orElse(null);
                if (hit != null) {
                    hitPos = hit;
                    break;
                }
            }
        } else {
            hitPos = entity.getBoundingBox().inflate(bbInflation).clip(start, end).orElse(null);
        }
        if (hitPos != null)
            return new EntityHitResult(entity, hitPos);
        else
            return BlockHitResult.miss(end, Direction.UP, new BlockPos(end));
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
    }

    @Override
    public void handleEntityEvent(byte p_21375_) {
        if(p_21375_==4){
            this.attackTimer=300;
            this.spinning.start(this.tickCount);
            this.idle.stop();
            this.idleTimer = 300;
        }else if(p_21375_==8){
            this.particleHurt();
        }else if(p_21375_==16){
            this.startLazerTimer=110;
            this.startLazer.start(this.tickCount);
            this.setActionForID(2);
            this.idle.stop();
            this.idleTimer=110;
        }else if(p_21375_==32){
            this.lazerTimer=300;
            this.shootLaser.start(this.tickCount);
            this.startLazer.stop();
            this.idle.stop();
            this.idleTimer=300;
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

    static class JellyfishSpinAround extends Goal{
        private final JellyfishEntity harpy;
        private final double speed;
        private final double circleRadius;
        private double circlingAngle;
        private final int minAltitude;
        private final int maxAltitude;
        private Vec3 circlingPosition;
        public boolean rot=false;

        public BlockPos pos = new BlockPos(0,0,0);

        public JellyfishSpinAround(JellyfishEntity harpy, double speed, double circleRadius, int attackInterval, int minAltitude, int maxAltitude) {
            this.harpy = harpy;
            this.speed = speed;
            this.circleRadius = circleRadius;
            this.minAltitude = 40;
            this.maxAltitude = 40;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
            this.circlingAngle = 0.0;
        }

        public boolean canUse() {
            return this.harpy.actuallyPhase==PhaseAttack.SPIN_AROUND;
        }

        public void start() {
            this.circlingPosition = null;
            this.circlingAngle = 0.0;
            this.rot=this.harpy.level.random.nextBoolean();
        }

        public void stop() {
            this.circlingPosition = null;
        }

        public void tick() {
            LivingEntity target = this.harpy.getTarget();
            this.circlingAngle += this.rot ? 0.05F : -0.05F;
            Vec3 direction;
            if (target != null) {
                double offsetX = Math.cos(this.circlingAngle) * this.circleRadius;
                double offsetZ = Math.sin(this.circlingAngle) * this.circleRadius;
                double heightOffset = this.calculateHeightOffset(target);
                this.circlingPosition = new Vec3(target.getX() + offsetX, target.getY() + heightOffset, target.getZ() + offsetZ);
                direction = this.circlingPosition.subtract(this.harpy.position()).normalize();
                this.harpy.setDeltaMovement(direction.scale(this.speed));

                this.rotateTowardsTarget(target);
            }else {
                this.pos = new BlockPos(0,this.harpy.level.getHeight(Heightmap.Types.WORLD_SURFACE,0,0),0);
                double offsetX = Math.cos(this.circlingAngle) * this.circleRadius;
                double offsetZ = Math.sin(this.circlingAngle) * this.circleRadius;
                double heightOffset = this.calculateHeightOffset(pos);
                this.circlingPosition = new Vec3(pos.getX() + offsetX, pos.getY() + heightOffset, pos.getZ() + offsetZ);
                direction = this.circlingPosition.subtract(this.harpy.position()).normalize();
                this.harpy.setDeltaMovement(direction.scale(this.speed));

                this.rotateTowardsTarget(pos);
            }


        }
        public boolean requiresUpdateEveryTick() {
            return true;
        }


        private void rotateTowardsTarget(LivingEntity target) {
            Vec3 targetPos = target.position();
            Vec3 harpyPos = this.harpy.position();
            double dx = targetPos.x - harpyPos.x;
            double dy = targetPos.y - harpyPos.y;
            double dz = targetPos.z - harpyPos.z;
            double targetYaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
            double pitch = -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
            this.harpy.setYRot(this.lerpRotation(this.harpy.getYRot(), (float)targetYaw, 30.0F));
            this.harpy.setXRot((float)pitch);
        }

        private void rotateTowardsTarget(BlockPos target) {
            Vec3 targetPos = Vec3.atCenterOf(target);
            Vec3 harpyPos = this.harpy.position();
            double dx = targetPos.x - harpyPos.x;
            double dy = targetPos.y - harpyPos.y;
            double dz = targetPos.z - harpyPos.z;
            double targetYaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
            double pitch = -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
            this.harpy.setYRot(this.lerpRotation(this.harpy.getYRot(), (float)targetYaw, 30.0F));
            this.harpy.setXRot((float)pitch);
        }

        private float lerpRotation(float currentYaw, float targetYaw, float maxTurnSpeed) {
            float deltaYaw = Mth.floor(targetYaw - currentYaw);
            return currentYaw > -267 ? currentYaw + Mth.clamp(deltaYaw, -maxTurnSpeed, maxTurnSpeed) : currentYaw+360;
        }



        private double calculateHeightOffset(LivingEntity target) {
            double currentAltitude = this.harpy.getY();
            double targetAltitude = target.getY();
            double targetHeight = targetAltitude + (double)this.minAltitude + Math.random() * (double)(this.maxAltitude - this.minAltitude);
            return targetHeight - currentAltitude;
        }

        private double calculateHeightOffset(BlockPos target) {
            double currentAltitude = this.harpy.getY();
            double targetAltitude = target.getY();
            double targetHeight = targetAltitude + (double)this.minAltitude + Math.random() * (double)(this.maxAltitude - this.minAltitude);
            return targetHeight - currentAltitude;
        }
    }

    static class JellyfishLazer extends Goal{
        private final JellyfishEntity harpy;
        private Vec3 circlingPosition;
        private double circlingAngle;

        public BlockPos pos = new BlockPos(0,0,0);

        public JellyfishLazer(JellyfishEntity harpy) {
            this.harpy = harpy;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        public boolean canUse() {
            return this.harpy.actuallyPhase==PhaseAttack.LAZER;
        }

        public void start() {
            this.circlingPosition=null;
            this.circlingAngle = 0.0F;
            this.pos=this.harpy.getTarget()==null ? this.pos : this.harpy.getTarget().getOnPos();

        }

        public void stop() {
            this.circlingPosition = null;
        }

        public void tick() {
            this.harpy.setDeltaMovement(this.harpy.getDeltaMovement().multiply(1.0F,0.0F,1.0F));
            this.pos = new BlockPos(0,this.harpy.level.getHeight(Heightmap.Types.WORLD_SURFACE,0,0),0);
            double offsetX = Math.cos(this.circlingAngle) * 10;
            double offsetZ = Math.sin(this.circlingAngle) * 10;

            this.circlingPosition = new Vec3(pos.getX() + offsetX, this.harpy.getY(), pos.getZ() + offsetZ);
            Vec3 direction = this.circlingPosition.subtract(this.harpy.position());
            this.harpy.setDeltaMovement(direction.normalize().scale(1.0F));

            BeyondTheEnd.LOGGER.debug("longitud :"+direction.length());
            if(direction.length()<1){
                this.harpy.startLazerTimer=110;
                if(!this.harpy.level.isClientSide){
                    this.harpy.level.broadcastEntityEvent(this.harpy,(byte) 16);
                }
            }
            if(this.harpy.startLazerTimer>0){
                if(this.harpy.getTarget()!=null){
                    this.rotateTowardsTarget(this.harpy.getTarget());
                }
            }

        }
        public boolean requiresUpdateEveryTick() {
            return true;
        }


        private void rotateTowardsTarget(LivingEntity target) {
            Vec3 targetPos = target.position();
            Vec3 harpyPos = this.harpy.position();
            double dx = targetPos.x - harpyPos.x;
            double dy = targetPos.y - harpyPos.y;
            double dz = targetPos.z - harpyPos.z;
            double targetYaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
            double pitch = -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
            this.harpy.setYRot(this.lerpRotation(this.harpy.getYRot(), (float)targetYaw, 30.0F));
            this.harpy.setXRot((float)pitch);
        }

        private void rotateTowardsTarget(BlockPos target) {
            Vec3 targetPos = Vec3.atCenterOf(target);
            Vec3 harpyPos = this.harpy.position();
            double dx = targetPos.x - harpyPos.x;
            double dy = targetPos.y - harpyPos.y;
            double dz = targetPos.z - harpyPos.z;
            double targetYaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
            double pitch = -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
            this.harpy.setYRot(this.lerpRotation(this.harpy.getYRot(), (float)targetYaw, 30.0F));
            this.harpy.setXRot((float)pitch);
        }

        private float lerpRotation(float currentYaw, float targetYaw, float maxTurnSpeed) {
            float deltaYaw = Mth.floor(targetYaw - currentYaw);
            return currentYaw > -267 ? currentYaw + Mth.clamp(deltaYaw, -maxTurnSpeed, maxTurnSpeed) : currentYaw+360;
        }
    }


    static class AttackGoal extends Goal {
        protected final JellyfishEntity mob;
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

        public AttackGoal(JellyfishEntity p_25552_, double p_25553_, boolean p_25554_) {
            this.mob = p_25552_;
            this.speedModifier = p_25553_;
            this.followingTargetEvenIfNotSeen = p_25554_;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        public boolean canUse() {
            return this.mob.actuallyPhase==PhaseAttack.ATTACK && canStart();
        }

        public boolean canStart(){
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
            this.resetAttackCooldown();
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
                    } else if (d0 > 256.0D) {
                        this.ticksUntilNextPathRecalculation += 5;
                    }

                    if (!this.mob.getNavigation().moveTo(livingentity, this.speedModifier)) {
                        this.ticksUntilNextPathRecalculation += 15;
                    }

                    this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);
                }

                this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
                this.checkAndPerformAttack(livingentity, d0);
            }
        }

        protected void checkAndPerformAttack(LivingEntity p_25557_, double p_25558_) {
            double d0 = this.getAttackReachSqr(p_25557_);
            if (p_25558_ <= d0) {
                p_25557_.hurt(DamageSource.MAGIC,10.0F);
            }

        }

        protected void resetAttackCooldown() {
            this.mob.attackTimer=300;
            this.mob.level.broadcastEntityEvent(this.mob,(byte) 4);
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

    public enum PhaseAttack{
        ATTACK,
        SUMMONING,
        LAZER,
        SPIN_AROUND;
    }
}
