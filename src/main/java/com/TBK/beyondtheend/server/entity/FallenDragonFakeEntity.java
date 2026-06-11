package com.TBK.beyondtheend.server.entity;

import com.TBK.beyondtheend.BeyondTheEnd;
import com.TBK.beyondtheend.server.network.PacketHandler;
import com.TBK.beyondtheend.server.network.message.PacketNextActionJellyfish;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.ArrayList;

public class FallenDragonFakeEntity extends LivingEntity implements IAnimatable {
    public final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    public static final TicketType<Unit> FAKE = TicketType.create("fake", (p_9460_, p_9461_) -> {
        return 0;
    });
    public int idTarget = -1;

    public FallenDragonFakeEntity(EntityType<? extends LivingEntity> p_21683_, Level p_21684_) {
        super(p_21683_, p_21684_);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    public boolean isPushable() {
        return false;
    }
    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this,"controller",10, state -> {
            state.getController().setAnimationSpeed(1.0D);
            state.getController().setAnimation(new AnimationBuilder().loop("death"));
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }


    @Override
    public void tick() {
        this.noPhysics = true;
        this.setNoGravity(true);
        JellyfishEntity jellyfish;
        if(this.idTarget == -1){
           jellyfish = this.level.getEntitiesOfClass(JellyfishEntity.class,this.getBoundingBox().inflate(500)).stream().findFirst().orElse(null);
           if(jellyfish!=null){
               this.idTarget = jellyfish.getId();
           }
        }else {
            jellyfish = this.level.getEntity(this.idTarget) instanceof JellyfishEntity ? (JellyfishEntity) this.level.getEntity(this.idTarget) :  null;
        }
        if(jellyfish!=null){
            Vec3 vec32 = this.position().subtract(jellyfish.getEyePosition().add(0,-jellyfish.getEyeHeight()/2,0));
            this.setDeltaMovement(vec32.multiply(-1,-1,-1).normalize().scale(0.35D));
            jellyfish.dragonDeath=this.position();
            jellyfish.setTarget(this);
            double f5 = -Math.toDegrees(Math.atan2(vec32.y,Math.sqrt(vec32.x*vec32.x + vec32.z*vec32.z)));
            double f6 = Math.toDegrees(Math.atan2(vec32.z, vec32.x)) - 90.0F;
            this.setYHeadRot((float) f6);
            this.setYRot((float) f6);
            this.setXRot((float) f5);
            this.setRot(this.getYRot(),this.getXRot());
            if(!this.level.getEntities(this,this.getBoundingBox(),e->e==jellyfish).isEmpty()) {
                BeyondTheEnd.LOGGER.debug("Termino");
                jellyfish.shootLaser.stop();
                jellyfish.setActionForID(4);
                jellyfish.positionLastGroundPos=0;
                jellyfish.setTarget(null);
                if(!this.level.isClientSide){
                    PacketHandler.sendToAllTracking(new PacketNextActionJellyfish(jellyfish.getId(),0,4),jellyfish);
                }
                this.discard();
            }
        }
        super.tick();
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @Override
    protected void checkInsideBlocks() {

    }


    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return new ArrayList<>();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot p_21127_) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot p_21036_, ItemStack p_21037_) {

    }



    @Override
    public void checkDespawn() {

    }
}
