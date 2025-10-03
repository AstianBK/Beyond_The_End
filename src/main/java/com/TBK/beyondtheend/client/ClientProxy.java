package com.TBK.beyondtheend.client;

import com.TBK.beyondtheend.CommonProxy;
import com.TBK.beyondtheend.common.registry.BkBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {
    public static int lastTick = -1;
    public static float[] randomOffsets = new float[3];

    public void init(){
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
        BlockEntityRenderers.register(BkBlockEntity.PORTAL.get(), TheEndPortalRenderer::new);
    }
}
