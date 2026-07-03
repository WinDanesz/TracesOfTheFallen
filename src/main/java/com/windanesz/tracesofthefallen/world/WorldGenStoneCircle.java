package com.windanesz.tracesofthefallen.world;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.block.BlockStoneCircle;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.Random;

public class WorldGenStoneCircle extends WorldGenBase {

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		random.setSeed(random.nextLong() + getRandomSeedModifier());

		if (!ArrayUtils.contains(Settings.worldgenSettings.dimensionList, world.provider.getDimension())) {
			return;
		}

		if (getFrequency() <= 0 || random.nextInt(getFrequency()) != 0) {
			return;
		}

		int attempts = 8 + random.nextInt(8);
		for (int i = 0; i < attempts; i++) {
			int x = (chunkX << 4) + 8 + random.nextInt(8) - 4;
			int z = (chunkZ << 4) + 8 + random.nextInt(8) - 4;

			BlockPos topPos = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z));
			if (topPos.getY() >= world.getActualHeight()) {
				topPos = new BlockPos(x, random.nextInt(world.getActualHeight()), z);
			}

			Integer surfaceY = getNearestSolidSurface(world, topPos, false, Settings.worldgenSettings.surfaceSearchRange);
			if (surfaceY == null) {
				continue;
			}

			BlockPos groundPos = new BlockPos(x, surfaceY, z);
			Biome biome = world.getBiome(groundPos);
			ResourceLocation biomeRL = biome.getRegistryName();

			if (!getBiomeWhitelist().isEmpty() && !getBiomeWhitelist().contains(biomeRL)) {
				continue;
			}
			if (getBiomeBlacklist().contains(biomeRL)) {
				continue;
			}
			if (!BlockStoneCircle.canGenerateStructureAt(world, groundPos)) {
				continue;
			}

			BlockPos placePos = groundPos.up();
			BlockStoneCircle.placeStructure(world, placePos, getBlockState(random, world, placePos), 2);
			break;
		}
	}

	@Override
	public int getFrequency() {
		return Settings.worldgenSettings.stoneCircleChance;
	}

	@Override
	public List<ResourceLocation> getBiomeWhitelist() {
		return TracesOfTheFallen.settings.stoneCircleBiomeWhitelist;
	}

	@Override
	public List<ResourceLocation> getBiomeBlacklist() {
		return TracesOfTheFallen.settings.stoneCircleBiomeBlacklist;
	}

	@Override
	public IBlockState getBlockState(Random random, World world, BlockPos pos) {
		EnumFacing facing = EnumFacing.Plane.HORIZONTAL.random(random);
		boolean isSnowy = world.getBiome(pos).isSnowyBiome();
		return ModBlocks.stone_circle.getDefaultState().withProperty(BlockStoneCircle.FACING, facing).withProperty(BlockStoneCircle.SNOWY, isSnowy);
	}


	@Override
	public int getRandomSeedModifier() {
		return 645174;
	}
}
