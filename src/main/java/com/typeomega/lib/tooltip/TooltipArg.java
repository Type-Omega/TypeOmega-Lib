package com.typeomega.lib.tooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.typeomega.lib.lang.Tr;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Coloured arguments for tooltip lines, following D-410: green for what the player configures, cyan for units
 * and derived values, white for highlighted terms, bold for emphasis such as "NOT".
 */
public final class TooltipArg {

    private TooltipArg() {}

    /** Something the player sets in the controller (green). */
    public static MutableComponent configurable(Object value) {
        return styled(value, ChatFormatting.GREEN);
    }

    /** A unit or a value derived from the balance definition (cyan). */
    public static MutableComponent value(Object value) {
        return styled(value, ChatFormatting.AQUA);
    }

    /** A term the player should notice, such as an item or a buff name (white). */
    public static MutableComponent highlight(Object value) {
        return styled(value, ChatFormatting.WHITE);
    }

    /** Bold emphasis, such as "NOT" in a warning. */
    public static MutableComponent emphasis(Object value) {
        return styled(value, ChatFormatting.BOLD);
    }

    public static MutableComponent styled(Object value, ChatFormatting... style) {
        return component(value).withStyle(style);
    }

    /**
     * Turns a value into a component: {@link Tr} and {@link Component} are kept translatable, whole numbers get
     * thousands separators, anything else is shown as is.
     */
    static MutableComponent component(Object value) {
        if (value instanceof Tr tr) return tr.component();
        if (value instanceof Component c) return c.copy();
        if (value instanceof Long || value instanceof Integer || value instanceof Short) {
            return Component.literal(NumberFormat.getIntegerInstance(Locale.ROOT).format(value));
        }
        return Component.literal(String.valueOf(value));
    }
}
