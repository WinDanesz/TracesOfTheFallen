package com.windanesz.tracesofthefallen.world.structure;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.entity.EntityGoblinNest;
import com.windanesz.tracesofthefallen.entity.EntityJawTrap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import java.util.List;

/**
 * Spawning shared by the warren's pieces. Every call is handed the population region being placed and only
 * acts on positions inside it, so a piece that reproduces the same choices in each region never doubles or
 * loses a resident at a region seam.
 */
final class WarrenSpawner {

    private WarrenSpawner() {
    }

    static boolean isFarFrom(BlockPos cell, List<BlockPos> others, int distance) {
        for (BlockPos other : others) {
            if (cell.distanceSq(other) < distance * distance) {
                return false;
            }
        }
        return true;
    }

    static int randomBetween(java.util.Random random, int min, int max) {
        int low = Math.max(0, min);
        int high = Math.max(low, max);
        return low + (high > low ? random.nextInt(high - low + 1) : 0);
    }

    static void spawnMob(World world, StructureBoundingBox region, BlockPos pos, String type, float yaw) {
        if (!region.isVecInside(pos)) {
            return;
        }
        Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(TracesOfTheFallen.MODID, type), world);
        if (!(entity instanceof EntityLiving)) {
            return;
        }
        // Nudge boundary spawns inward so the whole body is in terrain this region has already placed.
        double spawnX = Math.max(region.minX + 0.6D, Math.min(region.maxX + 0.4D, pos.getX() + 0.5D));
        double spawnZ = Math.max(region.minZ + 0.6D, Math.min(region.maxZ + 0.4D, pos.getZ() + 0.5D));
        entity.setLocationAndAngles(spawnX, pos.getY(), spawnZ, yaw, 0.0F);
        if (world.getCollisionBoxes(entity, entity.getEntityBoundingBox()).isEmpty()
                && !world.containsAnyLiquid(entity.getEntityBoundingBox())) {
            ((EntityLiving) entity).enablePersistence();
            world.spawnEntity(entity);
        }
    }

    static void spawnJawTrap(World world, StructureBoundingBox region, BlockPos pos) {
        if (!region.isVecInside(pos)) {
            return;
        }
        double x = Math.max(region.minX + 0.6D, Math.min(region.maxX + 0.4D, pos.getX() + 0.5D));
        double z = Math.max(region.minZ + 0.6D, Math.min(region.maxZ + 0.4D, pos.getZ() + 0.5D));
        world.spawnEntity(new EntityJawTrap(world, x, pos.getY(), z));
    }

    /** The nest is 2 blocks wide, so it sits on the shared corner of the 2x2 patch whose cells are given. */
    static void spawnNest(World world, StructureBoundingBox region, BlockPos near, BlockPos far, EnumFacing facing) {
        // Midpoint of two diagonal cell centers, so it holds for every rotation and mirror.
        double centerX = (near.getX() + far.getX()) / 2.0D + 0.5D;
        double centerZ = (near.getZ() + far.getZ()) / 2.0D + 0.5D;
        // Spawn from the region that owns the nest's center, so it never lands in a chunk that isn't loaded.
        if (!region.isVecInside(new BlockPos(MathHelper.floor(centerX), near.getY(), MathHelper.floor(centerZ)))) {
            return;
        }
        EntityGoblinNest nest = new EntityGoblinNest(world, centerX, near.getY(), centerZ);
        nest.setFacing(facing);
        nest.setPersistentBroods(true);
        world.spawnEntity(nest);
    }
}
