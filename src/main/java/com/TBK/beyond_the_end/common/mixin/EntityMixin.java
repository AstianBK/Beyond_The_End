package com.TBK.beyond_the_end.common.mixin;

import com.TBK.beyond_the_end.BeyondTheEnd;
import com.TBK.beyond_the_end.common.blocks.BKPortalForcer;
import com.TBK.beyond_the_end.common.registry.BkDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public Level level;

    @Shadow public abstract void unRide();

    @Shadow public abstract boolean isRemoved();

    @Shadow @Nullable protected abstract PortalInfo findDimensionEntryPoint(ServerLevel p_19923_);

    @Shadow public abstract EntityType<?> getType();

    @Shadow protected abstract void removeAfterChangingDimensions();

    @Shadow private float yRot;

    @Shadow public abstract Vec3 getDeltaMovement();

    @Shadow public abstract float getYRot();

    @Shadow public abstract float getXRot();

    @Inject(
            method = {"changeDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraftforge/common/util/ITeleporter;)Lnet/minecraft/world/entity/Entity;"},
            at = @At(value = "HEAD"),
            cancellable = true)
    public void cancelObsidianPlataform(ServerLevel p_20118_, ITeleporter teleporter, CallbackInfoReturnable<Entity> cir){
        if(teleporter instanceof BKPortalForcer){
            Entity entity1=((Entity)((Object)this));
            if (!net.minecraftforge.common.ForgeHooks.onTravelToDimension(entity1, p_20118_.dimension())){
                cir.setReturnValue(null);
            }
            if (this.level instanceof ServerLevel && !this.isRemoved()) {
                this.level.getProfiler().push("changeDimension");
                this.unRide();
                this.level.getProfiler().push("reposition");
                PortalInfo portalinfo = teleporter.getPortalInfo(entity1, p_20118_, this::findDimensionEntryPoint);
                if (portalinfo == null) {
                    cir.setReturnValue(null);
                } else {
                    Entity transportedEntity = teleporter.placeEntity(entity1, (ServerLevel) this.level, p_20118_, this.yRot, spawnPortal -> { //Forge: Start vanilla logic
                        this.level.getProfiler().popPush("reloading");
                        Entity entity = this.getType().create(p_20118_);
                        if (entity != null) {
                            entity.restoreFrom(entity1);
                            entity.moveTo(portalinfo.pos.x, portalinfo.pos.y, portalinfo.pos.z, portalinfo.yRot, entity.getXRot());
                            entity.setDeltaMovement(portalinfo.speed);
                            p_20118_.addDuringTeleport(entity);
                        }
                        if (spawnPortal && p_20118_.dimension() == BkDimension.BEYOND_END_LEVEL) {
                            ServerLevel.makeObsidianPlatform(p_20118_);
                        }
                        return entity;
                    }); //Forge: End vanilla logic

                    this.removeAfterChangingDimensions();
                    this.level.getProfiler().pop();
                    ((ServerLevel)this.level).resetEmptyTime();
                    p_20118_.resetEmptyTime();
                    this.level.getProfiler().pop();
                    cir.setReturnValue(transportedEntity);
                }
            } else {
                cir.setReturnValue(null);
            }
        }
    }

    @Inject(
            method = {"findDimensionEntryPoint"},
            at = @At(value = "HEAD"),
            cancellable = true)
    public void findBKPointSpawn(ServerLevel p_19923_, CallbackInfoReturnable<PortalInfo> cir){
        if(p_19923_.dimension() == BkDimension.BEYOND_END_LEVEL){
            BlockPos blockpos=BlockPos.ZERO;
            cir.setReturnValue(new PortalInfo(new Vec3((double)blockpos.getX() + 0.5D, 50.0D, (double)blockpos.getZ() + 0.5D), this.getDeltaMovement(), this.getYRot(), this.getXRot()));
        }
    }
}
