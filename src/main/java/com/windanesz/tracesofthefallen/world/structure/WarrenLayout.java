package com.windanesz.tracesofthefallen.world.structure;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.template.Template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * What a warren room needs to know about its template, read from the template itself so a room can be reshaped
 * in the NBT file without touching code. All coordinates are template-local; the same layout serves every
 * rotation and mirror of the room.
 */
final class WarrenLayout {
    // Natural ground the room stands on; props like bonepiles, beds and slabs are not valid floors.
    private static final Set<String> FLOOR_BLOCKS = new HashSet<>(Arrays.asList(
            "minecraft:stone", "minecraft:dirt", "minecraft:grass"));
    private static final String FIRE_RUNE_BLOCK = "totf:bonepile_fire_rune";

    /** A gap in one of the template's four side walls, where a tunnel should leave it. */
    static final class Exit {
        final EnumFacing outward;
        /** Template-local floor cell just outside the wall, centered on the gap. */
        final BlockPos mouth;

        Exit(EnumFacing outward, BlockPos mouth) {
            this.outward = outward;
            this.mouth = mouth;
        }
    }

    final int sizeX;
    final int sizeZ;
    /** Open cells with natural ground below and two blocks of headroom, in a fixed scan order. */
    final List<BlockPos> standCells = new ArrayList<>();
    final Set<BlockPos> standSet = new HashSet<>();
    final List<Exit> exits = new ArrayList<>();
    /** The fire rune block a hall's shaman is stationed beside; null if the template has none. */
    BlockPos fireRune;

    private WarrenLayout(int sizeX, int sizeZ) {
        this.sizeX = sizeX;
        this.sizeZ = sizeZ;
    }

    static WarrenLayout read(Template template) {
        NBTTagCompound tag = template.writeToNBT(new NBTTagCompound());
        NBTTagList palette = tag.getTagList("palette", 10);
        boolean[] air = new boolean[palette.tagCount()];
        boolean[] floor = new boolean[palette.tagCount()];
        boolean[] fireRune = new boolean[palette.tagCount()];
        for (int index = 0; index < palette.tagCount(); index++) {
            String name = palette.getCompoundTagAt(index).getString("Name");
            air[index] = name.equals("minecraft:air");
            floor[index] = FLOOR_BLOCKS.contains(name);
            fireRune[index] = name.equals(FIRE_RUNE_BLOCK);
        }

        BlockPos size = template.getSize();
        int sizeX = size.getX();
        int sizeY = size.getY();
        int sizeZ = size.getZ();
        int[][][] states = new int[sizeX][sizeY][sizeZ];
        for (int[][] plane : states) {
            for (int[] column : plane) {
                Arrays.fill(column, -1);
            }
        }
        NBTTagList blocks = tag.getTagList("blocks", 10);
        for (int index = 0; index < blocks.tagCount(); index++) {
            NBTTagCompound block = blocks.getCompoundTagAt(index);
            NBTTagList pos = block.getTagList("pos", 3);
            states[pos.getIntAt(0)][pos.getIntAt(1)][pos.getIntAt(2)] = block.getInteger("state");
        }

        WarrenLayout layout = new WarrenLayout(sizeX, sizeZ);
        for (int index = 0; index < blocks.tagCount(); index++) {
            NBTTagCompound block = blocks.getCompoundTagAt(index);
            if (fireRune[block.getInteger("state")]) {
                NBTTagList pos = block.getTagList("pos", 3);
                layout.fireRune = new BlockPos(pos.getIntAt(0), pos.getIntAt(1), pos.getIntAt(2));
                break;
            }
        }
        for (int x = 0; x < sizeX; x++) {
            for (int y = 1; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    if (isAir(states, air, x, y, z) && isAir(states, air, x, y + 1, z) && isAir(states, air, x, y + 2, z)
                            && isFloor(states, floor, x, y - 1, z)) {
                        BlockPos cell = new BlockPos(x, y, z);
                        layout.standCells.add(cell);
                        layout.standSet.add(cell);
                    }
                }
            }
        }

        for (EnumFacing side : EnumFacing.Plane.HORIZONTAL) {
            int minY = Integer.MAX_VALUE;
            int minAcross = Integer.MAX_VALUE;
            int maxAcross = Integer.MIN_VALUE;
            // North/south walls run along X; east/west walls run along Z.
            boolean northSouth = side.getAxis() == EnumFacing.Axis.Z;
            int faceCoordinate = side.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE
                    ? (northSouth ? sizeZ : sizeX) - 1 : 0;
            int across = northSouth ? sizeX : sizeZ;
            for (int a = 0; a < across; a++) {
                for (int y = 1; y < sizeY; y++) {
                    int x = northSouth ? a : faceCoordinate;
                    int z = northSouth ? faceCoordinate : a;
                    if (isAir(states, air, x, y, z)) {
                        minY = Math.min(minY, y);
                        minAcross = Math.min(minAcross, a);
                        maxAcross = Math.max(maxAcross, a);
                    }
                }
            }
            if (minY == Integer.MAX_VALUE) {
                continue;
            }
            int centre = (minAcross + maxAcross) / 2;
            int mouthX = northSouth ? centre : faceCoordinate + side.getXOffset();
            int mouthZ = northSouth ? faceCoordinate + side.getZOffset() : centre;
            layout.exits.add(new Exit(side, new BlockPos(mouthX, minY, mouthZ)));
        }
        return layout;
    }

    private static boolean isAir(int[][][] states, boolean[] air, int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= states.length || y >= states[0].length || z >= states[0][0].length) {
            return false;
        }
        int state = states[x][y][z];
        return state >= 0 && air[state];
    }

    private static boolean isFloor(int[][][] states, boolean[] floor, int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= states.length || y >= states[0].length || z >= states[0][0].length) {
            return false;
        }
        int state = states[x][y][z];
        return state >= 0 && floor[state];
    }

    /** The standing cell closest to the fire the rune sustains (the cell above it), or null without a rune. */
    BlockPos cellBesideFireRune() {
        if (this.fireRune == null) {
            return null;
        }
        BlockPos fire = this.fireRune.up();
        BlockPos best = null;
        for (BlockPos cell : this.standCells) {
            if (best == null || cell.distanceSq(fire) < best.distanceSq(fire)) {
                best = cell;
            }
        }
        return best;
    }

    /** A goblin nest is 2 blocks wide, so it needs a flat 2x2 patch of standing cells (cell is its min corner). */
    boolean hasNestPatch(BlockPos cell) {
        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                if (!standSet.contains(cell.add(dx, 0, dz))) {
                    return false;
                }
            }
        }
        return true;
    }
}
