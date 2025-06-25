package com.TBK.beyond_the_end.client.layer;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.client.model.JellyfishModel;
import com.TBK.beyond_the_end.server.entity.JellyfishEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class RespawnLayer<T extends JellyfishEntity,M extends JellyfishModel<T>> extends RenderLayer<T,M> {
    public final ResourceLocation DECA = new ResourceLocation(BeyondTheEnd.MODID,"textures/entity/jellyfish/jellyfish_trans.png");
    public static final ResourceLocation TEXTURE = new ResourceLocation(BeyondTheEnd.MODID,"textures/entity/jellyfish/jellyfish.png");

    private static final RenderType DECAL = RenderType.entityDecal(TEXTURE);

    public RespawnLayer(RenderLayerParent<T, M> p_117346_) {
        super(p_117346_);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int p_117351_, T animatable, float p_117353_, float p_117354_, float p_117355_, float p_117356_, float p_117357_, float p_117358_) {
        if(animatable.actuallyPhase == JellyfishEntity.PhaseAttack.SPAWN){
            poseStack.pushPose();
            float porcent=1.0F-((float) animatable.timerSpawn /200.0F);

            this.getParentModel().renderToBuffer(poseStack,bufferSource.getBuffer(RenderType.dragonExplosionAlpha(DECA)),p_117351_, OverlayTexture.NO_OVERLAY,1.0F,1.0F,1.0F,porcent);
            VertexConsumer vertexconsumer1 = bufferSource.getBuffer(DECAL);
            this.getParentModel().renderToBuffer(poseStack, vertexconsumer1, p_117351_, OverlayTexture.pack(0.0F, false), 1.0F, 1.0F, 1.0F, 1.0F);

            poseStack.popPose();

            /*for(int i = 0; (float)i < (porcent + porcent * porcent) / 2.0F * 60.0F; ++i) {
                poseStack.mulPose(Vector3f.XP.rotationDegrees(randomsource.nextFloat() * 360.0F));
                poseStack.mulPose(Vector3f.YP.rotationDegrees(randomsource.nextFloat() * 360.0F));
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(randomsource.nextFloat() * 360.0F));
                poseStack.mulPose(Vector3f.XP.rotationDegrees(randomsource.nextFloat() * 360.0F));
                poseStack.mulPose(Vector3f.YP.rotationDegrees(randomsource.nextFloat() * 360.0F));
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(randomsource.nextFloat() * 360.0F + porcent * 90.0F));
                float f3 = randomsource.nextFloat() * 20.0F + 5.0F + f7 * 10.0F;
                float f4 = randomsource.nextFloat() * 2.0F + 1.0F + f7 * 2.0F;
                Matrix4f matrix4f = poseStack.last().pose();
                int j = (int)(255.0F * (1.0F - f7));
                vertex01(vertexconsumer2, matrix4f, j);
                vertex2(vertexconsumer2, matrix4f, f3, f4);
                vertex3(vertexconsumer2, matrix4f, f3, f4);
                vertex01(vertexconsumer2, matrix4f, j);
                vertex3(vertexconsumer2, matrix4f, f3, f4);
                vertex4(vertexconsumer2, matrix4f, f3, f4);
                vertex01(vertexconsumer2, matrix4f, j);
                vertex4(vertexconsumer2, matrix4f, f3, f4);
                vertex2(vertexconsumer2, matrix4f, f3, f4);
            }*/
        }
    }
}
