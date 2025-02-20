package com.TBK.beyond_the_end.server.network.message;

import com.TBK.beyond_the_end.client.particle.BKParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.Random;
import java.util.function.Supplier;

public class PacketLineParticles implements Packet<PacketListener> {
    private final int xInitial;
    private final int yInitial;
    private final int zInitial;
    private final int xEnd;
    private final int yEnd;
    private final int zEnd;

    public PacketLineParticles(FriendlyByteBuf buf) {
        this.xInitial =buf.readInt();
        this.yInitial =buf.readInt();
        this.zInitial =buf.readInt();

        this.xEnd =buf.readInt();
        this.yEnd =buf.readInt();
        this.zEnd =buf.readInt();
    }

    public PacketLineParticles(Vec3 initial, Vec3 end) {
        this.xInitial = (int) initial.x;
        this.yInitial = (int) initial.y;
        this.zInitial = (int) initial.z;

        this.xEnd = (int) end.x;
        this.yEnd = (int) end.y;
        this.zEnd = (int) end.z;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.xInitial);
        buf.writeInt(this.yInitial);
        buf.writeInt(this.zInitial);

        buf.writeInt(this.xEnd);
        buf.writeInt(this.yEnd);
        buf.writeInt(this.zEnd);

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
        Vec3 end=new Vec3(this.xEnd,this.yEnd,this.zEnd);
        Vec3 start=new Vec3(this.xInitial,this.yInitial,this.zInitial);
        Vec3 line=end.subtract(start).normalize();
        for (int i=0;i<300;i++){
            mc.particleEngine.createParticle(BKParticles.FLAME_PARTICLE.get(), start.x, start.y, start.z,
                    0.0F,
                    0.0F,
                    0.0F);
            start=start.add(line);
        }
    }

    @Override
    public void handle(PacketListener p_131342_) {

    }
}
