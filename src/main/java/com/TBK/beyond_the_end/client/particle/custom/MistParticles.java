package com.TBK.beyond_the_end.client.particle.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class MistParticles extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected MistParticles(ClientLevel level, double x, double y, double z, double sX, double sY, double sZ, SpriteSet spriteSet) {
        super(level,x,y,z,sX,sY,sZ);
        this.friction = 0.98F;
        this.xd = sX;
        this.yd = sY;
        this.zd = sZ;
        this.quadSize *= 1F;
        this.scale(30.0F);
        this.lifetime = (int)(100.0D / ((double)this.random.nextFloat() * 0.8D + 0.2D));
        this.hasPhysics = false;
        this.sprites = spriteSet;
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick() {
        super.tick();
        if(this.onGround){
            this.remove();
        }
        if(this.age%10==0){
            this.alpha-=0.15F;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new MistParticles(world,x,y,z,xSpeed,ySpeed,zSpeed,this.spriteSet);
        }
    }
}
