package com.TBK.beyond_the_end.common.mixin;

import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccesor {
    @Accessor("random")
    RandomSource aether$getRandom();

    @Accessor("portalEntrancePos")
    BlockPos aether$getPortalEntrancePos();

    @Accessor("portalEntrancePos")
    void aether$setPortalEntrancePos(BlockPos portalEntrancePos);

    @Invoker
    Vec3 callGetRelativePortalPosition(Direction.Axis axis, BlockUtil.FoundRectangle portal);
}
