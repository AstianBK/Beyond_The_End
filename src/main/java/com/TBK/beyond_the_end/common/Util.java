package com.TBK.beyond_the_end.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraftforge.entity.PartEntity;

import java.util.ArrayList;
import java.util.List;

public class Util {
    public static HitResult raycastForEntity(Level level, Entity originEntity, float distance, boolean checkForBlocks, float bbInflation) {
        Vec3 start = originEntity.getEyePosition();
        Vec3 end = originEntity.getLookAngle().normalize().scale(distance).add(start);

        return internalRaycastForEntity(level, originEntity, start, end, checkForBlocks, bbInflation);
    }
    public static List<HitResult> raycastForAllEntities(Level level, Entity originEntity, float distance, boolean checkForBlocks, float bbInflation) {
        Vec3 start = originEntity.getEyePosition();
        Vec3 end = originEntity.getLookAngle().normalize().scale(distance).add(start);

        return internalRaycastForAllEntity(level, originEntity, start, end, checkForBlocks, bbInflation);
    }

    public static List<HitResult> internalRaycastForAllEntity(Level level, Entity originEntity, Vec3 start, Vec3 end, boolean checkForBlocks, float bbInflation) {
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
            return hits;
        } else if (checkForBlocks) {
            for (BlockPos pos :BlockPos.betweenClosed(blockHitResult.getBlockPos().offset(-1,0,0),blockHitResult.getBlockPos().offset(0,0,-1))){
                hits.add(new BlockHitResult(end,Direction.UP,pos,false));
            }
            return hits;
        }
        return List.of(BlockHitResult.miss(end, Direction.UP, new BlockPos(end)));
    }


    public static HitResult internalRaycastForEntity(Level level, Entity originEntity, Vec3 start, Vec3 end, boolean checkForBlocks, float bbInflation) {
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
}
