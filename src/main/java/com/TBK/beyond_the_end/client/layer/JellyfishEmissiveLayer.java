package com.TBK.beyond_the_end.client.layer;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.client.model.JellyfishModel;
import com.TBK.beyond_the_end.server.entity.JellyfishEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.WardenModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class JellyfishEmissiveLayer<T extends Mob, M extends HierarchicalModel<T>> extends RenderLayer<T, M> {
    private final ResourceLocation texture;
    private final AlphaFunction<T> alphaFunction;
    private final DrawSelector<T, M> drawSelector;

    public JellyfishEmissiveLayer(RenderLayerParent<T, M> p_234885_, ResourceLocation p_234886_,AlphaFunction<T> p_234887_, DrawSelector<T, M> p_234888_) {
        super(p_234885_);
        this.texture = p_234886_;
        this.alphaFunction = p_234887_;
        this.drawSelector = p_234888_;
    }

    public void render(PoseStack p_234902_, MultiBufferSource p_234903_, int p_234904_, T p_234905_, float p_234906_, float p_234907_, float p_234908_, float p_234909_, float p_234910_, float p_234911_) {
        if (!p_234905_.isInvisible()) {
            this.onlyDrawSelectedParts();
            VertexConsumer vertexconsumer = p_234903_.getBuffer(RenderType.entityTranslucentEmissive(this.texture));
            this.getParentModel().renderToBuffer(p_234902_, vertexconsumer, p_234904_, LivingEntityRenderer.getOverlayCoords(p_234905_, 0.0F), 1.0F, 1.0F, 1.0F,this.alphaFunction.apply(p_234905_, p_234908_, p_234909_));
            this.resetDrawForAllParts();
        }
    }

    private void onlyDrawSelectedParts() {
        List<ModelPart> list = this.drawSelector.getPartsToDraw(this.getParentModel());
        this.getParentModel().root().getAllParts().forEach((p_234918_) -> {
            p_234918_.skipDraw = true;
        });
        list.forEach(this::skipDraw);
    }

    public void skipDraw(ModelPart part){
        part.skipDraw = false;
        for (ModelPart part1 : part.getAllParts().toList()){
            if(part1!=part){
                this.skipDraw(part1);
            }
        }
    }

    private void resetDrawForAllParts() {
        this.getParentModel().root().getAllParts().forEach((p_234913_) -> {
            p_234913_.skipDraw = false;
        });
    }

    @OnlyIn(Dist.CLIENT)
    public interface AlphaFunction<T extends Mob> {
        float apply(T p_234920_, float p_234921_, float p_234922_);
    }

    @OnlyIn(Dist.CLIENT)
    public interface DrawSelector<T extends Mob, M extends EntityModel<T>> {
        List<ModelPart> getPartsToDraw(M p_234924_);
    }
}
