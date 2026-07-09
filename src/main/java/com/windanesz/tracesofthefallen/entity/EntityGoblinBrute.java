package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.init.ModItems;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class EntityGoblinBrute extends EntityGoblin {
	private boolean tastedBlood = false;
	private int rageTimer = 0;
	private int rageCooldown = 0;

	public EntityGoblinBrute(World worldIn) {
		super(worldIn);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(Settings.goblinSettings.goblinBruteMaxHealth);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(Settings.goblinSettings.goblinBruteAttackDamage);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
	}

	@Override
	protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
		super.setEquipmentBasedOnDifficulty(difficulty);
		if (rand.nextBoolean()) {
			this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.STONE_AXE));
		} else {
			this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.primitive_mace));
		}
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (!this.world.isRemote && this.getHeldItemMainhand().isEmpty() && this.ticksExisted < 20) {
			if (this.rand.nextBoolean()) {
				this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.STONE_AXE));
			} else {
				this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.primitive_mace));
			}
		}

		if (!this.world.isRemote) {
			if (this.rageTimer > 0) {
				this.rageTimer--;
				if (this.rageTimer == 0) {
					this.rageCooldown = 600;
					this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
				} else if (this.rageTimer > 200 && this.world instanceof WorldServer) {
					((WorldServer) this.world).spawnParticle(EnumParticleTypes.REDSTONE,
							this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width,
							this.posY + this.rand.nextDouble() * (double)this.height,
							this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width,
							3, 0.2D, 0.2D, 0.2D, 0.0D);
				}
			} else if (this.rageCooldown > 0) {
				this.rageCooldown--;
			} else if (this.getAttackTarget() != null && this.getAttackTarget().isEntityAlive()) {
				this.rageTimer = 80;
				this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.28D);
				if (this.world instanceof WorldServer) {
					((WorldServer) this.world).spawnParticle(EnumParticleTypes.VILLAGER_ANGRY,
							this.posX,
							this.posY + (double)this.height + 0.2D,
							this.posZ,
							2, 0.2D, 0.2D, 0.2D, 0.0D);
					((WorldServer) this.world).spawnParticle(EnumParticleTypes.REDSTONE,
							this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width,
							this.posY + this.rand.nextDouble() * (double)this.height,
							this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width,
							5, 0.2D, 0.2D, 0.2D, 0.0D);
				}
			}
		}
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
		compound.setInteger("RageTimer", this.rageTimer);
		compound.setInteger("RageCooldown", this.rageCooldown);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		this.tastedBlood = compound.getBoolean("TastedBlood");
		this.rageTimer = compound.getInteger("RageTimer");
		this.rageCooldown = compound.getInteger("RageCooldown");
	}
}
