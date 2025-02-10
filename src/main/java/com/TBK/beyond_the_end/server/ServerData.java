package com.TBK.beyond_the_end.server;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.common.registry.BkDimension;
import com.TBK.beyond_the_end.server.entity.FallenDragonFight;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

/**
 * Data that will be saved to the world in .nbt form
 * For saving across server restarts.
 * Remember to call markDirty() after setting a value to ensure it saves
 *
 * @author duzo
 */
public class ServerData extends SavedData {
	private FallenDragonFight.StructureManager realmManager;
	public FallenDragonFight.StructureManager getStructureManager() {
		if (this.realmManager == null) {
			BeyondTheEnd.LOGGER.warn("Missing realm manager! Creating..");
			this.realmManager = new FallenDragonFight.StructureManager();
		}

		return this.realmManager;
	}

	public static ServerData get() {
		DimensionDataStorage manager = BeyondTheEnd.getServer().getLevel(BkDimension.BEYOND_END_LEVEL).getDataStorage();

		ServerData state = manager.computeIfAbsent(
				ServerData::load,
				ServerData::new,
				BeyondTheEnd.MODID
		);

		state.setDirty(); // bad code

		return state;
	}

	@Override
	public CompoundTag save(CompoundTag data) {
		data.put("StructureManager",this.getStructureManager().serialise());
		return data;
	}
	public static ServerData load(CompoundTag data) {
		ServerData created = new ServerData();
		CompoundTag tag=data.getCompound("StructureManager");
		created.realmManager = new FallenDragonFight.StructureManager(tag);
		return created;
	}

}
