package com.windanesz.tracesofthefallen.world.structure;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.entity.EntityGoblinNest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponentTemplate;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;

import java.util.*;

/**
 * A goblin-held cavern buried in solid rock: the NBT template (a stone shell with the cavern hollowed out
 * of it), plus a winding tunnel carved out of every opening the template leaves in its walls, plus its
 * inhabitants. Placed one population region at a time, so everything here is deterministic from the piece's
 * seed and only touches blocks inside the region it is handed.
 */
public class ComponentGoblinCavern extends StructureComponentTemplate {
    // The cavern's standing garrison: one of each, always.
    private static final String[] GARRISON = {"goblin_shaman", "goblin_brute", "goblin_trapper"};
    // Natural ground the template stands on; props like bonepiles, beds and slabs are not valid floors.
    private static final Set<String> FLOOR_BLOCKS = new HashSet<>(Arrays.asList(
            "minecraft:stone", "minecraft:dirt", "minecraft:grass"));

    private static final int TUNNEL_RADIUS = 1;
    private static final int TUNNEL_HEIGHT = 3;
    private static final int TUNNEL_MIN_LENGTH = 14;
    private static final int TUNNEL_EXTRA_LENGTH = 13;
    private static final int TUNNEL_MAX_DRIFT = 3;

    private String templateName;
    private Rotation rotation;
    private long seed;
    private final Set<Long> generatedSections = new HashSet<>();

    // Rebuilt from the template whenever the piece is created or loaded; never saved.
    private Layout layout;
    private final List<List<BlockPos>> tunnels = new ArrayList<>();

    // Required by MapGenStructureIO when loading existing structures.
    public ComponentGoblinCavern() {
    }

    public ComponentGoblinCavern(TemplateManager templateManager, String templateName, BlockPos templatePosition, Rotation rotation, long seed) {
        super(0);
        this.templateName = templateName;
        this.templatePosition = templatePosition;
        this.rotation = rotation;
        this.seed = seed;
        this.loadTemplate(templateManager);
    }

    private void loadTemplate(TemplateManager templateManager) {
        Template template = templateManager.getTemplate(null, new ResourceLocation(TracesOfTheFallen.MODID, this.templateName));
        this.setup(template, this.templatePosition, new PlacementSettings().setRotation(this.rotation));
        this.layout = Layout.read(template);

        // Vanilla's template bounding box is one block too wide in X and Z, so build the exact one.
        BlockPos size = template.getSize();
        BlockPos near = toWorld(BlockPos.ORIGIN);
        BlockPos far = toWorld(new BlockPos(size.getX() - 1, size.getY() - 1, size.getZ() - 1));
        this.boundingBox = new StructureBoundingBox(
                Math.min(near.getX(), far.getX()), Math.min(near.getY(), far.getY()), Math.min(near.getZ(), far.getZ()),
                Math.max(near.getX(), far.getX()), Math.max(near.getY(), far.getY()), Math.max(near.getZ(), far.getZ()));

        this.tunnels.clear();
        for (int index = 0; index < this.layout.exits.size(); index++) {
            this.tunnels.add(buildTunnel(this.layout.exits.get(index), new Random(this.seed ^ (0x2545F4914F6CDD1DL * (index + 1)))));
        }
        // The shell is only the cavern; the structure also covers every tunnel leading away from it.
        for (List<BlockPos> tunnel : this.tunnels) {
            for (BlockPos point : tunnel) {
                BlockPos world = toWorld(point);
                this.boundingBox.expandTo(new StructureBoundingBox(
                        world.getX() - TUNNEL_RADIUS, world.getY(), world.getZ() - TUNNEL_RADIUS,
                        world.getX() + TUNNEL_RADIUS, world.getY() + TUNNEL_HEIGHT - 1, world.getZ() + TUNNEL_RADIUS));
            }
        }
    }

    @Override
    protected void writeStructureToNBT(NBTTagCompound tagCompound) {
        super.writeStructureToNBT(tagCompound);
        tagCompound.setString("Template", this.templateName);
        tagCompound.setString("Rot", this.rotation.name());
        tagCompound.setLong("CavernSeed", this.seed);

        NBTTagList sections = new NBTTagList();
        for (long key : this.generatedSections) {
            NBTTagCompound section = new NBTTagCompound();
            section.setLong("Pos", key);
            sections.appendTag(section);
        }
        tagCompound.setTag("GeneratedSections", sections);
    }

    @Override
    protected void readStructureFromNBT(NBTTagCompound tagCompound, TemplateManager templateManager) {
        super.readStructureFromNBT(tagCompound, templateManager);
        this.templateName = tagCompound.getString("Template");
        this.rotation = Rotation.valueOf(tagCompound.getString("Rot"));
        this.seed = tagCompound.getLong("CavernSeed");

        this.generatedSections.clear();
        NBTTagList sections = tagCompound.getTagList("GeneratedSections", 10);
        for (int index = 0; index < sections.tagCount(); index++) {
            this.generatedSections.add(sections.getCompoundTagAt(index).getLong("Pos"));
        }

        this.loadTemplate(templateManager);
    }

    private BlockPos toWorld(BlockPos local) {
        return Template.transformedBlockPos(this.placeSettings, local).add(this.templatePosition);
    }

    @Override
    public boolean addComponentParts(World world, Random random, StructureBoundingBox region) {
        long section = ChunkPos.asLong(region.minX >> 4, region.minZ >> 4);
        if (this.generatedSections.contains(section)) {
            return true;
        }

        super.addComponentParts(world, random, region);
        carveTunnels(world, region);
        populate(world, region);

        this.generatedSections.add(section);
        return true;
    }

    @Override
    protected void handleDataMarker(String function, BlockPos pos, World worldIn, Random rand, StructureBoundingBox sbb) {
        // The cavern template carries no data markers. Never leave a stray structure block behind if one is added.
        worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
    }

    // ---------------------------------------------------------------------------------------------------
    // Entrance tunnels
    // ---------------------------------------------------------------------------------------------------

    /**
     * Winds a tunnel out from an opening in the template wall, in template-local coordinates. The tunnel
     * never turns back toward the cavern, so it can't re-enter the shell or double back on itself, and it
     * drifts by at most one block per bend so the floor stays walkable. Each point is the floor cell of a
     * 3x3x3 slice.
     */
    private static List<BlockPos> buildTunnel(Exit exit, Random random) {
        List<BlockPos> path = new ArrayList<>();
        EnumFacing outward = exit.outward;
        EnumFacing heading = outward;
        int length = TUNNEL_MIN_LENGTH + random.nextInt(TUNNEL_EXTRA_LENGTH);
        int untilBend = 3 + random.nextInt(3);
        int x = exit.mouth.getX();
        int y = exit.mouth.getY();
        int z = exit.mouth.getZ();

        for (int step = 0; step < length; step++) {
            path.add(new BlockPos(x, y, z));

            if (--untilBend <= 0) {
                untilBend = 3 + random.nextInt(4);
                int roll = random.nextInt(4);
                EnumFacing next = roll == 0 ? heading.rotateY() : roll == 1 ? heading.rotateYCCW() : heading;
                if (next.getXOffset() * outward.getXOffset() + next.getZOffset() * outward.getZOffset() < 0) {
                    next = next.getOpposite();
                }
                heading = next;
                y = Math.max(1, Math.min(exit.mouth.getY() + TUNNEL_MAX_DRIFT,
                        Math.max(exit.mouth.getY() - TUNNEL_MAX_DRIFT, y + random.nextInt(3) - 1)));
            }

            x += heading.getXOffset();
            z += heading.getZOffset();
        }
        return path;
    }

    private void carveTunnels(World world, StructureBoundingBox region) {
        for (List<BlockPos> tunnel : this.tunnels) {
            for (BlockPos point : tunnel) {
                for (int dx = -TUNNEL_RADIUS; dx <= TUNNEL_RADIUS; dx++) {
                    for (int dz = -TUNNEL_RADIUS; dz <= TUNNEL_RADIUS; dz++) {
                        for (int dy = 0; dy < TUNNEL_HEIGHT; dy++) {
                            BlockPos local = point.add(dx, dy, dz);
                            // The template already decides what its own footprint looks like. A tunnel slice
                            // beside the shell must not nibble its wall open.
                            if (this.layout.isInsideFootprint(local)) {
                                continue;
                            }
                            BlockPos pos = toWorld(local);
                            if (!region.isVecInside(pos)) {
                                continue;
                            }
                            IBlockState state = world.getBlockState(pos);
                            if (state.getBlock().hasTileEntity(state) || state.getBlockHardness(world, pos) < 0
                                    || state.getMaterial().isLiquid()) {
                                continue;
                            }
                            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
                        }
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------------------------------------------
    // Inhabitants
    // ---------------------------------------------------------------------------------------------------

    /**
     * Every placement is chosen from the seed alone, so each population region reproduces the same choices
     * and spawns only the ones that fall inside it - no inhabitant is doubled or lost at a region seam.
     */
    private void populate(World world, StructureBoundingBox region) {
        Settings.WorldgenSettings settings = Settings.worldgenSettings;
        Random random = new Random(this.seed ^ 0x9E3779B97F4A7C15L);
        List<BlockPos> candidates = new ArrayList<>(this.layout.standCells);
        Collections.shuffle(candidates, random);

        int nestTarget = randomBetween(random, settings.goblinCavernMinNests, settings.goblinCavernMaxNests);

        List<BlockPos> nestCells = new ArrayList<>();
        for (BlockPos cell : candidates) {
            if (nestCells.size() >= nestTarget) {
                break;
            }
            if (this.layout.hasNestPatch(cell) && isFarFrom(cell, nestCells, 5)) {
                nestCells.add(cell);
            }
        }

        List<BlockPos> goblinCells = new ArrayList<>();
        for (BlockPos cell : candidates) {
            if (goblinCells.size() >= GARRISON.length) {
                break;
            }
            if (isFarFrom(cell, nestCells, 3) && isFarFrom(cell, goblinCells, 2)) {
                goblinCells.add(cell);
            }
        }

        for (BlockPos cell : nestCells) {
            spawnNest(world, region, cell);
        }
        for (int index = 0; index < goblinCells.size(); index++) {
            // Rolled before the region check so the sequence is the same in every region.
            float yaw = random.nextFloat() * 360.0F;
            spawnGoblin(world, region, goblinCells.get(index), GARRISON[index], yaw);
        }
    }

    private static int randomBetween(Random random, int min, int max) {
        int low = Math.max(0, min);
        int high = Math.max(low, max);
        return low + (high > low ? random.nextInt(high - low + 1) : 0);
    }

    private static boolean isFarFrom(BlockPos cell, List<BlockPos> others, int distance) {
        for (BlockPos other : others) {
            if (cell.distanceSq(other) < distance * distance) {
                return false;
            }
        }
        return true;
    }

    /** The nest is 2 blocks wide, so it sits on the shared corner of a 2x2 patch whose min corner is localCell. */
    private void spawnNest(World world, StructureBoundingBox region, BlockPos localCell) {
        BlockPos near = toWorld(localCell);
        BlockPos far = toWorld(localCell.add(1, 0, 1));
        // Midpoint of two diagonal cell centers, so it holds for every rotation.
        double centerX = (near.getX() + far.getX()) / 2.0D + 0.5D;
        double centerZ = (near.getZ() + far.getZ()) / 2.0D + 0.5D;
        // Spawn from the region that owns the nest's center, so it never lands in a chunk that isn't loaded.
        if (!region.isVecInside(new BlockPos(MathHelper.floor(centerX), near.getY(), MathHelper.floor(centerZ)))) {
            return;
        }
        EntityGoblinNest nest = new EntityGoblinNest(world, centerX, near.getY(), centerZ);
        nest.setFacing(this.rotation.rotate(this.layout.facingTowardCenter(localCell)));
        nest.setPersistentBroods(true);
        world.spawnEntity(nest);
    }

    private void spawnGoblin(World world, StructureBoundingBox region, BlockPos localCell, String type, float yaw) {
        BlockPos pos = toWorld(localCell);
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

    // ---------------------------------------------------------------------------------------------------
    // Template geometry
    // ---------------------------------------------------------------------------------------------------

    /** A gap in one of the template's four side walls, where a tunnel should leave it. */
    private static final class Exit {
        final EnumFacing outward;
        /** Template-local floor cell just outside the wall, centered on the gap. */
        final BlockPos mouth;

        Exit(EnumFacing outward, BlockPos mouth) {
            this.outward = outward;
            this.mouth = mouth;
        }
    }

    /**
     * What the piece needs to know about the template's contents, read from the template itself so the
     * cavern can be reshaped in the NBT file without touching this code. All coordinates are template-local.
     */
    private static final class Layout {
        final int sizeX;
        final int sizeZ;
        /** Open cells with natural ground below and two blocks of headroom, in a fixed scan order. */
        final List<BlockPos> standCells = new ArrayList<>();
        final Set<BlockPos> standSet = new HashSet<>();
        final List<Exit> exits = new ArrayList<>();

        private Layout(int sizeX, int sizeZ) {
            this.sizeX = sizeX;
            this.sizeZ = sizeZ;
        }

        static Layout read(Template template) {
            NBTTagCompound tag = template.writeToNBT(new NBTTagCompound());
            NBTTagList palette = tag.getTagList("palette", 10);
            boolean[] air = new boolean[palette.tagCount()];
            boolean[] floor = new boolean[palette.tagCount()];
            for (int index = 0; index < palette.tagCount(); index++) {
                String name = palette.getCompoundTagAt(index).getString("Name");
                air[index] = name.equals("minecraft:air");
                floor[index] = FLOOR_BLOCKS.contains(name);
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

            Layout layout = new Layout(sizeX, sizeZ);
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

        boolean isInsideFootprint(BlockPos cell) {
            return cell.getX() >= 0 && cell.getX() < sizeX && cell.getZ() >= 0 && cell.getZ() < sizeZ;
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

        EnumFacing facingTowardCenter(BlockPos cell) {
            double deltaX = sizeX / 2.0D - (cell.getX() + 0.5D);
            double deltaZ = sizeZ / 2.0D - (cell.getZ() + 0.5D);
            if (Math.abs(deltaX) > Math.abs(deltaZ)) {
                return deltaX > 0.0D ? EnumFacing.EAST : EnumFacing.WEST;
            }
            return deltaZ > 0.0D ? EnumFacing.SOUTH : EnumFacing.NORTH;
        }
    }
}
