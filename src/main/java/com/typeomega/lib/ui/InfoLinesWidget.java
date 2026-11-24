package com.typeomega.lib.ui;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * The text lines of an illustrated panel, dark on the grey background as in GTNH. Fixed lines for a recipe page; on a
 * machine GUI a server-side supplier is read every tick and the lines are synced when they change.
 */
public class InfoLinesWidget extends Widget {

    public static final int TEXT_COLOUR = 0xFF2B2B2B;

    private List<Component> lines;
    private final @Nullable Supplier<List<Component>> server;

    public InfoLinesWidget(List<Component> lines) {
        super(0, 0, 10, lines.size() * PanelLayout.LINE_HEIGHT);
        this.lines = List.copyOf(lines);
        this.server = null;
    }

    /** Lines that change while the machine runs; the supplier is only read on the server. */
    public InfoLinesWidget(Supplier<List<Component>> server, int maxLines) {
        super(0, 0, 10, maxLines * PanelLayout.LINE_HEIGHT);
        this.lines = List.of();
        this.server = server;
    }

    public List<Component> lines() {
        return lines;
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        if (server != null) lines = List.copyOf(server.get());
        write(buffer, lines);
    }

    @Override
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        lines = read(buffer);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (server == null) return;
        var now = List.copyOf(server.get());
        if (now.equals(lines)) return;
        lines = now;
        writeUpdateInfo(1, buffer -> write(buffer, now));
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 1) lines = read(buffer);
        else super.readUpdateInfo(id, buffer);
    }

    private static void write(FriendlyByteBuf buffer, List<Component> lines) {
        buffer.writeVarInt(lines.size());
        for (var line : lines) buffer.writeComponent(line);
    }

    private static List<Component> read(FriendlyByteBuf buffer) {
        int n = buffer.readVarInt();
        List<Component> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) out.add(buffer.readComponent());
        return List.copyOf(out);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        var font = Minecraft.getInstance().font;
        int y = getPositionY() + 1;
        for (var line : lines) {
            graphics.drawString(font, line, getPositionX(), y, TEXT_COLOUR, false);
            y += PanelLayout.LINE_HEIGHT;
        }
    }
}
