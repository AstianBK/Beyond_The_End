
package com.TBK.beyondtheend.client.reproductor;

import com.TBK.beyondtheend.client.screen.CreditsScreen;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import org.lwjgl.glfw.GLFW;

import java.awt.image.BufferedImage;

public class VideoScreen extends Screen {

    private static final float FADE_SECONDS = 0.9F;

    private final ResourceLocation videoLocation;
    private final boolean showCreditsAfterVideo;

    private DynamicTexture texture;
    private ResourceLocation texId;
    private BufferedImage lastUploaded;

    private boolean started = false;
    private boolean transitionStarted = false;
    private boolean screenChanged = false;
    private long transitionStartNs = 0L;

    public VideoScreen(ResourceLocation videoLocation) {
        this(videoLocation, false);
    }

    public VideoScreen(ResourceLocation videoLocation, boolean showCreditsAfterVideo) {
        super(Component.literal("Video"));

        this.videoLocation = videoLocation;
        this.showCreditsAfterVideo = showCreditsAfterVideo;
    }

    @Override
    protected void init() {
        super.init();

        if (!started) {
            started = true;
            VideoManager.INSTANCE.play(videoLocation);
        }
    }


    @Override
    public void render(PoseStack ps, int mouseX, int mouseY, float partialTick) {
        renderBackground(ps);

        VideoManager manager = VideoManager.INSTANCE;

        if (manager.isFinished() && showCreditsAfterVideo && !transitionStarted) {

            startTransition();
        }

        BufferedImage img = manager.getCurrentFrame();

        if (!transitionStarted && img != null) {
            uploadFrame(img);
        }

        if (texId != null && lastUploaded != null) {
            RenderSystem.setShaderTexture(0, texId);

            blit(ps, 0, 0, width, height, 0, 0, lastUploaded.getWidth(), lastUploaded.getHeight(), lastUploaded.getWidth(), lastUploaded.getHeight());
        } else if (!transitionStarted) {
            String message = "Preparando video...";

            if (manager.getError() != null) {
                message = "Error: " + manager.getError();
            }

            drawCenteredString(ps, font, message, width / 2, height / 2, 0xFFFFFF);
        }

        float fade = 0.0F;

        if (transitionStarted) {
            fade = getTransitionProgress();
            float smooth = fade * fade * (3.0F - 2.0F * fade);

            int alpha = Math.min(255, Math.round(smooth * 255.0F));

            int color = (alpha << 24) | (CreditsScreen.BACKGROUND & 0x00FFFFFF);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            fill(ps, 0, 0, width, height, color);

            RenderSystem.disableBlend();
        }

        super.render(ps, mouseX, mouseY, partialTick);

        if (transitionStarted && fade >= 1.0F) {
            openCredits(false);
            return;
        }
        if (!showCreditsAfterVideo && manager.isFinished()) {
            Minecraft.getInstance().setScreen(null);
        }
    }

    private void startTransition() {
        transitionStarted = true;
        transitionStartNs = System.nanoTime();
    }

    private float getTransitionProgress() {
        double elapsedSeconds = (System.nanoTime() - transitionStartNs) / 1_000_000_000.0D;

        return (float) Math.min(1.0D, elapsedSeconds / FADE_SECONDS);
    }

    private void openCredits(boolean skipped) {
        if (screenChanged) {
            return;
        }

        screenChanged = true;

        Minecraft.getInstance().setScreen(new CreditsScreen(null, skipped, true));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (showCreditsAfterVideo && (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_SPACE)) {
            openCredits(true);
            return true;
        }

        return super.keyPressed(
                keyCode,
                scanCode,
                modifiers
        );
    }
    private void uploadFrame(BufferedImage img) {
        if (img == lastUploaded) {
            return;
        }

        int w = img.getWidth();
        int h = img.getHeight();

        if (texture == null || texture.getPixels() == null || texture.getPixels().getWidth() != w || texture.getPixels().getHeight() != h) {

            releaseTexture();

            texture = new DynamicTexture(w, h, false);

            texId = Minecraft.getInstance().getTextureManager().register("bte_video", texture);
        }

        NativeImage pixels = texture.getPixels();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {

                int argb = img.getRGB(x, y);

                int abgr = (argb & 0xFF00FF00) | ((argb & 0xFF) << 16) | ((argb >>> 16) & 0xFF);

                pixels.setPixelRGBA(x, y, abgr);
            }
        }

        texture.upload();
        lastUploaded = img;
    }

    private void releaseTexture() {
        if (texId != null) {
            Minecraft.getInstance().getTextureManager().release(texId);}


        texture = null;
        texId = null;
        lastUploaded = null;
    }

    @Override
    public void removed() {
        VideoManager.INSTANCE.stop();
        releaseTexture();

        super.removed();
    }
}
