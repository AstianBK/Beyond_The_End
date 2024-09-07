package com.TBK.beyond_the_end.server.network.message;

import com.TBK.beyond_the_end.common.DimensionUtil;
import com.TBK.beyond_the_end.server.capabilities.PortalPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketSync implements Packet<PacketListener> {
    private final Player player;
    public PacketSync(FriendlyByteBuf buf) {
        assert Minecraft.getInstance().level!=null;
        this.player=Minecraft.getInstance().level.getPlayerByUUID(buf.readUUID());

    }

    public PacketSync(Player player) {
        this.player=player;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.player.getUUID());
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
        PortalPlayer.get(this.player).ifPresent(portalPlayer -> {
            portalPlayer.setPlayer(this.player);
        });
    }

    @Override
    public void handle(PacketListener p_131342_) {

    }
}
