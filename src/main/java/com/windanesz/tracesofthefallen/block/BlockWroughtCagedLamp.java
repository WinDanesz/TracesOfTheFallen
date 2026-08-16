package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockWroughtCagedLamp extends BlockLampBase {
    private static final AxisAlignedBB AABB_UP = new AxisAlignedBB(0.1875D, 0.0D, 0.1875D, 0.8125D, 1.125D, 0.8125D);
    private static final AxisAlignedBB AABB_DOWN = new AxisAlignedBB(0.1875D, -0.125D, 0.1875D, 0.8125D, 1.0D, 0.8125D);
    private static final AxisAlignedBB AABB_NORTH = new AxisAlignedBB(0.1875D, -0.25D, 0.1875D, 0.8125D, 0.625D, 1.0D);
    private static final AxisAlignedBB AABB_SOUTH = new AxisAlignedBB(0.1875D, -0.25D, 0.0D, 0.8125D, 0.625D, 0.8125D);
    private static final AxisAlignedBB AABB_WEST = new AxisAlignedBB(0.1875D, -0.25D, 0.1875D, 1.0D, 0.625D, 0.8125D);
    private static final AxisAlignedBB AABB_EAST = new AxisAlignedBB(0.0D, -0.25D, 0.1875D, 0.8125D, 0.625D, 0.8125D);

    public BlockWroughtCagedLamp() {
        super(Material.IRON);
        this.setHardness(3.5F);
        this.setSoundType(SoundType.METAL);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(FACING)) {
            case UP:
                return AABB_UP;
            case DOWN:
                return AABB_DOWN;
            case NORTH:
                return AABB_NORTH;
            case SOUTH:
                return AABB_SOUTH;
            case WEST:
                return AABB_WEST;
            case EAST:
                return AABB_EAST;
            default:
                return super.getBoundingBox(state, source, pos);
        }
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityWroughtCagedLamp) {
            return ((TileEntityWroughtCagedLamp) te).getLightLevel();
        }
        return 8; // Dim light
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityWroughtCagedLamp();
    }
}
