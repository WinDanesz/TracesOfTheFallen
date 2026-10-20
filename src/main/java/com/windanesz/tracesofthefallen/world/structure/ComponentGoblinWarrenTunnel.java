package com.windanesz.tracesofthefallen.world.structure;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.template.TemplateManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;

/**
 * A tunnel of the goblin warren: either a connector between two rooms, or an approach, the long way in from the
 * outside. Placed one population region at a time like the rooms, from nothing but the numbers saved here.
 */
public class ComponentGoblinWarrenTunnel extends StructureComponent {
    public enum Kind {
        CONNECTOR,
        APPROACH
    }

    private static final int APPROACH_MAX_DRIFT = 6;

    private Kind kind;
    private BlockPos from;
    /** Connector only: the doorway mouth the tunnel must arrive at. */
    private BlockPos to;
    /** Approach only: the way out of the doorway, and how many slices the tunnel runs for. */
    private EnumFacing outward;
    private int length;
    private int highY;
    private long seed;
    private final List<StructureBoundingBox> keepOut = new ArrayList<>();
    private final Set<Long> generatedSections = new HashSet<>();

    // Rebuilt whenever the piece is created or loaded; never saved.
    private List<WarrenCave.Point> path;

    // Required by MapGenStructureIO when loading existing structures.
    public ComponentGoblinWarrenTunnel() {
    }

    private ComponentGoblinWarrenTunnel(Kind kind, BlockPos from, BlockPos to, EnumFacing outward, int length, int highY, long seed,
                                        List<StructureBoundingBox> keepOut) {
        super(0);
        this.kind = kind;
        this.from = from;
        this.to = to;
        this.outward = outward;
        this.length = length;
        this.highY = highY;
        this.seed = seed;
        this.keepOut.addAll(keepOut);
        this.buildPath();
    }

    public static ComponentGoblinWarrenTunnel connector(BlockPos from, BlockPos to, long seed, List<StructureBoundingBox> keepOut) {
        return new ComponentGoblinWarrenTunnel(Kind.CONNECTOR, from, to, null, 0, 0, seed, keepOut);
    }

    public static ComponentGoblinWarrenTunnel approach(BlockPos from, EnumFacing outward, int length, int highY, long seed,
                                                       List<StructureBoundingBox> keepOut) {
        return new ComponentGoblinWarrenTunnel(Kind.APPROACH, from, null, outward, length, highY, seed, keepOut);
    }

    private void buildPath() {
        Random random = new Random(this.seed);
        if (this.kind == Kind.CONNECTOR) {
            this.path = WarrenCave.connector(this.from, this.to, random);
        } else {
            int lowY = WarrenCave.minFloorY(this.from.getY(), APPROACH_MAX_DRIFT);
            this.path = WarrenCave.approach(this.from, this.outward, this.length, lowY, Math.max(this.highY, this.from.getY()), random);
        }
        this.boundingBox = WarrenCave.bounds(this.path);
    }

    @Override
    protected void writeStructureToNBT(NBTTagCompound tag) {
        tag.setString("Kind", this.kind.name());
        tag.setInteger("FromX", this.from.getX());
        tag.setInteger("FromY", this.from.getY());
        tag.setInteger("FromZ", this.from.getZ());
        if (this.to != null) {
            tag.setInteger("ToX", this.to.getX());
            tag.setInteger("ToY", this.to.getY());
            tag.setInteger("ToZ", this.to.getZ());
        }
        if (this.outward != null) {
            tag.setString("Outward", this.outward.getName());
        }
        tag.setInteger("Length", this.length);
        tag.setInteger("HighY", this.highY);
        tag.setLong("TunnelSeed", this.seed);

        NBTTagList keep = new NBTTagList();
        for (StructureBoundingBox box : this.keepOut) {
            keep.appendTag(box.toNBTTagIntArray());
        }
        tag.setTag("KeepOut", keep);

        NBTTagList sections = new NBTTagList();
        for (long key : this.generatedSections) {
            NBTTagCompound section = new NBTTagCompound();
            section.setLong("Pos", key);
            sections.appendTag(section);
        }
        tag.setTag("GeneratedSections", sections);
    }

    @Override
    protected void readStructureFromNBT(NBTTagCompound tag, TemplateManager templateManager) {
        this.kind = Kind.valueOf(tag.getString("Kind"));
        this.from = new BlockPos(tag.getInteger("FromX"), tag.getInteger("FromY"), tag.getInteger("FromZ"));
        this.to = tag.hasKey("ToX") ? new BlockPos(tag.getInteger("ToX"), tag.getInteger("ToY"), tag.getInteger("ToZ")) : null;
        this.outward = tag.hasKey("Outward") ? EnumFacing.byName(tag.getString("Outward")) : null;
        this.length = tag.getInteger("Length");
        this.highY = tag.getInteger("HighY");
        this.seed = tag.getLong("TunnelSeed");

        this.keepOut.clear();
        NBTTagList keep = tag.getTagList("KeepOut", 11);
        for (int index = 0; index < keep.tagCount(); index++) {
            this.keepOut.add(new StructureBoundingBox(keep.getIntArrayAt(index)));
        }
        this.generatedSections.clear();
        NBTTagList sections = tag.getTagList("GeneratedSections", 10);
        for (int index = 0; index < sections.tagCount(); index++) {
            this.generatedSections.add(sections.getCompoundTagAt(index).getLong("Pos"));
        }
        this.buildPath();
    }

    @Override
    public boolean addComponentParts(World world, Random random, StructureBoundingBox region) {
        long section = ChunkPos.asLong(region.minX >> 4, region.minZ >> 4);
        if (this.generatedSections.contains(section)) {
            return true;
        }

        WarrenCave.carve(world, region, this.path, this.keepOut);
        Random props = new Random(this.seed ^ 0x51AFD7ED558CCD5BL);
        WarrenCave.decorate(world, region, this.path, props, this.kind == Kind.APPROACH ? 5 : 4, 7);
        if (this.kind == Kind.APPROACH) {
            populate(world, region);
        }

        this.generatedSections.add(section);
        return true;
    }

    /**
     * An approach is watched: a trapper lies in wait part-way along it, a starved goblin prowls deeper in, and
     * jaw traps are set along the way. Chosen from the seed alone, so each region spawns only what falls in it.
     */
    private void populate(World world, StructureBoundingBox region) {
        Random random = new Random(this.seed ^ 0x9E3779B97F4A7C15L);
        int last = this.path.size() - 1;
        int trapper = Math.min(last, Math.max(8, this.path.size() * 3 / 5));
        int starved = Math.min(last, Math.max(trapper + 3, this.path.size() * 4 / 5));

        List<Integer> trapIndexes = new ArrayList<>();
        for (int index = 8; index < last - 1; index += 7 + random.nextInt(4)) {
            if (Math.abs(index - trapper) > 2) {
                trapIndexes.add(index);
            }
        }
        float trapperYaw = random.nextFloat() * 360.0F;
        float starvedYaw = random.nextFloat() * 360.0F;

        WarrenSpawner.spawnMob(world, region, this.path.get(trapper).floor, "goblin_trapper", trapperYaw);
        WarrenSpawner.spawnMob(world, region, this.path.get(starved).floor, "goblin_starved", starvedYaw);
        for (int index : trapIndexes) {
            WarrenSpawner.spawnJawTrap(world, region, this.path.get(index).floor);
        }
    }
}
