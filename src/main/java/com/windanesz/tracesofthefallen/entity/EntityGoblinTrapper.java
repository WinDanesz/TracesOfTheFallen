package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import java.util.List;

public class EntityGoblinTrapper extends EntityGoblin {
	public EntityGoblinTrapper(World worldIn) {
		super(worldIn);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(Settings.goblinSettings.goblinTrapperMaxHealth);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(Settings.goblinSettings.goblinTrapperAttackDamage);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0D);
	}

	private int trapCooldown = 0;

	@Override
	protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
		super.setEquipmentBasedOnDifficulty(difficulty);
		this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.WOODEN_SHOVEL));
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();

		if (!this.world.isRemote && this.getHeldItemMainhand().isEmpty() && this.ticksExisted < 20) {
			this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.WOODEN_SHOVEL));
		}

		if (!this.world.isRemote) {
			if (this.trapCooldown > 0) {
				this.trapCooldown--;
			}
			if (this.getAttackTarget() != null && this.trapCooldown == 0) {
				double distSq = this.getDistanceSq(this.getAttackTarget());
				if (distSq > 4.0D && distSq < 144.0D && this.onGround && this.rand.nextInt(30) == 0) {
					int chunkX = MathHelper.floor(this.posX) >> 4;
					int chunkZ = MathHelper.floor(this.posZ) >> 4;
					AxisAlignedBB chunkBounds = new AxisAlignedBB(chunkX << 4, 0.0D, chunkZ << 4, (chunkX + 1) << 4, 256.0D, (chunkZ + 1) << 4);
					List<EntityJawTrap> traps = this.world.getEntitiesWithinAABB(EntityJawTrap.class, chunkBounds);
					if (traps.size() < 3) {
						EntityJawTrap trap = new EntityJawTrap(this.world, this.posX, this.posY, this.posZ);
						trap.rotationYaw = this.rotationYaw;
						trap.prevRotationYaw = this.rotationYaw;
						this.world.spawnEntity(trap);
						this.trapCooldown = 160 + this.rand.nextInt(80);
					} else {
						this.trapCooldown = 60;
					}
				}
			}
		}
	}
}
