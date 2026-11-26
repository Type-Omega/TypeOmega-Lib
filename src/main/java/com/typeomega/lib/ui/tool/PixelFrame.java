package com.typeomega.lib.ui.tool;

import com.lowdragmc.lowdraglib.gui.texture.TransformTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;

import net.minecraft.client.gui.GuiGraphics;

/**
 * A flat frame drawn with solid rectangles: a fill and one or more 1-pixel rings, outermost first, on the sides
 * picked by a mask. The same idea as LDLib's {@code ColorRectTexture} plus {@code ColorBorderTexture}, but with
 * rings that can skip a side (a selected tab opens onto its page); the geometry is {@link FrameLayout}, which a
 * unit test can check without a client.
 * <p>
 * EN: sizes are in GUI pixels, so the rings stay crisp at any GUI scale.
 * PT: os tamanhos são em pixels de GUI, então os anéis ficam nítidos em qualquer escala.
 * ZH: 尺寸以 GUI 像素计，因此在任何 GUI 缩放下边框都清晰。
 */
public final class PixelFrame extends TransformTexture {

    public static final int TOP = FrameLayout.TOP, BOTTOM = FrameLayout.BOTTOM, LEFT = FrameLayout.LEFT,
            RIGHT = FrameLayout.RIGHT, ALL = FrameLayout.ALL;

    private final int fill, sides;
    private final int[] rings;

    public PixelFrame(int fill, int sides, int... rings) {
        this.fill = fill;
        this.sides = sides;
        this.rings = rings.clone();
    }

    public static PixelFrame of(int fill, int... rings) {
        return new PixelFrame(fill, ALL, rings);
    }

    @Override
    protected void drawInternal(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width,
                                int height) {
        int ox = Math.round(x), oy = Math.round(y);
        for (FrameLayout.Rect r : FrameLayout.layout(fill, sides, rings, width, height)) {
            DrawerHelper.drawSolidRect(graphics, ox + r.x(), oy + r.y(), r.w(), r.h(), r.colour());
        }
    }
}
