package com.windanesz.tracesofthefallen.entity.ai;

import com.windanesz.tracesofthefallen.entity.living.EntitySlug;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Idle behaviour for {@link EntitySlug}: with no target, it alternates between wandering on the ground and
 * climbing up to cling to a nearby wall or ceiling for a while, so it naturally spends part of its time on
 * walls even outside of a hunt. If {@link AISlugClimbHunt} hands off to this task while the slug is already
 * mid-climb (its target went away while climbing), this resumes clinging from exactly that spot instead of
 * dropping to the ground first. Stands down permanently once revealed (see {@link AISlugMeleeAttack}).
 */
public class AISlugIdleClimb extends EntityAIBase {

	private static final int MIN_CLING_TICKS = 100;
	private static final int MAX_CLING_TICKS = 300;
	private static final int MIN_WALL_SEARCH_COOLDOWN = 100;
	private static final int MAX_WALL_SEARCH_COOLDOWN = 300;
	private static final int WALL_SEARCH_MIN_RADIUS = 2;
	private static final int WALL_SEARCH_MAX_RADIUS = 8;
	private static final int APPROACH_GIVE_UP_TICKS = 100;
	private static final int CEILING_SEEK_MAX_HEIGHT = 8;
	private static final int SURFACE_WANDER_RANGE = 3;
	private static final int MIN_SURFACE_WANDER_COOLDOWN = 100;
	private static final int MAX_SURFACE_WANDER_COOLDOWN = 200;
	private static final int CEILING_WALL_SEARCH_RANGE = 16;

	private enum Phase {GROUND, APPROACHING, CLINGING}

	private final EntitySlug slug;
	private final double climbSpeed;

	private Phase phase = Phase.GROUND;
	private BlockPos wallPerch;
	private int wallSearchCooldown;
	private int repathCooldown;
	private int approachTicks;
	private int clingTicks;
	private boolean descending;
	private BlockPos wanderGoal;
	private int wanderCooldown;

	public AISlugIdleClimb(EntitySlug slug, double climbSpeed) {
		this.slug = slug;
		this.climbSpeed = climbSpeed;
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute() {
		return this.slug.getAttackTarget() == null && !this.slug.isRevealed() && !this.slug.isAttachedActive();
	}

	@Override
	public boolean shouldContinueExecuting() {
		return this.shouldExecute();
	}

	@Override
	public void startExecuting() {
		if (this.slug.isClimbing()) {
			this.beginClinging(this.slug.getClimbFacing());
		} else {
			this.phase = Phase.GROUND;
			this.wallPerch = null;
			this.wallSearchCooldown = 0;
		}
	}

	@Override
	public void resetTask() {
		this.slug.getNavigator().clearPath();
		this.wallPerch = null;
		this.wanderGoal = null;
	}

	@Override
	public void updateTask() {
		switch (this.phase) {
			case CLINGING:
				this.handleClinging();
				break;
			case APPROACHING:
				this.handleApproaching();
				break;
			default:
				this.handleGround();
				break;
		}
	}

	private void handleGround() {
		// Latch on immediately if a climbable face already happens to be right next to us, rather than only
		// ever finding walls through the periodic radius search below.
		EnumFacing wall = this.wallSearchCooldown <= 0 ? this.slug.findAdjacentWall() : null;
		if (wall != null) {
			this.beginClinging(wall);
			return;
		}

		if (this.wallSearchCooldown-- <= 0) {
			this.wallSearchCooldown = MIN_WALL_SEARCH_COOLDOWN + this.slug.getRNG().nextInt(MAX_WALL_SEARCH_COOLDOWN - MIN_WALL_SEARCH_COOLDOWN);
			this.wallPerch = EntitySlug.findNearbyWallPerch(this.slug.world, this.slug.getPosition(), WALL_SEARCH_MIN_RADIUS, WALL_SEARCH_MAX_RADIUS);
			if (this.wallPerch != null) {
				this.approachTicks = 0;
				this.repathCooldown = 0;
				this.phase = Phase.APPROACHING;
				return;
			}
		}

		if (this.slug.getNavigator().noPath() && this.slug.getRNG().nextInt(100) == 0) {
			Vec3d wander = RandomPositionGenerator.findRandomTarget(this.slug, 10, 7);
			if (wander != null) {
				this.slug.getNavigator().tryMoveToXYZ(wander.x, wander.y, wander.z, 1.0D);
			}
		}
	}

	private void handleApproaching() {
		EnumFacing wall = this.slug.findAdjacentWall();
		if (wall != null) {
			this.beginClinging(wall);
			return;
		}

		// The chosen wall may be unreachable (water, lava, a sheer drop) or get mined out from under the
		// attempt - give up and go back to plain wandering rather than stalling on it forever.
		if (++this.approachTicks > APPROACH_GIVE_UP_TICKS) {
			this.wallPerch = null;
			this.phase = Phase.GROUND;
			return;
		}

		if (this.slug.getNavigator().noPath() || this.repathCooldown-- <= 0) {
			this.slug.getNavigator().tryMoveToXYZ(this.wallPerch.getX() + 0.5D, this.wallPerch.getY(), this.wallPerch.getZ() + 0.5D, 1.0D);
			this.repathCooldown = 20;
		}
	}

	private void handleClinging() {
		if (!this.descending && --this.clingTicks <= 0) {
			this.descending = true;
			this.wanderGoal = null;
			this.wanderCooldown = 0;
		}

		// Prefer hopping onto a ceiling the moment one's directly overhead, same as the hunting AI does -
		// without this a still-solid wall always just keeps clinging to itself and never even looks up.
		EnumFacing facing = this.slug.getClimbFacing();
		if (this.descending && facing == EnumFacing.DOWN) {
			EnumFacing wall = this.slug.findAdjacentWall();
			if (wall != null) {
				facing = wall;
			}
		} else if (!this.descending && facing.getAxis().isHorizontal() && this.slug.hasClimbableCeilingAbove()) {
			facing = EnumFacing.DOWN;
		}

		EnumFacing support = this.slug.findSupportingFace(facing);
		if (support == null || support == EnumFacing.UP) {
			// null: genuinely lost grip. UP: corner-turned onto the flat top of whatever we were climbing -
			// either way, stop climbing and hand back to ordinary ground physics rather than passing UP into
			// tickClimbMovement, which has no notion of it as a surface normal and would leave gravity
			// disabled forever with nothing left to ever turn it back on.
			this.finishClinging();
			return;
		}
		if (support != this.slug.getClimbFacing()) {
			this.wanderGoal = null;
			this.wanderCooldown = 0;
		}
		this.slug.setClimbFacing(support);

		if (this.descending) {
			if (support.getAxis().isHorizontal()) {
				if (this.slug.onGround) {
					this.finishClinging();
					return;
				}
				this.slug.tickClimbMovement(support, this.slug.getPosition().down(), this.climbSpeed);
			} else {
				if (this.wanderCooldown-- <= 0 || this.wanderGoal != null && this.slug.getCenterPos().equals(this.wanderGoal)) {
					this.wanderGoal = this.findCeilingStepToWall();
					this.wanderCooldown = 20;
				}
				// An isolated ceiling has no safe way down: keep holding on and periodically look again.
				this.slug.tickClimbMovement(support, this.wanderGoal, this.climbSpeed);
			}
			return;
		}

		BlockPos goal = null;
		if (support.getAxis() != EnumFacing.Axis.Y) {
			// Only commit to climbing towards a ceiling if the wall we're actually on reaches that high -
			// otherwise a "ceiling" found further up in open air (a floor of a taller structure, uneven
			// terrain above) would have the slug climb straight off the top of a short wall with nothing
			// left to hold onto.
			BlockPos ceiling = EntitySlug.findCeilingPerch(this.slug.world, this.slug.getPosition(), 1, CEILING_SEEK_MAX_HEIGHT);
			if (ceiling != null && EntitySlug.isClimbableSurface(this.slug.world, ceiling.offset(support.getOpposite()))) {
				goal = ceiling;
			}
		}

		if (goal == null) {
			// Not currently making for a ceiling: wander around on whatever surface we're on (wall or
			// ceiling alike) instead of just holding perfectly still for the rest of the cling duration.
			if (this.wanderGoal == null || this.wanderCooldown-- <= 0) {
				this.wanderGoal = this.findSurfaceWanderGoal(support);
				this.wanderCooldown = MIN_SURFACE_WANDER_COOLDOWN + this.slug.getRNG().nextInt(MAX_SURFACE_WANDER_COOLDOWN - MIN_SURFACE_WANDER_COOLDOWN);
			}
			goal = this.wanderGoal;
		} else {
			this.wanderGoal = null;
		}

		this.slug.tickClimbMovement(support, goal, this.climbSpeed);
	}

	private void finishClinging() {
		this.slug.releaseSurface();
		this.slug.motionX = this.slug.motionY = this.slug.motionZ = 0.0D;
		this.phase = Phase.GROUND;
		this.wanderGoal = null;
		this.wallSearchCooldown = MIN_WALL_SEARCH_COOLDOWN + this.slug.getRNG().nextInt(MAX_WALL_SEARCH_COOLDOWN - MIN_WALL_SEARCH_COOLDOWN);
	}

	/** Search connected ceiling cells, retaining the first step so the route cannot cut across holes. */
	private BlockPos findCeilingStepToWall() {
		BlockPos start = this.slug.getCenterPos();
		ArrayDeque<BlockPos> pending = new ArrayDeque<>();
		Map<BlockPos, BlockPos> firstSteps = new HashMap<>();
		pending.add(start);
		firstSteps.put(start, null);
		while (!pending.isEmpty()) {
			BlockPos current = pending.removeFirst();
			for (EnumFacing direction : EnumFacing.HORIZONTALS) {
				BlockPos next = current.offset(direction);
				if (EntitySlug.isClimbableSurface(this.slug.world, next)) {
					return firstSteps.get(current);
				}
				if (Math.abs(next.getX() - start.getX()) > CEILING_WALL_SEARCH_RANGE
						|| Math.abs(next.getZ() - start.getZ()) > CEILING_WALL_SEARCH_RANGE
						|| firstSteps.containsKey(next) || !EntitySlug.isClimbableSurface(this.slug.world, next.up())) {
					continue;
				}
				firstSteps.put(next, current.equals(start) ? next : firstSteps.get(current));
				pending.addLast(next);
			}
		}
		return null;
	}

	/**
	 * Picks a random nearby point on the plane of the given surface (wall or ceiling alike) that's open and
	 * still backed by a climbable block, for idle wandering while clinging. Returns {@code null} if a few
	 * random attempts all miss, e.g. right at the edge of a small surface.
	 */
	private BlockPos findSurfaceWanderGoal(EnumFacing facing) {
		BlockPos center = this.slug.getCenterPos();
		Random rand = this.slug.getRNG();
		for (int attempt = 0; attempt < 4; attempt++) {
			int a = rand.nextInt(SURFACE_WANDER_RANGE * 2 + 1) - SURFACE_WANDER_RANGE;
			int b = rand.nextInt(SURFACE_WANDER_RANGE * 2 + 1) - SURFACE_WANDER_RANGE;
			BlockPos candidate = facing.getAxis() == EnumFacing.Axis.X ? center.add(0, a, b)
					: facing.getAxis() == EnumFacing.Axis.Z ? center.add(a, b, 0)
					: center.add(a, 0, b);
			if (EntitySlug.isClimbableSurface(this.slug.world, candidate.offset(facing.getOpposite()))
					&& !EntitySlug.isClimbableSurface(this.slug.world, candidate)) {
				return candidate;
			}
		}
		return null;
	}

	private void beginClinging(EnumFacing facing) {
		this.slug.getNavigator().clearPath();
		this.slug.setClimbFacing(facing);
		this.phase = Phase.CLINGING;
		this.clingTicks = MIN_CLING_TICKS + this.slug.getRNG().nextInt(MAX_CLING_TICKS - MIN_CLING_TICKS);
		this.descending = false;
		this.wanderGoal = null;
		this.wanderCooldown = 0;
	}
}
