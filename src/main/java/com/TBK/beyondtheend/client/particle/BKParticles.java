package com.TBK.beyondtheend.client.particle;

import com.TBK.beyondtheend.BeyondTheEnd;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BKParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, BeyondTheEnd.MODID);


    public static final RegistryObject<SimpleParticleType> FLAME_PARTICLE =
            PARTICLE_TYPES.register("flame_particle", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> MIST_PARTICLE =
            PARTICLE_TYPES.register("mist", () -> new SimpleParticleType(true));

}
