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
		this.dropping = false;
		this.perch = null;
		// Deliberately not releasing the climbing surface here: this task can also stop simply because the
		// target went away (creative mode, died, out of range, ...) while the slug is mid-climb, and it
		// should keep clinging in place rather than fall - AISlugIdleClimb picks up from exactly where this
		// leaves off. A real release only happens where it's actually warranted: on taking damage
		// (EntitySlug#attackEntityFrom) or losing the physical surface (handleClimbing below).
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
		// Opportunistically climb any wall immediately at hand before doing anything else - a slug spawned
		// (or that has wandered) right next to a wall should scale it straight away, rather than only ever
		// recognising walls that happen to also be conveniently close to the target's own position.
		EnumFacing wall = this.slug.findAdjacentWall();
		if (wall != null) {
			this.slug.getNavigator().clearPath();
			this.slug.setClimbFacing(wall);
			return;
		}

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
	}

	private void handleClimbing(EntityLivingBase target, EnumFacing facing) {
		if (this.recalcCooldown-- <= 0) {
			this.perch = this.findTrackingPerch(target);
			this.recalcCooldown = 20 + this.slug.getRNG().nextInt(20);
		}
		BlockPos goal = this.perch != null ? this.perch : target.getPosition().up(2);

		// A wall can still read as "solid behind me" once a ceiling overhangs its own column (very common
		// room geometry, since ceilings usually extend over the walls that support them), which would
		// otherwise trap the slug pressing upward into the ceiling forever without ever letting go of the
		// wall. Actively prefer hopping onto the ceiling once one is available overhead and we're climbing
		// towards it.
		if (facing.getAxis() != EnumFacing.Axis.Y && goal.getY() > this.slug.posY && this.slug.hasClimbableCeilingAbove()) {
			facing = EnumFacing.DOWN;
		}

		EnumFacing support = this.slug.findSupportingFace(facing);
		if (support == null || support == EnumFacing.UP) {
			// null: genuinely lost grip. UP: corner-turned onto the flat top of whatever we were climbing,
			// which findSupportingFace treats as a legitimate landing spot - either way, stop climbing and
			// hand back to ordinary ground physics. tickClimbMovement has no notion of UP as a surface normal
			// (it would force noGravity on and never turn it back off, since ground-approach logic never
			// touches gravity), so UP must never reach it.
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

	private BlockPos findPerchPoint(EntityLivingBase target) {
		World world = this.slug.world;

		// Always prefer a wall right at hand over anything near the target - otherwise a ceiling somewhere
		// over the target's head (which is common: any room, cave, or bit of overhead foliage) would keep
		// winning and the slug would trek towards the target's general vicinity instead of using what's
		// conveniently close, which looks just like beelining straight at them.
		BlockPos nearSlug = EntitySlug.findNearbyWallPerch(world, this.slug.getPosition(), 1, 6);
		if (nearSlug != null) {
			return nearSlug;
		}

		// Otherwise, the ideal ambush spot: a ceiling directly above the target.
		BlockPos targetBase = target.getPosition();
		for (int dy = 2; dy <= 12; dy++) {
			BlockPos above = targetBase.up(dy);
			if (EntitySlug.isClimbableSurface(world, above)) {
				BlockPos perchPos = above.down();
				return !EntitySlug.isClimbableSurface(world, perchPos) ? perchPos : null;
			}
		}

		// Failing that, any wall reasonably close to the target will still do.
		return EntitySlug.findNearbyWallPerch(world, targetBase, 3, 7);
	}

	/**
	 * Recomputes where to be while already climbing and stalking {@code target} - always relative to the
	 * target, unlike {@link #findPerchPoint} (used only for the initial "which wall do I climb" decision).
	 * Once already up on a wall or ceiling, "nearest wall to myself" stops being a meaningful preference -
	 * almost everywhere the slug can be while climbing is near <i>some</i> wall - and reusing that same
	 * preference here just pins the goal to wherever the slug already happens to be, so it stops tracking a
	 * moving target at all instead of continuing to stalk them.
	 */
	private BlockPos findTrackingPerch(EntityLivingBase target) {
		World world = this.slug.world;
		BlockPos targetBase = target.getPosition();
		for (int dy = 2; dy <= 12; dy++) {
			BlockPos above = targetBase.up(dy);
			if (EntitySlug.isClimbableSurface(world, above)) {
				BlockPos perchPos = above.down();
				return !EntitySlug.isClimbableSurface(world, perchPos) ? perchPos : null;
			}
		}
		return EntitySlug.findNearbyWallPerch(world, targetBase, 3, 7);
	}
}
