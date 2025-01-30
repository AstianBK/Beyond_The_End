package com.TBK.beyond_the_end.server.entity.phase;

import com.TBK.beyond_the_end.server.entity.FallenDragonEntity;
import com.TBK.beyond_the_end.server.network.PacketHandler;
import com.TBK.beyond_the_end.server.network.message.PacketTargetAttack;
import com.TBK.beyond_the_end.server.network.message.PacketTargetDragon;
import com.mojang.logging.LogUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import javax.annotation.Nullable;

public class ChargingPhase extends AbstractDragonPhaseInstance{
    private static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    private Vec3 targetLocation;
    private LivingEntity target;
    private int timeSinceCharge;

    public ChargingPhase(FallenDragonEntity p_31206_) {
        super(p_31206_);
    }

    @Override
    public void doClientTick() {
        super.doClientTick();
        if(this.dragon.level.isClientSide){
            for (int i=0;i<30;i++){
                this.dragon.particleCharged();
            }
        }
    }

    public void doServerTick() {
        if (this.targetLocation == null || this.target==null) {
            this.dragon.isCharging=false;
            this.dragon.level.broadcastEntityEvent(this.dragon,(byte) 69);
            this.playSkyfall();
        } else if (this.timeSinceCharge > 0 && this.timeSinceCharge++ >= 300) {
            this.dragon.isCharging=false;
            this.dragon.level.broadcastEntityEvent(this.dragon,(byte) 69);
            this.playSkyfall();
        } else {
            double d0 = this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
            if (d0 < 100.0D || d0 > 22500.0D) {
                ++this.timeSinceCharge;
            }
            this.targetLocation=this.target.position();
            float dx = this.dragon.distanceTo(target);
            Vec3 vector3d1 = new Vec3(target.getX() - this.dragon.getX(), 0.0D, target.getZ() - this.dragon.getZ());

            if(dx < 1024D && !this.dragon.isCharging){
                if (vector3d1.lengthSqr() > 1.0E-7D) {
                    vector3d1 = vector3d1.normalize().scale(Math.min(dx, 15) * 0.2F);
                }
                this.dragon.setDeltaMovement(vector3d1.x, vector3d1.y - 0.6F - 0.1F * Mth.clamp(target.getEyeY() - this.dragon.getY(), 0, 10), vector3d1.z);
                this.dragon.isCharging=true;
                this.dragon.level.broadcastEntityEvent(this.dragon,(byte) 68);
            }else if(vector3d1.lengthSqr() < 1.0E-7D){
                this.dragon.isCharging=false;
                this.dragon.level.broadcastEntityEvent(this.dragon,(byte) 69);
            }
        }
    }

    public void playSkyfall() {
        if(this.target!=null){
            int random = this.dragon.getRandom().nextInt(0,4);
            int x= (int) ((target.getX())) + this.dragon.posX[random]*this.dragon.getRandom().nextInt(16,64);
            int z= (int) ((target.getZ())) + this.dragon.posZ[random]*this.dragon.getRandom().nextInt(16,64);
            this.dragon.teleportTo(x,85,z);
            this.dragon.phaseManager.setPhase(FallenDragonPhase.SKYFALL);
            this.dragon.phaseManager.getPhase(FallenDragonPhase.SKYFALL).setTarget(this.target);
            PacketHandler.sendToAllTracking(new PacketTargetDragon(this.dragon.getId(),this.target.getId(),0),this.dragon);
        }else {
            this.dragon.phaseManager.setPhase(FallenDragonPhase.HOLDING_PATTERN);
        }
    }


    public void begin() {
        this.targetLocation = null;
        this.timeSinceCharge = 0;
    }

    public void setTarget(LivingEntity p_31208_) {
        this.target=p_31208_;
        this.targetLocation = p_31208_.position();
    }

    public float getFlySpeed() {
        return 5.0F;
    }

    @Nullable
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    @Override
    public FallenDragonPhase<? extends FallenDragonInstance> getPhase() {
        return FallenDragonPhase.CHARGING;
    }
}
