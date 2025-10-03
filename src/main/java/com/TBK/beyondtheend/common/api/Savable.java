package com.TBK.beyondtheend.common.api;

import net.minecraft.nbt.CompoundTag;

public interface Savable {
	CompoundTag serialise();
	void deserialise(CompoundTag data);
}
