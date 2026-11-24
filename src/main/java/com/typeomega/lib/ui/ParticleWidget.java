package com.typeomega.lib.ui;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A round coloured badge with a symbol, like the particle icons of the GTNH Beam Crafter and Hadron Collider pages.
 * Drawn from code, so a mod gets one per particle or mode without a texture.
 */
public class ParticleWidget extends Widget {

    private final int colour;
    private final String symbol;

    public ParticleWidget(int x, int y, int colour, String symbol, Component... tooltip) {
        super(x, y, 16, 16);
        this.colour = 0xFF000000 | colour;
        this.symbol = symbol;
        setHoverTooltips(tooltip);
    }

    /** Pixel rows of a 14 px disc: the half-width of each row from the top. */
    static final int[] DISC = { 3, 5, 6, 6, 7, 7, 7, 7, 7, 7, 6, 6, 5, 3 };

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int cx = getPositionX() + 8, top = getPositionY() + 1;
        int dark = shade(colour, 0.45F), light = shade(colour, 1.45F);
        for (int row = 0; row < DISC.length; row++) {
            int half = DISC[row];
            graphics.fill(cx - half - 1, top + row, cx + half + 1, top + row + 1, 0xFF1E1E1E);
        }
        for (int row = 1; row < DISC.length - 1; row++) {
            int half = DISC[row] - 1;
            int fill = row < 4 ? light : row > 10 ? dark : colour;
            graphics.fill(cx - half - 1, top + row, cx + half + 1, top + row + 1, fill);
        }
        graphics.fill(cx - 4, top + 3, cx - 2, top + 5, 0xFFFFFFFF);
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, symbol, cx - font.width(symbol) / 2, top + 3, 0xFFFFFFFF, true);
    }

    static int shade(int argb, float factor) {
        int r = Math.min(255, Math.round((argb >> 16 & 0xFF) * factor));
        int g = Math.min(255, Math.round((argb >> 8 & 0xFF) * factor));
        int b = Math.min(255, Math.round((argb & 0xFF) * factor));
        return 0xFF000000 | r << 16 | g << 8 | b;
    }
}
