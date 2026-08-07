package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Interface implemented by blocks that use BlockTechnicalBlock as proxy blocks for multi-block structures.
 */
public interface IProxyMainBlock {
    /**
     * Determines whether the block at mainPos is the main block responsible for the proxy block at proxyPos.
     */
    boolean isMainBlockForProxy(IBlockAccess world, BlockPos mainPos, BlockPos proxyPos);

    /**
     * Returns the bounding box for a proxy cell in [0,1] local block coordinates.
     * Return {@link net.minecraft.block.Block#NULL_AABB} for cells with no geometry (fully transparent).
     *
     * @param state    the main block's state (carries facing etc.)
     * @param world    world access
     * @param mainPos  position of the main block
     * @param proxyPos position of the proxy block whose AABB is requested
     */
    AxisAlignedBB getProxyCellAABB(IBlockState state, IBlockAccess world, BlockPos mainPos, BlockPos proxyPos);
}
