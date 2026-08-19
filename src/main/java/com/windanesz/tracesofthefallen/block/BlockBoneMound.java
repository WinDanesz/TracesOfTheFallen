package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockBoneMound extends BlockBush {
    
    protected static final AxisAlignedBB BONE_MOUND_AABB = new AxisAlignedBB(0.1D, 0.0D, 0.1D, 0.9D, 0.55D, 0.9D);

    public BlockBoneMound() {
        super();
        this.setSoundType(SoundType.GROUND);
        this.setHardness(0.8F);
    }
    
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BONE_MOUND_AABB;
    }

    @Override
    protected boolean canSustainBush(IBlockState state) {
        return super.canSustainBush(state) || state.isTopSolid() || (state.getBlock() instanceof BlockBonepile && state.getValue(BlockBonepile.LAYERS) == 8);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        Random rand = world instanceof World ? ((World)world).rand : new Random();
        int count = 1 + rand.nextInt(2);
        drops.add(new ItemStack(Items.BONE, count));
    }
}
