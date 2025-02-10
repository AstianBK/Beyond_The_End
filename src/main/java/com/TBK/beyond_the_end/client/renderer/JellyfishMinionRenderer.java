package com.TBK.beyond_the_end.client.renderer;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.client.animacion.JellyfishMinionAnim;
import com.TBK.beyond_the_end.client.layer.JellyfishEmissiveLayer;
import com.TBK.beyond_the_end.client.model.JellyfishMinionModel;
import com.TBK.beyond_the_end.client.model.JellyfishModel;
import com.TBK.beyond_the_end.common.Util;
import com.TBK.beyond_the_end.server.entity.JellyfishEntity;
import com.TBK.beyond_the_end.server.entity.JellyfishMinionEntity;
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

public class JellyfishMinionRenderer<T extends JellyfishMinionEntity,M extends JellyfishMinionModel<T>> extends MobRenderer<T,M> {
    public final ResourceLocation TEXTURE = new ResourceLocation(BeyondTheEnd.MODID,"textures/entity/jellyfish/jellyfish_minion.png");
    public final ResourceLocation GLOWING = new ResourceLocation(BeyondTheEnd.MODID,"textures/entity/jellyfish/jellyfish_minion_glowing.png");

    public JellyfishMinionRenderer(EntityRendererProvider.Context p_174304_) {
        super(p_174304_, (M) new JellyfishMinionModel<>(p_174304_.bakeLayer(JellyfishMinionModel.LAYER_LOCATION)), 0.0F);
        this.addLayer(new JellyfishEmissiveLayer<>(this,GLOWING,(entity, f1, f2)->{
            return 1.0F;
        },JellyfishMinionModel::getEye));

    }


    @Override
    public void render(T p_115455_, float p_115456_, float p_115457_, PoseStack p_115458_, MultiBufferSource p_115459_, int p_115460_) {
        super.render(p_115455_, p_115456_, p_115457_, p_115458_, p_115459_, p_115460_);
    }

    @Override
    public ResourceLocation getTextureLocation(T p_114482_) {
        return TEXTURE;
    }
}
