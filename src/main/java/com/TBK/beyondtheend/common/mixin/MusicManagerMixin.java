package com.TBK.beyondtheend.common.mixin;

import com.TBK.beyondtheend.common.registry.BkDimension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Silencia la musica situacional (la que gestiona MusicManager) dentro de la dimension Beyond The End.
 * Cubre la musica de vanilla, Medieval Music (que solo reemplaza los ogg de vanilla) y Biome Music
 * (que sustituye Minecraft#getSituationalMusic). La musica de los jefes no pasa por aqui: la lanza
 * ClientEvents directamente con el SoundManager.
 */
@Mixin(MusicManager.class)
public abstract class MusicManagerMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private SoundInstance currentMusic;

    @Shadow
    public abstract void stopPlaying();

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void beyondtheend$silenceMusic(CallbackInfo ci) {
        if (this.minecraft.level != null && this.minecraft.level.dimension().equals(BkDimension.BEYOND_END_LEVEL)) {
            if (this.currentMusic != null) {
                this.stopPlaying();
            }
            ci.cancel();
        }
    }
}
