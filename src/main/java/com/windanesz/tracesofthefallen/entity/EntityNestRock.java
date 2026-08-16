package com.windanesz.tracesofthefallen.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityNestRock extends EntityThrowable {

	public EntityNestRock(World worldIn) {
		super(worldIn);
	}

	public EntityNestRock(World worldIn, EntityLivingBase throwerIn) {
		super(worldIn, throwerIn);
	}

	public EntityNestRock(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (result.entityHit != null) {
			if (result.entityHit instanceof EntityGoblinNest || result.entityHit instanceof EntityGoblin) {
				return;
			}
			result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), 2.0F);
		}

		if (!this.world.isRemote) {
			this.world.setEntityState(this, (byte) 3);
			this.setDead();
		}
	}

	@Override
	public void handleStatusUpdate(byte id) {
		if (id == 3) {
			for (int i = 0; i < 8; ++i) {
				this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, this.posX, this.posY, this.posZ, ((double) this.rand.nextFloat() - 0.5D) * 0.08D, ((double) this.rand.nextFloat() - 0.5D) * 0.08D, ((double) this.rand.nextFloat() - 0.5D) * 0.08D, Item.getIdFromItem(Item.getItemFromBlock(Blocks.COBBLESTONE)));
			}
		}
	}
}
