package com.TBK.beyond_the_end.server;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.common.DimensionUtil;
import com.TBK.beyond_the_end.common.registry.BkDimension;
import com.TBK.beyond_the_end.server.capabilities.BkCapabilities;
import com.TBK.beyond_the_end.server.capabilities.PortalPlayer;
import com.TBK.beyond_the_end.server.capabilities.PortalPlayerCapability;
import com.TBK.beyond_the_end.server.entity.FallenDragonFight;
import com.TBK.beyond_the_end.server.entity.JellyfishFightEvent;
import com.TBK.beyond_the_end.server.network.PacketHandler;
import com.TBK.beyond_the_end.server.network.message.PacketSync;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BeyondTheEnd.MODID)

public class Events {

    @SubscribeEvent
    public static void onPlayerLoginDimension(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        DimensionUtil.startInBEL(player);
    }

    @SubscribeEvent
    public static void onUseItem(PlayerInteractEvent.RightClickItem event) {
        Player player = (Player) event.getEntity();
        if(event.getItemStack().is(Items.STICK)){
            PortalPlayer.get(player).ifPresent(PortalPlayerCapability::addCharge);
            event.getItemStack().shrink(1);
        }
    }


    @SubscribeEvent
    public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> event){
        if(event.getObject() instanceof LivingEntity){
            PortalPlayerCapability oldCap =BkCapabilities.getEntityPatch(event.getObject(), PortalPlayerCapability.class);
            if(oldCap==null){
                if(event.getObject() instanceof Player player){
                    PortalPlayerCapability.PortalPlayerProvider prov=new PortalPlayerCapability.PortalPlayerProvider();
                    PortalPlayer cap = prov.getCapability(BkCapabilities.PORTAL_PLAYER_CAPABILITY,null).orElse(null);
                    cap.setPlayer(player);
                    event.addCapability(new ResourceLocation(BeyondTheEnd.MODID,"portal"),prov);
                }
            }
        }
    }


    @SubscribeEvent
    public static void onInteractWithPortalFrame(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos blockPos = event.getPos();
        Direction direction = event.getFace();
        ItemStack itemStack = event.getItemStack();
        InteractionHand interactionHand = event.getHand();
        if (DimensionUtil.createPortal(player, level, blockPos, direction, itemStack, interactionHand)) {
            event.setCanceled(true);
        }
    }


    @SubscribeEvent
    public static void onWaterExistsInsidePortalFrame(BlockEvent.NeighborNotifyEvent event) {
        LevelAccessor level = event.getLevel();
        BlockPos blockPos = event.getPos();
        BlockState blockState = level.getBlockState(blockPos);
        FluidState fluidState = level.getFluidState(blockPos);
        if (DimensionUtil.detectWaterInFrame(level, blockPos, blockState, fluidState)) {
            event.setCanceled(true);
        }
    }


    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        Level level = event.level;
        if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.END) {
            ServerData.get().getStructureManager().getStructure().getCentre();
            if(level.getServer()!=null && ServerData.get().tag()!=null){
                Vec3i pos=new BlockPos(0, 80, 0);

                CompoundTag nbt = ServerData.get().tag().getCompound("dragonBattle");
                boolean hasDefeat= nbt.getBoolean("PreviouslyKilled");
                if(BeyondTheEnd.bossFight!=null){
                    BeyondTheEnd.bossFight.tick();

                }else {
                    if(level.dimensionTypeRegistration().is(BkDimension.BEYOND_END_TYPE) && !hasDefeat){
                        long i = level.getServer().getWorldData().worldGenSettings().seed();
                        BeyondTheEnd.bossFight = new FallenDragonFight((ServerLevel) level, i, nbt);
                        BeyondTheEnd.bossFight.setPortalLocation(new BlockPos(pos));
                    }else {
                        BeyondTheEnd.bossFight=null;
                    }
                }
                if(hasDefeat){
                    BeyondTheEnd.bossFight=null;
                }
                if(hasDefeat){
                    CompoundTag tag = ServerData.get().tag().getCompound("jellyfishBattle");

                    if(BeyondTheEnd.jellyfishFightEvent != null && BeyondTheEnd.jellyfishFightEvent.level != null){
                        BeyondTheEnd.jellyfishFightEvent.tick();
                    }else {
                        if(level.dimensionTypeRegistration().is(BkDimension.BEYOND_END_TYPE)){
                            BeyondTheEnd.jellyfishFightEvent = new JellyfishFightEvent((ServerLevel) level, tag);
                            BeyondTheEnd.jellyfishFightEvent.setPortalLocation(new BlockPos(pos));
                        }else {
                            BeyondTheEnd.jellyfishFightEvent = null;
                        }
                    }
                }
            }
        }else if(event.side == LogicalSide.CLIENT && event.phase == TickEvent.Phase.END){
            if(BeyondTheEnd.jellyfishFightEvent!=null){
                BeyondTheEnd.jellyfishFightEvent.tickClient();
            } else if(level.dimensionTypeRegistration().is(BkDimension.BEYOND_END_TYPE)){
                BeyondTheEnd.jellyfishFightEvent = new JellyfishFightEvent();
            }
        }
    }


    @SubscribeEvent
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        Entity entity = event.getEntity();
        ResourceKey<Level> dimension = event.getDimension();
        DimensionUtil.dimensionTravel(entity, dimension);
        if(dimension.equals(BkDimension.BEYOND_END_LEVEL)){
            entity.moveTo(new Vec3(100,80,0));
        }
    }


    @SubscribeEvent
    public static void onPlayerTraveling(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        DimensionUtil.travelling(player);
    }


    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        LevelAccessor level = event.getLevel();
        DimensionUtil.initializeLevelData(level);
    }


    @SubscribeEvent
    public static void onSleepFinish(SleepFinishedTimeEvent event) {
        LevelAccessor level = event.getLevel();
        Long time = DimensionUtil.finishSleep(level, event.getNewTime());
        if (time != null) {
            event.setTimeAddition(time);
        }
    }


    @SubscribeEvent
    public static void onTriedToSleep(SleepingTimeCheckEvent event) {
        Player player = event.getEntity();
        if (DimensionUtil.isEternalDay(player)) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if(!event.getEntity().level.isClientSide){
            Player player = event.getEntity();
            PortalPlayer.get(player).ifPresent(portalPlayer -> {
                portalPlayer.setPlayer(player);
                PacketHandler.sendToAllTracking(new PacketSync(portalPlayer.getCharge(),portalPlayer.animTimer),player);
            });

        }
    }


    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        //CapabilityHooks.AetherPlayerHooks.logout(player);
    }


    @SubscribeEvent
    public static void onPlayerJoinLevel(EntityJoinLevelEvent event) {
        if(!event.getLevel().isClientSide){
            if(event.getEntity() instanceof Player){
                Player player = (Player) event.getEntity();
                PortalPlayer.get(player).ifPresent(e->{
                    PacketHandler.sendToPlayer(new PacketSync(e.getCharge(),e.animTimer), (ServerPlayer) player);
                });
            }
        }

    }


    @SubscribeEvent
    public static void onPlayerUpdate(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Player player) {
            PortalPlayer.get(player).ifPresent(PortalPlayer::onUpdate);
        }
    }


    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (!player.level.isClientSide()) {

        }
    }

}
