package com.windanesz.tracesofthefallen.world.structure;

import com.windanesz.tracesofthefallen.block.BlockBonepile;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The warren's tunnels, in world coordinates. A tunnel is a list of cave slices, each a blob standing on a floor
 * cell. The path is computed from a few numbers and a seed, so every population region reproduces the same
 * tunnel and only carves the blocks inside its own region.
 */
final class WarrenCave {
    private static final float MIN_RADIUS = 1.6F;
    private static final float MAX_RADIUS = 2.3F;
    private static final float CHAMBER_RADIUS = 3.0F;
    private static final int MIN_HEIGHT = 3;
    private static final int MAX_HEIGHT = 5;
    private static final int MIN_WORLD_Y = 6;
    private static final double MAX_FLOOR_STEP = 0.75D;
    private static final double STRAIGHT_TURN_LIMIT = Math.toRadians(8);
    private static final double APPROACH_TURN_LIMIT = Math.toRadians(50);
    private static final int HOLD_COURSE_STEPS = 6;

    private WarrenCave() {
    }

    /** One slice of a cave: a blob of the given radius and height standing on a floor cell. */
    static final class Point {
        final BlockPos floor;
        final float radius;
        final int height;

        Point(BlockPos floor, float radius, int height) {
            this.floor = floor;
            this.radius = radius;
            this.height = height;
        }

        /** Whole blocks the slice reaches sideways from its floor cell. */
        int reach() {
            return MathHelper.ceil(this.radius);
        }
    }

    private static int heightFor(float radius) {
        return Math.max(MIN_HEIGHT, Math.min(MAX_HEIGHT, Math.round(radius * 1.6F + 0.5F)));
    }

    private static Point point(double x, double floorY, double z, float radius) {
        return new Point(new BlockPos(MathHelper.floor(x), (int) Math.round(floorY), MathHelper.floor(z)), radius, heightFor(radius));
    }

    /**
     * A tunnel joining two doorway mouths. It leaves and arrives in a straight line so it meets each doorway
     * squarely, wanders in between, and slopes evenly from one floor level to the other.
     */
    static List<Point> connector(BlockPos from, BlockPos to, Random random) {
        List<Point> points = new ArrayList<>();
        double targetX = to.getX() + 0.5D;
        double targetZ = to.getZ() + 0.5D;
        double x = from.getX() + 0.5D;
        double z = from.getZ() + 0.5D;
        double floorY = from.getY();
        double wobble = 0.0D;
        double travelled = 0.0D;
        float radius = 2.0F;
        int maxSteps = (int) (Math.hypot(targetX - x, targetZ - z) * 2.0D) + 16;

        for (int step = 0; step < maxSteps; step++) {
            double deltaX = targetX - x;
            double deltaZ = targetZ - z;
            double remaining = Math.hypot(deltaX, deltaZ);
            points.add(point(x, floorY, z, radius));
            if (remaining < 1.0D) {
                break;
            }

            double ease = Math.min(1.0D, Math.min(travelled, remaining) / 5.0D);
            wobble = Math.max(-1.0D, Math.min(1.0D, wobble + (random.nextDouble() - 0.5D) * 0.5D));
            double angle = StrictMath.atan2(deltaZ, deltaX) + wobble * 0.6D * ease;
            x += StrictMath.cos(angle);
            z += StrictMath.sin(angle);
            travelled += 1.0D;

            double desired = from.getY() + (to.getY() - from.getY()) * (travelled / (travelled + remaining));
            double wander = (random.nextDouble() - 0.5D) * 0.3D * ease;
            floorY += Math.max(-MAX_FLOOR_STEP, Math.min(MAX_FLOOR_STEP, desired + wander - floorY));
            float breathing = radius + (random.nextFloat() - 0.5F) * 0.3F;
            radius = ease < 1.0D ? 2.0F : Math.max(1.8F, Math.min(2.4F, breathing));
        }
        Point last = points.get(points.size() - 1);
        if (!last.floor.equals(to)) {
            points.add(new Point(to, 2.0F, 4));
        }
        return points;
    }

    /**
     * A way into the lair: a short, winding tunnel heading away from a doorway. It holds its course until it has
     * cleared the room, then meanders inside a cone around the way out, and swells now and then into a wider
     * pocket. The floor rises and sinks gently between the given world heights.
     */
    static List<Point> approach(BlockPos from, EnumFacing outward, int length, int lowY, int highY, Random random) {
        List<Point> points = new ArrayList<>();
        double outwardAngle = StrictMath.atan2(outward.getZOffset(), outward.getXOffset());
        double x = from.getX() + 0.5D;
        double z = from.getZ() + 0.5D;
        double floorY = Math.max(lowY, Math.min(highY, from.getY()));
        double slope = 0.0D;
        double offset = 0.0D;
        float radius = MIN_RADIUS + random.nextFloat() * (MAX_RADIUS - MIN_RADIUS);
        float targetRadius = radius;
        int untilResize = 4 + random.nextInt(6);

        for (int step = 0; step < length; step++) {
            points.add(point(x, floorY, z, radius));

            if (--untilResize <= 0) {
                untilResize = 4 + random.nextInt(7);
                targetRadius = random.nextInt(6) == 0
                        ? MAX_RADIUS + random.nextFloat() * (CHAMBER_RADIUS - MAX_RADIUS)
                        : MIN_RADIUS + random.nextFloat() * (MAX_RADIUS - MIN_RADIUS);
            }
            radius += Math.max(-0.2F, Math.min(0.2F, targetRadius - radius));

            double limit = step < HOLD_COURSE_STEPS ? STRAIGHT_TURN_LIMIT : APPROACH_TURN_LIMIT;
            offset += (random.nextDouble() - 0.5D) * 0.45D;
            offset = Math.max(-limit, Math.min(limit, offset));
            x += StrictMath.cos(outwardAngle + offset);
            z += StrictMath.sin(outwardAngle + offset);

            slope = Math.max(-0.4D, Math.min(0.4D, slope + (random.nextDouble() - 0.5D) * 0.3D));
            floorY += slope;
            if (floorY < lowY || floorY > highY) {
                floorY = Math.max(lowY, Math.min(highY, floorY));
                slope = -slope;
            }
        }
        return points;
    }

    /** The lowest floor level a tunnel may sink to. */
    static int minFloorY(int startY, int maxDrift) {
        return Math.max(MIN_WORLD_Y, startY - maxDrift);
    }

    static StructureBoundingBox bounds(List<Point> path) {
        StructureBoundingBox box = null;
        for (Point point : path) {
            int reach = point.reach();
            StructureBoundingBox slice = new StructureBoundingBox(
                    point.floor.getX() - reach, point.floor.getY() - 1, point.floor.getZ() - reach,
                    point.floor.getX() + reach, point.floor.getY() + point.height - 1, point.floor.getZ() + reach);
            if (box == null) {
                box = slice;
            } else {
                box.expandTo(slice);
            }
        }
        return box;
    }

    private static boolean kept(List<StructureBoundingBox> keepOut, BlockPos pos) {
        for (StructureBoundingBox box : keepOut) {
            if (box.isVecInside(pos)) {
                return true;
            }
        }
        return false;
    }

    /** Digs the path out of the rock and lays a floor under it. Never touches the keep-out boxes (the rooms). */
    static void carve(World world, StructureBoundingBox region, List<Point> path, List<StructureBoundingBox> keepOut) {
        for (Point point : path) {
            int reach = point.reach();
            // Half an ellipsoid-ish blob standing on the floor cell: rounder than a box, never narrower at
            // the centre column than a walkable passage.
            double centreY = (point.height - 1) / 2.0D;
            double verticalRadius = point.height / 2.0D;
            for (int dx = -reach; dx <= reach; dx++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    double across = (dx * dx + dz * dz) / (double) (point.radius * point.radius);
                    for (int dy = 0; dy < point.height; dy++) {
                        double vertical = (dy - centreY) / verticalRadius;
                        if (across + vertical * vertical > 1.0D) {
                            continue;
                        }
                        BlockPos pos = point.floor.add(dx, dy, dz);
                        if (!region.isVecInside(pos) || kept(keepOut, pos)) {
                            continue;
                        }
                        IBlockState state = world.getBlockState(pos);
                        if (state.getBlock().hasTileEntity(state) || state.getBlockHardness(world, pos) < 0
                                || state.getMaterial().isLiquid()) {
                            continue;
                        }
                        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);

                        if (dy == 0) {
                            layFloor(world, region, keepOut, pos.down());
                        }
                    }
                }
            }
        }
    }

    private static void layFloor(World world, StructureBoundingBox region, List<StructureBoundingBox> keepOut, BlockPos below) {
        if (!region.isVecInside(below) || kept(keepOut, below)) {
            return;
        }
        IBlockState state = world.getBlockState(below);
        if ((!state.getMaterial().isSolid() || state.getMaterial().isLiquid())
                && !state.getBlock().hasTileEntity(state) && state.getBlockHardness(world, below) >= 0) {
            world.setBlockState(below, Blocks.STONE.getDefaultState(), 2);
        }
    }

    /**
     * Scatters bones along the path where goblins have been through. All the rolls
     * happen before any world check so every region sees the same sequence.
     */
    static void decorate(World world, StructureBoundingBox region, List<Point> path, Random random, int first, int every) {
        for (int index = first; index < path.size() - 2; index += every + random.nextInt(3)) {
            int side = random.nextBoolean() ? 1 : -1;
            int kind = random.nextInt(4);
            int layers = 1 + random.nextInt(3);

            Point here = path.get(index);
            Point next = path.get(index + 1);
            int heading = Math.abs(next.floor.getX() - here.floor.getX()) >= Math.abs(next.floor.getZ() - here.floor.getZ()) ? 0 : 1;
            int offset = Math.max(1, (int) here.radius - 1);
            BlockPos cell = heading == 0 ? here.floor.add(0, 0, side * offset) : here.floor.add(side * offset, 0, 0);
            if (!region.isVecInside(cell) || !world.isAirBlock(cell) || !world.getBlockState(cell.down()).getMaterial().isSolid()) {
                continue;
            }
            if (kind == 0) {
                world.setBlockState(cell, ModBlocks.bone_mound.getDefaultState(), 2);
            } else {
                world.setBlockState(cell, ModBlocks.bonepile.getDefaultState().withProperty(BlockBonepile.LAYERS, layers), 2);
            }
        }
    }
}
