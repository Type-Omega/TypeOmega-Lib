package com.typeomega.lib.preview;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PreviewLayersTest {

    @Test
    void capOfZeroMeansNoLimit() {
        assertFalse(PreviewLayers.overCap(5_000_000, 0));
        assertFalse(PreviewLayers.overCap(5_000_000, -1));
    }

    @Test
    void capIsExclusiveAbove() {
        assertFalse(PreviewLayers.overCap(32_768, 32_768));
        assertTrue(PreviewLayers.overCap(32_769, 32_768));
        assertFalse(PreviewLayers.overCap(21_823, 32_768));
        assertTrue(PreviewLayers.overCap(171_491, 32_768));
    }

    @Test
    void underTheCapTheCycleStartsAndEndsOnAll() {
        assertEquals(PreviewLayers.ALL, PreviewLayers.firstLayer(false, 7));
        int layer = PreviewLayers.ALL;
        int[] seen = new int[5];
        for (int i = 0; i < seen.length; i++) {
            layer = PreviewLayers.nextLayer(layer, 3, false);
            seen[i] = layer;
        }
        assertArrayEquals(new int[] { 0, 1, 2, -1, 0 }, seen);
    }

    @Test
    void overTheCapTheCycleStartsAtTheControllerAndNeverShowsAll() {
        assertEquals(7, PreviewLayers.firstLayer(true, 7));
        int layer = 7;
        int[] seen = new int[6];
        for (int i = 0; i < seen.length; i++) {
            layer = PreviewLayers.nextLayer(layer, 9, true);
            seen[i] = layer;
        }
        assertArrayEquals(new int[] { 8, 0, 1, 2, 3, 4 }, seen);
        assertNotEquals(PreviewLayers.ALL, PreviewLayers.nextLayer(8, 9, true));
    }
}
