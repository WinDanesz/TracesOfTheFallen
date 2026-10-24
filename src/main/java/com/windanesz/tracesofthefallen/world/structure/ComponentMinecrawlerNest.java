package com.windanesz.tracesofthefallen.world.structure;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.block.BlockCrawlerWaxWet;
import com.windanesz.tracesofthefallen.block.BlockSkeletonCorpse;
import com.windanesz.tracesofthefallen.block.BlockTOFT;
import com.windanesz.tracesofthefallen.block.TileEntitySkeletonCorpse;
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

/** A supported mine passage giving way to a wax-lined chamber, placed one population region at a time. */
public class ComponentMinecrawlerNest extends StructureComponent {
    private static final int WIDTH = 11;
    private static final int LENGTH = 38;
    private static final int HEIGHT = 7;
    private long decorationSeed;
    private boolean mesa;
    private static final float SKELETON_CHANCE = 0.60F;
    private static final float SECOND_SKELETON_CHANCE = 0.15F;
    private final Set<Long> generatedSections = new HashSet<>();

    // Required by MapGenStructureIO when loading existing mineshafts.
    public ComponentMinecrawlerNest() {
    }

    public ComponentMinecrawlerNest(BlockPos entranceFloor, EnumFacing facing, boolean mesa, long seed) {
        super(0);
        setCoordBaseMode(facing);
        this.mesa = mesa;
        this.decorationSeed = seed;
        int x = entranceFloor.getX();
        int y = entranceFloor.getY();
        int z = entranceFloor.getZ();
        switch (facing) {
            case NORTH: boundingBox = new StructureBoundingBox(x - 5, y, z - LENGTH + 1, x + 5, y + HEIGHT - 1, z); break;
            case SOUTH: boundingBox = new StructureBoundingBox(x - 5, y, z, x + 5, y + HEIGHT - 1, z + LENGTH - 1); break;
            case WEST: boundingBox = new StructureBoundingBox(x - LENGTH + 1, y, z - 5, x, y + HEIGHT - 1, z + 5); break;
            case EAST: boundingBox = new StructureBoundingBox(x, y, z - 5, x + LENGTH - 1, y + HEIGHT - 1, z + 5); break;
            default: throw new IllegalArgumentException("Minecrawler branches must face horizontally");
        }
    }

    @Override
    protected void writeStructureToNBT(NBTTagCompound tag) {
        tag.setLong("NestSeed", decorationSeed);
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
        decorationSeed = tag.getLong("NestSeed");
        mesa = tag.getBoolean("Mesa");
        generatedSections.clear();
        NBTTagList sections = tag.getTagList("GeneratedSections", 10);
        for (int i = 0; i < sections.tagCount(); i++) generatedSections.add(sections.getCompoundTagAt(i).getLong("Pos"));
    }

    BlockPos worldPos(int x, int y, int z) {
        return new BlockPos(getXWithOffset(x, z), getYWithOffset(y), getZWithOffset(x, z));
    }

    private int clearance(int x, int z) {
        if (x < 0 || x >= WIDTH || z < 0 || z >= LENGTH) return 0;
        if (z <= 11 && x >= 4 && x <= 6) return 3;
        // The central passage continues past the first chamber. Most nests open into a second
        // chamber; the remainder stop after the original room, preserving some compact variants.
        int connectorEnd = hasRoomTunnel() ? 26 : 25;
        if (z >= 22 && z <= connectorEnd && x >= 4 && x <= 6) return hasExtraRoom() ? 3 : 0;
        double radiusX = 4.3D + (decorationSeed & 3) * 0.1D;
        double radiusZ = 5.0D + ((decorationSeed >>> 2) & 3) * 0.1D;
        double centerZ = (hasExtraRoom() && z >= (hasRoomTunnel() ? 27 : 25)) ? 32.0D : 16.0D;
        if (!hasExtraRoom() && z >= 22) return 0;
        double distance = Math.pow((x - 5) / radiusX, 2) + Math.pow((z - centerZ) / radiusZ, 2);
        return distance <= 1 ? 2 + (int) Math.round(3 * (1 - distance)) : 0;
    }

    private boolean hasExtraRoom() {
        return new Random(decorationSeed ^ 0x5DEECE66DL).nextFloat() < 0.75F;
    }

    private boolean hasRoomTunnel() {
        return hasExtraRoom() && new Random(decorationSeed ^ 0x6A09E667F3BCC909L).nextFloat() < 0.50F;
    }

    private boolean interior(int x, int y, int z) {
        return y >= 1 && y <= clearance(x, z);
    }

    private boolean shell(int x, int y, int z) {
        return interior(x, y - 1, z) || interior(x, y + 1, z)
                || interior(x - 1, y, z) || interior(x + 1, y, z)
                || interior(x, y, z - 1) || interior(x, y, z + 1);
    }

    @Override
    public boolean addComponentParts(World world, Random ignored, StructureBoundingBox region) {
        if (!boundingBox.intersectsWith(region)) return true;
        long section = ChunkPos.asLong(region.minX >> 4, region.minZ >> 4);
        if (generatedSections.contains(section)) return true;

        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < LENGTH; z++) {
                Random cell = new Random(decorationSeed ^ (x * 341873128712L) ^ (z * 132897987541L));
                boolean wax = cell.nextFloat() < (z < 6 ? 0.08F : z < 11 ? 0.35F : 0.65F);
                for (int y = 0; y < HEIGHT; y++) {
                    BlockPos pos = worldPos(x, y, z);
                    if (!region.isVecInside(pos)) continue;
                    IBlockState old = world.getBlockState(pos);
                    if (old.getBlock().hasTileEntity(old) || old.getBlockHardness(world, pos) < 0) continue;
                    if (interior(x, y, z)) {
                        setBlockState(world, Blocks.AIR.getDefaultState(), x, y, z, region);
                    } else if (shell(x, y, z)) {
                        if (wax && y == 0) {
                            setBlockState(world, ModBlocks.crawler_wax_block.getDefaultState(), x, y, z, region);
                        } else if (!old.isFullCube() || old.getMaterial().isLiquid() || old.getBlock() instanceof BlockFalling) {
                            // Seal cave/liquid openings with a one-block rock shell. This
                            // keeps a nest whole even when neighboring regions arrive later.
                            setBlockState(world, Blocks.STONE.getDefaultState(), x, y, z, region);
                        }
                    }
                }
            }
        }

        IBlockState planks = Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT,
                mesa ? BlockPlanks.EnumType.DARK_OAK : BlockPlanks.EnumType.OAK);
        IBlockState fence = (mesa ? Blocks.DARK_OAK_FENCE : Blocks.OAK_FENCE).getDefaultState();
        for (int z : new int[]{2, 7}) {
            for (int y = 1; y <= 2; y++) {
                setBlockState(world, fence, 4, y, z, region);
                setBlockState(world, fence, 6, y, z, region);
            }
            for (int x = 4; x <= 6; x++) setBlockState(world, planks, x, 3, z, region);
        }
        for (int z : new int[]{0, 1, 3, 4, 6, 8}) {
            setBlockState(world, Blocks.RAIL.getDefaultState().withProperty(BlockRail.SHAPE,
                    BlockRailBase.EnumRailDirection.NORTH_SOUTH), 5, 1, z, region);
        }

        for (int x = 1; x < WIDTH - 1; x++) {
            for (int z = 5; z < LENGTH - 1; z++) {
                int height = clearance(x, z);
                if (height == 0) continue;
                Random cell = new Random(decorationSeed ^ (x * 73428767L) ^ (z * 912931L));
                if (cell.nextFloat() < (z < 11 ? 0.22F : 0.5F)) {
                    placeWax(world, region, x, 1, z, EnumFacing.UP);
                }
                if (z >= 11 && cell.nextFloat() < 0.3F) {
                    placeWax(world, region, x, height, z, EnumFacing.DOWN);
                }
            }
        }

        Random decoration = new Random(decorationSeed);
        int minCocoons = Math.max(0, Settings.worldgenSettings.minecrawlerLairMinCocoons);
        int maxCocoons = Math.max(minCocoons, Settings.worldgenSettings.minecrawlerLairMaxCocoons);
        int cocoons = minCocoons + (maxCocoons > minCocoons ? decoration.nextInt(maxCocoons - minCocoons + 1) : 0);
        // Sites are spread through the chamber instead of stopping at three fixed positions. The
        // old 1-3 roll made most nests look empty, especially after a wax decoration occupied one
        // of those positions.
        int[][] cocoonSites = hasExtraRoom()
                ? new int[][]{{2, 13}, {8, 14}, {3, 16}, {7, 16}, {2, 18}, {8, 19}, {2, 27}, {8, 29}, {5, 32}}
                : new int[][]{{2, 13}, {8, 14}, {3, 16}, {7, 16}, {2, 18}, {8, 19}, {5, 20}, {5, 15}};
        cocoons = Math.min(cocoons, cocoonSites.length);
        for (int i = 0; i < cocoons; i++) {
            int x = cocoonSites[i][0];
            int z = cocoonSites[i][1];
            setBlockState(world, ModBlocks.crawler_wax_block.getDefaultState(), x, 0, z, region);
            setBlockState(world, ModBlocks.minecrawler_cocoon.getDefaultState()
                    .withProperty(BlockTOFT.FACING, EnumFacing.Plane.HORIZONTAL.random(decoration))
                    .withProperty(BlockTOFT.SNOWY, false), x, 1, z, region);
        }
        if (decoration.nextInt(4) == 0) {
            setBlockState(world, ModBlocks.lost_crate_potions.getDefaultState()
                    .withProperty(BlockTOFT.FACING, EnumFacing.WEST).withProperty(BlockTOFT.SNOWY, false), 7, 1, 14, region);
        }
        // Most nests have one visible skeleton, while some remain crawler-only. A second corpse is
        // deliberately uncommon so skeletons stay a discovery rather than standard scenery.
        if (decoration.nextFloat() < SKELETON_CHANCE) {
            placeCorpse(world, region, decoration, 3, 18);
            if (decoration.nextFloat() < SECOND_SKELETON_CHANCE) placeCorpse(world, region, decoration, 7, 19);
            if (hasExtraRoom() && decoration.nextFloat() < 0.25F) placeCorpse(world, region, decoration, 5, 30);
        }

        spawnLootMinecart(world, region);
        spawnCrawler(world, region, 5, 5);
        spawnCrawler(world, region, 4, 15);
        spawnCrawler(world, region, 6, 18);
        generatedSections.add(section);
        return true;
    }

    private void spawnLootMinecart(World world, StructureBoundingBox region) {
        // One seeded roll per nest, independent of population order and other decorations.
        // Only the region containing this rail can spawn it; generatedSections persists that guard.
        Random lootRandom = new Random(decorationSeed ^ 0xBB67AE8584CAA73BL);
        if (lootRandom.nextInt(4) != 0) return;
        BlockPos pos = worldPos(5, 1, 17);
        if (!region.isVecInside(pos)) return;
        // A dedicated spot inside the main chamber, clear of cocoons and corpse sites.
        setBlockState(world, Blocks.RAIL.getDefaultState().withProperty(BlockRail.SHAPE,
                BlockRailBase.EnumRailDirection.NORTH_SOUTH), 5, 1, 17, region);
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
        // Its 1.1-block width can straddle a region boundary at a block center.
        // Nudge boundary spawns inward so their entire body is in carved terrain.
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
