package com.TBK.beyondtheend.server.world.featured;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class SpikesBossConfiguration implements FeatureConfiguration {
    public static final Codec<SpikesBossConfiguration> CODEC = RecordCodecBuilder.create((p_68115_) -> {
        return p_68115_.group(Codec.BOOL.fieldOf("crystal_invulnerable").orElse(false).forGetter((p_161195_) -> {
            return p_161195_.crystalInvulnerable;
        }), SpikeBossFeatured.EndSpike.CODEC.listOf().fieldOf("spikes").forGetter((p_161193_) -> {
            return p_161193_.spikes;
        }), BlockPos.CODEC.optionalFieldOf("crystal_beam_target").forGetter((p_161191_) -> {
            return Optional.ofNullable(p_161191_.crystalBeamTarget);
        })).apply(p_68115_, SpikesBossConfiguration::new);
    });
    private final boolean crystalInvulnerable;
    private final List<SpikeBossFeatured.EndSpike> spikes;
    @Nullable
    private final BlockPos crystalBeamTarget;

    public SpikesBossConfiguration(boolean p_68105_, List<SpikeBossFeatured.EndSpike> p_68106_, @Nullable BlockPos p_68107_) {
        this(p_68105_, p_68106_, Optional.ofNullable(p_68107_));
    }

    private SpikesBossConfiguration(boolean p_68109_, List<SpikeBossFeatured.EndSpike> p_68110_, Optional<BlockPos> p_68111_) {
        this.crystalInvulnerable = p_68109_;
        this.spikes = p_68110_;
        this.crystalBeamTarget = p_68111_.orElse((BlockPos)null);
    }

    public boolean isCrystalInvulnerable() {
        return this.crystalInvulnerable;
    }

    public List<SpikeBossFeatured.EndSpike> getSpikes() {
        return this.spikes;
    }

    @Nullable
    public BlockPos getCrystalBeamTarget() {
        return this.crystalBeamTarget;
    }
}
