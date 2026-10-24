package com.windanesz.tracesofthefallen.entity.living;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.entity.ai.AISlugClimbHunt;
import com.windanesz.tracesofthefallen.entity.ai.AISlugIdleClimb;
import com.windanesz.tracesofthefallen.entity.ai.AISlugMeleeAttack;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * Shared behaviour for the slug-family mobs: they crawl on floors, walls and ceilings, prefer to stalk
 * a target from above out of sight, drop onto it to attach to its back and drain health over time, and
 * fall back to ordinary ground melee once they take damage.
 */
public abstract class EntitySlug extends EntityMob {

	/** Outward surface normal the slug is currently clinging to; {@link EnumFacing#UP} means "on the ground / falling". */
	private static final DataParameter<EnumFacing> CLIMB_FACING = EntityDataManager.createKey(EntitySlug.class, DataSerializers.FACING);
	private static final DataParameter<Boolean> REVEALED = EntityDataManager.createKey(EntitySlug.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> ATTACHED = EntityDataManager.createKey(EntitySlug.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> ATTACHED_TARGET_ID = EntityDataManager.createKey(EntitySlug.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> ATTACK_ANIMATION_TICKS = EntityDataManager.createKey(EntitySlug.class, DataSerializers.VARINT);
	private static final int ATTACK_ANIMATION_DURATION = 14;

	/** How long a slug may sit on the ground stalking a target with no climbable surface in reach before giving up on stealth. */
	private static final int GROUND_STALL_GIVE_UP_TICKS = 100;

	private EntityLivingBase attachedTarget;
	private int attachTicks;
	private boolean dropAttacking;
	private int groundStallTicks;
	private boolean restoreClimbSurface;

	protected EntitySlug(World worldIn) {
		super(worldIn);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(CLIMB_FACING, EnumFacing.UP);
		this.dataManager.register(REVEALED, false);
		this.dataManager.register(ATTACHED, false);
		this.dataManager.register(ATTACHED_TARGET_ID, -1);
		this.dataManager.register(ATTACK_ANIMATION_TICKS, 0);
	}

	@Override
	protected void initEntityAI() {
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new AISlugMeleeAttack(this));
		this.tasks.addTask(2, new AISlugClimbHunt(this, Settings.mobSettings.slugClimbSpeed));
		this.tasks.addTask(3, new AISlugIdleClimb(this, Settings.mobSettings.slugClimbSpeed));
		this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(7, new EntityAILookIdle(this));

		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(24.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(this.getMeleeSpeed());
	}

	protected abstract double getMeleeSpeed();

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setInteger("SlugClimbFacing", this.getClimbFacing().getIndex());
		compound.setBoolean("SlugRevealed", this.isRevealed());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		this.setRevealed(compound.getBoolean("SlugRevealed"));
		int index = compound.hasKey("SlugClimbFacing", 99) ? compound.getInteger("SlugClimbFacing") : EnumFacing.UP.getIndex();
		EnumFacing facing = index >= 0 && index < EnumFacing.values().length ? EnumFacing.byIndex(index) : EnumFacing.UP;
		if (this.isRevealed()) {
			facing = EnumFacing.UP;
		}
		// Restore synchronized orientation without snapping or querying chunks during entity loading.
		this.dataManager.set(CLIMB_FACING, facing);
		this.restoreClimbSurface = facing != EnumFacing.UP || (!compound.hasKey("SlugClimbFacing", 99)
				&& compound.getBoolean("NoGravity") && !this.isRevealed());
		this.setNoGravity(this.restoreClimbSurface);
		this.attachedTarget = null;
		this.dataManager.set(ATTACHED, false);
		this.dataManager.set(ATTACHED_TARGET_ID, -1);
		this.noClip = false;
		this.dropAttacking = false;
	}

	// === Synced state accessors ===

	public EnumFacing getClimbFacing() {
		return this.dataManager.get(CLIMB_FACING);
	}

	public boolean isClimbing() {
		return this.getClimbFacing() != EnumFacing.UP;
	}

	public void setClimbFacing(EnumFacing facing) {
		if (this.getClimbFacing() != facing) {
			this.dataManager.set(CLIMB_FACING, facing);
			if (facing != EnumFacing.UP) {
				this.snapToSurface(facing);
			}
		}
	}

	/**
	 * Repositions the entity flush against the surface in the given (non-UP) facing's normal direction,
	 * along the single axis that surface constrains. Called every tick while climbing (see
	 * {@link #tickClimbMovement}) as well as on a fresh latch-on or corner-turn.
	 * <p>
	 * Deliberately does NOT go through {@link #setPosition}: that also fires Forge's per-move chunk
	 * registration hook ({@code World#updateEntityWithOptionalForce}), which is meant for infrequent,
	 * one-off repositioning (teleports, spawns) - not routine per-tick physics. Vanilla's own continuous
	 * movement path ({@code Entity#move}) skips it too, updating the bounding box directly instead; this
	 * does the same.
	 */
	private void snapToSurface(EnumFacing facing) {
		BlockPos center = this.getCenterPos();
		double halfWidth = this.width * 0.5D;
		double x = this.posX, y = this.posY, z = this.posZ;
		switch (facing) {
			case EAST:
				x = center.getX() + halfWidth;
				break;
			case WEST:
				x = center.getX() + 1.0D - halfWidth;
				break;
			case SOUTH:
				z = center.getZ() + halfWidth;
				break;
			case NORTH:
				z = center.getZ() + 1.0D - halfWidth;
				break;
			case DOWN:
				y = center.getY() + 1.0D - this.height;
				break;
			default:
				return;
		}
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.setEntityBoundingBox(new AxisAlignedBB(x - halfWidth, y, z - halfWidth, x + halfWidth, y + this.height, z + halfWidth));
	}

	public boolean isRevealed() {
		return this.dataManager.get(REVEALED);
	}

	public void setRevealed(boolean revealed) {
		this.dataManager.set(REVEALED, revealed);
	}

	public boolean isAttached() {
		return this.dataManager.get(ATTACHED);
	}

	/** Resolve the victim on either side so rendering can follow its position without network lag. */
	public EntityLivingBase getAttachedTarget() {
		if (!this.isAttached()) {
			return null;
		}
		if (!this.world.isRemote) {
			return this.attachedTarget;
		}
		Entity target = this.world.getEntityByID(this.dataManager.get(ATTACHED_TARGET_ID));
		return target instanceof EntityLivingBase ? (EntityLivingBase) target : null;
	}

	/**
	 * True while this slug is latched onto a living victim's back. Movement AI must stand down while this is true.
	 */
	public boolean isAttachedActive() {
		return this.attachedTarget != null && this.attachedTarget.isEntityAlive() && this.dataManager.get(ATTACHED);
	}

	/** Starts the telegraphed melee pose. The timer is synchronized so nearby clients see the same wind-up. */
	public void startAttackAnimation() {
		this.dataManager.set(ATTACK_ANIMATION_TICKS, ATTACK_ANIMATION_DURATION);
	}

	/** 0 is neutral, 1 is the end of the bite/lunge follow-through. */
	public float getAttackAnimationProgress(float partialTicks) {
		int ticks = this.dataManager.get(ATTACK_ANIMATION_TICKS);
		if (ticks <= 0) {
			return 0.0F;
		}
		float elapsed = ATTACK_ANIMATION_DURATION - (ticks - partialTicks);
		return MathHelper.clamp(elapsed / ATTACK_ANIMATION_DURATION, 0.0F, 1.0F);
	}

	// === Climbing helpers used by AISlugClimbHunt ===

	public static boolean isClimbableSurface(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		return !state.getBlock().isAir(state, world, pos) && state.getMaterial().isSolid();
	}

	/**
	 * Checks whether the block behind the given facing (relative to this entity's centre) is still solid,
	 * i.e. whether the slug is still legitimately clinging to that surface.
	 */
	public boolean isSurfaceStillSolid(EnumFacing facing) {
		BlockPos against = this.getCenterPos().offset(facing.getOpposite());
		return isClimbableSurface(this.world, against);
	}

	public BlockPos getCenterPos() {
		return new BlockPos(this.posX, this.posY + this.height * 0.5D, this.posZ);
	}

	/**
	 * Attempts to find a still-solid face to cling to, trying the current facing first, then the four
	 * faces perpendicular to it (corner-turning), skipping the opposite of {@code current}.
	 * Returns {@code null} if no adjacent solid face remains, meaning the slug should fall.
	 */
	public EnumFacing findSupportingFace(EnumFacing current) {
		if (this.isSurfaceStillSolid(current)) {
			return current;
		}
		BlockPos center = this.getCenterPos();
		for (EnumFacing candidate : EnumFacing.values()) {
			if (candidate == current || candidate == current.getOpposite()) {
				continue;
			}
			if (isClimbableSurface(this.world, center.offset(candidate.getOpposite()))) {
				return candidate;
			}
		}
		return null;
	}

	private static final EnumFacing[] HORIZONTALS = {EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST};

	/**
	 * Checks the four horizontal neighbours of this slug's centre for a climbable face, returning the
	 * outward normal to cling to if one is immediately at hand. Shared by the hunting and idling AI so a
	 * slug latches onto a wall it's already standing next to instead of only ever pathing to one further off.
	 */
	public EnumFacing findAdjacentWall() {
		BlockPos center = this.getCenterPos();
		for (EnumFacing dir : HORIZONTALS) {
			if (isClimbableSurface(this.world, center.offset(dir))) {
				return dir.getOpposite();
			}
		}
		return null;
	}

	/**
	 * Whether there's a climbable ceiling directly above this slug's centre - used to prefer hopping onto
	 * a ceiling over continuing along a wall, both when actively hunting and when idly clinging.
	 */
	public boolean hasClimbableCeilingAbove() {
		return isClimbableSurface(this.world, this.getCenterPos().up());
	}

	/**
	 * Searches straight up from {@code base} (skipping {@code minHeight} blocks first) for the underside of
	 * a ceiling, returning the open position just below it to perch in, or {@code null} if none is found
	 * within {@code maxHeight} blocks (or the spot right below it is itself not open).
	 */
	public static BlockPos findCeilingPerch(World world, BlockPos base, int minHeight, int maxHeight) {
		for (int dy = minHeight; dy <= maxHeight; dy++) {
			BlockPos above = base.up(dy);
			if (isClimbableSurface(world, above)) {
				BlockPos perchPos = above.down();
				return !isClimbableSurface(world, perchPos) ? perchPos : null;
			}
		}
		return null;
	}

	/**
	 * Searches the given horizontal radius range, in each cardinal direction and at a few heights above
	 * {@code base}, for a climbable wall face with open air beside it to perch in.
	 */
	public static BlockPos findNearbyWallPerch(World world, BlockPos base, int minRadius, int maxRadius) {
		for (int r = minRadius; r <= maxRadius; r++) {
			for (EnumFacing dir : HORIZONTALS) {
				for (int dy = 0; dy <= 2; dy++) {
					BlockPos wallPos = base.up(dy).offset(dir, r);
					if (isClimbableSurface(world, wallPos)) {
						BlockPos perchPos = wallPos.offset(dir.getOpposite());
						if (!isClimbableSurface(world, perchPos)) {
							return perchPos;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Applies one tick of surface-crawling movement towards {@code goal} (or holds position if {@code goal} is null),
	 * gluing the slug to the given outward-facing surface normal. Called every tick from the hunting AI while climbing.
	 */
	public void tickClimbMovement(EnumFacing facing, BlockPos goal, double speed) {
		this.setNoGravity(true);
		this.fallDistance = 0.0F;

		// Re-assert flush contact every tick rather than trusting collision to maintain it on its own: moving
		// along a wall means the tiny constant adhesion into it is deliberately clipped to ~0 by collision
		// every single tick, which is exactly the condition vanilla's step-up assist (Entity#move) treats as
		// "maybe I should climb over this" - it can't tell a full wall we're pressing into on purpose apart
		// from a genuine half-block step, and nudges the entity's position while trying to find out. Snapping
		// back to the correct flush position afterwards sidesteps that entirely instead of fighting it.
		this.snapToSurface(facing);

		double nx = facing.getXOffset();
		double ny = facing.getYOffset();
		double nz = facing.getZOffset();

		double mx = 0.0D, my = 0.0D, mz = 0.0D;
		if (goal != null) {
			double dx = (goal.getX() + 0.5D) - this.posX;
			double dy = (goal.getY() + 0.5D) - this.posY;
			double dz = (goal.getZ() + 0.5D) - this.posZ;
			// project the desired direction onto the plane perpendicular to the surface normal
			double dot = dx * nx + dy * ny + dz * nz;
			dx -= dot * nx;
			dy -= dot * ny;
			dz -= dot * nz;
			double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
			if (len > 0.05D) {
				mx = dx / len * speed;
				my = dy / len * speed;
				mz = dz / len * speed;
				this.updateYawForMovement(mx, my, mz, facing);
			}
		}

		// gentle constant adhesion into the surface, mainly to resist getting shoved off by incidental pushes
		// (e.g. other entities bumping into it) between the per-tick snaps above
		mx -= nx * 0.03D;
		my -= ny * 0.03D;
		mz -= nz * 0.03D;

		this.motionX = mx;
		this.motionY = my;
		this.motionZ = mz;
	}

	@Override
	public void move(MoverType type, double x, double y, double z) {
		EnumFacing facing = this.getClimbFacing();
		if (!this.world.isRemote && this.isClimbing() && this.getAttackTarget() == null
				&& !this.isRevealed() && this.isSurfaceStillSolid(facing)) {
			// Check the whole move, including pushes, before an idle slug can cross an unsupported edge.
			// Keep adhesion along the normal but stop travel along the surface when the path has a gap.
			int steps = Math.max(1, (int) Math.ceil(Math.max(Math.abs(x), Math.max(Math.abs(y), Math.abs(z))) / 0.25D));
			for (int step = 1; step <= steps; step++) {
				double fraction = (double) step / steps;
				BlockPos center = new BlockPos(this.posX + x * fraction,
						this.posY + this.height * 0.5D + y * fraction, this.posZ + z * fraction);
				if (!isClimbableSurface(this.world, center.offset(facing.getOpposite()))) {
					if (facing.getAxis() != EnumFacing.Axis.X) this.motionX = x = 0.0D;
					if (facing.getAxis() != EnumFacing.Axis.Y) this.motionY = y = 0.0D;
					if (facing.getAxis() != EnumFacing.Axis.Z) this.motionZ = z = 0.0D;
					break;
				}
			}
		}
		super.move(type, x, y, z);
	}

	/** Maximum degrees {@link #updateYawForMovement} may turn the model per tick - see that method. */
	private static final float MAX_CLIMB_TURN_PER_TICK = 12.0F;

	/**
	 * Points the model at the direction it's actually crawling in, accounting for the extra tip rotation
	 * {@code RenderSlug} applies on top of yaw while climbing a wall or ceiling (see its derivation there):
	 * each case below is the algebraic inverse of that tip, mapping the desired world-space travel direction
	 * back into the "pre-tip" horizontal direction the ordinary ground-yaw formula expects.
	 * <p>
	 * The turn is rate-limited rather than snapped straight to the target heading: while hovering almost
	 * directly above a stalked target (the intended ceiling ambush position), the horizontal offset driving
	 * this angle is tiny, so ordinary target movement can swing the raw target heading wildly tick to tick.
	 * Vanilla ground movement avoids this the same way (see {@code EntityMoveHelper#limitAngle}); this custom
	 * climbing movement bypasses that path entirely, so it needs its own equivalent clamp.
	 */
	private void updateYawForMovement(double mx, double my, double mz, EnumFacing facing) {
		if (mx * mx + my * my + mz * mz > 1.0E-5D) {
			double dx, dz;
			switch (facing) {
				case DOWN:
					dx = mx;
					dz = -mz;
					break;
				case NORTH:
					dx = mx;
					dz = my;
					break;
				case SOUTH:
					dx = mx;
					dz = -my;
					break;
				case EAST:
					dx = -my;
					dz = mz;
					break;
				case WEST:
					dx = my;
					dz = mz;
					break;
				default:
					dx = mx;
					dz = mz;
					break;
			}
			float targetYaw = (float) (MathHelper.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
			float delta = MathHelper.clamp(MathHelper.wrapDegrees(targetYaw - this.rotationYaw),
					-MAX_CLIMB_TURN_PER_TICK, MAX_CLIMB_TURN_PER_TICK);
			float yaw = this.rotationYaw + delta;
			this.rotationYaw = yaw;
			this.renderYawOffset = yaw;
		}
	}

	/**
	 * Stops clinging and resumes ordinary gravity/physics (used both when falling off and when starting a drop attack).
	 */
	public void releaseSurface() {
		this.setClimbFacing(EnumFacing.UP);
		this.setNoGravity(false);
	}

	public void beginDropAttack(EntityLivingBase target) {
		this.releaseSurface();
		this.dropAttacking = true;
		double dx = target.posX - this.posX;
		double dz = target.posZ - this.posZ;
		double len = Math.sqrt(dx * dx + dz * dz);
		if (len > 1.0E-4D) {
			this.motionX = dx / len * 0.12D;
			this.motionZ = dz / len * 0.12D;
		}
		this.motionY = Math.min(this.motionY, 0.0D);
	}

	public boolean isDropAttacking() {
		return this.dropAttacking;
	}

	public void cancelDropAttack() {
		this.dropAttacking = false;
	}

	/**
	 * Called by the hunting AI once the falling slug reaches its target: latches on and starts the damage-over-time.
	 */
	public void attachTo(EntityLivingBase target) {
		this.attachedTarget = target;
		this.attachTicks = 0;
		this.dropAttacking = false;
		this.dataManager.set(ATTACHED_TARGET_ID, target.getEntityId());
		this.dataManager.set(ATTACHED, true);
		this.setClimbFacing(EnumFacing.UP);
		this.setNoGravity(true);
		this.motionX = this.motionY = this.motionZ = 0.0D;
		this.noClip = true;
		this.getNavigator().clearPath();
		this.playSound(this.getAttachSound(), 1.0F, 0.9F + this.rand.nextFloat() * 0.2F);
	}

	public void detach() {
		this.attachedTarget = null;
		this.dataManager.set(ATTACHED, false);
		this.dataManager.set(ATTACHED_TARGET_ID, -1);
		this.noClip = false;
		this.setNoGravity(false);
		this.motionX = this.motionY = this.motionZ = 0.0D;
		this.fallDistance = 0.0F;
	}

	private void updateAttached() {
		EntityLivingBase target = this.attachedTarget;
		if (target == null || !target.isEntityAlive() || target.world != this.world || this.getDistanceSq(target) > 400.0D) {
			this.detach();
			return;
		}

		this.attachTicks++;
		this.motionX = this.motionY = this.motionZ = 0.0D;
		this.fallDistance = 0.0F;

		float yaw = target.renderYawOffset;
		float yawRad = yaw * ((float) Math.PI / 180F);
		double backOffset = target.width * 0.5D + this.width * 0.35D;
		this.setPosition(target.posX + MathHelper.sin(yawRad) * backOffset,
				target.posY + target.height * 0.55D,
				target.posZ - MathHelper.cos(yawRad) * backOffset);
		this.rotationYaw = yaw;
		this.renderYawOffset = yaw;
		this.rotationYawHead = yaw;

		int interval = Math.max(1, Settings.mobSettings.slugAttachDamageInterval);
		if (this.attachTicks % interval == 0) {
			target.attackEntityFrom(DamageSource.causeMobDamage(this), (float) Settings.mobSettings.slugAttachDamageAmount);
			this.onAttachDamageTick(target);
		}

		if (this.attachTicks >= Settings.mobSettings.slugMaxAttachDuration) {
			this.detach();
			this.setRevealed(true);
		}
	}

	/** Overridable hook for species-specific effects applied alongside the periodic attach damage. */
	protected void onAttachDamageTick(EntityLivingBase target) {
	}

	protected net.minecraft.util.SoundEvent getAttachSound() {
		return net.minecraft.init.SoundEvents.ENTITY_SLIME_ATTACK;
	}

	@Override
	protected boolean isMovementBlocked() {
		return this.isAttachedActive() || super.isMovementBlocked();
	}

	@Override
	public void fall(float distance, float damageMultiplier) {
		if (this.dropAttacking) {
			this.fallDistance = 0.0F;
			return;
		}
		super.fall(distance, damageMultiplier);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		boolean wasAttached = this.isAttachedActive();
		boolean hurt = super.attackEntityFrom(source, amount);
		if (hurt && !this.world.isRemote && !this.isRevealed()) {
			this.setRevealed(true);
			if (wasAttached) {
				this.detach();
			}
			if (this.isClimbing()) {
				this.releaseSurface();
			}
		}
		return hurt;
	}

	@Override
	public void onLivingUpdate() {
		if (!this.world.isRemote && this.restoreClimbSurface) {
			this.restoreClimbSurface = false;
			// Older saves have NoGravity but no facing. Recover their ceiling/wall once the world is ready.
			EnumFacing support = this.findSupportingFace(this.isClimbing() ? this.getClimbFacing() : EnumFacing.DOWN);
			if (support != null && support != EnumFacing.UP) {
				this.setClimbFacing(support);
				this.setNoGravity(true);
			} else {
				this.releaseSurface();
			}
		}
		super.onLivingUpdate();
		if (!this.world.isRemote) {
			int attackTicks = this.dataManager.get(ATTACK_ANIMATION_TICKS);
			if (attackTicks > 0) {
				this.dataManager.set(ATTACK_ANIMATION_TICKS, attackTicks - 1);
			}
			if (this.isAttachedActive()) {
				this.updateAttached();
			} else if (this.dropAttacking && this.onGround) {
				this.cancelDropAttack();
			}

			// Watchdog against sitting idle with a target but no climbable surface in reach: this is tracked
			// here (driven by real per-tick state) rather than inside the hunting AI task itself, so a brief
			// target-acquisition hiccup restarting that task can't silently reset the countdown forever.
			if (!this.isRevealed() && !this.isAttachedActive() && !this.isClimbing() && !this.dropAttacking
					&& this.getAttackTarget() != null) {
				if (++this.groundStallTicks > GROUND_STALL_GIVE_UP_TICKS) {
					this.setRevealed(true);
				}
			} else {
				this.groundStallTicks = 0;
			}
		}
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		this.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
		return super.attackEntityAsMob(entityIn);
	}
}
