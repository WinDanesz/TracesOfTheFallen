package com.windanesz.tracesofthefallen.world;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.block.BlockDecoration;
import com.windanesz.tracesofthefallen.block.BlockLampBase;
import com.windanesz.tracesofthefallen.block.TileEntityBalance;
import com.windanesz.tracesofthefallen.entity.living.EntitySubterfuge;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import com.windanesz.tracesofthefallen.world.shop.ShopTrades;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.IWorldGenerator;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A Subterfuge's shop: a small brick room dug into the ground, reached by a flight of stairs from the surface,
 * with her behind a row of pedestals that each hold a balance offering one trinket for Tallies.
 * <p>
 * Built whole inside the 16x16 population area of one chunk, so it needs no multi-chunk bookkeeping. The size,
 * stone and pedestal layout vary per shop; the shop is anchored to the keeper entity, which holds its bounds.
 */
public class WorldGenSubterfugeShop implements IWorldGenerator {

	private static final long SEED_MODIFIER = 0x5B1F7CA4D3E29L;
	private static final int ATTEMPTS = 6;
	/** Edge of the area population may write to: a chunk's own 16x16 offset by 8 into its neighbours. */
	private static final int REGION = 16;
	/** How far the highest and lowest ground under the shop may differ. */
	private static final int MAX_TERRAIN_SPREAD = 6;
	/** How far the stair entrance may sit above the lowest ground; this sets the stair length. */
	private static final int MAX_ENTRANCE_RISE = 3;
	private static final int MIN_FLOOR_Y = 8;
	private static final int MAX_PEDESTALS_PER_SIDE = 4;

	private static final IBlockState AIR = Blocks.AIR.getDefaultState();

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		int frequency = Settings.worldgenSettings.subterfugeShopFrequency;
		if (frequency <= 0 || !ArrayUtils.contains(Settings.worldgenSettings.dimensionList, world.provider.getDimension())) {
			return;
		}

		Random shopRandom = new Random(world.getSeed() ^ ((long) chunkX * 341873128712L) ^ ((long) chunkZ * 132897987541L) ^ SEED_MODIFIER);
		if (shopRandom.nextInt(frequency) != 0) {
			return;
		}
		for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
			if (tryBuild(world, shopRandom, chunkX, chunkZ)) {
				return;
			}
		}
	}

	private boolean tryBuild(World world, Random random, int chunkX, int chunkZ) {
		EnumFacing inward = EnumFacing.Plane.HORIZONTAL.random(random);
		int width = 5 + 2 * random.nextInt(2);
		int height = 3 + random.nextInt(2);
		int outerWidth = width + 2;
		int centre = outerWidth / 2;

		boolean alongX = inward.getAxis() == EnumFacing.Axis.X;
		int sizeX = alongX ? REGION : outerWidth;
		int sizeZ = alongX ? outerWidth : REGION;
		int minX = chunkX * 16 + 8 + random.nextInt(REGION - sizeX + 1);
		int minZ = chunkZ * 16 + 8 + random.nextInt(REGION - sizeZ + 1);
		Frame frame = new Frame(inward, minX, minZ, outerWidth, REGION);

		// Survey the ground under the whole area: the stairs start at the entrance column, and the room goes
		// in far enough below the lowest ground that its roof is buried everywhere.
		int[][] surface = new int[outerWidth][REGION];
		int lowest = Integer.MAX_VALUE;
		int highest = Integer.MIN_VALUE;
		for (int lx = 0; lx < outerWidth; lx++) {
			for (int lz = 0; lz < REGION; lz++) {
				BlockPos column = frame.at(lx, 0, lz);
				Integer y = surfaceY(world, column.getX(), column.getZ());
				if (y == null) {
					return false;
				}
				surface[lx][lz] = y;
				lowest = Math.min(lowest, y);
				highest = Math.max(highest, y);
			}
		}
		int entrance = surface[centre][0];
		if (highest - lowest > MAX_TERRAIN_SPREAD || entrance - lowest > MAX_ENTRANCE_RISE) {
			return false;
		}
		int floorY = lowest - height - 2;
		if (floorY < MIN_FLOOR_Y || isBadBiome(world.getBiome(frame.at(centre, 0, REGION / 2)))) {
			return false;
		}
		frame.floorY = floorY;

		int stairs = entrance - floorY;
		int roomLength = stairs + 9 <= REGION && random.nextBoolean() ? 7 : 5;
		int totalLength = stairs + roomLength + 2;
		if (totalLength > REGION) {
			return false;
		}
		int roomStart = stairs;
		int roomEnd = stairs + roomLength + 1;
		int keeperRow = roomEnd - 1;
		Theme theme = Theme.values()[random.nextInt(Theme.values().length)];

		buildRoom(world, random, frame, theme, outerWidth, height, centre, roomStart, roomEnd);
		buildStairs(world, random, frame, theme, surface, centre, stairs, entrance);

		List<BlockPos> balances = placePedestals(world, random, frame, theme, width, height, roomStart, roomEnd);
		placeLighting(world, random, frame, width, height, centre, roomStart, roomEnd, keeperRow);
		stockBalances(world, random, balances);

		BlockPos cornerA = frame.at(0, 0, 0);
		BlockPos cornerB = frame.at(outerWidth - 1, 0, totalLength - 1);
		StructureBoundingBox bounds = new StructureBoundingBox(
				Math.min(cornerA.getX(), cornerB.getX()), floorY - 1, Math.min(cornerA.getZ(), cornerB.getZ()),
				Math.max(cornerA.getX(), cornerB.getX()), entrance + 4, Math.max(cornerA.getZ(), cornerB.getZ()));
		spawnKeeper(world, random, frame, bounds, balances, centre, keeperRow);

		TracesOfTheFallen.LOGGER.info("Generated a Subterfuge shop near {}", frame.at(centre, 0, roomStart));
		announceShop(world, frame.at(centre, 1, keeperRow));
		return true;
	}

	/** Debug: tells every player where a shop just generated, with a click-to-suggest teleport command. */
	private static void announceShop(World world, BlockPos keeperPos) {
		MinecraftServer server = world.getMinecraftServer();
		if (server == null) {
			return;
		}
		String command = String.format("/tp %d %d %d", keeperPos.getX(), keeperPos.getY(), keeperPos.getZ());
		ITextComponent message = new TextComponentString("[TotF] Subterfuge shop generated (dim " + world.provider.getDimension() + "): " + command);
		message.getStyle().setColor(TextFormatting.LIGHT_PURPLE)
				.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
		server.getPlayerList().sendMessage(message);
	}

	// ---------------------------------------------------------------------------------------------------
	// Terrain
	// ---------------------------------------------------------------------------------------------------

	/** Y of the ground at a column, or null if it is water, a tree, a building or anything else not worth digging into. */
	@Nullable
	private static Integer surfaceY(World world, int x, int z) {
		BlockPos ground = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).down();
		if (ground.getY() < 1) {
			return null;
		}
		// getTopSolidOrLiquidBlock only stops at blocks that block movement, so it sees the bed under water.
		if (world.getBlockState(ground.up()).getMaterial().isLiquid()) {
			return null;
		}
		Material material = world.getBlockState(ground).getMaterial();
		boolean diggable = material == Material.GRASS || material == Material.GROUND || material == Material.SAND
				|| material == Material.ROCK || material == Material.CLAY || material == Material.CRAFTED_SNOW;
		return diggable ? ground.getY() : null;
	}

	private static boolean isBadBiome(Biome biome) {
		return BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN)
				|| BiomeDictionary.hasType(biome, BiomeDictionary.Type.RIVER)
				|| BiomeDictionary.hasType(biome, BiomeDictionary.Type.BEACH);
	}

	// ---------------------------------------------------------------------------------------------------
	// Building
	// ---------------------------------------------------------------------------------------------------

	/** The shell of the room: floor, ceiling and walls, with the interior hollowed out and a doorway in the front wall. */
	private void buildRoom(World world, Random random, Frame frame, Theme theme, int outerWidth, int height, int centre, int roomStart, int roomEnd) {
		for (int lx = 0; lx < outerWidth; lx++) {
			for (int lz = roomStart; lz <= roomEnd; lz++) {
				for (int ly = 0; ly <= height + 1; ly++) {
					IBlockState state;
					if (ly == 0) {
						state = theme.floor(random);
					} else if (ly == height + 1) {
						state = theme.wall(random);
					} else if (lx == 0 || lx == outerWidth - 1 || lz == roomStart || lz == roomEnd) {
						// Full interior height: the last step lifts a player's head well above a normal doorway.
						boolean doorway = lx == centre && lz == roomStart;
						state = doorway ? AIR : theme.wall(random);
					} else {
						state = AIR;
					}
					world.setBlockState(frame.at(lx, ly, lz), state, 2);
				}
			}
		}
	}

	/** A one-wide flight of stairs from the surface down to the doorway, lined with brick and marked by two lit pillars. */
	private void buildStairs(World world, Random random, Frame frame, Theme theme, int[][] surface, int centre, int stairs, int entrance) {
		IBlockState stair = ModBlocks.bricks_stone_stair.getDefaultState().withProperty(BlockStairs.FACING, frame.inward.getOpposite());
		for (int step = 0; step < stairs; step++) {
			int ly = stairs - step;
			int stairY = frame.floorY + ly;

			world.setBlockState(frame.at(centre, ly - 1, step), theme.wall(random), 2);
			world.setBlockState(frame.at(centre, ly, step), stair, 2);
			// Open to the sky, and clear of whatever grew over it.
			for (int y = stairY + 1; y <= Math.max(surface[centre][step] + 3, stairY + 3); y++) {
				BlockPos pos = frame.at(centre, y - frame.floorY, step);
				if (!world.isAirBlock(pos)) {
					world.setBlockState(pos, AIR, 2);
				}
			}

			for (int side = centre - 1; side <= centre + 1; side += 2) {
				int wallTop = Math.max(surface[side][step], stairY + 2);
				for (int y = stairY - 1; y <= wallTop; y++) {
					world.setBlockState(frame.at(side, y - frame.floorY, step), theme.wall(random), 2);
				}
				if (step == 0) {
					int pillarTop = Math.max(wallTop, entrance + 2);
					for (int y = wallTop + 1; y <= pillarTop; y++) {
						world.setBlockState(frame.at(side, y - frame.floorY, 0), ModBlocks.bricks_stone_pillar.getDefaultState(), 2);
					}
					world.setBlockState(frame.at(side, pillarTop + 1 - frame.floorY, 0),
							ModBlocks.subtorch.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.UP), 2);
				}
			}
		}
	}

	/** Pedestals along both side walls, each carrying a balance that faces the aisle. Returns the balance positions. */
	private List<BlockPos> placePedestals(World world, Random random, Frame frame, Theme theme, int width, int height, int roomStart, int roomEnd) {
		List<BlockPos> balances = new ArrayList<>();
		int tallestPedestal = height - 2;
		boolean tallNearDoor = random.nextBoolean();

		for (int side = 0; side < 2; side++) {
			int lx = side == 0 ? 1 : width;
			EnumFacing facing = side == 0 ? frame.right : frame.right.getOpposite();

			// Balances are wider than their block, so two in neighbouring rows overlap: always leave a gap row.
			// Picking c rows from (rows - (c - 1)) slots and spreading them out gives every spaced layout equal odds.
			int firstRow = roomStart + 1;
			int rowCount = roomEnd - 2 - firstRow + 1;
			int maxCount = Math.min(MAX_PEDESTALS_PER_SIDE, (rowCount + 1) / 2);
			int count = 2 + random.nextInt(Math.max(1, maxCount - 1));
			List<Integer> slots = new ArrayList<>();
			for (int slot = 0; slot < rowCount - (count - 1); slot++) {
				slots.add(slot);
			}
			Collections.shuffle(slots, random);
			List<Integer> picked = new ArrayList<>(slots.subList(0, count));
			Collections.sort(picked);
			List<Integer> chosen = new ArrayList<>();
			for (int index = 0; index < picked.size(); index++) {
				chosen.add(firstRow + picked.get(index) + index);
			}

			for (int index = 0; index < chosen.size(); index++) {
				int lz = chosen.get(index);
				int pedestalHeight = 1;
				if (tallestPedestal > 1) {
					boolean nearDoorHalf = index < (chosen.size() + 1) / 2;
					pedestalHeight = nearDoorHalf == tallNearDoor ? tallestPedestal : 1;
				}
				for (int ly = 1; ly <= pedestalHeight; ly++) {
					world.setBlockState(frame.at(lx, ly, lz), theme.carved(random), 2);
				}
				BlockPos pos = frame.at(lx, pedestalHeight + 1, lz);
				world.setBlockState(pos, ModBlocks.balance.getDefaultState().withProperty(BlockDecoration.FACING, facing), 2);
				balances.add(pos);
			}

			// Her bell, on a low pedestal in the corner nearest the keeper.
			if (random.nextInt(3) != 0) {
				int lz = roomEnd - 1;
				world.setBlockState(frame.at(lx, 1, lz), theme.carved(random), 2);
				world.setBlockState(frame.at(lx, 2, lz), ModBlocks.desk_bell.getDefaultState().withProperty(BlockDecoration.FACING, facing), 2);
			}
		}
		return balances;
	}

	private void placeLighting(World world, Random random, Frame frame, int width, int height, int centre, int roomStart, int roomEnd, int keeperRow) {
		// Purple flames either side of her, on the back wall.
		IBlockState backTorch = ModBlocks.subtorch.getDefaultState().withProperty(BlockTorch.FACING, frame.inward.getOpposite());
		int offset = width == 7 ? 2 : 1;
		world.setBlockState(frame.at(centre - offset, 3, keeperRow), backTorch, 2);
		world.setBlockState(frame.at(centre + offset, 3, keeperRow), backTorch, 2);

		// Sometimes another pair by the door.
		if (random.nextBoolean()) {
			IBlockState doorTorch = ModBlocks.subtorch.getDefaultState().withProperty(BlockTorch.FACING, frame.inward);
			world.setBlockState(frame.at(centre - 1, 3, roomStart + 1), doorTorch, 2);
			world.setBlockState(frame.at(centre + 1, 3, roomStart + 1), doorTorch, 2);
		}

		// One or two lamps hanging over the aisle.
		IBlockState lamp = ModBlocks.wrought_caged_lamp.getDefaultState().withProperty(BlockLampBase.FACING, EnumFacing.DOWN);
		List<Integer> rows = new ArrayList<>();
		for (int lz = roomStart + 2; lz <= roomEnd - 2; lz++) {
			rows.add(lz);
		}
		Collections.shuffle(rows, random);
		int lamps = 1 + random.nextInt(2);
		for (int index = 0; index < lamps && index < rows.size(); index++) {
			world.setBlockState(frame.at(centre, height, rows.get(index)), lamp, 2);
		}
	}

	/** Fills each pedestal with a different trade; any left over (a short trade list) restock as soon as they can. */
	private void stockBalances(World world, Random random, List<BlockPos> balances) {
		List<ShopTrades.Trade> trades = ShopTrades.roll(random, balances.size());
		for (int index = 0; index < balances.size(); index++) {
			TileEntity tileEntity = world.getTileEntity(balances.get(index));
			if (!(tileEntity instanceof TileEntityBalance)) {
				continue;
			}
			TileEntityBalance balance = (TileEntityBalance) tileEntity;
			if (index < trades.size()) {
				ShopTrades.stock(balance, trades.get(index), false);
			} else {
				balance.setShopStock(ItemStack.EMPTY, ItemStack.EMPTY, false);
				balance.setRestockAt(1L);
			}
		}
	}

	private void spawnKeeper(World world, Random random, Frame frame, StructureBoundingBox bounds, List<BlockPos> balances, int centre, int keeperRow) {
		BlockPos feet = frame.at(centre, 1, keeperRow);
		float yaw = frame.inward.getOpposite().getHorizontalAngle();

		long seed = random.nextLong();
		EntitySubterfuge keeper = new EntitySubterfuge(world);
		keeper.setLocationAndAngles(feet.getX() + 0.5D, feet.getY(), feet.getZ() + 0.5D, yaw, 0.0F);
		keeper.rotationYawHead = yaw;
		keeper.renderYawOffset = yaw;
		keeper.setShop(bounds, balances, seed);
		keeper.setHiddenState(!EntitySubterfuge.isPresentDuring(seed, world.getTotalWorldTime()));
		world.spawnEntity(keeper);
	}

	// ---------------------------------------------------------------------------------------------------
	// Layout helpers
	// ---------------------------------------------------------------------------------------------------

	/** How worn the shop's stonework looks. Mostly clean: even the worn themes only sprinkle in a little grime or moss. */
	private enum Theme {
		CLEAN, WORN, MOSSY;

		IBlockState wall(Random random) {
			switch (this) {
				case WORN:
					return (random.nextInt(6) == 0 ? ModBlocks.bricks_stone_dirty : ModBlocks.bricks_stone).getDefaultState();
				case MOSSY:
					return (random.nextInt(5) == 0 ? ModBlocks.bricks_stone_mossy : ModBlocks.bricks_stone).getDefaultState();
				default:
					return (random.nextInt(16) == 0 ? ModBlocks.bricks_stone_dirty : ModBlocks.bricks_stone).getDefaultState();
			}
		}

		IBlockState floor(Random random) {
			return (random.nextInt(20) == 0 ? ModBlocks.bricks_stone_dirty : ModBlocks.bricks_stone_smooth).getDefaultState();
		}

		IBlockState carved(Random random) {
			switch (this) {
				case WORN:
					return (random.nextInt(6) == 0 ? ModBlocks.bricks_stone_carved_dirty : ModBlocks.bricks_stone_carved).getDefaultState();
				case MOSSY:
					return (random.nextInt(4) == 0 ? ModBlocks.bricks_stone_carved_mossy : ModBlocks.bricks_stone_carved).getDefaultState();
				default:
					return ModBlocks.bricks_stone_carved.getDefaultState();
			}
		}
	}

	/**
	 * Maps the shop's local layout onto the world. Local x runs across the room, local z runs from the top of
	 * the stairs (0) toward the back wall, and local y is the height above the room's floor.
	 */
	private static final class Frame {
		final EnumFacing inward;
		final EnumFacing right;
		final int originX;
		final int originZ;
		int floorY;

		Frame(EnumFacing inward, int minX, int minZ, int width, int length) {
			this.inward = inward;
			this.right = inward.rotateY();
			int minRawX = Integer.MAX_VALUE;
			int minRawZ = Integer.MAX_VALUE;
			for (int lx : new int[] {0, width - 1}) {
				for (int lz : new int[] {0, length - 1}) {
					minRawX = Math.min(minRawX, right.getXOffset() * lx + inward.getXOffset() * lz);
					minRawZ = Math.min(minRawZ, right.getZOffset() * lx + inward.getZOffset() * lz);
				}
			}
			// Slide the layout so its lowest corner lands on the chosen corner of the area.
			this.originX = minX - minRawX;
			this.originZ = minZ - minRawZ;
		}

		BlockPos at(int lx, int ly, int lz) {
			return new BlockPos(
					originX + right.getXOffset() * lx + inward.getXOffset() * lz,
					floorY + ly,
					originZ + right.getZOffset() * lx + inward.getZOffset() * lz);
		}
	}
}
