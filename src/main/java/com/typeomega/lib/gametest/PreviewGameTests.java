package com.typeomega.lib.gametest;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.typeomega.lib.TypeOmegaLib;
import com.typeomega.lib.preview.BlockInfoCache;
import com.typeomega.lib.preview.PreviewCells;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Preview helpers that need the block registry. */
@PrefixGameTestTemplate(false)
@GameTestHolder(TypeOmegaLib.MOD_ID)
public final class PreviewGameTests {

    private PreviewGameTests() {}

    private static BlockInfo[][][] shape() {
        BlockInfo air = BlockInfoCache.of(Blocks.AIR.defaultBlockState());
        BlockInfo stone = BlockInfoCache.of(Blocks.STONE.defaultBlockState());
        BlockInfo glass = BlockInfoCache.of(Blocks.GLASS.defaultBlockState());
        BlockInfo[][][] blocks = new BlockInfo[3][4][5];
        for (BlockInfo[][] aisle : blocks) for (BlockInfo[] column : aisle) Arrays.fill(column, air);
        blocks[0][0][0] = stone;
        blocks[1][2][3] = glass;
        blocks[2][2][4] = stone;
        blocks[2][3][0] = null;
        return blocks;
    }

    @GameTest(template = "qa_empty_5x5", timeoutTicks = 20)
    public static void previewCellsSkipAirAndKeepCoordinates(GameTestHelper h) {
        h.assertTrue(PreviewCells.countSolid(shape()) == 3, "solid count");
        h.assertTrue(PreviewCells.height(shape()) == 4, "height");
        List<String> seen = new ArrayList<>();
        PreviewCells.forEachSolid(shape(), -1, (x, y, z, state, info) -> seen.add(x + "," + y + "," + z + ":" +
                state.getBlock().getDescriptionId()));
        h.assertTrue(seen.equals(List.of("0,0,0:block.minecraft.stone", "1,2,3:block.minecraft.glass",
                "2,2,4:block.minecraft.stone")), "visit order " + seen);
        seen.clear();
        PreviewCells.forEachSolid(shape(), 2, (x, y, z, state, info) -> seen.add(x + "," + y + "," + z));
        h.assertTrue(seen.equals(List.of("1,2,3", "2,2,4")), "layer 2 " + seen);
        seen.clear();
        PreviewCells.forEachSolid(shape(), 1, (x, y, z, state, info) -> seen.add("x"));
        h.assertTrue(seen.isEmpty(), "layer 1 has no blocks");
        h.succeed();
    }

    @GameTest(template = "qa_empty_5x5", timeoutTicks = 20)
    public static void blockInfoCacheMatchesFromBlockState(GameTestHelper h) {
        BlockInfo a = BlockInfoCache.of(Blocks.STONE.defaultBlockState());
        BlockInfo b = BlockInfoCache.of(Blocks.STONE.defaultBlockState());
        h.assertTrue(a == b, "plain blocks share one BlockInfo");
        h.assertTrue(!a.hasBlockEntity() && a.getBlockState() == Blocks.STONE.defaultBlockState(), "plain state");
        var chest = Blocks.CHEST.defaultBlockState();
        BlockInfo c1 = BlockInfoCache.of(chest);
        BlockInfo c2 = BlockInfoCache.of(chest);
        h.assertTrue(c1 != c2, "entity blocks get an info per cell");
        h.assertTrue(c1.hasBlockEntity() == BlockInfo.fromBlockState(chest).hasBlockEntity(), "entity flag");
        h.assertTrue(c1.getBlockState() == chest, "entity state");
        for (var block : List.of(Blocks.AIR, Blocks.GLASS, Blocks.FURNACE, Blocks.BARREL, Blocks.WATER)) {
            var state = block.defaultBlockState();
            BlockInfo cached = BlockInfoCache.of(state);
            BlockInfo reference = BlockInfo.fromBlockState(state);
            h.assertTrue(cached.hasBlockEntity() == reference.hasBlockEntity() && cached.getBlockState() == state,
                    "cache differs from fromBlockState for " + block);
        }
        h.succeed();
    }
}
