package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class BlockTOTFPillar extends BlockRotatedPillar {
	public BlockTOTFPillar(Material material) {
		super(material);
		setHardness(1.5F);
		setResistance(10.0F);
		setSoundType(SoundType.STONE);
		setHarvestLevel("pickaxe", 0);
	}
}
