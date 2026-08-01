package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.BlockPane;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockTOTFGlassPane extends BlockPane {

	public BlockTOTFGlassPane() {
		super(Material.GLASS, false);
		setHardness(0.3F);
		setSoundType(SoundType.GLASS);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}
}
