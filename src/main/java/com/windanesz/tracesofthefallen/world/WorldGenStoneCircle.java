package com.windanesz.tracesofthefallen.world;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.block.BlockStoneCircle;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Random;

public class WorldGenStoneCircle implements IWorldGenerator {

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (!ArrayUtils.contains(Settings.worldgenSettings.dimensionList, world.provider.getDimension())) {
            return;
        }

        if (Settings.worldgenSettings.stoneCircleChance <= 0 || random.nextInt(Settings.worldgenSettings.stoneCircleChance) != 0) {
            return;
        }

        int x = (chunkX << 4) + random.nextInt(16);
        int z = (chunkZ << 4) + random.nextInt(16);

        BlockPos pos = findSuitableSurface(world, x, z);

        if (pos != null) {
            IBlockState state = ModBlocks.stone_circle.getDefaultState().withProperty(BlockStoneCircle.FACING, EnumFacing.Plane.HORIZONTAL.random(random));
            world.setBlockState(pos, state, 2);
        }
    }

    private BlockPos findSuitableSurface(World world, int x, int z) {
		BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(x, world.getHeight(x, z), z);

		while (mutablePos.getY() > 0) {
			mutablePos.setY(mutablePos.getY() - 1);
			IBlockState groundState = world.getBlockState(mutablePos);
			Block groundBlock = groundState.getBlock();

			// Check if ground is solid and not something we want to avoid (like leaves, wood, or water)
			if (groundState.getMaterial().isSolid() && groundBlock.isOpaqueCube(groundState) && !groundState.getMaterial().isLiquid() &&
					!groundBlock.isLeaves(groundState, world, mutablePos) && !groundBlock.isWood(world, mutablePos)) {

				BlockPos placementPos = mutablePos.toImmutable().up();
				IBlockState placementState = world.getBlockState(placementPos);

				// Check if there's air or replaceable block to place the circle
				if (world.isAirBlock(placementPos) || placementState.getMaterial().isReplaceable()) {
					return placementPos;
				}
			}
		}
		return null;
	}
}
