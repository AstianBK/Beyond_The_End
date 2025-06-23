package com.TBK.beyond_the_end.server.entity.phase;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.server.entity.FallenDragonEntity;
import com.TBK.beyond_the_end.server.network.PacketHandler;
import com.TBK.beyond_the_end.server.network.message.PacketTargetDragon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class ContraAttack extends AbstractDragonPhaseInstance{
    private static final TargetingConditions NEW_TARGET_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight();
    private boolean firstTick;
    @Nullable
    private Path currentPath;
    @Nullable
    private Vec3 targetLocation;

    public ContraAttack(FallenDragonEntity p_31370_) {
        super(p_31370_);
    }

    public void doServerTick() {
        if (!this.firstTick && this.currentPath != null) {
            BlockPos blockpos = this.dragon.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(0, 60, 0));
            if (!blockpos.closerToCenterThan(this.dragon.position(), 10.0D)) {
                double d0 = 64.0D;
                Player player = this.dragon.level.getNearestPlayer(NEW_TARGET_TARGETING, this.dragon, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
                if (player != null) {
                    d0 = blockpos.distToCenterSqr(player.position()) / 512.0D;
                }

                if (player != null && (this.dragon.getRandom().nextInt(Mth.abs((int)d0) + 2) == 0 || this.dragon.getRandom().nextInt(2) == 0)) {
                    this.strafePlayer(player);
                }
            }
        } else {
            this.firstTick = false;
            this.findNewTarget();
        }

    }

    public void begin() {
        this.firstTick = true;
        this.currentPath = null;
        this.targetLocation = null;
    }

    private void strafePlayer(Player p_31237_) {
        if(this.dragon.canFly()){
            this.dragon.phaseManager.setPhase(FallenDragonPhase.SKYFALL);
            this.dragon.phaseManager.getPhase(FallenDragonPhase.SKYFALL).setTarget(p_31237_);
            PacketHandler.sendToAllTracking(new PacketTargetDragon(this.dragon.getId(),p_31237_.getId(),0),this.dragon);
        }
    }

    private void findNewTarget() {

        int i = this.dragon.findClosestNode();
        Vec3 vec3 = this.dragon.getHeadLookVector(1.0F);
        int j = this.dragon.findClosestNode(-vec3.x * 40.0D, 70.0D, -vec3.z * 40.0D);
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
        this.navigateToNextPathNode();
    }

    private void navigateToNextPathNode() {
        if (this.currentPath != null) {
            this.currentPath.advance();
            if (!this.currentPath.isDone()) {
                Vec3i vec3i = this.currentPath.getNextNodePos();
                this.currentPath.advance();

                double d0;
                do {
                    d0 = (double)((float)vec3i.getY() + this.dragon.getRandom().nextFloat() * 20.0F);
                } while(d0 < (double)vec3i.getY());

                this.targetLocation = new Vec3((double)vec3i.getX(), d0, (double)vec3i.getZ());
            }
        }

    }

    @Nullable
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    public FallenDragonPhase<? extends FallenDragonInstance> getPhase() {
        return FallenDragonPhase.CONTRA_ATTACK;
    }

}
