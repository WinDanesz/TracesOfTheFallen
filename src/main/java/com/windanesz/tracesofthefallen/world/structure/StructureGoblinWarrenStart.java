package com.windanesz.tracesofthefallen.world.structure;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.world.structure.ComponentGoblinWarrenRoom.Role;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Lays out a goblin warren: a hall with a works, an optional den and an optional vault hanging off its walls,
 * joined by short tunnels, with the remaining hall doorways opening onto long approaches from the outside.
 * <pre>
 *   approach -- HALL -- BORE -- WORKS -- connector (descending) -- VAULT
 *                 |
 *            connector -- DEN        (or a second approach)
 * </pre>
 */
public class StructureGoblinWarrenStart extends StructureStart {

    private static final String HALL_TEMPLATE = "goblin_warren_hall";
    private static final String BORE_TEMPLATE = "goblin_warren_bore";
    private static final String WORKS_TEMPLATE = "goblin_warren_works";
    private static final String VAULT_TEMPLATE = "goblin_warren_vault";
    private static final String DEN_TEMPLATE = "goblin_warren_den";

    // Blocks of tunnel between two rooms.
    private static final int MIN_GAP = 8;
    private static final int GAP_RANGE = 7;
    // A tunnel may drop at most this much floor per block of length, so it stays walkable.
    private static final int GAP_PER_DROP = 2;
    private static final int MIN_VAULT_DROP = 3;
    private static final int MAX_VAULT_DROP = 8;
    // Lowest world Y for the bottom of any room; keeps the vault well clear of bedrock.
    private static final int MIN_ROOM_BASE_Y = 7;
    private static final int MIN_APPROACH_LENGTH = 22;
    private static final int APPROACH_EXTRA_LENGTH = 13;
    private static final int APPROACH_HEADROOM = 6;
    private static final int ATTEMPTS = 8;

    // Required by MapGenStructureIO when loading existing structures.
    public StructureGoblinWarrenStart() {
    }

    public StructureGoblinWarrenStart(World world, Random random, int chunkX, int chunkZ) {
        super(chunkX, chunkZ);
        TemplateManager templateManager = world.getSaveHandler().getStructureTemplateManager();

        int configuredMinY = Settings.worldgenSettings.goblinWarrenMinY;
        int configuredMaxY = Settings.worldgenSettings.goblinWarrenMaxY;
        int minY = Math.min(configuredMinY, configuredMaxY);
        int maxY = Math.max(configuredMinY, configuredMaxY);
        BlockPos pos = new BlockPos(chunkX * 16 + 8, minY + random.nextInt(maxY - minY + 1), chunkZ * 16 + 8);

        List<StructureComponent> parts = null;
        for (int attempt = 0; attempt < ATTEMPTS && parts == null; attempt++) {
            parts = assemble(templateManager, random, pos, true);
        }
        if (parts == null) {
            // Rooms would not fit around this hall; fall back to the hall and its approaches alone.
            parts = assemble(templateManager, random, pos, false);
        }
        if (parts != null) {
            this.components.addAll(parts);
        }
        this.updateBoundingBox();
    }

    private static List<StructureComponent> assemble(TemplateManager templateManager, Random random, BlockPos pos, boolean withRooms) {
        Settings.WorldgenSettings settings = Settings.worldgenSettings;
        Rotation rotation = Rotation.values()[random.nextInt(Rotation.values().length)];
        Mirror mirror = Mirror.values()[random.nextInt(Mirror.values().length)];
        ComponentGoblinWarrenRoom hall = new ComponentGoblinWarrenRoom(templateManager, Role.HALL, HALL_TEMPLATE, pos,
                rotation, mirror, random.nextLong());
        int exitCount = hall.exitCount();
        if (exitCount < 2) {
            return null;
        }

        List<StructureComponent> parts = new ArrayList<>();
        parts.add(hall);
        List<StructureBoundingBox> occupied = new ArrayList<>();
        occupied.add(hall.getBoundingBox());

        List<Integer> order = new ArrayList<>();
        for (int index = 0; index < exitCount; index++) {
            order.add(index);
        }
        Collections.shuffle(order, random);
        boolean[] used = new boolean[exitCount];

        if (withRooms) {
            // The diggers' camp sits between the hall and the works; if it will not fit the works hangs off the hall.
            ComponentGoblinWarrenRoom worksParent = hall;
            int worksParentExit = order.get(0);
            if (settings.goblinWarrenBoreEnabled) {
                Attached bore = attach(templateManager, random, hall, order.get(0), Role.BORE, BORE_TEMPLATE, 0, occupied, parts);
                if (bore != null && bore.farExit >= 0) {
                    worksParent = bore.room;
                    worksParentExit = bore.farExit;
                }
            }
            Attached works = attach(templateManager, random, worksParent, worksParentExit, Role.WORKS, WORKS_TEMPLATE, 0, occupied, parts);
            if (works == null) {
                return null;
            }
            used[order.get(0)] = true;

            if (settings.goblinWarrenVaultEnabled && works.farExit >= 0) {
                BlockPos farMouth = works.room.worldMouth(works.farExit);
                int drop = Math.min(MIN_VAULT_DROP + random.nextInt(MAX_VAULT_DROP - MIN_VAULT_DROP + 1), farMouth.getY() - (MIN_ROOM_BASE_Y + 1));
                if (drop >= MIN_VAULT_DROP) {
                    attach(templateManager, random, works.room, works.farExit, Role.VAULT, VAULT_TEMPLATE, drop, occupied, parts);
                }
            }
            // Always rolled, so the sequence does not depend on how many exits the hall has.
            boolean wantDen = random.nextInt(100) < settings.goblinWarrenDenChance;
            if (wantDen && exitCount > 2
                    && attach(templateManager, random, hall, order.get(2), Role.DEN, DEN_TEMPLATE, 0, occupied, parts) != null) {
                used[order.get(2)] = true;
            }
        }

        // Every hall doorway not leading to a room is a way in from the outside.
        for (int index : order) {
            if (!used[index]) {
                BlockPos mouth = hall.worldMouth(index);
                EnumFacing outward = hall.worldOutward(index);
                int length = MIN_APPROACH_LENGTH + random.nextInt(APPROACH_EXTRA_LENGTH);
                parts.add(ComponentGoblinWarrenTunnel.approach(mouth, outward, length, pos.getY() + APPROACH_HEADROOM,
                        random.nextLong(), Collections.singletonList(hall.getBoundingBox())));
            }
        }
        return parts;
    }

    private static final class Attached {
        final ComponentGoblinWarrenRoom room;
        /** The room's other doorway, or -1 if it has only one. */
        final int farExit;

        Attached(ComponentGoblinWarrenRoom room, int farExit) {
            this.room = room;
            this.farExit = farExit;
        }
    }

    /**
     * Hangs a room off one of a placed room's doorways, with a tunnel between. The new room is turned (and
     * maybe mirrored) so one of its own doorways faces back along the tunnel, then set down so that doorway's
     * mouth is exactly where the tunnel ends. Returns null, adding nothing, if it would not fit.
     */
    private static Attached attach(TemplateManager templateManager, Random random, ComponentGoblinWarrenRoom parent, int parentExit,
                                   Role role, String template, int drop, List<StructureBoundingBox> occupied, List<StructureComponent> parts) {
        BlockPos mouth = parent.worldMouth(parentExit);
        EnumFacing direction = parent.worldOutward(parentExit);
        int gap = Math.max(MIN_GAP + random.nextInt(GAP_RANGE), drop * GAP_PER_DROP);
        BlockPos target = mouth.offset(direction, gap).down(drop);

        WarrenLayout layout = ComponentGoblinWarrenRoom.layoutOf(templateManager, template);
        List<int[]> options = new ArrayList<>();
        for (int exit = 0; exit < layout.exits.size(); exit++) {
            for (Rotation rotation : Rotation.values()) {
                for (Mirror mirror : Mirror.values()) {
                    PlacementSettings settings = new PlacementSettings().setRotation(rotation).setMirror(mirror);
                    if (ComponentGoblinWarrenRoom.outwardOf(layout.exits.get(exit), settings) == direction.getOpposite()) {
                        options.add(new int[]{exit, rotation.ordinal(), mirror.ordinal()});
                    }
                }
            }
        }
        if (options.isEmpty()) {
            return null;
        }
        int[] pick = options.get(random.nextInt(options.size()));
        Rotation rotation = Rotation.values()[pick[1]];
        Mirror mirror = Mirror.values()[pick[2]];
        PlacementSettings settings = new PlacementSettings().setRotation(rotation).setMirror(mirror);
        BlockPos origin = target.subtract(Template.transformedBlockPos(settings, layout.exits.get(pick[0]).mouth));
        if (origin.getY() < MIN_ROOM_BASE_Y) {
            return null;
        }

        ComponentGoblinWarrenRoom room = new ComponentGoblinWarrenRoom(templateManager, role, template, origin, rotation, mirror, random.nextLong());
        ComponentGoblinWarrenTunnel tunnel = ComponentGoblinWarrenTunnel.connector(mouth, target, random.nextLong(),
                java.util.Arrays.asList(parent.getBoundingBox(), room.getBoundingBox()));
        for (StructureBoundingBox box : occupied) {
            boolean isParent = box == parent.getBoundingBox();
            if (box.intersectsWith(room.getBoundingBox()) || (!isParent && box.intersectsWith(tunnel.getBoundingBox()))) {
                return null;
            }
        }

        occupied.add(room.getBoundingBox());
        parts.add(tunnel);
        parts.add(room);
        int farExit = layout.exits.size() > 1 ? (pick[0] == 0 ? 1 : 0) : -1;
        return new Attached(room, farExit);
    }
}
