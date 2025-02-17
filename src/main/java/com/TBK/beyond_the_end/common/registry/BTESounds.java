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

    //GUNS
    public static final RegistryObject<SoundEvent> ARQUEBUS_SHOOT =
            registerSoundEvent("arquebus_shoot");

    public static final RegistryObject<SoundEvent> HANDGONNE_SHOOT =
            registerSoundEvent("handgonne_shoot");

    //MELEE

    public static final RegistryObject<SoundEvent> MORNINGSTAR_HIT =
            registerSoundEvent("morningstar_hit");

    public static final RegistryObject<SoundEvent> EVENINGSTAR_HIT =
            registerSoundEvent("eveningstar_hit");

    public static final RegistryObject<SoundEvent> THROW_WEAPON =
            registerSoundEvent("throw_weapon");


    public static RegistryObject<SoundEvent> registerSoundEvent(String name){
        return SOUND_EVENTS.register(name, () ->new  SoundEvent(new ResourceLocation(BeyondTheEnd.MODID, name)));
    }

    public static void register(IEventBus eventBus){
        SOUND_EVENTS.register(eventBus);
    }
}


