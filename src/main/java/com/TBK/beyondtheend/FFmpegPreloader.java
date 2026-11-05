package com.TBK.beyondtheend;

import com.TBK.beyondtheend.BeyondTheEnd;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.bytedeco.javacv.FFmpegFrameGrabber;

@Mod.EventBusSubscriber(modid = BeyondTheEnd.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class FFmpegPreloader {

    private static volatile boolean ready = false;
    private static volatile boolean failed = false;

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {

        Thread thread = new Thread(() -> {
            long start = System.currentTimeMillis();

            try {
                System.out.println("[VIDEO] Precargando FFmpeg...");

                FFmpegFrameGrabber.tryLoad();

                ready = true;

                System.out.println("[VIDEO] FFmpeg cargado en " + (System.currentTimeMillis() - start) + " ms");

            } catch (Throwable e) {
                failed = true;

                System.err.println("[VIDEO] Error al precargar FFmpeg");
                e.printStackTrace();
            }

        }, "ffmpeg-preloader");

        thread.setDaemon(true);
        thread.start();
    }

    public static boolean isReady() {
        return ready;
    }

    public static boolean hasFailed() {
        return failed;
    }
}
