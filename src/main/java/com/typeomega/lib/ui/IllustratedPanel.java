package com.typeomega.lib.ui;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * An illustrated panel (GTIA-D-435): the GTCEu bevelled grey background, the picture of the process, the slots and
 * tanks placed inside it and the text lines under it. The same widget serves the EMI recipe page and the machine GUI;
 * only the slot widgets differ, so they come from a factory.
 */
public class IllustratedPanel extends WidgetGroup {

    private final PanelLayout layout;
    private final @Nullable ResourceTexture art;

    public IllustratedPanel(PanelLayout layout, int lines) {
        super(0, 0, layout.panelWidth(), layout.panelHeight(lines));
        this.layout = layout;
        this.art = layout.art() == null ? null : new ResourceTexture(layout.art());
    }

    public PanelLayout layout() {
        return layout;
    }

    /** Adds one widget per layout slot; the factory gets the slot and returns a widget, or null to leave it empty. */
    public IllustratedPanel slots(Function<PanelLayout.Slot, @Nullable Widget> factory) {
        for (var slot : layout.slots()) {
            Widget widget = factory.apply(slot);
            if (widget == null) continue;
            widget.setSelfPosition(PanelLayout.PAD + slot.x(), PanelLayout.PAD + slot.y());
            addWidget(widget);
        }
        return this;
    }

    /** Adds a widget at a mark of the layout, sized to it. */
    public IllustratedPanel atMark(String id, Function<PanelLayout.Mark, Widget> factory) {
        for (var mark : layout.marks()) {
            if (!mark.id().equals(id)) continue;
            Widget widget = factory.apply(mark);
            widget.setSelfPosition(PanelLayout.PAD + mark.x(), PanelLayout.PAD + mark.y());
            widget.setSize(mark.width(), mark.height());
            addWidget(widget);
        }
        return this;
    }

    /** Animates every arrow the layout marks with a {@code flow_} id ({@link FlowArrowWidget}). */
    public IllustratedPanel flows() {
        if (layout.art() == null) return this;
        for (var mark : layout.marks()) {
            if (!mark.id().startsWith(FlowArrowWidget.MARK_PREFIX)) continue;
            var widget = new FlowArrowWidget(layout.art(), mark);
            widget.setSelfPosition(PanelLayout.PAD + mark.x(), PanelLayout.PAD + mark.y());
            addWidget(widget);
        }
        return this;
    }

    /** Puts the text lines under the picture. */
    public IllustratedPanel lines(InfoLinesWidget lines) {
        lines.setSelfPosition(PanelLayout.PAD + 1, layout.linesTop());
        lines.setSize(layout.width() - 2, getSizeHeight() - layout.linesTop() - PanelLayout.PAD);
        addWidget(lines);
        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = getPositionX(), y = getPositionY();
        GuiTextures.BACKGROUND.draw(graphics, mouseX, mouseY, x, y, getSizeWidth(), getSizeHeight());
        if (art != null) {
            art.draw(graphics, mouseX, mouseY, x + PanelLayout.PAD, y + PanelLayout.PAD, layout.width(),
                    layout.height());
        }
        drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
    }
}
