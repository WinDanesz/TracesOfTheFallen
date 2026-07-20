package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class BlockTOTFGlass extends Block {

	public BlockTOTFGlass() {
		super(Material.GLASS);
		setHardness(0.3F);
		setSoundType(SoundType.GLASS);
	}
}
