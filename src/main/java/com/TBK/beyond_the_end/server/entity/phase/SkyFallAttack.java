package com.TBK.beyond_the_end.server.entity.phase;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.client.particle.BKParticles;
import com.TBK.beyond_the_end.server.entity.FallenDragonEntity;
import com.TBK.beyond_the_end.server.network.PacketHandler;
import com.TBK.beyond_the_end.server.network.message.PacketFlameParticles;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.PartEntity;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SkyFallAttack extends AbstractDragonPhaseInstance {
    private static final Logger LOGGER = LogUtils.getLogger();
    private int fireballCharge;
    private int flameTime;
    @Nullable
    private Path currentPath;
    @Nullable
    private Vec3 targetLocation;
    @Nullable
    private LivingEntity attackTarget;
    private boolean holdingPatternClockwise;

    public SkyFallAttack(FallenDragonEntity p_31357_) {
        super(p_31357_);
    }


    @Override
    public float getFlySpeed() {
        return this.dragon.isFlame ? 3.0F : 8.0F;
    }

    public void doServerTick() {
        if (this.attackTarget == null) {
            this.dragon.phaseManager.setPhase(FallenDragonPhase.HOLDING_PATTERN);
        } else {
            if (this.currentPath != null && this.currentPath.isDone()) {
                double d0 = this.attackTarget.getX();
                double d1 = this.attackTarget.getZ();
                this.targetLocation = new Vec3(d0, this.attackTarget.getY()+10.0D, d1);
            }

            double d12 = this.targetLocation == null ? 0.0D : this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());

            if (d12 < 100.0D || d12 > 22500.0D) {
                this.findNewTarget();
            }

            Vec3 pos=new Vec3(this.dragon.getX(),this.dragon.level.getHeight(Heightmap.Types.WORLD_SURFACE, (int) this.dragon.getX(), (int) this.dragon.getZ()),this.dragon.getZ());

            if (this.dragon.getY() > this.attackTarget.getY()+5.0D && this.dragon.getY() < this.attackTarget.getY()+14.0D) {
                this.flameTime++;
                if (this.dragon.hasLineOfSight(this.attackTarget)) {
                    this.fireballCharge++;
                    if(this.fireballCharge>=5 && this.dragon.distanceToSqr(pos)<1024){
                        this.fireballCharge=0;
                        this.spawnAreaDamage(new BlockPos(pos));
                    }
                    List<HitResult> results=SkyFallAttack.raycastForEntity(this.dragon.level,this.dragon,100,true,3);
                    for (HitResult result:results){
                        if(result.getType().equals(HitResult.Type.ENTITY)){
                            ((EntityHitResult)result).getEntity().hurt(DamageSource.indirectMagic(this.dragon,null),3F);
                        }
                    }
                    this.dragon.level.broadcastEntityEvent(this.dragon,(byte) 4);
                } else if (this.fireballCharge > 0) {
                    this.fireballCharge=0;
                    this.dragon.level.broadcastEntityEvent(this.dragon,(byte) 65);
                }
            } else if (this.fireballCharge > 0) {
                this.dragon.level.broadcastEntityEvent(this.dragon,(byte) 65);
                this.fireballCharge=0;
                double d0 = this.attackTarget.getX();
                double d1 = this.attackTarget.getZ();
                this.targetLocation = new Vec3(d0, this.attackTarget.getY()+10.0D, d1);
                this.findNewTarget();

            }



            if(this.flameTime>400){
                this.dragon.teleportTowards(this.attackTarget);
                this.dragon.phaseManager.setPhase(FallenDragonPhase.FLAME);
                this.dragon.phaseManager.getPhase(FallenDragonPhase.FLAME).setIsFlyMode(true);
                this.dragon.phaseManager.getPhase(FallenDragonPhase.FLAME).setTarget(this.attackTarget);
            }

        }
    }
    public void spawnAreaDamage(BlockPos pos){
        AreaEffectCloud areaeffectcloud = new AreaEffectCloud(this.dragon.level, this.dragon.getX(), this.dragon.getY(),this.dragon.getZ());
        areaeffectcloud.setOwner(this.dragon);

        areaeffectcloud.setParticle(ParticleTypes.DRAGON_BREATH);
        areaeffectcloud.setRadius(8.0F);
        areaeffectcloud.setDuration(100);
        areaeffectcloud.setRadiusPerTick((7.0F - areaeffectcloud.getRadius()) / (float)areaeffectcloud.getDuration());
        areaeffectcloud.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 1));
        areaeffectcloud.setPos(pos.getX(),pos.getY(),pos.getZ());
        this.dragon.level.addFreshEntity(areaeffectcloud);
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
                d1 = (double)((float)vec3i.getY() + this.dragon.getRandom().nextFloat() * 15.0F);
            } while(d1 < (double)vec3i.getY());

            this.targetLocation = new Vec3(d0, d1, d2);
        }

    }

    public void begin() {
        this.fireballCharge = 0;
        this.targetLocation = null;
        this.currentPath = null;
        this.attackTarget = null;
        this.flameTime = 0;
    }

    @Override
    public void end() {
        this.fireballCharge = 0;
    }

    public void setTarget(LivingEntity p_31359_) {
        if(p_31359_!=null){
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

    }

    @Nullable
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    @Override
    public FallenDragonPhase<? extends FallenDragonInstance> getPhase() {
        return FallenDragonPhase.SKYFALL;
    }

    public static List<HitResult> raycastForEntity(Level level, FallenDragonEntity originEntity, float distance, boolean checkForBlocks, float bbInflation) {
        Vec3 start = originEntity.head.position();
        Vec3 end = originEntity.getLookAngle().add(Math.cos((float)originEntity.tickCount*0.1F)*0.523599F,-0.785398,Math.cos((float)originEntity.tickCount*0.1F)*0.523599F).multiply(-1.0F,1.0F,-1.0F).normalize().scale(distance).add(start.x,level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) start.x, (int) start.z),start.z);

        return internalRaycastForEntity(level, originEntity, start, end, checkForBlocks, 10.0F);
    }

    private static List<HitResult> internalRaycastForEntity(Level level, FallenDragonEntity originEntity, Vec3 start, Vec3 end, boolean checkForBlocks, float bbInflation) {
        BlockHitResult blockHitResult = null;
        if (checkForBlocks) {
            blockHitResult = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, originEntity));
            end = blockHitResult.getLocation();
        }
        AABB range = originEntity.getBoundingBox().expandTowards(end.subtract(start));


        if(!level.isClientSide){
            PacketHandler.sendToAllTracking(new PacketFlameParticles(start,end),originEntity);
        }

        List<HitResult> hits = new ArrayList<>();
        List<? extends Entity> entities = level.getEntities(originEntity, range, e-> e instanceof LivingEntity entity);
        for (Entity target : entities) {
            HitResult hit = checkEntityIntersecting(target, start, end, bbInflation);
            if (hit.getType() != HitResult.Type.MISS)
                hits.add(hit);
        }

        if (!hits.isEmpty()) {
            hits.sort((o1, o2) -> (int) o1.getLocation().distanceToSqr(start));
            return hits;
        } else if (checkForBlocks) {
            return List.of(blockHitResult);
        }
        return List.of(BlockHitResult.miss(end, Direction.UP, new BlockPos(end)));
    }


    public static HitResult checkEntityIntersecting(Entity entity, Vec3 start, Vec3 end, float bbInflation) {
        Vec3 hitPos = null;
        if (entity.isMultipartEntity()) {
            for (PartEntity<?> p : entity.getParts()) {
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
}
