package com.TBK.beyond_the_end.common.registry;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.common.blocks.block_entity.PortalBlockEntity;
import com.TBK.beyond_the_end.server.world.processor.BossIslandProcessor;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BkBlockEntity {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BeyondTheEnd.MODID);
    public static final RegistryObject<BlockEntityType<PortalBlockEntity>> BOSS_ROOM = BLOCK_ENTITY_TYPE.register("portal",
            ()->BlockEntityType.Builder.of(PortalBlockEntity::new, BkCommonRegistry.PORTAL.get())
                    .build(null));

}
