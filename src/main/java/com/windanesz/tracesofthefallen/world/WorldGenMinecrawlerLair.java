package com.windanesz.tracesofthefallen.world;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.block.BlockCrawlerWaxWet;
import com.windanesz.tracesofthefallen.block.BlockSkeletonCorpse;
import com.windanesz.tracesofthefallen.block.BlockTOFT;
import com.windanesz.tracesofthefallen.block.TileEntitySkeletonCorpse;
import com.windanesz.tracesofthefallen.entity.EntityMinecrawler;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * Hand-carved procedural underground pocket housing a small, non-despawning Minecrawler nest.
 * Unlike the multipart NBT dungeon system, this generator carves its chamber directly out of
 * whatever stone it finds, so its size/shape varies per-instance instead of coming from a template.
 */
public class WorldGenMinecrawlerLair implements IWorldGenerator {

	private static final long SEED_MODIFIER = 913174639L;

	// Every horizontal neighbor direction, paired with the FACING value BlockCrawlerWaxWet needs to
	// render flush against a support block sitting in that direction. Like a torch, FACING points
	// AWAY from its support (support = pos.offset(facing.getOpposite())), so each entry here is the
	// opposite of its neighbor offset.
	private static final int[][] NEIGHBORS = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
	private static final EnumFacing[] NEIGHBOR_FACING = {EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.WEST};

	// The 2x2 footprint of the fall-trap shaft, relative to its anchor corner.
	private static final int[][] SHAFT_COLUMNS = {{0, 0}, {1, 0}, {0, 1}, {1, 1}};
	private static final float FALL_TRAP_CHANCE = 0.15F;

	// Every lair gets one guaranteed entrance tunnel off the main room; if it completes without
	// breaking into a natural cave, these gate whether it leads to another (smaller) connected room
	// instead of just dead-ending, up to a total of MAX_ROOMS.
	private static final int MAX_ROOMS = 3;
	private static final float SECOND_ROOM_CHANCE = 0.80F;
	private static final float THIRD_ROOM_CHANCE = 0.45F;
	private static final float EXTRA_ROOM_MIN_SCALE = 0.55F;
	private static final float EXTRA_ROOM_MAX_SCALE = 0.85F;

	// Entrance tunnels are irregular caves rather than fixed-width corridors.
	private static final int TUNNEL_MIN_LENGTH = 50;
	private static final int TUNNEL_EXTRA_LENGTH = 41;
	private static final int TUNNEL_MAX_BRANCHES = 2;
	private static final int TUNNEL_BRANCH_MIN_STEP = 8;
	private static final int TUNNEL_MAX_DRIFT = 8;
	private static final int TUNNEL_MIN_Y = 6;
	// Steps ignored when watching for contact with a natural cave, since the first slices overlap the chamber.
	private static final int TUNNEL_CONTACT_GRACE = 6;
	private static final float TUNNEL_MIN_RADIUS = 1.3F;
	private static final float TUNNEL_MAX_RADIUS = 2.6F;
	private static final float TUNNEL_CHAMBER_RADIUS = 3.6F;
	private static final double TUNNEL_TURN_LIMIT = Math.toRadians(80);
	private static final double[] TUNNEL_STEER_OFFSETS = {Math.toRadians(35), Math.toRadians(70), Math.toRadians(110), Math.toRadians(150), Math.PI};

	// Independent decoration rolls per inhabited room; a room can have any combination, or none.
	private static final float MUSHROOM_CLUSTER_CHANCE = 0.15F;
	private static final float SKELETON_CRATE_CHANCE = 0.10F;
	private static final float LOST_CRATE_POTIONS_CHANCE = 0.06F;
	private static final float MAIN_ROOM_SKELETON_CHANCE = 0.60F;
	private static final float SIDE_ROOM_SKELETON_CHANCE = 0.35F;
	private static final float SKELETON_CORPSE_EXTRA_CHANCE = 0.15F;
	private static final int SKELETON_CORPSE_MAX = 3;

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		random.setSeed(random.nextLong() + SEED_MODIFIER);

		if (!ArrayUtils.contains(Settings.worldgenSettings.dimensionList, world.provider.getDimension())) {
			return;
		}

		int frequency = Settings.worldgenSettings.minecrawlerLairFrequency;
		if (frequency <= 0 || random.nextInt(frequency) != 0) {
			return;
		}

		int minY = Settings.worldgenSettings.minecrawlerLairMinY;
		int maxY = Math.max(minY + 1, Settings.worldgenSettings.minecrawlerLairMaxY);

		// Random 1.5x-2.0x size multiplier so lairs vary noticeably from one to the next.
		double scale = 1.5D + random.nextDouble() * 0.5D;
		int radius = (int) Math.round((3 + random.nextInt(3)) * scale);
		int domeHeight = (int) Math.round((2 + random.nextInt(2)) * scale);

		int attempts = 6;
		for (int i = 0; i < attempts; i++) {
			int x = (chunkX << 4) + 4 + random.nextInt(8);
			int z = (chunkZ << 4) + 4 + random.nextInt(8);
			int y = minY + random.nextInt(maxY - minY);

			BlockPos floorPos = new BlockPos(x, y, z);
			if (isSuitableFloor(world, floorPos, radius)) {
				carveLair(world, random, floorPos, radius, domeHeight);
				return;
			}
		}
	}

	/**
	 * Requires a solid, non-liquid floor with mostly solid rock around it, so the carved chamber
	 * doesn't just poke a hole into an existing cavern or flood from a nearby liquid pocket.
	 */
	private boolean isSuitableFloor(World world, BlockPos pos, int radius) {
		if (!world.isBlockLoaded(pos)) {
			return false;
		}

		IBlockState floorState = world.getBlockState(pos);
		if (!floorState.getMaterial().isSolid() || floorState.getMaterial().isLiquid() || !floorState.isTopSolid()) {
			return false;
		}

		int step = Math.max(1, radius / 2);
		int solid = 0;
		int checked = 0;
		for (int dx = -radius; dx <= radius; dx += step) {
			for (int dy = 1; dy <= radius; dy += step) {
				for (int dz = -radius; dz <= radius; dz += step) {
					checked++;
					IBlockState state = world.getBlockState(pos.add(dx, dy, dz));
					if (state.getMaterial().isSolid() && !state.getMaterial().isLiquid()) {
						solid++;
					}
				}
			}
		}
		return solid >= checked * 0.8D;
	}

	/** Geometry and actual carved floor cells, retained for the final decoration pass. */
	private static final class RoomResult {
		final int boundRadius;
		final int centerHeight;
		final List<BlockPos> floors;

		RoomResult(int boundRadius, int centerHeight, List<BlockPos> floors) {
			this.boundRadius = boundRadius;
			this.centerHeight = centerHeight;
			this.floors = floors;
		}
	}

	private void carveLair(World world, Random random, BlockPos floorPos, int radius, int domeHeight) {
		RoomResult mainRoom = carveRoom(world, random, floorPos, radius, domeHeight);
		List<RoomResult> rooms = new ArrayList<>();
		rooms.add(mainRoom);

		if (mainRoom.centerHeight > 0 && random.nextFloat() < FALL_TRAP_CHANCE) {
			carveFallTrap(world, random, floorPos, mainRoom.centerHeight);
		}

		// Every lair gets at least one real, fully-carved entrance tunnel out of the main room (the
		// first loop iteration always runs it before any chance check). If that tunnel completes
		// without breaking into a natural cave, roll a chance to chain another, smaller connected room
		// off the end of it - and possibly a third off that one - instead of always stopping at one.
		BlockPos linkAnchor = floorPos;
		List<BlockPos> linkFloors = mainRoom.floors;
		double linkRadius = mainRoom.boundRadius;
		int roomsPlaced = 1;

		while (roomsPlaced < MAX_ROOMS) {
			BlockPos tunnelEnd = carveTunnel(world, random, linkAnchor, linkFloors);
			float extraRoomChance = roomsPlaced == 1 ? SECOND_ROOM_CHANCE : THIRD_ROOM_CHANCE;
			if (tunnelEnd == null || random.nextFloat() >= extraRoomChance) {
				break;
			}

			double nextRadius = Math.max(2.5D, linkRadius * (EXTRA_ROOM_MIN_SCALE + random.nextDouble() * (EXTRA_ROOM_MAX_SCALE - EXTRA_ROOM_MIN_SCALE)));
			int nextHeight = Math.max(2, (int) Math.round(domeHeight * (0.7D + random.nextDouble() * 0.3D)));
			RoomResult extraRoom = carveRoom(world, random, tunnelEnd, nextRadius, nextHeight);
			rooms.add(extraRoom);

			roomsPlaced++;
			linkAnchor = tunnelEnd;
			linkFloors = extraRoom.floors;
			linkRadius = extraRoom.boundRadius;
		}

		// Reserve floor space for remains before cocoons and mushrooms compete for it. The main
		// chamber always gets a corpse when a usable floor cell remains after carving.
		for (RoomResult room : rooms) {
			if (ModBlocks.skeleton_corpse instanceof BlockSkeletonCorpse) {
				placeSkeletons(world, random, room.floors, room == mainRoom, (BlockSkeletonCorpse) ModBlocks.skeleton_corpse);
			}
		}
		// Finish all room/tunnel carving before placing cocoons, so later rooms cannot erase them.
		for (RoomResult room : rooms) {
			for (BlockPos cocoon : placeCocoons(world, random, room.floors, ModBlocks.minecrawler_cocoon)) {
				decorateAroundCocoon(world, random, cocoon);
			}
		}
		for (RoomResult room : rooms) {
			decorateExtras(world, random, room.floors);
		}
		// Nothing may carve or place blocks after inhabitants have been positioned.
		for (RoomResult room : rooms) {
			spawnCrawlers(world, random, room.floors);
		}
	}

	/**
	 * Carves and decorates one chamber. The footprint is an ellipse (independently randomized X/Z
	 * radii) rather than a perfect circle, and each column's height gets its own random jitter, so the
	 * silhouette comes out lumpy and organic instead of a smooth, identical hemisphere every time -
	 * used for the main room, chained extra rooms, and the fall trap's hidden upper cave alike.
	 */
	private RoomResult carveRoom(World world, Random random, BlockPos anchor, double baseRadius, int height) {
		double radiusX = baseRadius * (0.75D + random.nextDouble() * 0.5D);
		double radiusZ = baseRadius * (0.75D + random.nextDouble() * 0.5D);
		int boundRadius = (int) Math.ceil(Math.max(radiusX, radiusZ));
		int size = 2 * boundRadius + 1;
		// carvedHeight[x][z] == 0 means that column stays solid; otherwise it's how many blocks of
		// clearance were carved above the floor there. Pre-computing this lets the decoration pass
		// tell open cells from walls without re-deriving the room's shape.
		int[][] carvedHeight = new int[size][size];
		List<BlockPos> floors = new ArrayList<>();

		for (int dx = -boundRadius; dx <= boundRadius; dx++) {
			for (int dz = -boundRadius; dz <= boundRadius; dz++) {
				double normalized = Math.sqrt((dx / radiusX) * (dx / radiusX) + (dz / radiusZ) * (dz / radiusZ));
				if (normalized > 1.0D) {
					continue;
				}

				double jitter = 0.75D + random.nextDouble() * 0.5D;
				int columnHeight = (int) Math.round(height * jitter * Math.sqrt(Math.max(0.0D, 1.0D - normalized * normalized)));
				if (columnHeight <= 0) {
					continue;
				}
				// Minecrawlers are 1.4 blocks tall and suffocate in a 1-tall pocket, so any carved
				// column must have at least 2 blocks of clearance - or none at all.
				columnHeight = Math.max(columnHeight, 2);

				if (isColumnCarvable(world, anchor, dx, dz, columnHeight)) {
					carvedHeight[dx + boundRadius][dz + boundRadius] = columnHeight;
					floors.add(anchor.add(dx, 0, dz));
				}
			}
		}

		for (int dx = -boundRadius; dx <= boundRadius; dx++) {
			for (int dz = -boundRadius; dz <= boundRadius; dz++) {
				int h = carvedHeight[dx + boundRadius][dz + boundRadius];
				for (int dy = 1; dy <= h; dy++) {
					world.setBlockToAir(anchor.add(dx, dy, dz));
				}
			}
		}

		for (int dx = -boundRadius; dx <= boundRadius; dx++) {
			for (int dz = -boundRadius; dz <= boundRadius; dz++) {
				int h = carvedHeight[dx + boundRadius][dz + boundRadius];
				if (h <= 0) {
					continue;
				}
				decorateFloorAndCeiling(world, random, anchor, dx, dz, h);
				decorateWalls(world, random, anchor, carvedHeight, boundRadius, dx, dz, h);
			}
		}

		return new RoomResult(boundRadius, carvedHeight[boundRadius][boundRadius], floors);
	}

	/**
	 * A tricky variant: punches a 2x2 hole through the roof, straight up into a small hidden cave, then
	 * seals the top of the hole with a horizontal skin of Wet Crawler Wax instead of solid ground. That
	 * block has no collision at all, so from above it just looks like part of the upper cave's floor -
	 * step on it and you fall straight through into the nest below. A dead-end shaft (no room for the
	 * upper cave) is left as a plain uncapped hole instead of forcing the trap.
	 */
	private void carveFallTrap(World world, Random random, BlockPos floorPos, int centerHeight) {
		BlockPos shaftBase = floorPos.add(0, centerHeight + 1, 0);
		int shaftLength = 3 + random.nextInt(3); // 3-5 blocks

		int actualLength = 0;
		outer:
		for (int dy = 0; dy < shaftLength; dy++) {
			for (int[] offset : SHAFT_COLUMNS) {
				IBlockState state = world.getBlockState(shaftBase.add(offset[0], dy, offset[1]));
				if (state.getBlock() == Blocks.BEDROCK || state.getMaterial().isLiquid()) {
					break outer;
				}
			}
			actualLength = dy + 1;
		}

		if (actualLength < 2) {
			return;
		}

		for (int dy = 0; dy < actualLength; dy++) {
			for (int[] offset : SHAFT_COLUMNS) {
				world.setBlockToAir(shaftBase.add(offset[0], dy, offset[1]));
			}
		}

		BlockPos caveAnchor = shaftBase.add(0, actualLength, 0);
		carveRoom(world, random, caveAnchor, 2 + random.nextInt(2), 2);

		// carveRoom treats its own anchor plane as solid ground (same as the main chamber's floor), so
		// the shaft's opening into it is still capped by rock at this point - punch it open, then seal
		// it with the fake wax "floor" instead of leaving it as an obvious hole.
		for (int[] offset : SHAFT_COLUMNS) {
			BlockPos plugPos = shaftBase.add(offset[0], actualLength, offset[1]);
			world.setBlockState(plugPos, ModBlocks.crawler_wax_wet.getDefaultState().withProperty(BlockCrawlerWaxWet.FACING, EnumFacing.UP), 2);
		}
	}

	/**
	 * Dry-runs the column so we never partially carve it into a bedrock/liquid-truncated 1-tall pocket.
	 * Also refuses to touch a column in a not-yet-loaded chunk - a big room near a chunk edge could
	 * otherwise reach into terrain that isn't safely generated yet at populate time.
	 */
	private boolean isColumnCarvable(World world, BlockPos floorPos, int dx, int dz, int columnHeight) {
		BlockPos columnPos = floorPos.add(dx, 0, dz);
		if (!world.isBlockLoaded(columnPos)) {
			return false;
		}
		for (int dy = 1; dy <= columnHeight; dy++) {
			IBlockState state = world.getBlockState(columnPos.up(dy));
			if (state.getBlock() == Blocks.BEDROCK || state.getMaterial().isLiquid()) {
				return false;
			}
		}
		return true;
	}

	private void decorateFloorAndCeiling(World world, Random random, BlockPos floorPos, int dx, int dz, int height) {
		BlockPos groundPos = floorPos.add(dx, 0, dz);

		BlockPos ceilingPos = floorPos.add(dx, height + 1, dz);
		IBlockState ceilingState = world.getBlockState(ceilingPos);
		if (ceilingState.getMaterial().isSolid() && !ceilingState.getMaterial().isLiquid()) {
			if (random.nextFloat() < 0.65F) {
				world.setBlockState(ceilingPos, ModBlocks.crawler_wax_block.getDefaultState(), 2);
			}
			if (random.nextFloat() < 0.35F) {
				BlockPos dripPos = floorPos.add(dx, height, dz);
				if (world.isAirBlock(dripPos)) {
					world.setBlockState(dripPos, ModBlocks.crawler_wax_wet.getDefaultState().withProperty(BlockCrawlerWaxWet.FACING, EnumFacing.DOWN), 2);
				}
			}
		}

		// Heavy floor coverage - most of the nest floor is coated one way or another, only a
		// minority of bare stone peeks through.
		float floorRoll = random.nextFloat();
		if (floorRoll < 0.5F) {
			BlockPos puddlePos = floorPos.add(dx, 1, dz);
			if (world.isAirBlock(puddlePos)) {
				world.setBlockState(puddlePos, ModBlocks.crawler_wax_wet.getDefaultState().withProperty(BlockCrawlerWaxWet.FACING, EnumFacing.UP), 2);
			}
		} else if (floorRoll < 0.85F) {
			world.setBlockState(groundPos, ModBlocks.crawler_wax_block.getDefaultState(), 2);
		}
	}

	/** Coats the exposed rock walls between an open cell and any still-solid neighboring column. */
	private void decorateWalls(World world, Random random, BlockPos floorPos, int[][] carvedHeight, int radius, int dx, int dz, int height) {
		for (int dy = 1; dy <= height; dy++) {
			for (int n = 0; n < NEIGHBORS.length; n++) {
				int nx = dx + NEIGHBORS[n][0];
				int nz = dz + NEIGHBORS[n][1];
				int neighborHeight = 0;
				if (nx >= -radius && nx <= radius && nz >= -radius && nz <= radius) {
					neighborHeight = carvedHeight[nx + radius][nz + radius];
				}
				if (dy > neighborHeight && random.nextFloat() < 0.5F) {
					BlockPos wallPos = floorPos.add(nx, dy, nz);
					IBlockState wallState = world.getBlockState(wallPos);
					if (!wallState.getMaterial().isSolid() || wallState.getMaterial().isLiquid()) {
						continue;
					}
					if (random.nextBoolean()) {
						world.setBlockState(wallPos, ModBlocks.crawler_wax_block.getDefaultState(), 2);
					} else {
						BlockPos openPos = floorPos.add(dx, dy, dz);
						if (world.isAirBlock(openPos)) {
							world.setBlockState(openPos, ModBlocks.crawler_wax_wet.getDefaultState().withProperty(BlockCrawlerWaxWet.FACING, NEIGHBOR_FACING[n]), 2);
						}
					}
				}
			}
		}
	}

	/** One step of a cave walk: where its floor cell is and which way the walk was heading there. */
	private static final class TunnelStep {
		final BlockPos floor;
		final double angle;

		TunnelStep(BlockPos floor, double angle) {
			this.floor = floor;
			this.angle = angle;
		}
	}

	/**
	 * Digs a long, irregular cave out from the chamber instead of a dead-end stub, so the lair always has a
	 * real entrance and actually leads somewhere - either it breaks into an existing cave/cavern (detected
	 * and stopped on contact) or it simply runs its full length. The walk heads out in a random direction,
	 * meanders within 80 degrees of it (so it can't double back through the chamber), rises and sinks
	 * gently, swells into chambers and narrows again, and may fork into short dead-end side branches.
	 * <p>
	 * Every slice is gated on {@code world.isAreaLoaded} before anything is touched. Without that, a long
	 * tunnel routinely wanders outside the small area a chunk populate() call guarantees is already
	 * generated (this codebase already works around exactly this "cascading worldgen" hazard elsewhere,
	 * e.g. WorldGenBase's chunk-center offset) - reading/writing further out force-generates neighboring
	 * chunks mid-populate, and whatever got carved out there does not reliably survive. That was the actual
	 * cause of nests turning up with no visible tunnel at all. Rather than giving up at the first unloaded
	 * edge, the walk steers away from it, so it uses the loaded area it does have.
	 *
	 * @return the final position reached, so a caller can chain another room off of it. Reaching a
	 * natural cave stops this tunnel but still leaves the reached position available for another chamber.
	 */
	private BlockPos carveTunnel(World world, Random random, BlockPos floorPos, List<BlockPos> roomFloors) {
		EnumFacing dir = EnumFacing.Plane.HORIZONTAL.random(random);
		BlockPos entrance = findTunnelEntrance(floorPos, roomFloors, dir);
		double angle = StrictMath.atan2(dir.getZOffset(), dir.getXOffset());
		int length = TUNNEL_MIN_LENGTH + random.nextInt(TUNNEL_EXTRA_LENGTH);

		Set<Long> carved = new HashSet<>();
		List<TunnelStep> trail = new ArrayList<>();
		BlockPos end = walkCave(world, random, entrance, angle, length, carved, trail, true);

		int branches = random.nextInt(TUNNEL_MAX_BRANCHES + 1);
		int mainSteps = trail.size();
		for (int i = 0; i < branches && mainSteps > TUNNEL_BRANCH_MIN_STEP + 6; i++) {
			TunnelStep from = trail.get(TUNNEL_BRANCH_MIN_STEP + random.nextInt(mainSteps - TUNNEL_BRANCH_MIN_STEP - 4));
			double side = random.nextBoolean() ? 1.0D : -1.0D;
			double branchAngle = from.angle + side * Math.toRadians(45 + random.nextInt(30));
			walkCave(world, random, from.floor, branchAngle, 10 + random.nextInt(14), carved, new ArrayList<>(), false);
		}
		return end;
	}

	private BlockPos walkCave(World world, Random random, BlockPos start, double startAngle, int length,
							  Set<Long> carved, List<TunnelStep> trail, boolean stopOnCave) {
		double x = start.getX();
		double z = start.getZ();
		double floorY = start.getY();
		int lowY = Math.max(TUNNEL_MIN_Y, start.getY() - TUNNEL_MAX_DRIFT);
		int highY = start.getY() + TUNNEL_MAX_DRIFT;
		double angle = startAngle;
		double reference = startAngle;
		double slope = 0.0D;
		float radius = TUNNEL_MIN_RADIUS + random.nextFloat() * (TUNNEL_MAX_RADIUS - TUNNEL_MIN_RADIUS);
		float targetRadius = radius;
		int untilResize = 4 + random.nextInt(8);
		BlockPos end = start;

		for (int step = 0; step < length; step++) {
			if (--untilResize <= 0) {
				untilResize = 4 + random.nextInt(9);
				// Now and then the cave opens into a chamber; otherwise it just breathes in and out.
				targetRadius = random.nextInt(6) == 0
						? TUNNEL_MAX_RADIUS + random.nextFloat() * (TUNNEL_CHAMBER_RADIUS - TUNNEL_MAX_RADIUS)
						: TUNNEL_MIN_RADIUS + random.nextFloat() * (TUNNEL_MAX_RADIUS - TUNNEL_MIN_RADIUS);
			}
			radius += Math.max(-0.2F, Math.min(0.2F, targetRadius - radius));
			int height = Math.max(3, Math.min(5, Math.round(radius * 1.5F + 0.5F)));

			angle += (random.nextDouble() - 0.5D) * 0.6D;
			angle = reference + Math.max(-TUNNEL_TURN_LIMIT, Math.min(TUNNEL_TURN_LIMIT, angle - reference));
			slope = Math.max(-0.4D, Math.min(0.4D, slope + (random.nextDouble() - 0.5D) * 0.3D));
			double nextY = floorY + slope;
			if (nextY < lowY || nextY > highY) {
				nextY = Math.max(lowY, Math.min(highY, nextY));
				slope = -slope;
			}

			// Prefer the current heading; if the slice there would hit liquid, bedrock or unloaded terrain,
			// swing away from it (and re-centre the heading cone on the new course).
			double chosen = Double.NaN;
			BlockPos next = null;
			double sign = random.nextBoolean() ? 1.0D : -1.0D;
			for (int attempt = 0; attempt <= TUNNEL_STEER_OFFSETS.length * 2; attempt++) {
				double candidate = attempt == 0 ? angle
						: angle + (attempt % 2 == 1 ? sign : -sign) * TUNNEL_STEER_OFFSETS[(attempt - 1) / 2];
				BlockPos base = new BlockPos(MathHelper.floor(x + StrictMath.cos(candidate) + 0.5D),
						(int) Math.round(nextY), MathHelper.floor(z + StrictMath.sin(candidate) + 0.5D));
				if (canCarveBlob(world, base, radius, height)) {
					chosen = candidate;
					next = base;
					break;
				}
			}
			if (next == null) {
				return end;
			}
			if (chosen != angle) {
				angle = chosen;
				reference = chosen;
			}

			x += StrictMath.cos(angle);
			z += StrictMath.sin(angle);
			floorY = nextY;
			end = next;
			trail.add(new TunnelStep(next, angle));

			boolean touchedCave = carveBlob(world, next, radius, height, carved);
			if (stopOnCave && touchedCave && step >= TUNNEL_CONTACT_GRACE) {
				// Keep the reached position as a valid room anchor. Previously this returned null,
				// which discarded the second/third-room roll whenever the tunnel met a natural cave.
				return end;
			}
		}
		return end;
	}

	/**
	 * The cells of a cave slice standing on {@code base}: an ellipsoid of the given radius whose cells run from
	 * one block above the floor up to {@code height}. A margin grows it into the shell around the slice.
	 */
	private static List<BlockPos> blobCells(BlockPos base, float radius, int height, int margin) {
		List<BlockPos> cells = new ArrayList<>();
		double horizontal = radius + margin;
		double vertical = height / 2.0D + margin;
		double centre = (height + 1) / 2.0D;
		int reach = MathHelper.ceil(horizontal);
		for (int dx = -reach; dx <= reach; dx++) {
			for (int dz = -reach; dz <= reach; dz++) {
				double across = (dx * dx + dz * dz) / (horizontal * horizontal);
				for (int dy = 1 - margin; dy <= height + margin; dy++) {
					double up = (dy - centre) / vertical;
					if (across + up * up <= 1.0D) {
						cells.add(base.add(dx, dy, dz));
					}
				}
			}
		}
		return cells;
	}

	/** A slice may only be carved where the whole area is loaded and neither it nor its shell holds liquid or bedrock. */
	private boolean canCarveBlob(World world, BlockPos base, float radius, int height) {
		int reach = MathHelper.ceil(radius) + 1;
		if (!world.isAreaLoaded(base.add(-reach, 0, -reach), base.add(reach, height + 1, reach))) {
			return false;
		}
		for (BlockPos pos : blobCells(base, radius, height, 1)) {
			IBlockState state = world.getBlockState(pos);
			if (state.getBlock() == Blocks.BEDROCK || state.getMaterial().isLiquid()) {
				return false;
			}
		}
		return true;
	}

	/** Clears one cave slice; returns true if it opened onto air that this tunnel hadn't carved itself. */
	private boolean carveBlob(World world, BlockPos base, float radius, int height, Set<Long> carved) {
		boolean touchedAir = false;
		for (BlockPos pos : blobCells(base, radius, height, 0)) {
			if (world.isAirBlock(pos)) {
				touchedAir |= !carved.contains(pos.toLong());
			} else {
				world.setBlockToAir(pos);
			}
			carved.add(pos.toLong());
		}
		return touchedAir;
	}

	/**
	 * Select the outermost floor cell in the passage direction, rather than estimating the room edge
	 * from its rectangular bounds. An elliptical room can be several blocks narrower along one axis;
	 * using its bound radius placed the first tunnel slice in solid rock, disconnected from the nest.
	 */
	static BlockPos findTunnelEntrance(BlockPos fallback, List<BlockPos> roomFloors, EnumFacing direction) {
		BlockPos entrance = fallback;
		int farthest = Integer.MIN_VALUE;
		double closestToCenter = Double.MAX_VALUE;
		for (BlockPos floor : roomFloors) {
			int distance = floor.getX() * direction.getXOffset() + floor.getZ() * direction.getZOffset();
			double offsetFromCenter = floor.distanceSq(fallback);
			if (distance > farthest || (distance == farthest && offsetFromCenter < closestToCenter)) {
				farthest = distance;
				closestToCenter = offsetFromCenter;
				entrance = floor;
			}
		}
		return entrance;
	}

	List<BlockPos> placeCocoons(World world, Random random, List<BlockPos> floors, Block block) {
		int min = Settings.worldgenSettings.minecrawlerLairMinCocoons;
		int max = Math.max(min, Settings.worldgenSettings.minecrawlerLairMaxCocoons);
		int count = min + (max > min ? random.nextInt(max - min + 1) : 0);
		List<BlockPos> placed = new ArrayList<>();
		int surfaceCount = (count + 1) / 2;

		for (BlockPos groundPos : decorationFloors(world, random, floors)) {
			if (placed.size() >= count) {
				break;
			}
			if (!canPlaceFloorDecoration(world, groundPos)) {
				continue;
			}
			// Keep at least half in plain view; remaining cocoons can occupy open pits/wall nooks.
			// If a nook is impossible, use the known valid surface instead of losing that cocoon.
			BlockPos cocoonPos = groundPos.up();
			if (placed.size() >= surfaceCount) {
				float roll = random.nextFloat();
				BlockPos nook = roll < 0.5F ? carveDownwardNook(world, random, groundPos)
						: roll < 0.8F ? carveSidewaysNook(world, random, groundPos) : null;
				if (nook != null) {
					cocoonPos = nook;
				}
			}
			EnumFacing facing = EnumFacing.Plane.HORIZONTAL.random(random);
			if (world.setBlockState(cocoonPos, block.getDefaultState()
					.withProperty(BlockTOFT.FACING, facing).withProperty(BlockTOFT.SNOWY, false), 2)) {
				placed.add(cocoonPos);
			}
		}
		return placed;
	}

	/** Digs from the floor itself, leaving a visible opening and solid support below the cocoon. */
	BlockPos carveDownwardNook(World world, Random random, BlockPos groundPos) {
		int depth = 1 + random.nextInt(2);
		BlockPos bottom = null;
		for (int i = 0; i < depth; i++) {
			BlockPos next = groundPos.down(i);
			if (!canCarveNookBlock(world, next) || !world.isBlockLoaded(next.down())
					|| !world.getBlockState(next.down()).isTopSolid()) {
				break;
			}
			bottom = next;
		}
		if (bottom != null) {
			world.setBlockToAir(groundPos.up());
			for (int y = groundPos.getY(); y >= bottom.getY(); y--) {
				world.setBlockToAir(new BlockPos(groundPos.getX(), y, groundPos.getZ()));
			}
		}
		return bottom;
	}

	private boolean canCarveNookBlock(World world, BlockPos pos) {
		if (!world.isBlockLoaded(pos)) {
			return false;
		}
		IBlockState state = world.getBlockState(pos);
		return state.getBlock() != Blocks.BEDROCK && state.getMaterial().isSolid()
				&& !state.getMaterial().isLiquid() && !state.getBlock().hasTileEntity(state);
	}

	/** Digs a small 1-2 block nook sideways into a wall for a cocoon to sit in. */
	private BlockPos carveSidewaysNook(World world, Random random, BlockPos groundPos) {
		EnumFacing dir = EnumFacing.Plane.HORIZONTAL.random(random);
		int depth = 1 + random.nextInt(2); // 1-2 blocks
		BlockPos cursor = groundPos.up();
		BlockPos lastCarved = null;
		for (int i = 0; i < depth; i++) {
			BlockPos next = cursor.offset(dir);
			if (!canCarveNookBlock(world, next) || !world.isBlockLoaded(next.down())) {
				break;
			}
			if (!world.getBlockState(next.down()).isTopSolid()) {
				break; // no floor under the nook - don't carve out over open space
			}
			world.setBlockToAir(next);
			lastCarved = next;
			cursor = next;
		}
		return lastCarved;
	}

	/** Cocoons sit in a dense knot of wax - the nest's actual egg-clutch, not just ambient hazard dressing. */
	private void decorateAroundCocoon(World world, Random random, BlockPos cocoonPos) {
		BlockPos below = cocoonPos.down();
		if (isCoatableSolid(world, below)) {
			world.setBlockState(below, ModBlocks.crawler_wax_block.getDefaultState(), 2);
		}

		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				if (dx == 0 && dz == 0) {
					continue;
				}

				BlockPos floorNeighbor = below.add(dx, 0, dz);
				if (isCoatableSolid(world, floorNeighbor) && random.nextFloat() < 0.7F) {
					world.setBlockState(floorNeighbor, ModBlocks.crawler_wax_block.getDefaultState(), 2);
				}

				BlockPos openNeighbor = cocoonPos.add(dx, 0, dz);
				if (world.isAirBlock(openNeighbor) && random.nextFloat() < 0.5F) {
					EnumFacing towardSupport = findAttachment(world, openNeighbor);
					world.setBlockState(openNeighbor, ModBlocks.crawler_wax_wet.getDefaultState()
							.withProperty(BlockCrawlerWaxWet.FACING, towardSupport.getOpposite()), 2);
				}
			}
		}

		BlockPos above = cocoonPos.up();
		if (world.isAirBlock(above)) {
			BlockPos ceiling = above.up();
			if (isCoatableSolid(world, ceiling) && random.nextFloat() < 0.6F) {
				world.setBlockState(ceiling, ModBlocks.crawler_wax_block.getDefaultState(), 2);
			}
			if (random.nextFloat() < 0.4F) {
				EnumFacing towardSupport = findAttachment(world, above);
				world.setBlockState(above, ModBlocks.crawler_wax_wet.getDefaultState()
						.withProperty(BlockCrawlerWaxWet.FACING, towardSupport.getOpposite()), 2);
			}
		}
	}

	/**
	 * Finds a direction from pos toward an adjacent solid block, for orienting an attached wet-wax decal.
	 * If nothing solid is actually adjacent (placing into open air, e.g. near a cocoon in the middle of
	 * a big chamber), defaults to claiming support from above rather than skipping the placement - a
	 * crawler spinning webbing across open space would anchor it to the ceiling either way.
	 */
	private EnumFacing findAttachment(World world, BlockPos pos) {
		for (EnumFacing direction : EnumFacing.values()) {
			if (isCoatableSolid(world, pos.offset(direction))) {
				return direction;
			}
		}
		return EnumFacing.UP;
	}

	private boolean isCoatableSolid(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		return state.getBlock() != Blocks.BEDROCK && state.getMaterial().isSolid() && !state.getMaterial().isLiquid();
	}

	private void spawnCrawlers(World world, Random random, List<BlockPos> floors) {
		int min = Settings.worldgenSettings.minecrawlerLairMinCrawlers;
		int max = Math.max(min, Settings.worldgenSettings.minecrawlerLairMaxCrawlers);
		int count = min + (max > min ? random.nextInt(max - min + 1) : 0);

		List<BlockPos> candidates = new ArrayList<>(floors);
		Collections.shuffle(candidates, random);
		int spawned = 0;
		for (BlockPos groundPos : candidates) {
			if (spawned >= count) {
				break;
			}
			EntityMinecrawler minecrawler = new EntityMinecrawler(world);
			minecrawler.setLocationAndAngles(groundPos.getX() + 0.5D, groundPos.getY() + 1, groundPos.getZ() + 0.5D, random.nextFloat() * 360F, 0F);
			if (!canSpawnCrawler(world, groundPos, minecrawler.getEntityBoundingBox())) {
				continue;
			}
			minecrawler.enablePersistence();
			if (world.spawnEntity(minecrawler)) {
				spawned++;
			}
		}
	}

	static boolean canSpawnCrawler(World world, BlockPos groundPos, AxisAlignedBB body) {
		// Collision queries inspect neighboring blocks too. Do not load terrain during population.
		if (!world.isAreaLoaded(new BlockPos(body.minX - 1, body.minY - 1, body.minZ - 1),
				new BlockPos(body.maxX + 1, body.maxY + 1, body.maxZ + 1))) {
			return false;
		}
		return world.getBlockState(groundPos).isTopSolid()
				&& world.getCollisionBoxes(null, body).isEmpty()
				&& !world.containsAnyLiquid(body)
				&& world.checkNoEntityCollision(body);
	}

	/** Independent decoration rolls per inhabited room, using only its actual carved floor cells. */
	private void decorateExtras(World world, Random random, List<BlockPos> floors) {
		if (random.nextFloat() < MUSHROOM_CLUSTER_CHANCE) {
			placeMushroomCluster(world, random, floors);
		}
		if (random.nextFloat() < SKELETON_CRATE_CHANCE) {
			placeLootProp(world, random, floors, ModBlocks.skeleton_crate);
		}
		if (random.nextFloat() < LOST_CRATE_POTIONS_CHANCE) {
			placeLootProp(world, random, floors, ModBlocks.lost_crate_potions);
		}

	}

	void placeSkeletons(World world, Random random, List<BlockPos> floors, boolean mainRoom, BlockSkeletonCorpse block) {
		float chance = mainRoom ? MAIN_ROOM_SKELETON_CHANCE : SIDE_ROOM_SKELETON_CHANCE;
		if (random.nextFloat() >= chance) {
			return;
		}
		int corpseCount = 1;
		while (corpseCount < SKELETON_CORPSE_MAX && random.nextFloat() < SKELETON_CORPSE_EXTRA_CHANCE) {
			corpseCount++;
		}
		for (int i = 0; i < corpseCount; i++) {
			placeSkeletonCorpse(world, random, floors, block);
		}
	}

	/** Wet wax is surface dressing and may give way to a prop; cocoons and other blocks may not. */
	private boolean canPlaceFloorDecoration(World world, BlockPos groundPos) {
		BlockPos placePos = groundPos.up();
		if (!world.isBlockLoaded(groundPos) || !world.isBlockLoaded(placePos)) {
			return false;
		}
		return world.getBlockState(groundPos).isTopSolid()
				&& (world.isAirBlock(placePos) || world.getBlockState(placePos).getBlock() instanceof BlockCrawlerWaxWet);
	}

	private List<BlockPos> decorationFloors(World world, Random random, List<BlockPos> floors) {
		List<BlockPos> candidates = new ArrayList<>();
		for (BlockPos groundPos : floors) {
			if (canPlaceFloorDecoration(world, groundPos)) {
				candidates.add(groundPos);
			}
		}
		Collections.shuffle(candidates, random);
		return candidates;
	}

	void placeMushroomCluster(World world, Random random, List<BlockPos> floors) {
		List<BlockPos> candidates = decorationFloors(world, random, floors);
		candidates.removeIf(groundPos -> !Blocks.BROWN_MUSHROOM.canBlockStay(world, groundPos.up(), Blocks.BROWN_MUSHROOM.getDefaultState()));
		if (candidates.isEmpty()) {
			return;
		}
		BlockPos anchor = candidates.get(0);
		// Start at a known valid cell, then use distinct nearby cells. Repeated random offsets used
		// to hit wax, walls, or the same mushroom repeatedly and could yield an entirely empty cluster.
		candidates.sort(Comparator.comparingDouble(groundPos -> groundPos.distanceSq(anchor)));
		int remaining = 4 + random.nextInt(5);
		for (BlockPos groundPos : candidates) {
			if (Math.abs(groundPos.getX() - anchor.getX()) <= 2 && Math.abs(groundPos.getZ() - anchor.getZ()) <= 2
					&& world.setBlockState(groundPos.up(), Blocks.BROWN_MUSHROOM.getDefaultState(), 2) && --remaining == 0) {
				break;
			}
		}
	}

	/** Places a single BlockTOFT-style loot prop (skeleton_crate, lost_crate_potions, ...) on the floor. */
	private void placeLootProp(World world, Random random, List<BlockPos> floors, Block block) {
		for (BlockPos groundPos : decorationFloors(world, random, floors)) {
			BlockPos placePos = groundPos.up();
			EnumFacing facing = EnumFacing.Plane.HORIZONTAL.random(random);
			boolean isSnowy = world.getBiome(placePos).isSnowyBiome();
			if (world.setBlockState(placePos, block.getDefaultState()
					.withProperty(BlockTOFT.FACING, facing).withProperty(BlockTOFT.SNOWY, isSnowy), 2)) {
				return;
			}
		}
	}

	/**
	 * Places a totf:skeleton_corpse on the floor - leaning against a wall in the "sit" pose if one is
	 * immediately adjacent, or lying flat in the "ground" pose with a random fine rotation otherwise.
	 * Mirrors ComponentNbtPiece#placeSkeletonCorpse's baked-held-item handling for worldgen placement.
	 */
	void placeSkeletonCorpse(World world, Random random, List<BlockPos> floors, BlockSkeletonCorpse block) {
		for (BlockPos groundPos : decorationFloors(world, random, floors)) {
			BlockPos placePos = groundPos.up();

			// Like a torch, FACING points away from its support, so a wall found in direction `wallSide`
			// means the corpse must face the opposite way to lean back against it.
			EnumFacing wallSide = null;
			for (EnumFacing horizontal : EnumFacing.Plane.HORIZONTAL) {
				if (isCoatableSolid(world, placePos.offset(horizontal))) {
					wallSide = horizontal;
					break;
				}
			}

			IBlockState state;
			int fineRotation = 0;
			if (wallSide != null) {
				state = block.getDefaultState().withProperty(BlockSkeletonCorpse.POSE, BlockSkeletonCorpse.Pose.SIT)
						.withProperty(BlockSkeletonCorpse.FACING, wallSide.getOpposite());
			} else {
				state = block.getDefaultState().withProperty(BlockSkeletonCorpse.POSE, BlockSkeletonCorpse.Pose.GROUND);
				fineRotation = random.nextInt(16);
			}

			if (!world.setBlockState(placePos, state, 2)) {
				continue;
			}
			TileEntity te = world.getTileEntity(placePos);
			if (te instanceof TileEntitySkeletonCorpse) {
				TileEntitySkeletonCorpse corpse = (TileEntitySkeletonCorpse) te;
				corpse.setRotation(fineRotation);
				corpse.setHeldItem(block.rollHeldItem(world, random));
			}
			return;
		}
	}
}
