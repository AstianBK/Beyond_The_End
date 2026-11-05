package com.TBK.beyondtheend.server.network.message;

import com.TBK.beyondtheend.EndingTeleportHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FinishEndingPacket implements Packet<PacketListener> {
    public FinishEndingPacket(){

    }
    public FinishEndingPacket(FriendlyByteBuf buf){

    }
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();

        ServerPlayer player = context.getSender();

        if (player != null) {
            EndingTeleportHandler.finishEnding(player);
        }

        context.setPacketHandled(true);
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {

    }

    @Override
    public void handle(PacketListener packetListener) {

    }
}
