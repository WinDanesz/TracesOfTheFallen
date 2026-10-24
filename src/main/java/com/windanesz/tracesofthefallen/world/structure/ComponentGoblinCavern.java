package com.windanesz.tracesofthefallen.world.structure;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.entity.living.EntityGoblinNest;
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

    private static final String FIRE_RUNE_BLOCK = "totf:bonepile_fire_rune";

    // Tunnels are carved as irregular caves. The longest one (cavern half-width + length + widest chamber)
    // must stay well inside the 8-chunk range MapGenBase plans starts from, or distant chunks would never see it.
    private static final int TUNNEL_MIN_LENGTH = 45;
    private static final int TUNNEL_EXTRA_LENGTH = 36;
    private static final int TUNNEL_MAX_DRIFT = 10;
    private static final int TUNNEL_MIN_WORLD_Y = 6;
    private static final float TUNNEL_MIN_RADIUS = 1.5F;
    private static final float TUNNEL_MAX_RADIUS = 3.2F;
    private static final float CHAMBER_MAX_RADIUS = 4.6F;
    private static final int TUNNEL_MIN_HEIGHT = 3;
    private static final int TUNNEL_MAX_HEIGHT = 6;
    // Caves stay below this world Y so they never break through to the surface.
    private static final int TUNNEL_MAX_WORLD_Y = 46;
    // A tunnel keeps heading roughly straight out of the shell until it has cleared the cavern by this many blocks.
    private static final int TUNNEL_MIN_CLEARANCE = 8;
    private static final double STRAIGHT_TURN_LIMIT = Math.toRadians(25);
    private static final double CAVE_TURN_LIMIT = Math.toRadians(80);
    private static final int MAX_BRANCHES = 2;

    private String templateName;
    private Rotation rotation;
    private long seed;
    private final Set<Long> generatedSections = new HashSet<>();

    // Rebuilt from the template whenever the piece is created or loaded; never saved.
    private Layout layout;
    private final List<List<CavePoint>> tunnels = new ArrayList<>();

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
            this.tunnels.add(buildTunnel(this.layout.exits.get(index), this.templatePosition.getY(),
                    new Random(this.seed ^ (0x2545F4914F6CDD1DL * (index + 1)))));
        }
        // The shell is only the cavern; the structure also covers every tunnel leading away from it.
        for (List<CavePoint> tunnel : this.tunnels) {
            for (CavePoint point : tunnel) {
                BlockPos world = toWorld(point.floor);
                int reach = point.reach();
                this.boundingBox.expandTo(new StructureBoundingBox(
                        world.getX() - reach, world.getY(), world.getZ() - reach,
                        world.getX() + reach, world.getY() + point.height - 1, world.getZ() + reach));
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
     * Digs a long, irregular cave out from an opening in the template wall, in template-local coordinates.
     * The path is a random walk: it holds its course out of the shell until it has cleared it, then meanders
     * (never more than 80 degrees off the way out, so it can't double back on the cavern), rises and sinks
     * gently, swells into chambers and narrows again, and may fork into short side branches. Each point is
     * the floor cell of one cave slice; the floor never steps by more than a block, so it stays walkable.
     */
    private static List<CavePoint> buildTunnel(Exit exit, int baseY, Random random) {
        List<CavePoint> path = new ArrayList<>();
        double outwardAngle = StrictMath.atan2(exit.outward.getZOffset(), exit.outward.getXOffset());
        int length = TUNNEL_MIN_LENGTH + random.nextInt(TUNNEL_EXTRA_LENGTH);

        int mouthWorldY = exit.mouth.getY() + baseY;
        int lowY = Math.max(TUNNEL_MIN_WORLD_Y, mouthWorldY - TUNNEL_MAX_DRIFT) - baseY;
        int highY = Math.max(mouthWorldY, Math.min(mouthWorldY + TUNNEL_MAX_DRIFT, TUNNEL_MAX_WORLD_Y)) - baseY;

        // Branches fork off the main cave once it is well clear of the shell.
        int branches = random.nextInt(MAX_BRANCHES + 1);
        List<int[]> forks = new ArrayList<>();
        for (int index = 0; index < branches; index++) {
            forks.add(new int[]{TUNNEL_MIN_CLEARANCE + 6 + random.nextInt(Math.max(1, length - TUNNEL_MIN_CLEARANCE - 14)), index});
        }

        List<CavePoint> main = walkCave(exit.mouth, outwardAngle, outwardAngle, length, lowY, highY, true, random);
        path.addAll(main);
        for (int[] fork : forks) {
            if (fork[0] >= main.size()) {
                continue;
            }
            CavePoint from = main.get(fork[0]);
            double side = random.nextBoolean() ? 1.0D : -1.0D;
            double angle = outwardAngle + side * Math.toRadians(40 + random.nextInt(30));
            path.addAll(walkCave(from.floor, angle, outwardAngle, 12 + random.nextInt(14), lowY, highY, false, random));
        }
        return path;
    }

    private static List<CavePoint> walkCave(BlockPos start, double startAngle, double outwardAngle, int length,
                                            int lowY, int highY, boolean holdCourseAtStart, Random random) {
        List<CavePoint> points = new ArrayList<>();
        double x = start.getX();
        double z = start.getZ();
        double floorY = Math.max(lowY, Math.min(highY, start.getY()));
        double angle = startAngle;
        double slope = 0.0D;
        float radius = TUNNEL_MIN_RADIUS + random.nextFloat() * (TUNNEL_MAX_RADIUS - TUNNEL_MIN_RADIUS);
        float targetRadius = radius;
        int untilResize = 4 + random.nextInt(8);
        double travelled = 0.0D;

        for (int step = 0; step < length; step++) {
            int height = Math.max(TUNNEL_MIN_HEIGHT, Math.min(TUNNEL_MAX_HEIGHT, Math.round(radius * 1.6F + 0.5F)));
            points.add(new CavePoint(new BlockPos(MathHelper.floor(x + 0.5D), (int) Math.round(floorY), MathHelper.floor(z + 0.5D)), radius, height));

            if (--untilResize <= 0) {
                untilResize = 4 + random.nextInt(9);
                // Now and then the cave opens into a chamber; otherwise it just breathes in and out.
                targetRadius = random.nextInt(6) == 0
                        ? TUNNEL_MAX_RADIUS + random.nextFloat() * (CHAMBER_MAX_RADIUS - TUNNEL_MAX_RADIUS)
                        : TUNNEL_MIN_RADIUS + random.nextFloat() * (TUNNEL_MAX_RADIUS - TUNNEL_MIN_RADIUS);
            }
            radius += Math.max(-0.2F, Math.min(0.2F, targetRadius - radius));

            // Keep the heading within a cone around the way out of the shell; narrow until the shell is cleared.
            double limit = holdCourseAtStart && travelled < TUNNEL_MIN_CLEARANCE ? STRAIGHT_TURN_LIMIT : CAVE_TURN_LIMIT;
            angle += (random.nextDouble() - 0.5D) * 0.5D;
            angle = outwardAngle + Math.max(-limit, Math.min(limit, angle - outwardAngle));
            x += StrictMath.cos(angle);
            z += StrictMath.sin(angle);
            travelled += 1.0D;

            // The floor wanders up and down by at most a block per step, bouncing off the depth limits.
            slope = Math.max(-0.4D, Math.min(0.4D, slope + (random.nextDouble() - 0.5D) * 0.3D));
            floorY += slope;
            if (floorY < lowY || floorY > highY) {
                floorY = Math.max(lowY, Math.min(highY, floorY));
                slope = -slope;
            }
        }
        return points;
    }

    private void carveTunnels(World world, StructureBoundingBox region) {
        for (List<CavePoint> tunnel : this.tunnels) {
            for (CavePoint point : tunnel) {
                int reach = point.reach();
                // Half an ellipsoid-ish blob standing on the floor cell: rounder than a box, never narrower at
                // the centre column than a walkable passage.
                double centreY = (point.height - 1) / 2.0D;
                double verticalRadius = point.height / 2.0D;
                for (int dx = -reach; dx <= reach; dx++) {
                    for (int dz = -reach; dz <= reach; dz++) {
                        for (int dy = 0; dy < point.height; dy++) {
                            double across = (dx * dx + dz * dz) / (double) (point.radius * point.radius);
                            double vertical = (dy - centreY) / verticalRadius;
                            if (across + vertical * vertical > 1.0D) {
                                continue;
                            }
                            BlockPos local = point.floor.add(dx, dy, dz);
                            // The template already decides what its own footprint looks like. A cave slice
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

        // The shaman keeps the fire: it always stands right beside the fire rune, whatever the shuffle says.
        BlockPos shamanCell = this.layout.cellBesideFireRune();
        List<BlockPos> reserved = new ArrayList<>();
        if (shamanCell != null) {
            reserved.add(shamanCell);
        }

        List<BlockPos> nestCells = new ArrayList<>();
        for (BlockPos cell : candidates) {
            if (nestCells.size() >= nestTarget) {
                break;
            }
            if (this.layout.hasNestPatch(cell) && isFarFrom(cell, nestCells, 5) && isFarFrom(cell, reserved, 4)) {
                nestCells.add(cell);
            }
        }

        // GARRISON[0] is the shaman; the rest are picked from the shuffled cells as before.
        List<BlockPos> goblinCells = new ArrayList<>();
        if (shamanCell != null) {
            goblinCells.add(shamanCell);
        }
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

    /** One slice of a cave: a blob of the given radius and height standing on a template-local floor cell. */
    private static final class CavePoint {
        final BlockPos floor;
        final float radius;
        final int height;

        CavePoint(BlockPos floor, float radius, int height) {
            this.floor = floor;
            this.radius = radius;
            this.height = height;
        }

        /** Whole blocks the slice reaches sideways from its floor cell. */
        int reach() {
            return MathHelper.ceil(this.radius);
        }
    }

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
        /** The fire rune block the garrison's shaman is stationed beside; null if the template has none. */
        BlockPos fireRune;

        private Layout(int sizeX, int sizeZ) {
            this.sizeX = sizeX;
            this.sizeZ = sizeZ;
        }

        static Layout read(Template template) {
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

            Layout layout = new Layout(sizeX, sizeZ);
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

        boolean isInsideFootprint(BlockPos cell) {
            return cell.getX() >= 0 && cell.getX() < sizeX && cell.getZ() >= 0 && cell.getZ() < sizeZ;
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
