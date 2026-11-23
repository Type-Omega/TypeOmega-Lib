package com.typeomega.lib.tooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.typeomega.lib.lang.Tr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    private final List<Component> lines = new ArrayList<>();
    private boolean hasType;
    private boolean hasMod;

    MultiblockTooltipBuilder() {}

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

    /** Last line: the mod name in blue italics; a proper name, so it is not translated. */
    public MultiblockTooltipBuilder mod(String modName) {
        add(Component.literal(modName).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
        hasMod = true;
        return this;
    }

    public List<Component> build() {
        if (!hasType) throw new IllegalStateException("tooltip has no machineType line");
        if (!hasMod) throw new IllegalStateException("tooltip has no mod line");
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
