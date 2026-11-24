package com.typeomega.lib.ui;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InfoLinesTest {

    private static TranslatableContents tr(Component c) {
        return (TranslatableContents) c.getContents();
    }

    private static String tierText(Component line) {
        var arg = (Component) tr(line).getArgs()[1];
        return ((LiteralContents) arg.getContents()).text();
    }

    @Test
    void tiersFollowTheGtceuVoltages() {
        assertEquals(0, InfoLines.tierOf(8));
        assertEquals(1, InfoLines.tierOf(9));
        assertEquals(1, InfoLines.tierOf(32));
        assertEquals(3, InfoLines.tierOf(512));
        assertEquals(4, InfoLines.tierOf(1920));
        assertEquals(4, InfoLines.tierOf(2048));
        assertEquals(6, InfoLines.tierOf(30_720));
        assertEquals(14, InfoLines.tierOf(Math.pow(2, 31)));
        assertEquals(15, InfoLines.tierOf(Math.pow(2, 33)));
    }

    @Test
    void gtnhOrderAndColouredTier() {
        var lines = InfoLines.create().total(1_179_648_000L).energy(491_520, false).time(2400).build();
        assertEquals(3, lines.size());
        assertEquals("typeomegalib.ui.total", tr(lines.get(0)).getKey());
        assertEquals("1,179,648,000", tr(lines.get(0)).getArgs()[0]);
        assertEquals("typeomegalib.ui.usage", tr(lines.get(1)).getKey());
        assertEquals("491,520", tr(lines.get(1)).getArgs()[0]);
        assertEquals("§3UV", tierText(lines.get(1)));
        assertEquals("120", tr(lines.get(2)).getArgs()[0]);
    }

    @Test
    void generatorsSayOutput() {
        var line = InfoLines.create().energy(2048, true).build().get(0);
        assertEquals("typeomegalib.ui.output", tr(line).getKey());
        assertEquals("§6HV", tierText(InfoLines.create().energy(512, true).build().get(0)));
    }

    @Test
    void secondsKeepFractions() {
        assertEquals("10", InfoLines.seconds(200));
        assertEquals("1.5", InfoLines.seconds(30));
        assertEquals("0.05", InfoLines.seconds(1));
    }

    @Test
    void hugeValuesStayReadable() {
        assertEquals("8,589,934,592", InfoLines.format(Math.pow(2, 33)));
        assertEquals("1.00E20", InfoLines.format(1e20));
    }

    @Test
    void extraLinesFollow() {
        var lines = InfoLines.create().time(20).line(UiTexts.CONTINUOUS).build();
        assertEquals("typeomegalib.ui.continuous", tr(lines.get(1)).getKey());
    }
}
