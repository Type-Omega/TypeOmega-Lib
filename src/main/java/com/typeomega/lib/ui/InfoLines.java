package com.typeomega.lib.ui;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.typeomega.lib.lang.Tr;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The text under an illustrated panel, in GTNH order: Total, Usage (or Output) with the voltage tier in its colour,
 * Time, then the lines of the recipe type (amperage, start-up energy, chance...).
 */
public final class InfoLines {

    private final List<Component> lines = new ArrayList<>();

    public static InfoLines create() {
        return new InfoLines();
    }

    public InfoLines total(long eu) {
        return line(UiTexts.TOTAL.component(format(eu)));
    }

    public InfoLines usage(long eut) {
        return line(UiTexts.USAGE.component(format(eut), tierName(eut)));
    }

    public InfoLines usage(double eut) {
        return line(UiTexts.USAGE.component(format(eut), tierName(eut)));
    }

    /** Usage for a consumer, Output for a generator. */
    public InfoLines energy(long eut, boolean generator) {
        return line((generator ? UiTexts.OUTPUT : UiTexts.USAGE).component(format(eut), tierName(eut)));
    }

    public InfoLines time(int ticks) {
        return line(UiTexts.TIME.component(seconds(ticks)));
    }

    public InfoLines continuous() {
        return line(UiTexts.CONTINUOUS.component());
    }

    public InfoLines line(Tr text, Object... args) {
        return line(text.component(args));
    }

    public InfoLines line(Component text) {
        lines.add(text);
        return this;
    }

    public int size() {
        return lines.size();
    }

    public List<Component> build() {
        return List.copyOf(lines);
    }

    /** Voltage tier of an EU/t, ULV = 0; past MAX it keeps counting (MAX+1, MAX+2...) like the GTCEu names. */
    public static int tierOf(double eut) {
        if (eut <= GTValues.V[0]) return 0;
        int tier = (int) Math.ceil((Math.log(eut) / Math.log(2) - 3) / 2 - 1e-9);
        return Math.min(tier, GTValues.VNF.length - 1);
    }

    /** The tier name with the GTCEu colour codes (HV gold, EV purple...). */
    public static MutableComponent tierName(double eut) {
        return Component.literal(GTValues.VNF[tierOf(eut)]);
    }

    /** 1920 -> "1,920"; big values keep every digit, like GTNH. */
    public static String format(long value) {
        return String.format(Locale.US, "%,d", value);
    }

    /** Whole numbers with separators; past 1e18 in scientific form, since a long would overflow. */
    public static String format(double value) {
        if (Math.abs(value) < 1e18) return format(Math.round(value));
        return new DecimalFormat("0.00E0", DecimalFormatSymbols.getInstance(Locale.US)).format(value);
    }

    /** Ticks as seconds: 200 -> "10", 30 -> "1.5". */
    public static String seconds(int ticks) {
        return BigDecimal.valueOf(ticks).divide(BigDecimal.valueOf(20), 2, RoundingMode.HALF_UP).stripTrailingZeros()
                .toPlainString();
    }
}
