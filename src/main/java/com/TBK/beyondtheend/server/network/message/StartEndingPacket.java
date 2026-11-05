package com.TBK.beyondtheend.server.network.message;

import com.TBK.beyondtheend.server.network.ClientEndingHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StartEndingPacket implements Packet<PacketListener> {
    public StartEndingPacket(){

    }
    public StartEndingPacket(FriendlyByteBuf buf){

    }
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientEndingHandler::request);

        ctx.get().setPacketHandled(true);
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {

    }

    @Override
    public void handle(PacketListener packetListener) {

    }
}
