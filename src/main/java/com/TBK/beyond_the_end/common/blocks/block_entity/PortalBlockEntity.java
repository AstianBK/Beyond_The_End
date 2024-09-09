package com.TBK.beyond_the_end.common.blocks.block_entity;

import com.TBK.beyond_the_end.common.registry.BkBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PortalBlockEntity extends TheEndPortalBlockEntity {
    protected PortalBlockEntity(BlockEntityType<?> p_155855_, BlockPos p_155856_, BlockState p_155857_) {
        super(p_155855_, p_155856_, p_155857_);
    }

    public PortalBlockEntity(BlockPos p_155859_, BlockState p_155860_) {
        this(BkBlockEntity.BOSS_ROOM.get(), p_155859_, p_155860_);
    }

    public boolean shouldRenderFace(Direction p_59980_) {
        return p_59980_.getAxis() == Direction.Axis.Y;
    }
}
