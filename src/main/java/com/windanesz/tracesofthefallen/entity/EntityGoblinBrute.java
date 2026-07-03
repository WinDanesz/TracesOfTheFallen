package com.windanesz.tracesofthefallen.entity;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntityGoblinBrute extends EntityGoblin {
	private boolean tastedBlood = false;

	public EntityGoblinBrute(World worldIn) {
		super(worldIn);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(12.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.28D);
	}

	@Override
	protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
		super.setEquipmentBasedOnDifficulty(difficulty);
		this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		this.tastedBlood = true;
		return super.attackEntityFrom(source, amount);
	}

	@Override
	public boolean attackEntityAsMob(net.minecraft.entity.Entity entityIn) {
		this.tastedBlood = true;
		return super.attackEntityAsMob(entityIn);
	}

	@Override
	public boolean isImmuneToIdol() {
		return this.tastedBlood;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setBoolean("TastedBlood", this.tastedBlood);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		this.tastedBlood = compound.getBoolean("TastedBlood");
	}
}
