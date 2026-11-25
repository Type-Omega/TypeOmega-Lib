package com.typeomega.lib.ui.report;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportTest {

    @Test
    void roundTripsThroughABuffer() {
        var report = Report.builder(Component.literal("Scan"))
                .group(Component.literal("Missing"), 0x55FF55,
                        List.of(new Report.Line(Component.literal("stone"), new BlockPos(1, 2, 3)),
                                new Report.Line(Component.literal("note"), null)))
                .build();
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        report.write(buf);
        var back = Report.read(buf);
        assertEquals(report.lineCount(), back.lineCount());
        assertEquals(new BlockPos(1, 2, 3), back.groups().get(0).lines().get(0).pos());
        assertNull(back.groups().get(0).lines().get(1).pos());
        assertEquals(0x55FF55, back.groups().get(0).colour());
    }

    @Test
    void capsTheLinesButKeepsTheTotals() {
        List<Report.Line> many = new ArrayList<>();
        for (int i = 0; i < Report.MAX_LINES + 50; i++) many.add(new Report.Line(Component.literal("x"), null));
        var report = Report.builder(Component.literal("Big")).group(Component.literal("a"), 0, many)
                .group(Component.literal("b"), 0, many.subList(0, 10)).build();
        assertEquals(Report.MAX_LINES, report.lineCount());
        assertEquals(Report.MAX_LINES + 50, report.groups().get(0).total());
        assertEquals(10, report.groups().get(1).total());
        assertTrue(report.groups().get(1).lines().isEmpty());
    }
}
