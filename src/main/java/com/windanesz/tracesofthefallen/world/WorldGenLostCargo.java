package com.windanesz.tracesofthefallen.world;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import com.windanesz.tracesofthefallen.block.BlockTOFT;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Random;

public class WorldGenLostCargo implements IWorldGenerator {

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (!ArrayUtils.contains(Settings.worldgenSettings.dimensionList, world.provider.getDimension())) {
            return;
        }

        if (Settings.worldgenSettings.lostCargoFrequency <= 0 || random.nextInt(Settings.worldgenSettings.lostCargoFrequency) != 0) {
            return;
        }

        int attempts = 8 + random.nextInt(8); // Try 8-15 positions per chunk
        for (int i = 0; i < attempts; i++) {
            int x = (chunkX << 4) + random.nextInt(16);
            int z = (chunkZ << 4) + random.nextInt(16);
            int y = world.getHeight(x, z) - 1;
            BlockPos pos = new BlockPos(x, y, z);

            Biome biome = world.getBiome(pos);
            ResourceLocation biomeRL = biome.getRegistryName();
            // Whitelist/blacklist logic
            if (!TracesOfTheFallen.settings.lostCargoBiomeWhitelist.isEmpty() && !TracesOfTheFallen.settings.lostCargoBiomeWhitelist.contains(biomeRL)) {
                continue;
            }
            if (TracesOfTheFallen.settings.lostCargoBiomeBlacklist.contains(biomeRL)) {
                continue;
            }

            // Only spawn on surface: block below must be solid, and current block must be air/replaceable/grass/flower
            BlockPos placePos = pos.up();
            if (!world.isAirBlock(placePos) && !world.getBlockState(placePos).getMaterial().isReplaceable() && !isGrassOrFlower(world, placePos)) {
                continue;
            }
            if (!world.getBlockState(pos).isTopSolid()) {
                continue;
            }

            EnumFacing facing = EnumFacing.Plane.HORIZONTAL.random(random);
            world.setBlockState(placePos, ModBlocks.lost_cargo.getDefaultState().withProperty(BlockTOFT.FACING, facing), 2);
            break; // Only place one per chunk
        }
    }

    private boolean isGrassOrFlower(World world, BlockPos pos) {
        String name = world.getBlockState(pos).getBlock().getRegistryName().toString();
        return name.contains("grass") || name.contains("flower");
    }
}
