package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraft.world.gen.ChunkProviderServer;

import javax.annotation.Nullable;
import java.util.Random;

public class TileEntityStoneCircle extends TileEntity implements ITickable {

	private BlockPos pair = null;
	private int channelProgress = 0;

	private boolean pairingComplete = false;
	private ForgeChunkManager.Ticket chunkTicket = null;
	private BlockPos pendingTwinLocation = null;
	private int generationCooldown = 0; // Kept for NBT compatibility, no longer used in logic

	@Override
	public void update() {
		if (world.isRemote || this.pairingComplete || getPair() != null) {
			return;
		}

		// We only do the pairing attempt once; it's now synchronous when we get a ticket.
		if (this.pendingTwinLocation == null) {
			if (!findAndForceLoadTwinChunk()) {
				// Could not get a ticket, so abort pairing for now.
				this.pairingComplete = false;
			}
		}
	}

	/**
	 * Picks a random far location, forces the altar chunk + neighbors to generate & populate,
	 * then immediately tries to generate the twin altar.
	 */
	private boolean findAndForceLoadTwinChunk() {
		if (!(world instanceof WorldServer)) {
			TracesOfTheFallen.LOGGER.error("Stone Circle TE in non-WorldServer world at {}. Cannot force-generate chunks.", this.pos);
			return false;
		}

		Random random = world.rand;
		int minDistance = 1000;
		int maxDistance = 5000;

		double angle = random.nextDouble() * 2 * Math.PI;
		int distance = minDistance + random.nextInt(maxDistance - minDistance);
		int x = this.pos.getX() + (int) (Math.cos(angle) * distance);
		int z = this.pos.getZ() + (int) (Math.sin(angle) * distance);

		this.pendingTwinLocation = new BlockPos(x, 0, z);

		this.chunkTicket = ForgeChunkManager.requestTicket(TracesOfTheFallen.instance, world, ForgeChunkManager.Type.NORMAL);
		if (this.chunkTicket == null) {
			TracesOfTheFallen.LOGGER.error("Could not obtain chunk loading ticket for Stone Circle at {}. Pairing aborted.", this.pos);
			this.pendingTwinLocation = null;
			return false;
		}

		int chunkX = x >> 4;
		int chunkZ = z >> 4;
		ChunkPos centerChunk = new ChunkPos(chunkX, chunkZ);

		// Keep the center chunk loaded while we do the work.
		ForgeChunkManager.forceChunk(this.chunkTicket, centerChunk);

		// Make sure the altar chunk and a small ring of neighbors are fully generated + populated.
		forceGenerateAndPopulateArea((WorldServer) world, chunkX, chunkZ, 1); // radius 1 = 3x3 chunks

		// Now that worldgen/deco is finished in this area, safely place the altar.
		attemptGenerateTwinAndReleaseTicket();
		cleanupAfterPairingAttempt();

		return true;
	}

	/**
	 * Synchronously generates & populates the center chunk and its neighbors in a small radius.
	 * This ensures no later tree/grass decoration will clip into the altar area.
	 */
	private void forceGenerateAndPopulateArea(WorldServer ws, int centerChunkX, int centerChunkZ, int radius) {
		ChunkProviderServer provider = ws.getChunkProvider();

		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				int cx = centerChunkX + dx;
				int cz = centerChunkZ + dz;
				// This call synchronously loads/generates and populates the chunk.
				provider.provideChunk(cx, cz);
			}
		}
	}

	private void cleanupAfterPairingAttempt() {
		if (this.chunkTicket != null) {
			ForgeChunkManager.releaseTicket(this.chunkTicket);
		}
		this.chunkTicket = null;
		this.pendingTwinLocation = null;
		this.generationCooldown = 0;
		markDirty();
	}

	private BlockPos findSurface(World world, int x, int z) {
		BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(x, world.getHeight() - 1, z);

		while (mutablePos.getY() > 0) {
			IBlockState state = world.getBlockState(mutablePos);
			if (state.getMaterial().isSolid() && state.getMaterial().blocksMovement()
					&& !state.getBlock().isLeaves(state, world, mutablePos)) {
				return mutablePos.toImmutable();
			}
			mutablePos.setY(mutablePos.getY() - 1);
		}
		return null;
	}

	private void attemptGenerateTwinAndReleaseTicket() {
		if (this.pendingTwinLocation != null) {

			BlockPos finalAltarPos = null;
			int searchRadius = 16;

			// FIRST: find the actual altar spot
			for (int dx = -searchRadius; dx <= searchRadius; dx++) {
				for (int dz = -searchRadius; dz <= searchRadius; dz++) {
					int currentX = this.pendingTwinLocation.getX() + dx;
					int currentZ = this.pendingTwinLocation.getZ() + dz;

					BlockPos groundPos = findSurface(world, currentX, currentZ);

					if (groundPos != null && canGenerateAltarAt(world, groundPos)) {
						finalAltarPos = groundPos.up();
						break;
					}
				}
				if (finalAltarPos != null) break;
			}

			if (finalAltarPos != null && world instanceof WorldServer) {

				WorldServer ws = (WorldServer) world;

				int altarChunkX = finalAltarPos.getX() >> 4;
				int altarChunkZ = finalAltarPos.getZ() >> 4;

				// keep altar chunk loaded
				ForgeChunkManager.forceChunk(this.chunkTicket, new ChunkPos(altarChunkX, altarChunkZ));

				// NOW: Pre-generate and populate a 5x5 area around the altar
				forceGenerateAndPopulateArea(ws, altarChunkX, altarChunkZ, 2);
			}

			// after pregen, safe to place altar
			if (finalAltarPos != null) {
				generateTwinAltar(world, finalAltarPos);

				TileEntity otherTe = world.getTileEntity(finalAltarPos);
				if (otherTe instanceof TileEntityStoneCircle) {
					TileEntityStoneCircle otherStoneCircle = (TileEntityStoneCircle) otherTe;
					this.setPair(finalAltarPos);
					otherStoneCircle.setPair(this.pos);
					otherStoneCircle.pairingComplete = true;
					otherStoneCircle.markDirty();
				}
			}
		}

		cleanupAfterPairingAttempt();
	}


	private void generateTwinAltar(World world, BlockPos newPos) {
		clearAndPlaceSimpleAltar(world, newPos);
	}

	private void clearAndPlaceSimpleAltar(World world, BlockPos center) {
		for (int x = -1; x <= 1; x++) {
			for (int y = 0; y <= 2; y++) {
				for (int z = -1; z <= 1; z++) {
					BlockPos pos = center.add(x, y, z);
					if (x == 0 && y == 0 && z == 0) {
						continue;
					}
					world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
				}
			}
		}
		placeAltarBlock(world, center);
	}

	private void placeAltarBlock(World world, BlockPos pos) {
		Block block = world.getBlockState(this.pos).getBlock();
		if (block instanceof BlockStoneCircle) {
			IBlockState state = block.getDefaultState()
					.withProperty(BlockStoneCircle.SNOWY, world.getBiome(pos).isSnowyBiome());
			world.setBlockState(pos, state, 2);
		}
	}

	private boolean isBlockSoft(IBlockState state, World world, BlockPos pos) {
		Block block = state.getBlock();
		Material mat = state.getMaterial();

		if (mat.isLiquid()) {
			return false;
		}
		if (mat == Material.GROUND || mat == Material.GRASS || mat == Material.SAND
				|| mat == Material.CLAY || mat == Material.CRAFTED_SNOW || mat == Material.SNOW) {
			return true;
		}
		return block.isAir(state, world, pos)
				|| block.isReplaceable(world, pos)
				|| mat.isReplaceable()
				|| block instanceof net.minecraft.block.BlockBush
				|| block instanceof net.minecraft.block.BlockLeaves
				|| block instanceof net.minecraft.block.BlockVine;
	}

	private boolean canGenerateAltarAt(World world, BlockPos pos) {
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				BlockPos groundCheckPos = pos.add(x, 0, z);
				IBlockState groundState = world.getBlockState(groundCheckPos);
				if (!groundState.getMaterial().isSolid() || groundState.getMaterial().isLiquid()) {
					return false;
				}
				for (int y = 1; y <= 2; y++) {
					BlockPos spaceCheckPos = groundCheckPos.add(0, y, 0);
					if (!isBlockSoft(world.getBlockState(spaceCheckPos), world, spaceCheckPos)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public BlockPos getPair() {
		return pair;
	}

	public void setPair(BlockPos pair) {
		this.pair = pair;
		markDirty();
	}

	public int getChannelProgress() {
		return channelProgress;
	}

	public void setChannelProgress(int channelProgress) {
		this.channelProgress = channelProgress;
		markDirty();
	}

	public void incrementChannelProgress() {
		incrementChannelProgress(1);
	}

	public void incrementChannelProgress(int amount) {
		this.channelProgress += amount;
		markDirty();
	}

	public void resetChanneling() {
		this.channelProgress = 0;
		markDirty();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		if (this.pair != null) {
			compound.setTag("Pair", NBTUtil.createPosTag(this.pair));
		}
		compound.setInteger("ChannelProgress", this.channelProgress);
		compound.setBoolean("PairingComplete", this.pairingComplete);
		if (this.pendingTwinLocation != null) {
			compound.setTag("PendingTwinLocation", NBTUtil.createPosTag(this.pendingTwinLocation));
		}
		compound.setInteger("GenerationCooldown", this.generationCooldown);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.pair = compound.hasKey("Pair") ? NBTUtil.getPosFromTag(compound.getCompoundTag("Pair")) : null;
		this.channelProgress = compound.getInteger("ChannelProgress");
		this.pendingTwinLocation = compound.hasKey("PendingTwinLocation")
				? NBTUtil.getPosFromTag(compound.getCompoundTag("PendingTwinLocation"))
				: null;
		this.generationCooldown = compound.getInteger("GenerationCooldown");

		if (compound.hasKey("PairingComplete")) {
			this.pairingComplete = compound.getBoolean("PairingComplete");
		} else { // Handle migration from older versions
			boolean isDone = false;
			if (compound.hasKey("PairingState")) {
				int stateOrdinal = compound.getInteger("PairingState");
				// Check for old DONE states from 4-state and 3-state enums
				if (stateOrdinal == 3) { // Original DONE state
					isDone = true;
				} else if (stateOrdinal == 2 && !compound.hasKey("PendingTwinLocation")) { // Refactored DONE state
					isDone = true;
				}
			} else if (compound.getBoolean("HasAttemptedPairing")) { // Legacy boolean state
				isDone = true;
			}
			this.pairingComplete = isDone;
		}
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}

	@Nullable
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, 0, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}
}
