package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.entity.ai.GoblinAIShamanDance;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

public class EntityGoblinShaman extends EntityGoblin {
	private static final DataParameter<Integer> DANCE_TYPE = EntityDataManager.createKey(EntityGoblinShaman.class, DataSerializers.VARINT);

	public EntityGoblinShaman(World worldIn) {
		super(worldIn);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(DANCE_TYPE, 0);
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(4, new GoblinAIShamanDance(this));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(14.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.18D);
	}

	public int getDanceType() {
		return this.dataManager.get(DANCE_TYPE);
	}

	public void setDanceType(int type) {
		this.dataManager.set(DANCE_TYPE, type);
	}

	public int danceCooldown = 0;

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (this.danceCooldown > 0) {
			this.danceCooldown--;
		}
	}

	public boolean isDancing() {
		return this.getDanceType() > 0;
	}

	public void setDancing(boolean dancing) {
		this.setDanceType(dancing ? 1 : 0);
	}
}
