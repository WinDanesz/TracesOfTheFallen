package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.entity.ai.AISlugClimbHunt;
import com.windanesz.tracesofthefallen.entity.ai.AISlugMeleeAttack;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
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

	/** How long a slug may sit on the ground stalking a target with no climbable surface in reach before giving up on stealth. */
	private static final int GROUND_STALL_GIVE_UP_TICKS = 100;

	private EntityLivingBase attachedTarget;
	private int attachTicks;
	private boolean dropAttacking;
	private int groundStallTicks;

	protected EntitySlug(World worldIn) {
		super(worldIn);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(CLIMB_FACING, EnumFacing.UP);
		this.dataManager.register(REVEALED, false);
		this.dataManager.register(ATTACHED, false);
	}

	@Override
	protected void initEntityAI() {
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new AISlugMeleeAttack(this, this.getMeleeSpeed()));
		this.tasks.addTask(2, new AISlugClimbHunt(this, Settings.mobSettings.slugClimbSpeed));
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
		}
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

	/**
	 * True while this slug is latched onto a living victim's back. Movement AI must stand down while this is true.
	 */
	public boolean isAttachedActive() {
		return this.attachedTarget != null && this.attachedTarget.isEntityAlive() && this.dataManager.get(ATTACHED);
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

	/**
	 * Applies one tick of surface-crawling movement towards {@code goal} (or holds position if {@code goal} is null),
	 * gluing the slug to the given outward-facing surface normal. Called every tick from the hunting AI while climbing.
	 */
	public void tickClimbMovement(EnumFacing facing, BlockPos goal, double speed) {
		this.setNoGravity(true);
		this.fallDistance = 0.0F;

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

		// gentle constant adhesion into the surface; block collision below clips the slug flush against it
		mx -= nx * 0.03D;
		my -= ny * 0.03D;
		mz -= nz * 0.03D;

		this.motionX = mx;
		this.motionY = my;
		this.motionZ = mz;
	}

	/**
	 * Points the model at the direction it's actually crawling in, accounting for the extra tip rotation
	 * {@code RenderSlug} applies on top of yaw while climbing a wall or ceiling (see its derivation there):
	 * each case below is the algebraic inverse of that tip, mapping the desired world-space travel direction
	 * back into the "pre-tip" horizontal direction the ordinary ground-yaw formula expects.
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
			float yaw = (float) (MathHelper.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
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
		super.onLivingUpdate();
		if (!this.world.isRemote) {
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
