package com.windanesz.tracesofthefallen.world.structure;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.block.BlockCrawlerWaxWet;
import com.windanesz.tracesofthefallen.block.BlockSkeletonCorpse;
import com.windanesz.tracesofthefallen.block.BlockTOFT;
import com.windanesz.tracesofthefallen.tile.TileEntitySkeletonCorpse;
import com.windanesz.tracesofthefallen.entity.living.EntityMinecrawler;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockRail;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.loot.LootTableList;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * A short mine passage that ends at the lip of a wide vertical shaft. A wax-slick ledge spirals down
 * the wall to a brood chamber at the bottom. Placed one population region at a time, like the nest.
 *
 * Local coordinates: z = 0 is the entrance plane and grows away from the corridor, x is centered on
 * {@link #CENTER_X}, and y = 0 is the floor block of the brood chamber. The corridor floor block sits at
 * y = {@link #DROP}.
 */
public class ComponentMinecrawlerShaft extends StructureComponent {
    /** Blocks between the corridor floor and the brood chamber floor. */
    public static final int DROP = 16;
    private static final int WIDTH = 13;
    private static final int LENGTH = 17;
    private static final int HEIGHT = DROP + 5;
    private static final int CENTER_X = 6;
    private static final int CENTER_Z = 9;
    private static final double SHAFT_RADIUS_SQ = 13.5D;
    private static final double LEDGE_INNER_RADIUS_SQ = 5.8D;
    private static final int LEDGE_SECTORS = 8;
    private static final float SKELETON_CHANCE = 0.60F;
    private static final float SECOND_SKELETON_CHANCE = 0.15F;
    private static final int[][] COCOON_SITES = {{3, 5}, {9, 5}, {3, 13}, {9, 13}, {6, 3}};
    private static final int[][] CRAWLER_SITES = {{2, 9}, {10, 9}, {4, 5}};

    private long decorationSeed;
    private boolean mesa;
    private final Set<Long> generatedSections = new HashSet<>();

    // Required by MapGenStructureIO when loading existing mineshafts.
    public ComponentMinecrawlerShaft() {
    }

    public ComponentMinecrawlerShaft(BlockPos entranceFloor, EnumFacing facing, boolean mesa, long seed) {
        super(0);
        setCoordBaseMode(facing);
        this.mesa = mesa;
        this.decorationSeed = seed;
        int x = entranceFloor.getX();
        int y = entranceFloor.getY() - DROP;
        int z = entranceFloor.getZ();
        int top = y + HEIGHT - 1;
        int half = CENTER_X;
        switch (facing) {
            case NORTH: boundingBox = new StructureBoundingBox(x - half, y, z - LENGTH + 1, x + half, top, z); break;
            case SOUTH: boundingBox = new StructureBoundingBox(x - half, y, z, x + half, top, z + LENGTH - 1); break;
            case WEST: boundingBox = new StructureBoundingBox(x - LENGTH + 1, y, z - half, x, top, z + half); break;
            case EAST: boundingBox = new StructureBoundingBox(x, y, z - half, x + LENGTH - 1, top, z + half); break;
            default: throw new IllegalArgumentException("Minecrawler branches must face horizontally");
        }
    }

    @Override
    protected void writeStructureToNBT(NBTTagCompound tag) {
        tag.setLong("ShaftSeed", decorationSeed);
        tag.setBoolean("Mesa", mesa);
        NBTTagList sections = new NBTTagList();
        for (long key : generatedSections) {
            NBTTagCompound section = new NBTTagCompound();
            section.setLong("Pos", key);
            sections.appendTag(section);
        }
        tag.setTag("GeneratedSections", sections);
    }

    @Override
    protected void readStructureFromNBT(NBTTagCompound tag, TemplateManager templates) {
        decorationSeed = tag.getLong("ShaftSeed");
        mesa = tag.getBoolean("Mesa");
        generatedSections.clear();
        NBTTagList sections = tag.getTagList("GeneratedSections", 10);
        for (int i = 0; i < sections.tagCount(); i++) generatedSections.add(sections.getCompoundTagAt(i).getLong("Pos"));
    }

    BlockPos worldPos(int x, int y, int z) {
        return new BlockPos(getXWithOffset(x, z), getYWithOffset(y), getZWithOffset(x, z));
    }

    private static boolean inApproach(int x, int z) {
        return z <= 6 && x >= CENTER_X - 1 && x <= CENTER_X + 1;
    }

    private static double shaftDistanceSq(int x, int z) {
        return (x - CENTER_X) * (x - CENTER_X) + (z - CENTER_Z) * (z - CENTER_Z);
    }

    /** Normalized distance from the chamber center; at most 1 inside the ellipse. */
    private double chamberDistance(int x, int z) {
        double radiusX = 5.6D + (decorationSeed & 3) * 0.1D;
        double radiusZ = 6.4D + ((decorationSeed >>> 2) & 3) * 0.1D;
        return Math.pow((x - CENTER_X) / radiusX, 2) + Math.pow((z - CENTER_Z) / radiusZ, 2);
    }

    private boolean interior(int x, int y, int z) {
        if (x < 0 || x >= WIDTH || z < 0 || z >= LENGTH || y < 1) return false;
        if (inApproach(x, z) && y >= DROP + 1 && y <= DROP + 3) return true;
        if (shaftDistanceSq(x, z) <= SHAFT_RADIUS_SQ && y <= DROP + 3) return true;
        double distance = chamberDistance(x, z);
        return distance <= 1 && y <= 2 + (int) Math.round(3 * (1 - distance));
    }

    private boolean shell(int x, int y, int z) {
        return interior(x, y - 1, z) || interior(x, y + 1, z)
                || interior(x - 1, y, z) || interior(x + 1, y, z)
                || interior(x, y, z - 1) || interior(x, y, z + 1);
    }

    /**
     * Which step of the spiral a wall cell belongs to, counted from the passage mouth, or -1 when the
     * cell is not on the wall ring. Each step drops one block, so a full turn descends LEDGE_SECTORS.
     */
    private static int ledgeSector(int x, int z) {
        double distanceSq = shaftDistanceSq(x, z);
        if (distanceSq > SHAFT_RADIUS_SQ || distanceSq < LEDGE_INNER_RADIUS_SQ) return -1;
        double angle = Math.atan2(z - CENTER_Z, x - CENTER_X) + Math.PI / 2;
        angle -= Math.floor(angle / (2 * Math.PI)) * 2 * Math.PI;
        return Math.min(LEDGE_SECTORS - 1, (int) (angle / (2 * Math.PI) * LEDGE_SECTORS));
    }

    private boolean isLedge(int x, int y, int z) {
        if (y < 2 || y > DROP) return false;
        // The landing at the passage mouth also lets the lower turns of the spiral stay continuous.
        if (y == DROP && z == CENTER_Z - 3 && inApproach(x, z)) return true;
        int sector = ledgeSector(x, z);
        return sector >= 0 && (DROP - y - sector) >= 0 && (DROP - y - sector) % LEDGE_SECTORS == 0;
    }

    @Override
    public boolean addComponentParts(World world, Random ignored, StructureBoundingBox region) {
        if (!boundingBox.intersectsWith(region)) return true;
        long section = ChunkPos.asLong(region.minX >> 4, region.minZ >> 4);
        if (generatedSections.contains(section)) return true;

        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < LENGTH; z++) {
                for (int y = 0; y < HEIGHT; y++) {
                    BlockPos pos = worldPos(x, y, z);
                    if (!region.isVecInside(pos)) continue;
                    IBlockState old = world.getBlockState(pos);
                    if (old.getBlock().hasTileEntity(old) || old.getBlockHardness(world, pos) < 0) continue;
                    if (interior(x, y, z)) {
                        setBlockState(world, Blocks.AIR.getDefaultState(), x, y, z, region);
                    } else if (shell(x, y, z)) {
                        Random cell = new Random(decorationSeed ^ (x * 341873128712L) ^ (z * 132897987541L) ^ (y * 73428767L));
                        if (cell.nextFloat() < (y == 0 ? 0.65F : 0.2F)) {
                            setBlockState(world, ModBlocks.crawler_wax_block.getDefaultState(), x, y, z, region);
                        } else if (!old.isFullCube() || old.getMaterial().isLiquid() || old.getBlock() instanceof BlockFalling) {
                            // Seal cave/liquid openings with a one-block rock shell, so the shaft stays
                            // whole even when neighboring regions arrive later.
                            setBlockState(world, Blocks.STONE.getDefaultState(), x, y, z, region);
                        }
                    }
                }
            }
        }

        // Spiral ledge hugging the wall, wetted with wax so the climb down is treacherous.
        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < LENGTH; z++) {
                for (int y = 2; y <= DROP; y++) {
                    if (!isLedge(x, y, z)) continue;
                    setBlockState(world, ModBlocks.crawler_wax_block.getDefaultState(), x, y, z, region);
                    Random cell = new Random(decorationSeed ^ (x * 912931L) ^ (z * 73428767L) ^ (y * 341873128712L));
                    if (cell.nextFloat() < 0.35F) placeWax(world, region, x, y + 1, z, EnumFacing.UP);
                }
            }
        }

        IBlockState planks = Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT,
                mesa ? BlockPlanks.EnumType.DARK_OAK : BlockPlanks.EnumType.OAK);
        IBlockState fence = (mesa ? Blocks.DARK_OAK_FENCE : Blocks.OAK_FENCE).getDefaultState();
        for (int y = DROP + 1; y <= DROP + 2; y++) {
            setBlockState(world, fence, CENTER_X - 1, y, 2, region);
            setBlockState(world, fence, CENTER_X + 1, y, 2, region);
        }
        for (int x = CENTER_X - 1; x <= CENTER_X + 1; x++) setBlockState(world, planks, x, DROP + 3, 2, region);
        // The rail runs straight off the landing and into the shaft.
        for (int z : new int[]{0, 1, 3, 4, 5}) {
            setBlockState(world, Blocks.RAIL.getDefaultState().withProperty(BlockRail.SHAPE,
                    BlockRailBase.EnumRailDirection.NORTH_SOUTH), CENTER_X, DROP + 1, z, region);
        }

        for (int x = 1; x < WIDTH - 1; x++) {
            for (int z = 1; z < LENGTH - 1; z++) {
                double distance = chamberDistance(x, z);
                if (distance > 1 || shaftDistanceSq(x, z) <= SHAFT_RADIUS_SQ) continue;
                int height = 2 + (int) Math.round(3 * (1 - distance));
                Random cell = new Random(decorationSeed ^ (x * 73428767L) ^ (z * 912931L));
                if (cell.nextFloat() < 0.45F) placeWax(world, region, x, 1, z, EnumFacing.UP);
                if (cell.nextFloat() < 0.3F) placeWax(world, region, x, height, z, EnumFacing.DOWN);
            }
        }

        Random decoration = new Random(decorationSeed);
        int minCocoons = Math.max(0, Settings.worldgenSettings.minecrawlerLairMinCocoons);
        int maxCocoons = Math.max(minCocoons, Settings.worldgenSettings.minecrawlerLairMaxCocoons);
        int cocoons = minCocoons + (maxCocoons > minCocoons ? decoration.nextInt(maxCocoons - minCocoons + 1) : 0);
        cocoons = Math.min(cocoons, COCOON_SITES.length);
        for (int i = 0; i < cocoons; i++) {
            int x = COCOON_SITES[i][0];
            int z = COCOON_SITES[i][1];
            setBlockState(world, ModBlocks.crawler_wax_block.getDefaultState(), x, 0, z, region);
            setBlockState(world, ModBlocks.minecrawler_cocoon.getDefaultState()
                    .withProperty(BlockTOFT.FACING, EnumFacing.Plane.HORIZONTAL.random(decoration))
                    .withProperty(BlockTOFT.SNOWY, false), x, 1, z, region);
        }
        if (decoration.nextFloat() < SKELETON_CHANCE) {
            placeCorpse(world, region, decoration, 4, 14);
            if (decoration.nextFloat() < SECOND_SKELETON_CHANCE) placeCorpse(world, region, decoration, 8, 4);
        }

        spawnLootMinecart(world, region);
        for (int[] site : CRAWLER_SITES) spawnCrawler(world, region, site[0], site[1]);
        generatedSections.add(section);
        return true;
    }

    private void spawnLootMinecart(World world, StructureBoundingBox region) {
        // One seeded roll per shaft, independent of population order and other decorations.
        Random lootRandom = new Random(decorationSeed ^ 0xBB67AE8584CAA73BL);
        if (lootRandom.nextInt(3) != 0) return;
        BlockPos pos = worldPos(CENTER_X, 1, 15);
        if (!region.isVecInside(pos)) return;
        setBlockState(world, Blocks.RAIL.getDefaultState().withProperty(BlockRail.SHAPE,
                BlockRailBase.EnumRailDirection.NORTH_SOUTH), CENTER_X, 1, 15, region);
        EntityMinecartChest cart = new EntityMinecartChest(world,
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        cart.setLootTable(LootTableList.CHESTS_ABANDONED_MINESHAFT, lootRandom.nextLong());
        world.spawnEntity(cart);
    }

    private void placeWax(World world, StructureBoundingBox region, int x, int y, int z, EnumFacing facing) {
        BlockPos pos = worldPos(x, y, z);
        if (region.isVecInside(pos) && world.isAirBlock(pos)) {
            setBlockState(world, ModBlocks.crawler_wax_wet.getDefaultState()
                    .withProperty(BlockCrawlerWaxWet.FACING, facing), x, y, z, region);
        }
    }

    private void placeCorpse(World world, StructureBoundingBox region, Random random, int x, int z) {
        BlockPos pos = worldPos(x, 1, z);
        if (!region.isVecInside(pos)) return;
        setBlockState(world, ModBlocks.skeleton_corpse.getDefaultState()
                .withProperty(BlockSkeletonCorpse.POSE, BlockSkeletonCorpse.Pose.GROUND), x, 1, z, region);
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntitySkeletonCorpse) {
            TileEntitySkeletonCorpse corpse = (TileEntitySkeletonCorpse) tile;
            corpse.setRotation(random.nextInt(16));
            corpse.setHeldItem(((BlockSkeletonCorpse) ModBlocks.skeleton_corpse).rollHeldItem(world, random));
        }
    }

    private void spawnCrawler(World world, StructureBoundingBox region, int x, int z) {
        BlockPos pos = worldPos(x, 1, z);
        if (!region.isVecInside(pos)) return;
        EntityMinecrawler crawler = new EntityMinecrawler(world);
        // Nudge boundary spawns inward so their whole body is in carved terrain.
        double spawnX = Math.max(region.minX + 0.6D, Math.min(region.maxX + 0.4D, pos.getX() + 0.5D));
        double spawnZ = Math.max(region.minZ + 0.6D, Math.min(region.maxZ + 0.4D, pos.getZ() + 0.5D));
        crawler.setLocationAndAngles(spawnX, pos.getY(), spawnZ, 0, 0);
        AxisAlignedBB body = crawler.getEntityBoundingBox();
        if (world.getCollisionBoxes(crawler, body).isEmpty() && !world.containsAnyLiquid(body)) {
            crawler.enablePersistence();
            world.spawnEntity(crawler);
        }
    }
}
