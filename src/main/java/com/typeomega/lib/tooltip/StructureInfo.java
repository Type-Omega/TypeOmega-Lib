package com.typeomega.lib.tooltip;

import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Set;

/**
 * What the structure section of a multiblock tooltip shows (D-430): the dimensions, where the controller goes,
 * every block with how many the structure needs, and every hatch with how many it takes and where it may go.
 * {@code com.typeomega.lib.tooltip.structure.GtceuPatterns} fills it from a GTCEu pattern, so the numbers come from
 * the pattern and not from hand counts.
 * <p>
 * Type Omega does not show this section (D-431); it only reads {@link #abilities()}, {@link #multiAmp()} and
 * {@link #wireless()} for the "Supports" line.
 *
 * @param controller where the controller sits, or {@code null} when the pattern has none
 * @param abilities  part ability names found in the pattern (for example {@code input_laser}), for the
 *                   "Supports ...!" line
 * @param multiAmp   whether an energy hatch slot accepts multi-amp hatches
 * @param wireless   whether a hatch slot accepts wireless hatches
 */
public record StructureInfo(Range width, Range height, Range depth, Component controller, List<Block> blocks,
                            List<Part> parts, Set<String> abilities, boolean multiAmp, boolean wireless) {

    public StructureInfo {
        blocks = List.copyOf(blocks);
        parts = List.copyOf(parts);
        abilities = Set.copyOf(abilities);
    }

    /** A count or a size; {@code max} is {@link #UNBOUNDED} when the pattern sets no upper limit. */
    public record Range(int min, int max) {

        public static final int UNBOUNDED = Integer.MAX_VALUE;

        public static Range exactly(int n) {
            return new Range(n, n);
        }

        public Range plus(Range other) {
            long top = (long) max + other.max;
            return new Range(min + other.min, top >= UNBOUNDED ? UNBOUNDED : (int) top);
        }

        /** "8", "26-44" or "1+". */
        public String text() {
            if (max == UNBOUNDED) return min + "+";
            return min == max ? String.valueOf(min) : min + "-" + max;
        }
    }

    /**
     * A block of the structure (casing, glass, coil, frame, gearbox).
     *
     * @param tiered whether several tiers of the block fit the same position, such as heating coils or glass
     */
    public record Block(Range count, Component name, boolean tiered) {}

    /**
     * A hatch or bus.
     *
     * @param where the blocks it may replace, empty when it has its own positions
     */
    public record Part(Range count, Component name, List<Component> where) {

        public Part {
            where = List.copyOf(where);
        }
    }
}
