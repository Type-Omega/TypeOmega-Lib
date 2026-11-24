package com.typeomega.lib.ui;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.DoubleSupplier;

/**
 * A flat GTNH-style bar (dark frame, coloured fill) for start-up charge, stability, heat... The supplier is read on
 * the server and the value synced; on a client-only page it is read directly.
 */
public class ProgressBarWidget extends Widget {

    private final DoubleSupplier progress;
    private final int colour;
    private double value;

    public ProgressBarWidget(int x, int y, int width, int height, DoubleSupplier progress, int colour) {
        super(x, y, width, height);
        this.progress = progress;
        this.colour = 0xFF000000 | colour;
    }

    public double value() {
        return value;
    }

    static double clamp(double v) {
        return Double.isNaN(v) ? 0 : Math.max(0, Math.min(1, v));
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        value = clamp(progress.getAsDouble());
        buffer.writeDouble(value);
    }

    @Override
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        value = buffer.readDouble();
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        double now = clamp(progress.getAsDouble());
        if (Math.abs(now - value) < 0.002) return;
        value = now;
        writeUpdateInfo(1, buffer -> buffer.writeDouble(now));
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 1) value = buffer.readDouble();
        else super.readUpdateInfo(id, buffer);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (isClientSideWidget) value = clamp(progress.getAsDouble());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = getPositionX(), y = getPositionY(), w = getSizeWidth(), h = getSizeHeight();
        graphics.fill(x, y, x + w, y + h, 0xFF1E1E1E);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF555555);
        int fill = (int) Math.round((w - 2) * value);
        if (fill > 0) {
            graphics.fill(x + 1, y + 1, x + 1 + fill, y + h - 1, colour);
            graphics.fill(x + 1, y + 1, x + 1 + fill, y + 2, ParticleWidget.shade(colour, 1.4F));
        }
    }
}
