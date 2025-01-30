package com.TBK.beyond_the_end.client.layer;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.client.model.LightballModel;
import com.TBK.beyond_the_end.server.capabilities.PortalPlayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ChargeLayer<T extends Player,M extends PlayerModel<T>> extends RenderLayer<T,M> {
    public final ItemRenderer renderer;
    public final ResourceLocation TEXTURE = new ResourceLocation(BeyondTheEnd.MODID,"textures/entity/lightball/lightball.png");
    public final ResourceLocation[] ADDONS = new ResourceLocation[]{
            new ResourceLocation(BeyondTheEnd.MODID,"textures/entity/lightball/lightning1.png"),
            new ResourceLocation(BeyondTheEnd.MODID,"textures/entity/lightball/lightning2.png"),
            new ResourceLocation(BeyondTheEnd.MODID,"textures/entity/lightball/lightning3.png")
    };
    public ChargeLayer(RenderLayerParent<T, M> p_117346_) {
        super(p_117346_);
        this.renderer= Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(PoseStack p_117349_, MultiBufferSource p_117350_, int p_117351_, T p_117352_, float p_117353_, float p_117354_, float p_117355_, float p_117356_, float p_117357_, float p_117358_) {

        PortalPlayer.get(p_117352_).ifPresent(e->{
            if(e.getCharge()>0){
                for(int i=0;i<e.getCharge();i++){
                    LightballModel<T> model = new LightballModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(LightballModel.LAYER_LOCATION));
                    LightballModel<T> model1 = new LightballModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(LightballModel.LAYER_LOCATION));

                    p_117349_.pushPose();
                    float f0 = ((float)(p_117352_.tickCount - 50 * i));
                    float f1 = Mth.cos(f0 / 10);
                    float f2 = Mth.sin(f0 / 10);
                    float f3 = Mth.sin(f0 / 50);
                    p_117349_.translate(f1 * 0.75F,f3 * 0.25F ,f2 * 0.75F);
                    p_117349_.scale(0.5F,0.5F,0.5F);

                    Quaternion rotation = Vector3f.YP.rotation((float)(Math.PI)*f0/100 );
                    rotation.mul(Vector3f.ZP.rotation((float)Math.PI));
                    p_117349_.mulPose(rotation);

                    model.setupAnim(p_117352_,0.0F,0.0F,p_117352_.tickCount+Minecraft.getInstance().getPartialTick(),0.0F,0.0F);
                    int pos = e.chargePos;

                    model1.renderToBuffer(p_117349_,p_117350_.getBuffer(RenderType.entityCutoutNoCull(ADDONS[pos])),p_117351_,OverlayTexture.NO_OVERLAY,1.0F,1.0F,1.0F,1.0F);
                    model.renderToBuffer(p_117349_,p_117350_.getBuffer(RenderType.entityTranslucent(TEXTURE)),p_117351_,OverlayTexture.NO_OVERLAY,1.0F,1.0F,1.0F,1.0F);


                    p_117349_.popPose();

                }
            }
        });

    }
}
