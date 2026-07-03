package com.windanesz.tracesofthefallen.entity.ai;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.entity.EntityGoblinEngineer;
import com.windanesz.tracesofthefallen.entity.EntityWroughtBomb;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

public class GoblinAIEngineerEscape extends EntityAIBase {
	private final EntityGoblinEngineer engineer;
	private final PathNavigate navigator;
	private double randPosX;
	private double randPosY;
	private double randPosZ;

	public GoblinAIEngineerEscape(EntityGoblinEngineer engineer) {
		this.engineer = engineer;
		this.navigator = engineer.getNavigator();
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute() {
		if (this.engineer.getHealth() > this.engineer.getMaxHealth() / 2.0F) {
			return false;
		}

		EntityWroughtBomb mountedBomb = null;
		for (Entity passenger : this.engineer.getPassengers()) {
			if (passenger instanceof EntityWroughtBomb) {
				mountedBomb = (EntityWroughtBomb) passenger;
				break;
			}
		}
		if (mountedBomb == null) {
			return false;
		}

		EntityLivingBase attacker = this.engineer.getRevengeTarget();
		if (attacker == null) {
			attacker = this.engineer.getAttackTarget();
		}
		if (attacker == null) {
			EntityPlayer closestPlayer = this.engineer.world.getClosestPlayerToEntity(this.engineer, 16.0D);
			if (closestPlayer != null && !closestPlayer.isCreative() && !closestPlayer.isSpectator()) {
				attacker = closestPlayer;
			}
		}
		if (attacker == null) {
			return false;
		}

		double distanceSq = this.engineer.getDistanceSq(attacker);
		if (distanceSq <= 64.0D) { // Close distance <= 8 blocks
			if (!this.engineer.world.isRemote) {
				mountedBomb.dismountRidingEntity();
				mountedBomb.setPosition(this.engineer.posX, this.engineer.posY, this.engineer.posZ);
				mountedBomb.setFuse(Settings.miscSettings.wroughtBombFuseTime);
				this.engineer.world.playSound(null, this.engineer.posX, this.engineer.posY, this.engineer.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}
		}

		Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.engineer, 16, 7, attacker.getPositionVector());
		if (vec3d == null) {
			return false;
		}
		this.randPosX = vec3d.x;
		this.randPosY = vec3d.y;
		this.randPosZ = vec3d.z;
		return true;
	}

	@Override
	public void startExecuting() {
		this.navigator.tryMoveToXYZ(this.randPosX, this.randPosY, this.randPosZ, 1.4D);
	}

	@Override
	public boolean shouldContinueExecuting() {
		return !this.navigator.noPath() && this.engineer.getHealth() <= this.engineer.getMaxHealth() / 2.0F;
	}
}
