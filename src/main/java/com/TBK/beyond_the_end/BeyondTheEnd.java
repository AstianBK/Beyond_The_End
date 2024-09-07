package com.TBK.beyond_the_end;

import com.TBK.beyond_the_end.client.ClientProxy;
import com.TBK.beyond_the_end.common.registry.BkCommonRegistry;
import com.TBK.beyond_the_end.common.registry.BkPoi;
import com.TBK.beyond_the_end.server.network.PacketHandler;
import com.TBK.beyond_the_end.server.world.BKConfigFeatures;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import javax.annotation.Nullable;

@Mod(BeyondTheEnd.MODID)
public class BeyondTheEnd
{
    public static final String MODID = "beyond_the_end";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static CommonProxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);


    public BeyondTheEnd()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::clientSetup);
        BKConfigFeatures.FEATURES.register(modEventBus);
        BkCommonRegistry.BLOCKS.register(modEventBus);
        BkCommonRegistry.ITEMS.register(modEventBus);
        modEventBus.addListener(this::registerDimensionSpecialEffects);
        BkPoi.POI.register(modEventBus);
        PacketHandler.registerMessages();
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> PROXY.init());
    }

    @OnlyIn(Dist.CLIENT)
    public void registerDimensionSpecialEffects(FMLClientSetupEvent event) {
        System.out.print("\n--------------entro-----------\n");
        DimensionSpecialEffects customEffect = new DimensionSpecialEffects(Float.NaN, true, DimensionSpecialEffects.SkyType.END, false, false) {
            @Override
            public Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight) {
                return color.scale((double)0.15F);
            }
            @Nullable
            public float[] getSunriseColor(float p_108888_, float p_108889_) {
                return null;
            }


            @Override
            public boolean isFoggyAt(int x, int y) {
                return false;
            }
        };
        event.enqueueWork(() -> DimensionSpecialEffects.EFFECTS.put(new ResourceLocation("beyond_the_end:the_new_end"), customEffect));
    }
}
