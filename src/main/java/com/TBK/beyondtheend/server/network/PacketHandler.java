package com.TBK.beyondtheend.server.network;

import com.TBK.beyondtheend.BeyondTheEnd;
import com.TBK.beyondtheend.server.network.message.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1.0";
    public static SimpleChannel MOD_CHANNEL;

    public static void registerMessages() {
        int index = 0;
        SimpleChannel channel= NetworkRegistry.ChannelBuilder.named(
                        new ResourceLocation(BeyondTheEnd.MODID, "messages"))
                .networkProtocolVersion(()-> PROTOCOL_VERSION)
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        MOD_CHANNEL=channel;

        channel.registerMessage(index++, PacketLeavingDimension.class, PacketLeavingDimension::write,
                PacketLeavingDimension::new, PacketLeavingDimension::handle);

        channel.registerMessage(index++, PacketTravelDimension.class, PacketTravelDimension::write,
                PacketTravelDimension::new, PacketTravelDimension::handle);

        channel.registerMessage(index++, PacketSync.class, PacketSync::write,
                PacketSync::new, PacketSync::handle);

        channel.registerMessage(index++, PacketTargetDragon.class, PacketTargetDragon::write,
                PacketTargetDragon::new, PacketTargetDragon::handle);
        channel.registerMessage(index++, PacketActionDragon.class, PacketActionDragon::write,
                PacketActionDragon::new, PacketActionDragon::handle);

        channel.registerMessage(index++, PacketScreenDirt.class, PacketScreenDirt::write,
                PacketScreenDirt::new, PacketScreenDirt::handle);

        channel.registerMessage(index++, PacketFlameParticles.class, PacketFlameParticles::write,
                PacketFlameParticles::new, PacketFlameParticles::handle);

        channel.registerMessage(index++, PacketNextActionJellyfish.class, PacketNextActionJellyfish::write,
                PacketNextActionJellyfish::new, PacketNextActionJellyfish::handle);

        channel.registerMessage(index++, PacketPlaySound.class, PacketPlaySound::toBytes,
                PacketPlaySound::new, PacketPlaySound::handle);






    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        MOD_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),message);
    }

    public static <MSG> void sendToServer(MSG message) {
        MOD_CHANNEL.sendToServer(message);
    }

    public static <MSG> void sendToAllTracking(MSG message, LivingEntity entity) {
        MOD_CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), message);
    }
}
