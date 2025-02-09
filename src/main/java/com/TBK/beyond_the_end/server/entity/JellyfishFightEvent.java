package com.TBK.beyond_the_end.server.entity;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.common.registry.BKEntityType;
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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockPredicate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class JellyfishFightEvent {
    private static final Predicate<Entity> VALID_PLAYER = EntitySelector.ENTITY_STILL_ALIVE.and(EntitySelector.withinDistance(0.0D, 128.0D, 0.0D, 192.0D));
    private final ServerBossEvent dragonEvent = (ServerBossEvent)(new ServerBossEvent(Component.translatable("entity.beyond_the_end.jellyfish"), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS)).setPlayBossMusic(true).setCreateWorldFog(false);
    private final ServerLevel level;
    private final BlockPattern exitPortalPattern;
    private int ticksSinceDragonSeen;
    private int ticksSinceJellyfishSeen;
    private int minionsAlive;
    private int ticksSinceLastPlayerScan;
    private boolean dragonKilled;
    private boolean previouslyKilled;
    public Set<Integer> minions = Sets.newHashSet();
    @Nullable
    private UUID jellyfishUUID;
    private boolean needsStateScanning = true;
    @Nullable
    private BlockPos portalLocation;
    @Nullable
    private int respawnTime;

    public JellyfishFightEvent(ServerLevel p_64078_, long p_64079_, CompoundTag p_64080_) {
        this.level = p_64078_;
        if (p_64080_.contains("NeedsStateScanning")) {
            this.needsStateScanning = p_64080_.getBoolean("NeedsStateScanning");
        }

        if (p_64080_.contains("jellyfishKilled", 99)) {
            if (p_64080_.hasUUID("jellyfish")) {
                this.jellyfishUUID = p_64080_.getUUID("jellyfish");
            }

            if(p_64080_.contains("minions",10)){
                ListTag tag = p_64080_.getList("minions",9);
                for(int i = 0 ; i < tag.size() ; i++){
                    CompoundTag nbt = tag.getCompound(i);
                    this.minions.add(nbt.getInt("minion"));
                }
            }

            this.dragonKilled = p_64080_.getBoolean("jellyfishKilled");
            this.previouslyKilled = p_64080_.getBoolean("PreviouslyKilled");
            this.needsStateScanning = !p_64080_.getBoolean("LegacyScanPerformed"); // Forge: fix MC-105080

            if (p_64080_.contains("ExitPortalLocation", 10)) {
                this.portalLocation = NbtUtils.readBlockPos(p_64080_.getCompound("ExitPortalLocation"));
            }
        } else {
            this.dragonKilled = false;
            this.previouslyKilled = false;
        }

        this.exitPortalPattern = BlockPatternBuilder.start().aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ").aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ").aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ").aisle("  ###  ", " #   # ", "#     #", "#  #  #", "#     #", " #   # ", "  ###  ").aisle("       ", "  ###  ", " ##### ", " ##### ", " ##### ", "  ###  ", "       ").where('#', BlockInWorld.hasState(BlockPredicate.forBlock(Blocks.BEDROCK))).build();
    }

    public void saveData() {
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.putBoolean("NeedsStateScanning", this.needsStateScanning);
        if (this.jellyfishUUID != null) {
            compoundtag.putUUID("jellyfish", this.jellyfishUUID);
        }
        ListTag list = new ListTag();
        if(!this.minions.isEmpty()){
            for (Integer uuid : this.minions){
                CompoundTag tag = new CompoundTag();
                tag.putInt("minion",uuid);
                list.add(tag);
            }
        }

        compoundtag.putBoolean("jellyfishKilled", this.dragonKilled);
        compoundtag.putBoolean("PreviouslyKilled", this.previouslyKilled);
        compoundtag.putBoolean("LegacyScanPerformed", !this.needsStateScanning);

        if (this.portalLocation != null) {
            compoundtag.put("ExitPortalLocation", NbtUtils.writeBlockPos(this.portalLocation));
        }

        compoundtag.put("minions",list);
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

        if (!this.dragonEvent.getPlayers().isEmpty()) {
            this.level.getChunkSource().addRegionTicket(TicketType.DRAGON, new ChunkPos(0, 0), 14, Unit.INSTANCE);
            boolean flag = this.isArenaLoaded();
            if (this.needsStateScanning && flag) {
                this.scanState();
                this.needsStateScanning = false;
            }

            if (!this.dragonKilled) {
                if(this.jellyfishUUID !=null && flag && this.minionsAlive<=0 && ++this.ticksSinceDragonSeen >= 1200){
                    this.findMinions();
                }

                if ((this.jellyfishUUID == null || ++this.ticksSinceDragonSeen >= 1200) && flag) {
                    this.findOrCreateDragon();
                    this.ticksSinceDragonSeen = 0;
                }


            }
        } else {
            this.level.getChunkSource().removeRegionTicket(TicketType.DRAGON, new ChunkPos(0, 0), 14, Unit.INSTANCE);
        }
    }

    private void findMinions() {
        List<? extends JellyfishMinionEntity> list = this.level.getEntities(BKEntityType.JELLYFISH_MINION.get(), LivingEntity::isAlive);
        if (!list.isEmpty()) {
            for(JellyfishMinionEntity minion : list){
                this.minions.add(minion.getId());
            }
        }
        this.ticksSinceJellyfishSeen=0;
    }

    private void scanState() {
        boolean flag = this.hasActiveExitPortal();
        if (flag) {
            this.previouslyKilled = true;
        } else {
            this.previouslyKilled = false;
        }

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
    public void resetMinions(){
        this.minionsAlive=7;
        this.minions.clear();
        this.findMinions();
    }

    private JellyfishEntity createNewDragon() {
        if(this.portalLocation!=null){
            this.level.getChunkAt(this.portalLocation);
            JellyfishEntity enderdragon = BKEntityType.JELLYFISH.get().create(this.level);
            enderdragon.moveTo(this.portalLocation.getX(),this.portalLocation.getY()+40.0D,this.portalLocation.getZ(), this.level.random.nextFloat() * 360.0F, 0.0F);
            enderdragon.actuallyPhase= JellyfishEntity.PhaseAttack.SPAWN;
            this.level.addFreshEntity(enderdragon);
            this.jellyfishUUID = enderdragon.getUUID();
            return enderdragon;
        }
        return null;
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

    public int getMinionsAlive() {
        return this.minionsAlive;
    }

    public void onMinionDeath(JellyfishMinionEntity p_64083_, DamageSource p_64084_) {
        this.minionsAlive--;
    }

    public boolean hasPreviouslyKilledDragon() {
        return this.previouslyKilled;
    }


    public void addPlayer(ServerPlayer player) {
        this.dragonEvent.addPlayer(player);
    }

    public void removePlayer(ServerPlayer player) {
        this.dragonEvent.removePlayer(player);
    }


}
