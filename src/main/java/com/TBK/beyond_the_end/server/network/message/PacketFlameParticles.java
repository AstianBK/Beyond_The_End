package com.TBK.beyond_the_end.server.network.message;

import com.TBK.beyond_the_end.client.particle.BKParticles;
import com.TBK.beyond_the_end.server.entity.FallenDragonEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.Random;
import java.util.function.Supplier;

public class PacketFlameParticles implements Packet<PacketListener> {
    private final int xInitial;
    private final int yInitial;
    private final int zInitial;
    private final int xEnd;
    private final int yEnd;
    private final int zEnd;

    public PacketFlameParticles(FriendlyByteBuf buf) {
        this.xInitial =buf.readInt();
        this.yInitial =buf.readInt();
        this.zInitial =buf.readInt();

        this.xEnd =buf.readInt();
        this.yEnd =buf.readInt();
        this.zEnd =buf.readInt();
    }

    public PacketFlameParticles(Vec3 initial,Vec3 end) {
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
        Vec3 line=end.subtract(start).normalize().scale(6.5F);
        for (int i=0;i<300;i++){
            Random r=new Random();
            double d9 =  line.x;
            double d10 =  line.y;
            double d11 =  line.z;
            Particle particle= mc.particleEngine.createParticle(BKParticles.FLAME_PARTICLE.get(), start.x, start.y, start.z,
                    d9*0.4D+r.nextDouble(-1.2D,1.2D),
                    d10*0.1D+r.nextDouble(-0.1D,0.1D),
                    d11*0.4D+r.nextDouble(-1.2D,1.2D));
        }
    }

    @Override
    public void handle(PacketListener p_131342_) {

    }
}
