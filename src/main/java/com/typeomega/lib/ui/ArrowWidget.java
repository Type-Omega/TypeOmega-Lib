package com.typeomega.lib.ui;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A white pixel arrow with a dark outline, solid or dashed, as the flow arrows drawn over the GTNH illustrations. A
 * dashed arrow moves its dashes along the flow.
 */
public class ArrowWidget extends Widget {

    public enum Direction {
        RIGHT,
        LEFT,
        UP,
        DOWN
    }

    private final Direction direction;
    private final boolean dashed;

    public ArrowWidget(int x, int y, int width, int height, Direction direction, boolean dashed) {
        super(x, y, width, height);
        this.direction = direction;
        this.dashed = dashed;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        boolean horizontal = direction == Direction.RIGHT || direction == Direction.LEFT;
        int length = horizontal ? getSizeWidth() : getSizeHeight();
        int shift = dashed ? (int) (System.currentTimeMillis() / 120 % 6) : 0;
        for (int along = 0; along < length - 4; along++) {
            if (dashed && (along + (forward() ? -shift : shift) + 60) % 6 >= 4) continue;
            plot(graphics, along, -1, 0xFF1E1E1E);
            plot(graphics, along, 0, 0xFFFFFFFF);
            plot(graphics, along, 1, 0xFF1E1E1E);
        }
        for (int i = 0; i < 4; i++) {
            for (int w = -i; w <= i; w++) plot(graphics, length - 1 - i, w, Math.abs(w) == i ? 0xFF1E1E1E : -1);
        }
    }

    private boolean forward() {
        return direction == Direction.RIGHT || direction == Direction.DOWN;
    }

    /** Plots a pixel at {@code along} the arrow from its tail and {@code across} its axis. */
    @OnlyIn(Dist.CLIENT)
    private void plot(GuiGraphics graphics, int along, int across, int colour) {
        int x0 = getPositionX(), y0 = getPositionY(), w = getSizeWidth(), h = getSizeHeight();
        int x, y;
        switch (direction) {
            case RIGHT -> {
                x = x0 + along;
                y = y0 + h / 2 + across;
            }
            case LEFT -> {
                x = x0 + w - 1 - along;
                y = y0 + h / 2 + across;
            }
            case DOWN -> {
                x = x0 + w / 2 + across;
                y = y0 + along;
            }
            default -> {
                x = x0 + w / 2 + across;
                y = y0 + h - 1 - along;
            }
        }
        graphics.fill(x, y, x + 1, y + 1, colour);
    }
}
