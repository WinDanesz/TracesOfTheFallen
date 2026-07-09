package com.windanesz.tracesofthefallen.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Interface implemented by blocks that use BlockTechnicalBlock as proxy blocks for multi-block structures.
 */
public interface IProxyMainBlock {
    /**
     * Determines whether the block at mainPos is the main block responsible for the proxy block at proxyPos.
     *
     * @param world The world access
     * @param mainPos The candidate main block position
     * @param proxyPos The position of the technical proxy block
     * @return true if mainPos is the main block for proxyPos
     */
    boolean isMainBlockForProxy(IBlockAccess world, BlockPos mainPos, BlockPos proxyPos);
}
