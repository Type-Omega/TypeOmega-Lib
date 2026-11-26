package com.typeomega.lib.preview;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.world.level.block.state.BlockState;

/**
 * Walks the dense {@code BlockInfo[x][y][z]} of a multiblock preview shape without paying for its air. The shape of a
 * megastructure is a bounding box with 1% to 3% real blocks; every loop that allocated a position or a
 * {@link BlockInfo} per cell before testing for air cost 30 to 80 times what the structure needs.
 */
public final class PreviewCells {

    /** Receives one non-air cell. */
    @FunctionalInterface
    public interface CellVisitor {

        void accept(int x, int y, int z, BlockState state, BlockInfo info);
    }

    private PreviewCells() {}

    public static boolean isAir(BlockInfo info) {
        return info == null || info.getBlockState().isAir();
    }

    /** Number of cells that are not air. */
    public static int countSolid(BlockInfo[][][] blocks) {
        int count = 0;
        for (BlockInfo[][] aisle : blocks) {
            for (BlockInfo[] column : aisle) {
                for (BlockInfo info : column) {
                    if (!isAir(info)) count++;
                }
            }
        }
        return count;
    }

    /** Height of the shape: the longest aisle (the {@code y} extent). */
    public static int height(BlockInfo[][][] blocks) {
        int height = 0;
        for (BlockInfo[][] aisle : blocks) height = Math.max(height, aisle.length);
        return height;
    }

    /**
     * Visits every non-air cell in {@code x, y, z} order.
     *
     * @param onlyLayer a {@code y} index to restrict to, or {@code -1} for every layer
     */
    public static void forEachSolid(BlockInfo[][][] blocks, int onlyLayer, CellVisitor visitor) {
        for (int x = 0; x < blocks.length; x++) {
            BlockInfo[][] aisle = blocks[x];
            for (int y = 0; y < aisle.length; y++) {
                if (onlyLayer != -1 && onlyLayer != y) continue;
                BlockInfo[] column = aisle[y];
                for (int z = 0; z < column.length; z++) {
                    BlockInfo info = column[z];
                    if (isAir(info)) continue;
                    visitor.accept(x, y, z, info.getBlockState(), info);
                }
            }
        }
    }
}
