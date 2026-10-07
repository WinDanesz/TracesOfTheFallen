package com.windanesz.tracesofthefallen.world.structure;

import com.windanesz.tracesofthefallen.Settings;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

/**
 * Hooks the goblin cavern structure into population. The cavern is wider than a population region, so it
 * is planned from nearby chunks and placed one chunk at a time, like the multipart dungeon.
 */
public class WorldGenGoblinCavernDelegate implements IWorldGenerator {

    // A generator remembers every start it has seen and its saved data, so each world needs its own.
    private final Map<World, MapGenGoblinCavern> generators = Collections.synchronizedMap(new WeakHashMap<>());

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (Settings.worldgenSettings.goblinCavernSpacing <= 0
                || !ArrayUtils.contains(Settings.worldgenSettings.dimensionList, world.provider.getDimension())) {
            return;
        }

        MapGenGoblinCavern generator = generators.computeIfAbsent(world, ignored -> new MapGenGoblinCavern());
        try {
            // 1. Plan any cavern whose start is near this chunk (no blocks placed yet).
            generator.generate(world, chunkX, chunkZ, null);
            // 2. Place only the part of each cavern that falls inside this chunk's population area.
            generator.generateStructure(world, random, new ChunkPos(chunkX, chunkZ));
        } finally {
            generator.detachWorld();
        }
    }
}
