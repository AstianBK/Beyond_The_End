package com.TBK.beyondtheend.common.registry;

import com.TBK.beyondtheend.BeyondTheEnd;
import com.TBK.beyondtheend.server.world.processor.BossIslandProcessor;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class BKStructuresProcessor {
    public static final DeferredRegister<StructureProcessorType<?>> STRUCTURE_PROCESSOR_TYPES = DeferredRegister.create(Registry.STRUCTURE_PROCESSOR_REGISTRY, BeyondTheEnd.MODID);
    public static final RegistryObject< StructureProcessorType<BossIslandProcessor>> BOSS_ROOM = STRUCTURE_PROCESSOR_TYPES.register("boss_room", () -> () -> BossIslandProcessor.CODEC);

}
