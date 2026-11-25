package com.typeomega.lib.ui.report;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.typeomega.lib.ui.UiTexts;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Shows a {@link Report} on the GTCEu grey: a filter button per group, groups that fold on a click of their header,
 * a scrolling list, and a click on a line with a position copies "x y z" to the clipboard. An optional action (for
 * example "show boxes in the world") gets the groups that are visible.
 */
@OnlyIn(Dist.CLIENT)
public class ReportScreen extends Screen {

    private static final int ROW = 10;
    private static final int TEXT = 0xFF2B2B2B;

    private final Report report;
    private final @Nullable Consumer<List<Report.Group>> show;
    private final Set<Integer> hidden = new HashSet<>();
    private final Set<Integer> folded = new HashSet<>();
    private int scroll;
    private int left, top, w, h, listTop;

    public ReportScreen(Report report, @Nullable Consumer<List<Report.Group>> show) {
        super(report.title());
        this.report = report;
        this.show = show;
    }

    @Override
    protected void init() {
        w = Math.min(360, width - 16);
        h = Math.min(240, height - 16);
        left = (width - w) / 2;
        top = (height - h) / 2;
        int x = left + 6, y = top + 18;
        for (int i = 0; i < report.groups().size(); i++) {
            int index = i;
            var group = report.groups().get(i);
            var label = Component.literal(group.total() + " ").append(group.name());
            int bw = Math.min(110, font.width(label) + 10);
            if (x + bw > left + w - 6) {
                x = left + 6;
                y += 16;
            }
            addRenderableWidget(Button.builder(label, b -> {
                if (!hidden.remove(index)) hidden.add(index);
            }).bounds(x, y, bw, 14).build());
            x += bw + 2;
        }
        listTop = y + 18;
        if (show != null) {
            addRenderableWidget(Button.builder(UiTexts.REPORT_SHOW.component(), b -> show.accept(visibleGroups()))
                    .bounds(left + w - 86, top + 3, 80, 13).build());
        }
    }

    private List<Report.Group> visibleGroups() {
        List<Report.Group> out = new ArrayList<>();
        for (int i = 0; i < report.groups().size(); i++) if (!hidden.contains(i)) out.add(report.groups().get(i));
        return out;
    }

    /** The rows shown now: a header per visible group, then its lines unless folded. */
    private List<Row> rows() {
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < report.groups().size(); i++) {
            if (hidden.contains(i)) continue;
            var group = report.groups().get(i);
            rows.add(new Row(i, null));
            if (folded.contains(i)) continue;
            for (var line : group.lines()) rows.add(new Row(i, line));
        }
        return rows;
    }

    private record Row(int group, @Nullable Report.Line line) {}

    private int visibleRows() {
        return Math.max(1, (top + h - 6 - listTop) / ROW);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        GuiTextures.BACKGROUND.draw(graphics, mouseX, mouseY, left, top, w, h);
        graphics.drawString(font, title, left + 6, top + 6, TEXT, false);
        GuiTextures.DISPLAY.draw(graphics, mouseX, mouseY, left + 4, listTop - 2, w - 8, top + h - listTop - 2);
        var rows = rows();
        scroll = Math.max(0, Math.min(scroll, rows.size() - visibleRows()));
        int y = listTop;
        for (int i = scroll; i < rows.size() && i < scroll + visibleRows(); i++, y += ROW) {
            var row = rows.get(i);
            var group = report.groups().get(row.group);
            if (row.line == null) {
                String mark = folded.contains(row.group) ? "+ " : "- ";
                graphics.drawString(font, Component.literal(mark).append(group.name())
                        .append(" (" + group.total() + ")"), left + 8, y, group.colour() | 0xFF000000, false);
            } else {
                graphics.drawString(font, row.line.text(), left + 16, y, 0xFFE0E0E0, false);
            }
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (mouseY < listTop || mouseX < left || mouseX > left + w) return false;
        int index = scroll + (int) ((mouseY - listTop) / ROW);
        var rows = rows();
        if (index >= rows.size()) return false;
        var row = rows.get(index);
        if (row.line == null) {
            if (!folded.remove(row.group)) folded.add(row.group);
        } else if (row.line.pos() != null) {
            var p = row.line.pos();
            Minecraft.getInstance().keyboardHandler.setClipboard(p.getX() + " " + p.getY() + " " + p.getZ());
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.displayClientMessage(UiTexts.REPORT_COPIED.component(), true);
            }
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scroll -= (int) Math.signum(delta) * 3;
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
