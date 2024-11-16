package com.TBK.beyond_the_end.server.entity.phase;

import com.TBK.beyond_the_end.server.entity.FallenDragonEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.slf4j.Logger;

import javax.annotation.Nullable;

public class FallenDragonPhaseManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final FallenDragonEntity dragon;
    private final FallenDragonInstance[] phases = new FallenDragonInstance[FallenDragonPhase.getCount()];
    @Nullable
    private FallenDragonInstance currentPhase;

    public FallenDragonPhaseManager(FallenDragonEntity p_31414_) {
        this.dragon = p_31414_;
        this.setPhase(FallenDragonPhase.HOVERING);
    }

    public void setPhase(FallenDragonPhase<?> p_31417_) {
        if (this.currentPhase == null || p_31417_ != this.currentPhase.getPhase()) {
            if (this.currentPhase != null) {
                this.currentPhase.end();
            }

            this.currentPhase = this.getPhase(p_31417_);
            if (!this.dragon.level.isClientSide) {
                this.dragon.getEntityData().set(FallenDragonEntity.DATA_PHASE, p_31417_.getId());
            }

            LOGGER.debug("Dragon is now in phase {} on the {}", p_31417_, this.dragon.level.isClientSide ? "client" : "server");
            this.currentPhase.begin();
        }
    }

    public FallenDragonInstance getCurrentPhase() {
        return this.currentPhase;
    }

    public <T extends FallenDragonInstance> T getPhase(FallenDragonPhase<T> p_31419_) {
        int i = p_31419_.getId();
        if (this.phases[i] == null) {
            this.phases[i] = p_31419_.createInstance(this.dragon);
        }

        return (T)this.phases[i];
    }
}
