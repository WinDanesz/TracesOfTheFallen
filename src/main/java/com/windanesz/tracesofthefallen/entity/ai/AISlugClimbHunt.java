package com.windanesz.tracesofthefallen.entity.ai;

import com.windanesz.tracesofthefallen.entity.EntitySlug;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Stealth-hunting AI for {@link EntitySlug}: while undetected, it stalks its target from a wall or ceiling
 * perch above them, then releases its grip to drop-attack and latch onto their back. Stands down once the
 * slug has been hurt (see {@link AISlugMeleeAttack}).
 */
public class AISlugClimbHunt extends EntityAIBase {

	private static final EnumFacing[] HORIZONTALS = {EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST};

	private final EntitySlug slug;
	private final double climbSpeed;

	private BlockPos perch;
	private int recalcCooldown;
	private int repathCooldown;
	private boolean dropping;

	public AISlugClimbHunt(EntitySlug slug, double climbSpeed) {
		this.slug = slug;
		this.climbSpeed = climbSpeed;
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute() {
		EntityLivingBase target = this.slug.getAttackTarget();
		return target != null && target.isEntityAlive() && !this.slug.isRevealed() && !this.slug.isAttachedActive();
	}

	@Override
	public boolean shouldContinueExecuting() {
		return this.shouldExecute();
	}

	@Override
	public void startExecuting() {
		this.perch = null;
		this.recalcCooldown = 0;
		this.repathCooldown = 0;
		this.dropping = false;
	}

	@Override
	public void resetTask() {
		this.slug.getNavigator().clearPath();
		if (this.dropping) {
			this.slug.cancelDropAttack();
		}
		if (this.slug.isClimbing()) {
			this.slug.releaseSurface();
		}
		this.dropping = false;
		this.perch = null;
	}

	@Override
	public void updateTask() {
		EntityLivingBase target = this.slug.getAttackTarget();
		if (target == null) {
			return;
		}

		this.slug.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);

		if (this.dropping) {
			this.handleDropping(target);
			return;
		}

		EnumFacing facing = this.slug.getClimbFacing();
		if (facing != EnumFacing.UP) {
			this.handleClimbing(target, facing);
		} else {
			this.handleGroundApproach(target);
		}
	}

	private void handleGroundApproach(EntityLivingBase target) {
		if (this.recalcCooldown-- <= 0) {
			this.perch = this.findPerchPoint(target);
			this.recalcCooldown = 20 + this.slug.getRNG().nextInt(20);
		}

		// Re-path periodically rather than only once the current path finishes, so the chase keeps tracking
		// a moving target instead of walking to a stale point and stalling until the old path completes.
		if (this.slug.getNavigator().noPath() || this.repathCooldown-- <= 0) {
			BlockPos moveGoal = this.perch != null ? this.perch : target.getPosition();
			this.slug.getNavigator().tryMoveToXYZ(moveGoal.getX() + 0.5D, moveGoal.getY(), moveGoal.getZ() + 0.5D, 1.0D);
			this.repathCooldown = 20;
		}

		if (this.perch != null && this.perch.getY() > this.slug.posY + 1.0D) {
			EnumFacing wall = this.findAdjacentWall();
			if (wall != null) {
				this.slug.getNavigator().clearPath();
				this.slug.setClimbFacing(wall);
			}
		}
	}

	private void handleClimbing(EntityLivingBase target, EnumFacing facing) {
		if (this.recalcCooldown-- <= 0) {
			this.perch = this.findPerchPoint(target);
			this.recalcCooldown = 20 + this.slug.getRNG().nextInt(20);
		}
		BlockPos goal = this.perch != null ? this.perch : target.getPosition().up(2);

		// A wall can still read as "solid behind me" once a ceiling overhangs its own column (very common
		// room geometry, since ceilings usually extend over the walls that support them), which would
		// otherwise trap the slug pressing upward into the ceiling forever without ever letting go of the
		// wall. Actively prefer hopping onto the ceiling once one is available overhead and we're climbing
		// towards it.
		if (facing.getAxis() != EnumFacing.Axis.Y && goal.getY() > this.slug.posY
				&& EntitySlug.isClimbableSurface(this.slug.world, this.slug.getCenterPos().up())) {
			facing = EnumFacing.DOWN;
		}

		EnumFacing support = this.slug.findSupportingFace(facing);
		if (support == null) {
			this.slug.releaseSurface();
			return;
		}
		this.slug.setClimbFacing(support);
		facing = support;

		this.slug.tickClimbMovement(facing, goal, this.climbSpeed);

		double dx = target.posX - this.slug.posX;
		double dz = target.posZ - this.slug.posZ;
		double horizDistSq = dx * dx + dz * dz;
		double vertDist = this.slug.posY - target.posY;
		if (horizDistSq < 6.25D && vertDist > 1.0D && vertDist < 14.0D && this.slug.getEntitySenses().canSee(target)) {
			this.slug.beginDropAttack(target);
			this.dropping = true;
		}
	}

	private void handleDropping(EntityLivingBase target) {
		if (!target.isEntityAlive()) {
			this.slug.cancelDropAttack();
			this.dropping = false;
			return;
		}
		if (this.slug.getDistance(target) < 1.4D) {
			this.slug.attachTo(target);
			this.dropping = false;
			return;
		}
		if (!this.slug.isDropAttacking()) {
			// landed without reaching the target; go back to stalking
			this.dropping = false;
			return;
		}
		double dx = target.posX - this.slug.posX;
		double dz = target.posZ - this.slug.posZ;
		double len = Math.sqrt(dx * dx + dz * dz);
		if (len > 1.0E-4D) {
			this.slug.motionX += dx / len * 0.02D;
			this.slug.motionZ += dz / len * 0.02D;
		}
	}

	private EnumFacing findAdjacentWall() {
		BlockPos center = this.slug.getCenterPos();
		for (EnumFacing dir : HORIZONTALS) {
			if (EntitySlug.isClimbableSurface(this.slug.world, center.offset(dir))) {
				return dir.getOpposite();
			}
		}
		return null;
	}

	private BlockPos findPerchPoint(EntityLivingBase target) {
		World world = this.slug.world;
		BlockPos base = target.getPosition();

		for (int dy = 2; dy <= 12; dy++) {
			BlockPos above = base.up(dy);
			if (EntitySlug.isClimbableSurface(world, above)) {
				BlockPos perchPos = above.down();
				return !EntitySlug.isClimbableSurface(world, perchPos) ? perchPos : null;
			}
		}

		for (int r = 3; r <= 7; r++) {
			for (EnumFacing dir : HORIZONTALS) {
				BlockPos wallPos = base.up(2).offset(dir, r);
				if (EntitySlug.isClimbableSurface(world, wallPos)) {
					BlockPos perchPos = wallPos.offset(dir.getOpposite());
					if (!EntitySlug.isClimbableSurface(world, perchPos)) {
						return perchPos;
					}
				}
			}
		}
		return null;
	}
}
