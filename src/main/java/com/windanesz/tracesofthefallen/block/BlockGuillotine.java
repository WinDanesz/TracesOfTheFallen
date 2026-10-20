package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockGuillotine extends BlockContainer {
    public enum EnumAttachFace implements IStringSerializable {
        FLOOR("floor"),
        WALL("wall"),
        CEILING("ceiling");
        private final String name;
        EnumAttachFace(String name) { this.name = name; }
        @Override public String getName() { return name; }
    }

    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyEnum<EnumAttachFace> FACE = PropertyEnum.create("face", EnumAttachFace.class);

    private static final AxisAlignedBB AABB_CEILING_Z = new AxisAlignedBB(0.0D, 0.875D, 0.375D, 1.0D, 1.0D, 0.625D);
    private static final AxisAlignedBB AABB_CEILING_X = new AxisAlignedBB(0.375D, 0.875D, 0.0D, 0.625D, 1.0D, 1.0D);
    private static final AxisAlignedBB AABB_FLOOR_Z = new AxisAlignedBB(0.0D, 0.0D, 0.375D, 1.0D, 0.125D, 0.625D);
    private static final AxisAlignedBB AABB_FLOOR_X = new AxisAlignedBB(0.375D, 0.0D, 0.0D, 0.625D, 0.125D, 1.0D);
    private static final AxisAlignedBB AABB_NORTH = new AxisAlignedBB(0.0D, 0.375D, 0.875D, 1.0D, 0.625D, 1.0D);
    private static final AxisAlignedBB AABB_SOUTH = new AxisAlignedBB(0.0D, 0.375D, 0.0D, 1.0D, 0.625D, 0.125D);
    private static final AxisAlignedBB AABB_WEST = new AxisAlignedBB(0.875D, 0.375D, 0.0D, 1.0D, 0.625D, 1.0D);
    private static final AxisAlignedBB AABB_EAST = new AxisAlignedBB(0.0D, 0.375D, 0.0D, 0.125D, 0.625D, 1.0D);

    public BlockGuillotine(Material materialIn) {
        super(materialIn);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACE, EnumAttachFace.CEILING).withProperty(FACING, EnumFacing.NORTH));
        this.setHardness(2.0F);
        this.setResistance(10.0F);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        EnumAttachFace face = state.getValue(FACE);
        EnumFacing facing = state.getValue(FACING);

        if (face == EnumAttachFace.WALL) {
            switch (facing) {
                case NORTH: return AABB_NORTH;
                case SOUTH: return AABB_SOUTH;
                case WEST: return AABB_WEST;
                case EAST: return AABB_EAST;
            }
        } else if (face == EnumAttachFace.CEILING) {
            return (facing.getAxis() == EnumFacing.Axis.Z) ? AABB_CEILING_Z : AABB_CEILING_X;
        } else {
            return (facing.getAxis() == EnumFacing.Axis.Z) ? AABB_FLOOR_Z : AABB_FLOOR_X;
        }
        return AABB_CEILING_Z;
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
        EnumAttachFace attachFace = EnumAttachFace.WALL;
        if (facing == EnumFacing.UP) attachFace = EnumAttachFace.FLOOR;
        else if (facing == EnumFacing.DOWN) attachFace = EnumAttachFace.CEILING;
        
        EnumFacing horizontalFacing = placer.getHorizontalFacing().getOpposite();
        if (attachFace == EnumAttachFace.WALL) {
            horizontalFacing = facing;
        }
        
        return this.getDefaultState().withProperty(FACE, attachFace).withProperty(FACING, horizontalFacing);
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int faceIndex = state.getValue(FACE).ordinal();
        int facingIndex = state.getValue(FACING).getHorizontalIndex();
        return (faceIndex * 4) + facingIndex;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        int faceIndex = meta / 4;
        int facingIndex = meta % 4;
        EnumAttachFace face = EnumAttachFace.WALL;
        if (faceIndex == 0) face = EnumAttachFace.FLOOR;
        else if (faceIndex == 2) face = EnumAttachFace.CEILING;
        
        return this.getDefaultState()
                .withProperty(FACE, face)
                .withProperty(FACING, EnumFacing.byHorizontalIndex(facingIndex));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACE, FACING);
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
            if (te instanceof TileEntityGuillotine guillotine) {
                if (isPowered && !guillotine.isPoweredLastTick()) {
                    guillotine.activate();
                }
                guillotine.setPoweredLastTick(isPowered);
            }
        }
    }
}
