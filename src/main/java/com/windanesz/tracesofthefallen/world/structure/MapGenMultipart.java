package com.windanesz.tracesofthefallen.world.structure;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureStart;

public class MapGenMultipart extends MapGenStructure {

    private final int maxDistance = 32;
    private final int minDistance = 8;

    @Override
    public String getStructureName() {
        return "TOTF_MultipartDungeon";
    }

    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
        int i = chunkX;
        int j = chunkZ;

        if (chunkX < 0) chunkX -= this.maxDistance - 1;
        if (chunkZ < 0) chunkZ -= this.maxDistance - 1;

        int k = chunkX / this.maxDistance;
        int l = chunkZ / this.maxDistance;
        this.rand.setSeed((long)k * 341873128712L + (long)l * 132897987541L + this.world.getWorldInfo().getSeed() + 10387312L);
        k = k * this.maxDistance;
        l = l * this.maxDistance;
        k = k + this.rand.nextInt(this.maxDistance - this.minDistance);
        l = l + this.rand.nextInt(this.maxDistance - this.minDistance);

        if (i == k && j == l) {
            return true;
        }

        return false;
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ) {
        return new StructureMultipartStart(this.world, this.rand, chunkX, chunkZ);
    }

    @Override
    public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean findUnexplored) {
        this.world = worldIn;
        return findNearestStructurePosBySpacing(worldIn, this, pos, this.maxDistance, this.minDistance, 10387312, true, 100, findUnexplored);
    }
}
