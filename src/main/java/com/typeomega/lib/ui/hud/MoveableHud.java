package com.typeomega.lib.ui.hud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A client HUD overlay that can be registered in {@link HudRegistry}, toggled on or off, and
 * dragged to a persisted screen-percentage position in {@link HudEditorScreen}.
 */
@OnlyIn(Dist.CLIENT)
public interface MoveableHud {

    /** Stable identifier used to save this HUD's position (for example {@code "gtna:wireless_energy"}). */
    String id();

    /** Human-readable label shown on the toggle button in {@link HudEditorScreen}. */
    Component displayName();

    /** Current panel width in GUI pixels. */
    int width();

    /** Current panel height in GUI pixels. */
    int height();

    /** Draws the HUD at the given top-left GUI coordinates and size. */
    void render(GuiGraphics graphics, int x, int y, int width, int height);

    /** Whether the player has enabled this HUD. */
    boolean isEnabled();

    /** Turns this HUD on or off and persists the choice in the owning mod. */
    void setEnabled(boolean enabled);

    /**
     * Extra in-game visibility check on top of {@link #isEnabled()} (for example hiding until a
     * network snapshot arrives). {@link HudEditorScreen} ignores this so enabled HUDs can still be
     * dragged before data arrives.
     */
    default boolean shouldRenderInGame() {
        return true;
    }

    /** Default horizontal anchor percentage ({@code 0..100}) used before the player moves this HUD. */
    default int defaultXPercent() {
        return 5;
    }

    /** Default vertical anchor percentage ({@code 0..100}) used before the player moves this HUD. */
    default int defaultYPercent() {
        return 5;
    }

    static void drawOutline(GuiGraphics graphics, Rect2i bounds, int color) {
        if (bounds == null) return;
        drawOutline(graphics, bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), color);
    }

    static void drawOutline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        if (width <= 0 || height <= 0) return;
        int right = x + width - 1;
        int bottom = y + height - 1;
        graphics.hLine(x, right, y, color);
        graphics.hLine(x, right, bottom, color);
        graphics.vLine(x, y, bottom, color);
        graphics.vLine(right, y, bottom, color);
    }
}
