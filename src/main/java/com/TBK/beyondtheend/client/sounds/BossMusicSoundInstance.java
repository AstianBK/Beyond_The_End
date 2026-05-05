package com.TBK.beyondtheend.client.sounds;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class BossMusicSoundInstance extends AbstractSoundInstance implements TickableSoundInstance {
    private boolean stopped = false;
    public BossMusicSoundInstance(SoundEvent sound) {
        super(sound.getLocation(), SoundSource.MUSIC, RandomSource.create());
        this.looping = true; this.delay = 0; this.volume = 1.0F; this.pitch = 1.0F;
        this.x = 0; this.y = 0; this.z = 0;
        this.attenuation = Attenuation.NONE;
    }
    @Override public boolean isStopped() { return stopped; }
    @Override public void tick() {}
    public void stopMusic() { this.stopped = true; }
}
