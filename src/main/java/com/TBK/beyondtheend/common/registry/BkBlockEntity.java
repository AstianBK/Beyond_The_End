package com.TBK.beyondtheend.common.registry;

import com.TBK.beyondtheend.BeyondTheEnd;
import com.TBK.beyondtheend.common.blocks.block_entity.PortalBlockEntity;
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
