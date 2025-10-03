package com.TBK.beyondtheend.server.network.message;

import com.TBK.beyondtheend.client.sounds.LaserSoundInstance;
import com.TBK.beyondtheend.server.entity.JellyfishEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketPlaySound {
    private final int jellyId;

    public PacketPlaySound(int jellyId) {
        this.jellyId = jellyId;

    }

    public PacketPlaySound(FriendlyByteBuf buf) {
        this.jellyId =buf.readInt();
    }



    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.jellyId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(this::sound);
        return true;
    }
    @OnlyIn(Dist.CLIENT)
    public void sound(){
        Minecraft minecraft =Minecraft.getInstance();
        assert minecraft.level!=null;
        Player player=minecraft.player;
        Entity entity=minecraft.level.getEntity(this.jellyId);
        if(entity instanceof JellyfishEntity){
            EntityBoundSoundInstance instance=new LaserSoundInstance(player,((JellyfishEntity) entity));
            Minecraft.getInstance().getSoundManager().play(instance);
        }

    }
}
