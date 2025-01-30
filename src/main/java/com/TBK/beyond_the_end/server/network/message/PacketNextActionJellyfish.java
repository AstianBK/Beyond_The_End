package com.TBK.beyond_the_end.server.network.message;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.server.entity.JellyfishEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketNextActionJellyfish implements Packet<PacketListener> {
    private final int timeForNextAction;
    private final int idJellyfish;
    private final int idAction;
    public PacketNextActionJellyfish(FriendlyByteBuf buf) {
        this.idJellyfish = buf.readInt();
        this.timeForNextAction = buf.readInt();
        this.idAction = buf.readInt();
    }

    public PacketNextActionJellyfish(int idJellyfish, int timeForNextAction, int idAction) {
        this.idJellyfish = idJellyfish;
        this.timeForNextAction = timeForNextAction;
        this.idAction = idAction;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.idJellyfish);
        buf.writeInt(this.timeForNextAction);
        buf.writeInt(this.idAction);
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
        Minecraft mc=Minecraft.getInstance();
        assert mc.level!=null;
        Entity dragon=mc.level.getEntity(this.idJellyfish);
        if(dragon instanceof JellyfishEntity jellyfish){
            jellyfish.setActionForID(this.idAction);
            jellyfish.nextTimer=0;
            if(this.idAction!=0){
                jellyfish.maxNextTimer=timeForNextAction;
            }
        }
    }

    @Override
    public void handle(PacketListener p_131342_) {

    }
}
