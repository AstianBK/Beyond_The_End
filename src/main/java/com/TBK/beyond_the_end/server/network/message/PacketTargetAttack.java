package com.TBK.beyond_the_end.server.network.message;

import com.TBK.beyond_the_end.server.entity.FallenDragonEntity;
import com.TBK.beyond_the_end.server.entity.JellyfishEntity;
import com.TBK.beyond_the_end.server.entity.phase.FallenDragonPhase;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketTargetAttack implements Packet<PacketListener> {
    private final int x;
    private final int y;
    private final int z;

    private final int idDragon;
    public PacketTargetAttack(FriendlyByteBuf buf) {
        this.x=buf.readInt();
        this.y=buf.readInt();
        this.z=buf.readInt();
        this.idDragon = buf.readInt();
    }

    public PacketTargetAttack(int idDragon, int x,int y,int z) {
        this.x=x;
        this.y=y;
        this.z=z;
        this.idDragon = idDragon;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        buf.writeInt(this.idDragon);
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
        Entity dragon=mc.level.getEntity(this.idDragon);
        if(dragon instanceof FallenDragonEntity fallenDragon){
            fallenDragon.targetPos=new BlockPos(x,y,z);
        }else if(dragon instanceof JellyfishEntity jellyfish){
            jellyfish.directionBlock=new Vec3(x,y,z);
        }
    }

    @Override
    public void handle(PacketListener p_131342_) {

    }
}
