package com.TBK.beyondtheend;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = BeyondTheEnd.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    static {
        BUILDER.push("jellyfish");
    }

    public static final ForgeConfigSpec.DoubleValue JELLYFISH_HEALTH_PER_EXTRA_PLAYER = BUILDER
            .comment("Vida extra del Jellyfish por cada jugador adicional en la arena (0.5 = +50%).")
            .defineInRange("healthPerExtraPlayer", 0.5D, 0.0D, 10.0D);

    public static final ForgeConfigSpec.DoubleValue JELLYFISH_DAMAGE_PER_EXTRA_PLAYER = BUILDER
            .comment("Dano extra del Jellyfish y sus proyectiles por cada jugador adicional (0.25 = +25%).")
            .defineInRange("damagePerExtraPlayer", 0.25D, 0.0D, 10.0D);

    public static final ForgeConfigSpec.IntValue JELLYFISH_MAX_SCALED_PLAYERS = BUILDER
            .comment("Numero maximo de jugadores que cuentan para el escalado.")
            .defineInRange("maxScaledPlayers", 8, 1, 64);

    public static final ForgeConfigSpec.DoubleValue JELLYFISH_REGEN_PERCENT_PER_SECOND = BUILDER
            .comment("Porcentaje de vida maxima que regenera cada segundo mientras no esta en una ventana de dano (0 = desactivado).")
            .defineInRange("regenPercentPerSecond", 0.20D, 0.0D, 10.0D);

    public static final ForgeConfigSpec.BooleanValue JELLYFISH_SHIELD_WITH_MINIONS = BUILDER
            .comment("Si es true, el Jellyfish no recibe dano mientras quede algun minion vivo.")
            .define("shieldWithMinions", true);

    static {
        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();


    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {

    }
}
