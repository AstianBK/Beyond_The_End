package com.TBK.beyond_the_end.common.registry;

import com.TBK.beyond_the_end.BeyondTheEnd;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BTESounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, BeyondTheEnd.MODID);

    //ENTITY
    public static final RegistryObject<SoundEvent> JELLYFISH_SHOOT1 =
            registerSoundEvent("arquebus_shoot");

    public static final RegistryObject<SoundEvent> JELLYFISH_SHOOT2 =
            registerSoundEvent("jellyfish_shoot2");

    public static final RegistryObject<SoundEvent> JELLYFISH_CHARGE =
            registerSoundEvent("jellyfish_charge");

    public static final RegistryObject<SoundEvent> JELLYFISH_LASERLOOP =
            registerSoundEvent("jellyfish_laserloop");

    public static final RegistryObject<SoundEvent> JELLYFISH_EXPLOSION =
            registerSoundEvent("jellyfish_explosion");

    public static final RegistryObject<SoundEvent> JELLYFISH_HURT =
            registerSoundEvent("jellyfish_hurt");

    public static final RegistryObject<SoundEvent> JELLYFISH_SUMMON =
            registerSoundEvent("jellyfish_summon");

    public static RegistryObject<SoundEvent> registerSoundEvent(String name){
        return SOUND_EVENTS.register(name, () ->new  SoundEvent(new ResourceLocation(BeyondTheEnd.MODID, name)));
    }

    public static void register(IEventBus eventBus){
        SOUND_EVENTS.register(eventBus);
    }
}


