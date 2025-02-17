package com.TBK.beyond_the_end.server.entity;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.common.Util;
import com.TBK.beyond_the_end.common.registry.BKEntityType;
import com.TBK.beyond_the_end.server.capabilities.PortalPlayer;
import com.TBK.beyond_the_end.server.capabilities.PortalPlayerCapability;
import com.TBK.beyond_the_end.server.entity.projectile.ChargeFlash;
import com.TBK.beyond_the_end.server.entity.projectile.ChargeFollowing;
import com.TBK.beyond_the_end.server.network.PacketHandler;
import com.TBK.beyond_the_end.server.network.message.PacketNextActionJellyfish;
import com.TBK.beyond_the_end.server.network.message.PacketSync;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public class JellyfishMinionEntity extends PathfinderMob {
    public int idleTimer = 0;
    public int shootTimer = 0;
    public int spawnTimer = 0;
    private final double circleRadius = 50.0D;
    private double circlingAngle = 0.0F;
    private Vec3 circlingPosition;
    public boolean rot=false;
    public double offsetX=0.0D;
    public double offsetZ=0.0D;
    public double heightOffset = 0.0D;
    private final double speed = 0.25F;
    public Vec3 directionBlock=Vec3.ZERO;
    public PhaseAttack actuallyPhase= PhaseAttack.SPIN_AROUND;
    public AnimationState idle = new AnimationState();
    public AnimationState shoot = new AnimationState();
    public int nextTimer=0;
    public int maxNextTimer=50;
    public BlockPos origin= new BlockPos(0,70,0);

    public JellyfishEntity jellyfish;
    private int discardTimer=0;

    public JellyfishMinionEntity(EntityType<? extends PathfinderMob> p_21368_, Level p_21369_) {
        super(p_21368_, p_21369_);
        this.moveControl = new JellyfishMinionEntityMoveControl(this);
    }

    public BlockPos getBoundOrigin(){
        return this.origin;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.FOLLOW_RANGE,100.0D)
                .add(Attributes.FLYING_SPEED,0.35D);
    }

    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {

    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0,new JellyfishMinionEntityRandomMoveGoal());
        this.goalSelector.addGoal(1,new MinionShoot(this));
        this.targetSelector.addGoal(1,new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this, Player.class,true));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_21484_) {
        super.addAdditionalSaveData(p_21484_);
        if(this.origin!=null){
            p_21484_.putInt("x",this.origin.getX());
            p_21484_.putInt("y",this.origin.getY());
            p_21484_.putInt("z",this.origin.getZ());
        }
        p_21484_.putDouble("height",this.heightOffset);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_21450_) {
        super.readAdditionalSaveData(p_21450_);
        if(p_21450_.contains("x") && p_21450_.contains("y") && p_21450_.contains("z")){
            this.origin=new BlockPos(p_21450_.getInt("x"),p_21450_.getInt("y"),p_21450_.getInt("z"));
        }
        this.heightOffset = p_21450_.getDouble("height");
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);

        if(this.actuallyPhase == PhaseAttack.SPAWN){
            this.spawnTimer++;
            if(this.spawnTimer<40){
                this.setDeltaMovement(this.getDeltaMovement().add(0.0F,0.01F,0.0F));
            }else {
                if(!this.level.isClientSide){
                    this.actuallyPhase=PhaseAttack.SPIN_AROUND;
                    this.setActionForID(0);
                    PacketHandler.sendToAllTracking(new PacketNextActionJellyfish(this.getId(),0,0),this);
                }
            }
        }

        if(this.actuallyPhase== PhaseAttack.SPIN_AROUND){
            this.nextTimer++;
        }

        if(!this.level.isClientSide){
            if(this.jellyfish==null && this.discardTimer++>100){
                this.discard();
            }

            if(this.nextTimer>this.maxNextTimer && this.actuallyPhase == PhaseAttack.SPIN_AROUND){
                int time= 60 + this.level.random.nextInt(0,5)*this.level.random.nextInt(0,5);
                this.maxNextTimer=time;
                this.nextTimer=0;
                int nextAction=this.level.random.nextInt(0,2);
                this.setActionForID(nextAction);
                PacketHandler.sendToAllTracking(new PacketNextActionJellyfish(this.getId(),time,nextAction),this);
            }
        }

        if(this.shootTimer > 0){
            this.shootTimer--;
            if(this.shootTimer==0){
                if(!this.level.isClientSide){
                    if(this.getTarget()!=null && this.shootTimer<=0){
                        if(this.level.random.nextFloat()>0.15F){
                            ChargeFlash ball = new ChargeFlash(this.level,this);
                            ball.setPos(this.getEyePosition());
                            ball.shoot(this.getTarget().getEyePosition().x-this.getEyePosition().x,this.getTarget().getEyePosition().y-this.getEyePosition().y,this.getTarget().getEyePosition().z-this.getEyePosition().z,1.0F,1.0F);
                            this.level.addFreshEntity(ball);
                        }else {
                            this.level.addFreshEntity(new ChargeFollowing(this.level,this,this.getTarget()));
                            this.playSound(SoundEvents.SHULKER_SHOOT, 2.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                        }
                    }
                    this.setActionForID(0);
                    PacketHandler.sendToAllTracking(new PacketNextActionJellyfish(this.getId(),0,0),this);

                }
            }
        }

        if(this.level.isClientSide){
            this.clientAnim();
        }

        this.refreshDimensions();
    }

    public JellyfishEntity findJellyfish(ServerLevel level){
        List<? extends JellyfishEntity> list = level.getEntities(BKEntityType.JELLYFISH.get(), LivingEntity::isAlive);
        if (list.isEmpty()) {
            BeyondTheEnd.LOGGER.debug("Haven't seen the jelly, respawning it");
            return null;
        } else {
            BeyondTheEnd.LOGGER.debug("Haven't seen our jelly, but found another one to use.");
            this.jellyfish = list.get(0);
            return list.get(0);
        }
    }

    public void spinAround(){
        LivingEntity target = this.getTarget();
        Vec3 direction;
        if(target==null){
            target=this.jellyfish==null ? this.findJellyfish((ServerLevel) this.level) : this.jellyfish;
        }
        if(this.actuallyPhase== JellyfishMinionEntity.PhaseAttack.SPIN_AROUND ){
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


    private float lerpRotation(float currentYaw, float targetYaw, float maxTurnSpeed) {
        float deltaYaw = Mth.floor(targetYaw - currentYaw);
        return currentYaw > -269 ? currentYaw + Mth.clamp(deltaYaw, -maxTurnSpeed, maxTurnSpeed) : currentYaw+360;
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


    private double calculateHeightOffset(LivingEntity target) {
        double currentAltitude = this.getY();
        double targetAltitude = target.getY();
        double targetHeight = targetAltitude + 20.0D + this.heightOffset;
        return targetHeight - currentAltitude;
    }

    private double calculateHeightOffset(BlockPos target) {
        double currentAltitude = this.getY();
        double targetAltitude = target.getY();
        double targetHeight = targetAltitude + 20.0D  + this.heightOffset;
        return targetHeight - currentAltitude;
    }
    @Override
    public EntityDimensions getDimensions(Pose p_21047_) {
        return this.getType().getDimensions().scale(this.getScale(), 1.0F);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_21434_, DifficultyInstance p_21435_, MobSpawnType p_21436_, @Nullable SpawnGroupData p_21437_, @Nullable CompoundTag p_21438_) {
        this.origin=this.blockPosition();
        return super.finalizeSpawn(p_21434_, p_21435_, p_21436_, p_21437_, p_21438_);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if(!this.level.isClientSide){
            this.spinAround();
        }
    }


    @Override
    public void die(DamageSource p_21014_) {
        super.die(p_21014_);
        if(p_21014_.getEntity() instanceof Player player){
            PortalPlayer.get(player).ifPresent(e->{
                e.addCharge();
                if(!this.level.isClientSide){
                    PacketHandler.sendToPlayer(new PacketSync(e.getCharge(),e.animTimer), (ServerPlayer) player);
                }
            });
        }
    }

    @Override
    public void checkDespawn() {

    }

    public void clientAnim(){
        if(this.idleTimer<=0){
            this.idleTimer = 80;
            this.idle.start(this.tickCount);
            this.shootTimer = 0;
        }else {
            this.idleTimer--;
        }
    }

    @Override
    public void handleEntityEvent(byte p_21375_) {
        if(p_21375_==4){
            this.shootTimer =7;
            this.shoot.start(this.tickCount);
            this.idle.stop();
            this.idleTimer=7;
        }
        super.handleEntityEvent(p_21375_);
    }

    public void setActionForID(int idAction) {
        switch (idAction){
            case 0 ->{
                this.actuallyPhase= PhaseAttack.SPIN_AROUND;
            }
            case 1 ->{
                this.actuallyPhase= PhaseAttack.SHOOT;
            }
            case 2 ->{
                this.actuallyPhase= PhaseAttack.DEATH;
            }
            case 3 ->{
                this.actuallyPhase= PhaseAttack.SPAWN;
            }
        }
    }

    class JellyfishMinionEntityMoveControl extends MoveControl {
        public JellyfishMinionEntityMoveControl(JellyfishMinionEntity p_34062_) {
            super(p_34062_);
        }

        public void tick() {
            if (this.operation == MoveControl.Operation.MOVE_TO) {
                Vec3 vec3 = new Vec3(this.wantedX - JellyfishMinionEntity.this.getX(), this.wantedY - JellyfishMinionEntity.this.getY(), this.wantedZ - JellyfishMinionEntity.this.getZ());
                double d0 = vec3.length();
                if (d0 < JellyfishMinionEntity.this.getBoundingBox().getSize()) {
                    this.operation = MoveControl.Operation.WAIT;
                    JellyfishMinionEntity.this.setDeltaMovement(JellyfishMinionEntity.this.getDeltaMovement().scale(0.5D));
                } else {
                    JellyfishMinionEntity.this.setDeltaMovement(JellyfishMinionEntity.this.getDeltaMovement().add(vec3.scale(this.speedModifier * 0.05D / d0)));
                    if (JellyfishMinionEntity.this.getTarget() == null) {
                        Vec3 vec31 = JellyfishMinionEntity.this.getDeltaMovement();
                        JellyfishMinionEntity.this.setYRot(-((float)Mth.atan2(vec31.x, vec31.z)) * (180F / (float)Math.PI));
                    } else {
                        double d2 = JellyfishMinionEntity.this.getTarget().getX() - JellyfishMinionEntity.this.getX();
                        double d1 = JellyfishMinionEntity.this.getTarget().getZ() - JellyfishMinionEntity.this.getZ();
                        JellyfishMinionEntity.this.setYRot(-((float)Mth.atan2(d2, d1)) * (180F / (float)Math.PI));
                    }
                    JellyfishMinionEntity.this.yBodyRot = JellyfishMinionEntity.this.getYRot();
                }

            }
        }
    }

    class JellyfishMinionEntityRandomMoveGoal extends Goal {
        public JellyfishMinionEntityRandomMoveGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canUse() {
            return !JellyfishMinionEntity.this.getMoveControl().hasWanted();
        }

        public boolean canContinueToUse() {
            return false;
        }

        public void tick() {
            BlockPos blockpos = JellyfishMinionEntity.this.getBoundOrigin();
            if (blockpos == null) {
                blockpos = JellyfishMinionEntity.this.blockPosition();
            }

            for(int i = 0; i < 3; ++i) {
                BlockPos blockpos1 = blockpos.offset(JellyfishMinionEntity.this.random.nextInt(15) - 7, JellyfishMinionEntity.this.random.nextInt(11) - 5, JellyfishMinionEntity.this.random.nextInt(15) - 7);
                if (JellyfishMinionEntity.this.level.isEmptyBlock(blockpos1)) {
                    JellyfishMinionEntity.this.moveControl.setWantedPosition((double)blockpos1.getX() + 0.5D, (double)blockpos1.getY() + 0.5D, (double)blockpos1.getZ() + 0.5D, 0.25D);
                    if (JellyfishMinionEntity.this.getTarget() == null) {
                        JellyfishMinionEntity.this.getLookControl().setLookAt((double)blockpos1.getX() + 0.5D, (double)blockpos1.getY() + 0.5D, (double)blockpos1.getZ() + 0.5D, 180.0F, 20.0F);
                    }
                    break;
                }
            }

        }
    }

    static class MinionShoot extends Goal{
        private final JellyfishMinionEntity harpy;


        public MinionShoot(JellyfishMinionEntity harpy) {
            this.harpy = harpy;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        public boolean canUse() {
            return this.harpy.actuallyPhase== PhaseAttack.SHOOT;
        }

        @Override
        public void start() {
            super.start();
            this.harpy.shootTimer = 7;

            if(!this.harpy.level.isClientSide){
                this.harpy.level.broadcastEntityEvent(this.harpy,(byte) 4);
            }
        }

        public void tick() {

        }
        public boolean requiresUpdateEveryTick() {
            return true;
        }

    }




    public enum PhaseAttack{
        SPAWN,
        SHOOT,
        SPIN_AROUND,
        DEATH;
    }
}
