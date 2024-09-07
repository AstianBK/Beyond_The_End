package com.TBK.beyond_the_end.common.mixin;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Level.class)
public interface LevelAccesor {
    @Mutable
    @Accessor("levelData")
    void setLevelData(WritableLevelData data);
}
