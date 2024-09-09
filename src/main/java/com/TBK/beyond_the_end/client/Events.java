package com.TBK.beyond_the_end.client;

import com.TBK.beyond_the_end.BeyondTheEnd;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BeyondTheEnd.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Events {
    @SubscribeEvent
    public static void registerDimensionSpecialEffects(RegisterDimensionSpecialEffectsEvent event) {
        DimensionSpecialEffects effects1=new DimensionSpecialEffects(Float.NaN, true, DimensionSpecialEffects.SkyType.END, false, true) {
            @Override
            public Vec3 getBrightnessDependentFogColor(Vec3 p_108908_, float p_108909_) {
                return p_108908_;
            }

            @Override
            public boolean isFoggyAt(int p_108874_, int p_108875_) {
                return true;
            }
        };
        event.register(new ResourceLocation(BeyondTheEnd.MODID, "the_new_end"), effects1);
    }
}
