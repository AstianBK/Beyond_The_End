package com.TBK.beyondtheend.client;

import com.TBK.beyondtheend.BeyondTheEnd;

import com.TBK.beyondtheend.client.reproductor.VideoManager;
import com.TBK.beyondtheend.client.reproductor.VideoScreen;
import com.TBK.beyondtheend.client.screen.CreditsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = BeyondTheEnd.MODID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class ChatTrigger {

    private static final ResourceLocation VIDEO =
            new ResourceLocation(
                    BeyondTheEnd.MODID,
                    "videos/video.mp4"
            );

    private static boolean preloadStarted = false;
    private static boolean pendingVideo = false;
    private static boolean pendingCredits = false;
    @SubscribeEvent
    public static void onChat(ClientChatEvent event) {
        if (event.getMessage().equalsIgnoreCase("pon video")) {
            event.setCanceled(true);
            pendingVideo = true;
        }
        if (event.getMessage().equalsIgnoreCase("pon creditos")) {
            event.setCanceled(true);
            pendingCredits = true;
            return;
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        // Precargar al llegar al menu principal.
        // No hace falta entrar en una partida.
        if (!preloadStarted && mc.screen instanceof TitleScreen) {
            preloadStarted = true;

            VideoManager.INSTANCE.preload(VIDEO);

            System.out.println(
                    "[VIDEO] Precarga iniciada desde el menu."
            );
        }

        // Abrir despues de que el chat procese Enter.
        if (pendingVideo) {
            pendingVideo = false;

            // Respaldo por si no se inicio la precarga.
            VideoManager.INSTANCE.preload(VIDEO);

            if (!(mc.screen instanceof VideoScreen)) {
                mc.setScreen(new VideoScreen(VIDEO));
            }
        }
        if (pendingCredits) {
            pendingCredits = false;

            Minecraft.getInstance().setScreen(
                    new CreditsScreen()
            );
        }
    }
}
