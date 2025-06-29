package com.TBK.beyond_the_end.server.network.message;

import com.TBK.beyond_the_end.common.registry.BTESounds;
import com.TBK.beyond_the_end.server.entity.JellyfishEntity;
import com.TBK.beyond_the_end.server.entity.JellyfishMinionEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.Random;
import java.util.function.Supplier;

public class PacketNextActionJellyfish implements Packet<PacketListener> {
    private final int value;
    private final int idJellyfish;
    private final int idAction;
    public PacketNextActionJellyfish(FriendlyByteBuf buf) {
        this.idJellyfish = buf.readInt();
        this.value = buf.readInt();
        this.idAction = buf.readInt();
    }

    public PacketNextActionJellyfish(int idJellyfish, int timeForNextAction, int idAction) {
        this.idJellyfish = idJellyfish;
        this.value = timeForNextAction;
        this.idAction = idAction;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.idJellyfish);
        buf.writeInt(this.value);
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
                jellyfish.positionLastGroundPos = value;
                jellyfish.maxJumpCount = value % 2 == 0 ? 1 :3;
            }else if(this.idAction==5){
                jellyfish.idleTimer = 0;
                jellyfish.level.playLocalSound(jellyfish.getX(),jellyfish.getY(),jellyfish.getZ(),BTESounds.JELLYFISH_LAND.get(), SoundSource.HOSTILE,8.0F,1.0F,false);
                this.particlePoof(mc.level,20,jellyfish.blockPosition());
            }else if(this.idAction==6){
                jellyfish.prepareTimer=10;
                jellyfish.positionNextPosIndex = value;
                jellyfish.jump.start(jellyfish.tickCount);
            }else if(this.idAction==7){
                jellyfish.level.playLocalSound(jellyfish.getX(),jellyfish.getY(),jellyfish.getZ(),BTESounds.JELLYFISH_JUMP.get(), SoundSource.HOSTILE,8.0F,1.0F,false);
                jellyfish.idleTimer = 0;
            }else if(this.idAction==8){
                jellyfish.maxTimerInAir = value;
                jellyfish.timerInAir = 0;
            }else if(this.idAction!=0){
                jellyfish.maxNextTimer = value;
            }

        }else if(dragon instanceof JellyfishMinionEntity minion){
            minion.setActionForID(this.idAction);
            minion.nextTimer=0;
            if(this.idAction!=0){
                minion.maxNextTimer= value;
            }
        }
    }
    public void particlePoof(Level level, int range, BlockPos center){
        ParticleOptions particleoptions = ParticleTypes.POOF;
        int i;
        float f1;
        i = Mth.ceil((float)Math.PI * range * range);
        f1 = range;

        Random random = new Random();
        for(int j = 0; j < i; ++j) {
            float f2 = random.nextFloat() * ((float)Math.PI * 2F);
            float f3 = Mth.sqrt(random.nextFloat()) * f1;
            double d0 = center.getX() + (double)(Mth.cos(f2) * f3);
            double d4 = center.getZ() + (double)(Mth.sin(f2) * f3);
            double d2 = level.getHeight(Heightmap.Types.WORLD_SURFACE, (int) d0, (int) d4);
            double d5;
            double d6;
            double d7;
            if (particleoptions.getType() != ParticleTypes.ENTITY_EFFECT) {
                d5 = (0.5D - random.nextDouble()) * 0.15D;
                d6 = (double)0.01F;
                d7 = (0.5D - random.nextDouble()) * 0.15D;

            } else {
                int k = 16777215;
                d5 = (double)((float)(k >> 16 & 255) / 255.0F);
                d6 = (double)((float)(k >> 8 & 255) / 255.0F);
                d7 = (double)((float)(k & 255) / 255.0F);
            }

            level.addAlwaysVisibleParticle(particleoptions, d0, d2, d4, d5, d6, d7);
        }
    }
    @Override
    public void handle(PacketListener p_131342_) {}
}
