package com.TBK.beyondtheend.server;

import com.TBK.beyondtheend.BeyondTheEnd;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Precarga las 14 estructuras del lobby (isla de Beyond The End) en la
 * cache de StructureTemplateManager durante el arranque del servidor, en
 * vez de dejar que se lean+parseen de disco de forma sincrona la primera
 * vez que un jugador entra (que es lo que congela el hilo del servidor).
 *
 * No cambia nada de la colocacion de bloques ni de FallenDragonFight: solo
 * adelanta el trabajo de lectura/parseo de NBT a un momento en el que un
 * "freeze" no se nota (la pantalla de carga del servidor), aprovechando que
 * StructureTemplateManager#get(...) cachea el resultado internamente, asi
 * que cuando FallenDragonFight.Structure#findStructure(...) lo pida despues,
 * sera un cache hit casi instantaneo en lugar de una lectura de disco.
 */
@Mod.EventBusSubscriber(modid = BeyondTheEnd.MODID)
public class StructureWarmup {

    // Mismos 14 nombres que usa FallenDragonFight.Structure#makeInitialIsland(...)
    private static final String[] LOBBY_STRUCTURES = {
            "center",
            "island_east",
            "island_south_0",
            "island_south_1",
            "island_north",
            "island_west_0",
            "island_west_1",
            "west_north_0",
            "west_north_1",
            "west_south_0",
            "west_south_1",
            "island_east_north",
            "east_south_0",
            "east_south_1"
    };

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        StructureTemplateManager structureManager = server.getStructureManager();

        long start = System.currentTimeMillis();
        BeyondTheEnd.LOGGER.info(
                "[BeyondTheEnd] Precargando {} estructuras del lobby en el arranque del servidor...",
                LOBBY_STRUCTURES.length
        );

        for (String name : LOBBY_STRUCTURES) {
            ResourceLocation loc = new ResourceLocation(BeyondTheEnd.MODID, name);
            try {
                // El resultado se descarta a proposito: lo unico que nos
                // interesa es forzar la carga+parseo ahora, para que quede
                // en la cache interna de StructureTemplateManager.
                structureManager.get(loc);
            } catch (Exception e) {
                BeyondTheEnd.LOGGER.error("[BeyondTheEnd] Fallo precargando estructura {}", loc, e);
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        BeyondTheEnd.LOGGER.info(
                "[BeyondTheEnd] Estructuras del lobby precargadas en {} ms (arranque del servidor)",
                elapsed
        );
    }
}
