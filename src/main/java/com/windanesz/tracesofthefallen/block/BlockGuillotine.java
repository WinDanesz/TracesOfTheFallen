package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockGuillotine extends BlockContainer {
    public static final PropertyDirection FACING = BlockDirectional.FACING;
    // Base AABB unrotated (placed on ceiling, pointing DOWN)
    private static final AxisAlignedBB AABB_DOWN = new AxisAlignedBB(0.0D, 1.0D, 0.375D, 1.0D, 1.125D, 0.625D);
    private static final AxisAlignedBB AABB_UP = new AxisAlignedBB(0.0D, -0.125D, 0.375D, 1.0D, 0.0D, 0.625D);
    private static final AxisAlignedBB AABB_NORTH = new AxisAlignedBB(0.0D, 0.375D, 1.0D, 1.0D, 0.625D, 1.125D);
    private static final AxisAlignedBB AABB_SOUTH = new AxisAlignedBB(0.0D, 0.375D, -0.125D, 1.0D, 0.625D, 0.0D);
    private static final AxisAlignedBB AABB_WEST = new AxisAlignedBB(1.0D, 0.375D, 0.0D, 1.125D, 0.625D, 1.0D);
    private static final AxisAlignedBB AABB_EAST = new AxisAlignedBB(-0.125D, 0.375D, 0.0D, 0.0D, 0.625D, 1.0D);

    public BlockGuillotine(Material materialIn) {
        super(materialIn);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.DOWN));
        this.setHardness(2.0F);
        this.setResistance(10.0F);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(FACING)) {
            case UP:
                return AABB_UP;
            case NORTH:
                return AABB_NORTH;
            case SOUTH:
                return AABB_SOUTH;
            case WEST:
                return AABB_WEST;
            case EAST:
                return AABB_EAST;
            case DOWN:
            default:
                return AABB_DOWN;
        }
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.values()[meta % 6]);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityGuillotine();
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        if (!worldIn.isRemote) {
            boolean isPowered = worldIn.isBlockPowered(pos);
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileEntityGuillotine) {
                TileEntityGuillotine guillotine = (TileEntityGuillotine) te;
                if (isPowered && !guillotine.isPoweredLastTick()) {
                    guillotine.activate();
                }
                guillotine.setPoweredLastTick(isPowered);
            }
        }
    }
}
