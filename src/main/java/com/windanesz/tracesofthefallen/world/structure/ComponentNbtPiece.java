package com.windanesz.tracesofthefallen.world.structure;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.block.*;
import com.windanesz.tracesofthefallen.entity.living.EntityGnossic;
import com.windanesz.tracesofthefallen.entity.living.EntityGoblinNest;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import com.windanesz.tracesofthefallen.tile.TileEntitySkeletonCorpse;
import com.windanesz.tracesofthefallen.tile.TileEntityStoneChest;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureComponentTemplate;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;

import java.util.*;

public class ComponentNbtPiece extends StructureComponentTemplate {
    private String templateName;
    private Rotation rotation;
    private final Set<BlockPos> closedDoorways = new HashSet<>();
    private boolean trapGenerated = false;
    private boolean gnossicSpawnRollMade = false;
    private boolean gnossicSpawnPending = false;

    private static final String[] HALLS = {"x11_5_hall", "x11_5_cross", "x11_5_t-junction", "x11_5_corner"};
    private static final String[] ROOMS = {"x21_7_hall_factory", "x21_9_cross_claustrophobic", "x21_9_cross_pots", "x21_10_cross_moon", "x11_9_goblin_camp", "x21_10_goblin_infestation"};
    private static final Set<String> GNOSSIC_ROOMS = new HashSet<>(Arrays.asList(ROOMS));
    private static final String MOON_ROOM = "x21_10_cross_moon";
    private static final String GOBLIN_INFESTATION = "x21_10_goblin_infestation";
    private static final String[] ROOMS_WITHOUT_MOON = Arrays.stream(ROOMS)
            .filter(room -> !room.equals(MOON_ROOM))
            .toArray(String[]::new);
    private static final String[] DEAD_ENDS = {"x11_5_deadend", "x11_5_deadend_library", "x11_5_deadend_collapsed"};

    private static final Set<String> CORRIDOR_TRAP_TEMPLATES = new HashSet<>(Arrays.asList(
            "x11_5_hall", "x21_7_hall_factory"
    ));

    // Rooms whose baked-in stone chests always get the rich loot table; every other room's chests are usually common.
    private static final Set<String> RICH_LOOT_ROOMS = new HashSet<>(Arrays.asList(
            "x21_10_origin_teleporter_1", "x21_9_cross_pots"
    ));
    private static final Set<String> BAKED_CHEST_ROOMS = new HashSet<>(Arrays.asList(
            "x21_10_origin_teleporter_1", "x21_9_cross_pots", "x21_10_cross_moon", "x21_7_hall_factory", "x21_9_cross_claustrophobic"
    ));
    private static final int RICH_LOOT_CHANCE_DENOMINATOR = 6;

    private static final int MAX_DEPTH = 20;
    private static final int MAX_PIECES = 20;

    public ComponentNbtPiece() {
    }

    public ComponentNbtPiece(TemplateManager templateManager, String templateName, BlockPos templatePosition, Rotation rotation, int componentType) {
        super(componentType);
        this.templateName = templateName;
        this.templatePosition = templatePosition;
        this.rotation = rotation;
        this.componentType = componentType;
        this.loadTemplate(templateManager);
    }

    private void loadTemplate(TemplateManager templateManager) {
        Template template = templateManager.getTemplate(null, new ResourceLocation("totf", templateName));
        PlacementSettings placementsettings = (new PlacementSettings()).setRotation(this.rotation);
        this.setup(template, this.templatePosition, placementsettings);
        
        BlockPos size = template.getSize();
        int sizeX = size.getX();
        int sizeZ = size.getZ();
        
        if (this.rotation == Rotation.CLOCKWISE_90 || this.rotation == Rotation.COUNTERCLOCKWISE_90) {
            sizeX = size.getZ();
            sizeZ = size.getX();
        }
        
        int minX = this.templatePosition.getX();
        int minZ = this.templatePosition.getZ();
        
        if (this.rotation == Rotation.CLOCKWISE_90) {
            minX -= sizeX - 1;
        } else if (this.rotation == Rotation.CLOCKWISE_180) {
            minX -= sizeX - 1;
            minZ -= sizeZ - 1;
        } else if (this.rotation == Rotation.COUNTERCLOCKWISE_90) {
            minZ -= sizeZ - 1;
        }
        
        this.boundingBox = new StructureBoundingBox(
            minX,
            this.templatePosition.getY(),
            minZ,
            minX + sizeX - 1,
            this.templatePosition.getY() + size.getY() - 1,
            minZ + sizeZ - 1
        );
    }

    @Override
    protected void writeStructureToNBT(NBTTagCompound tagCompound) {
        super.writeStructureToNBT(tagCompound);
        tagCompound.setString("Template", this.templateName);
        tagCompound.setString("Rot", this.rotation.name());

        NBTTagList closedDoorwayTags = new NBTTagList();
        for (BlockPos doorway : this.closedDoorways) {
            NBTTagCompound doorwayTag = new NBTTagCompound();
            doorwayTag.setInteger("X", doorway.getX());
            doorwayTag.setInteger("Y", doorway.getY());
            doorwayTag.setInteger("Z", doorway.getZ());
            closedDoorwayTags.appendTag(doorwayTag);
        }
        tagCompound.setTag("ClosedDoorways", closedDoorwayTags);
        tagCompound.setBoolean("TrapGenerated", this.trapGenerated);
        tagCompound.setBoolean("GnossicSpawnRollMade", this.gnossicSpawnRollMade);
        tagCompound.setBoolean("GnossicSpawnPending", this.gnossicSpawnPending);
    }

    @Override
    protected void readStructureFromNBT(NBTTagCompound tagCompound, TemplateManager templateManager) {
        super.readStructureFromNBT(tagCompound, templateManager);
        this.templateName = tagCompound.getString("Template");
        this.rotation = Rotation.valueOf(tagCompound.getString("Rot"));
        this.closedDoorways.clear();

        NBTTagList closedDoorwayTags = tagCompound.getTagList("ClosedDoorways", 10);
        for (int index = 0; index < closedDoorwayTags.tagCount(); index++) {
            NBTTagCompound doorwayTag = closedDoorwayTags.getCompoundTagAt(index);
            this.closedDoorways.add(new BlockPos(doorwayTag.getInteger("X"), doorwayTag.getInteger("Y"), doorwayTag.getInteger("Z")));
        }
        this.trapGenerated = tagCompound.getBoolean("TrapGenerated");
        this.gnossicSpawnRollMade = tagCompound.getBoolean("GnossicSpawnRollMade");
        this.gnossicSpawnPending = tagCompound.getBoolean("GnossicSpawnPending");

        this.loadTemplate(templateManager);
    }

    @Override
    public boolean addComponentParts(World worldIn, Random rand, StructureBoundingBox region) {
        boolean placed = super.addComponentParts(worldIn, rand, region);
        this.assignStoneChestLoot(worldIn, rand, region);
        this.maybeSpawnDungeonGnossic(worldIn, rand, region);
        if (this.templateName.equals(GOBLIN_INFESTATION)) {
            this.infestGoblinRoom(worldIn, rand, region);
        }
        return placed;
    }

    /**
     * Gives the stone chests baked into the room templates a loot table. Chests that already carry one
     * (e.g. the vault's) are left alone. Each chest lies in exactly one chunk's region, so it is rolled once.
     */
    private void assignStoneChestLoot(World worldIn, Random rand, StructureBoundingBox region) {
        if (!BAKED_CHEST_ROOMS.contains(this.templateName)) {
            return;
        }
        boolean richRoom = RICH_LOOT_ROOMS.contains(this.templateName);
        for (BlockPos pos : BlockPos.getAllInBox(
                Math.max(this.boundingBox.minX, region.minX), this.boundingBox.minY, Math.max(this.boundingBox.minZ, region.minZ),
                Math.min(this.boundingBox.maxX, region.maxX), this.boundingBox.maxY, Math.min(this.boundingBox.maxZ, region.maxZ))) {
            if (worldIn.getBlockState(pos).getBlock() != ModBlocks.stone_chest) {
                continue;
            }
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if (!(tileEntity instanceof TileEntityStoneChest) || tileEntity.writeToNBT(new NBTTagCompound()).hasKey("LootTable")) {
                continue;
            }
            boolean rich = richRoom || rand.nextInt(RICH_LOOT_CHANCE_DENOMINATOR) == 0;
            ((TileEntityStoneChest) tileEntity).setLootTable(rich ? TileEntityStoneChest.LOOT_RICH : TileEntityStoneChest.LOOT_COMMON, rand.nextLong());
        }
    }

    private void maybeSpawnDungeonGnossic(World worldIn, Random rand, StructureBoundingBox region) {
        if (!GNOSSIC_ROOMS.contains(this.templateName)) {
            return;
        }
        if (!this.gnossicSpawnRollMade) {
            this.gnossicSpawnRollMade = true;
            this.gnossicSpawnPending = rand.nextDouble() < Settings.worldgenSettings.dungeonGnossicSpawnChance;
        }
        if (!this.gnossicSpawnPending) {
            return;
        }

        int floorY = this.boundingBox.minY;
        for (int attempt = 0; attempt < 24; attempt++) {
            BlockPos floorPos = new BlockPos(
                    this.boundingBox.minX + 1 + rand.nextInt(Math.max(1, this.boundingBox.getXSize() - 2)),
                    floorY,
                    this.boundingBox.minZ + 1 + rand.nextInt(Math.max(1, this.boundingBox.getZSize() - 2)));
            if (!contains(region, floorPos)
                    || !worldIn.getBlockState(floorPos).isFullBlock()
                    || !worldIn.isAirBlock(floorPos.up())
                    || !worldIn.isAirBlock(floorPos.up(2))) {
                continue;
            }

            EntityGnossic gnossic = new EntityGnossic(worldIn);
            gnossic.setLocationAndAngles(floorPos.getX() + 0.5D, floorPos.getY() + 1, floorPos.getZ() + 0.5D, rand.nextFloat() * 360.0F, 0.0F);
            gnossic.enablePersistence();
            worldIn.spawnEntity(gnossic);
            this.gnossicSpawnPending = false;
            return;
        }
    }

    @Override
    protected void handleDataMarker(String function, BlockPos pos, World worldIn, Random rand, StructureBoundingBox sbb) {
        if (isDoorMarker(function)) {
            if (!this.trapGenerated) {
                this.trapGenerated = true;
                maybeGenerateTrap(worldIn, rand, pos);
            }

            IBlockState brickState = getBrickState();

            if (this.closedDoorways.contains(pos)) {
                closeDoorway(worldIn, pos, brickState);
            } else if (pos.getY() == this.templatePosition.getY()) {
                worldIn.setBlockState(pos, brickState, 3);
            } else {
                worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
            }
        } else if (function.equals("goblin_camp_decor")) {
            decorateGoblinCamp(worldIn, pos, rand, sbb);
            worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
        } else if (function.startsWith("spawn_")) {
            boolean shouldSpawn = true;
            if (function.equals("spawn_goblin_shaman") || function.equals("spawn_goblin_wayfarer_chance")) {
                shouldSpawn = rand.nextBoolean(); // 50% chance for 0-2 variance
            }
            
            if (shouldSpawn) {
                String entityName = function.replace("_chance", "").replace("_guaranteed", "").replace("spawn_", "");
                Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation("totf", entityName), worldIn);
                if (entity != null) {
                    entity.setLocationAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
                    if (entity instanceof EntityLiving) {
                        ((EntityLiving)entity).enablePersistence();
                    }
                    worldIn.spawnEntity(entity);
                }
            }
            worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
        } else if (function.equals("stone_chest_common") || function.equals("stone_chest_rich")) {
            // Template marker: places a lootable stone chest of the given tier.
            worldIn.setBlockState(pos, ModBlocks.stone_chest.getDefaultState()
                .withProperty(BlockStoneChest.FACING, EnumFacing.HORIZONTALS[rand.nextInt(EnumFacing.HORIZONTALS.length)]), 2);
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if (tileEntity instanceof TileEntityStoneChest) {
                ((TileEntityStoneChest) tileEntity).setLootTable(function.equals("stone_chest_rich") ? TileEntityStoneChest.LOOT_RICH : TileEntityStoneChest.LOOT_COMMON, rand.nextLong());
            }
        } else if (function.startsWith("chest_")) {
            worldIn.setBlockState(pos, Blocks.CHEST.getDefaultState(), 2);
            net.minecraft.tileentity.TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof net.minecraft.tileentity.TileEntityChest) {
                String lootTable = function.replace("chest_", "");
                ((net.minecraft.tileentity.TileEntityChest)tileentity).setLootTable(new ResourceLocation("minecraft", "chests/" + lootTable), rand.nextLong());
            }
        } else if (function.startsWith("skeleton_corpse_")) {
            placeSkeletonCorpse(worldIn, rand, pos, function.substring("skeleton_corpse_".length()));
        } else if (function.startsWith("gnossic_mask_chance_")) {
            placeGnossicMask(worldIn, rand, pos, function.substring("gnossic_mask_chance_".length()));
        }
    }

    /**
     * Hangs a totf:gnossic_mask on a wall with the configured {@code dungeonGnossicMaskChance}. The marker's suffix is the
     * template-local direction the mask faces (away from its wall), converted into world space via this
     * piece's rotation. The marker is always cleared, whether or not a mask is placed.
     */
    private void placeGnossicMask(World worldIn, Random rand, BlockPos pos, String variant) {
        worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
        EnumFacing localFacing = EnumFacing.byName(variant);
        if (localFacing == null || localFacing.getAxis() == EnumFacing.Axis.Y || rand.nextDouble() >= Settings.worldgenSettings.dungeonGnossicMaskChance) {
            return;
        }
        EnumFacing worldFacing = this.rotation.rotate(localFacing);
        worldIn.setBlockState(pos, ModBlocks.gnossic_mask.getDefaultState()
                .withProperty(BlockGnossicMask.FLOOR, false)
                .withProperty(BlockGnossicMask.FACING, worldFacing), 2);
    }

    /**
     * Places a totf:skeleton_corpse from a structure data marker. The marker's suffix is either
     * "ground" (lying flat, random fine rotation) or "sit_<north|south|east|west>" (leaning against
     * the wall on that template-local side, converted into world space via this piece's rotation).
     * The held-item loot pool is rolled once here and baked into the tile entity so it's deterministic
     * and matches what's rendered on the model - it is never re-rolled when the block is later broken.
     */
    private void placeSkeletonCorpse(World worldIn, Random rand, BlockPos pos, String variant) {
        if (!(ModBlocks.skeleton_corpse instanceof BlockSkeletonCorpse)) {
            return;
        }
        BlockSkeletonCorpse block = (BlockSkeletonCorpse) ModBlocks.skeleton_corpse;

        IBlockState state;
        int fineRotation = 0;
        if (variant.equals("ground")) {
            state = block.getDefaultState().withProperty(BlockSkeletonCorpse.POSE, BlockSkeletonCorpse.Pose.GROUND);
            fineRotation = rand.nextInt(16);
        } else if (variant.startsWith("sit_")) {
            EnumFacing localFacing = EnumFacing.byName(variant.substring("sit_".length()));
            if (localFacing == null) {
                return;
            }
            EnumFacing worldFacing = this.rotation.rotate(localFacing);
            state = block.getDefaultState().withProperty(BlockSkeletonCorpse.POSE, BlockSkeletonCorpse.Pose.SIT).withProperty(BlockSkeletonCorpse.FACING, worldFacing);
        } else {
            return;
        }

        worldIn.setBlockState(pos, state, 2);
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntitySkeletonCorpse) {
            TileEntitySkeletonCorpse corpse = (TileEntitySkeletonCorpse) te;
            corpse.setRotation(fineRotation);
            corpse.setHeldItem(block.rollHeldItem(worldIn, rand));
        }
    }

    private static boolean isDoorMarker(String function) {
        return function.isEmpty() || function.equals("door");
    }

    private static IBlockState getBrickState() {
        net.minecraft.block.Block brickBlock = net.minecraft.block.Block.getBlockFromName("totf:bricks_stone");
        return (brickBlock == null ? Blocks.STONEBRICK : brickBlock).getDefaultState();
    }

    private static final int TRAP_SEARCH_ATTEMPTS = 24;
    private static final int GUILLOTINE_ROW_SIZE = 3;

    private void maybeGenerateTrap(World worldIn, Random rand, BlockPos markerPos) {
        if (!CORRIDOR_TRAP_TEMPLATES.contains(this.templateName)) {
            return;
        }
        if (rand.nextDouble() >= Settings.worldgenSettings.trapRoomChance) {
            return;
        }

        generateGuillotineTrap(worldIn, rand, markerPos);
    }

    /**
     * Places a row of pressure-plate/guillotine traps spanning the corridor's width, so the player can't
     * dodge sideways past them. Each plate replaces the floor block directly beneath its own ceiling-mounted
     * guillotine, and the ceiling block the guillotine attaches to is itself the redstone receptor.
     */
    private void generateGuillotineTrap(World worldIn, Random rand, BlockPos markerPos) {
        boolean travelAlongX = markerPos.getX() == this.boundingBox.minX || markerPos.getX() == this.boundingBox.maxX;
        EnumFacing widthDir = travelAlongX ? EnumFacing.SOUTH : EnumFacing.EAST;
        EnumFacing bladeFacing = travelAlongX ? EnumFacing.EAST : EnumFacing.NORTH;

        int floorY = this.boundingBox.minY;
        int ceilingY = this.boundingBox.maxY - 1;
        int rowHalf = GUILLOTINE_ROW_SIZE / 2;

        for (int attempt = 0; attempt < TRAP_SEARCH_ATTEMPTS; attempt++) {
            BlockPos center;
            if (travelAlongX) {
                int travelCoord = this.boundingBox.minX + 2 + rand.nextInt(Math.max(1, this.boundingBox.getXSize() - 4));
                int widthCoord = (this.boundingBox.minZ + this.boundingBox.maxZ) / 2;
                center = new BlockPos(travelCoord, floorY, widthCoord);
            } else {
                int travelCoord = this.boundingBox.minZ + 2 + rand.nextInt(Math.max(1, this.boundingBox.getZSize() - 4));
                int widthCoord = (this.boundingBox.minX + this.boundingBox.maxX) / 2;
                center = new BlockPos(widthCoord, floorY, travelCoord);
            }

            int placedCount = 0;
            for (int offset = -rowHalf; offset <= rowHalf; offset++) {
                if (placeGuillotineOverPlate(worldIn, center.offset(widthDir, offset), ceilingY, bladeFacing)) {
                    placedCount++;
                }
            }

            if (placedCount > 0) {
                return;
            }
        }
    }

    private boolean placeGuillotineOverPlate(World worldIn, BlockPos floorPos, int ceilingY, EnumFacing bladeFacing) {
        BlockPos guillotinePos = new BlockPos(floorPos.getX(), ceilingY, floorPos.getZ());
        BlockPos receiverPos = guillotinePos.up();

        // The pressure plate is a disguised floor tile: it must replace an existing solid floor block, not float above it.
        if (!contains(this.boundingBox, floorPos)
                || !worldIn.getBlockState(floorPos).isFullBlock()
                || !worldIn.isAirBlock(floorPos.up())
                || !worldIn.isAirBlock(guillotinePos)
                || !worldIn.getBlockState(receiverPos).isFullBlock()) {
            return false;
        }

        worldIn.setBlockState(guillotinePos, ModBlocks.guillotine.getDefaultState()
                .withProperty(BlockGuillotine.FACE, BlockGuillotine.EnumAttachFace.CEILING)
                .withProperty(BlockGuillotine.FACING, bladeFacing), 3);

        // The ceiling block the guillotine hangs from is itself the redstone receptor.
        worldIn.setBlockState(receiverPos, ModBlocks.stone_receiver.getDefaultState(), 3);

        worldIn.setBlockState(floorPos, ModBlocks.stone_pressure_plate.getDefaultState()
                .withProperty(BlockStonePressurePlate.FACING, EnumFacing.UP), 3);
        return true;
    }

    private void closeDoorway(World worldIn, BlockPos doorwayPos, IBlockState brickState) {
        boolean northOrSouthDoorway = doorwayPos.getZ() == this.boundingBox.minZ || doorwayPos.getZ() == this.boundingBox.maxZ;

        for (int widthOffset = -1; widthOffset <= 1; widthOffset++) {
            for (int y = this.boundingBox.minY; y <= this.boundingBox.maxY; y++) {
                BlockPos closedPos = northOrSouthDoorway
                    ? new BlockPos(doorwayPos.getX() + widthOffset, y, doorwayPos.getZ())
                    : new BlockPos(doorwayPos.getX(), y, doorwayPos.getZ() + widthOffset);
                worldIn.setBlockState(closedPos, brickState, 3);
            }
        }
    }

    // A camp's inhabitants besides the guaranteed wayfarer and the 50% shaman; one or two of these.
    private static final String[] CAMP_GUARDS = {"goblin_warrior", "goblin_trapper"};

    /**
     * The camp templates carry no spawn markers of their own (only a single decor marker), so the camp is
     * populated from here. Only positions inside {@code region} are used: this runs once per population
     * region, and an entity placed in a neighbouring chunk that isn't loaded yet is silently dropped.
     */
    private void spawnGoblinCampInhabitants(World worldIn, BlockPos markerPos, List<BlockPos> occupiedPositions, StructureBoundingBox region, Random rand) {
        List<String> inhabitants = new ArrayList<>();
        inhabitants.add("goblin_wayfarer");
        if (rand.nextBoolean()) {
            inhabitants.add("goblin_shaman");
        }
        int guards = 1 + rand.nextInt(2);
        for (int index = 0; index < guards; index++) {
            inhabitants.add(CAMP_GUARDS[rand.nextInt(CAMP_GUARDS.length)]);
        }

        for (String name : inhabitants) {
            BlockPos spot = findPositionInRegion(worldIn, markerPos, occupiedPositions, 2, false, region, rand);
            if (spot == null) {
                // The marker itself is always inside the region and was left clear for this.
                spot = markerPos;
            }
            Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation("totf", name), worldIn);
            if (!(entity instanceof EntityLiving)) {
                continue;
            }
            entity.setLocationAndAngles(spot.getX() + 0.5D, spot.getY(), spot.getZ() + 0.5D, rand.nextFloat() * 360.0F, 0.0F);
            if (!worldIn.getCollisionBoxes(entity, entity.getEntityBoundingBox()).isEmpty()) {
                continue;
            }
            ((EntityLiving) entity).enablePersistence();
            worldIn.spawnEntity(entity);
            occupiedPositions.add(spot);
        }
    }

    /** Like findClearDecorationPosition, but only returns a spot inside the region currently being placed. */
    private BlockPos findPositionInRegion(World worldIn, BlockPos markerPos, List<BlockPos> occupiedPositions, int minimumSeparation, boolean requireNestClearance, StructureBoundingBox region, Random rand) {
        return findClearDecorationPosition(worldIn, markerPos, occupiedPositions, minimumSeparation, requireNestClearance, region, rand);
    }

    private void placeGoblinNests(World worldIn, BlockPos markerPos, List<BlockPos> occupiedPositions, StructureBoundingBox region, int nestCount, Random rand) {
        for (int index = 0; index < nestCount; index++) {
            BlockPos nestPos = findPositionInRegion(worldIn, markerPos, occupiedPositions, 4, true, region, rand);
            if (nestPos == null) {
                continue;
            }

            EntityGoblinNest nest = new EntityGoblinNest(worldIn, nestPos.getX() + 0.5D, nestPos.getY(), nestPos.getZ() + 0.5D);
            nest.setFacing(getFacingTowardRoomCenter(nestPos));
            worldIn.spawnEntity(nest);
            occupiedPositions.add(nestPos);
        }
    }

    /**
     * The infestation template is hand-built (bonepiles and all) and carries no decor or spawn markers, so its
     * nests and goblins are added from here. Runs for the one population region that contains the room's centre.
     */
    private void infestGoblinRoom(World worldIn, Random rand, StructureBoundingBox region) {
        BlockPos centerPos = new BlockPos((this.boundingBox.minX + this.boundingBox.maxX) / 2, this.boundingBox.minY + 1, (this.boundingBox.minZ + this.boundingBox.maxZ) / 2);
        if (!region.isVecInside(centerPos)) {
            return;
        }

        List<BlockPos> occupiedPositions = new ArrayList<>();
        placeGoblinNests(worldIn, centerPos, occupiedPositions, region, 2 + rand.nextInt(3), rand);
        spawnGoblinCampInhabitants(worldIn, centerPos, occupiedPositions, region, rand);
    }

    private void decorateGoblinCamp(World worldIn, BlockPos markerPos, Random rand, StructureBoundingBox region) {
        List<BlockPos> occupiedPositions = new ArrayList<>();
        // The data marker block is still in the world while it is being handled; clear it so it is neither
        // solid to the inhabitants spawned on it nor an invalid spot for decoration.
        worldIn.setBlockState(markerPos, Blocks.AIR.getDefaultState(), 3);
        weatherGoblinCampFloor(worldIn, markerPos.down(), rand);

        if (rand.nextBoolean()) {
            placeCollapsedArea(worldIn, markerPos, occupiedPositions, rand);
        }

        int torchCount = 1 + rand.nextInt(3);
        for (int index = 0; index < torchCount; index++) {
            BlockPos torchPos = findWallDecorationPosition(worldIn, markerPos, occupiedPositions, 3, rand);
            if (torchPos == null) {
                continue;
            }

            worldIn.setBlockState(torchPos, Blocks.OAK_FENCE.getDefaultState(), 3);
            worldIn.setBlockState(torchPos.up(), Blocks.TORCH.getDefaultState(), 3);
            occupiedPositions.add(torchPos);
        }

        placeGoblinNests(worldIn, markerPos, occupiedPositions, region, 2 + rand.nextInt(3), rand);

        int bonepileClusterCount = 2 + rand.nextInt(2);
        for (int index = 0; index < bonepileClusterCount; index++) {
            BlockPos bonepileCenter = findClearDecorationPosition(worldIn, markerPos, occupiedPositions, 3, false, rand);
            if (bonepileCenter == null) {
                continue;
            }

            placeBonepileCluster(worldIn, bonepileCenter, occupiedPositions, rand);
        }

        int pileCount = 5 + rand.nextInt(4);
        for (int index = 0; index < pileCount; index++) {
            BlockPos pilePos = findStonePilePosition(worldIn, markerPos, occupiedPositions, rand);
            if (pilePos == null) {
                continue;
            }

            placeStonePile(worldIn, pilePos, rand);
            occupiedPositions.add(pilePos);
        }

        if (rand.nextInt(4) == 0) {
            int cobwebCount = 1 + rand.nextInt(2);
            for (int index = 0; index < cobwebCount; index++) {
                BlockPos cobwebPos = findClearDecorationPosition(worldIn, markerPos, occupiedPositions, 2, false, rand);
                if (cobwebPos != null) {
                    worldIn.setBlockState(cobwebPos, Blocks.WEB.getDefaultState(), 3);
                    occupiedPositions.add(cobwebPos);
                }
            }
        }

        if (rand.nextInt(5) == 0) {
            BlockPos chestPos = findClearDecorationPosition(worldIn, markerPos, occupiedPositions, 2, false, rand);
            if (chestPos != null) {
                boolean open = rand.nextBoolean();
                IBlockState chestState = ModBlocks.stone_chest.getDefaultState()
                    .withProperty(BlockStoneChest.FACING, EnumFacing.HORIZONTALS[rand.nextInt(EnumFacing.HORIZONTALS.length)])
                    .withProperty(BlockStoneChest.OPEN, open);
                worldIn.setBlockState(chestPos, chestState, 3);

                TileEntity tileEntity = worldIn.getTileEntity(chestPos);
                if (tileEntity instanceof TileEntityStoneChest) {
                    TileEntityStoneChest stoneChest = (TileEntityStoneChest) tileEntity;
                    stoneChest.setLidOpen(open);
                    stoneChest.setLootTable(rand.nextInt(4) == 0 ? TileEntityStoneChest.LOOT_RICH : TileEntityStoneChest.LOOT_COMMON, rand.nextLong());
                }
            }
        }

        // The larger camp template places its own goblins through spawn_ markers; the small one has none.
        if (!this.templateName.equals("x21_9_goblin_camp")) {
            spawnGoblinCampInhabitants(worldIn, markerPos, occupiedPositions, region, rand);
        }
    }

    private EnumFacing getFacingTowardRoomCenter(BlockPos pos) {
        double centerX = (this.boundingBox.minX + this.boundingBox.maxX + 1) / 2.0D;
        double centerZ = (this.boundingBox.minZ + this.boundingBox.maxZ + 1) / 2.0D;
        double deltaX = centerX - (pos.getX() + 0.5D);
        double deltaZ = centerZ - (pos.getZ() + 0.5D);

        if (Math.abs(deltaX) > Math.abs(deltaZ)) {
            return deltaX > 0.0D ? EnumFacing.EAST : EnumFacing.WEST;
        }
        return deltaZ > 0.0D ? EnumFacing.SOUTH : EnumFacing.NORTH;
    }

    private void placeCollapsedArea(World worldIn, BlockPos markerPos, List<BlockPos> occupiedPositions, Random rand) {
        BlockPos collapseCenter = findWallDecorationPosition(worldIn, markerPos, occupiedPositions, 4, rand);
        if (collapseCenter == null) {
            return;
        }

        EnumFacing wallFacing = getClosestWallFacing(collapseCenter);
        int halfWidth = 1 + rand.nextInt(2);
        int depth = 2 + rand.nextInt(2);

        for (int depthOffset = 0; depthOffset < depth; depthOffset++) {
            for (int widthOffset = -halfWidth; widthOffset <= halfWidth; widthOffset++) {
                BlockPos rubblePos = getCollapsedAreaPosition(collapseCenter, wallFacing, widthOffset, depthOffset);
                int height = Math.max(1, 4 - depthOffset - Math.abs(widthOffset) / 2);
                if (!hasClearColumn(worldIn, rubblePos, height)) {
                    continue;
                }

                for (int yOffset = 0; yOffset < height; yOffset++) {
                    worldIn.setBlockState(rubblePos.up(yOffset), getCollapsedStoneState(rand), 3);
                }
                occupiedPositions.add(rubblePos);
            }
        }
    }

    private EnumFacing getClosestWallFacing(BlockPos pos) {
        int westDistance = pos.getX() - this.boundingBox.minX;
        int eastDistance = this.boundingBox.maxX - pos.getX();
        int northDistance = pos.getZ() - this.boundingBox.minZ;
        int southDistance = this.boundingBox.maxZ - pos.getZ();
        int nearestDistance = Math.min(Math.min(westDistance, eastDistance), Math.min(northDistance, southDistance));

        if (nearestDistance == westDistance) {
            return EnumFacing.WEST;
        }
        if (nearestDistance == eastDistance) {
            return EnumFacing.EAST;
        }
        return nearestDistance == northDistance ? EnumFacing.NORTH : EnumFacing.SOUTH;
    }

    private BlockPos getCollapsedAreaPosition(BlockPos centerPos, EnumFacing wallFacing, int widthOffset, int depthOffset) {
        switch (wallFacing) {
            case WEST:
                return new BlockPos(this.boundingBox.minX + 1 + depthOffset, centerPos.getY(), centerPos.getZ() + widthOffset);
            case EAST:
                return new BlockPos(this.boundingBox.maxX - 1 - depthOffset, centerPos.getY(), centerPos.getZ() + widthOffset);
            case NORTH:
                return new BlockPos(centerPos.getX() + widthOffset, centerPos.getY(), this.boundingBox.minZ + 1 + depthOffset);
            default:
                return new BlockPos(centerPos.getX() + widthOffset, centerPos.getY(), this.boundingBox.maxZ - 1 - depthOffset);
        }
    }

    private void placeBonepileCluster(World worldIn, BlockPos centerPos, List<BlockPos> occupiedPositions, Random rand) {
        int radius = 2;
        for (int xOffset = -radius; xOffset <= radius; xOffset++) {
            for (int zOffset = -radius; zOffset <= radius; zOffset++) {
                int distanceSquared = xOffset * xOffset + zOffset * zOffset;
                if (distanceSquared > radius * radius) {
                    continue;
                }

                BlockPos bonepilePos = centerPos.add(xOffset, 0, zOffset);
                if (!canPlaceDecorationAt(worldIn, bonepilePos)) {
                    continue;
                }

                int layers = getBonepileLayers(distanceSquared, rand);
                worldIn.setBlockState(bonepilePos, ModBlocks.bonepile.getDefaultState().withProperty(BlockBonepile.LAYERS, layers), 3);
                if (layers == 8 && rand.nextBoolean() && worldIn.isAirBlock(bonepilePos.up())) {
                    worldIn.setBlockState(bonepilePos.up(), ModBlocks.bone_mound.getDefaultState(), 3);
                }
                occupiedPositions.add(bonepilePos);
            }
        }
    }

    private static int getBonepileLayers(int distanceSquared, Random rand) {
        if (distanceSquared == 0) {
            return 8;
        }
        if (distanceSquared == 1) {
            return 6 + rand.nextInt(2);
        }
        if (distanceSquared == 2) {
            return 4 + rand.nextInt(2);
        }
        return 2 + rand.nextInt(2);
    }

    private void weatherGoblinCampFloor(World worldIn, BlockPos floorCenter, Random rand) {
        IBlockState[] weatheredBricks = {
            ModBlocks.bricks_stone_dirty.getDefaultState(),
            ModBlocks.bricks_stone_mossy.getDefaultState(),
            ModBlocks.bricks_stone_carved_dirty.getDefaultState(),
            ModBlocks.bricks_stone_carved_mossy.getDefaultState()
        };

        for (int x = this.boundingBox.minX + 2; x <= this.boundingBox.maxX - 2; x++) {
            for (int z = this.boundingBox.minZ + 2; z <= this.boundingBox.maxZ - 2; z++) {
                BlockPos floorPos = new BlockPos(x, floorCenter.getY(), z);
                if (isCentralGoblinCampFloor(floorPos) && worldIn.getBlockState(floorPos).getBlock() == ModBlocks.bricks_stone) {
                    worldIn.setBlockState(floorPos, ModBlocks.bricks_stone_dirty.getDefaultState(), 3);
                }
            }
        }

        for (int index = 0; index < 30; index++) {
            BlockPos floorPos = findInteriorPosition(floorCenter.getY(), rand);
            if (!isCentralGoblinCampFloor(floorPos) && worldIn.getBlockState(floorPos).getBlock() == ModBlocks.bricks_stone) {
                worldIn.setBlockState(floorPos, weatheredBricks[rand.nextInt(weatheredBricks.length)], 3);
            }
        }
    }

    private boolean isCentralGoblinCampFloor(BlockPos pos) {
        int centerX = (this.boundingBox.minX + this.boundingBox.maxX) / 2;
        int centerZ = (this.boundingBox.minZ + this.boundingBox.maxZ) / 2;
        int radius = Math.max(2, Math.min(this.boundingBox.getXSize(), this.boundingBox.getZSize()) / 4);
        return Math.abs(pos.getX() - centerX) <= radius && Math.abs(pos.getZ() - centerZ) <= radius;
    }

    private void placeStonePile(World worldIn, BlockPos pilePos, Random rand) {
        for (int index = 0; index < 2 + rand.nextInt(3); index++) {
            BlockPos partPos = index == 0 ? pilePos : pilePos.add(rand.nextInt(3) - 1, 0, rand.nextInt(3) - 1);
            if (canPlaceDecorationAt(worldIn, partPos)) {
                worldIn.setBlockState(partPos, getStonePileState(rand), 3);
            }
        }
    }

    private IBlockState getCollapsedStoneState(Random rand) {
        switch (rand.nextInt(4)) {
            case 0:
                return ModBlocks.bricks_stone_dirty.getDefaultState();
            case 1:
                return ModBlocks.bricks_stone_mossy.getDefaultState();
            case 2:
                return ModBlocks.bricks_stone_carved_dirty.getDefaultState();
            default:
                return ModBlocks.bricks_stone_carved_mossy.getDefaultState();
        }
    }

    private IBlockState getStonePileState(Random rand) {
        switch (rand.nextInt(5)) {
            case 0:
                return ModBlocks.bricks_stone_stair.getDefaultState()
                    .withProperty(BlockStairs.FACING, EnumFacing.HORIZONTALS[rand.nextInt(EnumFacing.HORIZONTALS.length)]);
            case 1:
                return ModBlocks.bricks_stone_slab.getDefaultState();
            case 2:
                return ModBlocks.bricks_stone_wall.getDefaultState();
            case 3:
                return ModBlocks.bricks_stone_dirty.getDefaultState();
            default:
                return ModBlocks.bricks_stone_mossy.getDefaultState();
        }
    }

    private BlockPos findClearDecorationPosition(World worldIn, BlockPos markerPos, List<BlockPos> occupiedPositions, int minimumSeparation, boolean requireNestClearance, Random rand) {
        return findClearDecorationPosition(worldIn, markerPos, occupiedPositions, minimumSeparation, requireNestClearance, null, rand);
    }

    /** When {@code region} is given, only spots whose whole footprint lies inside it are considered. */
    private BlockPos findClearDecorationPosition(World worldIn, BlockPos markerPos, List<BlockPos> occupiedPositions, int minimumSeparation, boolean requireNestClearance, StructureBoundingBox region, Random rand) {
        for (int attempt = 0; attempt < 64; attempt++) {
            BlockPos candidate = findInteriorPosition(markerPos.getY(), rand);
            if (isValidDecorationSpot(worldIn, candidate, occupiedPositions, minimumSeparation, requireNestClearance, region)) {
                return candidate;
            }
        }

        for (int x = this.boundingBox.minX + 2; x <= this.boundingBox.maxX - 2; x++) {
            for (int z = this.boundingBox.minZ + 2; z <= this.boundingBox.maxZ - 2; z++) {
                BlockPos candidate = new BlockPos(x, markerPos.getY(), z);
                if (isValidDecorationSpot(worldIn, candidate, occupiedPositions, minimumSeparation, requireNestClearance, region)) {
                    return candidate;
                }
            }
        }

        return null;
    }

    private boolean isValidDecorationSpot(World worldIn, BlockPos candidate, List<BlockPos> occupiedPositions, int minimumSeparation, boolean requireNestClearance, StructureBoundingBox region) {
        if (!canPlaceDecorationAt(worldIn, candidate) || !isFarEnoughFrom(candidate, occupiedPositions, minimumSeparation)) {
            return false;
        }
        if (region != null) {
            // Nests are 3x3, so the whole footprint has to be in the region, not just its centre.
            int margin = requireNestClearance ? 1 : 0;
            if (!region.isVecInside(candidate.add(-margin, 0, -margin)) || !region.isVecInside(candidate.add(margin, 0, margin))) {
                return false;
            }
        }
        return !requireNestClearance || hasClearNestFootprint(worldIn, candidate);
    }

    private BlockPos findStonePilePosition(World worldIn, BlockPos markerPos, List<BlockPos> occupiedPositions, Random rand) {
        if (rand.nextInt(10) != 0) {
            BlockPos wallPos = findWallDecorationPosition(worldIn, markerPos, occupiedPositions, 2, rand);
            if (wallPos != null) {
                return wallPos;
            }
        }
        return findClearDecorationPosition(worldIn, markerPos, occupiedPositions, 2, false, rand);
    }

    private BlockPos findWallDecorationPosition(World worldIn, BlockPos markerPos, List<BlockPos> occupiedPositions, int minimumSeparation, Random rand) {
        for (int attempt = 0; attempt < 64; attempt++) {
            BlockPos candidate = findWallPosition(markerPos.getY(), rand);
            if (canPlaceDecorationAt(worldIn, candidate) && isFarEnoughFrom(candidate, occupiedPositions, minimumSeparation)) {
                return candidate;
            }
        }
        return findClearDecorationPosition(worldIn, markerPos, occupiedPositions, minimumSeparation, false, rand);
    }

    private BlockPos findWallPosition(int y, Random rand) {
        int edgeOffset = 1 + rand.nextInt(2);
        int minX = this.boundingBox.minX + edgeOffset;
        int maxX = this.boundingBox.maxX - edgeOffset;
        int minZ = this.boundingBox.minZ + edgeOffset;
        int maxZ = this.boundingBox.maxZ - edgeOffset;

        switch (rand.nextInt(4)) {
            case 0:
                return new BlockPos(getOuterWallCoordinate(minX, maxX, rand), y, minZ);
            case 1:
                return new BlockPos(getOuterWallCoordinate(minX, maxX, rand), y, maxZ);
            case 2:
                return new BlockPos(minX, y, getOuterWallCoordinate(minZ, maxZ, rand));
            default:
                return new BlockPos(maxX, y, getOuterWallCoordinate(minZ, maxZ, rand));
        }
    }

    private static int getOuterWallCoordinate(int min, int max, Random rand) {
        int center = (min + max) / 2;
        int leftMax = center - 2;
        int rightMin = center + 2;

        if (rand.nextBoolean()) {
            return min + rand.nextInt(leftMax - min + 1);
        }
        return rightMin + rand.nextInt(max - rightMin + 1);
    }

    private BlockPos findInteriorPosition(int y, Random rand) {
        int minX = this.boundingBox.minX + 2;
        int maxX = this.boundingBox.maxX - 2;
        int minZ = this.boundingBox.minZ + 2;
        int maxZ = this.boundingBox.maxZ - 2;
        return new BlockPos(minX + rand.nextInt(maxX - minX + 1), y, minZ + rand.nextInt(maxZ - minZ + 1));
    }

    private static boolean canPlaceDecorationAt(World worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.down()).isTopSolid() && worldIn.isAirBlock(pos) && worldIn.isAirBlock(pos.up());
    }

    private static boolean hasClearColumn(World worldIn, BlockPos pos, int height) {
        if (!worldIn.getBlockState(pos.down()).isTopSolid()) {
            return false;
        }
        for (int yOffset = 0; yOffset < height; yOffset++) {
            if (!worldIn.isAirBlock(pos.up(yOffset))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isFarEnoughFrom(BlockPos candidate, List<BlockPos> positions, int minimumSeparation) {
        int minimumDistanceSquared = minimumSeparation * minimumSeparation;
        for (BlockPos position : positions) {
            int deltaX = candidate.getX() - position.getX();
            int deltaZ = candidate.getZ() - position.getZ();
            if (deltaX * deltaX + deltaZ * deltaZ < minimumDistanceSquared) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasClearNestFootprint(World worldIn, BlockPos nestPos) {
        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int zOffset = -1; zOffset <= 1; zOffset++) {
                BlockPos footprintPos = nestPos.add(xOffset, 0, zOffset);
                if (!canPlaceDecorationAt(worldIn, footprintPos)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean containsTemplate(List<StructureComponent> listIn, String templateName) {
        for (StructureComponent component : listIn) {
            if (component instanceof ComponentNbtPiece && templateName.equals(((ComponentNbtPiece) component).templateName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean contains(StructureBoundingBox boundingBox, BlockPos pos) {
        return pos.getX() >= boundingBox.minX && pos.getX() <= boundingBox.maxX
            && pos.getY() >= boundingBox.minY && pos.getY() <= boundingBox.maxY
            && pos.getZ() >= boundingBox.minZ && pos.getZ() <= boundingBox.maxZ;
    }

    public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand, TemplateManager templateManager) {
        if (this.templateName.equals("x1_5_seal")) return;
        // If we are already a dead end, stop generating.
        for (String deadEnd : DEAD_ENDS) {
            if (this.templateName.equals(deadEnd)) {
                return;
            }
        }
        
        Template template = templateManager.getTemplate(null, new ResourceLocation("totf", this.templateName));
        PlacementSettings placementsettings = (new PlacementSettings()).setRotation(this.rotation);
        
        Map<BlockPos, String> dataBlocks = template.getDataBlocks(this.templatePosition, placementsettings);

        // Collect children from all of this piece's markers before recursing into any of them, so a branching
        // piece (T-junction, cross) gets all its exits placed before one branch's subtree can eat the whole
        // MAX_PIECES budget and starve its siblings into being closed off.
        List<ComponentNbtPiece> newChildren = new ArrayList<>();

        for (Map.Entry<BlockPos, String> entry : dataBlocks.entrySet()) {
            if (!isDoorMarker(entry.getValue())) {
                continue;
            }

            BlockPos markerPos = entry.getKey();
            
            // 1. Determine which way the CURRENT marker is facing in world space
            net.minecraft.util.EnumFacing outFacing = null;
            StructureBoundingBox bb = this.getBoundingBox();

            if (markerPos.getX() == bb.minX) outFacing = net.minecraft.util.EnumFacing.WEST;
            else if (markerPos.getX() == bb.maxX) outFacing = net.minecraft.util.EnumFacing.EAST;
            else if (markerPos.getZ() == bb.minZ) outFacing = net.minecraft.util.EnumFacing.NORTH;
            else if (markerPos.getZ() == bb.maxZ) outFacing = net.minecraft.util.EnumFacing.SOUTH;

            if (outFacing == null) {
                continue; // Marker is not on the edge, cannot infer direction!
            }

            // Target position for the NEXT marker is one block outside our bounding box
            BlockPos targetMarkerPos = markerPos.offset(outFacing);

            boolean alreadyConnected = false;
            boolean blocked = false;
            for (StructureComponent component : listIn) {
                if (component == this || !contains(component.getBoundingBox(), targetMarkerPos)) {
                    continue;
                }

                if (component == componentIn) {
                    alreadyConnected = true;
                } else {
                    blocked = true;
                }
            }
            if (alreadyConnected) {
                continue;
            }
            if (blocked) {
                this.closedDoorways.add(markerPos);
                continue;
            }

            if (listIn.size() >= MAX_PIECES) {
                this.closedDoorways.add(markerPos);
                continue;
            }

            boolean isCapping = this.componentType >= MAX_DEPTH;
            
            java.util.List<String> templatesToTry = new java.util.ArrayList<>();
            if (isCapping) {
                templatesToTry.add(DEAD_ENDS[rand.nextInt(DEAD_ENDS.length)]);
                templatesToTry.add("x1_5_seal");
            } else {
                boolean forceHall = this.templateName.startsWith("x21_");

                if (forceHall) {
                    for (int i = 0; i < 4; i++) {
                        templatesToTry.add(HALLS[rand.nextInt(HALLS.length)]);
                    }
                } else {
                    String[] rooms = containsTemplate(listIn, MOON_ROOM) ? ROOMS_WITHOUT_MOON : ROOMS;
                    for (int i = 0; i < 6; i++) {
                        String[] pool = rand.nextBoolean() ? HALLS : rooms;
                        templatesToTry.add(pool[rand.nextInt(pool.length)]);
                    }
                }
                
                // Add a few dead ends to try if everything else fails
                for (int i = 0; i < 2; i++) {
                    templatesToTry.add(DEAD_ENDS[rand.nextInt(DEAD_ENDS.length)]);
                }
                templatesToTry.add("x1_5_seal");
            }
            
            boolean matched = false;
            
            for (String nextTemplate : templatesToTry) {
                Template nextTemp = templateManager.getTemplate(null, new ResourceLocation("totf", nextTemplate));

                // Try different random rotations to find one that fits
                int startRot = rand.nextInt(4);
                for (int i = 0; i < 4; i++) {
                    Rotation nextRotation = Rotation.values()[(startRot + i) % 4];
                    PlacementSettings nextSettings = (new PlacementSettings()).setRotation(nextRotation);
                    
                    // Get next template's markers rotated but relative to origin (0,0,0)
                    Map<BlockPos, String> nextMarkers = nextTemp.getDataBlocks(BlockPos.ORIGIN, nextSettings);
                    
                    for (BlockPos nextLocalMarker : nextMarkers.keySet()) {
                        // Calculate where the new piece's origin must be to align the markers
                        BlockPos newOrigin = targetMarkerPos.subtract(nextLocalMarker);
                        
                        ComponentNbtPiece nextPiece = new ComponentNbtPiece(templateManager, nextTemplate, newOrigin, nextRotation, this.componentType + 1);
                        StructureBoundingBox nextBB = nextPiece.getBoundingBox();
                        
                        // Verify that the chosen marker is actually on the CORRECT edge of the new piece
                        boolean validEdge = false;
                        BlockPos worldNextMarker = nextLocalMarker.add(newOrigin);

                        if (outFacing == net.minecraft.util.EnumFacing.NORTH && worldNextMarker.getZ() == nextBB.maxZ) validEdge = true;
                        if (outFacing == net.minecraft.util.EnumFacing.SOUTH && worldNextMarker.getZ() == nextBB.minZ) validEdge = true;
                        if (outFacing == net.minecraft.util.EnumFacing.WEST && worldNextMarker.getX() == nextBB.maxX) validEdge = true;
                        if (outFacing == net.minecraft.util.EnumFacing.EAST && worldNextMarker.getX() == nextBB.minX) validEdge = true;
                        
                        if (!validEdge) {
                            continue;
                        }
                        
                        StructureComponent intersecting = null;
                        
                        for (StructureComponent comp : listIn) {
                            if (comp.getBoundingBox().intersectsWith(nextBB)) {
                                intersecting = comp;
                                break;
                            }
                        }
                        
                        if (intersecting == null) {
                            listIn.add(nextPiece);
                            newChildren.add(nextPiece);
                            matched = true;
                            break;
                        }
                    }
                    if (matched) break;
                }
                if (matched) break;
            }

            if (!matched) {
                this.closedDoorways.add(markerPos);
            }
        }

        for (ComponentNbtPiece child : newChildren) {
            child.buildComponent(this, listIn, rand, templateManager);
        }
    }
}
