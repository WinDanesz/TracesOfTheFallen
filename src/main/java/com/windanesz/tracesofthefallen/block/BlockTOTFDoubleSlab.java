package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;

import java.util.Random;

public class BlockTOTFDoubleSlab extends BlockTOTFSlab {
	private final Block halfSlab;

	public BlockTOTFDoubleSlab(Material material, Block halfSlab) {
		super(material);
		this.halfSlab = halfSlab;
	}

	@Override
	public boolean isDouble() {
		return true;
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(this.halfSlab);
	}
}
