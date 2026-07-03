package com.windanesz.tracesofthefallen.entity.ai;

import com.windanesz.tracesofthefallen.entity.EntityGoblin;
import com.windanesz.tracesofthefallen.entity.EntityWroughtBomb;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class GoblinAIPickupBomb extends EntityAIBase {
	private final EntityGoblin goblin;
	private final PathNavigate navigator;
	private Entity targetBomb;
	private BlockPos targetBlockPos;
	private int searchCooldown;
	private static final double DETECTION_RANGE = 8.0D;
	private static final int SEARCH_INTERVAL = 20;

	public GoblinAIPickupBomb(EntityGoblin goblin) {
		this.goblin = goblin;
		this.navigator = goblin.getNavigator();
		this.setMutexBits(3);
		this.searchCooldown = 0;
	}

	@Override
	public boolean shouldExecute() {
		if (this.goblin.getClass() != EntityGoblin.class) {
			return false;
		}
		if (this.goblin.isCarryingBomb() || this.goblin.isHoldingIdol() || this.goblin.isRiding()) {
			return false;
		}
		if (this.searchCooldown > 0) {
			this.searchCooldown--;
			return false;
		}

		this.searchCooldown = SEARCH_INTERVAL;
		this.targetBomb = this.findNearestBomb();
		if (this.targetBomb != null) {
			return true;
		}
		this.targetBlockPos = this.findNearestBombBlock();
		return this.targetBlockPos != null;
	}

	@Override
	public boolean shouldContinueExecuting() {
		if (this.goblin.isCarryingBomb() || this.goblin.isHoldingIdol() || this.goblin.isRiding()) {
			return false;
		}
		if (this.targetBomb != null) {
			if (this.targetBomb.isDead || this.goblin.getDistanceSq(this.targetBomb) > DETECTION_RANGE * DETECTION_RANGE * 2.0D || this.targetBomb.isRiding()) {
				return false;
			}
		} else if (this.targetBlockPos != null) {
			if (this.goblin.world.getBlockState(this.targetBlockPos).getBlock() != ModBlocks.wrought_bomb || this.goblin.getDistanceSqToCenter(this.targetBlockPos) > DETECTION_RANGE * DETECTION_RANGE * 2.0D) {
				return false;
			}
		} else {
			return false;
		}
		return !this.navigator.noPath();
	}

	@Override
	public void startExecuting() {
		if (this.targetBomb != null) {
			this.navigator.tryMoveToXYZ(this.targetBomb.posX, this.targetBomb.posY, this.targetBomb.posZ, 1.2D);
		} else if (this.targetBlockPos != null) {
			this.navigator.tryMoveToXYZ(this.targetBlockPos.getX() + 0.5D, this.targetBlockPos.getY(), this.targetBlockPos.getZ() + 0.5D, 1.2D);
		}
	}

	@Override
	public void resetTask() {
		this.targetBomb = null;
		this.targetBlockPos = null;
		this.navigator.clearPath();
	}

	@Override
	public void updateTask() {
		if (this.targetBomb != null && !this.targetBomb.isDead) {
			this.goblin.getLookHelper().setLookPositionWithEntity(this.targetBomb, 10.0F, (float) this.goblin.getVerticalFaceSpeed());

			if (this.goblin.getDistanceSq(this.targetBomb) > 2.5D) {
				if (this.navigator.noPath() || this.goblin.ticksExisted % 10 == 0) {
					this.navigator.tryMoveToXYZ(this.targetBomb.posX, this.targetBomb.posY, this.targetBomb.posZ, 1.2D);
				}
			} else {
				if (this.targetBomb instanceof EntityWroughtBomb) {
					if (!this.goblin.world.isRemote) {
						this.targetBomb.startRiding(this.goblin);
					}
				} else if (this.targetBomb instanceof EntityItem) {
					ItemStack stack = ((EntityItem) this.targetBomb).getItem();
					if (!stack.isEmpty() && stack.getItem() == Item.getItemFromBlock(ModBlocks.wrought_bomb)) {
						if (!this.goblin.world.isRemote) {
							EntityWroughtBomb bomb = new EntityWroughtBomb(this.goblin.world, this.goblin.posX, this.goblin.posY, this.goblin.posZ, this.goblin);
							bomb.setFuse(-1);
							this.goblin.world.spawnEntity(bomb);
							bomb.startRiding(this.goblin);
						}
						stack.shrink(1);
						if (stack.isEmpty()) {
							this.targetBomb.setDead();
						}
					}
				}
			}
		} else if (this.targetBlockPos != null && this.goblin.world.getBlockState(this.targetBlockPos).getBlock() == ModBlocks.wrought_bomb) {
			this.goblin.getLookHelper().setLookPosition(this.targetBlockPos.getX() + 0.5D, this.targetBlockPos.getY() + 0.5D, this.targetBlockPos.getZ() + 0.5D, 10.0F, (float) this.goblin.getVerticalFaceSpeed());

			if (this.goblin.getDistanceSqToCenter(this.targetBlockPos) > 3.0D) {
				if (this.navigator.noPath() || this.goblin.ticksExisted % 10 == 0) {
					this.navigator.tryMoveToXYZ(this.targetBlockPos.getX() + 0.5D, this.targetBlockPos.getY(), this.targetBlockPos.getZ() + 0.5D, 1.2D);
				}
			} else {
				if (!this.goblin.world.isRemote) {
					this.goblin.world.setBlockToAir(this.targetBlockPos);
					EntityWroughtBomb bomb = new EntityWroughtBomb(this.goblin.world, this.goblin.posX, this.goblin.posY, this.goblin.posZ, this.goblin);
					bomb.setFuse(-1);
					this.goblin.world.spawnEntity(bomb);
					bomb.startRiding(this.goblin);
				}
			}
		}
	}

	private Entity findNearestBomb() {
		List<Entity> nearbyEntities = this.goblin.world.getEntitiesWithinAABB(
			Entity.class,
			this.goblin.getEntityBoundingBox().grow(DETECTION_RANGE, 2.0D, DETECTION_RANGE)
		);

		Entity closestBomb = null;
		double closestDistance = Double.MAX_VALUE;

		for (Entity entity : nearbyEntities) {
			if (entity instanceof EntityWroughtBomb && !entity.isRiding() && !entity.isDead) {
				double distance = this.goblin.getDistanceSq(entity);
				if (distance < closestDistance) {
					closestDistance = distance;
					closestBomb = entity;
				}
			} else if (entity instanceof EntityItem && !entity.isDead) {
				ItemStack stack = ((EntityItem) entity).getItem();
				if (!stack.isEmpty() && stack.getItem() == Item.getItemFromBlock(ModBlocks.wrought_bomb)) {
					double distance = this.goblin.getDistanceSq(entity);
					if (distance < closestDistance) {
						closestDistance = distance;
						closestBomb = entity;
					}
				}
			}
		}

		return closestBomb;
	}

	private BlockPos findNearestBombBlock() {
		BlockPos goblinPos = new BlockPos(this.goblin);
		BlockPos closestPos = null;
		double closestDistance = Double.MAX_VALUE;
		int range = (int) DETECTION_RANGE;

		for (int x = -range; x <= range; x++) {
			for (int y = -3; y <= 3; y++) {
				for (int z = -range; z <= range; z++) {
					BlockPos checkPos = goblinPos.add(x, y, z);
					if (this.goblin.world.getBlockState(checkPos).getBlock() == ModBlocks.wrought_bomb) {
						double dist = this.goblin.getDistanceSqToCenter(checkPos);
						if (dist < closestDistance) {
							closestDistance = dist;
							closestPos = checkPos;
						}
					}
				}
			}
		}
		return closestPos;
	}
}
