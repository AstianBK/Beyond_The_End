package com.TBK.beyond_the_end.client.renderer;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.client.layer.JellyfishEmissiveLayer;
import com.TBK.beyond_the_end.client.model.JellyfishMinionModel;
import com.TBK.beyond_the_end.client.model.LightballModel;
import com.TBK.beyond_the_end.server.entity.projectile.ChargeFollowing;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

public class ChargeFollowingRenderer<T extends ChargeFollowing,M extends LightballModel<T>> extends EntityRenderer<T> {
    public final ResourceLocation TEXTURE = new ResourceLocation(BeyondTheEnd.MODID,"textures/entity/lightball/lightball.png");
    public final ResourceLocation RAY = new ResourceLocation(BeyondTheEnd.MODID,"textures/entity/lightball/lightning3.png");
    public final ResourceLocation ALPHA = new ResourceLocation(BeyondTheEnd.MODID,"textures/entity/lightball/lighttrans.png");

    public final LightballModel<T> model;
    public final LightballModel<T> layer1;
    public final LightballModel<T> layer2;
    private DrawSelector<T, M> drawSelector;

    public ChargeFollowingRenderer(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
        layer1=new LightballModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(LightballModel.LAYER_LOCATION));
        layer2=new LightballModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(LightballModel.LAYER_LOCATION));

        this.model=new LightballModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(LightballModel.LAYER_LOCATION));
    }

    @Override
    public void render(T pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        pMatrixStack.pushPose();
        float f0 = ((float)(pEntity.tickCount));
        pMatrixStack.translate(0.0f,1.5F,0.0F);

        Quaternion rotation = Vector3f.YP.rotation((float)(Math.PI)*f0/100 );

        rotation.mul(Vector3f.ZP.rotation((float)Math.PI));

        pMatrixStack.mulPose(rotation);


        float porcentaje= pEntity.getAnimTimer(pPartialTicks);

        layer2.renderToBuffer(pMatrixStack,pBuffer.getBuffer(RenderType.dragonExplosionAlpha(ALPHA)),pPackedLight,OverlayTexture.NO_OVERLAY,1.0F,1.0F,1.0F,porcentaje);

        layer1.renderToBuffer(pMatrixStack,pBuffer.getBuffer(RenderType.entityDecal(RAY)),pPackedLight,OverlayTexture.NO_OVERLAY,1.0F,1.0F,1.0F,1.0F);

        this.model.renderToBuffer(pMatrixStack,pBuffer.getBuffer(RenderType.eyes(TEXTURE)),pPackedLight,OverlayTexture.NO_OVERLAY,1.0F,1.0F,1.0F,1.0F);

        this.model.renderToBuffer(pMatrixStack,pBuffer.getBuffer(RenderType.entityTranslucent(TEXTURE)),pPackedLight,OverlayTexture.NO_OVERLAY,1.0F,1.0F,1.0F,1.0F);

        render(pMatrixStack,pBuffer,pPackedLight,pEntity,pPartialTicks,pEntity.tickCount+pPartialTicks);

        pMatrixStack.popPose();
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }

    public void render(PoseStack p_234902_, MultiBufferSource p_234903_, int p_234904_, T p_234905_, float p_234908_, float p_234909_) {
        if(drawSelector==null){
            drawSelector= p_234924_ -> List.of(model.root());
        }
        if (!p_234905_.isInvisible()) {
            this.onlyDrawSelectedParts();
            VertexConsumer vertexconsumer = p_234903_.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
            this.model.renderToBuffer(p_234902_, vertexconsumer, p_234904_, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F,1.0F);
            this.resetDrawForAllParts();
        }
    }

    private void onlyDrawSelectedParts() {
        List<ModelPart> list = this.drawSelector.getPartsToDraw((M) this.model);
        this.model.root().getAllParts().forEach((p_234918_) -> {
            p_234918_.skipDraw = true;
        });
        list.forEach(this::skipDraw);
    }

    public void skipDraw(ModelPart part){
        part.skipDraw = false;
        for (ModelPart part1 : part.getAllParts().toList()){
            if(part1!=part){
                this.skipDraw(part1);
            }
        }
    }

    private void resetDrawForAllParts() {
        this.model.root().getAllParts().forEach((p_234913_) -> {
            p_234913_.skipDraw = false;
        });
    }

    @OnlyIn(Dist.CLIENT)
    public interface DrawSelector<T extends Entity, M extends EntityModel<T>> {
        List<ModelPart> getPartsToDraw(M p_234924_);
    }


    @Override
    public ResourceLocation getTextureLocation(T p_114482_) {
        return TEXTURE;
    }


}
