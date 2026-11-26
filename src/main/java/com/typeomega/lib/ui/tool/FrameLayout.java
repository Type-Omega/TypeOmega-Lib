package com.typeomega.lib.ui.tool;

import java.util.ArrayList;
import java.util.List;

/** The geometry of a {@link PixelFrame}: plain rectangles, so a unit test can check it without a client. */
public final class FrameLayout {

    public static final int TOP = 1, BOTTOM = 2, LEFT = 4, RIGHT = 8, ALL = TOP | BOTTOM | LEFT | RIGHT;

    /** One solid rectangle, relative to the frame's corner. */
    public record Rect(int x, int y, int w, int h, int colour) {}

    private FrameLayout() {}

    /**
     * The rectangles of a frame {@code w} by {@code h}: the fill first (skipped when fully transparent), then every
     * 1-pixel ring from the outside in, only on the sides in the mask. A side without a ring lets the neighbouring
     * rings run to the edge.
     */
    public static List<Rect> layout(int fill, int sides, int[] rings, int w, int h) {
        List<Rect> out = new ArrayList<>();
        if ((fill >>> 24) != 0) add(out, 0, 0, w, h, fill);
        for (int i = 0; i < rings.length; i++) {
            int c = rings[i];
            int x0 = (sides & LEFT) != 0 ? i : 0, x1 = w - ((sides & RIGHT) != 0 ? i : 0);
            int y0 = (sides & TOP) != 0 ? i : 0, y1 = h - ((sides & BOTTOM) != 0 ? i : 0);
            if ((sides & TOP) != 0) add(out, x0, i, x1 - x0, 1, c);
            if ((sides & BOTTOM) != 0) add(out, x0, h - 1 - i, x1 - x0, 1, c);
            if ((sides & LEFT) != 0) add(out, i, y0, 1, y1 - y0, c);
            if ((sides & RIGHT) != 0) add(out, w - 1 - i, y0, 1, y1 - y0, c);
        }
        return out;
    }

    private static void add(List<Rect> out, int x, int y, int w, int h, int colour) {
        if (w > 0 && h > 0) out.add(new Rect(x, y, w, h, colour));
    }
}
