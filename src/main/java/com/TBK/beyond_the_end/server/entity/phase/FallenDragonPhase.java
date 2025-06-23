package com.TBK.beyond_the_end.server.entity.phase;

import com.TBK.beyond_the_end.server.entity.FallenDragonEntity;

import java.lang.reflect.Constructor;
import java.util.Arrays;

public class FallenDragonPhase<T extends FallenDragonInstance> {
    private static FallenDragonPhase<?>[] phases = new FallenDragonPhase[0];
    public static final FallenDragonPhase<FallenDragonHoldingPatternPhase> HOLDING_PATTERN = create(FallenDragonHoldingPatternPhase.class, "HoldingPattern");
    public static final FallenDragonPhase<RespawnPhase> RESPAWN = create(RespawnPhase.class, "Respawn");
    public static final FallenDragonPhase<DiyingPhase> DEATH = create(DiyingPhase.class, "Death");

    public static final FallenDragonPhase<SkyFallAttack> SKYFALL = create(SkyFallAttack.class, "SkyFall");
    public static final FallenDragonPhase<FlameDragonAttackPhase> FLAME = create(FlameDragonAttackPhase.class, "Flame");

    public static final FallenDragonPhase<FallenDragonHoverPhase> HOVERING = create(FallenDragonHoverPhase.class, "Hover");
    public static final FallenDragonPhase<ContraAttack> CONTRA_ATTACK = create(ContraAttack.class, "ContraAttack");

    private final Class<? extends FallenDragonInstance> instanceClass;
    private final int id;
    private final String name;

    private FallenDragonPhase(int p_31394_, Class<? extends FallenDragonInstance> p_31395_, String p_31396_) {
        this.id = p_31394_;
        this.instanceClass = p_31395_;
        this.name = p_31396_;
    }

    public FallenDragonInstance createInstance(FallenDragonEntity p_31401_) {
        try {
            Constructor<? extends FallenDragonInstance> constructor = this.getConstructor();
            return constructor.newInstance(p_31401_);
        } catch (Exception exception) {
            throw new Error(exception);
        }
    }

    protected Constructor<? extends FallenDragonInstance> getConstructor() throws NoSuchMethodException {
        return this.instanceClass.getConstructor(FallenDragonEntity.class);
    }

    public int getId() {
        return this.id;
    }

    public String toString() {
        return this.name + " (#" + this.id + ")";
    }

    public static FallenDragonPhase<?> getById(int p_31399_) {
        return p_31399_ >= 0 && p_31399_ < phases.length ? phases[p_31399_] : HOLDING_PATTERN;
    }

    public static int getCount() {
        return phases.length;
    }

    private static <T extends FallenDragonInstance> FallenDragonPhase<T> create(Class<T> p_31403_, String p_31404_) {
        FallenDragonPhase<T> enderdragonphase = new FallenDragonPhase<>(phases.length, p_31403_, p_31404_);
        phases = Arrays.copyOf(phases, phases.length + 1);
        phases[enderdragonphase.getId()] = enderdragonphase;
        return enderdragonphase;
    }
}
