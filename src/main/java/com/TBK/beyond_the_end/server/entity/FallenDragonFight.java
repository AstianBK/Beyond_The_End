package com.TBK.beyond_the_end.server.entity;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.common.api.Savable;
import com.TBK.beyond_the_end.common.registry.BKEntityType;
import com.TBK.beyond_the_end.common.registry.BkDimension;
import com.TBK.beyond_the_end.server.entity.phase.FallenDragonPhase;
import com.google.common.collect.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.data.worldgen.features.EndFeatures;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
import net.minecraft.world.level.dimension.end.DragonRespawnAnimation;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class FallenDragonFight extends EndDragonFight {
    private static final Predicate<Entity> VALID_PLAYER = EntitySelector.ENTITY_STILL_ALIVE.and(EntitySelector.withinDistance(0.0D, 128.0D, 0.0D, 192.0D));
    private final ServerBossEvent dragonEvent = (ServerBossEvent)(new ServerBossEvent(Component.translatable("entity.beyond_the_end.fallen_dragon"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS)).setPlayBossMusic(true).setCreateWorldFog(true);
    private final ServerLevel level;
    private final ObjectArrayList<Integer> gateways = new ObjectArrayList<>();
    private final BlockPattern exitPortalPattern;
    private int ticksSinceDragonSeen;
    private int crystalsAlive;
    private int ticksSinceCrystalsScanned;
    private int ticksSinceLastPlayerScan;
    private boolean dragonKilled;
    private boolean previouslyKilled;
    @Nullable
    private UUID dragonUUID;
    private boolean needsStateScanning = true;
    @Nullable
    private BlockPos portalLocation;
    @Nullable
    private DragonRespawnAnimation respawnStage;
    private int respawnTime;
    @Nullable
    private List<EndCrystal> respawnCrystals;

    public FallenDragonFight(ServerLevel p_64078_, long p_64079_, CompoundTag p_64080_) {
        super(p_64078_,p_64079_,p_64080_);
        this.level = p_64078_;
        if (p_64080_.contains("NeedsStateScanning")) {
            this.needsStateScanning = p_64080_.getBoolean("NeedsStateScanning");
        }

        if (p_64080_.contains("DragonKilled", 99)) {
            if (p_64080_.hasUUID("Dragon")) {
                this.dragonUUID = p_64080_.getUUID("Dragon");
            }

            this.dragonKilled = p_64080_.getBoolean("DragonKilled");
            this.previouslyKilled = p_64080_.getBoolean("PreviouslyKilled");
            this.needsStateScanning = !p_64080_.getBoolean("LegacyScanPerformed"); // Forge: fix MC-105080
            if (p_64080_.getBoolean("IsRespawning")) {
                this.respawnStage = DragonRespawnAnimation.START;
            }

            if (p_64080_.contains("ExitPortalLocation", 10)) {
                this.portalLocation = NbtUtils.readBlockPos(p_64080_.getCompound("ExitPortalLocation"));
            }
        } else {
            this.dragonKilled = true;
            this.previouslyKilled = true;
        }

        this.exitPortalPattern = BlockPatternBuilder.start().aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ").aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ").aisle("       ", "       ", "       ", "   #   ", "       ", "       ", "       ").aisle("  ###  ", " #   # ", "#     #", "#  #  #", "#     #", " #   # ", "  ###  ").aisle("       ", "  ###  ", " ##### ", " ##### ", " ##### ", "  ###  ", "       ").where('#', BlockInWorld.hasState(BlockPredicate.forBlock(Blocks.BEDROCK))).build();
    }

    public CompoundTag saveData() {
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.putBoolean("NeedsStateScanning", this.needsStateScanning);
        if (this.dragonUUID != null) {
            compoundtag.putUUID("Dragon", this.dragonUUID);
        }

        compoundtag.putBoolean("DragonKilled", this.dragonKilled);
        compoundtag.putBoolean("PreviouslyKilled", this.previouslyKilled);
        compoundtag.putBoolean("LegacyScanPerformed", !this.needsStateScanning); // Forge: fix MC-105080
        if (this.portalLocation != null) {
            compoundtag.put("ExitPortalLocation", NbtUtils.writeBlockPos(this.portalLocation));
        }
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

        if (!this.dragonEvent.getPlayers().isEmpty()) {
            this.level.getChunkSource().addRegionTicket(TicketType.DRAGON, new ChunkPos(0, 0), 14, Unit.INSTANCE);
            boolean flag = this.isArenaLoaded();
            if (this.needsStateScanning && flag) {
                this.scanState();
                this.needsStateScanning = false;
            }

            if (!this.dragonKilled) {
                if ((this.dragonUUID == null || ++this.ticksSinceDragonSeen >= 1200) && flag) {
                    this.findOrCreateDragon();
                    this.ticksSinceDragonSeen = 0;
                }

                if (++this.ticksSinceCrystalsScanned >= 100 && flag) {
                    this.updateCrystalCount();
                    this.ticksSinceCrystalsScanned = 0;
                }
            }
        } else {
            this.level.getChunkSource().removeRegionTicket(TicketType.DRAGON, new ChunkPos(0, 0), 14, Unit.INSTANCE);
        }
    }

    private void scanState() {
        BeyondTheEnd.LOGGER.info("Scanning for legacy world dragon fight...");
        boolean flag = this.hasActiveExitPortal();
        if (flag) {
            BeyondTheEnd.LOGGER.info("Found that the dragon has been killed in this world already.");
            this.previouslyKilled = true;
        } else {
            BeyondTheEnd.LOGGER.info("Found that the dragon has not yet been killed in this world.");
            this.previouslyKilled = false;
            if (this.findExitPortal() == null) {
                this.spawnExitPortal(false);
            }
        }

        List<? extends FallenDragonEntity> list = this.level.getEntities(BKEntityType.FALLEN_DRAGON.get(), LivingEntity::isAlive);
        if (list.isEmpty()) {
            this.dragonKilled = true;
        } else {
            FallenDragonEntity enderdragon = list.get(0);
            this.dragonUUID = enderdragon.getUUID();
            BeyondTheEnd.LOGGER.info("Found that there's a dragon still alive ({})", (Object)enderdragon);
            this.dragonKilled = false;
            if (!flag) {
                BeyondTheEnd.LOGGER.info("But we didn't have a portal, let's remove it.");
                enderdragon.discard();
                this.dragonUUID = null;
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
        List<? extends FallenDragonEntity> list = this.level.getEntities(BKEntityType.FALLEN_DRAGON.get(), LivingEntity::isAlive);
        if (list.isEmpty()) {
            BeyondTheEnd.LOGGER.debug("Haven't seen the dragon, respawning it");
            this.createNewDragon();
        } else {
            BeyondTheEnd.LOGGER.debug("Haven't seen our dragon, but found another one to use.");
            this.dragonUUID = list.get(0).getUUID();
        }
    }

    protected void setRespawnStage(DragonRespawnAnimation p_64088_) {

    }

    private boolean hasActiveExitPortal() {
        for(int i = -8; i <= 8; ++i) {
            for(int j = -8; j <= 8; ++j) {
                LevelChunk levelchunk = this.level.getChunk(i, j);

                for(BlockEntity blockentity : levelchunk.getBlockEntities().values()) {
                    if (blockentity instanceof TheEndPortalBlockEntity) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Nullable
    private BlockPattern.BlockPatternMatch findExitPortal() {
        for(int i = -8; i <= 8; ++i) {
            for(int j = -8; j <= 8; ++j) {
                LevelChunk levelchunk = this.level.getChunk(i, j);

                for(BlockEntity blockentity : levelchunk.getBlockEntities().values()) {
                    if (blockentity instanceof TheEndPortalBlockEntity) {
                        BlockPattern.BlockPatternMatch blockpattern$blockpatternmatch = this.exitPortalPattern.find(this.level, blockentity.getBlockPos());
                        if (blockpattern$blockpatternmatch != null) {
                            BlockPos blockpos = blockpattern$blockpatternmatch.getBlock(3, 3, 3).getPos();
                            if (this.portalLocation == null) {
                                this.portalLocation = blockpos;
                            }

                            return blockpattern$blockpatternmatch;
                        }
                    }
                }
            }
        }

        int k = this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, EndPodiumFeature.END_PODIUM_LOCATION).getY();

        for(int l = k; l >= this.level.getMinBuildHeight(); --l) {
            BlockPattern.BlockPatternMatch blockpattern$blockpatternmatch1 = this.exitPortalPattern.find(this.level, new BlockPos(EndPodiumFeature.END_PODIUM_LOCATION.getX(), l, EndPodiumFeature.END_PODIUM_LOCATION.getZ()));
            if (blockpattern$blockpatternmatch1 != null) {
                if (this.portalLocation == null) {
                    this.portalLocation = blockpattern$blockpatternmatch1.getBlock(3, 3, 3).getPos();
                }

                return blockpattern$blockpatternmatch1;
            }
        }

        return null;
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

    private void updateCrystalCount() {
        this.ticksSinceCrystalsScanned = 0;
        this.crystalsAlive = 0;

        for(SpikeFeature.EndSpike spikefeature$endspike : SpikeFeature.getSpikesForLevel(this.level)) {
            this.crystalsAlive += this.level.getEntitiesOfClass(EndCrystal.class, spikefeature$endspike.getTopBoundingBox()).size();
        }

        BeyondTheEnd.LOGGER.debug("Found {} end crystals still alive", (int)this.crystalsAlive);
    }

    public void setDragonKilled(FallenDragonEntity p_64086_) {
        if (p_64086_.getUUID().equals(this.dragonUUID)) {
            this.dragonEvent.setProgress(0.0F);
            this.dragonEvent.setVisible(false);
            this.spawnExitPortal(true);
            this.spawnNewGateway();

            this.previouslyKilled = true;
            this.dragonKilled = true;
        }

    }

    private void spawnNewGateway() {

    }

    private void spawnNewGateway(BlockPos p_64090_) {
        this.level.levelEvent(3000, p_64090_, 0);
    }

    private void spawnExitPortal(boolean p_64094_) {

    }

    private FallenDragonEntity createNewDragon() {
        if(this.portalLocation!=null){
            this.level.getChunkAt(this.portalLocation);
            FallenDragonEntity enderdragon = BKEntityType.FALLEN_DRAGON.get().create(this.level);
            enderdragon.moveTo(this.portalLocation.getX(),this.portalLocation.getY()+40.0D,this.portalLocation.getZ(), this.level.random.nextFloat() * 360.0F, 0.0F);
            enderdragon.phaseManager.setPhase(FallenDragonPhase.RESPAWN);
            enderdragon.level.broadcastEntityEvent(enderdragon,(byte) 66);
            this.level.addFreshEntity(enderdragon);
            this.dragonUUID = enderdragon.getUUID();
            return enderdragon;
        }
        return null;
    }

    public void updateDragon(FallenDragonEntity p_64097_) {
        if (p_64097_.getUUID().equals(this.dragonUUID)) {
            this.dragonEvent.setProgress(p_64097_.getHealth() / p_64097_.getMaxHealth());
            this.ticksSinceDragonSeen = 0;
            if (p_64097_.hasCustomName()) {
                this.dragonEvent.setName(p_64097_.getDisplayName());
            }
        }

    }

    public int getCrystalsAlive() {
        return this.crystalsAlive;
    }

    public void onCrystalDestroyed(EndCrystal p_64083_, DamageSource p_64084_) {

    }

    public boolean hasPreviouslyKilledDragon() {
        return this.previouslyKilled;
    }

    public void tryRespawn() {
        if (this.dragonKilled && this.respawnStage == null) {
            BlockPos blockpos = this.portalLocation;
            if (blockpos == null) {
                BeyondTheEnd.LOGGER.debug("Tried to respawn, but need to find the portal first.");
                BlockPattern.BlockPatternMatch blockpattern$blockpatternmatch = this.findExitPortal();
                if (blockpattern$blockpatternmatch == null) {
                    BeyondTheEnd.LOGGER.debug("Couldn't find a portal, so we made one.");
                    this.spawnExitPortal(true);
                } else {
                    BeyondTheEnd.LOGGER.debug("Found the exit portal & saved its location for next time.");
                }

                blockpos = this.portalLocation;
            }

            List<EndCrystal> list1 = Lists.newArrayList();
            BlockPos blockpos1 = blockpos.above(1);

            for(Direction direction : Direction.Plane.HORIZONTAL) {
                List<EndCrystal> list = this.level.getEntitiesOfClass(EndCrystal.class, new AABB(blockpos1.relative(direction, 2)));
                if (list.isEmpty()) {
                    return;
                }

                list1.addAll(list);
            }
            
            BeyondTheEnd.LOGGER.debug("Found all crystals, respawning dragon.");
            this.respawnDragon(list1);
        }

    }

    private void respawnDragon(List<EndCrystal> p_64092_) {
        if (this.dragonKilled && this.respawnStage == null) {
            for(BlockPattern.BlockPatternMatch blockpattern$blockpatternmatch = this.findExitPortal(); blockpattern$blockpatternmatch != null; blockpattern$blockpatternmatch = this.findExitPortal()) {
                for(int i = 0; i < this.exitPortalPattern.getWidth(); ++i) {
                    for(int j = 0; j < this.exitPortalPattern.getHeight(); ++j) {
                        for(int k = 0; k < this.exitPortalPattern.getDepth(); ++k) {
                            BlockInWorld blockinworld = blockpattern$blockpatternmatch.getBlock(i, j, k);
                            if (blockinworld.getState().is(Blocks.BEDROCK) || blockinworld.getState().is(Blocks.END_PORTAL)) {
                                this.level.setBlockAndUpdate(blockinworld.getPos(), Blocks.END_STONE.defaultBlockState());
                            }
                        }
                    }
                }
            }

            this.respawnStage = DragonRespawnAnimation.START;
            this.respawnTime = 0;
            this.spawnExitPortal(false);
            this.respawnCrystals = p_64092_;
        }

    }

    public void resetSpikeCrystals() {

    }

    public void addPlayer(ServerPlayer player) {
        this.dragonEvent.addPlayer(player);
    }

    public void removePlayer(ServerPlayer player) {
        this.dragonEvent.removePlayer(player);
    }
    public static class StructureManager implements Savable{
        private Structure structure;

        public StructureManager() {}
        public StructureManager(CompoundTag data) {
            this.deserialise(data);
        }

        @Override
        public CompoundTag serialise() {
            CompoundTag data = new CompoundTag();

            data.put("Structure", this.getStructure().serialise());

            return data;
        }

        @Override
        public void deserialise(CompoundTag data) {
            this.structure = new Structure(data.getCompound("Structure"));
        }

        public Structure getStructure() {
            if (this.structure == null) {
                BeyondTheEnd.LOGGER.warn("Missing realm structure! Creating..");
                this.structure = new Structure();
            }

            return this.structure;
        }
        public static ServerLevel getDimension() {
            return BeyondTheEnd.getServer().getLevel(BkDimension.BEYOND_END_LEVEL);
        }
    }

    
    public static class Structure implements Savable {
        private final ResourceLocation structure;
        private boolean isPlaced;
        private BlockPos centre;
        public Structure(ResourceLocation structure, @Nullable BlockPos centre) {
            this.structure = structure;
            this.isPlaced = false;
            this.centre = centre;
        }
        public Structure() {
            this(getDefaultStructure(), new BlockPos(0, 50, 0)); // default island structure
        }
        public Structure(CompoundTag data) {
            this.structure = new ResourceLocation(data.getString("structure"));

            this.deserialise(data);
        }

        @Override
        public CompoundTag serialise() {
            CompoundTag data = new CompoundTag();

            data.putString("structure", this.structure.toString());

            data.putBoolean("isPlaced", this.isPlaced);
            if (this.centre != null)
                data.put("Centre", NbtUtils.writeBlockPos(this.centre));

            return data;
        }

        @Override
        public void deserialise(CompoundTag data) {
            this.isPlaced = data.getBoolean("isPlaced");

            if (data.contains("Centre")) {
                this.centre = NbtUtils.readBlockPos(data.getCompound("Centre"));
            }
        }

        public boolean isPlaced() {
            return this.isPlaced;
        }

        private static ResourceLocation getDefaultStructure() {
            return new ResourceLocation(BeyondTheEnd.MODID, "center");
        }
        private Optional<StructureTemplate> findStructure(ResourceLocation structure) {
            return BeyondTheEnd.getServer().getStructureManager().get(structure);
        }

        /**
         * places the structure if it is not already placed
         */
        public void verify() {
            if (!this.isPlaced()) {
                this.place();
            }
        }

        private void place() {
            this.place(StructureManager.getDimension(), true);
        }

        private void place(ServerLevel level, boolean inform) {
            long start = System.currentTimeMillis();


            if (this.isPlaced() && level!=null && level.getRandom()!=null) {
                BeyondTheEnd.LOGGER.warn("Tried to place realm structure twice");
                return;
            }

            makeInitialIsland(level,start);

            this.isPlaced = true;

        }

        public Vec3i placeComponent(long start, ServerLevel level, int addX, int height, int addZ, ResourceLocation location, StructurePlaceSettings settings){
            StructureTemplate component = this.findStructure(location).orElse(null);

            if (component == null || level==null) {
                BeyondTheEnd.LOGGER.error("Could not find island component :" + location.toString());
                return Vec3i.ZERO;
            }
            Vec3i size = component.getSize();
            BlockPos offset = new BlockPos(-size.getX() / 2+addX, height, -size.getZ() / 2 +addZ);


            component.placeInWorld(
                    level,
                    offset,
                    offset,
                    settings,
                    level.getRandom(),
                    Block.UPDATE_KNOWN_SHAPE
            );
            BeyondTheEnd.LOGGER.info("Placed " + component + " at " + offset + " in " + (System.currentTimeMillis() - start) + "ms");
            return size;
        }

        public void makeInitialIsland(ServerLevel level, long start){
            StructurePlaceSettings settings=new StructurePlaceSettings();

            Vec3i center=this.placeComponent(start,level,0,50,0,new ResourceLocation(BeyondTheEnd.MODID, "center"),settings);
            this.placeComponent(start,level,center.getX()+26,50,1,new ResourceLocation(BeyondTheEnd.MODID, "island_east"),settings);
            Vec3i south=this.placeComponent(start,level,0,51,center.getZ()+6,new ResourceLocation(BeyondTheEnd.MODID, "island_south_0"),settings);
            this.placeComponent(start,level,0,51+south.getY(),center.getZ()+6,new ResourceLocation(BeyondTheEnd.MODID, "island_south_1"),settings);

            this.placeComponent(start,level,0,50,-center.getZ()+17,new ResourceLocation(BeyondTheEnd.MODID, "island_north"),settings);
            Vec3i west=this.placeComponent(start,level,-center.getX()+15,50,0,new ResourceLocation(BeyondTheEnd.MODID, "island_west_0"),settings);
            this.placeComponent(start,level,-center.getX()+15,50+west.getY(),0,new ResourceLocation(BeyondTheEnd.MODID, "island_west_1"),settings);


            this.placeComponent(start,level,center.getX()+24,50,-center.getZ()+18,new ResourceLocation(BeyondTheEnd.MODID, "island_east_north"),settings);

            Vec3i west_n=this.placeComponent(start,level,-center.getX()+15,50,-center.getZ()+17,new ResourceLocation(BeyondTheEnd.MODID, "west_north_0"),settings);
            this.placeComponent(start,level,-center.getX()+15,50+west_n.getY(),-center.getZ()+17,new ResourceLocation(BeyondTheEnd.MODID, "west_north_1"),settings);

            Vec3i west_s=this.placeComponent(start,level,-center.getX()+15,50,center.getZ()-4,new ResourceLocation(BeyondTheEnd.MODID, "west_south_0"),settings);
            this.placeComponent(start,level,-center.getX()+15,50+west_s.getY(),center.getZ()-4,new ResourceLocation(BeyondTheEnd.MODID, "west_south_1"),settings);

            Vec3i east_s=this.placeComponent(start,level,center.getX()-11,50,center.getZ()+7,new ResourceLocation(BeyondTheEnd.MODID, "east_south_0"),settings);
            this.placeComponent(start,level,center.getX()-11,50+east_s.getY(),center.getZ()+7,new ResourceLocation(BeyondTheEnd.MODID, "east_south_1"),settings);

        }

        public BlockPos getCentre() {
            this.verify();

            if (this.centre != null) return this.centre;


            this.centre = new BlockPos(0, 80, 0);

            return this.centre;
        }

        @Override
        public String toString() {
            return "RealmStructure{" +
                    "structure=" + structure+
                    ", isPlaced=" + isPlaced +
                    ", centre=" + centre +
                    '}';
        }
    }

}
