package com.typeomega.lib.ui;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.platform.NativeImage;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Animates an arrow or pipe that is painted in the panel art, GTNH style: the white pixels of the art inside the
 * widget are drawn grey, and a white fill runs along them from tail to head, then starts again. The art is not
 * changed; a layout only marks where the arrow is ({@code flow_right}, {@code flow_left}, {@code flow_down},
 * {@code flow_up}).
 */
public class FlowArrowWidget extends Widget {

    /** Mark ids that become flow arrows start with this. */
    public static final String MARK_PREFIX = "flow_";
    private static final int PERIOD_MS = 2000;
    private static final int EMPTY = 0xFF8B8B8B;
    private static final int FULL = 0xFFFFFFFF;
    private static final Map<ResourceLocation, int[][]> ART = new ConcurrentHashMap<>();

    private final ResourceLocation art;
    private final ArrowWidget.Direction direction;
    /** Top-left of the widget inside the art, in art pixels. */
    private final int artX, artY;

    public FlowArrowWidget(ResourceLocation art, PanelLayout.Mark mark) {
        super(0, 0, mark.width(), mark.height());
        this.art = art;
        this.direction = direction(mark.id());
        this.artX = mark.x();
        this.artY = mark.y();
        setClientSideWidget();
    }

    static ArrowWidget.Direction direction(String id) {
        return switch (id.substring(MARK_PREFIX.length())) {
            case "left" -> ArrowWidget.Direction.LEFT;
            case "down" -> ArrowWidget.Direction.DOWN;
            case "up" -> ArrowWidget.Direction.UP;
            default -> ArrowWidget.Direction.RIGHT;
        };
    }

    /** Fill fraction at {@code ms}, from 0 to 1 over one period. */
    static double progress(long ms) {
        return (ms % PERIOD_MS) / (double) PERIOD_MS;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int[][] pixels = ART.computeIfAbsent(art, FlowArrowWidget::load);
        if (pixels.length == 0) return;
        int w = getSizeWidth(), h = getSizeHeight(), x0 = getPositionX(), y0 = getPositionY();
        boolean horizontal = direction == ArrowWidget.Direction.RIGHT || direction == ArrowWidget.Direction.LEFT;
        int length = horizontal ? w : h;
        int filled = (int) Math.round(progress(System.currentTimeMillis()) * length);
        for (int dy = 0; dy < h; dy++) {
            int ay = artY + dy;
            if (ay < 0 || ay >= pixels.length) continue;
            for (int dx = 0; dx < w; dx++) {
                int ax = artX + dx;
                if (ax < 0 || ax >= pixels[ay].length || pixels[ay][ax] != FULL) continue;
                int along = switch (direction) {
                    case RIGHT -> dx;
                    case LEFT -> w - 1 - dx;
                    case DOWN -> dy;
                    case UP -> h - 1 - dy;
                };
                graphics.fill(x0 + dx, y0 + dy, x0 + dx + 1, y0 + dy + 1, along < filled ? FULL : EMPTY);
            }
        }
    }

    /** The art as ARGB rows; empty when it cannot be read. */
    @OnlyIn(Dist.CLIENT)
    private static int[][] load(ResourceLocation art) {
        var resource = Minecraft.getInstance().getResourceManager().getResource(art);
        if (resource.isEmpty()) return new int[0][];
        try (InputStream in = resource.get().open(); NativeImage image = NativeImage.read(in)) {
            int[][] out = new int[image.getHeight()][image.getWidth()];
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    // NativeImage stores ABGR.
                    int abgr = image.getPixelRGBA(x, y);
                    out[y][x] = (abgr & 0xFF00FF00) | (abgr & 0xFF) << 16 | (abgr >> 16 & 0xFF);
                }
            }
            return out;
        } catch (Exception e) {
            return new int[0][];
        }
    }
}
