package com.windanesz.tracesofthefallen.world.structure;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public class WorldGenMultipartDelegate implements IWorldGenerator {
    private MapGenMultipart mapGen = new MapGenMultipart();

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.getDimension() == 0) { // Overworld only
            // 1. Calculate structure locations and build the layout blueprint (doesn't place blocks)
            mapGen.generate(world, chunkX, chunkZ, null);
            
            // 2. Actually place the blocks for the current chunk ONLY
            mapGen.generateStructure(world, random, new ChunkPos(chunkX, chunkZ));
        }
    }
}
