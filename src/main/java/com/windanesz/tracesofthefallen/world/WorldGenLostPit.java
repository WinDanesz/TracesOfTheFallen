package com.windanesz.tracesofthefallen.world;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.block.BlockBonepile;
import com.windanesz.tracesofthefallen.block.BlockSkeletonCorpse;
import com.windanesz.tracesofthefallen.entity.living.EntityLost;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.*;

/** Plans and validates the entire excavation before writing any blocks. */
public class WorldGenLostPit implements IWorldGenerator {
    private static final String[] STRUCTURES = {"Mineshaft", "Stronghold", "Village", "Temple", "Monument", "Mansion"};

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world,
                         IChunkGenerator generator, IChunkProvider provider) {
        if (world.provider.getDimension() != 0 || !Settings.worldgenSettings.lostPitsEnabled
                || Settings.worldgenSettings.lostPitFrequency <= 0) return;
        // Independent stream: do not reseed the shared population RNG.
        Random rng = new Random(world.getSeed() ^ (chunkX * 341873128712L)
                ^ (chunkZ * 132897987541L) ^ 0x4C4F5354504954L);
        if (rng.nextInt(Settings.worldgenSettings.lostPitFrequency) != 0) return;
        boolean surface = rng.nextDouble() < Settings.worldgenSettings.lostPitSurfaceChance;
        boolean ledges = !surface && rng.nextDouble() < Settings.worldgenSettings.lostPitLedgeChance;
        int radius = 4 + rng.nextInt(3);
        int depth = 4 + rng.nextInt(4);
        BlockPos column = new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8);
        if (!world.isAreaLoaded(column.add(-radius - 4, 0, -radius - 4),
                column.add(radius + 4, 255, radius + 4))) return;
        int roof = world.getHeight(column).getY();
        int min = Math.max(4, Settings.worldgenSettings.lostPitMinY);
        int max = Math.min(Math.min(240, Settings.worldgenSettings.lostPitMaxY), roof - depth - 5);
        if (max < min) return;
        List<Integer> depths = new ArrayList<>();
        for (int y = min; y <= max; y++) depths.add(y);
        Collections.shuffle(depths, rng);
        // A surface attempt gets exactly one candidate, and never falls back to an ordinary pit.
        for (int y : depths) {
            BlockPos center = new BlockPos(column.getX(), y, column.getZ());
            EnumFacing entrance = EnumFacing.Plane.HORIZONTAL.random(rng);
            if (!surface) {
                entrance = null;
                for (EnumFacing side : EnumFacing.Plane.HORIZONTAL) {
                    BlockPos lip = center.offset(side, radius + 2).up(depth);
                    if (world.getBlockState(lip).isTopSolid() && world.isAirBlock(lip.up())
                            && world.isAirBlock(lip.up(2)) && !world.canSeeSky(lip.up())) {
                        entrance = side;
                        break;
                    }
                }
                if (entrance == null) continue;
            }
            Plan plan = plan(center, radius, depth, entrance, ledges, surface ? roof : -1, rng);
            if (validate(world, generator, plan)) {
                build(world, rng, plan);
                return;
            }
            if (surface) return;
        }
    }

    static final class Plan {
        final Set<BlockPos> air = new LinkedHashSet<>();
        final Set<BlockPos> floors = new LinkedHashSet<>();
        final Set<BlockPos> steps = new LinkedHashSet<>();
        final Set<BlockPos> water = new LinkedHashSet<>();
        final Set<BlockPos> poolShell = new LinkedHashSet<>();
        final Set<BlockPos> shaft = new LinkedHashSet<>();
        final Set<BlockPos> clues = new LinkedHashSet<>();
        final BlockPos center;
        final boolean surface;
        Plan(BlockPos center, boolean surface) { this.center = center; this.surface = surface; }
    }

    static Plan plan(BlockPos center, int radius, int depth, EnumFacing side,
                     boolean ledges, int surfaceY, Random rng) {
        Plan p = new Plan(center, surfaceY >= 0);
        double phase = rng.nextDouble() * Math.PI * 2;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double angle = Math.atan2(z, x);
                double edge = radius + 0.3 * Math.sin(angle * 3 + phase);
                if (x * x + z * z > edge * edge) continue;
                BlockPos floor = center.add(x, 0, z);
                p.floors.add(floor);
                int height = depth + 3 - (int) (2 * Math.sqrt(x * x + z * z) / radius);
                for (int dy = 1; dy <= height; dy++) p.air.add(floor.up(dy));
            }
        }
        if (!p.surface) {
            // Short opening through the rim; the far end was checked against an existing cave.
            for (int d = radius; d <= radius + 2; d++) {
                BlockPos lip = center.offset(side, d).up(depth);
                p.steps.add(lip);
                p.air.add(lip.up());
                p.air.add(lip.up(2));
                p.air.add(lip.up(3));
                p.clues.add(lip.offset(side.rotateY()));
            }
            if (ledges) {
                // One-block rises join the chamber floor to the entrance without parkour jumps.
                for (int rise = 1; rise <= depth; rise++) {
                    BlockPos step = center.offset(side, radius - depth + rise).up(rise);
                    for (int dy = 0; dy < rise; dy++) p.steps.add(step.down(dy));
                    for (int dy = 1; dy <= 3; dy++) p.air.add(step.up(dy));
                }
            }
        } else {
            // 2x2 shaft, with a wider 4x4, two-block-deep catch pool. No ledges in the fall path.
            for (int x = -1; x <= 2; x++) for (int z = -1; z <= 2; z++) {
                for (int y = -1; y <= 0; y++) p.water.add(center.add(x, y, z));
            }
            for (BlockPos water : p.water) {
                for (EnumFacing facing : EnumFacing.values()) {
                    BlockPos neighbor = water.offset(facing);
                    if (neighbor.getY() <= center.getY() && !p.water.contains(neighbor)) p.poolShell.add(neighbor);
                }
            }
            for (int x = 0; x <= 1; x++) for (int z = 0; z <= 1; z++) {
                for (int y = center.getY() + 1; y <= surfaceY + 1; y++) {
                    BlockPos pos = new BlockPos(center.getX() + x, y, center.getZ() + z);
                    p.air.add(pos);
                    p.shaft.add(pos);
                }
            }
            p.clues.add(new BlockPos(center.getX() - 1, surfaceY - 1, center.getZ()));
            p.clues.add(new BlockPos(center.getX() + 2, surfaceY - 1, center.getZ() + 1));
        }
        p.air.removeAll(p.steps);
        p.floors.removeAll(p.water);
        return p;
    }

    static boolean natural(IBlockState state) {
        Block b = state.getBlock();
        return b == Blocks.AIR || b == Blocks.STONE || b == Blocks.DIRT || b == Blocks.GRASS
                || b == Blocks.GRAVEL || b == Blocks.SAND || b == Blocks.SANDSTONE
                || b == Blocks.RED_SANDSTONE || b == Blocks.HARDENED_CLAY || b == Blocks.STAINED_HARDENED_CLAY
                || b == Blocks.COAL_ORE || b == Blocks.IRON_ORE || b == Blocks.GOLD_ORE
                || b == Blocks.REDSTONE_ORE || b == Blocks.LIT_REDSTONE_ORE || b == Blocks.LAPIS_ORE
                || b == Blocks.DIAMOND_ORE || b == Blocks.EMERALD_ORE;
    }

    static boolean validate(World world, IChunkGenerator generator, Plan p) {
        Set<BlockPos> changed = new HashSet<>(p.air);
        changed.addAll(p.floors);
        changed.addAll(p.steps);
        changed.addAll(p.water);
        changed.addAll(p.poolShell);
        Set<BlockPos> checked = new HashSet<>(changed);
        for (BlockPos pos : changed) for (EnumFacing side : EnumFacing.values()) checked.add(pos.offset(side));
        // Check loading before any reads, avoiding cascading world generation.
        for (BlockPos pos : checked) if (pos.getY() < 1 || pos.getY() > 254 || !world.isBlockLoaded(pos)) return false;
        for (BlockPos pos : checked) {
            IBlockState state = world.getBlockState(pos);
            if (!natural(state) || world.getTileEntity(pos) != null) return false;
            if (generator != null) for (String structure : STRUCTURES) {
                if (generator.isInsideStructure(world, structure, pos)) return false;
            }
        }
        for (BlockPos floor : p.floors) if (!world.getBlockState(floor).isTopSolid()) return false;
        for (BlockPos shell : p.poolShell) if (!world.getBlockState(shell).isFullCube()) return false;
        for (BlockPos air : p.air) {
            if (p.shaft.contains(air) && !p.shaft.contains(air.up()) && !world.canSeeSky(air.up())) return false;
            if (!p.shaft.contains(air) && air.getY() >= world.getHeight(air).getY() - 2) return false;
            // Loose ceilings must not collapse into the excavation or obstruct the fall shaft.
            BlockPos above = air.up();
            if (!p.air.contains(above)) {
                Block block = world.getBlockState(above).getBlock();
                if (block == Blocks.SAND || block == Blocks.GRAVEL) return false;
            }
        }
        return true;
    }

    private void build(World world, Random rng, Plan p) {
        for (BlockPos pos : p.air) world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
        for (BlockPos pos : p.steps) world.setBlockState(pos, Blocks.STONE.getDefaultState(), 2);
        for (BlockPos pos : p.poolShell) world.setBlockState(pos, Blocks.STONE.getDefaultState(), 2);
        for (BlockPos pos : p.water) world.setBlockState(pos, Blocks.WATER.getDefaultState(), 2);
        List<BlockPos> available = new ArrayList<>();
        for (BlockPos floor : p.floors) if (world.isAirBlock(floor.up()) && world.isAirBlock(floor.up(2))) available.add(floor);
        Collections.shuffle(available, rng);
        // Reserve dry stone patches for inhabitants and props before adding layered flooring.
        int residents = rng.nextDouble() < Settings.worldgenSettings.lostPitPairChance ? 2 : 1;
        Set<BlockPos> reserved = spawnResidents(world, rng, available, residents);
        List<BlockPos> props = new ArrayList<>(available);
        props.removeAll(reserved);
        int corpses = 1 + rng.nextInt(3);
        for (int i = 0; i < corpses && !props.isEmpty(); i++) {
            BlockPos floor = props.remove(0);
            new WorldGenMinecrawlerLair().placeSkeletonCorpse(world, rng, Collections.singletonList(floor),
                    (BlockSkeletonCorpse) ModBlocks.skeleton_corpse);
            reserved.add(floor);
        }
        int skulls = rng.nextInt(4);
        for (int i = 0; i < skulls && !props.isEmpty(); i++) {
            BlockPos floor = props.remove(0);
            placeSkull(world, rng, floor.up());
            reserved.add(floor);
        }
        for (BlockPos floor : available) {
            if (reserved.contains(floor)) continue;
            double roll = rng.nextDouble();
            if (roll < 0.78) world.setBlockState(floor.up(), ModBlocks.bonepile.getDefaultState()
                    .withProperty(BlockBonepile.LAYERS, 1 + rng.nextInt(5)), 2);
            else if (roll < 0.88) world.setBlockState(floor.up(), ModBlocks.bone_mound.getDefaultState(), 2);
        }
        for (BlockPos clue : p.clues) {
            if (world.getBlockState(clue).isTopSolid() && world.isAirBlock(clue.up())) {
                world.setBlockState(clue.up(), ModBlocks.bonepile.getDefaultState().withProperty(BlockBonepile.LAYERS, 1 + rng.nextInt(2)), 2);
            }
        }
    }

    static Set<BlockPos> spawnResidents(World world, Random rng, List<BlockPos> floors, int count) {
        Set<BlockPos> occupied = new HashSet<>();
        for (BlockPos floor : floors) {
            if (count <= 0) break;
            if (!world.isBlockLoaded(floor) || !world.getBlockState(floor).isTopSolid()) continue;
            EntityLost lost = new EntityLost(world);
            lost.setLocationAndAngles(floor.getX() + 0.5, floor.getY() + 1, floor.getZ() + 0.5, rng.nextFloat() * 360, 0);
            if (!world.getCollisionBoxes(lost, lost.getEntityBoundingBox()).isEmpty()
                    || world.containsAnyLiquid(lost.getEntityBoundingBox())
                    || !world.checkNoEntityCollision(lost.getEntityBoundingBox())) continue;
            lost.enablePersistence();
            if (world.spawnEntity(lost)) {
                occupied.add(floor);
                count--;
            }
        }
        return occupied;
    }

    private static void placeSkull(World world, Random rng, BlockPos pos) {
        world.setBlockState(pos, Blocks.SKULL.getDefaultState().withProperty(BlockSkull.FACING, EnumFacing.UP), 2);
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntitySkull) {
            ((TileEntitySkull) tile).setType(0);
            ((TileEntitySkull) tile).setSkullRotation(rng.nextInt(16));
        }
    }
}
