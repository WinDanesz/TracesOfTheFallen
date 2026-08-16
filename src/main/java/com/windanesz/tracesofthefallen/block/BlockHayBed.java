package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockHayBed extends BlockTOFT implements IProxyMainBlock {

    public static final PropertyBool OCCUPIED = PropertyBool.create("occupied");

    public BlockHayBed(Material material) {
        super(material);
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(FACING, EnumFacing.NORTH)
                .withProperty(SNOWY, false)
                .withProperty(OCCUPIED, false));
        setHardness(0.5F);
        setResistance(1.0F);
        setSpawnGoblins(false);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(FACING)) {
            case SOUTH:
            case NORTH:
            default:
                return new AxisAlignedBB(0.125D, 0.0D, 0.0D, 0.875D, 0.125D, 1.0D);
            case WEST:
            case EAST:
                return new AxisAlignedBB(0.0D, 0.0D, 0.125D, 1.0D, 0.125D, 0.875D);
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return getBoundingBox(blockState, worldIn, pos);
    }

    @Override
    public AxisAlignedBB getProxyCellAABB(IBlockState state, IBlockAccess world, BlockPos mainPos, BlockPos proxyPos) {
        // Proxy is the foot block; return the overhanging portion in the foot cell's [0,1] local space.
        switch (state.getValue(FACING)) {
            case NORTH: // foot is at dz=+1; bed extends to z=1.5625 in NORTH, so foot gets z [0, 0.5625]
                return new AxisAlignedBB(0.125D, 0.0D, 0.0D, 0.875D, 0.125D, 0.5625D);
            case SOUTH: // foot is at dz=-1; bed extends to z=-0.5625, so foot gets z [0.4375, 1]
                return new AxisAlignedBB(0.125D, 0.0D, 0.4375D, 0.875D, 0.125D, 1.0D);
            case WEST:  // foot is at dx=+1; bed extends to x=1.5625, so foot gets x [0, 0.5625]
                return new AxisAlignedBB(0.0D, 0.0D, 0.125D, 0.5625D, 0.125D, 0.875D);
            case EAST:  // foot is at dx=-1; bed extends to x=-0.5625, so foot gets x [0.4375, 1]
            default:
                return new AxisAlignedBB(0.4375D, 0.0D, 0.125D, 1.0D, 0.125D, 0.875D);
        }
    }

    @Override
    public boolean isMainBlockForProxy(IBlockAccess world, BlockPos mainPos, BlockPos proxyPos) {
        IBlockState state = world.getBlockState(mainPos);
        if (state.getBlock() != this) return false;
        return proxyPos.equals(mainPos.offset(state.getValue(FACING).getOpposite()));
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        if (!worldIn.isRemote) {
            BlockPos proxyPos = pos.offset(state.getValue(FACING).getOpposite());
            IBlockState proxyState = worldIn.getBlockState(proxyPos);
            if (proxyState.getBlock().isReplaceable(worldIn, proxyPos) || worldIn.isAirBlock(proxyPos)) {
                worldIn.setBlockState(proxyPos, ModBlocks.technical_block.getDefaultState(), 3);
            } else if (proxyState.getBlock() != ModBlocks.technical_block) {
                // Cannot place bed if the foot block space is occupied
                worldIn.setBlockToAir(pos);
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
                } else if (!worldIn.isAirBlock(proxyPos)) {
                    worldIn.setBlockToAir(pos);
                }
            }
        }
    }

    @Override
    public boolean isBed(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable Entity player) {
        return true;
    }

    @Override
    public EnumFacing getBedDirection(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(FACING);
    }

    public boolean isBedOccupied(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(OCCUPIED);
    }

    @Override
    public void setBedOccupied(IBlockAccess world, BlockPos pos, EntityPlayer player, boolean occupied) {
        if (world instanceof World) {
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() == this) {
                ((World) world).setBlockState(pos, state.withProperty(OCCUPIED, occupied), 4);
            }
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            return true;
        } else {
            if (worldIn.provider.canRespawnHere() && worldIn.getBiome(pos) != Biomes.HELL) {
                EntityPlayer.SleepResult result = playerIn.trySleep(pos);
                if (result == EntityPlayer.SleepResult.OK) {
                    return true;
                } else {
                    if (result == EntityPlayer.SleepResult.NOT_POSSIBLE_NOW) {
                        playerIn.sendMessage(new TextComponentTranslation("tile.bed.noSleep"));
                    } else if (result == EntityPlayer.SleepResult.NOT_SAFE) {
                        playerIn.sendMessage(new TextComponentTranslation("tile.bed.notSafe"));
                    } else if (result == EntityPlayer.SleepResult.TOO_FAR_AWAY) {
                        playerIn.sendMessage(new TextComponentTranslation("tile.bed.tooFarAway"));
                    }
                    return true;
                }
            } else {
                worldIn.setBlockToAir(pos);
                BlockPos proxyPos = pos.offset(state.getValue(FACING).getOpposite());
                if (worldIn.getBlockState(proxyPos).getBlock() == ModBlocks.technical_block) {
                    worldIn.setBlockToAir(proxyPos);
                }
                worldIn.newExplosion(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 5.0F, true, true);
                return true;
            }
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = state.getValue(FACING).getHorizontalIndex();
        if (state.getValue(SNOWY)) {
            i |= 4;
        }
        if (state.getValue(OCCUPIED)) {
            i |= 8;
        }
        return i;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3))
                .withProperty(SNOWY, (meta & 4) != 0)
                .withProperty(OCCUPIED, (meta & 8) != 0);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, SNOWY, OCCUPIED);
    }
}
