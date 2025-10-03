package com.TBK.beyondtheend.client.renderer;

import com.TBK.beyondtheend.BeyondTheEnd;
import com.TBK.beyondtheend.client.layer.DragonGlowingLayer;
import com.TBK.beyondtheend.client.model.FallenDragonFakeModel;
import com.TBK.beyondtheend.server.entity.FallenDragonFakeEntity;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class FallenDragonFakeRenderer<T extends FallenDragonFakeEntity> extends GeoEntityRenderer<T> {
    private static final ResourceLocation DRAGON_EXPLODING_LOCATION = new ResourceLocation(BeyondTheEnd.MODID,"textures/entity/degra.png");
    private static final float HALF_SQRT_3 = (float)(Math.sqrt(3.0D) / 2.0D);

    public FallenDragonFakeRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new FallenDragonFakeModel<>());
        this.addLayer(new DragonGlowingLayer<>(this));
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


}
