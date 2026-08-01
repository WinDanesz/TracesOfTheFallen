package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import com.windanesz.tracesofthefallen.init.ModCreativeTab;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockStonePressurePlate extends Block {

    public static final PropertyDirection FACING = BlockDirectional.FACING;
    public static final PropertyBool ACTIVATED = PropertyBool.create("activated");

    public BlockStonePressurePlate() {
        super(Material.ROCK);
        this.setHardness(1.5F);
        this.setResistance(10.0F);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.UP).withProperty(ACTIVATED, false));
        this.setCreativeTab(ModCreativeTab.TOTF_TAB);
    }

    protected AxisAlignedBB getTriggerBox(BlockPos pos, EnumFacing facing) {
        double d = 0.125D;
        switch(facing) {
            case UP: return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1 + d, pos.getZ() + 1);
            case DOWN: return new AxisAlignedBB(pos.getX(), pos.getY() - d, pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            case NORTH: return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ() - d, pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            case SOUTH: return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1 + d);
            case WEST: return new AxisAlignedBB(pos.getX() - d, pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            case EAST: return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1 + d, pos.getY() + 1, pos.getZ() + 1);
        }
        return new AxisAlignedBB(pos);
    }



    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, ACTIVATED);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        EnumFacing facing = blockState.getValue(FACING);
        double d = 0.0625D; // 1 pixel inward
        switch (facing) {
            case UP:
                return new AxisAlignedBB(0, 0, 0, 1, 1 - d, 1);
            case DOWN:
                return new AxisAlignedBB(0, d, 0, 1, 1, 1);
            case NORTH:
                return new AxisAlignedBB(0, 0, d, 1, 1, 1);
            case SOUTH:
                return new AxisAlignedBB(0, 0, 0, 1, 1, 1 - d);
            case WEST:
                return new AxisAlignedBB(d, 0, 0, 1, 1, 1);
            case EAST:
                return new AxisAlignedBB(0, 0, 0, 1 - d, 1, 1);
        }
        return FULL_BLOCK_AABB;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.byIndex(meta & 7);
        boolean activated = (meta & 8) > 0;
        return this.getDefaultState().withProperty(FACING, facing).withProperty(ACTIVATED, activated);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(FACING).getIndex();
        if (state.getValue(ACTIVATED)) {
            meta |= 8;
        }
        return meta;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return this.getDefaultState().withProperty(FACING, facing).withProperty(ACTIVATED, false);
    }

    @Override
    public int tickRate(World worldIn) {
        return 20;
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        if (!worldIn.isRemote) {
            if (!state.getValue(ACTIVATED)) {
                this.updateState(worldIn, pos, state);
            }
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            if (!state.getValue(ACTIVATED)) {
                this.setState(worldIn, pos, state, true);
                worldIn.scheduleUpdate(new BlockPos(pos), this, this.tickRate(worldIn));
            } else {
                // If clicked while active, just refresh the timer
                worldIn.scheduleUpdate(new BlockPos(pos), this, this.tickRate(worldIn));
            }
        }
        return true;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.isRemote) {
            if (state.getValue(ACTIVATED)) {
                this.updateState(worldIn, pos, state);
            }
        }
    }

    private void updateState(World worldIn, BlockPos pos, IBlockState state) {
        boolean currentlyActivated = state.getValue(ACTIVATED);
        boolean shouldBeActivated = false;

        List<Entity> list = worldIn.getEntitiesWithinAABBExcludingEntity(null, this.getTriggerBox(pos, state.getValue(FACING)));
        if (!list.isEmpty()) {
            for (Entity entity : list) {
                if (!entity.doesEntityNotTriggerPressurePlate()) {
                    shouldBeActivated = true;
                    break;
                }
            }
        }

        if (shouldBeActivated && !currentlyActivated) {
            this.setState(worldIn, pos, state, true);
        } else if (!shouldBeActivated && currentlyActivated) {
            this.setState(worldIn, pos, state, false);
        }

        if (shouldBeActivated) {
            worldIn.scheduleUpdate(new BlockPos(pos), this, this.tickRate(worldIn));
        }
    }

    private void setState(World worldIn, BlockPos pos, IBlockState state, boolean activated) {
        worldIn.setBlockState(pos, state.withProperty(ACTIVATED, activated), 3);
        if (activated) {
            worldIn.playSound(null, pos, SoundEvents.BLOCK_STONE_PRESSPLATE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.6F);
            this.triggerReceivers(worldIn, pos);
        } else {
            worldIn.playSound(null, pos, SoundEvents.BLOCK_STONE_PRESSPLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.5F);
        }
    }

    private void triggerReceivers(World worldIn, BlockPos pos) {
        int radius = Settings.miscSettings.stonePressurePlateSearchRadius;
        BlockPos minPos = pos.add(-radius, -radius, -radius);
        BlockPos maxPos = pos.add(radius, radius, radius);

        for (BlockPos targetPos : BlockPos.getAllInBoxMutable(minPos, maxPos)) {
            IBlockState targetState = worldIn.getBlockState(targetPos);
            if (targetState.getBlock() == ModBlocks.stone_receiver) {
                TileEntity te = worldIn.getTileEntity(targetPos);
                if (te instanceof TileEntityStoneReceiver) {
                    ((TileEntityStoneReceiver) te).trigger();
                }
            }
        }
    }
}
