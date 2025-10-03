package com.TBK.beyondtheend.server.entity.phase;

import com.TBK.beyondtheend.BeyondTheEnd;
import com.TBK.beyondtheend.server.entity.FallenDragonEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import javax.annotation.Nullable;

public class MeleeAttackAttack extends AbstractDragonPhaseInstance {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int FIREBALL_CHARGE_AMOUNT = 5;
    private int fireballCharge;
    @Nullable
    private Path currentPath;
    @Nullable
    private Vec3 targetLocation;
    @Nullable
    private LivingEntity attackTarget;
    private boolean holdingPatternClockwise;
    public int attackTime=0;

    public MeleeAttackAttack(FallenDragonEntity p_31357_) {
        super(p_31357_);
    }

    public void doServerTick() {
        if (this.attackTarget == null) {
            BeyondTheEnd.LOGGER.warn("Skipping player strafe phase because no player was found");
            this.dragon.phaseManager.setPhase(FallenDragonPhase.HOLDING_PATTERN);
        } else {
            BeyondTheEnd.LOGGER.debug("Empenzo el Ataque melee");

            if (this.currentPath != null && this.currentPath.isDone()) {
                double d0 = this.attackTarget.getX();
                double d1 = this.attackTarget.getZ();
                double d2 = d0 - this.dragon.getX();
                double d3 = d1 - this.dragon.getZ();
                double d4 = Math.sqrt(d2 * d2 + d3 * d3);
                double d5 = Math.min((double)0.4F + d4 / 80.0D - 1.0D, 10.0D);
                this.targetLocation = new Vec3(d0, this.attackTarget.getY(), d1);
            }

            double d12 = this.targetLocation == null ? 0.0D : this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
            if (d12 < 100.0D || d12 > 22500.0D) {
                this.findNewTarget();
            }

            double d13 = 64.0D;
            if (this.attackTarget.distanceToSqr(this.dragon) < 4096.0D) {
                if (this.dragon.hasLineOfSight(this.attackTarget)) {
                    if(this.attackTarget.distanceToSqr(this.dragon)<36.0D && this.dragon.isOnGround() && this.attackTime<=0){
                        this.dragon.level.broadcastEntityEvent(this.dragon,(byte) 8);
                        this.attackTime=60;
                    }

                    if(this.attackTime==30){
                        this.dragon.doHurtTarget(this.attackTarget);
                    }
                    this.attackTime--;
                } else if (this.fireballCharge > 0) {
                    --this.fireballCharge;
                }
            } else if (this.fireballCharge > 0) {
                --this.fireballCharge;
            }

        }
    }



    private void findNewTarget() {
        if (this.currentPath == null || this.currentPath.isDone()) {
            int i = this.dragon.findClosestNode();
            int j = i;
            if (this.dragon.getRandom().nextInt(8) == 0) {
                this.holdingPatternClockwise = !this.holdingPatternClockwise;
                j = i + 6;
            }

            if (this.holdingPatternClockwise) {
                ++j;
            } else {
                --j;
            }

            if (this.dragon.getDragonFight() != null && this.dragon.getDragonFight().getCrystalsAlive() > 0) {
                j %= 12;
                if (j < 0) {
                    j += 12;
                }
            } else {
                j -= 12;
                j &= 7;
                j += 12;
            }

            this.currentPath = this.dragon.findPath(i, j, (Node)null);
            if (this.currentPath != null) {
                this.currentPath.advance();
            }
        }

        this.navigateToNextPathNode();
    }

    private void navigateToNextPathNode() {
        if (this.currentPath != null && !this.currentPath.isDone()) {
            Vec3i vec3i = this.currentPath.getNextNodePos();
            this.currentPath.advance();
            double d0 = (double)vec3i.getX();
            double d2 = (double)vec3i.getZ();

            double d1;
            do {
                d1 = (double)((float)vec3i.getY() + this.dragon.getRandom().nextFloat() * 20.0F);
            } while(d1 < (double)vec3i.getY());

            this.targetLocation = new Vec3(d0, d1, d2);
        }

    }

    public void begin() {
        this.fireballCharge = 0;
        this.targetLocation = null;
        this.currentPath = null;
        this.attackTarget = null;
    }

    public void setTarget(LivingEntity p_31359_) {
        this.attackTarget = p_31359_;
        int i = this.dragon.findClosestNode();
        int j = this.dragon.findClosestNode(this.attackTarget.getX(), this.attackTarget.getY(), this.attackTarget.getZ());
        int k = this.attackTarget.getBlockX();
        int l = this.attackTarget.getBlockZ();
        double d0 = (double)k - this.dragon.getX();
        double d1 = (double)l - this.dragon.getZ();
        double d2 = Math.sqrt(d0 * d0 + d1 * d1);
        double d3 = Math.min((double)0.4F + d2 / 80.0D - 1.0D, 10.0D);
        int i1 = Mth.floor(this.attackTarget.getY() + d3);
        Node node = new Node(k, i1, l);
        this.currentPath = this.dragon.findPath(i, j, node);
        if (this.currentPath != null) {
            this.currentPath.advance();
            this.navigateToNextPathNode();
        }

    }

    @Nullable
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    @Override
    public FallenDragonPhase<? extends FallenDragonInstance> getPhase() {
        return FallenDragonPhase.SKYFALL;
    }
}
