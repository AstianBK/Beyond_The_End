package com.TBK.beyond_the_end.server.network.message;

import com.TBK.beyond_the_end.server.capabilities.PortalPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketScreenDirt implements Packet<PacketListener> {
    private final int value;
    public PacketScreenDirt(FriendlyByteBuf buf) {
        this.value = buf.readInt();
    }

    public PacketScreenDirt(int value) {
        this.value = value;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.value);

    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() ->{
            assert context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT;
            handlerAnim();
        });
        context.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handlerAnim() {
        PortalPlayer.get(Minecraft.getInstance().player).ifPresent(e->e.setCharge(this.value));
    }

    @Override
    public void handle(PacketListener p_131342_) {

    }
}
