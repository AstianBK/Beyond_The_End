package com.TBK.beyond_the_end.common.mixin;

import com.TBK.beyond_the_end.common.registry.BkCommonRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndDragonFight.class)
public class EndDragonFightMixins {
    @Shadow
    private boolean previouslyKilled;
    @Shadow
    private ServerLevel level;


    @Inject(
            method = {"setDragonKilled"},
            at = @At(value = "TAIL")
    )
    public void dragonKilled(EnderDragon p_64086_, CallbackInfo ci){
        this.level.setBlockAndUpdate(this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, EndPodiumFeature.END_PODIUM_LOCATION.above()), BkCommonRegistry.PORTAL.get().defaultBlockState());
    }
}
