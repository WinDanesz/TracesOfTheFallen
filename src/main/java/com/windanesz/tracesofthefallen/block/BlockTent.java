package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockTent extends BlockTOFT {
	
	private final boolean shouldSpawnGoblins;
	
	public BlockTent() {
		this(false);
	}
	
	public BlockTent(boolean shouldSpawnGoblins) {
		super(Material.WOOD);
		this.shouldSpawnGoblins = shouldSpawnGoblins;
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if (shouldSpawnGoblins) {
			return new TileEntityTent();
		}
		return new TileEntityLostLoot();
	}
}
