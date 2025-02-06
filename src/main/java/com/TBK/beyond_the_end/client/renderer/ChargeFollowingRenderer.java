package com.TBK.beyond_the_end.client.renderer;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.client.model.LightballModel;
import com.TBK.beyond_the_end.server.entity.projectile.ChargeFollowing;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
    public final LightballModel<T> model;
    public ChargeFollowingRenderer(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
        this.model=new LightballModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(LightballModel.LAYER_LOCATION));
    }

    @Override
    public void render(T pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        this.model.renderToBuffer(pMatrixStack,pBuffer.getBuffer(RenderType.entityTranslucentEmissive(this.getTextureLocation(pEntity))),pPackedLight,OverlayTexture.NO_OVERLAY,1.0F,1.0F,1.0F,1.0F);
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(T p_114482_) {
        return TEXTURE;
    }

}
