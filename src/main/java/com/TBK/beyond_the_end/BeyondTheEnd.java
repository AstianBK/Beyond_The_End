package com.TBK.beyond_the_end;

import com.TBK.beyond_the_end.client.ClientProxy;
import com.TBK.beyond_the_end.client.particle.BKParticles;
import com.TBK.beyond_the_end.client.particle.custom.FlameParticles;
import com.TBK.beyond_the_end.client.particle.custom.MistParticles;
import com.TBK.beyond_the_end.client.renderer.*;
import com.TBK.beyond_the_end.common.registry.*;
import com.TBK.beyond_the_end.server.ServerData;
import com.TBK.beyond_the_end.server.entity.FallenDragonFight;
import com.TBK.beyond_the_end.server.entity.JellyfishFightEvent;
import com.TBK.beyond_the_end.server.network.PacketHandler;
import com.TBK.beyond_the_end.server.world.BKConfigFeatures;
import com.mojang.logging.LogUtils;
import net.minecraft.client.particle.DragonBreathParticle;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.DragonFireballRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Unit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

@Mod(BeyondTheEnd.MODID)
public class BeyondTheEnd
{
    public static FallenDragonFight bossFight = null;
    public static JellyfishFightEvent jellyfishFightEvent = null;
    public static final String MODID = "beyond_the_end";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static CommonProxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    public static final TicketType<Unit> JELLY = TicketType.create("jelly", (p_9460_, p_9461_) -> {
        return 0;
    });
    public static int z=5;
    public static int x=1;

    public BeyondTheEnd()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::clientSetup);
        BKEntityType.ENTITY_TYPES.register(modEventBus);
        BKConfigFeatures.FEATURES.register(modEventBus);
        BkBlockEntity.BLOCK_ENTITY_TYPE.register(modEventBus);
        BkCommonRegistry.BLOCKS.register(modEventBus);
        BkCommonRegistry.ITEMS.register(modEventBus);
        BKParticles.PARTICLE_TYPES.register(modEventBus);
        BTESounds.SOUND_EVENTS.register(modEventBus);
        BkPoi.POI.register(modEventBus);
        PacketHandler.registerMessages();
        MinecraftForge.EVENT_BUS.register(this);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->{
            modEventBus.addListener(this::registerParticleFactories);
            modEventBus.addListener(this::registerRenderers);
        });
    }

    @OnlyIn(Dist.CLIENT)
    public void registerRenderers(FMLCommonSetupEvent event){
        EntityRenderers.register(BKEntityType.FALLEN_DRAGON.get(), FallenDragonRenderer::new);
        EntityRenderers.register(BKEntityType.FALLEN_DRAGON_FAKE.get(), FallenDragonFakeRenderer::new);
        EntityRenderers.register(BKEntityType.JELLYFISH.get(), JellyfishRenderer::new);
        EntityRenderers.register(BKEntityType.JELLYFISH_MINION.get(), JellyfishMinionRenderer::new);
        EntityRenderers.register(BKEntityType.CHARGE_FOLLOWING.get(), ChargeFollowingRenderer::new);
        EntityRenderers.register(BKEntityType.CHARGE_FLASH.get(), ChargeFlashRenderer::new);
        EntityRenderers.register(BKEntityType.FALLEN_DRAGON_FIREBALL.get(), FallenFireballRenderer::new);

    }

    @OnlyIn(Dist.CLIENT)
    public void registerParticleFactories(RegisterParticleProvidersEvent event) {
        if(BKParticles.FLAME_PARTICLE.isPresent()){
            event.register(BKParticles.FLAME_PARTICLE.get(), FlameParticles.Factory::new);
        }
        if(BKParticles.MIST_PARTICLE.isPresent()){
            event.register(BKParticles.MIST_PARTICLE.get(), MistParticles.Factory::new);
        }
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> PROXY.init());
    }

    public static MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

}
