package com.windanesz.tracesofthefallen.world;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.block.BlockGlassFloat;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.IWorldGenerator;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Random;

/** Generates landed glass floats on sand/gravel in beach biomes. */
public class WorldGenBeachedGlassFloat implements IWorldGenerator {

	private static final long SEED_MODIFIER = 73194827L;
	private static final int CLEAR_BLUE_PERCENT = 70;

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		int chance = Settings.worldgenSettings.beachedGlassFloatChance;
		if (chance <= 0 || !ArrayUtils.contains(Settings.worldgenSettings.dimensionList, world.provider.getDimension())) {
			return;
		}

		random.setSeed(random.nextLong() + SEED_MODIFIER);
		if (random.nextInt(chance) != 0) {
			return;
		}

		// Offset by 8 so the position stays within the area populated by this chunk
		int x = (chunkX << 4) + 8 + random.nextInt(16);
		int z = (chunkZ << 4) + 8 + random.nextInt(16);

		Biome biome = world.getBiome(new BlockPos(x, 0, z));
		if (!BiomeDictionary.hasType(biome, BiomeDictionary.Type.BEACH)) {
			return;
		}

		BlockPos groundPos = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).down();
		Block ground = world.getBlockState(groundPos).getBlock();
		if (ground != Blocks.SAND && ground != Blocks.GRAVEL) {
			return;
		}

		BlockPos placePos = groundPos.up();
		IBlockState above = world.getBlockState(placePos);
		if (!world.isAirBlock(placePos) && !(above.getMaterial().isReplaceable() && !above.getMaterial().isLiquid())) {
			return;
		}

		world.setBlockState(placePos, getLandedBlock(pickVariant(random)).getDefaultState(), 2);
	}

	/** Clear blue is the common variant; the other three are rarer collectibles. */
	private static BlockGlassFloat.FloatVariant pickVariant(Random random) {
		if (random.nextInt(100) < CLEAR_BLUE_PERCENT) {
			return BlockGlassFloat.FloatVariant.CLEAR_BLUE;
		}
		BlockGlassFloat.FloatVariant[] rare = {
				BlockGlassFloat.FloatVariant.WINE_GREEN,
				BlockGlassFloat.FloatVariant.LONGING_BLUE,
				BlockGlassFloat.FloatVariant.BLOOD_RED
		};
		return rare[random.nextInt(rare.length)];
	}

	private static Block getLandedBlock(BlockGlassFloat.FloatVariant variant) {
		switch (variant) {
			case WINE_GREEN:
				return ModBlocks.glass_float_wine_green_landed;
			case LONGING_BLUE:
				return ModBlocks.glass_float_longing_blue_landed;
			case BLOOD_RED:
				return ModBlocks.glass_float_blood_red_landed;
			case CLEAR_BLUE:
			default:
				return ModBlocks.glass_float_clear_blue_landed;
		}
	}
}
