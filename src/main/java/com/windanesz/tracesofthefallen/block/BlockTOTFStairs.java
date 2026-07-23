package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;

public class BlockTOTFStairs extends BlockStairs {
	public BlockTOTFStairs(IBlockState modelState) {
		super(modelState);
		this.useNeighborBrightness = true;
	}
}
