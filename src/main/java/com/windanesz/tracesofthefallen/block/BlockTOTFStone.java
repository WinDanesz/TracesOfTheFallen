package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class BlockTOTFStone extends Block {

	public BlockTOTFStone() {
		super(Material.ROCK);
		setHardness(1.5F);
		setResistance(10.0F);
		setSoundType(SoundType.STONE);
		setHarvestLevel("pickaxe", 0);
	}
}
