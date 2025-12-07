package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.entity.EntityGoblin;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

public class TileEntityTent extends TileEntityLostLoot implements ITickable {

	private boolean hasSpawnedGoblins = false;

	@Override
	public void update() {
		// Only run on server side
		if (world.isRemote || hasSpawnedGoblins || world.getTotalWorldTime() % 20 != 0) {
			return;
		}

		// Check for nearby players within detection range
		EntityPlayer nearestPlayer = world.getClosestPlayer(
				pos.getX() + 0.5,
				pos.getY() + 0.5,
				pos.getZ() + 0.5,
				Settings.miscSettings.tentGoblinDetectionRange,
				false
		);

		// If player found and not in creative/spectator mode, spawn goblins
		if (nearestPlayer != null && !nearestPlayer.isCreative() && !nearestPlayer.isSpectator()) {
			spawnGoblins();
		}
	}

	private void spawnGoblins() {
		// Check if this is a bush_crate and apply spawn chance
		Block block = world.getBlockState(this.pos).getBlock();
		if (block == ModBlocks.bush_crate) {
			if (world.rand.nextDouble() > Settings.miscSettings.bushCrateGoblinSpawnChance) {
				// Failed spawn chance check, mark as checked and don't spawn
				hasSpawnedGoblins = true;
				markDirty();
				return;
			}
		}
		
		// Determine how many goblins to spawn (1-3)
		int goblinCount = Settings.miscSettings.tentGoblinMinCount + 
				world.rand.nextInt(Settings.miscSettings.tentGoblinMaxCount - Settings.miscSettings.tentGoblinMinCount + 1);

		// Spawn goblins at tent position with 0.3y offset
		for (int i = 0; i < goblinCount; i++) {
			EntityGoblin goblin = new EntityGoblin(world);
			goblin.setPosition(pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5);
			world.spawnEntity(goblin);
		}

		// Mark as spawned and save state
		hasSpawnedGoblins = true;
		markDirty();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setBoolean("hasSpawnedGoblins", this.hasSpawnedGoblins);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.hasSpawnedGoblins = compound.getBoolean("hasSpawnedGoblins");
	}
}
