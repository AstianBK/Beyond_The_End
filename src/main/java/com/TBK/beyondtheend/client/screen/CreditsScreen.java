package com.TBK.beyondtheend.client.screen;

import com.TBK.beyondtheend.client.credits.CreditCategory;
import com.TBK.beyondtheend.client.credits.CreditsManager;
import com.TBK.beyondtheend.server.network.PacketHandler;
import com.TBK.beyondtheend.server.network.message.FinishEndingPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class CreditsScreen extends Screen {
    private static final float SCROLL_SPEED = 1.0F;
    private static final float FAST_MULTIPLIER = 5.0F;

    public static final int BACKGROUND= 0xFF080A12;
    private static final int TITLE_COLOR = 0xFFE4CB83;
    private static final int CATEGORY_COLOR = 0xFFFFDD88;
    private static final int AUTHOR_COLOR = 0xFFFFFFFF;

    private static final int TITLE_SPACING = 48;
    private static final int INTRO_SPACING = 50;
    private static final int CATEGORY_SPACING = 28;
    private static final int AUTHOR_SPACING = 16;
    private static final int GROUP_SPACING = 32;

    // ==========================================
    // ESTADO
    // ==========================================

    private final Screen previousScreen;

    private List<CreditCategory> categories = List.of();

    private float scroll;
    private boolean paused = false;
    private boolean loaded = false;
    private final boolean teleportAfterCredits;
    private boolean closing = false;
    private int contentHeight = 0;

    public CreditsScreen() {
        this(null, false, false);
    }

    public CreditsScreen(boolean skipIntro) {
        this(null, skipIntro, false);
    }

    public CreditsScreen(Screen previousScreen) {
        this(previousScreen, false, false);
    }

    public CreditsScreen(Screen previousScreen, boolean skipIntro) {
        this(previousScreen, skipIntro, false);
    }

    public CreditsScreen(
            Screen previousScreen,
            boolean skipIntro,
            boolean teleportAfterCredits
    ) {
        super(Component.literal("Beyond The End - Credits"));

        this.previousScreen = previousScreen;
        this.teleportAfterCredits = teleportAfterCredits;

        this.scroll = skipIntro ? 52.0F : -24.0F;
    }

    @Override
    protected void init() {
        super.init();

        if (!loaded) {
            CreditsManager.load();

            categories = CreditsManager.getCredits();
            loaded = true;
        }

        calculateContentHeight();
    }

    private void calculateContentHeight() {
        int total = TITLE_SPACING + INTRO_SPACING;

        for (CreditCategory category : categories) {
            total += CATEGORY_SPACING;
            total += category.authors().size() * AUTHOR_SPACING;
            total += GROUP_SPACING;
        }

        total += 76;

        contentHeight = total;
    }

    @Override
    public void tick() {
        super.tick();

        if (paused || categories.isEmpty()) {
            return;
        }

        scroll += getScrollSpeed();

        if (scroll > height + contentHeight) {
            onClose();
        }
    }

    private float getScrollSpeed() {
        return hasShiftDown() ? SCROLL_SPEED * FAST_MULTIPLIER : SCROLL_SPEED;
    }

    @Override
    public void render(PoseStack ps, int mouseX, int mouseY, float partialTick) {
        fill(ps, 0, 0, width, height, BACKGROUND);

        float renderScroll = scroll;

        if (!paused && !categories.isEmpty()) {
            renderScroll += getScrollSpeed() * partialTick;
        }

        int y = Math.round(height - renderScroll);

        if (categories.isEmpty()) {
            drawCenteredString(ps, font, "No hay creditos disponibles", width / 2, height / 2, 0xFFFFFF);

            renderControls(ps);
            super.render(ps, mouseX, mouseY, partialTick);
            return;
        }


        drawScaledText(ps, "BEYOND THE END", y, 2.0F, TITLE_COLOR);

        y += TITLE_SPACING;

        drawScaledText(ps, "CREDITOS", y, 1.2F, AUTHOR_COLOR);

        y += INTRO_SPACING;

        for (CreditCategory category : categories) {

            drawScaledText(
                    ps,
                    category.category(),
                    y,
                    1.5F,
                    CATEGORY_COLOR
            );

            y += CATEGORY_SPACING;

            for (String author : category.authors()) {

                drawScaledText(
                        ps,
                        author,
                        y,
                        1.0F,
                        AUTHOR_COLOR
                );

                y += AUTHOR_SPACING;
            }

            y += GROUP_SPACING;
        }

        y += 36;

        drawScaledText(ps, "Gracias por jugar", y, 1.4F, TITLE_COLOR);

        super.render(ps, mouseX, mouseY, partialTick);

        renderControls(ps);
    }

    private void drawScaledText(PoseStack ps, String text, int y, float scale, int color) {
        if (y < -32 || y > height + 32) {
            return;
        }

        ps.pushPose();

        ps.translate(width / 2.0F, y, 0);

        ps.scale(scale, scale, 1.0F);

        drawCenteredString(ps, font, text, 0, 0, color);

        ps.popPose();
    }

    private void renderControls(PoseStack ps) {

        fill(ps, 0, height - 24, width, height, 0xE0080A12);

        String controls = paused ? "ESPACIO: Continuar | ESC: Salir"
                : "SHIFT: Acelerar | ESPACIO: Pausar | ESC: Salir";

        drawCenteredString(ps, font, controls, width / 2, height - 16, 0xFF999999);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        if (keyCode == GLFW.GLFW_KEY_SPACE) {
            paused = !paused;
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_UP) {
            scroll = Math.max(0.0F, scroll - 40.0F);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_DOWN) {
            scroll += 40.0F;
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scroll -= (float) delta * 25.0F;
        scroll = Math.max(0.0F, scroll);

        return true;
    }



    @Override
    public void onClose() {

        if (closing) {
            return;
        }
        closing = true;
        if (teleportAfterCredits && Minecraft.getInstance().getConnection() != null) {
            PacketHandler.MOD_CHANNEL.sendToServer(new FinishEndingPacket());
        }

        if (previousScreen != null) {
            Minecraft.getInstance().setScreen(previousScreen);
        } else {
            super.onClose();
        }
    }


    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
