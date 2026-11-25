package com.typeomega.lib.ui.hud;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HudPositionStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void savesAndReloadsPositionsById() {
        Path file = tempDir.resolve("nested/typeomegalib-huds.json");
        HudPositionStore writer = new HudPositionStore(file);

        assertTrue(writer.get("gtna:wireless_energy").isEmpty());
        assertEquals(new HudPositionStore.Position(5, 60),
                writer.getOrDefault("gtna:wireless_energy", 5, 60));

        writer.set("gtna:wireless_energy", 22, 84);
        writer.set("gtna:wireless_steam", 90, 15);
        assertTrue(Files.exists(file));

        HudPositionStore reader = new HudPositionStore(file);
        assertEquals(new HudPositionStore.Position(22, 84),
                reader.get("gtna:wireless_energy").orElseThrow());
        assertEquals(new HudPositionStore.Position(90, 15),
                reader.get("gtna:wireless_steam").orElseThrow());
    }

    @Test
    void clampsPercentagesAndRejectsBlankId() {
        HudPositionStore store = new HudPositionStore(tempDir.resolve("huds.json"));
        store.set("typeomega:kardashev", -12, 145);

        assertEquals(new HudPositionStore.Position(0, 100),
                store.get("typeomega:kardashev").orElseThrow());
        assertThrows(IllegalArgumentException.class, () -> store.set("  ", 10, 10));
    }

    @Test
    void setIfAbsentMigratesOnceWithoutOverwritingExisting() {
        Path file = tempDir.resolve("huds.json");
        HudPositionStore store = new HudPositionStore(file);

        assertTrue(store.setIfAbsent("gtna:wireless_energy", 40, 50));
        assertFalse(store.setIfAbsent("gtna:wireless_energy", 99, 99));
        assertEquals(new HudPositionStore.Position(40, 50),
                new HudPositionStore(file).get("gtna:wireless_energy").orElseThrow());
    }

    @Test
    void invalidJsonDoesNotCrashReader() throws Exception {
        Path file = tempDir.resolve("broken.json");
        Files.writeString(file, "{not valid json", StandardCharsets.UTF_8);

        HudPositionStore store = new HudPositionStore(file);
        assertTrue(store.get("gtna:wireless_energy").isEmpty());
    }

    @Test
    void pixelAndPercentConversionsRoundTripCleanly() {
        int screenW = 640;
        int screenH = 360;
        int hudW = 120;
        int hudH = 40;

        int px = HudPositionStore.pixelX(25, hudW, screenW);
        int py = HudPositionStore.pixelY(75, hudH, screenH);
        assertEquals(130, px);
        assertEquals(240, py);
        assertEquals(25, HudPositionStore.percentX(px, hudW, screenW));
        assertEquals(75, HudPositionStore.percentY(py, hudH, screenH));
    }
}
