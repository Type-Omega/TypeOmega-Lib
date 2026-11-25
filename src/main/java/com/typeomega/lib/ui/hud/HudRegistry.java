package com.typeomega.lib.ui.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLPaths;

import com.typeomega.lib.ui.hud.HudPositionStore.Position;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Shared client registry for {@link MoveableHud} overlays and their persisted screen positions.
 * Any mod can call {@link #register(MoveableHud)} during client setup so its HUD appears in
 * {@link HudEditorScreen}, responds to the shared editor keybind and stores its position by id.
 */
@OnlyIn(Dist.CLIENT)
public final class HudRegistry {

    public static final String CONFIG_FILE_NAME = "typeomegalib-huds.json";

    private static final Object LOCK = new Object();
    private static final Map<String, MoveableHud> HUDS = new LinkedHashMap<>();
    private static volatile HudPositionStore store;

    private HudRegistry() {}

    /** Registers a HUD overlay; idempotent for the same instance, rejects colliding ids. */
    public static void register(MoveableHud hud) {
        Objects.requireNonNull(hud, "hud");
        String id = hud.id();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("HUD id must not be blank");
        }
        synchronized (LOCK) {
            MoveableHud existing = HUDS.get(id);
            if (existing == hud) return;
            if (existing != null) {
                throw new IllegalArgumentException("Duplicate HUD id: " + id);
            }
            HUDS.put(id, hud);
        }
    }

    public static List<MoveableHud> all() {
        synchronized (LOCK) {
            return List.copyOf(HUDS.values());
        }
    }

    public static Optional<MoveableHud> get(String id) {
        synchronized (LOCK) {
            return Optional.ofNullable(HUDS.get(id));
        }
    }

    public static Position position(MoveableHud hud) {
        Objects.requireNonNull(hud, "hud");
        return store().getOrDefault(hud.id(), hud.defaultXPercent(), hud.defaultYPercent());
    }

    public static void setPosition(String id, int xPercent, int yPercent) {
        store().set(id, xPercent, yPercent);
    }

    /** Seeds a position only if {@code id} has no saved entry yet (used to migrate legacy configs). */
    public static boolean migratePositionIfAbsent(String id, int xPercent, int yPercent) {
        return store().setIfAbsent(id, xPercent, yPercent);
    }

    public static Rect2i bounds(MoveableHud hud, int screenWidth, int screenHeight) {
        int width = Math.max(0, hud.width());
        int height = Math.max(0, hud.height());
        Position pos = position(hud);
        int x = HudPositionStore.pixelX(pos.xPercent(), width, screenWidth);
        int y = HudPositionStore.pixelY(pos.yPercent(), height, screenHeight);
        return new Rect2i(x, y, width, height);
    }

    public static void setTopLeftPosition(MoveableHud hud, int x, int y, int screenWidth, int screenHeight) {
        int width = Math.max(0, hud.width());
        int height = Math.max(0, hud.height());
        int percentX = HudPositionStore.percentX(x, width, screenWidth);
        int percentY = HudPositionStore.percentY(y, height, screenHeight);
        setPosition(hud.id(), percentX, percentY);
    }

    public static void openEditor() {
        Minecraft.getInstance().setScreen(new HudEditorScreen());
    }

    private static HudPositionStore store() {
        HudPositionStore current = store;
        if (current == null) {
            synchronized (LOCK) {
                current = store;
                if (current == null) {
                    current = new HudPositionStore(FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE_NAME));
                    store = current;
                }
            }
        }
        return current;
    }

    static void setStoreForTests(HudPositionStore testStore) {
        synchronized (LOCK) {
            store = testStore;
        }
    }

    static void clearForTests() {
        synchronized (LOCK) {
            HUDS.clear();
            store = null;
        }
    }
}
