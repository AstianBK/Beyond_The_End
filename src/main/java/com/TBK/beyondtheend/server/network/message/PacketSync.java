package com.TBK.beyondtheend.server.network.message;

import com.TBK.beyondtheend.server.capabilities.PortalPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSync implements Packet<PacketListener> {
    private final int charge;
    private final int time;
    public PacketSync(FriendlyByteBuf buf) {
        this.charge=buf.readInt();
        this.time=buf.readInt();

    }

    public PacketSync(int charge,int timer) {
        this.charge=charge;
        this.time = timer;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.charge);
        buf.writeInt(this.time);
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
        Minecraft mc = Minecraft.getInstance();
        assert mc.player!=null;
        PortalPlayer.get(mc.player).ifPresent(portalPlayer -> {
            portalPlayer.setPlayer(mc.player);
            portalPlayer.setCharge(this.charge);
            portalPlayer.animTimer=this.time;
        });
    }

    @Override
    public void handle(PacketListener p_131342_) {

    }
}
