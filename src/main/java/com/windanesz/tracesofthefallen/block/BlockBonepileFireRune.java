package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockBonepileFireRune extends Block {

	public BlockBonepileFireRune() {
		super(Material.ROCK);
		this.setHardness(0.5F);
		this.setResistance(2.0F);
		this.setSoundType(BlockBonepile.SOUND_TYPE_BONEPILE);
		this.setHarvestLevel("pickaxe", 0);
	}

	/** Sustains fire on the top face, like netherrack. */
	@Override
	public boolean isFireSource(World world, BlockPos pos, EnumFacing side) {
		return side == EnumFacing.UP;
	}
}
