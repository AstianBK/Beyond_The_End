package com.TBK.beyondtheend.server.network;

import com.TBK.beyondtheend.BeyondTheEnd;
import com.TBK.beyondtheend.client.reproductor.VideoScreen;

import com.TBK.beyondtheend.client.screen.CreditsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = BeyondTheEnd.MODID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class ClientEndingHandler {

    private static final ResourceLocation ENDING_VIDEO =
            new ResourceLocation(
                    BeyondTheEnd.MODID,
                    "videos/video.mp4"
            );

    private static volatile boolean pendingEnding = false;

    private ClientEndingHandler() {
    }
    public static void request() {
        pendingEnding = true;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!pendingEnding) {
            return;
        }

        pendingEnding = false;

        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null) {
            return;
        }

        if (mc.screen instanceof VideoScreen || mc.screen instanceof CreditsScreen) {
            return;
        }

        System.out.println("[BTE] Abriendo VideoScreen desde: " + Thread.currentThread().getName());

        mc.setScreen(new VideoScreen(ENDING_VIDEO, true));
    }
}
