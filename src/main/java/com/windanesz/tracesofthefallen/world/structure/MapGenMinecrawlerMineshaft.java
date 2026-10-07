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
        int frequency = Settings.worldgenSettings.minecrawlerMineshaftFrequency;
        if (frequency <= 0 || !containsDimension(world.provider.getDimension())) {
            return start;
        }
        // Do not advance the vanilla generator's random stream for our extra content.
        Random random = new Random(world.getSeed() ^ (chunkX * 341873128712L) ^ (chunkZ * 132897987541L) ^ 913174639L);
        if (random.nextInt(frequency) == 0) {
            boolean mesa = world.getBiome(new BlockPos((chunkX << 4) + 8, 64, (chunkZ << 4) + 8)) instanceof BiomeMesa;
            attachNest(start, random, mesa, world.getSeaLevel() - 8);
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
        List<StructureComponent> candidates = new ArrayList<>(start.getComponents());
        Collections.shuffle(candidates, random);
        for (StructureComponent candidate : candidates) {
            if (!(candidate instanceof StructureMineshaftPieces.Corridor) || candidate.getCoordBaseMode() == null) continue;
            StructureBoundingBox box = candidate.getBoundingBox();
            if (box.minY - 1 < 8 || box.minY - 1 > maxFloorY) continue;

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
                ComponentMinecrawlerNest nest = new ComponentMinecrawlerNest(
                        new BlockPos(x, box.minY - 1, z), facing, mesa, random.nextLong());
                if (StructureComponent.findIntersecting(start.getComponents(), nest.getBoundingBox()) == null) {
                    start.getComponents().add(nest);
                    start.getBoundingBox().expandTo(nest.getBoundingBox());
                    return true;
                }
            }
        }
        return false;
    }
}
