package com.windanesz.tracesofthefallen.entity;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.world.World;

public class EntityGoblinStarved extends EntityGoblin {
	public EntityGoblinStarved(World worldIn) {
		super(worldIn);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.22D);
	}
}
