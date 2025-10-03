package com.TBK.beyondtheend.server.world.processor;

import com.TBK.beyondtheend.common.registry.BKStructuresProcessor;
import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

public class BossIslandProcessor extends StructureProcessor {

    public static final BossIslandProcessor INSTANCE = new BossIslandProcessor();

    public static final Codec<BossIslandProcessor> CODEC = Codec.unit(BossIslandProcessor.INSTANCE);

    @Override
    protected StructureProcessorType<?> getType() {
        return BKStructuresProcessor.BOSS_ROOM.get();
    }
}
