package com.TBK.beyond_the_end.server.entity.phase;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.server.entity.FallenDragonEntity;
import com.TBK.beyond_the_end.server.network.PacketHandler;
import com.TBK.beyond_the_end.server.network.message.PacketTargetDragon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class FallenDragonHoldingPatternPhase extends AbstractDragonPhaseInstance{
    private static final TargetingConditions NEW_TARGET_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight();
    @Nullable
    private Path currentPath;
    @Nullable
    private Vec3 targetLocation;
    private boolean clockwise;

    public FallenDragonHoldingPatternPhase(FallenDragonEntity p_31230_) {
        super(p_31230_);
    }

    public FallenDragonPhase<? extends FallenDragonInstance> getPhase() {
        return FallenDragonPhase.HOLDING_PATTERN;
    }

    public void doServerTick() {
        double d0 = this.targetLocation == null ? 0.0D : this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if (d0 < 100.0D || d0 > 22500.0D || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
            BeyondTheEnd.LOGGER.warn("Busca target.");
            this.findNewTarget();
        }

    }

    public void begin() {
        this.currentPath = null;
        this.targetLocation = null;
    }

    @Nullable
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    private void findNewTarget() {
        if (this.currentPath != null && this.currentPath.isDone()) {
            BlockPos blockpos = this.dragon.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(0, 60, 0));
            int i = this.dragon.getDragonFight() == null ? 0 : this.dragon.getDragonFight().getCrystalsAlive();
            /*if (this.dragon.getRandom().nextInt(i + 3) == 0) {
                this.dragon.getPhaseManager().setPhase(EnderDragonPhase.LANDING_APPROACH);
                return;
            }*/

            double d0 = 64.0D;
            Player player = this.dragon.level.getNearestPlayer(NEW_TARGET_TARGETING, this.dragon, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
            if (player != null) {
                d0 = blockpos.distToCenterSqr(player.position()) / 512.0D;
            }

            if (player != null && (this.dragon.getRandom().nextInt(Mth.abs((int)d0) + 2) == 0 || this.dragon.getRandom().nextInt(i + 2) == 0)) {
                this.strafePlayer(player);
                return;
            }
        }

        if (this.currentPath == null || this.currentPath.isDone()) {
            int j = this.dragon.findClosestNode();
            int k = j;
            if (this.dragon.getRandom().nextInt(8) == 0) {
                this.clockwise = !this.clockwise;
                k = j + 6;
            }

            if (this.clockwise) {
                ++k;
            } else {
                --k;
            }

            if (this.dragon.getDragonFight() != null) {
                k %= 12;
                if (k < 0) {
                    k += 12;
                }
            } else {
                k -= 12;
                k &= 7;
                k += 12;
            }

            this.currentPath = this.dragon.findPath(j, k, (Node)null);
            if (this.currentPath != null) {
                this.currentPath.advance();
            }
        }

        this.navigateToNextPathNode();
    }

    private void strafePlayer(Player p_31237_) {
        if(this.dragon.canFly()){
            this.dragon.phaseManager.setPhase(FallenDragonPhase.CONTRA_ATTACK);
        }
    }

    private void navigateToNextPathNode() {
        if (this.currentPath != null && !this.currentPath.isDone()) {
            Vec3i vec3i = this.currentPath.getNextNodePos();
            this.currentPath.advance();
            double d0 = (double)vec3i.getX();
            double d1 = (double)vec3i.getZ();

            double d2;
            do {
                d2 = (double)((float)vec3i.getY() + this.dragon.getRandom().nextFloat() * 5.0F);
            } while(d2 < (double)vec3i.getY());

            this.targetLocation = new Vec3(d0, d2, d1);
        }

    }

    public void onCrystalDestroyed(EndCrystal p_31232_, BlockPos p_31233_, DamageSource p_31234_, @Nullable Player p_31235_) {
        if (p_31235_ != null && this.dragon.canAttack(p_31235_)) {
            this.strafePlayer(p_31235_);
        }

    }
}
