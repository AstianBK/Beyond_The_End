package com.TBK.beyondtheend.client.renderer;

import com.TBK.beyondtheend.BeyondTheEnd;
import com.TBK.beyondtheend.client.layer.JellyfishEmissiveLayer;
import com.TBK.beyondtheend.client.model.JellyfishMinionModel;
import com.TBK.beyondtheend.server.entity.JellyfishMinionEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

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
