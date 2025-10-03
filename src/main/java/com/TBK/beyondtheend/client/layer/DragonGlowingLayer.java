package com.TBK.beyondtheend.client.layer;

import com.TBK.beyondtheend.BeyondTheEnd;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class DragonGlowingLayer<T extends Entity & IAnimatable> extends GeoLayerRenderer<T> {
    public final ResourceLocation GLOWING = new ResourceLocation(BeyondTheEnd.MODID,"textures/entity/fallen_dragon_glowing.png");
    public DragonGlowingLayer(IGeoRenderer<T> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, T entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        renderCopyModel(this.getEntityModel(),GLOWING,matrixStackIn,bufferIn,packedLightIn,entityLivingBaseIn,partialTicks,1.0F,1.0F,1.0F);
    }

    @Override
    public RenderType getRenderType(ResourceLocation textureLocation) {
        return RenderType.entityTranslucentEmissive(textureLocation);
    }
}
