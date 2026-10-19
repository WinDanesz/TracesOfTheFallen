package com.windanesz.tracesofthefallen.world.structure;

import com.windanesz.tracesofthefallen.Settings;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.BiomeMesa;
import net.minecraft.world.gen.structure.*;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Retains vanilla starts, placement rules, structure data and locate behavior. */
public class MapGenMinecrawlerMineshaft extends MapGenMineshaft {
    public MapGenMinecrawlerMineshaft(MapGenMineshaft original) {
        // In particular, retain the chance from customized/superflat generator options.
        super(Collections.singletonMap("chance", String.valueOf((Double) ReflectionHelper.getPrivateValue(
                MapGenMineshaft.class, original, new String[]{"chance", "field_82673_e"}))));
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ) {
        StructureStart start = super.getStructureStart(chunkX, chunkZ);
        if (!containsDimension(world.provider.getDimension())) {
            return start;
        }
        int nestFrequency = Settings.worldgenSettings.minecrawlerMineshaftFrequency;
        int shaftFrequency = Settings.worldgenSettings.minecrawlerBroodShaftFrequency;
        if (nestFrequency <= 0 && shaftFrequency <= 0) {
            return start;
        }
        // Do not advance the vanilla generator's random stream for our extra content.
        long seed = world.getSeed() ^ (chunkX * 341873128712L) ^ (chunkZ * 132897987541L);
        boolean mesa = world.getBiome(new BlockPos((chunkX << 4) + 8, 64, (chunkZ << 4) + 8)) instanceof BiomeMesa;
        int maxFloorY = world.getSeaLevel() - 8;
        if (nestFrequency > 0) {
            Random random = new Random(seed ^ 913174639L);
            // Sprawling shafts can hold a second infested branch; the rolls stay on our own random stream.
            if (random.nextInt(nestFrequency) == 0 && attachNest(start, random, mesa, maxFloorY)
                    && random.nextInt(3) == 0) {
                attachNest(start, random, mesa, maxFloorY);
            }
        }
        if (shaftFrequency > 0) {
            Random random = new Random(seed ^ 0x2545F4914F6CDD1DL);
            if (random.nextInt(shaftFrequency) == 0) {
                attachBroodShaft(start, random, mesa, maxFloorY);
            }
        }
        return start;
    }

    private static boolean containsDimension(int dimension) {
        for (int configured : Settings.worldgenSettings.dimensionList) {
            if (configured == dimension) return true;
        }
        return false;
    }

    static boolean attachNest(StructureStart start, Random random, boolean mesa, int maxFloorY) {
        return attachBranch(start, random, 8, maxFloorY, (floor, facing, seed) ->
                new ComponentMinecrawlerNest(floor, facing, mesa, seed));
    }

    /** The shaft digs below its corridor, so the corridor must sit high enough to keep its floor above bedrock. */
    static boolean attachBroodShaft(StructureStart start, Random random, boolean mesa, int maxFloorY) {
        return attachBranch(start, random, 8 + ComponentMinecrawlerShaft.DROP, maxFloorY, (floor, facing, seed) ->
                new ComponentMinecrawlerShaft(floor, facing, mesa, seed));
    }

    private interface BranchFactory {
        StructureComponent create(BlockPos entranceFloor, EnumFacing facing, long seed);
    }

    private static boolean attachBranch(StructureStart start, Random random, int minFloorY, int maxFloorY,
                                        BranchFactory factory) {
        List<StructureComponent> candidates = new ArrayList<>(start.getComponents());
        Collections.shuffle(candidates, random);
        for (StructureComponent candidate : candidates) {
            if (!(candidate instanceof StructureMineshaftPieces.Corridor) || candidate.getCoordBaseMode() == null) continue;
            StructureBoundingBox box = candidate.getBoundingBox();
            if (box.minY - 1 < minFloorY || box.minY - 1 > maxFloorY) continue;

            // Both ends are already open in vanilla corridors. An occupied end fails the
            // intersection check, so this naturally extends an available dead end.
            EnumFacing forward = candidate.getCoordBaseMode();
            for (EnumFacing facing : new EnumFacing[]{forward, forward.getOpposite()}) {
                int x = (box.minX + box.maxX) / 2;
                int z = (box.minZ + box.maxZ) / 2;
                if (facing == EnumFacing.NORTH) z = box.minZ - 1;
                if (facing == EnumFacing.SOUTH) z = box.maxZ + 1;
                if (facing == EnumFacing.WEST) x = box.minX - 1;
                if (facing == EnumFacing.EAST) x = box.maxX + 1;
                StructureComponent branch = factory.create(new BlockPos(x, box.minY - 1, z), facing, random.nextLong());
                if (StructureComponent.findIntersecting(start.getComponents(), branch.getBoundingBox()) == null) {
                    start.getComponents().add(branch);
                    start.getBoundingBox().expandTo(branch.getBoundingBox());
                    return true;
                }
            }
        }
        return false;
    }
}
