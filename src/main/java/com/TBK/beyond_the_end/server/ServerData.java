package com.TBK.beyond_the_end.server;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.common.registry.BkDimension;
import com.TBK.beyond_the_end.server.entity.FallenDragonFight;
import com.TBK.beyond_the_end.server.entity.JellyfishFightEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

/**
 * Data that will be saved to the world in .nbt form
 * For saving across server restarts.
 * Remember to call markDirty() after setting a value to ensure it saves
 *
 */
public class ServerData extends SavedData {
	private FallenDragonFight.StructureManager realmManager;
	private CompoundTag tag=null;
	public FallenDragonFight.StructureManager getStructureManager() {
		if (this.realmManager == null) {
			BeyondTheEnd.LOGGER.warn("Missing realm manager! Creating..");
			this.realmManager = new FallenDragonFight.StructureManager();
		}

		return this.realmManager;
	}

	public static ServerData get() {
		DimensionDataStorage manager = BeyondTheEnd.getServer().getLevel(BkDimension.BEYOND_END_LEVEL)
				.getDataStorage();

		ServerData state = manager.computeIfAbsent(
				ServerData::load,
				ServerData::new,
				BeyondTheEnd.MODID
		);

		state.setDirty();

		return state;
	}

	public CompoundTag tag(){
		this.setDirty();
		return this.tag;
	}

	@Override
	public CompoundTag save(CompoundTag data) {
		data.put("StructureManager",this.getStructureManager().serialise());
		if(BeyondTheEnd.bossFight!=null){
			data.put("dragonBattle",BeyondTheEnd.bossFight.saveData());
		}
		if(BeyondTheEnd.jellyfishFightEvent!=null){
			data.put("jellyfishBattle",BeyondTheEnd.jellyfishFightEvent.saveData());
		}
		this.tag=data;
		return data;
	}
	public static ServerData load(CompoundTag data) {
		ServerData created = new ServerData();
		CompoundTag tag=data.getCompound("StructureManager");
		created.realmManager = new FallenDragonFight.StructureManager(tag);
		created.tag=data;
		if (data.contains("dragonBattle")){
			long i = BeyondTheEnd.getServer().getWorldData().worldGenSettings().seed();
			BeyondTheEnd.bossFight = new FallenDragonFight(BeyondTheEnd.getServer().getLevel(BkDimension.BEYOND_END_LEVEL),i,data.getCompound("dragonBattle"));
		}
		if(data.contains("jellyfishBattle")){
			BeyondTheEnd.jellyfishFightEvent = new JellyfishFightEvent(BeyondTheEnd.getServer().getLevel(BkDimension.BEYOND_END_LEVEL),data.getCompound("jellyfishBattle"));
		}
		created.setDirty();
		return created;
	}

}
