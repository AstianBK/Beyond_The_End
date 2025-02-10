package com.TBK.beyond_the_end.client.renderer;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.client.model.LightballModel;
import com.TBK.beyond_the_end.server.entity.projectile.ChargeFollowing;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class ChargeFollowingRenderer<T extends ChargeFollowing> extends EntityRenderer<T> {
    public final ResourceLocation TEXTURE = new ResourceLocation(BeyondTheEnd.MODID,"textures/entity/lightball/lightball.png");
    public final ResourceLocation[] ADDONS = new ResourceLocation[]{
            new ResourceLocation(BeyondTheEnd.MODID,"textures/entity/lightball/lightning1.png"),
            new ResourceLocation(BeyondTheEnd.MODID,"textures/entity/lightball/lightning2.png"),
            new ResourceLocation(BeyondTheEnd.MODID,"textures/entity/lightball/lightning3.png")
    };
    public final ResourceLocation ALPHA = new ResourceLocation(BeyondTheEnd.MODID,"textures/entity/lightball/lighttrans.png");

    public final LightballModel<T> model;
    public ChargeFollowingRenderer(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
        this.model=new LightballModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(LightballModel.LAYER_LOCATION));
    }

    @Override
    public void render(T pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        pMatrixStack.pushPose();
        float f0 = ((float)(pEntity.tickCount));
        Quaternion rotation = Vector3f.YP.rotation((float)(Math.PI)*f0/100 );
        rotation.mul(Vector3f.ZP.rotation((float)Math.PI));
        pMatrixStack.mulPose(rotation);
        LightballModel<T> layer1=new LightballModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(LightballModel.LAYER_LOCATION));
        LightballModel<T> layer2=new LightballModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(LightballModel.LAYER_LOCATION));

        float porcentaje= pEntity.getAnimTimer(pPartialTicks);
        layer2.renderToBuffer(pMatrixStack,pBuffer.getBuffer(RenderType.dragonExplosionAlpha(ALPHA)),pPackedLight,OverlayTexture.NO_OVERLAY,1.0F,1.0F,1.0F,porcentaje);

        layer1.renderToBuffer(pMatrixStack,pBuffer.getBuffer(RenderType.entityDecal(ADDONS[2]) ),pPackedLight,OverlayTexture.NO_OVERLAY,1.0F,1.0F,1.0F,1.0F);

        this.model.renderToBuffer(pMatrixStack,pBuffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),pPackedLight,OverlayTexture.NO_OVERLAY,1.0F,1.0F,1.0F,1.0F);
        pMatrixStack.popPose();
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(T p_114482_) {
        return TEXTURE;
    }

}
