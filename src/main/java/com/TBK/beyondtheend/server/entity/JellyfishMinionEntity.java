package com.TBK.beyondtheend.server.entity;

import com.TBK.beyondtheend.common.registry.BKEntityType;
import com.TBK.beyondtheend.common.registry.BTESounds;
import com.TBK.beyondtheend.server.capabilities.PortalPlayer;
import com.TBK.beyondtheend.server.entity.projectile.ChargeFlash;
import com.TBK.beyondtheend.server.entity.projectile.ChargeFollowing;
import com.TBK.beyondtheend.server.network.PacketHandler;
import com.TBK.beyondtheend.server.network.message.PacketNextActionJellyfish;
import com.TBK.beyondtheend.server.network.message.PacketSync;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class JellyfishMinionEntity extends PathfinderMob {
    // Ticks entre disparos (se elige un valor aleatorio entre ambos limites).
    private static final int SHOOT_MIN_DELAY = 120;
    private static final int SHOOT_MAX_DELAY = 180;

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

    public JellyfishEntity jellyfish;
    private int discardTimer=0;

    public JellyfishMinionEntity(EntityType<? extends PathfinderMob> p_21368_, Level p_21369_) {
        super(p_21368_, p_21369_);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.FOLLOW_RANGE,100.0D)
                .add(Attributes.FLYING_SPEED,0.7D);
    }

    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {

    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1,new MinionShoot(this));
        this.targetSelector.addGoal(1,new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this, Player.class,true));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_21484_) {
        super.addAdditionalSaveData(p_21484_);
        p_21484_.putDouble("height",this.heightOffset);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_21239_) {
        return BTESounds.JELLYFISH_HURT.get();
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_21450_) {
        super.readAdditionalSaveData(p_21450_);
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
            this.checkJellyfish();

            if(this.nextTimer>this.maxNextTimer && this.actuallyPhase == PhaseAttack.SPIN_AROUND && this.getTarget()!=null){
                int time = SHOOT_MIN_DELAY + this.level.random.nextInt(SHOOT_MAX_DELAY - SHOOT_MIN_DELAY + 1);
                this.maxNextTimer=time;
                this.nextTimer=0;
                this.setActionForID(1);
                PacketHandler.sendToAllTracking(new PacketNextActionJellyfish(this.getId(),time,1),this);
            }
        }

        if(this.shootTimer > 0){
            this.shootTimer--;
            if(this.shootTimer==0){
                if(!this.level.isClientSide){
                    boolean flag = this.level.random.nextFloat()>0.15F;
                    float damageScale = this.jellyfish!=null ? this.jellyfish.getDamageScale() : 1.0F;
                    if(this.getTarget()!=null){
                        if(flag){
                            ChargeFlash ball = new ChargeFlash(this.level,this);
                            ball.setDamageScale(damageScale);
                            ball.setPos(this.getEyePosition());
                            ball.shoot(this.getTarget().getEyePosition().x-this.getEyePosition().x,this.getTarget().getEyePosition().y-this.getEyePosition().y,this.getTarget().getEyePosition().z-this.getEyePosition().z,1.0F,1.0F);
                            this.level.addFreshEntity(ball);
                            this.level.broadcastEntityEvent(this,(byte) 8);
                        }else {
                            ChargeFollowing following = new ChargeFollowing(this.level,this,this.getTarget());
                            following.setDamageScale(damageScale);
                            this.level.addFreshEntity(following);
                            this.level.broadcastEntityEvent(this,(byte) 9);
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
            return null;
        }
        this.jellyfish = list.get(0);
        return this.jellyfish;
    }

    private void checkJellyfish(){
        if(this.jellyfish==null && this.tickCount%20==0){
            this.findJellyfish((ServerLevel) this.level);
        }

        if(this.jellyfish==null){
            if(this.discardTimer++>100){
                this.discard();
            }
        }else if(!this.jellyfish.isAlive()){
            if(!this.isDeadOrDying()){
                this.hurt(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
            }
        }else {
            this.discardTimer=0;
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
            this.heightOffset = Math.cos(this.circlingAngle) * 9;
            if (target != null) {
                double heightOffset = this.calculateHeightOffset(target);
                this.circlingPosition = new Vec3(target.getX() + offsetX, target.getY() + heightOffset, target.getZ() + offsetZ);
                direction = this.circlingPosition.subtract(this.position()).normalize();
                this.setDeltaMovement(direction.scale(this.speed));

                this.rotateTowardsTarget(target);
            }else {
                if(this.circlingPosition!=null){
                    BlockPos pos = new BlockPos(this.circlingPosition);
                    double heightOffset = this.calculateHeightOffset(pos);
                    this.circlingPosition = new Vec3(pos.getX() + offsetX, pos.getY() + heightOffset, pos.getZ() + offsetZ);
                    direction = this.circlingPosition.subtract(this.position()).normalize();
                    this.setDeltaMovement(direction.scale(this.speed));

                    this.rotateTowardsTarget(pos);

                }
            }
        }else {
            if(target==null){
                BlockPos pos = new BlockPos(0,this.level.getHeight(Heightmap.Types.WORLD_SURFACE,0,0),0);
                this.rotateTowardsTarget(pos);
            }else {
                this.rotateTowardsTarget(target);
            }
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
        double targetHeight = targetAltitude + 10.0D + this.heightOffset;
        return targetHeight - currentAltitude;
    }

    private double calculateHeightOffset(BlockPos target) {
        double currentAltitude = this.getY();
        double targetAltitude = target.getY();
        double targetHeight = targetAltitude + 10.0D  + this.heightOffset;
        return targetHeight - currentAltitude;
    }
    @Override
    public EntityDimensions getDimensions(Pose p_21047_) {
        return this.getType().getDimensions().scale(this.getScale(), 1.0F);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if(!this.level.isClientSide){
            this.spinAround();
        }
    }

    @Override
    protected float getSoundVolume() {
        return super.getSoundVolume();
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
        }else if(p_21375_ == 8){
            this.level.playLocalSound(this.getX(),this.getY(),this.getZ(),BTESounds.JELLYFISH_SHOOT1.get(),SoundSource.HOSTILE,5.0F,1.0F,false);
        }else if(p_21375_ == 9){
            this.level.playLocalSound(this.getX(),this.getY(),this.getZ(),BTESounds.JELLYFISH_SHOOT2.get(),SoundSource.HOSTILE,5.0F,1.0F,false);
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
            case 3 ->{
                this.actuallyPhase= PhaseAttack.SPAWN;
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
        SPIN_AROUND;
    }
}
