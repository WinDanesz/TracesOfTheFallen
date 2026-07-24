package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockBrassGasLamp extends BlockLampBase {
    public static final PropertyInteger LIGHT_LEVEL = PropertyInteger.create("light", 0, 2);

    public BlockBrassGasLamp() {
        super(Material.IRON);
        this.setHardness(3.5F);
        this.setSoundType(SoundType.METAL);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, LIGHT_LEVEL);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityBrassGasLamp) {
            return state.withProperty(LIGHT_LEVEL, ((TileEntityBrassGasLamp) te).getLampLightLevel());
        }
        return state.withProperty(LIGHT_LEVEL, 0);
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityBrassGasLamp) {
            int level = ((TileEntityBrassGasLamp) te).getLampLightLevel();
            if (level == 0) return 10;
            if (level == 1) return 11;
            if (level == 2) return 12;
        }
        return 10;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityBrassGasLamp();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (hand == EnumHand.MAIN_HAND && !playerIn.isSneaking()) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileEntityBrassGasLamp) {
                if (!worldIn.isRemote) {
                    ((TileEntityBrassGasLamp) te).cycleLightLevel();
                }
                return true;
            }
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }
}
