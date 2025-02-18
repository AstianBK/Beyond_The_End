package com.TBK.beyond_the_end.client.renderer;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.client.layer.JellyfishEmissiveLayer;
import com.TBK.beyond_the_end.client.model.JellyfishModel;
import com.TBK.beyond_the_end.common.Util;
import com.TBK.beyond_the_end.server.entity.JellyfishEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class JellyfishRenderer<T extends JellyfishEntity,M extends JellyfishModel<T>> extends MobRenderer<T,M> {
    public final ResourceLocation TEXTURE = new ResourceLocation(BeyondTheEnd.MODID,"textures/entity/jellyfish/jellyfish.png");
    private static final ResourceLocation GUARDIAN_BEAM_LOCATION = new ResourceLocation(BeyondTheEnd.MODID,"textures/entity/beacon_beam.png");
    public final ResourceLocation GLOWING = new ResourceLocation(BeyondTheEnd.MODID,"textures/entity/jellyfish/jellyfish_glowing.png");
    public final ResourceLocation EYE = new ResourceLocation(BeyondTheEnd.MODID,"textures/entity/jellyfish/jellyfish_eyes.png");

    public JellyfishRenderer(EntityRendererProvider.Context p_174304_) {
        super(p_174304_, (M) new JellyfishModel<>(p_174304_.bakeLayer(JellyfishModel.LAYER_LOCATION)), 0.0F);

        this.addLayer(new JellyfishEmissiveLayer<>(this,EYE,(entity,f1,f2)->{
            return 1.0F;
        },JellyfishModel::getEye));
        this.addLayer(new JellyfishEmissiveLayer<>(this,GLOWING,(entity,f1,f2)->{
            return entity.actuallyPhase==JellyfishEntity.PhaseAttack.LAZER ? entity.getLazerAnim(f1) : entity.getHeartAnimation(f1);
        },JellyfishModel::getBody));


    }

    @Override
    public boolean shouldRender(T livingEntityIn, Frustum camera, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    protected void scale(T p_115314_, PoseStack p_115315_, float p_115316_) {
        super.scale(p_115314_, p_115315_, p_115316_);
        p_115315_.scale(10.0F,10.0F,10.0F);
    }

    @Override
    public void render(T p_115455_, float p_115456_, float p_115457_, PoseStack p_115458_, MultiBufferSource p_115459_, int p_115460_) {
        super.render(p_115455_, p_115456_, p_115457_, p_115458_, p_115459_, p_115460_);
        if(p_115455_.lazerTimer>0){
            render(p_115458_,p_115459_,p_115455_,p_115456_);
        }
    }

    @Override
    public Vec3 getRenderOffset(T p_114483_, float p_114484_) {
        return super.getRenderOffset(p_114483_, p_114484_);
    }

    @Override
    protected void setupRotations(T jellyfish, PoseStack p_115318_, float p_115319_, float p_115320_, float p_115321_) {
        super.setupRotations(jellyfish, p_115318_, p_115319_, p_115320_, p_115321_);
    }

    public static void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, JellyfishEntity jellyfish, float pPartialTicks) {

        pMatrixStack.pushPose();
        var hit = Util.internalRaycastForEntity(jellyfish.level, jellyfish,jellyfish.getEyePosition(),jellyfish.directionBlock,true,2F);

        Vec3 vec32 = hit.getLocation().subtract(jellyfish.getEyePosition());
        double f5 = Math.atan2(vec32.y,Math.sqrt(vec32.x*vec32.x + vec32.z*vec32.z));
        double f6 = Math.atan2(vec32.z, vec32.x) ;
        float distance = (float) jellyfish.getEyePosition(pPartialTicks).distanceTo(hit.getLocation()) * 0.1F + 1.0F;
        pMatrixStack.translate(0.0F, jellyfish.getEyeHeight() - 1.75F,0.0F);

        pMatrixStack.mulPose(Vector3f.YP.rotation((float) -(f6-1.57F)));

        pMatrixStack.mulPose(Vector3f.XP.rotation((float) -(f5-1.57F)));


        for (int i1 = 1; i1 <= distance; i1++) {
            float[] f1={0.3F,0.1F,0.99F};
            pMatrixStack.pushPose();
            pMatrixStack.scale(10F,10F,distance>1.0F ? 10.0F : 10.0F*distance );
            renderBeaconBeam(pMatrixStack,pBuffer, GUARDIAN_BEAM_LOCATION, pPartialTicks, 1.0F, jellyfish.level.getGameTime(), i1, (int) distance,f1, 0.2F, 0.25F);
            pMatrixStack.popPose();
        }

        pMatrixStack.popPose();

    }

    public static void renderBeaconBeam(PoseStack p_112185_, MultiBufferSource p_112186_, ResourceLocation p_112187_, float p_112188_, float p_112189_, long p_112190_, int p_112191_, int p_112192_, float[] p_112193_, float p_112194_, float p_112195_) {
        int i = p_112191_ + p_112192_;
        float f = (float)Math.floorMod(p_112190_, 40) + p_112188_;
        float f1 = p_112192_ < 0 ? f : -f;
        float f2 = Mth.frac(f1 * 0.2F - (float)Mth.floor(f1 * 0.1F));
        float f3 = p_112193_[0];
        float f4 = p_112193_[1];
        float f5 = p_112193_[2];
        float f6 = 0.0F;
        float f8 = 0.0F;
        float f9 = -p_112194_;
        float f12 = -p_112194_;
        float f15 = -1.0F + f2;
        float f16 = (float)p_112192_ * p_112189_ * (0.5F / p_112194_) + f15;
        p_112185_.pushPose();
        p_112185_.mulPose(Vector3f.YP.rotationDegrees(f * 2.25F - 45.0F));
        renderPart(p_112185_, p_112186_.getBuffer(RenderType.eyes(p_112187_)), 1.0F, 1.0F, 1.0F, 0.1F, p_112191_, i, 0.0F, p_112194_, p_112194_, 0.0F, f9, 0.0F, 0.0F, f12, 0.0F, 1.0F, f16, f15);
        p_112185_.popPose();
        f6 = -p_112195_;
        float f7 = -p_112195_;
        f8 = -p_112195_;
        f9 = -p_112195_;
        f15 = -1.0F + f2;
        f16 = (float)p_112192_ * p_112189_ + f15;
        renderPart(p_112185_, p_112186_.getBuffer(RenderType.eyes(p_112187_)), f3, f4, f5, 1.0F, p_112191_, i, f6, f7, p_112195_, f8, f9, p_112195_, p_112195_, p_112195_, 0.0F, 1.0F, f16, f15);
    }

    private static void renderPart(PoseStack p_112156_, VertexConsumer p_112157_, float p_112158_, float p_112159_, float p_112160_, float p_112161_, int p_112162_, int p_112163_, float p_112164_, float p_112165_, float p_112166_, float p_112167_, float p_112168_, float p_112169_, float p_112170_, float p_112171_, float p_112172_, float p_112173_, float p_112174_, float p_112175_) {
        PoseStack.Pose posestack$pose = p_112156_.last();
        Matrix4f matrix4f = posestack$pose.pose();
        Matrix3f matrix3f = posestack$pose.normal();
        renderQuad(matrix4f, matrix3f, p_112157_, p_112158_, p_112159_, p_112160_, p_112161_, p_112162_, p_112163_, p_112164_, p_112165_, p_112166_, p_112167_, p_112172_, p_112173_, p_112174_, p_112175_);
        renderQuad(matrix4f, matrix3f, p_112157_, p_112158_, p_112159_, p_112160_, p_112161_, p_112162_, p_112163_, p_112170_, p_112171_, p_112168_, p_112169_, p_112172_, p_112173_, p_112174_, p_112175_);
        renderQuad(matrix4f, matrix3f, p_112157_, p_112158_, p_112159_, p_112160_, p_112161_, p_112162_, p_112163_, p_112166_, p_112167_, p_112170_, p_112171_, p_112172_, p_112173_, p_112174_, p_112175_);
        renderQuad(matrix4f, matrix3f, p_112157_, p_112158_, p_112159_, p_112160_, p_112161_, p_112162_, p_112163_, p_112168_, p_112169_, p_112164_, p_112165_, p_112172_, p_112173_, p_112174_, p_112175_);
    }

    private static void renderQuad(Matrix4f p_253960_, Matrix3f p_254005_, VertexConsumer p_112122_, float p_112123_, float p_112124_, float p_112125_, float p_112126_, int p_112127_, int p_112128_, float p_112129_, float p_112130_, float p_112131_, float p_112132_, float p_112133_, float p_112134_, float p_112135_, float p_112136_) {
        addVertex(p_253960_, p_254005_, p_112122_, p_112123_, p_112124_, p_112125_, p_112126_, p_112128_, p_112129_, p_112130_, p_112134_, p_112135_);
        addVertex(p_253960_, p_254005_, p_112122_, p_112123_, p_112124_, p_112125_, p_112126_, p_112127_, p_112129_, p_112130_, p_112134_, p_112136_);
        addVertex(p_253960_, p_254005_, p_112122_, p_112123_, p_112124_, p_112125_, p_112126_, p_112127_, p_112131_, p_112132_, p_112133_, p_112136_);
        addVertex(p_253960_, p_254005_, p_112122_, p_112123_, p_112124_, p_112125_, p_112126_, p_112128_, p_112131_, p_112132_, p_112133_, p_112135_);
    }

    private static void addVertex(Matrix4f p_253955_, Matrix3f p_253713_, VertexConsumer p_253894_, float p_253871_, float p_253841_, float p_254568_, float p_254361_, int p_254357_, float p_254451_, float p_254240_, float p_254117_, float p_253698_) {
        p_253894_.vertex(p_253955_, p_254451_, (float)p_254357_, p_254240_).color(p_253871_, p_253841_, p_254568_, p_254361_).uv(p_254117_, p_253698_).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(p_253713_, 0.0F, 1.0F, 0.0F).endVertex();
    }


    @Override
    public ResourceLocation getTextureLocation(T p_114482_) {
        return TEXTURE;
    }
}
