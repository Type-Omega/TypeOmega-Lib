package com.typeomega.lib.tooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.typeomega.lib.lang.Tr;

/**
 * One table row: coloured name · values · effect, joined by " / ". Values alternate green and cyan like the
 * GTNH Quad-Cell Plasma Collider tooltip.
 */
public final class TooltipRow {

    private static final ChatFormatting[] VALUE_COLORS = { ChatFormatting.GREEN, ChatFormatting.AQUA };

    private final MutableComponent name;
    private final Object[] values;
    private MutableComponent effect;

    private TooltipRow(MutableComponent name, Object[] values) {
        this.name = name;
        this.values = values;
    }

    /** A row whose name is a translation (a material, a mode) shown in {@code color}. */
    public static TooltipRow of(Object name, ChatFormatting color, Object... values) {
        return new TooltipRow(TooltipArg.component(name).withStyle(color), values);
    }

    public TooltipRow effect(Tr text, Object... args) {
        effect = text.component(args);
        return this;
    }

    MutableComponent build() {
        MutableComponent line = Component.empty().append(name);
        for (int i = 0; i < values.length; i++) {
            separator(line);
            line.append(TooltipArg.component(values[i]).withStyle(VALUE_COLORS[i % VALUE_COLORS.length]));
        }
        if (effect != null) {
            separator(line);
            line.append(effect);
        }
        return line;
    }

    private static void separator(MutableComponent line) {
        line.append(Component.literal(" / ").withStyle(ChatFormatting.GRAY));
    }
}
