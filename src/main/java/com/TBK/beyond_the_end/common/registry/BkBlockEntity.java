package com.TBK.beyond_the_end.common.registry;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.common.blocks.block_entity.PortalBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BkBlockEntity {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BeyondTheEnd.MODID);
    public static final RegistryObject<BlockEntityType<PortalBlockEntity>> PORTAL = BLOCK_ENTITY_TYPE.register("portal",
            ()->BlockEntityType.Builder.of(PortalBlockEntity::new, BkCommonRegistry.PORTAL.get())
                    .build(null));

}
