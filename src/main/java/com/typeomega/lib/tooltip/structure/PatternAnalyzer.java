package com.typeomega.lib.tooltip.structure;

import net.minecraft.network.chat.Component;

import com.typeomega.lib.tooltip.StructureInfo;
import com.typeomega.lib.tooltip.StructureInfo.Range;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Turns the positions of a multiblock pattern into the counts of the tooltip structure section (D-430). It
 * knows nothing about GTCEu: {@link GtceuPatterns} reads the pattern into {@link Slot}s, so the counting rules can be
 * tested without Minecraft.
 * <p>
 * Rules, per slot (the positions that share one predicate):
 * <ul>
 * <li>a block that is the only option fills every position: its count is the number of positions;</li>
 * <li>when hatches share the slot, the block keeps at least its pattern minimum (or the positions the hatches can't
 * take) and at most the positions left after the hatches' minimums;</li>
 * <li>a hatch counts its pattern minimum and maximum; without a maximum it shows as "N+".</li>
 * </ul>
 * Counts of the same block or hatch in different slots are added up.
 */
public final class PatternAnalyzer {

    private PatternAnalyzer() {}

    /**
     * One thing a position accepts.
     *
     * @param key     what merges counts across slots (the block, or the part ability name)
     * @param part    a hatch or bus rather than a block
     * @param min     the pattern's global minimum, or -1 when it sets none
     * @param max     the pattern's global maximum, or -1 when it sets none
     * @param tiered  a block accepting several tiers, such as coils
     * @param ability the part ability name, or {@code null}
     */
    public record Option(Object key, Component name, boolean part, int min, int max, boolean tiered,
                         String ability) {

        public static Option block(Object key, Component name, int min, int max, boolean tiered) {
            return new Option(key, name, false, min, max, tiered, null);
        }

        public static Option part(Object key, Component name, int min, int max, String ability) {
            return new Option(key, name, true, min, max, false, ability);
        }
    }

    /** Positions that share one predicate; {@code min} and {@code max} differ when aisles repeat. */
    public record Slot(int minPositions, int maxPositions, List<Option> options) {

        public Slot {
            options = List.copyOf(options);
        }
    }

    public static StructureInfo analyze(Range width, Range height, Range depth, Component controller,
                                        List<Slot> slots, boolean multiAmp, boolean wireless) {
        Map<Object, Counted> blocks = new LinkedHashMap<>();
        Map<Object, CountedPart> parts = new LinkedHashMap<>();
        Set<String> abilities = new LinkedHashSet<>();
        for (Slot slot : slots) {
            List<Option> casings = new ArrayList<>();
            List<Option> hatches = new ArrayList<>();
            for (Option option : slot.options()) (option.part() ? hatches : casings).add(option);
            int hatchMin = 0;
            long hatchMax = 0;
            for (Option hatch : hatches) {
                hatchMin += Math.max(0, hatch.min());
                hatchMax = hatch.max() < 0 || hatchMax == Range.UNBOUNDED ? Range.UNBOUNDED : hatchMax + hatch.max();
            }
            for (Option casing : casings) {
                int max = Math.max(0, slot.maxPositions() - hatchMin);
                int min;
                if (casing.min() >= 0) min = casing.min();
                else if (casings.size() > 1 || hatchMax == Range.UNBOUNDED) min = 0;
                else min = (int) Math.max(0, slot.minPositions() - hatchMax);
                if (casing.max() >= 0) max = Math.min(max, casing.max());
                if (max == 0) continue;
                blocks.computeIfAbsent(casing.key(), k -> new Counted(casing.name(), casing.tiered()))
                        .add(new Range(Math.min(min, max), max));
            }
            for (Option hatch : hatches) {
                int min = Math.max(0, hatch.min());
                int max;
                if (hatch.max() >= 0) max = Math.min(hatch.max(), slot.maxPositions());
                else if (casings.isEmpty() && hatches.size() == 1) {
                    min = slot.minPositions();
                    max = slot.maxPositions();
                } else max = Range.UNBOUNDED;
                CountedPart counted = parts.computeIfAbsent(hatch.key(), k -> new CountedPart(hatch.name()));
                counted.add(new Range(min, max));
                for (Option casing : casings) counted.where.putIfAbsent(casing.key(), casing.name());
                if (hatch.ability() != null) abilities.add(hatch.ability());
            }
        }
        List<StructureInfo.Block> blockList = new ArrayList<>();
        blocks.values().stream().sorted(Comparator.comparingInt((Counted c) -> c.range.max()).reversed())
                .forEach(c -> blockList.add(new StructureInfo.Block(c.range, c.name, c.tiered)));
        List<StructureInfo.Part> partList = new ArrayList<>();
        for (CountedPart part : parts.values()) {
            // EN: A hatch with its own positions and casing positions lists only the casings it may replace.
            // PT: Uma escotilha com posições próprias e posições de casing lista só os casings que ela substitui.
            // ZH: 同时拥有专用位置和外壳位置的仓室，只列出它可替换的外壳。
            partList.add(new StructureInfo.Part(part.range, part.name, List.copyOf(part.where.values())));
        }
        return new StructureInfo(width, height, depth, controller, blockList, partList, abilities, multiAmp, wireless);
    }

    private static final class Counted {

        final Component name;
        final boolean tiered;
        Range range = Range.exactly(0);

        Counted(Component name, boolean tiered) {
            this.name = name;
            this.tiered = tiered;
        }

        void add(Range more) {
            range = range.plus(more);
        }
    }

    private static final class CountedPart {

        final Component name;
        final Map<Object, Component> where = new LinkedHashMap<>();
        Range range = Range.exactly(0);

        CountedPart(Component name) {
            this.name = name;
        }

        void add(Range more) {
            range = range.plus(more);
        }
    }
}
