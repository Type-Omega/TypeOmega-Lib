package com.typeomega.lib.preview;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code BlockState -> BlockInfo} for multiblock previews. LDLib's {@code BlockInfo.fromBlockState} builds a throwaway
 * block entity for every entity block just to learn whether one exists, once per cell: a 100 000 block structure
 * paid that 100 000 times. The answer depends on the state only, so it is asked once; a block without an entity
 * shares one immutable {@link BlockInfo}, and an entity block gets a fresh one per cell (it keeps its own entity).
 */
public final class BlockInfoCache {

    private static final Map<BlockState, BlockInfo> PLAIN = new ConcurrentHashMap<>();
    private static final Map<BlockState, Boolean> HAS_ENTITY = new ConcurrentHashMap<>();

    private BlockInfoCache() {}

    /** Same result as {@code BlockInfo.fromBlockState(state)}, without the entity built for every call. */
    public static BlockInfo of(BlockState state) {
        BlockInfo plain = PLAIN.get(state);
        if (plain != null) return plain;
        if (state.getBlock() instanceof EntityBlock entityBlock &&
                HAS_ENTITY.computeIfAbsent(state, s -> probe(entityBlock, s))) {
            return new BlockInfo(state, true);
        }
        return PLAIN.computeIfAbsent(state, BlockInfo::new);
    }

    private static boolean probe(EntityBlock block, BlockState state) {
        try {
            return block.newBlockEntity(BlockPos.ZERO, state) != null;
        } catch (Exception e) {
            return false;
        }
    }

    /** For tests: forget every cached state. */
    public static void clear() {
        PLAIN.clear();
        HAS_ENTITY.clear();
    }
}
