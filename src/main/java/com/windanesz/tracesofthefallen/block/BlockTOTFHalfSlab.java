package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.material.Material;

public class BlockTOTFHalfSlab extends BlockTOTFSlab {
	public BlockTOTFHalfSlab(Material material) {
		super(material);
	}

	@Override
	public boolean isDouble() {
		return false;
	}
}
