package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityDungeonSlug extends EntitySlug {

	public static final ResourceLocation LOOT_TABLE = new ResourceLocation(TracesOfTheFallen.MODID, "entities/dungeon_slug");

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
