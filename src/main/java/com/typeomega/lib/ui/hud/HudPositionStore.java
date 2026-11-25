package com.typeomega.lib.ui.hud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.typeomega.lib.TypeOmegaLib;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Persists HUD anchor positions (0..100% of available screen space) keyed by HUD id in a client
 * JSON file. Kept free of Minecraft client types so unit tests can exercise read/write round-trips.
 */
public final class HudPositionStore {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public record Position(int xPercent, int yPercent) {

        public Position {
            xPercent = clampPercent(xPercent);
            yPercent = clampPercent(yPercent);
        }
    }

    private final Path file;
    private final Map<String, Position> positions = new LinkedHashMap<>();
    private boolean loaded;

    public HudPositionStore(Path file) {
        this.file = file;
    }

    public synchronized Optional<Position> get(String id) {
        ensureLoaded();
        return Optional.ofNullable(positions.get(id));
    }

    public synchronized Position getOrDefault(String id, int defaultXPercent, int defaultYPercent) {
        ensureLoaded();
        Position saved = positions.get(id);
        return saved != null ? saved : new Position(defaultXPercent, defaultYPercent);
    }

    public synchronized boolean has(String id) {
        ensureLoaded();
        return positions.containsKey(id);
    }

    public synchronized void set(String id, int xPercent, int yPercent) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("HUD id must not be blank");
        }
        ensureLoaded();
        positions.put(id, new Position(xPercent, yPercent));
        save();
    }

    /** Writes {@code (xPercent, yPercent)} only when {@code id} has no saved entry yet. */
    public synchronized boolean setIfAbsent(String id, int xPercent, int yPercent) {
        if (has(id)) return false;
        set(id, xPercent, yPercent);
        return true;
    }

    public synchronized void reload() {
        positions.clear();
        loaded = false;
        ensureLoaded();
    }

    public static int clampPercent(int percent) {
        return Math.max(0, Math.min(100, percent));
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int pixelX(int xPercent, int hudWidth, int screenWidth) {
        int maxX = Math.max(0, screenWidth - Math.max(0, hudWidth));
        return clampPercent(xPercent) * maxX / 100;
    }

    public static int pixelY(int yPercent, int hudHeight, int screenHeight) {
        int maxY = Math.max(0, screenHeight - Math.max(0, hudHeight));
        return clampPercent(yPercent) * maxY / 100;
    }

    public static int percentX(int x, int hudWidth, int screenWidth) {
        int maxX = Math.max(0, screenWidth - Math.max(0, hudWidth));
        return maxX <= 0 ? 0 : clampPercent((int) Math.round(clamp(x, 0, maxX) * 100.0 / maxX));
    }

    public static int percentY(int y, int hudHeight, int screenHeight) {
        int maxY = Math.max(0, screenHeight - Math.max(0, hudHeight));
        return maxY <= 0 ? 0 : clampPercent((int) Math.round(clamp(y, 0, maxY) * 100.0 / maxY));
    }

    private void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        if (!Files.exists(file)) return;
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (!parsed.isJsonObject()) return;
            JsonObject root = parsed.getAsJsonObject();
            for (var entry : root.entrySet()) {
                if (!entry.getValue().isJsonObject()) continue;
                JsonObject obj = entry.getValue().getAsJsonObject();
                if (!obj.has("xPercent") || !obj.has("yPercent")) continue;
                int x = obj.get("xPercent").getAsInt();
                int y = obj.get("yPercent").getAsInt();
                positions.put(entry.getKey(), new Position(x, y));
            }
        } catch (Exception e) {
            TypeOmegaLib.LOGGER.warn("Could not read HUD positions from {}", file, e);
        }
    }

    private void save() {
        try {
            Path parent = file.getParent();
            if (parent != null) Files.createDirectories(parent);
            JsonObject root = new JsonObject();
            for (var entry : positions.entrySet()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("xPercent", entry.getValue().xPercent());
                obj.addProperty("yPercent", entry.getValue().yPercent());
                root.add(entry.getKey(), obj);
            }
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException e) {
            TypeOmegaLib.LOGGER.warn("Could not save HUD positions to {}", file, e);
        }
    }
}
