package com.TBK.beyondtheend;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class EndingTeleportHandler {

    public static final ResourceKey<Level> REALM_KEY = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("ender_journey", "the_forgotten_realm"));

    private static final Set<UUID> pendingPlayers = new HashSet<>();

    private EndingTeleportHandler() {}

    public static void allowTeleport(ServerPlayer player) {
        pendingPlayers.add(player.getUUID());
    }

    public static void finishEnding(ServerPlayer player) {
        if (!pendingPlayers.contains(player.getUUID())) {
            return;
        }

        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }

        ServerLevel destination = server.getLevel(REALM_KEY);

        if (destination == null) {
            return;
        }
        pendingPlayers.remove(player.getUUID());
        player.stopRiding();

        player.teleportTo(destination, 0.5D, 142.0D, 0.5D, player.getYRot(), player.getXRot());
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
        System.out.println("Jugador teletransportado a " + REALM_KEY.location());
    }
}
