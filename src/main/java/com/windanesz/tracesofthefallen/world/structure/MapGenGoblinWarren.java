package com.windanesz.tracesofthefallen.world.structure;

import com.windanesz.tracesofthefallen.Settings;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureStart;

import javax.annotation.Nullable;
import java.util.Random;

/** Lays out underground goblin warrens on a jittered grid, one per spacing x spacing chunk cell. */
public class MapGenGoblinWarren extends MapGenStructure {

    private static final int SALT = 20931845;
    private static final long START_SEED_MODIFIER = 918273645L;

    @Override
    public String getStructureName() {
        return "TOTF_GoblinWarren";
    }

    private static int spacing() {
        return Math.max(2, Settings.worldgenSettings.goblinWarrenSpacing);
    }

    private static int separation() {
        return Math.max(1, spacing() / 3);
    }

    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
        if (Settings.worldgenSettings.goblinWarrenSpacing <= 0) {
            return false;
        }

        int spacing = spacing();
        int cellX = chunkX;
        int cellZ = chunkZ;
        if (cellX < 0) cellX -= spacing - 1;
        if (cellZ < 0) cellZ -= spacing - 1;
        cellX /= spacing;
        cellZ /= spacing;

        // Same seeding as World#setRandomSeed, which findNearestStructurePosBySpacing uses, but on a private
        // Random so population does not keep reseeding the world's shared one.
        Random random = new Random((long) cellX * 341873128712L + (long) cellZ * 132897987541L + this.world.getSeed() + SALT);
        int startX = cellX * spacing + random.nextInt(spacing - separation());
        int startZ = cellZ * spacing + random.nextInt(spacing - separation());
        return chunkX == startX && chunkZ == startZ;
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ) {
        Random random = new Random(this.world.getSeed() ^ ((long) chunkX * 341873128712L) ^ ((long) chunkZ * 132897987541L) ^ START_SEED_MODIFIER);
        return new StructureGoblinWarrenStart(this.world, random, chunkX, chunkZ);
    }

    @Nullable
    @Override
    public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean findUnexplored) {
        this.world = worldIn;
        return findNearestStructurePosBySpacing(worldIn, this, pos, spacing(), separation(), SALT, false, 100, findUnexplored);
    }

    /**
     * The shared generator keeps one instance per world, and MapGenBase holds its world strongly; letting go of
     * it between uses means an unloaded world is not kept alive by the generator that served it.
     */
    void detachWorld() {
        this.world = null;
    }
}
