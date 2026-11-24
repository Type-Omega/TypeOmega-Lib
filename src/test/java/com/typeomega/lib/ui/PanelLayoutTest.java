package com.typeomega.lib.ui;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PanelLayoutTest {

    private static PanelLayout.Builder tokamakLike() {
        return PanelLayout.builder(160, 80)
                .fluid(PanelLayout.Role.INPUT, 4, 4)
                .fluid(PanelLayout.Role.INPUT, 4, 22)
                .item(PanelLayout.Role.INPUT, 4, 58)
                .fluid(PanelLayout.Role.OUTPUT, 138, 13)
                .mark("progress", 70, 60, 20, 16);
    }

    @Test
    void soundLayoutHasNoProblems() {
        var layout = tokamakLike().build();
        assertEquals(List.of(), layout.problems());
        assertEquals(168, layout.panelWidth());
        assertEquals(4 + 80 + 3, layout.linesTop());
        assertEquals(layout.linesTop() + 5 * 10 + 4, layout.panelHeight(5));
    }

    @Test
    void indexesCountPerKindAndRole() {
        var layout = tokamakLike().build();
        var fluidsIn = layout.slots(PanelLayout.Kind.FLUID, PanelLayout.Role.INPUT);
        assertEquals(2, fluidsIn.size());
        assertEquals(0, fluidsIn.get(0).index());
        assertEquals(1, fluidsIn.get(1).index());
        assertEquals(0, layout.slots(PanelLayout.Kind.ITEM, PanelLayout.Role.INPUT).get(0).index());
        assertEquals(0, layout.slots(PanelLayout.Kind.FLUID, PanelLayout.Role.OUTPUT).get(0).index());
    }

    @Test
    void slotOutsideThePictureIsReported() {
        var layout = tokamakLike().item(PanelLayout.Role.OUTPUT, 150, 70).build();
        assertTrue(layout.problems().stream().anyMatch(p -> p.contains("outside")), layout.problems().toString());
    }

    @Test
    void overlappingSlotsAreReported() {
        var layout = tokamakLike().item(PanelLayout.Role.OUTPUT, 10, 10).build();
        assertTrue(layout.problems().stream().anyMatch(p -> p.contains("overlap")), layout.problems().toString());
    }

    @Test
    void markOutsideIsReported() {
        var layout = tokamakLike().mark("arrow", 150, 0, 20, 8).build();
        assertTrue(layout.problems().stream().anyMatch(p -> p.contains("mark arrow")));
    }

    @Test
    void emptySizeIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> PanelLayout.builder(0, 10));
    }
}
