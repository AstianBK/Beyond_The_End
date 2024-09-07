package com.TBK.beyond_the_end.server.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public interface PortalPlayer extends INBTSerializable<CompoundTag> {

    Player getPlayer();

    static LazyOptional<PortalPlayer> get(Player player) {
        return player.getCapability(BkCapabilities.PORTAL_PLAYER_CAPABILITY);
    }
    public void setPlayer(Player player);

    void onLogout();
    void onLogin();

    void onJoinLevel();

    void copyFrom(PortalPlayer other, boolean isWasDeath);

    void onUpdate();

    void setCanSpawnInAether(boolean canSpawnInAether);
    boolean canSpawnInAether();

    void givePortalItem();
    void setCanGetPortal(boolean canGetPortal);
    boolean canGetPortal();

    void setInPortal(boolean inPortal);
    boolean isInPortal();

    void setPortalTimer(int timer);
    int getPortalTimer();

    float getPortalAnimTime();
    float getPrevPortalAnimTime();

    void setHitting(boolean isHitting);
    boolean isHitting();

    void setMoving(boolean isMoving);
    boolean isMoving();

    void setJumping(boolean isJumping);
    boolean isJumping();
}
