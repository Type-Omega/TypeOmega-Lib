package com.typeomega.lib.ui.hud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HudRegistryTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setup() {
        HudRegistry.clearForTests();
        HudRegistry.setStoreForTests(new HudPositionStore(tempDir.resolve("huds.json")));
    }

    @AfterEach
    void tearDown() {
        HudRegistry.clearForTests();
    }

    @Test
    void registersInOrderAndRejectsCollidingIds() {
        StubHud first = new StubHud("gtna:wireless_energy", 100, 38, 5, 60);
        StubHud second = new StubHud("gtna:wireless_steam", 120, 64, 5, 75);

        HudRegistry.register(first);
        HudRegistry.register(first);
        HudRegistry.register(second);

        assertEquals(List.of(first, second), HudRegistry.all());
        assertSame(first, HudRegistry.get("gtna:wireless_energy").orElseThrow());
        assertThrows(IllegalArgumentException.class,
                () -> HudRegistry.register(new StubHud("gtna:wireless_energy", 50, 20, 0, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> HudRegistry.register(new StubHud(" ", 50, 20, 0, 0)));
    }

    @Test
    void computesDefaultBoundsAndPersistsTopLeftMoves() {
        StubHud hud = new StubHud("typeomega:kardashev", 100, 40, 5, 50);
        HudRegistry.register(hud);

        Rect2i initial = HudRegistry.bounds(hud, 500, 240);
        assertEquals(20, initial.getX());
        assertEquals(100, initial.getY());
        assertEquals(100, initial.getWidth());
        assertEquals(40, initial.getHeight());

        HudRegistry.setTopLeftPosition(hud, 200, 150, 500, 240);
        assertEquals(new HudPositionStore.Position(50, 75), HudRegistry.position(hud));

        Rect2i moved = HudRegistry.bounds(hud, 500, 240);
        assertEquals(200, moved.getX());
        assertEquals(150, moved.getY());
    }

    private static final class StubHud implements MoveableHud {

        private final String id;
        private final int width;
        private final int height;
        private final int defaultX;
        private final int defaultY;
        private boolean enabled;

        private StubHud(String id, int width, int height, int defaultX, int defaultY) {
            this.id = id;
            this.width = width;
            this.height = height;
            this.defaultX = defaultX;
            this.defaultY = defaultY;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public Component displayName() {
            return Component.literal(id);
        }

        @Override
        public int width() {
            return width;
        }

        @Override
        public int height() {
            return height;
        }

        @Override
        public void render(GuiGraphics graphics, int x, int y, int width, int height) {}

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public int defaultXPercent() {
            return defaultX;
        }

        @Override
        public int defaultYPercent() {
            return defaultY;
        }
    }
}
