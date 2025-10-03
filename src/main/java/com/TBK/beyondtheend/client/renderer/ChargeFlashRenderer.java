package com.TBK.beyondtheend.client.renderer;

import com.TBK.beyondtheend.BeyondTheEnd;
import com.TBK.beyondtheend.server.entity.projectile.ChargeFlash;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ChargeFlashRenderer<T extends ChargeFlash> extends EntityRenderer<T> {
    public final ResourceLocation TEXTURE = new ResourceLocation(BeyondTheEnd.MODID,"textures/entity/lightball/spark.png");
    public final ResourceLocation ALPHA = new ResourceLocation(BeyondTheEnd.MODID,"textures/entity/lightball/spark_alpha.png");

    public ChargeFlashRenderer(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
    }

    public void render(T pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        pMatrixStack.pushPose();
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(pPartialTicks, pEntity.yRotO, pEntity.getYRot()) - 90.0F));
        pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(pPartialTicks, pEntity.xRotO, pEntity.getXRot())));

        pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(45.0F));
        pMatrixStack.scale(0.05625F, 0.05625F, 0.05625F);
        pMatrixStack.translate(-4.0D, 0.0D, 0.0D);
        PoseStack.Pose posestack$pose = pMatrixStack.last();
        Matrix4f matrix4f = posestack$pose.pose();
        Matrix3f matrix3f = posestack$pose.normal();
        VertexConsumer vertexconsumer1 = pBuffer.getBuffer(RenderType.entityTranslucentEmissive(this.getTextureLocation(pEntity),false));
        this.renderSpark(matrix4f, matrix3f, pMatrixStack,pPackedLight,vertexconsumer1,1.0F);

        pMatrixStack.popPose();
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }

    private void renderSpark(Matrix4f matrix4f, Matrix3f matrix3f, PoseStack pMatrixStack, int pPackedLight, VertexConsumer vertexconsumer, float v) {
        this.vertex(matrix4f, matrix3f, vertexconsumer, -7, -2, -2, 0.0F, 0.15625F, -1, 0, 0, pPackedLight, (int) (v*255.0F));
        this.vertex(matrix4f, matrix3f, vertexconsumer, -7, -2, 2, 0.15625F, 0.15625F, -1, 0, 0, pPackedLight, (int) (v*255.0F));
        this.vertex(matrix4f, matrix3f, vertexconsumer, -7, 2, 2, 0.15625F, 0.3125F, -1, 0, 0, pPackedLight, (int) (v*255.0F));
        this.vertex(matrix4f, matrix3f, vertexconsumer, -7, 2, -2, 0.0F, 0.3125F, -1, 0, 0, pPackedLight, (int) (v*255.0F));
        this.vertex(matrix4f, matrix3f, vertexconsumer, -7, 2, -2, 0.0F, 0.15625F, 1, 0, 0, pPackedLight, (int) (v*255.0F));
        this.vertex(matrix4f, matrix3f, vertexconsumer, -7, 2, 2, 0.15625F, 0.15625F, 1, 0, 0, pPackedLight, (int) (v*255.0F));
        this.vertex(matrix4f, matrix3f, vertexconsumer, -7, -2, 2, 0.15625F, 0.3125F, 1, 0, 0,pPackedLight , (int) (v*255.0F));
        this.vertex(matrix4f, matrix3f, vertexconsumer, -7, -2, -2, 0.0F, 0.3125F, 1, 0, 0, pPackedLight, (int) (v*255.0F));

        for(int j = 0; j < 1; ++j) {
            this.vertex(matrix4f, matrix3f, vertexconsumer, -8, -2, 0, 0.0F, 0.0F, 0, 1, 0, pPackedLight, (int) (v*255.0F));
            this.vertex(matrix4f, matrix3f, vertexconsumer, 8, -2, 0, 0.5F, 0.0F, 0, 1, 0, pPackedLight, (int) (v*255.0F));
            this.vertex(matrix4f, matrix3f, vertexconsumer, 8, 2, 0, 0.5F, 0.15625F, 0, 1, 0, pPackedLight, (int) (v*255.0F));
            this.vertex(matrix4f, matrix3f, vertexconsumer, -8, 2, 0, 0.0F, 0.15625F, 0, 1, 0, pPackedLight, (int) (v*255.0F));
        }
    }

    public void vertex(Matrix4f pMatrix, Matrix3f pNormals, VertexConsumer pVertexBuilder, int pOffsetX, int pOffsetY, int pOffsetZ, float pTextureX, float pTextureY, int pNormalX, int p_113835_, int p_113836_, int pPackedLight,int alpha) {
        pVertexBuilder.vertex(pMatrix, (float)pOffsetX, (float)pOffsetY, (float)pOffsetZ).color(255, 255, 255, alpha).uv(pTextureX, pTextureY).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(pPackedLight).normal(pNormals, (float)pNormalX, (float)p_113836_, (float)p_113835_).endVertex();
    }
    @Override
    public ResourceLocation getTextureLocation(T p_114482_) {
        return TEXTURE;
    }

}
