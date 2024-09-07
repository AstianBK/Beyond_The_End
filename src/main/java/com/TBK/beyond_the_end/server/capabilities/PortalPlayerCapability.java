package com.TBK.beyond_the_end.server.capabilities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PortalPlayerCapability implements PortalPlayer{
    Player player;
    public int portalTime=0;
    private float prevPortalAnimTime;
    private float portalAnimTime;
    private boolean isInPortal;

    public void setPlayer(Player player){
        this.player=player;
    }
    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public void onLogout() {

    }

    @Override
    public void onLogin() {

    }

    @Override
    public void onJoinLevel() {

    }

    @Override
    public void copyFrom(PortalPlayer other, boolean isWasDeath) {

    }

    @Override
    public void onUpdate() {
        this.handleAetherPortal();
    }

    @Override
    public void setCanSpawnInAether(boolean canSpawnInAether) {

    }

    @Override
    public boolean canSpawnInAether() {
        return false;
    }

    @Override
    public void givePortalItem() {

    }

    @Override
    public void setCanGetPortal(boolean canGetPortal) {

    }

    @Override
    public boolean canGetPortal() {
        return false;
    }

    @Override
    public void setInPortal(boolean inPortal) {
        this.isInPortal=inPortal;
    }

    @Override
    public boolean isInPortal() {
        return this.isInPortal;
    }

    @Override
    public void setPortalTimer(int timer) {
        this.portalTime=timer;
    }

    @Override
    public int getPortalTimer() {
        return this.portalTime;
    }

    @Override
    public float getPortalAnimTime() {
        return this.portalAnimTime;
    }

    @Override
    public float getPrevPortalAnimTime() {
        return this.prevPortalAnimTime;
    }

    @Override
    public void setHitting(boolean isHitting) {

    }

    @Override
    public boolean isHitting() {
        return false;
    }

    @Override
    public void setMoving(boolean isMoving) {

    }

    @Override
    public boolean isMoving() {
        return false;
    }

    @Override
    public void setJumping(boolean isJumping) {

    }

    @Override
    public boolean isJumping() {
        return false;
    }

    @Override
    public CompoundTag serializeNBT() {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

    }

    private void handleAetherPortal() {
        if (this.getPlayer().level.isClientSide()) {
            this.prevPortalAnimTime = this.portalAnimTime;
            Minecraft minecraft = Minecraft.getInstance();
            if (this.isInPortal) {
                if (minecraft.screen != null && !minecraft.screen.isPauseScreen()) {
                    if (minecraft.screen instanceof AbstractContainerScreen) {
                        this.getPlayer().closeContainer();
                    }
                    minecraft.setScreen(null);
                }

                if (this.getPortalAnimTime() == 0.0F) {
                    //this.playPortalSound(minecraft);
                }
            }
        }

        if (this.isInPortal()) {
            ++this.portalTime;
            if (this.getPlayer().level.isClientSide()) {
                this.portalAnimTime += 0.0125F;
                if (this.getPortalAnimTime() > 1.0F) {
                    this.portalAnimTime = 1.0F;
                }
            }
            this.isInPortal = false;
        }
        else {
            if (this.getPlayer().level.isClientSide()) {
                if (this.getPortalAnimTime() > 0.0F) {
                    this.portalAnimTime -= 0.05F;
                }

                if (this.getPortalAnimTime() < 0.0F) {
                    this.portalAnimTime = 0.0F;
                }
            }
            if (this.getPortalTimer() > 0) {
                this.portalTime -= 4;
            }
        }
    }

    public static class PortalPlayerProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
        private final LazyOptional<PortalPlayer> instance=LazyOptional.of(PortalPlayerCapability::new);

        @NonNull
        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return BkCapabilities.PORTAL_PLAYER_CAPABILITY.orEmpty(cap,instance.cast());
        }

        @Override
        public CompoundTag serializeNBT() {
            return instance.orElseThrow(NullPointerException::new).serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            instance.orElseThrow(NullPointerException::new).deserializeNBT(nbt);
        }
    }
}
