package com.typeomega.lib.data;

import net.minecraft.core.BlockPos;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class BlockPosHashMapTest {

    @Test
    void isAHashMapSoCastsInGtceuHold() {
        Object map = new BlockPosHashMap<String>();
        assertInstanceOf(HashMap.class, map);
    }

    @Test
    void behavesLikeAnOrderedMapAgainstAnOracle() {
        BlockPosHashMap<Integer> map = new BlockPosHashMap<>();
        Map<BlockPos, Integer> oracle = new LinkedHashMap<>();
        Random rng = new Random(7);
        for (int i = 0; i < 20_000; i++) {
            BlockPos pos = new BlockPos(rng.nextInt(60), rng.nextInt(60), rng.nextInt(60));
            switch (rng.nextInt(4)) {
                case 0, 1 -> assertEquals(oracle.put(pos, i), map.put(pos, i));
                case 2 -> assertEquals(oracle.remove(pos), map.remove(pos));
                default -> {
                    assertEquals(oracle.get(pos), map.get(pos));
                    assertEquals(oracle.containsKey(pos), map.containsKey(pos));
                }
            }
        }
        assertEquals(oracle.size(), map.size());
        assertEquals(oracle.keySet(), map.keySet());
        assertEquals(oracle, map);
        assertEquals(oracle.hashCode(), map.hashCode());
    }

    @Test
    void keepsStructureNeighboursApart() {
        BlockPosHashMap<BlockPos> map = new BlockPosHashMap<>();
        for (int x = 0; x < 100; x++) {
            for (int y = 0; y < 100; y++) {
                for (int z = 0; z < 10; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    map.put(pos, pos);
                }
            }
        }
        assertEquals(100_000, map.size());
        assertEquals(new BlockPos(99, 99, 9), map.get(new BlockPos(99, 99, 9)));
        assertNull(map.get(new BlockPos(100, 0, 0)));
    }

    @Test
    void computeAndMergeGoThroughTheDelegate() {
        BlockPosHashMap<Integer> map = new BlockPosHashMap<>();
        BlockPos pos = new BlockPos(1, 2, 3);
        assertEquals(1, map.computeIfAbsent(pos, p -> 1));
        assertEquals(3, map.merge(pos, 2, Integer::sum));
        assertEquals(3, map.getOrDefault(pos, 0));
        assertEquals(0, map.getOrDefault(new BlockPos(9, 9, 9), 0));
        assertEquals(1, map.size());
        BlockPosHashMap<Integer> copy = (BlockPosHashMap<Integer>) map.clone();
        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(1, copy.size());
    }

    @Test
    void putAllAndIterationFollowInsertionOrder() {
        BlockPosHashMap<Integer> map = new BlockPosHashMap<>();
        Map<BlockPos, Integer> source = new LinkedHashMap<>();
        for (int i = 0; i < 50; i++) source.put(new BlockPos(50 - i, i, 0), i);
        map.putAll(source);
        assertEquals(source.keySet().stream().toList(), map.keySet().stream().toList());
        int[] seen = { 0 };
        map.forEach((pos, value) -> seen[0] += value);
        assertEquals(49 * 50 / 2, seen[0]);
    }
}
