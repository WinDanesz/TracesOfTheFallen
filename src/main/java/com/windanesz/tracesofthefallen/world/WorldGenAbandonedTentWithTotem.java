package com.windanesz.tracesofthefallen.world;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.block.BlockTOFT;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class WorldGenAbandonedTentWithTotem extends WorldGenBase {

	@Override
	public int getFrequency() {
		return Settings.worldgenSettings.abandonedTentWithTotemFrequency;
	}

	@Override
	public List<ResourceLocation> getBiomeWhitelist() {
		return TracesOfTheFallen.settings.abandonedTentWithTotemBiomeWhitelist;
	}

	@Override
	public List<ResourceLocation> getBiomeBlacklist() {
		return TracesOfTheFallen.settings.abandonedTentWithTotemBiomeBlacklist;
	}

	@Override
	public IBlockState getBlockState(Random random, World world, BlockPos pos) {
		EnumFacing facing = EnumFacing.Plane.HORIZONTAL.random(random);
		boolean isSnowy = world.getBiome(pos).isSnowyBiome();
		return ModBlocks.tent_abandoned_idol.getDefaultState().withProperty(BlockTOFT.FACING, facing).withProperty(BlockTOFT.SNOWY, isSnowy);
	}

	@Override
	public int getRandomSeedModifier() {
		return 271371;
	}
}
