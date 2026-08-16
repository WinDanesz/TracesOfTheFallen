package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Slices a list of master AABBs (in NORTH-canonical 2-block model space) into
 * per-cell [0,1] AABBs at construction time, then serves instant lookups with
 * facing-aware rotation at runtime.
 *
 * Master AABBs must be in NORTH-facing model space where the main block occupies
 * cell (0,0,0) and the grid extends up to (dxMax,dyMax,dzMax).
 */
public class MultiblockAABBHelper {

    private final Map<BlockPos, AxisAlignedBB> precomputedNorth;

    public MultiblockAABBHelper(List<AxisAlignedBB> masterAABBs,
            int dxMin, int dxMax, int dyMin, int dyMax, int dzMin, int dzMax) {
        Map<BlockPos, AxisAlignedBB> map = new HashMap<>();
        for (int dx = dxMin; dx <= dxMax; dx++) {
            for (int dy = dyMin; dy <= dyMax; dy++) {
                for (int dz = dzMin; dz <= dzMax; dz++) {
                    map.put(new BlockPos(dx, dy, dz), computeCell(masterAABBs, dx, dy, dz));
                }
            }
        }
        this.precomputedNorth = map;
    }

    /**
     * Returns the per-cell [0,1] AABB for a proxy at the given world positions.
     * Returns {@link Block#NULL_AABB} for cells with no master geometry.
     */
    public AxisAlignedBB get(EnumFacing facing, BlockPos mainPos, BlockPos proxyPos) {
        int dx = proxyPos.getX() - mainPos.getX();
        int dy = proxyPos.getY() - mainPos.getY();
        int dz = proxyPos.getZ() - mainPos.getZ();

        // Normalize world offset to NORTH-canonical model coordinates.
        // Derivation: EAST=CW, SOUTH=HT, WEST=CCW Y-rotation of the NORTH grid.
        int ndx, ndz;
        switch (facing) {
            case EAST:  ndx =  dz; ndz = -dx; break;
            case SOUTH: ndx = -dx; ndz = -dz; break;
            case WEST:  ndx = -dz; ndz =  dx; break;
            default:    ndx =  dx; ndz =  dz; break; // NORTH
        }

        AxisAlignedBB northAABB = precomputedNorth.get(new BlockPos(ndx, dy, ndz));
        if (northAABB == Block.NULL_AABB) return Block.NULL_AABB;

        // Rotate the canonical AABB into world-aligned local coordinates.
        switch (facing) {
            case EAST:  return rotateCW(northAABB);
            case SOUTH: return rotateHT(northAABB);
            case WEST:  return rotateCCW(northAABB);
            default:    return northAABB;
        }
    }

    /**
     * Clips a single AABB (in main-block-relative multi-block space) to a cell's
     * [0,1] local space. Returns {@link Block#NULL_AABB} when the cell is empty.
     */
    public static AxisAlignedBB clipToCell(AxisAlignedBB mainAABB, int dx, int dy, int dz) {
        double minX = Math.max(mainAABB.minX - dx, 0);
        double minY = Math.max(mainAABB.minY - dy, 0);
        double minZ = Math.max(mainAABB.minZ - dz, 0);
        double maxX = Math.min(mainAABB.maxX - dx, 1);
        double maxY = Math.min(mainAABB.maxY - dy, 1);
        double maxZ = Math.min(mainAABB.maxZ - dz, 1);
        if (minX >= maxX || minY >= maxY || minZ >= maxZ) return Block.NULL_AABB;
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    // -----------------------------------------------------------------------
    // Pre-computation helpers
    // -----------------------------------------------------------------------

    private static AxisAlignedBB computeCell(List<AxisAlignedBB> masterAABBs, int dx, int dy, int dz) {
        double minX = 1, minY = 1, minZ = 1, maxX = 0, maxY = 0, maxZ = 0;
        boolean any = false;
        for (AxisAlignedBB master : masterAABBs) {
            double cMinX = Math.max(master.minX - dx, 0);
            double cMinY = Math.max(master.minY - dy, 0);
            double cMinZ = Math.max(master.minZ - dz, 0);
            double cMaxX = Math.min(master.maxX - dx, 1);
            double cMaxY = Math.min(master.maxY - dy, 1);
            double cMaxZ = Math.min(master.maxZ - dz, 1);
            if (cMinX >= cMaxX || cMinY >= cMaxY || cMinZ >= cMaxZ) continue;
            if (!any) {
                minX = cMinX; minY = cMinY; minZ = cMinZ;
                maxX = cMaxX; maxY = cMaxY; maxZ = cMaxZ;
                any = true;
            } else {
                minX = Math.min(minX, cMinX); minY = Math.min(minY, cMinY); minZ = Math.min(minZ, cMinZ);
                maxX = Math.max(maxX, cMaxX); maxY = Math.max(maxY, cMaxY); maxZ = Math.max(maxZ, cMaxZ);
            }
        }
        return any ? new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ) : Block.NULL_AABB;
    }

    // -----------------------------------------------------------------------
    // AABB rotation helpers — all operate on [0,1] block-local coordinates.
    // Matching the Y-rotation directions used by BlockIncenseBurner.
    // -----------------------------------------------------------------------

    static AxisAlignedBB rotateCW(AxisAlignedBB b) {
        return new AxisAlignedBB(1.0 - b.maxZ, b.minY, b.minX, 1.0 - b.minZ, b.maxY, b.maxX);
    }

    static AxisAlignedBB rotateHT(AxisAlignedBB b) {
        return new AxisAlignedBB(1.0 - b.maxX, b.minY, 1.0 - b.maxZ, 1.0 - b.minX, b.maxY, 1.0 - b.minZ);
    }

    static AxisAlignedBB rotateCCW(AxisAlignedBB b) {
        return new AxisAlignedBB(b.minZ, b.minY, 1.0 - b.maxX, b.maxZ, b.maxY, 1.0 - b.minX);
    }
}
