package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockWroughtBars extends Block {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyBool TOP = PropertyBool.create("top");
    public static final PropertyBool CORNER = PropertyBool.create("corner");

    private static final AxisAlignedBB AABB_NS = new AxisAlignedBB(0.0D, 0.0D, 0.46875D, 1.0D, 1.0D, 0.53125D);
    private static final AxisAlignedBB AABB_EW = new AxisAlignedBB(0.46875D, 0.0D, 0.0D, 0.53125D, 1.0D, 1.0D);
    private static final AxisAlignedBB AABB_CORNER_NORTH = new AxisAlignedBB(0.46875D, 0.0D, 0.46875D, 1.0D, 1.0D, 1.0D);
    private static final AxisAlignedBB AABB_CORNER_EAST = new AxisAlignedBB(0.0D, 0.0D, 0.46875D, 0.53125D, 1.0D, 1.0D);
    private static final AxisAlignedBB AABB_CORNER_SOUTH = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.53125D, 1.0D, 0.53125D);
    private static final AxisAlignedBB AABB_CORNER_WEST = new AxisAlignedBB(0.46875D, 0.0D, 0.0D, 1.0D, 1.0D, 0.53125D);

    public BlockWroughtBars() {
        super(Material.IRON);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(TOP, false).withProperty(CORNER, false));
        this.setHardness(5.0F);
        this.setResistance(10.0F);
        this.setSoundType(net.minecraft.block.SoundType.METAL);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        EnumFacing facing = state.getValue(FACING);
        if (state.getValue(CORNER)) {
            switch (facing) {
                case NORTH: return AABB_CORNER_NORTH;
                case EAST:  return AABB_CORNER_EAST;
                case SOUTH: return AABB_CORNER_SOUTH;
                case WEST:  return AABB_CORNER_WEST;
                default: return AABB_NS;
            }
        }
        if (facing.getAxis() == EnumFacing.Axis.Z) {
            return AABB_NS;
        } else {
            return AABB_EW;
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
    public net.minecraft.util.BlockRenderLayer getRenderLayer() {
        return net.minecraft.util.BlockRenderLayer.CUTOUT;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite()).withProperty(CORNER, placer.isSneaking());
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return state.withProperty(TOP, worldIn.isAirBlock(pos.up()));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.byIndex(meta & 7);
        if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
            enumfacing = EnumFacing.NORTH;
        }
        return this.getDefaultState().withProperty(FACING, enumfacing).withProperty(CORNER, (meta & 8) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = state.getValue(FACING).getIndex();
        if (state.getValue(CORNER)) {
            i |= 8;
        }
        return i;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, net.minecraft.entity.player.EntityPlayer playerIn, net.minecraft.util.EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (playerIn.getHeldItemMainhand().isEmpty()) {
            if (!worldIn.isRemote) {
                worldIn.setBlockState(pos, state.cycleProperty(CORNER), 3);
            }
            return true;
        }
        return false;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, TOP, CORNER);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }
}
