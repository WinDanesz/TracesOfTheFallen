package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockDeskBell extends BlockDecoration {

	public BlockDeskBell(Material material) {
		super(material);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) {
			return true;
		}

		worldIn.playSound(null, pos, SoundEvents.BLOCK_NOTE_BELL, SoundCategory.BLOCKS, 0.7F, 1.4F);
		return true;
	}
}
