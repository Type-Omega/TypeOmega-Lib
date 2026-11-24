package com.typeomega.lib.tooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.typeomega.lib.lang.Tr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Multiblock tooltip in the GTNH layout (GTIA-D-410): {@code Machine Type:} with yellow types, short description
 * lines, separators, table rows, warnings, the green supported-hatches line and the mod name in blue italics. Every
 * text is a {@link Tr} with arguments, so numbers come from the machine's balance definition.
 * <p>
 * There is no "Hold [LSHIFT] to display structure guidelines" line on purpose (GTIA-D-428): Shift does not show the
 * structure in GTCEu, so the line would promise something the game does not do.
 * <p>
 * The result plugs into GTCEu: {@code .tooltips(tooltip.toArray())} or
 * {@code .tooltipBuilder((stack, list) -> list.addAll(tooltip.build()))}.
 */
public final class MultiblockTooltipBuilder {

    // EN: Width of the GTNH separator; long enough to read as a rule, short enough for narrow screens.
    // PT: Largura do separador do GTNH; longo para parecer régua, curto para telas estreitas.
    // ZH: GTNH 分隔线宽度；足够长以显示为分隔线，又不会超出窄屏。
    static final String SEPARATOR = "─".repeat(24);
    private static final String TAB = "   ";

    private final List<Component> lines = new ArrayList<>();
    private boolean hasType;
    private boolean hasMod;
    private boolean inStructure;

    private final boolean section;

    MultiblockTooltipBuilder() {
        this(false);
    }

    MultiblockTooltipBuilder(boolean section) {
        this.section = section;
    }

    /** First line: "Machine Type:" followed by the yellow types, comma separated. */
    public MultiblockTooltipBuilder machineType(Tr... types) {
        if (hasType || !lines.isEmpty()) throw new IllegalStateException("machineType must be the first line");
        if (types.length == 0) throw new IllegalArgumentException("machineType needs at least one type");
        MutableComponent joined = Component.empty().withStyle(ChatFormatting.YELLOW);
        for (int i = 0; i < types.length; i++) {
            if (i > 0) joined.append(", ");
            joined.append(types[i].component());
        }
        lines.add(TooltipTexts.MACHINE_TYPE.component(joined).withStyle(ChatFormatting.GRAY));
        hasType = true;
        return this;
    }

    /** A grey description or rule line; colour keywords with {@link TooltipArg}. */
    public MultiblockTooltipBuilder line(Tr text, Object... args) {
        return add(text.component(args).withStyle(ChatFormatting.GRAY));
    }

    /** A red warning line, for limits that stop the machine. */
    public MultiblockTooltipBuilder warning(Tr text, Object... args) {
        return add(text.component(args).withStyle(ChatFormatting.RED));
    }

    /** Optional humour line (GTIA-D-343), grey italics. */
    public MultiblockTooltipBuilder humor(Tr text, Object... args) {
        return add(text.component(args).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    public MultiblockTooltipBuilder separator() {
        return add(Component.literal(SEPARATOR).withStyle(ChatFormatting.DARK_GRAY));
    }

    public MultiblockTooltipBuilder table(TooltipRow... rows) {
        for (TooltipRow row : rows) add(row.build());
        return this;
    }

    /** Green "Supports Multi-Amp, Laser Hatches and Wireless Hatches!" line. */
    public MultiblockTooltipBuilder hatches(Hatch... hatches) {
        if (hatches.length == 0) return this;
        MutableComponent list = hatches[0].text().component();
        for (int i = 1; i < hatches.length; i++) {
            Component next = hatches[i].text().component();
            list = i == hatches.length - 1 ? TooltipTexts.AND.component(list, next) :
                    list.append(", ").append(next);
        }
        return add(TooltipTexts.SUPPORTS.component(list).withStyle(ChatFormatting.GREEN));
    }

    /** "Modes: A, B, C" when the controller switches between recipe types. */
    public MultiblockTooltipBuilder modes(List<? extends Component> modes) {
        if (modes.size() < 2) return this;
        MutableComponent list = Component.empty();
        for (int i = 0; i < modes.size(); i++) {
            if (i > 0) list.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
            list.append(modes.get(i).copy().withStyle(ChatFormatting.WHITE));
        }
        return add(StructureTexts.MODES.component(list).withStyle(ChatFormatting.GRAY));
    }

    /** "Causes N Pollution per second", as in GTNH; skipped when the machine does not pollute. */
    public MultiblockTooltipBuilder pollution(int perSecond) {
        if (perSecond <= 0) return this;
        return add(StructureTexts.POLLUTION.component(TooltipArg.value(perSecond)).withStyle(ChatFormatting.GRAY));
    }

    /**
     * Starts the structure section (GTIA-D-430): "Dimensions: WxHxD (Width x Height x Depth)" and "Structure:".
     * It is always shown: GTCEu has no Shift page for it (GTIA-D-428).
     */
    public MultiblockTooltipBuilder beginStructure(int width, int height, int depth) {
        return beginStructure(StructureInfo.Range.exactly(width), StructureInfo.Range.exactly(height),
                StructureInfo.Range.exactly(depth));
    }

    /** Variable-size structure: each range shows as "min-max". */
    public MultiblockTooltipBuilder beginStructure(StructureInfo.Range width, StructureInfo.Range height,
                                                   StructureInfo.Range depth) {
        MutableComponent size = Component.empty();
        StructureInfo.Range[] axes = { width, height, depth };
        for (int i = 0; i < axes.length; i++) {
            if (i > 0) size.append(Component.literal("x").withStyle(ChatFormatting.GRAY));
            size.append(Component.literal(axes[i].text()).withStyle(ChatFormatting.GOLD));
        }
        add(StructureTexts.DIMENSIONS.component(size).withStyle(ChatFormatting.WHITE));
        add(StructureTexts.STRUCTURE.component().withStyle(ChatFormatting.WHITE));
        inStructure = true;
        return this;
    }

    /** "Controller: front, bottom, center". */
    public MultiblockTooltipBuilder controller(Object where) {
        return structureLine(StructureTexts.CONTROLLER.component(TooltipArg.component(where)
                .withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.WHITE));
    }

    /** "26-44x Stable Titanium Casing"; {@code count} is a number, a {@link StructureInfo.Range} or a text. */
    public MultiblockTooltipBuilder casing(Object count, Object block) {
        return casing(count, block, false);
    }

    /** A block that accepts several tiers in the same position, marked "(Tiered)". */
    public MultiblockTooltipBuilder tieredCasing(Object count, Object block) {
        return casing(count, block, true);
    }

    private MultiblockTooltipBuilder casing(Object count, Object block, boolean tiered) {
        MutableComponent line = Component.literal(countText(count) + "x ").withStyle(ChatFormatting.GOLD)
                .append(TooltipArg.component(block).withStyle(ChatFormatting.WHITE));
        if (tiered) line.append(" ").append(StructureTexts.TIERED.component().withStyle(ChatFormatting.GRAY));
        return structureLine(line);
    }

    /** "1+ Energy Hatch: Any Stable Titanium Casing". */
    public MultiblockTooltipBuilder hatch(Object type, Object count, Object where) {
        return structureLine(Component.literal(countText(count) + " ").withStyle(ChatFormatting.YELLOW)
                .append(TooltipArg.component(type).withStyle(ChatFormatting.WHITE))
                .append(Component.literal(": ").withStyle(ChatFormatting.WHITE))
                .append(TooltipArg.component(where).withStyle(ChatFormatting.GRAY)));
    }

    /** A free line inside the structure section, such as a tier heading or a placement rule. */
    public MultiblockTooltipBuilder structureNote(Tr text, Object... args) {
        return structureLine(text.component(args).withStyle(ChatFormatting.GRAY));
    }

    /** GTNH blueprint channel line: which channel of the build tool sets which part of the structure. */
    public MultiblockTooltipBuilder channel(Object name, Object effect) {
        return structureLine(StructureTexts.CHANNEL.component(TooltipArg.highlight(name),
                TooltipArg.component(effect)).withStyle(ChatFormatting.GRAY));
    }

    /** The whole structure section from a pattern summary. */
    public MultiblockTooltipBuilder structure(StructureInfo info) {
        beginStructure(info.width(), info.height(), info.depth());
        if (info.controller() != null) controller(info.controller());
        for (StructureInfo.Block block : info.blocks()) casing(block.count(), block.name(), block.tiered());
        for (StructureInfo.Part part : info.parts()) {
            Component where = part.where().isEmpty() ? StructureTexts.OWN_POSITIONS.component() :
                    StructureTexts.ANY.component(StructureTexts.orList(part.where()));
            hatch(part.name(), part.count(), where);
        }
        return this;
    }

    private MultiblockTooltipBuilder structureLine(Component line) {
        if (!inStructure) throw new IllegalStateException("structure lines need beginStructure first");
        return add(Component.literal(TAB).append(line));
    }

    private static String countText(Object count) {
        return count instanceof StructureInfo.Range range ? range.text() : String.valueOf(count);
    }

    /**
     * The "Supports ...!" line from the hatches the pattern accepts: multi-amp, laser and wireless are read from the
     * structure, {@code extra} adds what the pattern can't tell.
     */
    public MultiblockTooltipBuilder hatches(StructureInfo info, Hatch... extra) {
        Set<Hatch> found = EnumSet.noneOf(Hatch.class);
        if (info.multiAmp()) found.add(Hatch.MULTI_AMP);
        if (info.wireless()) found.add(Hatch.WIRELESS);
        if (info.abilities().contains("input_laser") || info.abilities().contains("output_laser")) {
            found.add(Hatch.LASER);
        }
        found.addAll(List.of(extra));
        return hatches(found.toArray(Hatch[]::new));
    }

    /** Last line: the mod name in blue italics; a proper name, so it is not translated. */
    public MultiblockTooltipBuilder mod(String modName) {
        add(Component.literal(modName).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
        hasMod = true;
        return this;
    }

    public List<Component> build() {
        if (!section && !hasType) throw new IllegalStateException("tooltip has no machineType line");
        if (!section && !hasMod) throw new IllegalStateException("tooltip has no mod line");
        return Collections.unmodifiableList(new ArrayList<>(lines));
    }

    /** For GTCEu's {@code MultiblockMachineBuilder.tooltips(Component...)}. */
    public Component[] toArray() {
        return build().toArray(Component[]::new);
    }

    private MultiblockTooltipBuilder add(Component line) {
        if (hasMod) throw new IllegalStateException("mod must be the last line");
        lines.add(line);
        return this;
    }
}
