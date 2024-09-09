package com.TBK.beyond_the_end.client;

import com.TBK.beyond_the_end.CommonProxy;
import com.TBK.beyond_the_end.common.registry.BkBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {

    public void init(){
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
        BlockEntityRenderers.register(BkBlockEntity.PORTAL.get(), TheEndPortalRenderer::new);
    }
}
