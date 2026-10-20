package com.windanesz.tracesofthefallen.world.structure;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponentTemplate;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One room of a goblin warren: an NBT template (a stone shell with the room hollowed out of it) that knows what
 * the room is for. The role decides who lives there. Placed one population region at a time, so everything is
 * deterministic from the piece's seed and only touches blocks inside the region it is handed.
 */
public class ComponentGoblinWarrenRoom extends StructureComponentTemplate {
    public enum Role {
        /** The hub: nests, the fire and its shaman. */
        HALL,
        /** The diggers' camp between the hall and the works: a tunneler among spoil heaps. */
        BORE,
        /** The armoury: an engineer and sappers among bomb racks. */
        WORKS,
        /** The reward: a brute guarding the chest and the idol. */
        VAULT,
        /** A side pocket of minecrawlers and their goblin riders. */
        DEN
    }

    private static final Map<String, WarrenLayout> LAYOUTS = new ConcurrentHashMap<>();

    private Role role;
    private String templateName;
    private Rotation rotation;
    private Mirror mirror;
    private long seed;
    private final Set<Long> generatedSections = new HashSet<>();

    // Rebuilt from the template whenever the piece is created or loaded; never saved.
    private WarrenLayout layout;

    // Required by MapGenStructureIO when loading existing structures.
    public ComponentGoblinWarrenRoom() {
    }

    public ComponentGoblinWarrenRoom(TemplateManager templateManager, Role role, String templateName, BlockPos templatePosition,
                                     Rotation rotation, Mirror mirror, long seed) {
        super(0);
        this.role = role;
        this.templateName = templateName;
        this.templatePosition = templatePosition;
        this.rotation = rotation;
        this.mirror = mirror;
        this.seed = seed;
        this.loadTemplate(templateManager);
    }

    /** What a template offers, whatever way up it is placed. Cached: the same file always gives the same answer. */
    static WarrenLayout layoutOf(TemplateManager templateManager, String templateName) {
        return LAYOUTS.computeIfAbsent(templateName, name ->
                WarrenLayout.read(templateManager.getTemplate(null, new ResourceLocation(TracesOfTheFallen.MODID, name))));
    }

    private void loadTemplate(TemplateManager templateManager) {
        Template template = templateManager.getTemplate(null, new ResourceLocation(TracesOfTheFallen.MODID, this.templateName));
        this.setup(template, this.templatePosition, new PlacementSettings().setRotation(this.rotation).setMirror(this.mirror));
        this.layout = layoutOf(templateManager, this.templateName);

        // Vanilla's template bounding box is one block too wide in X and Z, so build the exact one.
        BlockPos size = template.getSize();
        BlockPos near = toWorld(BlockPos.ORIGIN);
        BlockPos far = toWorld(new BlockPos(size.getX() - 1, size.getY() - 1, size.getZ() - 1));
        this.boundingBox = new StructureBoundingBox(
                Math.min(near.getX(), far.getX()), Math.min(near.getY(), far.getY()), Math.min(near.getZ(), far.getZ()),
                Math.max(near.getX(), far.getX()), Math.max(near.getY(), far.getY()), Math.max(near.getZ(), far.getZ()));
    }

    public Role getRole() {
        return this.role;
    }

    public int exitCount() {
        return this.layout.exits.size();
    }

    /** The doorway mouth of exit {@code index} in the world: the floor cell just outside the wall. */
    public BlockPos worldMouth(int index) {
        return toWorld(this.layout.exits.get(index).mouth);
    }

    /** The way exit {@code index} faces in the world, whatever rotation and mirror the room was placed with. */
    public EnumFacing worldOutward(int index) {
        return outwardOf(this.layout.exits.get(index), this.placeSettings);
    }

    static EnumFacing outwardOf(WarrenLayout.Exit exit, PlacementSettings settings) {
        BlockPos outside = Template.transformedBlockPos(settings, exit.mouth);
        BlockPos inside = Template.transformedBlockPos(settings, exit.mouth.offset(exit.outward.getOpposite()));
        return EnumFacing.getFacingFromVector(outside.getX() - inside.getX(), 0.0F, outside.getZ() - inside.getZ());
    }

    private BlockPos toWorld(BlockPos local) {
        return Template.transformedBlockPos(this.placeSettings, local).add(this.templatePosition);
    }

    @Override
    protected void writeStructureToNBT(NBTTagCompound tagCompound) {
        super.writeStructureToNBT(tagCompound);
        tagCompound.setString("Role", this.role.name());
        tagCompound.setString("Template", this.templateName);
        tagCompound.setString("Rot", this.rotation.name());
        tagCompound.setString("Mirror", this.mirror.name());
        tagCompound.setLong("RoomSeed", this.seed);

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
        this.role = Role.valueOf(tagCompound.getString("Role"));
        this.templateName = tagCompound.getString("Template");
        this.rotation = Rotation.valueOf(tagCompound.getString("Rot"));
        this.mirror = Mirror.valueOf(tagCompound.getString("Mirror"));
        this.seed = tagCompound.getLong("RoomSeed");

        this.generatedSections.clear();
        NBTTagList sections = tagCompound.getTagList("GeneratedSections", 10);
        for (int index = 0; index < sections.tagCount(); index++) {
            this.generatedSections.add(sections.getCompoundTagAt(index).getLong("Pos"));
        }

        this.loadTemplate(templateManager);
    }

    @Override
    public boolean addComponentParts(World world, Random random, StructureBoundingBox region) {
        long section = ChunkPos.asLong(region.minX >> 4, region.minZ >> 4);
        if (this.generatedSections.contains(section)) {
            return true;
        }

        super.addComponentParts(world, random, region);
        populate(world, region);

        this.generatedSections.add(section);
        return true;
    }

    @Override
    protected void handleDataMarker(String function, BlockPos pos, World worldIn, Random rand, StructureBoundingBox sbb) {
        // The room templates carry no data markers. Never leave a stray structure block behind if one is added.
        worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
    }

    // ---------------------------------------------------------------------------------------------------
    // Residents
    // ---------------------------------------------------------------------------------------------------

    /**
     * Every placement is chosen from the seed alone, so each population region reproduces the same choices
     * and spawns only the ones that fall inside it.
     */
    private void populate(World world, StructureBoundingBox region) {
        Random random = new Random(this.seed ^ 0x9E3779B97F4A7C15L);
        List<BlockPos> candidates = new ArrayList<>(this.layout.standCells);
        Collections.shuffle(candidates, random);

        List<BlockPos> taken = new ArrayList<>();
        switch (this.role) {
            case HALL:
                populateHall(world, region, random, candidates, taken);
                break;
            case BORE:
                placeResidents(world, region, random, candidates, taken, "goblin_tunneler", 1);
                placeResidents(world, region, random, candidates, taken, "goblin", 1 + random.nextInt(2));
                break;
            case WORKS:
                placeResidents(world, region, random, candidates, taken, "goblin_engineer", 1);
                placeResidents(world, region, random, candidates, taken, "goblin_sapper", 1 + random.nextInt(2));
                break;
            case VAULT:
                placeResidents(world, region, random, candidates, taken, "goblin_brute", 1);
                placeResidents(world, region, random, candidates, taken, "goblin_warrior", 1);
                break;
            case DEN:
                placeResidents(world, region, random, candidates, taken, "minecrawler", 2);
                placeResidents(world, region, random, candidates, taken, "goblin", 1 + random.nextInt(2));
                break;
            default:
                break;
        }
    }

    private void populateHall(World world, StructureBoundingBox region, Random random, List<BlockPos> candidates, List<BlockPos> taken) {
        Settings.WorldgenSettings settings = Settings.worldgenSettings;
        int nestTarget = WarrenSpawner.randomBetween(random, settings.goblinWarrenMinNests, settings.goblinWarrenMaxNests);

        // The shaman keeps the fire: it always stands right beside the fire rune, whatever the shuffle says.
        BlockPos shamanCell = this.layout.cellBesideFireRune();
        if (shamanCell != null) {
            taken.add(shamanCell);
        }

        List<BlockPos> nestCells = new ArrayList<>();
        for (BlockPos cell : candidates) {
            if (nestCells.size() >= nestTarget) {
                break;
            }
            if (this.layout.hasNestPatch(cell) && WarrenSpawner.isFarFrom(cell, nestCells, 5) && WarrenSpawner.isFarFrom(cell, taken, 4)) {
                nestCells.add(cell);
            }
        }
        for (BlockPos cell : nestCells) {
            spawnNest(world, region, cell);
        }

        if (shamanCell != null) {
            WarrenSpawner.spawnMob(world, region, toWorld(shamanCell), "goblin_shaman", random.nextFloat() * 360.0F);
        }
        // Keep the nests' ground clear of the rest of the garrison.
        taken.addAll(nestCells);
        placeResidents(world, region, random, candidates, taken, "goblin_warrior", 1 + random.nextInt(2));
    }

    /** Puts {@code count} of one kind on cells that are clear of every resident already placed. */
    private void placeResidents(World world, StructureBoundingBox region, Random random, List<BlockPos> candidates,
                                List<BlockPos> taken, String type, int count) {
        List<BlockPos> chosen = new ArrayList<>();
        for (BlockPos cell : candidates) {
            if (chosen.size() >= count) {
                break;
            }
            if (WarrenSpawner.isFarFrom(cell, taken, 3) && WarrenSpawner.isFarFrom(cell, chosen, 2)) {
                chosen.add(cell);
            }
        }
        taken.addAll(chosen);
        for (BlockPos cell : chosen) {
            // Rolled before the region check so the sequence is the same in every region.
            float yaw = random.nextFloat() * 360.0F;
            WarrenSpawner.spawnMob(world, region, toWorld(cell), type, yaw);
        }
    }

    /** The nest sits on the shared corner of a 2x2 patch whose min corner is localCell, facing the room's middle. */
    private void spawnNest(World world, StructureBoundingBox region, BlockPos localCell) {
        BlockPos near = toWorld(localCell);
        BlockPos far = toWorld(localCell.add(1, 0, 1));
        double deltaX = (this.boundingBox.minX + this.boundingBox.maxX) / 2.0D - (near.getX() + far.getX()) / 2.0D;
        double deltaZ = (this.boundingBox.minZ + this.boundingBox.maxZ) / 2.0D - (near.getZ() + far.getZ()) / 2.0D;
        EnumFacing facing = Math.abs(deltaX) > Math.abs(deltaZ)
                ? (deltaX > 0.0D ? EnumFacing.EAST : EnumFacing.WEST)
                : (deltaZ > 0.0D ? EnumFacing.SOUTH : EnumFacing.NORTH);
        WarrenSpawner.spawnNest(world, region, near, far, facing);
    }
}
