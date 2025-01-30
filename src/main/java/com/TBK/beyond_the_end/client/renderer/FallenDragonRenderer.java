package com.TBK.beyond_the_end.client.renderer;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.client.model.FallenDragonModel;
import com.TBK.beyond_the_end.server.entity.FallenDragonEntity;
import com.TBK.beyond_the_end.server.entity.phase.RespawnPhase;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.compat.PatchouliCompat;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.util.Color;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.data.EntityModelData;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.util.EModelRenderCycle;

import java.util.Collections;

public class FallenDragonRenderer<T extends FallenDragonEntity> extends GeoEntityRenderer<T> {
    private static final ResourceLocation DRAGON_EXPLODING_LOCATION = new ResourceLocation(BeyondTheEnd.MODID,"textures/entity/degra.png");

    private static final float HALF_SQRT_3 = (float)(Math.sqrt(3.0D) / 2.0D);

    public FallenDragonRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new FallenDragonModel<>());
    }

    @Override
    public void render(T animatable, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        setCurrentModelRenderCycle(EModelRenderCycle.INITIAL);
        poseStack.pushPose();

        Entity leashHolder = animatable.getLeashHolder();

        if (leashHolder != null)
            renderLeash(animatable, partialTick, poseStack, bufferSource, leashHolder);


        this.dispatchedMat = poseStack.last().pose().copy();
        boolean shouldSit = animatable.isPassenger() && (animatable.getVehicle() != null && animatable.getVehicle().shouldRiderSit());
        EntityModelData entityModelData = new EntityModelData();
        entityModelData.isSitting = shouldSit;
        entityModelData.isChild = animatable.isBaby();

        float lerpBodyRot = Mth.rotLerp(partialTick, animatable.yBodyRotO, animatable.yBodyRot);
        float lerpHeadRot = Mth.rotLerp(partialTick, animatable.yHeadRotO, animatable.yHeadRot);
        float netHeadYaw = lerpHeadRot - lerpBodyRot;

        if (shouldSit && animatable.getVehicle() instanceof LivingEntity livingentity) {
            lerpBodyRot = Mth.rotLerp(partialTick, livingentity.yBodyRotO, livingentity.yBodyRot);
            netHeadYaw = lerpHeadRot - lerpBodyRot;
            float clampedHeadYaw = Mth.clamp(Mth.wrapDegrees(netHeadYaw), -85, 85);
            lerpBodyRot = lerpHeadRot - clampedHeadYaw;

            if (clampedHeadYaw * clampedHeadYaw > 2500f)
                lerpBodyRot += clampedHeadYaw * 0.2f;

            netHeadYaw = lerpHeadRot - lerpBodyRot;
        }

        if (animatable.getPose() == Pose.SLEEPING) {
            Direction bedDirection = animatable.getBedOrientation();

            if (bedDirection != null) {
                float eyePosOffset = animatable.getEyeHeight(Pose.STANDING) - 0.1F;

                poseStack.translate(-bedDirection.getStepX() * eyePosOffset, 0, -bedDirection.getStepZ() * eyePosOffset);
            }
        }

        float ageInTicks = animatable.tickCount + partialTick;
        float limbSwingAmount = 0;
        float limbSwing = 0;

        applyRotations(animatable, poseStack, ageInTicks, lerpBodyRot, partialTick);

        if (!shouldSit && animatable.isAlive()) {
            limbSwingAmount = Mth.lerp(partialTick, animatable.animationSpeedOld, animatable.animationSpeed);
            limbSwing = animatable.animationPosition - animatable.animationSpeed * (1 - partialTick);

            if (animatable.isBaby())
                limbSwing *= 3f;

            if (limbSwingAmount > 1f)
                limbSwingAmount = 1f;
        }

        float headPitch = Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot());
        entityModelData.headPitch = -headPitch;
        entityModelData.netHeadYaw = -netHeadYaw;

        AnimationEvent<T> predicate = new AnimationEvent<T>(animatable, limbSwing, limbSwingAmount, partialTick,
                (limbSwingAmount <= -getSwingMotionAnimThreshold() || limbSwingAmount > getSwingMotionAnimThreshold()), Collections.singletonList(entityModelData));
        GeoModel model = this.modelProvider.getModel(this.modelProvider.getModelResource(animatable));

        this.modelProvider.setCustomAnimations(animatable, getInstanceId(animatable), predicate);

        poseStack.translate(0, 0.01f, 0);
        RenderSystem.setShaderTexture(0, getTextureLocation(animatable));

        Color renderColor = getRenderColor(animatable, partialTick, poseStack, bufferSource, null, packedLight);
        RenderType renderType = getRenderType(animatable, partialTick, poseStack, bufferSource, null, packedLight,
                getTextureLocation(animatable));
        if(animatable.isRespawn()){
            float porcent=1.0F-((RespawnPhase)animatable.phaseManager.getCurrentPhase()).porcentTimer();
            render(model,
                    animatable, partialTick, RenderType.dragonExplosionAlpha(DRAGON_EXPLODING_LOCATION), poseStack, bufferSource, bufferSource.getBuffer(RenderType.dragonExplosionAlpha(DRAGON_EXPLODING_LOCATION)),
                    packedLight, LivingEntityRenderer.getOverlayCoords(animatable, 0), 1, 1,1, porcent);
            float f7 = Math.min(porcent > 0.8F ? (porcent - 0.8F) / 0.2F : 0.0F, 1.0F);
            RandomSource randomsource = RandomSource.create(432L);
            VertexConsumer vertexconsumer2 = bufferSource.getBuffer(RenderType.lightning());
            poseStack.pushPose();
            poseStack.translate(0.0D, 3.0D, -2.0D);

            for(int i = 0; (float)i < (porcent + porcent * porcent) / 2.0F * 60.0F; ++i) {
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
            }

            poseStack.popPose();

        }

        if(animatable.dragonDeathTime>0){
            float porcent=(float) animatable.dragonDeathTime/200.0F;
            render(model, animatable, partialTick, RenderType.dragonExplosionAlpha(DRAGON_EXPLODING_LOCATION), poseStack, bufferSource, bufferSource.getBuffer(RenderType.dragonExplosionAlpha(DRAGON_EXPLODING_LOCATION)),
                    packedLight, LivingEntityRenderer.getOverlayCoords(animatable, 0), 1, 1,1, porcent);
            float f7 = Math.min(porcent > 0.8F ? (porcent - 0.8F) / 0.2F : 0.0F, 1.0F);
            RandomSource randomsource = RandomSource.create(432L);
            VertexConsumer vertexconsumer2 = bufferSource.getBuffer(RenderType.lightning());
            poseStack.pushPose();
            poseStack.translate(0.0D, -1.0D, -2.0D);

            for(int i = 0; (float)i < (porcent + porcent * porcent) / 2.0F * 60.0F; ++i) {
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
            }

            poseStack.popPose();

        }


        if (!animatable.isInvisibleTo(Minecraft.getInstance().player)) {
            VertexConsumer glintBuffer = bufferSource.getBuffer(RenderType.entityGlintDirect());
            VertexConsumer translucentBuffer = bufferSource
                    .getBuffer(RenderType.entityTranslucentCull(getTextureLocation(animatable)));

            render(model, animatable, partialTick, renderType, poseStack, bufferSource,
                    glintBuffer != translucentBuffer ? VertexMultiConsumer.create(glintBuffer, translucentBuffer)
                            : null,
                    packedLight, getOverlay(animatable, 0), renderColor.getRed() / 255f,
                    renderColor.getGreen() / 255f, renderColor.getBlue() / 255f,
                    renderColor.getAlpha() / 255f);
        }

        if (!animatable.isSpectator()) {
            for (GeoLayerRenderer<T> layerRenderer : this.layerRenderers) {
                renderLayer(poseStack, bufferSource, packedLight, animatable, limbSwing, limbSwingAmount, partialTick, ageInTicks,
                        netHeadYaw, headPitch, bufferSource, layerRenderer);
            }
        }

        if (ModList.get().isLoaded("patchouli"))
            PatchouliCompat.patchouliLoaded(poseStack);

        poseStack.popPose();

        var renderNameTagEvent = new net.minecraftforge.client.event.RenderNameTagEvent(animatable, animatable.getDisplayName(), this, poseStack,bufferSource,packedLight,partialTick);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(renderNameTagEvent);
        if (renderNameTagEvent.getResult() != net.minecraftforge.eventbus.api.Event.Result.DENY && (renderNameTagEvent.getResult() == net.minecraftforge.eventbus.api.Event.Result.ALLOW || this.shouldShowName(animatable))) {
            this.renderNameTag(animatable, renderNameTagEvent.getContent(),poseStack,bufferSource,packedLight);
        }
    }

    private static void vertex01(VertexConsumer p_114220_, Matrix4f p_114221_, int p_114222_) {
        p_114220_.vertex(p_114221_, 0.0F, 0.0F, 0.0F).color(255, 255, 255, p_114222_).endVertex();
    }

    private static void vertex2(VertexConsumer p_114215_, Matrix4f p_114216_, float p_114217_, float p_114218_) {
        p_114215_.vertex(p_114216_, -HALF_SQRT_3 * p_114218_, p_114217_, -0.5F * p_114218_).color(255, 0, 255, 0).endVertex();
    }

    private static void vertex3(VertexConsumer p_114224_, Matrix4f p_114225_, float p_114226_, float p_114227_) {
        p_114224_.vertex(p_114225_, HALF_SQRT_3 * p_114227_, p_114226_, -0.5F * p_114227_).color(255, 0, 255, 0).endVertex();
    }

    private static void vertex4(VertexConsumer p_114229_, Matrix4f p_114230_, float p_114231_, float p_114232_) {
        p_114229_.vertex(p_114230_, 0.0F, p_114231_, 1.0F * p_114232_).color(255, 0, 255, 0).endVertex();
    }


    @Override
    public RenderType getRenderType(T animatable, float partialTick, PoseStack poseStack, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, int packedLight, ResourceLocation texture) {
        return animatable.isRespawn() || animatable.dragonDeathTime>0 ? RenderType.entityDecal(texture) : super.getRenderType(animatable, partialTick, poseStack, bufferSource, buffer, packedLight, texture);
    }
}
