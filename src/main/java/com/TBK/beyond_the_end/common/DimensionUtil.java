package com.TBK.beyond_the_end.common;

import com.TBK.beyond_the_end.common.blocks.PortalBlock;
import com.TBK.beyond_the_end.common.blocks.BKPortalForcer;
import com.TBK.beyond_the_end.common.blocks.PortalShape;
import com.TBK.beyond_the_end.common.registry.BkDimension;
import com.TBK.beyond_the_end.server.capabilities.PortalPlayer;
import com.TBK.beyond_the_end.server.network.PacketHandler;
import com.TBK.beyond_the_end.server.network.message.PacketLeavingDimension;
import com.TBK.beyond_the_end.server.network.message.PacketTravelDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import javax.annotation.Nullable;
import java.util.Optional;

public class DimensionUtil {
    public static boolean playerLeavingAether;
    public static boolean displayAetherTravel;
    public static int teleportationTimer;

    public static void startInBEL(Player player) {
        PortalPlayer.get(player).ifPresent(portalPlayer->{
            if (portalPlayer.canSpawnInDimension()) {// Checks if the player has been set to spawn in Dimension.
                if (player instanceof ServerPlayer serverPlayer) {
                    MinecraftServer server = serverPlayer.level.getServer();
                    if (server != null) {
                        ServerLevel level = server.getLevel(BkDimension.BEYOND_END_LEVEL);
                        if (level != null && serverPlayer.level.dimension() != BkDimension.BEYOND_END_LEVEL) {
                            if (player.changeDimension(level, new BKPortalForcer(level, false, true)) != null) {
                                serverPlayer.setRespawnPosition(BkDimension.BEYOND_END_LEVEL, serverPlayer.blockPosition(), serverPlayer.getYRot(), true, false);
                                portalPlayer.setCanSpawnInDimension(false); // Sets that the player has already spawned in Dimension.
                            }
                        }
                    }
                }
            }
        });
    }


    public static boolean createPortal(Player player, Level level, BlockPos pos, @Nullable Direction direction, ItemStack stack, InteractionHand hand) {
        if (direction != null) {
            BlockPos relativePos = pos.relative(direction);
            if (stack.is(Items.FLINT_AND_STEEL)) { // Checks if the item can activate the portal.
                // Checks whether the dimension can have a portal created in it, and that the portal isn't disabled.
                if ((level.dimension() == PortalBlock.returnDimension() || level.dimension() == PortalBlock.destinationDimension())) {
                    Optional<com.TBK.beyond_the_end.common.blocks.PortalShape> optional = PortalShape.findEmptyAetherPortalShape(level, relativePos, Direction.Axis.X);
                    if (optional.isPresent()) {
                        optional.get().createPortalBlocks();
                        player.playSound(SoundEvents.BUCKET_EMPTY, 1.0F, 1.0F);
                        player.swing(hand);
                        if (!player.isCreative()) {
                            if (stack.getCount() > 1) {
                                stack.shrink(1);
                                player.addItem(stack.hasCraftingRemainingItem() ? stack.getCraftingRemainingItem() : ItemStack.EMPTY);
                            } else if (stack.isDamageableItem()) {
                                stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                            } else {
                                player.setItemInHand(hand, stack.hasCraftingRemainingItem() ? stack.getCraftingRemainingItem() : ItemStack.EMPTY);
                            }
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean detectWaterInFrame(LevelAccessor levelAccessor, BlockPos pos, BlockState blockState, FluidState fluidState) {
        if (levelAccessor instanceof Level level) {
            if (fluidState.is(Fluids.WATER) && fluidState.createLegacyBlock().getBlock() == blockState.getBlock()) {
                if ((level.dimension() == PortalBlock.returnDimension() || level.dimension() == PortalBlock.destinationDimension()) ) {
                    Optional<com.TBK.beyond_the_end.common.blocks.PortalShape> optional = com.TBK.beyond_the_end.common.blocks.PortalShape.findEmptyAetherPortalShape(level, pos, Direction.Axis.X);
                    if (optional.isPresent()) {
                        optional.get().createPortalBlocks();
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public static void tickTime(Level level) {

    }




    public static void dimensionTravel(Entity entity, ResourceKey<Level> dimension) {
        if (entity instanceof Player player) {
            PortalPlayer.get(player).ifPresent(player1->{
                if (entity.level.dimension() == PortalBlock.destinationDimension() && dimension == PortalBlock.returnDimension()) { // We display the Descending GUI text to the player if they're about to return to the Overworld.
                    displayAetherTravel = true;
                    playerLeavingAether = true;
                    PacketHandler.sendToAllTracking(new PacketTravelDimension(true),player);
                    PacketHandler.sendToAllTracking(new PacketLeavingDimension(true),player);
                } else if (entity.level.dimension() == PortalBlock.returnDimension() && dimension == PortalBlock.destinationDimension()) { // We display the Ascending GUI text to the player if they're about to enter Dimension.
                    displayAetherTravel = true;
                    playerLeavingAether = false;
                    PacketHandler.sendToAllTracking(new PacketTravelDimension(true),player);
                    PacketHandler.sendToAllTracking(new PacketLeavingDimension(false),player);
                } else { // Don't display any text if not travelling between Dimension and Overworld or vice-versa.
                    displayAetherTravel = false;
                    PacketHandler.sendToAllTracking(new PacketTravelDimension(false),player);
                }
            });
        }
    }


    public static void travelling(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (teleportationTimer > 0) { // Prevents the player from being kicked for flying.
                ServerGamePacketListenerImpl serverGamePacketListenerImplAccessor =  serverPlayer.connection;
                serverGamePacketListenerImplAccessor.aboveGroundTickCount=0;
                serverGamePacketListenerImplAccessor.aboveGroundVehicleTickCount=0;
                teleportationTimer--;
            }
            if (teleportationTimer < 0 || serverPlayer.verticalCollisionBelow) {
                teleportationTimer = 0;
            }
        }
    }

    public static void initializeLevelData(LevelAccessor level) {
        if (level instanceof ServerLevel serverLevel && serverLevel.dimensionType().effectsLocation().equals(BkDimension.BEYOND_END_TYPE.location())) {

        }
    }


    @Nullable
    public static Long finishSleep(LevelAccessor level, long newTime) {
        if (level instanceof ServerLevel && level.dimensionType().effectsLocation().equals(BkDimension.BEYOND_END_TYPE.location())) {
            ServerLevelAccessor serverLevelAccessor = (ServerLevelAccessor) level;
            serverLevelAccessor.getLevel().serverLevelData.setRainTime(0);
            serverLevelAccessor.getLevel().serverLevelData.setRaining(false);
            serverLevelAccessor.getLevel().serverLevelData.setThunderTime(0);
            serverLevelAccessor.getLevel().serverLevelData.setThundering(false);

            long time = newTime + 48000L;
            return time - time % (long) BkDimension.BEYOND_END_TICKS_PER_DAY;
        }
        return null;
    }


    public static boolean isEternalDay(Player player) {
        if (player.level.dimensionType().effectsLocation().equals(BkDimension.BEYOND_END_TYPE.location())) {
            return true;
        }
        return false;
    }
}
