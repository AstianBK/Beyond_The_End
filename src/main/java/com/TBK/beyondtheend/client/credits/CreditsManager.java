
package com.TBK.beyondtheend.client.credits;

import com.TBK.beyondtheend.BeyondTheEnd;

import com.google.gson.*;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class CreditsManager {

    private static final ResourceLocation CREDITS_FILE =
            new ResourceLocation(
                    BeyondTheEnd.MODID,
                    "credits/credits.json"
            );

    private static List<CreditCategory> credits = List.of();

    private CreditsManager() {
    }

    // ==========================================
    // CARGAR JSON
    // ==========================================

    public static void load() {

        List<CreditCategory> loaded = new ArrayList<>();

        try {
            Optional<Resource> resource = Minecraft.getInstance()
                    .getResourceManager()
                    .getResource(CREDITS_FILE);

            if (resource.isEmpty()) {
                System.err.println(
                        "[CREDITS] No se encontro: " + CREDITS_FILE
                );
                credits = List.of();
                return;
            }

            try (InputStream stream = resource.get().open();
                 InputStreamReader reader = new InputStreamReader(
                         stream, StandardCharsets.UTF_8)) {

                JsonObject root = JsonParser
                        .parseReader(reader)
                        .getAsJsonObject();

                JsonArray categories = root.getAsJsonArray("Credits");

                if (categories == null) {
                    throw new JsonParseException(
                            "No existe la lista 'Credits'"
                    );
                }

                for (JsonElement element : categories) {

                    if (!element.isJsonObject()) {
                        continue;
                    }

                    JsonObject obj = element.getAsJsonObject();

                    if (!obj.has("category")
                            || !obj.get("category").isJsonPrimitive()) {
                        continue;
                    }

                    String name = obj.get("category").getAsString();

                    JsonArray authorsArray =
                            obj.getAsJsonArray("authors");

                    if (name.isBlank() || authorsArray == null) {
                        continue;
                    }

                    List<String> authors = new ArrayList<>();

                    for (JsonElement author : authorsArray) {
                        if (author.isJsonPrimitive()
                                && author.getAsJsonPrimitive().isString()) {

                            String authorName = author.getAsString();

                            if (!authorName.isBlank()) {
                                authors.add(authorName);
                            }
                        }
                    }

                    loaded.add(
                            new CreditCategory(name, authors)
                    );
                }
            }

            credits = List.copyOf(loaded);

            System.out.println(
                    "[CREDITS] Categorias cargadas: " + credits.size()
            );

        } catch (Exception e) {
            credits = List.of();

            System.err.println(
                    "[CREDITS] Error al leer el archivo JSON"
            );

            e.printStackTrace();
        }
    }

    // ==========================================
    // ACCESO A LOS CREDITOS
    // ==========================================

    public static List<CreditCategory> getCredits() {
        return credits;
    }

    public static boolean isEmpty() {
        return credits.isEmpty();
    }

    public static int getCategoryCount() {
        return credits.size();
    }

    public static int getAuthorCount() {
        int count = 0;

        for (CreditCategory category : credits) {
            count += category.authors().size();
        }

        return count;
    }
}
