package com.typeomega.lib.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import com.typeomega.lib.lang.Tr;
import com.typeomega.lib.tooltip.StructureInfo.Range;
import com.typeomega.lib.tooltip.structure.PatternAnalyzer;
import com.typeomega.lib.tooltip.structure.PatternAnalyzer.Option;
import com.typeomega.lib.tooltip.structure.PatternAnalyzer.Slot;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StructureSectionTest {

    private static final Tr TYPE = Tr.of("test.structure.type", "Macerator", "Macerador", "研磨机");
    private static final Component CASING = Component.literal("Stable Titanium Casing");
    private static final Component GLASS = Component.literal("Glass");
    private static final Component ENERGY = Component.literal("Energy Hatch");
    private static final Component BUS = Component.literal("Input Bus");

    /**
     * The GTNH Industrial Maceration Stack T1 casing slot in GTCEu terms: 44 casing positions with a minimum of 26,
     * energy 1-2, maintenance exactly 1, input buses without a limit.
     */
    private static StructureInfo macerator() {
        Slot casingSlot = new Slot(44, 44, List.of(
                Option.block("casing", CASING, 26, -1, false),
                Option.part("input_energy", ENERGY, 1, 2, "input_energy"),
                Option.part("import_items", BUS, -1, -1, "import_items")));
        Slot glassSlot = new Slot(8, 8, List.of(Option.block("glass", GLASS, -1, -1, true)));
        Slot moreCasing = new Slot(4, 12, List.of(Option.block("casing", CASING, -1, -1, false)));
        return PatternAnalyzer.analyze(Range.exactly(3), Range.exactly(6), new Range(3, 5),
                Component.literal("Front, bottom, center"), List.of(casingSlot, glassSlot, moreCasing), false, false);
    }

    @Test
    void blockCountsComeFromTheSlots() {
        StructureInfo info = macerator();
        StructureInfo.Block casing = info.blocks().get(0);
        assertEquals("Stable Titanium Casing", casing.name().getString());
        // 26..43 in the hatch slot (44 minus the energy minimum) plus 4..12 from the repeated aisle
        assertEquals("30-55", casing.count().text());
        StructureInfo.Block glass = info.blocks().get(1);
        assertEquals("8", glass.count().text());
        assertTrue(glass.tiered());
    }

    @Test
    void hatchCountsAndPlaces() {
        StructureInfo info = macerator();
        assertEquals("1-2", info.parts().get(0).count().text());
        assertEquals("0+", info.parts().get(1).count().text());
        assertEquals("Stable Titanium Casing", info.parts().get(0).where().get(0).getString());
        assertEquals(java.util.Set.of("input_energy", "import_items"), info.abilities());
    }

    @Test
    void dedicatedHatchPositionsCountExactly() {
        Slot own = new Slot(2, 2, List.of(Option.part("steam", Component.literal("Steam Hatch"), -1, -1, "steam")));
        StructureInfo info = PatternAnalyzer.analyze(Range.exactly(1), Range.exactly(1), Range.exactly(1), null,
                List.of(own), false, false);
        assertEquals("2", info.parts().get(0).count().text());
        assertTrue(info.parts().get(0).where().isEmpty());
    }

    @Test
    void casingWithoutMinimumLeavesRoomForLimitedHatches() {
        Slot slot = new Slot(10, 10, List.of(Option.block("c", CASING, -1, -1, false),
                Option.part("muffler", Component.literal("Muffler"), 1, 1, "muffler")));
        StructureInfo info = PatternAnalyzer.analyze(Range.exactly(1), Range.exactly(1), Range.exactly(1), null,
                List.of(slot), false, false);
        assertEquals("9", info.blocks().get(0).count().text());
        assertEquals("1", info.parts().get(0).count().text());
    }

    @Test
    void sectionLinesFollowGtnhOrder() {
        List<Component> lines = MultiblockTooltip.builder().machineType(TYPE).separator().structure(macerator())
                .hatches(macerator(), Hatch.WIRELESS).mod("M").build();
        assertEquals("typeomegalib.tooltip.structure.dimensions", key(lines.get(2)));
        Component size = (Component) args(lines.get(2))[0];
        assertEquals("3x6x3-5", size.getString());
        assertEquals("typeomegalib.tooltip.structure.header", key(lines.get(3)));
        assertEquals("   ", literal(lines.get(4)));
        assertEquals("typeomegalib.tooltip.structure.controller", key(lines.get(4).getSiblings().get(0)));
        assertEquals("   30-55x Stable Titanium Casing", literal(lines.get(5)));
        assertEquals("typeomegalib.tooltip.structure.tiered",
                key(lines.get(6).getSiblings().get(0).getSiblings().get(2)));
        assertEquals("   1-2 Energy Hatch: ", literal(lines.get(7)));
        assertEquals("typeomegalib.tooltip.supports", key(lines.get(9)));
    }

    @Test
    void supportsLineReadsLaserAndMultiAmp() {
        StructureInfo info = new StructureInfo(Range.exactly(1), Range.exactly(1), Range.exactly(1), null, List.of(),
                List.of(), java.util.Set.of("input_laser"), true, true);
        List<Component> lines = MultiblockTooltip.builder().machineType(TYPE).hatches(info).mod("M").build();
        Component list = (Component) args(lines.get(1))[0];
        assertEquals("typeomegalib.tooltip.list.and", key(list));
        StructureInfo none = new StructureInfo(Range.exactly(1), Range.exactly(1), Range.exactly(1), null, List.of(),
                List.of(), java.util.Set.of(), false, false);
        assertEquals(2, MultiblockTooltip.builder().machineType(TYPE).hatches(none).mod("M").build().size());
    }

    @Test
    void structureLinesNeedTheHeader() {
        assertThrows(IllegalStateException.class,
                () -> MultiblockTooltip.builder().machineType(TYPE).casing(1, CASING));
    }

    @Test
    void rangesFormatLikeGtnh() {
        assertEquals("8", Range.exactly(8).text());
        assertEquals("26-44", new Range(26, 44).text());
        assertEquals("1+", new Range(1, Range.UNBOUNDED).text());
        assertEquals(Range.UNBOUNDED, new Range(1, Range.UNBOUNDED).plus(Range.exactly(3)).max());
        assertFalse(StructureTexts.partName("import_fluids_4x") == null);
    }

    @Test
    void pollutionIsSkippedWhenZero() {
        assertEquals(2, MultiblockTooltip.builder().machineType(TYPE).pollution(0).mod("M").build().size());
        assertEquals(3, MultiblockTooltip.builder().machineType(TYPE).pollution(400).mod("M").build().size());
    }

    /** The literal text of a line, without resolving translations (no Minecraft language in unit tests). */
    private static String literal(Component c) {
        if (c.getContents() instanceof TranslatableContents) return "";
        StringBuilder out = new StringBuilder();
        c.getContents().visit(text -> {
            out.append(text);
            return java.util.Optional.empty();
        });
        for (Component sibling : c.getSiblings()) out.append(literal(sibling));
        return out.toString();
    }

    private static String key(Component c) {
        return ((TranslatableContents) c.getContents()).getKey();
    }

    private static Object[] args(Component c) {
        return ((TranslatableContents) c.getContents()).getArgs();
    }
}
