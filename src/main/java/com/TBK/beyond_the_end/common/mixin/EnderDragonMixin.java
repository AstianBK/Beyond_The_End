package com.TBK.beyond_the_end.common.mixin;

import com.TBK.beyond_the_end.common.blocks.BKPortalForcer;
import com.TBK.beyond_the_end.common.blocks.PortalBlock;
import com.TBK.beyond_the_end.common.registry.BKEntityType;
import com.TBK.beyond_the_end.common.registry.BkDimension;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragon.class)
public class EnderDragonMixin {
    @ModifyArg(method = "<init>",at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V"))
    private static EntityType<? extends Mob> summon(EntityType<? extends Mob> p_21368_,Level level){
        return level.dimension().equals(PortalBlock.destinationDimension()) ? BKEntityType.FALLEN_DRAGON.get() : EntityType.ENDER_DRAGON;
    }
}
