package com.TBK.beyond_the_end.server.network.message;

import com.TBK.beyond_the_end.server.entity.FallenDragonEntity;
import com.TBK.beyond_the_end.server.entity.phase.FallenDragonPhase;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketTargetDragon implements Packet<PacketListener> {
    private final int idTarget;
    private final int idDragon;
    private final int idAction;
    public PacketTargetDragon(FriendlyByteBuf buf) {
        this.idDragon = buf.readInt();
        this.idTarget = buf.readInt();
        this.idAction = buf.readInt();
    }

    public PacketTargetDragon(int idDragon,int idTarget,int idAction) {
        this.idDragon = idDragon;
        this.idTarget = idTarget;
        this.idAction = idAction;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.idDragon);
        buf.writeInt(this.idTarget);
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
        Entity entity=mc.level.getEntity(this.idTarget);
        Entity dragon=mc.level.getEntity(this.idDragon);
        if(this.idAction==0){
            if(dragon instanceof FallenDragonEntity fallenDragon){
                fallenDragon.phaseManager.setPhase(FallenDragonPhase.SKYFALL);
                fallenDragon.phaseManager.getPhase(FallenDragonPhase.SKYFALL).setTarget((LivingEntity) entity);
            }
        }else {
            if(dragon instanceof FallenDragonEntity fallenDragon){
                fallenDragon.phaseManager.setPhase(FallenDragonPhase.CHARGING);
                fallenDragon.phaseManager.getPhase(FallenDragonPhase.CHARGING).setTarget((LivingEntity) entity);
            }
        }
    }

    @Override
    public void handle(PacketListener p_131342_) {

    }
}
