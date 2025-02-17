package com.TBK.beyond_the_end.server.capabilities;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.server.entity.JellyfishEntity;
import com.TBK.beyond_the_end.server.network.PacketHandler;
import com.TBK.beyond_the_end.server.network.message.PacketSync;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
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
    private int charge;
    public int animTimer0=0;
    public int animTimer=0;

    public void setCharge(int charge){
        this.charge=charge;
    }
    public int getCharge(){
        return this.charge;
    }
    public void addCharge(){
        if(this.getCharge()<5){
            this.setCharge(this.getCharge()+1);
        }
    }

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
    public void setCanSpawnInDimension(boolean canSpawnInAether) {

    }

    @Override
    public boolean canSpawnInDimension() {
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

    public float damageFinal(JellyfishEntity jellyfish, float damage){
        if(this.getCharge()>0){
            this.setCharge(0);
            if(!this.player.level.isClientSide){
                PacketHandler.sendToPlayer(new PacketSync(0,this.animTimer), (ServerPlayer) this.player);
            }

            return damage+5*this.getCharge();
        }else {
            return damage*0.1F;
        }
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
        CompoundTag nbt=new CompoundTag();
        nbt.putInt("charge",this.getCharge());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.setCharge(nbt.getInt("charge"));
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

        if(this.getPlayer().level.isClientSide){
            this.animTimer0=this.animTimer;
            if(this.animTimer++>19){
                this.animTimer=0;
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

    public float getAnim(float partialTicks) {
        return Math.max((20.0F - Mth.lerp(partialTicks,(float) this.animTimer0,(float)this.animTimer))/ 20.0F,0.1F);
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
