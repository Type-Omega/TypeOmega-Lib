package com.typeomega.lib.ui;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IllustratedPanelTest {

    private static PanelLayout layout() {
        return PanelLayout.builder(160, 80)
                .fluid(PanelLayout.Role.INPUT, 4, 4)
                .fluid(PanelLayout.Role.INPUT, 4, 24)
                .item(PanelLayout.Role.CATALYST, 70, 30)
                .fluid(PanelLayout.Role.OUTPUT, 138, 13)
                .mark("progress", 100, 60, 20, 16)
                .build();
    }

    private static void assertInside(Widget parent, Widget child) {
        var p = parent.getSelfPosition();
        var c = child.getSelfPosition();
        assertTrue(c.x >= 0 && c.y >= 0, "negative position " + c);
        assertTrue(c.x + child.getSizeWidth() <= parent.getSizeWidth(), "past the right edge: " + c);
        assertTrue(c.y + child.getSizeHeight() <= parent.getSizeHeight(), "past the bottom edge: " + c);
        assertEquals(0, p.x);
    }

    @Test
    void panelMountsEverySlotInsideThePicture() {
        var panel = new IllustratedPanel(layout(), 4);
        panel.slots(slot -> new Widget(0, 0, PanelLayout.SLOT, PanelLayout.SLOT));
        panel.atMark("progress", mark -> new Widget(0, 0, 1, 1));
        panel.lines(new InfoLinesWidget(List.of()));
        assertEquals(4 + 1 + 1, panel.widgets.size());
        for (var child : panel.widgets) assertInside(panel, child);
        assertEquals(layout().panelWidth(), panel.getSizeWidth());
        assertEquals(layout().panelHeight(4), panel.getSizeHeight());
    }

    @Test
    void slotsSitOverTheirPlaceInThePicture() {
        var panel = new IllustratedPanel(layout(), 3);
        panel.slots(slot -> slot.kind() == PanelLayout.Kind.ITEM ? new Widget(0, 0, 18, 18) : null);
        assertEquals(1, panel.widgets.size());
        var pos = panel.widgets.get(0).getSelfPosition();
        assertEquals(PanelLayout.PAD + 70, pos.x);
        assertEquals(PanelLayout.PAD + 30, pos.y);
    }

    @Test
    void markWidgetsTakeTheMarkSize() {
        var panel = new IllustratedPanel(layout(), 3);
        panel.atMark("progress", mark -> new Widget(0, 0, 1, 1));
        var w = panel.widgets.get(0);
        assertEquals(20, w.getSizeWidth());
        assertEquals(16, w.getSizeHeight());
    }

    @Test
    void linesStartUnderThePicture() {
        var panel = new IllustratedPanel(layout(), 3);
        var lines = new InfoLinesWidget(List.of());
        panel.lines(lines);
        assertEquals(layout().linesTop(), lines.getSelfPosition().y);
        assertEquals(3 * PanelLayout.LINE_HEIGHT, lines.getSizeHeight());
    }

    @Test
    void displaySlotRolesMapToXeiRoles() {
        assertEquals(com.lowdragmc.lowdraglib.jei.IngredientIO.INPUT, DisplaySlots.io(PanelLayout.Role.INPUT));
        assertEquals(com.lowdragmc.lowdraglib.jei.IngredientIO.OUTPUT, DisplaySlots.io(PanelLayout.Role.OUTPUT));
        assertEquals(com.lowdragmc.lowdraglib.jei.IngredientIO.CATALYST,
                DisplaySlots.io(PanelLayout.Role.CATALYST));
    }

    @Test
    void progressBarClampsItsValue() {
        assertEquals(0, ProgressBarWidget.clamp(-1));
        assertEquals(1, ProgressBarWidget.clamp(5));
        assertEquals(0, ProgressBarWidget.clamp(Double.NaN));
        assertEquals(0.25, ProgressBarWidget.clamp(0.25));
    }

    @Test
    void particleShadesStayOpaque() {
        assertEquals(0xFF000000, ParticleWidget.shade(0xFF000000, 2) & 0xFF000000);
        assertEquals(0xFFFFFFFF, ParticleWidget.shade(0xFFFFFFFF, 2));
        assertEquals(0xFF402010, ParticleWidget.shade(0xFF804020, 0.5F));
    }
}
