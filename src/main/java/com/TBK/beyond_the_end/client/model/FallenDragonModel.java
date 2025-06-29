package com.TBK.beyond_the_end.client.model;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.server.entity.FallenDragonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.snapshot.BoneSnapshot;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

public class FallenDragonModel<T extends FallenDragonEntity> extends AnimatedGeoModel<T> {
    @Override
    public ResourceLocation getModelResource(T object) {
        return new ResourceLocation(BeyondTheEnd.MODID,"geo/fallen_dragon/fallen_dragon.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(T object) {
        return new ResourceLocation(BeyondTheEnd.MODID,"textures/entity/fallen_dragon.png");
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return new ResourceLocation(BeyondTheEnd.MODID,"animations/fallen_dragon.animation.json");
    }



    @Override
    public void setCustomAnimations(T entity, int instanceId, AnimationEvent animationEvent) {

        GeoBone main= (GeoBone) this.getBone("fallen_dragon");
        if(main!=null){
            this.resetMain(main);
        }
        super.setCustomAnimations(entity, instanceId, animationEvent);
        EntityModelData data= (EntityModelData) animationEvent.getExtraDataOfType(EntityModelData.class).get(0);
        if(main!=null){
            if(entity.canFly()){
                float rotY=135;
                if (entity.targetPos!=null){
                    BlockPos pos=entity.targetPos.subtract(new BlockPos(entity.position().x,entity.position().y,entity.position().z));
                    Vec3 sub=new Vec3(pos.getX(),pos.getY(),pos.getZ());
                    double d = sub.horizontalDistance();
                    float rotX = (float) (-(Mth.atan2(sub.y, d) * (double) Mth.RAD_TO_DEG)) - 90.0F;
                    main.setRotationX((float) Math.toRadians(rotX));
                    rotY=data.netHeadYaw * ((float) Math.PI / 180F);
                }
                main.setRotationY(rotY);
                main.setPositionZ(30);
            }
        }
    }

    public void resetMain(GeoBone main){
        for(GeoBone child:main.childBones){
            BoneSnapshot initial=child.getInitialSnapshot();
            child.setRotation(initial.rotationValueX, initial.rotationValueY, initial.rotationValueZ);
            child.setPosition(initial.positionOffsetX, initial.positionOffsetY, initial.positionOffsetZ);
            if(!child.childBones.isEmpty()){
                resetMain(child);
            }
        }
    }
}
