package com.TBK.beyond_the_end.server.entity;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.common.registry.BKEntityType;
import com.TBK.beyond_the_end.server.capabilities.PortalPlayer;
import com.TBK.beyond_the_end.server.capabilities.PortalPlayerCapability;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.util.Unit;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockPredicate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class JellyfishFightEvent {
    private static final Predicate<Entity> VALID_PLAYER = EntitySelector.ENTITY_STILL_ALIVE.and(EntitySelector.withinDistance(0.0D, 128.0D, 0.0D, 192.0D));
    private final ServerBossEvent dragonEvent = (ServerBossEvent)(new ServerBossEvent(Component.translatable("entity.beyond_the_end.jellyfish"), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS)).setPlayBossMusic(true).setCreateWorldFog(false);
    public final ServerLevel level;
    private final BlockPattern exitPortalPattern;
    private int ticksSinceDragonSeen;
    private int ticksSinceJellyfishSeen;
    private int minionsAlive;
    private int ticksSinceLastPlayerScan;
    private boolean dragonKilled;
    private boolean previouslyKilled;
    @Nullable
    private UUID jellyfishUUID;
    private boolean needsStateScanning = true;
    @Nullable
    private BlockPos portalLocation;
    @Nullable
    private int respawnTime;
    public float prevScreenShakeAmount=0.0F;
    public float screenShakeAmount=0.0F;
    @OnlyIn(Dist.CLIENT)
    public JellyfishFightEvent() {
        this.level = null;
        this.jellyfishUUID = null;
        this.dragonKilled = false;
        this.previouslyKilled = false;
        this.needsStateScanning = false; // Forge: fix MC-105080
        this.portalLocation = BlockPos.ZERO;

        this.exitPortalPattern = BlockPatternBuilder.start().aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ").aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ").aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ").aisle("  ###  ", " #   # ", "#     #", "#  #  #", "#     #", " #   # ", "  ###  ").aisle("       ", "  ###  ", " ##### ", " ##### ", " ##### ", "  ###  ", "       ").where('#', BlockInWorld.hasState(BlockPredicate.forBlock(Blocks.BEDROCK))).build();
    }
    public JellyfishFightEvent(ServerLevel p_64078_, CompoundTag p_64080_) {
        this.level = p_64078_;
        if (p_64080_.contains("jellyfishNeedsStateScanning")) {
            this.needsStateScanning = p_64080_.getBoolean("jellyfishNeedsStateScanning");
        }

        if (p_64080_.contains("jellyfishKilled", 99)) {
            if (p_64080_.hasUUID("jellyfish")) {
                this.jellyfishUUID = p_64080_.getUUID("jellyfish");
            }


            this.dragonKilled = p_64080_.getBoolean("jellyfishKilled");
            this.previouslyKilled = p_64080_.getBoolean("jellyfishPreviouslyKilled");
        } else {
            this.dragonKilled = false;
            this.previouslyKilled = false;
        }

        this.exitPortalPattern = BlockPatternBuilder.start().aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ").aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ").aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ").aisle("  ###  ", " #   # ", "#     #", "#  #  #", "#     #", " #   # ", "  ###  ").aisle("       ", "  ###  ", " ##### ", " ##### ", " ##### ", "  ###  ", "       ").where('#', BlockInWorld.hasState(BlockPredicate.forBlock(Blocks.BEDROCK))).build();
    }

    @OnlyIn(Dist.CLIENT)
    public void tickClient(){
        prevScreenShakeAmount = screenShakeAmount;

        if (screenShakeAmount > 0) {
            screenShakeAmount = Math.max(0, screenShakeAmount - 0.03F);
        }
    }

    public CompoundTag saveData() {
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.putBoolean("jellyfishNeedsStateScanning", this.needsStateScanning);
        if (this.jellyfishUUID != null) {
            compoundtag.putUUID("jellyfish", this.jellyfishUUID);
        }


        compoundtag.putBoolean("jellyfishKilled", this.dragonKilled);
        compoundtag.putBoolean("jellyfishPreviouslyKilled", this.previouslyKilled);

        return compoundtag;
    }

    public void setPortalLocation(BlockPos pos){
        this.portalLocation=pos;
    }

    public void tick() {
        this.dragonEvent.setVisible(!this.dragonKilled);
        if (++this.ticksSinceLastPlayerScan >= 20) {
            this.updatePlayers();
            this.ticksSinceLastPlayerScan = 0;
        }

        this.level.getChunkSource().removeRegionTicket(TicketType.DRAGON, new ChunkPos(0, 0), 14, Unit.INSTANCE);
        if (!this.dragonEvent.getPlayers().isEmpty()) {
            this.level.getChunkSource().addRegionTicket(BeyondTheEnd.JELLY, new ChunkPos(0, 0), 14, Unit.INSTANCE);
            boolean flag = this.isArenaLoaded();
            if (this.needsStateScanning && flag) {
                this.scanState();
                this.needsStateScanning = false;
            }

            if (!this.dragonKilled) {

                if ((this.jellyfishUUID == null || ++this.ticksSinceDragonSeen >= 1200) && flag) {
                    this.findOrCreateDragon();
                    this.ticksSinceDragonSeen = 0;
                }


            }
        } else {
            this.level.getChunkSource().removeRegionTicket(BeyondTheEnd.JELLY, new ChunkPos(0, 0), 14, Unit.INSTANCE);
        }
    }

    private void scanState() {
        boolean flag = this.hasActiveExitPortal();
        this.previouslyKilled = flag;

        List<? extends JellyfishEntity> list = this.level.getEntities(BKEntityType.JELLYFISH.get(), LivingEntity::isAlive);
        if (list.isEmpty()) {
            this.dragonKilled = true;
        } else {
            JellyfishEntity enderdragon = list.get(0);
            this.jellyfishUUID = enderdragon.getUUID();
            BeyondTheEnd.LOGGER.info("Found that there's a dragon still alive ({})", (Object)enderdragon);
            this.dragonKilled = false;
            if (!flag) {
                BeyondTheEnd.LOGGER.info("But we didn't have a portal, let's remove it.");
                enderdragon.discard();
                this.jellyfishUUID = null;
            }
        }

        if (!this.previouslyKilled && this.dragonKilled) {
            this.dragonKilled = false;
        }
    }

    public Collection<ServerPlayer> getPlayer(){
        return dragonEvent.getPlayers();
    }


    private void findOrCreateDragon() {
        List<? extends JellyfishEntity> list = this.level.getEntities(BKEntityType.JELLYFISH.get(), LivingEntity::isAlive);
        if (list.isEmpty()) {
            BeyondTheEnd.LOGGER.debug("Haven't seen the jelly, respawning it");
            this.createNewDragon();
        } else {
            BeyondTheEnd.LOGGER.debug("Haven't seen our jelly, but found another one to use.");
            this.jellyfishUUID = list.get(0).getUUID();
        }
    }

    private boolean hasActiveExitPortal() {
        for(int i = -8; i <= 8; ++i) {
            for(int j = -8; j <= 8; ++j) {
                LevelChunk levelchunk = this.level.getChunk(i, j);

                for(BlockEntity blockentity : levelchunk.getBlockEntities().values()) {
                    if (blockentity instanceof TheEndPortalBlockEntity) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean isArenaLoaded() {
        for(int i = -8; i <= 8; ++i) {
            for(int j = 8; j <= 8; ++j) {
                ChunkAccess chunkaccess = this.level.getChunk(i, j, ChunkStatus.FULL, false);
                if (!(chunkaccess instanceof LevelChunk)) {
                    return false;
                }

                ChunkHolder.FullChunkStatus chunkholder$fullchunkstatus = ((LevelChunk)chunkaccess).getFullStatus();
                if (!chunkholder$fullchunkstatus.isOrAfter(ChunkHolder.FullChunkStatus.TICKING)) {
                    return false;
                }
            }
        }

        return true;
    }

    private void updatePlayers() {
        Set<ServerPlayer> set = Sets.newHashSet();

        for(ServerPlayer serverplayer : this.level.getPlayers(VALID_PLAYER)) {
            this.dragonEvent.addPlayer(serverplayer);
            set.add(serverplayer);
        }

        Set<ServerPlayer> set1 = Sets.newHashSet(this.dragonEvent.getPlayers());
        set1.removeAll(set);

        for(ServerPlayer serverplayer1 : set1) {
            this.dragonEvent.removePlayer(serverplayer1);
        }

    }


    public void setJellyFishKilled(JellyfishEntity p_64086_) {
        if (p_64086_.getUUID().equals(this.jellyfishUUID)) {
            this.dragonEvent.setProgress(0.0F);
            this.dragonEvent.setVisible(false);

            this.previouslyKilled = true;
            this.dragonKilled = true;
        }

    }

    private JellyfishEntity createNewDragon() {
        this.level.getChunkAt(new BlockPos(0,120.0D,0));
        JellyfishEntity enderdragon = BKEntityType.JELLYFISH.get().create(this.level);
        enderdragon.moveTo(0,120.0D,0, this.level.random.nextFloat() * 360.0F, 0.0F);
        enderdragon.actuallyPhase= JellyfishEntity.PhaseAttack.SPAWN;
        this.level.addFreshEntity(enderdragon);
        this.jellyfishUUID = enderdragon.getUUID();
        return enderdragon;
    }

    public void updateJellyfish(JellyfishEntity p_64097_) {
        if (p_64097_.getUUID().equals(this.jellyfishUUID)) {
            this.dragonEvent.setProgress(p_64097_.getHealth() / p_64097_.getMaxHealth());
            this.ticksSinceDragonSeen = 0;
            if (p_64097_.hasCustomName()) {
                this.dragonEvent.setName(p_64097_.getDisplayName());
            }
        }

    }

    public void addPlayer(ServerPlayer player) {
        this.dragonEvent.addPlayer(player);
    }

    public void removePlayer(ServerPlayer player) {
        this.dragonEvent.removePlayer(player);
    }

    @OnlyIn(Dist.CLIENT)
    public float getScreenShakeAmount(float partialTick) {
        return prevScreenShakeAmount + (screenShakeAmount - prevScreenShakeAmount) * partialTick;

    }
}
