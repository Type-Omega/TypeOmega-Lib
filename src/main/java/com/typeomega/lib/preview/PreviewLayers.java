package com.typeomega.lib.preview;

/**
 * Which layer of a multiblock hologram to show. Under the block cap the hologram starts with every layer ({@code -1})
 * and each click moves to the next layer and back to all; above it "all" is not offered (it is what the cap exists
 * to avoid), the first layer is the controller's and the clicks cycle through the layers only.
 */
public final class PreviewLayers {

    public static final int ALL = -1;

    private PreviewLayers() {}

    /** A {@code cap} of 0 or less means no limit. */
    public static boolean overCap(int solidBlocks, int cap) {
        return cap > 0 && solidBlocks > cap;
    }

    public static int firstLayer(boolean overCap, int controllerLayer) {
        return overCap ? controllerLayer : ALL;
    }

    public static int nextLayer(int current, int height, boolean overCap) {
        int next = current + 1;
        if (next >= height) return overCap ? 0 : ALL;
        return next;
    }
}
