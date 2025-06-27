package com.TBK.beyond_the_end.server.network.message;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.common.registry.BTESounds;
import com.TBK.beyond_the_end.server.entity.JellyfishEntity;
import com.TBK.beyond_the_end.server.entity.JellyfishMinionEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundSource;
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
            if(this.idAction==4){
                jellyfish.shootLaser.stop();
                jellyfish.level.playSound(null,jellyfish,BTESounds.JELLYFISH_LAND.get(), SoundSource.HOSTILE,4.0F,1.0F);
                jellyfish.positionLastGroundPos = timeForNextAction;
                jellyfish.maxJumpCount = timeForNextAction % 2 == 0 ? 1 :3;
            }else if(this.idAction==5){
                jellyfish.idleTimer = 0;
            }else if(this.idAction==6){
                jellyfish.prepareTimer=10;
                jellyfish.positionNextPosIndex = timeForNextAction;
                jellyfish.jump.start(jellyfish.tickCount);
            }else if(this.idAction==7){
                jellyfish.level.playSound(null,jellyfish,BTESounds.JELLYFISH_JUMP.get(), SoundSource.HOSTILE,4.0F,1.0F);
                jellyfish.idleTimer = 0;
            }else if(this.idAction==8){
                jellyfish.maxTimerInAir = timeForNextAction;
                jellyfish.timerInAir = 0;
            }else if(this.idAction!=0){
                jellyfish.maxNextTimer = timeForNextAction;
            }

        }else if(dragon instanceof JellyfishMinionEntity minion){
            minion.setActionForID(this.idAction);
            minion.nextTimer=0;
            if(this.idAction!=0){
                minion.maxNextTimer=timeForNextAction;
            }
        }
    }

    @Override
    public void handle(PacketListener p_131342_) {}
}
