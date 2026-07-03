package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.entity.ai.GoblinAIEngineerBreachWall;
import com.windanesz.tracesofthefallen.entity.ai.GoblinAIEngineerEscape;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityGoblinEngineer extends EntityGoblin {
	private static final DataParameter<Boolean> POINTING = EntityDataManager.createKey(EntityGoblinEngineer.class, DataSerializers.BOOLEAN);

	public EntityGoblinEngineer(World worldIn) {
		super(worldIn);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(POINTING, false);
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(1, new GoblinAIEngineerEscape(this));
		this.tasks.addTask(2, new GoblinAIEngineerBreachWall(this));
	}

	public boolean isPointing() {
		return this.dataManager.get(POINTING);
	}

	public void setPointing(boolean pointing) {
		this.dataManager.set(POINTING, pointing);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
	}

	@Override
	protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
		super.setEquipmentBasedOnDifficulty(difficulty);
		this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
	}

	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
		livingdata = super.onInitialSpawn(difficulty, livingdata);
		if (!this.world.isRemote) {
			EntityWroughtBomb bomb = new EntityWroughtBomb(this.world, this.posX, this.posY, this.posZ, this);
			bomb.setFuse(-1);
			this.world.spawnEntity(bomb);
			bomb.startRiding(this);
		}
		return livingdata;
	}


	@Override
	public double getMountedYOffset() {
		if (!this.getPassengers().isEmpty() && this.getPassengers().get(0) instanceof EntityWroughtBomb) {
			return 0.45D;
		}
		return super.getMountedYOffset();
	}
}
