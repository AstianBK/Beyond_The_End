package com.TBK.beyond_the_end.common.blocks;

import com.TBK.beyond_the_end.common.blocks.block_entity.PortalBlockEntity;
import com.TBK.beyond_the_end.common.registry.BkDimension;
import com.TBK.beyond_the_end.server.capabilities.BkCapabilities;
import com.TBK.beyond_the_end.server.capabilities.PortalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.LazyOptional;

public class PortalBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    protected static final VoxelShape SHAPE = Block.box(0.0D, 6.0D, 0.0D, 16.0D, 12.0D, 16.0D);

    public PortalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }


    @SuppressWarnings("deprecation")
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        Entity entityAccessor = entity;
        if (!entity.isPassenger() && !entity.isVehicle() && entity.canChangeDimensions()) {
            if (entity.isOnPortalCooldown()) {
                entity.setPortalCooldown();
            } else {
                if (!entity.level.isClientSide() && !pos.equals(entityAccessor.portalEntrancePos)) {
                    entityAccessor.portalEntrancePos=pos.immutable();
                }
                LazyOptional<PortalPlayer> aetherPlayer = entity.getCapability(BkCapabilities.PORTAL_PLAYER_CAPABILITY);
                if (!aetherPlayer.isPresent()) {
                    this.handleTeleportation(entity);
                } else {
                    aetherPlayer.ifPresent(handler -> {
                        handler.setInPortal(true);
                        int waitTime = handler.getPortalTimer();
                        if (waitTime >= entity.getPortalWaitTime()) {
                            this.handleTeleportation(entity);
                            handler.setPortalTimer(0);
                        }
                    });
                }
            }
        }
    }

    private void handleTeleportation(Entity entity) {
        MinecraftServer server = entity.level.getServer();
        ResourceKey<Level> destinationKey = entity.level.dimension() ==destinationDimension() ? returnDimension() : destinationDimension();
        if (server != null) {
            ServerLevel destinationLevel = server.getLevel(destinationKey);
            if (destinationLevel != null && !entity.isPassenger()) {
                entity.level.getProfiler().push("portal");
                entity.setPortalCooldown();
                entity.changeDimension(destinationLevel, new BKPortalForcer(destinationLevel, true));
                entity.level.getProfiler().pop();
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(100) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.PORTAL_TRAVEL, SoundSource.BLOCKS, 0.5F, random.nextFloat() * 0.4F + 0.8F, false);
        }
        for (int i = 0; i < 4; ++i) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            double xSpeed = (random.nextFloat() - 0.5) * 0.5;
            double ySpeed = (random.nextFloat() - 0.5) * 0.5;
            double zSpeed = (random.nextFloat() - 0.5) * 0.5;
            int j = random.nextInt(2) * 2 - 1;
            if (!level.getBlockState(pos.west()).is(this) && !level.getBlockState(pos.east()).is(this)) {
                x = pos.getX() + 0.5 + 0.25 * j;
                xSpeed = random.nextFloat() * 2.0F * j;
            } else {
                z = pos.getZ() + 0.5 + 0.25 * j;
                zSpeed = random.nextFloat() * 2.0F * j;
            }
            level.addParticle(ParticleTypes.REVERSE_PORTAL, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }


    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /**
     * Warning for "deprecation" is suppressed because the method is fine to override.
     */
    @SuppressWarnings("deprecation")
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        Direction.Axis directionAxis = direction.getAxis();
        Direction.Axis blockAxis = state.getValue(AXIS);
        boolean flag = blockAxis != directionAxis && directionAxis.isHorizontal();
        return !flag && !facingState.is(this) && !(new PortalShape(level, currentPos, blockAxis).isComplete()) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, facingState, level, currentPos, facingPos);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PortalBlockEntity(pos, state);
    }
    @SuppressWarnings("deprecation")
    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    public static ResourceKey<Level> destinationDimension() {
        return ResourceKey.create(Registry.DIMENSION_REGISTRY,new ResourceLocation(BkDimension.BEYOND_END_LEVEL.location().toString()));
    }


    public static ResourceKey<Level> returnDimension() {
        return ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(Level.END.location().toString()));
    }
}
