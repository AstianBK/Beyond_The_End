package com.TBK.beyond_the_end.common.api;

import net.minecraft.nbt.CompoundTag;

public interface Savable {
	CompoundTag serialise();
	void deserialise(CompoundTag data);
}
