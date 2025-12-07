package com.windanesz.tracesofthefallen.world;

import com.windanesz.tracesofthefallen.Settings;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public abstract class WorldGenBase implements IWorldGenerator {

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		random.setSeed(random.nextLong() + getRandomSeedModifier());

		if (!ArrayUtils.contains(Settings.worldgenSettings.dimensionList, world.provider.getDimension())) {
			return;
		}

		if (getFrequency() <= 0 || random.nextInt(getFrequency()) != 0) {
			return;
		}

		int attempts = 8 + random.nextInt(8); // Try 8-15 positions per chunk
		for (int i = 0; i < attempts; i++) {
			// Offset by 8 to minimize cascading worldgen lag
			int x = (chunkX << 4) + 8 + random.nextInt(8) - 4;
			int z = (chunkZ << 4) + 8 + random.nextInt(8) - 4;
			
			// Get initial surface estimate
			BlockPos topPos = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z));
			
			// Check if we're at the top of the world (for cavern dimensions)
			if (topPos.getY() >= world.getActualHeight()) {
				topPos = new BlockPos(x, random.nextInt(world.getActualHeight()), z);
			}
			
			// Find the actual solid surface, searching downward
			Integer surfaceY = getNearestSolidSurface(world, topPos, false, Settings.worldgenSettings.surfaceSearchRange);
			
			if (surfaceY == null) {
				continue; // No suitable surface found
			}
			
			BlockPos groundPos = new BlockPos(x, surfaceY, z);

			// Biome checks
			Biome biome = world.getBiome(groundPos);
			ResourceLocation biomeRL = biome.getRegistryName();
			
			if (!getBiomeWhitelist().isEmpty() && !getBiomeWhitelist().contains(biomeRL)) {
				continue;
			}
			if (getBiomeBlacklist().contains(biomeRL)) {
				continue;
			}

			// Validate that the 3x3 area is flat enough
			if (!isFlat3x3Surface(world, groundPos, random)) {
				continue;
			}

			// Place the block
			BlockPos placePos = groundPos.up();
			world.setBlockState(placePos, getBlockState(random, world, placePos), 2);
			break; // Only place one per chunk
		}
	}

	public boolean isGrassOrFlower(World world, BlockPos pos) {
		String name = world.getBlockState(pos).getBlock().getRegistryName().toString();
		return name.contains("grass") || name.contains("flower");
	}

	/**
	 * Searches vertically from a starting position to find the nearest solid surface.
	 * Ignores leaves and other foliage blocks.
	 * 
	 * @param world The world
	 * @param startPos Starting position for the search
	 * @param searchUp If true, searches upward; if false, searches downward
	 * @param maxRange Maximum number of blocks to search
	 * @return The Y coordinate of the nearest solid surface, or null if none found
	 */
	@Nullable
	protected Integer getNearestSolidSurface(World world, BlockPos startPos, boolean searchUp, int maxRange) {
		BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(startPos);
		int direction = searchUp ? 1 : -1;
		
		for (int i = 0; i < maxRange; i++) {
			mutablePos.setY(mutablePos.getY() + direction);
			
			if (mutablePos.getY() < 0 || mutablePos.getY() >= world.getActualHeight()) {
				return null;
			}
			
			IBlockState state = world.getBlockState(mutablePos);
			
			// Check if this is a solid, collidable surface (not leaves/foliage)
			if (state.getMaterial().isSolid() && 
				state.getMaterial().blocksMovement() &&
				!state.getBlock().isLeaves(state, world, mutablePos) &&
				!state.getBlock().isFoliage(world, mutablePos)) {
				
				return mutablePos.getY();
			}
		}
		
		return null;
	}

	/**
	 * Validates that a 3x3 area around the center position is flat enough for structure placement.
	 * Checks all 9 positions in the 3x3 grid to ensure they have similar heights and are suitable for building.
	 * 
	 * @param world The world
	 * @param centerPos The center position of the 3x3 area (at ground level)
	 * @param random Random for additional checks
	 * @return True if the 3x3 area is suitable for structure placement
	 */
	protected boolean isFlat3x3Surface(World world, BlockPos centerPos, Random random) {
		int tolerance = Settings.worldgenSettings.flatSurfaceTolerance;
		int searchRange = Settings.worldgenSettings.surfaceSearchRange;
		boolean rejectLiquid = Settings.worldgenSettings.rejectLiquidIn3x3;
		
		int[] heights = new int[9];
		int index = 0;
		
		// Check all 9 positions in the 3x3 area
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				BlockPos checkPos = centerPos.add(x, 0, z);
				
				// Find the actual surface height at this position
				Integer surfaceY = getNearestSolidSurface(world, checkPos, false, searchRange);
				
				if (surfaceY == null) {
					return false; // No solid surface found
				}
				
				heights[index++] = surfaceY;
				
				BlockPos groundPos = new BlockPos(checkPos.getX(), surfaceY, checkPos.getZ());
				BlockPos abovePos = groundPos.up();
				
				// Check if ground block is solid
				if (!world.getBlockState(groundPos).isTopSolid()) {
					return false;
				}
				
				// Check if the block above is air/replaceable/grass/flower
				IBlockState aboveState = world.getBlockState(abovePos);
				if (!world.isAirBlock(abovePos) && 
					!aboveState.getMaterial().isReplaceable() && 
					!isGrassOrFlower(world, abovePos)) {
					return false;
				}
				
				// Check for liquids if configured
				if (rejectLiquid) {
					if (aboveState.getMaterial().isLiquid() || 
						world.getBlockState(groundPos).getMaterial().isLiquid()) {
						return false;
					}
				}
			}
		}
		
		// Calculate height variance in the 3x3 area
		int minHeight = Arrays.stream(heights).min().getAsInt();
		int maxHeight = Arrays.stream(heights).max().getAsInt();
		int heightDifference = maxHeight - minHeight;
		
		// Reject if the terrain is too uneven
		return heightDifference <= tolerance;
	}

	public abstract int getFrequency();

	public abstract List<ResourceLocation> getBiomeWhitelist();

	public abstract List<ResourceLocation> getBiomeBlacklist();

	public abstract IBlockState getBlockState(Random random, World world, BlockPos pos);

	public abstract int getRandomSeedModifier();

}
