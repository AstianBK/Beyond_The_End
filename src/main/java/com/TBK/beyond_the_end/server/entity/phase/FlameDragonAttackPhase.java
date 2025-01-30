package com.TBK.beyond_the_end.server.entity.phase;

import com.TBK.beyond_the_end.server.entity.FallenDragonEntity;
import com.TBK.beyond_the_end.server.network.PacketHandler;
import com.TBK.beyond_the_end.server.network.message.PacketTargetDragon;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonStrafePlayerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import javax.annotation.Nullable;

public class FlameDragonAttackPhase extends AbstractDragonPhaseInstance {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int FIREBALL_CHARGE_AMOUNT = 5;
    private boolean isFlyMode;
    private int fireballCharge;
    private int waitTime;
    @Nullable
    private Path currentPath;
    @Nullable
    private Vec3 targetLocation;
    @Nullable
    private LivingEntity attackTarget;
    private int countFireball;
    private int maxCountFireball=0;

    public FlameDragonAttackPhase(FallenDragonEntity p_31357_) {
        super(p_31357_);
    }

    public void doServerTick() {
        if (this.attackTarget == null || (this.attackTarget instanceof Player player && player.isCreative())) {
            this.dragon.phaseManager.setPhase(FallenDragonPhase.HOLDING_PATTERN);
            if(!this.dragon.canFly()){
                this.dragon.initFall();
            }
        } else {
            Vec3 vec31 = (new Vec3(this.attackTarget.getX() - this.dragon.getX(), 0.0D, this.attackTarget.getZ() - this.dragon.getZ())).normalize();
            Vec3 vec3 = (new Vec3((double) Mth.sin(this.dragon.getYRot() * ((float)Math.PI / 180F)), 0.0D, (double)(-Mth.cos(this.dragon.getYRot() * ((float)Math.PI / 180F))))).normalize();
            float f1 = (float)vec3.dot(vec31);
            float f = (float)(Math.acos((double)f1) * (double)(180F / (float)Math.PI));
            f += 0.5F;
            if(!(f >= 0.0F && f < 10.0F)){
                this.targetLocation=this.attackTarget.getEyePosition();
            }else if(this.attackTarget.distanceToSqr(this.dragon) < 4096.0D){
                this.targetLocation = this.dragon.position();
            }else {
                this.targetLocation=this.attackTarget.getEyePosition();
            }
            if (this.attackTarget.distanceToSqr(this.dragon) < 4096.0D && f >= 0.0F && f < 10.0F) {
                this.waitTime=0;
                ++this.fireballCharge;
                if (this.fireballCharge >= 15) {
                    Vec3 vec32 = this.dragon.getViewVector(1.0F);
                    double d6 = this.dragon.head.getX();
                    double d7 = this.dragon.head.getY(0.5D) + 0.5D;
                    double d8 = this.dragon.head.getZ();
                    Vec3 deltaTarget = this.attackTarget.getDeltaMovement();
                    double d9 = this.attackTarget.getX() - d6 + deltaTarget.x;
                    double d10 = this.attackTarget.getY(0.5D) - d7;
                    double d11 = this.attackTarget.getZ() - d8 + deltaTarget.z;
                    if (!this.dragon.isSilent()) {
                        this.dragon.level.levelEvent((Player)null, 1017, this.dragon.blockPosition(), 0);
                    }

                    DragonFireball dragonfireball = new DragonFireball(this.dragon.level, this.dragon, d9, d10, d11);
                    dragonfireball.setOwner(this.dragon);
                    dragonfireball.moveTo(d6, d7, d8, 0.0F, 0.0F);
                    this.dragon.level.addFreshEntity(dragonfireball);
                    this.fireballCharge = 0;
                    if (this.currentPath != null) {
                        while(!this.currentPath.isDone()) {
                            this.currentPath.advance();
                        }
                    }
                    this.countFireball++;
                    if(this.countFireball>this.maxCountFireball-1){
                        if(!this.isFlyMode){
                            this.dragon.initFall();
                        }else {
                            if(this.dragon.level.random.nextFloat()<0.4F){
                                this.dragon.phaseManager.setPhase(FallenDragonPhase.CHARGING);
                                this.dragon.phaseManager.getPhase(FallenDragonPhase.CHARGING).setTarget(this.attackTarget);
                                PacketHandler.sendToAllTracking(new PacketTargetDragon(this.dragon.getId(),this.attackTarget.getId(),1),this.dragon);
                            }else {
                                this.dragon.phaseManager.setPhase(FallenDragonPhase.HOLDING_PATTERN);
                            }
                        }
                    }
                }
            }else {
                this.fireballCharge=0;
                this.waitTime++;
            }

            if(this.waitTime>300){
                if(!this.isFlyMode){
                    this.dragon.initFall();
                }
                if(this.dragon.level.random.nextFloat()<0.4F){
                    this.dragon.phaseManager.setPhase(FallenDragonPhase.CHARGING);
                    this.dragon.phaseManager.getPhase(FallenDragonPhase.CHARGING).setTarget(this.attackTarget);
                    PacketHandler.sendToAllTracking(new PacketTargetDragon(this.dragon.getId(),this.attackTarget.getId(),1),this.dragon);
                }else {
                    this.dragon.phaseManager.setPhase(FallenDragonPhase.HOLDING_PATTERN);
                }
            }
        }
    }

    public void begin() {
        this.fireballCharge = 0;
        this.targetLocation = null;
        this.currentPath = null;
        this.attackTarget = null;
        this.countFireball = 0;
        this.maxCountFireball = 10 + this.dragon.level.random.nextInt(0,10);
        this.waitTime = 0;
        this.isFlyMode=false;
    }

    @Override
    public void end() {
        this.fireballCharge = 0;
        this.countFireball = 0;
        this.waitTime = 0;
    }

    public void setTarget(LivingEntity p_31359_) {
        this.attackTarget = p_31359_;
    }

    public void setIsFlyMode(boolean p_31359_) {
        this.isFlyMode = p_31359_;
    }


    @Nullable
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    @Override
    public FallenDragonPhase<? extends FallenDragonInstance> getPhase() {
        return FallenDragonPhase.FLAME;
    }
}
