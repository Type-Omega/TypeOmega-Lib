package com.typeomega.lib.ui.tool;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FrameLayoutTest {

    private static final int FILL = 0xFF111111, A = 0xFF222222, B = 0xFF333333;

    @Test
    void aSingleRingIsFourStripsOverTheFill() {
        List<FrameLayout.Rect> r = FrameLayout.layout(FILL, FrameLayout.ALL, new int[] { A }, 10, 6);
        assertEquals(5, r.size());
        assertEquals(new FrameLayout.Rect(0, 0, 10, 6, FILL), r.get(0));
        assertEquals(new FrameLayout.Rect(0, 0, 10, 1, A), r.get(1));
        assertEquals(new FrameLayout.Rect(0, 5, 10, 1, A), r.get(2));
        assertEquals(new FrameLayout.Rect(0, 0, 1, 6, A), r.get(3));
        assertEquals(new FrameLayout.Rect(9, 0, 1, 6, A), r.get(4));
    }

    @Test
    void ringsNestFromTheOutsideIn() {
        List<FrameLayout.Rect> r = FrameLayout.layout(FILL, FrameLayout.ALL, new int[] { A, B }, 10, 8);
        assertEquals(new FrameLayout.Rect(1, 1, 8, 1, B), r.get(5), "inner top strip is one pixel in");
        assertEquals(new FrameLayout.Rect(1, 6, 8, 1, B), r.get(6));
        assertEquals(new FrameLayout.Rect(1, 1, 1, 6, B), r.get(7));
        assertEquals(new FrameLayout.Rect(8, 1, 1, 6, B), r.get(8));
    }

    @Test
    void aMissingSideLetsTheOthersRunToTheEdge() {
        int sides = FrameLayout.ALL & ~FrameLayout.BOTTOM;
        List<FrameLayout.Rect> r = FrameLayout.layout(0, sides, new int[] { A }, 10, 6);
        assertEquals(3, r.size(), "no fill, no bottom strip");
        assertTrue(r.contains(new FrameLayout.Rect(0, 0, 1, 6, A)), "left runs to the bottom edge");
        assertTrue(r.contains(new FrameLayout.Rect(9, 0, 1, 6, A)), "right runs to the bottom edge");
        assertTrue(r.stream().noneMatch(x -> x.y() == 5 && x.h() == 1 && x.w() > 1), "nothing along the bottom");
    }

    @Test
    void aTransparentFillIsNotDrawn() {
        assertTrue(FrameLayout.layout(0, FrameLayout.ALL, new int[0], 10, 6).isEmpty());
        assertEquals(1, FrameLayout.layout(0x22FFFFFF, FrameLayout.ALL, new int[0], 10, 6).size());
    }

    @Test
    void tooSmallFramesDropRectanglesInsteadOfGoingNegative() {
        for (var r : FrameLayout.layout(FILL, FrameLayout.ALL, new int[] { A, B, A }, 3, 3)) {
            assertTrue(r.w() > 0 && r.h() > 0, "no empty or negative rectangle: " + r);
        }
    }
}
