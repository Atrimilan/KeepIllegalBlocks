package io.github.atrimilan.lockblockstate.services;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;

import java.util.*;

public class BlockInteractionService {

    private static final EnumSet<BlockFace> BLOCK_FACES = EnumSet.of(BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH,
            BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST);

    /**
     * Save the state of the source block and its relatives.
     *
     * @param sourceBlock
     * @return
     */
    public Map<Location, BlockState> saveSourceAndRelativesBlockStates(Block sourceBlock) {
        Map<Location, BlockState> blockStateMap = new HashMap<>();

        if (sourceBlock.getBlockData() instanceof Door door) {
            boolean isTopPart = Bisected.Half.TOP.equals(door.getHalf()); // Is source block the top part of a door

            Block topPart = isTopPart ? sourceBlock : sourceBlock.getRelative(BlockFace.UP);
            Block bottomPart = !isTopPart ? sourceBlock : sourceBlock.getRelative(BlockFace.DOWN);

            saveRelativeBlockState(blockStateMap, topPart, Set.of(BlockFace.DOWN));
            saveRelativeBlockState(blockStateMap, bottomPart, Set.of(BlockFace.UP));

            saveUpperDoorBlockState(blockStateMap, topPart);

        } else {
            saveRelativeBlockState(blockStateMap, sourceBlock, Collections.emptySet());
        }

        saveBlockState(blockStateMap, sourceBlock);

        return blockStateMap;
    }

    /**
     * Iterate recursively onto every upper block and save the block state if it's a door.
     *
     * @param blockStateMap The map of saved block states
     * @param doorBlock     The current door block
     */
    private void saveUpperDoorBlockState(Map<Location, BlockState> blockStateMap, Block doorBlock) {
        Block nextUpperBlock = doorBlock.getRelative(BlockFace.UP);
        if (nextUpperBlock.getBlockData() instanceof Door) {
            saveUpperDoorBlockState(blockStateMap, nextUpperBlock); // Recursion
        }
        saveBlockState(blockStateMap, doorBlock);
    }

    /**
     * For each face of the block, get the relative block (the adjacent block on the corresponding face) and save its
     * state.
     *
     * @param sourceBlock   The source block, from which we will look at the relative blocks
     * @param blockStateMap The map of saved block states
     * @param ignoredFace   A list of block faces we don't want to look at
     */
    private void saveRelativeBlockState(Map<Location, BlockState> blockStateMap, Block sourceBlock,
                                        Set<BlockFace> ignoredFace) {
        for (BlockFace face : BLOCK_FACES) {
            if (ignoredFace.contains(face)) continue;

            // Save the state of the block located on the given face of the source block
            saveBlockState(blockStateMap, sourceBlock.getRelative(face));
        }
    }

    /**
     * Save given block state (air is ignored).
     *
     * @param map   The map of saved block states
     * @param block The block to save
     */
    private void saveBlockState(Map<Location, BlockState> map, Block block) {
        if (!block.getType().isAir()) {
            map.put(block.getLocation(), block.getState());
        }
    }

    /**
     * Force restore of blocks from the given map, wherever they have been replaced by air.
     *
     * @param blockStateMap The map of block states to restore
     */
    public void restoreSourceAndRelativesBlockStates(Map<Location, BlockState> blockStateMap) {
        if (blockStateMap == null || blockStateMap.isEmpty()) return;

        blockStateMap.forEach((location, previousState) -> {
            if (location.getBlock().getType().isAir() && !previousState.getType().isAir()) // Was broken by interaction
                previousState.update(true, false); // Force restore of the previous block state
        });
    }
}
