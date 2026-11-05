
package com.TBK.beyondtheend.client.reproductor;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.Frame;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public final class VideoManager {
    public static final VideoManager INSTANCE = new VideoManager();

    private final Object lock = new Object();

    private ResourceLocation videoLocation;
    private Thread decoderThread;

    private volatile BufferedImage currentFrame;
    private volatile boolean ready = false;
    private volatile boolean playing = false;
    private volatile boolean finished = false;
    private volatile String error = null;

    private boolean resetRequested = false;

    private VideoManager() {
    }

    public void preload(ResourceLocation location) {
        synchronized (lock) {
            if (decoderThread != null) {
                if (!location.equals(videoLocation)) {
                    System.err.println(
                            "[VIDEO] El manager ya tiene otro video cargado."
                    );
                }
                return;
            }

            videoLocation = location;

            decoderThread = new Thread(() -> decoderLoop(location), "bte-video-decoder");

            decoderThread.setDaemon(true);
            decoderThread.start();
        }
    }

    public void play(ResourceLocation location) {
        preload(location);

        synchronized (lock) {
            if (!location.equals(videoLocation)) {
                return;
            }

            finished = false;
            playing = true;
            lock.notifyAll();
        }
    }

    public void stop() {
        synchronized (lock) {
            if (decoderThread == null) {
                return;
            }

            playing = false;
            finished = false;
            ready = false;

            currentFrame = null;

            resetRequested = true;
            lock.notifyAll();
        }
    }

    public BufferedImage getCurrentFrame() {
        return currentFrame;
    }

    public boolean isReady() {
        return ready;
    }

    public boolean isFinished() {
        return finished;
    }

    public String getError() {
        return error;
    }

    private void decoderLoop(ResourceLocation location) {
        File temp = null;
        try {
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(location);

            if (resource.isEmpty()) {
                throw new IllegalStateException(
                        "No se encontro el video: " + location
                );
            }

            temp = File.createTempFile("bte_video_", ".mp4");
            temp.deleteOnExit();

            try (InputStream in = resource.get().open()) {
                Files.copy(in, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }


            try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(temp); Java2DFrameConverter converter = new Java2DFrameConverter()) {
                grabber.start();

                double fps = grabber.getFrameRate();
                if (!Double.isFinite(fps) || fps <= 0) {
                    fps = 30.0;
                }

                long frameTimeMs = Math.max(1L, Math.round(1000.0 / fps));

                BufferedImage first = grabNextImage(grabber, converter);

                if (first == null) {
                    throw new IllegalStateException("El video no tiene fotogramas decodificables.");
                }

                currentFrame = first;
                ready = true;


                while (!Thread.currentThread().isInterrupted()) {

                    boolean restart;

                    synchronized (lock) {
                        while (!playing && !resetRequested) {
                            lock.wait();
                        }

                        restart = resetRequested;

                        if (restart) {
                            resetRequested = false;
                        }
                    }

                    // Volver al principio sin recrear FFmpeg.
                    if (restart) {
                        ready = false;

                        grabber.setTimestamp(0);

                        BufferedImage resetFrame = grabNextImage(grabber, converter);

                        if (resetFrame == null) {
                            throw new IllegalStateException("No se pudo reiniciar el video.");
                        }

                        currentFrame = resetFrame;
                        ready = true;

                        System.out.println(
                                "[VIDEO] Preparado para reproducir otra vez."
                        );

                        continue;
                    }

                    // Respetar aproximadamente los FPS del archivo.
                    synchronized (lock) {
                        if (playing && !resetRequested) {
                            lock.wait(frameTimeMs);
                        }

                        if (!playing || resetRequested) {
                            continue;
                        }
                    }

                    BufferedImage next = grabNextImage(grabber, converter);

                    if (next == null) {
                        synchronized (lock) {
                            if (playing && !resetRequested) {
                                playing = false;
                                finished = true;

                                System.out.println("[VIDEO] Reproduccion terminada.");
                            }
                        }

                        continue;
                    }

                    synchronized (lock) {
                        if (playing && !resetRequested) {
                            currentFrame = next;
                        }
                    }
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

        } catch (Throwable e) {
            error = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();

            ready = false;
            playing = false;

            System.err.println("[VIDEO] Error en el manager:");
            e.printStackTrace();

        } finally {
            if (temp != null && temp.exists()) {
                temp.delete();
            }
        }
    }

    private BufferedImage grabNextImage(FFmpegFrameGrabber grabber, Java2DFrameConverter converter) throws Exception {
        Frame frame = grabber.grabImage();

        if (frame == null) {
            return null;
        }

        BufferedImage image = converter.convert(frame);

        if (image == null) {
            return null;
        }
        return Java2DFrameConverter.cloneBufferedImage(image);
    }
}
