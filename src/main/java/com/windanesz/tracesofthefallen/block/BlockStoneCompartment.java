package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.network.ModGuiHandler;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
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
import net.minecraft.world.World;

public class BlockStoneCompartment extends Block implements ITileEntityProvider {
    public static final PropertyInteger OPEN = PropertyInteger.create("open", 0, 3);

    public BlockStoneCompartment() {
        super(Material.ROCK);
        this.setHardness(2.0F);
        this.setResistance(10.0F);
        this.setSoundType(SoundType.STONE);
        this.setDefaultState(this.blockState.getBaseState().withProperty(OPEN, 0));
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            if (state.getValue(OPEN) == 3) {
                playerIn.openGui(TracesOfTheFallen.instance, ModGuiHandler.GUI_STONE_COMPARTMENT, worldIn, pos.getX(), pos.getY(), pos.getZ());
            }
        }
        return true;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, OPEN);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(OPEN, meta);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(OPEN);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityStoneCompartment();
    }
}
