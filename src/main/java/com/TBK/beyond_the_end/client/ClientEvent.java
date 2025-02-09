package com.TBK.beyond_the_end.client;


import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.client.layer.ChargeLayer;
import com.TBK.beyond_the_end.client.model.JellyfishMinionModel;
import com.TBK.beyond_the_end.client.model.JellyfishModel;
import com.TBK.beyond_the_end.client.model.LightballModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BeyondTheEnd.MODID, bus = Mod.EventBusSubscriber.Bus.MOD,value = Dist.CLIENT)
public class ClientEvent {
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.AddLayers event){
        event.getSkins().forEach(s -> {
            event.getSkin(s).addLayer(new ChargeLayer(event.getSkin(s)));
            //event.getSkin(s).addLayer(new LivingProtectionLayer(event.getSkin(s)));
        });
    }

    @SubscribeEvent
    public static void registerLayerDefinition(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(LightballModel.LAYER_LOCATION, LightballModel::createBodyLayer);
        event.registerLayerDefinition(JellyfishModel.LAYER_LOCATION, JellyfishModel::createBodyLayer);
        event.registerLayerDefinition(JellyfishMinionModel.LAYER_LOCATION, JellyfishMinionModel::createBodyLayer);
    }
}
