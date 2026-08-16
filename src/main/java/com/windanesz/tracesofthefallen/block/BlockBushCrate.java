package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockBushCrate extends BlockTOFT implements IProxyMainBlock {

    public BlockBushCrate(Material material) {
        super(material);
    }

    @Override
    public boolean isMainBlockForProxy(IBlockAccess world, BlockPos mainPos, BlockPos proxyPos) {
        IBlockState state = world.getBlockState(mainPos);
        if (state.getBlock() != this) return false;
        return proxyPos.equals(mainPos.offset(state.getValue(FACING).getOpposite()));
    }

    @Override
    public AxisAlignedBB getProxyCellAABB(IBlockState state, IBlockAccess world, BlockPos mainPos, BlockPos proxyPos) {
        // Crate geometry extends ~0.125–0.875 into the proxy cell in the relevant axis
        switch (state.getValue(FACING)) {
            case EAST:
            case WEST:
                return new AxisAlignedBB(0.125D, 0.0D, 0.0D, 0.875D, 0.875D, 1.0D);
            default: // NORTH, SOUTH
                return new AxisAlignedBB(0.0D, 0.0D, 0.125D, 1.0D, 0.875D, 0.875D);
        }
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        if (!worldIn.isRemote) {
            BlockPos proxyPos = pos.offset(state.getValue(FACING).getOpposite());
            IBlockState proxyState = worldIn.getBlockState(proxyPos);
            if (proxyState.getBlock().isReplaceable(worldIn, proxyPos) || worldIn.isAirBlock(proxyPos)) {
                worldIn.setBlockState(proxyPos, ModBlocks.technical_block.getDefaultState(), 3);
            }
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote) {
            BlockPos proxyPos = pos.offset(state.getValue(FACING).getOpposite());
            if (worldIn.getBlockState(proxyPos).getBlock() == ModBlocks.technical_block) {
                worldIn.setBlockToAir(proxyPos);
            }
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        if (!worldIn.isRemote) {
            BlockPos proxyPos = pos.offset(state.getValue(FACING).getOpposite());
            IBlockState proxyState = worldIn.getBlockState(proxyPos);
            if (proxyState.getBlock() != ModBlocks.technical_block) {
                if (proxyState.getBlock().isReplaceable(worldIn, proxyPos) || worldIn.isAirBlock(proxyPos)) {
                    worldIn.setBlockState(proxyPos, ModBlocks.technical_block.getDefaultState(), 3);
                }
            }
        }
    }
}
