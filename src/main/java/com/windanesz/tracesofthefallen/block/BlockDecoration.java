package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.init.ModBlocks;
import com.windanesz.tracesofthefallen.init.ModCreativeTab;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A simple decorative block with a horizontal facing property.
 * No loot table, no tile entity - just a visual decoration that faces a direction.
 */
public class BlockDecoration extends Block {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    protected AxisAlignedBB boundingBox;

    public BlockDecoration(Material material) {
        super(material);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        setHardness(1.0F);
        setResistance(1.0F);
        setCreativeTab(ModCreativeTab.TOTF_TAB);
        this.boundingBox = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    }

    public BlockDecoration setBoundingBox(AxisAlignedBB bb) {
        this.boundingBox = bb;
        return this;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, net.minecraft.world.IBlockAccess source, BlockPos pos) {
        return this.boundingBox;
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        if (!super.canPlaceBlockAt(worldIn, pos)) {
            return false;
        }
        AxisAlignedBB bb = getBoundingBox(getDefaultState(), worldIn, pos);
        if (bb.maxY > 1.0D) {
            BlockPos up = pos.up();
            IBlockState upState = worldIn.getBlockState(up);
            if (!upState.getBlock().isReplaceable(worldIn, up)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        AxisAlignedBB bb = getBoundingBox(state, worldIn, pos);
        if (bb.maxY > 1.0D && !worldIn.isRemote) {
            BlockPos up = pos.up();
            if (worldIn.getBlockState(up).getBlock().isReplaceable(worldIn, up) || worldIn.isAirBlock(up)) {
                worldIn.setBlockState(up, ModBlocks.technical_block.getDefaultState(), 3);
            }
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        AxisAlignedBB bb = getBoundingBox(state, worldIn, pos);
        if (bb.maxY > 1.0D) {
            BlockPos up = pos.up();
            if (worldIn.getBlockState(up).getBlock() == ModBlocks.technical_block) {
                worldIn.setBlockToAir(up);
            }
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        AxisAlignedBB bb = getBoundingBox(state, worldIn, pos);
        if (bb.maxY > 1.0D && !worldIn.isRemote) {
            BlockPos up = pos.up();
            if (worldIn.getBlockState(up).getBlock() != ModBlocks.technical_block) {
                if (worldIn.getBlockState(up).getBlock().isReplaceable(worldIn, up) || worldIn.isAirBlock(up)) {
                    worldIn.setBlockState(up, ModBlocks.technical_block.getDefaultState(), 3);
                }
            }
        }
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
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
    public boolean isSideSolid(IBlockState base_state, net.minecraft.world.IBlockAccess world, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }
}

