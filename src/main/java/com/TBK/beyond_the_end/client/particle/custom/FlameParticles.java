package com.TBK.beyond_the_end.client.particle.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class FlameParticles extends TextureSheetParticle {
    private final SpriteSet sprites;
    private boolean hasHitGround;

    protected FlameParticles(ClientLevel level, double x, double y, double z, double sX, double sY, double sZ, SpriteSet spriteSet) {
        super(level,x,y,z,sX,sY,sZ);
        this.friction = 0.96F;
        this.xd = sX;
        this.yd = sY;
        this.zd = sZ;
        this.rCol = Mth.nextFloat(this.random, 0.7176471F, 0.8745098F);
        this.gCol = Mth.nextFloat(this.random, 0.0F, 0.0F);
        this.bCol = Mth.nextFloat(this.random, 0.8235294F, 0.9764706F);
        this.quadSize *= 0.75F;
        this.lifetime = (int)(20.0D / ((double)this.random.nextFloat() * 0.8D + 0.2D));
        this.hasHitGround = false;
        this.hasPhysics = false;
        this.sprites = spriteSet;
        this.setSpriteFromAge(spriteSet);
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
            return new FlameParticles(world,x,y,z,xSpeed,ySpeed,zSpeed,this.spriteSet);
        }
    }
}
