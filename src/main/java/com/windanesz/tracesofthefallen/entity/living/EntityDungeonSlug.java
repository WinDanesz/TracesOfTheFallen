package com.windanesz.tracesofthefallen.entity.living;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityDungeonSlug extends EntitySlug {

	public static final ResourceLocation LOOT_TABLE = new ResourceLocation(TracesOfTheFallen.MODID, "entities/dungeon_slug");
	private Vec3d lastMucusPosition;

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (!this.world.isRemote) {
			return;
		}
		Vec3d position = this.getPositionVector();
		if (!this.isEntityAlive() || this.isAttached() || this.isInWater() || this.isInLava()
				|| (!this.onGround && !this.isClimbing())) {
			this.lastMucusPosition = null;
			return;
		}
		if (this.lastMucusPosition == null) {
			this.lastMucusPosition = position;
			return;
		}
		double distanceSquared = position.squareDistanceTo(this.lastMucusPosition);
		if (distanceSquared < 0.32D * 0.32D) {
			return;
		}
		this.lastMucusPosition = position;
		if (distanceSquared > 4.0D) {
			return; // Do not draw a trail across teleports.
		}
		EnumFacing facing = this.getClimbFacing();
		Vec3d normal = new Vec3d(facing.getDirectionVec());
		Vec3d center = position.add(0, this.height * 0.5D, 0);
		double reach = (facing.getAxis() == EnumFacing.Axis.Y ? this.height : this.width) * 0.5D + 0.15D;
		RayTraceResult surface = this.world.rayTraceBlocks(center, center.subtract(normal.scale(reach)), false, true, false);
		if (surface != null && surface.typeOfHit == RayTraceResult.Type.BLOCK && surface.sideHit == facing) {
			TracesOfTheFallen.proxy.spawnSlugMucusParticle(this.world, surface.hitVec.x, surface.hitVec.y, surface.hitVec.z, facing);
		}
	}

	public EntityDungeonSlug(World worldIn) {
		super(worldIn);
		this.setSize(0.9F, 0.55F);
		this.experienceValue = 3;
	}

	@Override
	protected double getMeleeSpeed() {
		return 0.22D;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(Settings.mobSettings.dungeonSlugMaxHealth);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(Settings.mobSettings.dungeonSlugAttackDamage);
	}

	@Override
	public boolean getCanSpawnHere() {
		BlockPos pos = new BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ);
		if (pos.getY() >= 55 || this.world.canBlockSeeSky(pos)) {
			return false;
		}
		return super.getCanSpawnHere();
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_SLIME_SQUISH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.ENTITY_SLIME_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_SLIME_DEATH;
	}

	@Override
	protected SoundEvent getAttachSound() {
		return SoundEvents.ENTITY_SLIME_SQUISH;
	}

	@Override
	public EnumCreatureAttribute getCreatureAttribute() {
		return EnumCreatureAttribute.UNDEFINED;
	}

	@Nullable
	@Override
	protected ResourceLocation getLootTable() {
		return LOOT_TABLE;
	}
}
