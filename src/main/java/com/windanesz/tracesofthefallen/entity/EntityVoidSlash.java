package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockVine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.HashSet;
import java.util.Set;

public class EntityVoidSlash extends EntityThrowable {

	private static final DataParameter<Integer> THROWER_ID = EntityDataManager.createKey(EntityVoidSlash.class, DataSerializers.VARINT);
	private final Set<Integer> hitEntities = new HashSet<Integer>();

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(THROWER_ID, -1);
	}

	public EntityVoidSlash(World worldIn) {
		super(worldIn);
	}

	public EntityVoidSlash(World worldIn, EntityLivingBase throwerIn, EntityLivingBase targetIn) {
		super(worldIn, throwerIn);
		this.dataManager.set(THROWER_ID, throwerIn.getEntityId());

		// Place projectile high up around shoulder/head level when summoned
		this.setPosition(throwerIn.posX, throwerIn.posY + 1.4D, throwerIn.posZ);

		double d0 = targetIn.posX - throwerIn.posX;
		double d1 = (targetIn.posY + (double) (targetIn.height * 0.5F)) - this.posY;
		double d2 = targetIn.posZ - throwerIn.posZ;
		this.shoot(d0, d1, d2, 0.65F, 1.0F);
	}

	public EntityVoidSlash(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
	}

	@Override
	protected float getGravityVelocity() {
		return 0.0F; // Zero gravity so slow projectile travels straight
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (!this.world.isRemote && this.ticksExisted > 1200) {
			this.setDead();
			return;
		}

//		if (this.world.isRemote) {
//			for (int i = 0; i < 3; i++) {
//				this.world.spawnParticle(EnumParticleTypes.SPELL_WITCH,
//						this.posX + (this.rand.nextDouble() - 0.5D) * 0.8D,
//						this.posY + (this.rand.nextDouble() - 0.5D) * 0.8D,
//						this.posZ + (this.rand.nextDouble() - 0.5D) * 0.8D,
//						(this.rand.nextDouble() - 0.5D) * 0.05D,
//						0.02D,
//						(this.rand.nextDouble() - 0.5D) * 0.05D);
//				if (this.rand.nextBoolean()) {
//					this.world.spawnParticle(EnumParticleTypes.PORTAL,
//							this.posX + (this.rand.nextDouble() - 0.5D) * 0.6D,
//							this.posY + (this.rand.nextDouble() - 0.5D) * 0.6D,
//							this.posZ + (this.rand.nextDouble() - 0.5D) * 0.6D,
//							(this.rand.nextDouble() - 0.5D) * 0.1D,
//							(this.rand.nextDouble() - 0.5D) * 0.1D,
//							(this.rand.nextDouble() - 0.5D) * 0.1D);
//				}
//			}
//		}
	}

	@Override
	protected void onImpact(RayTraceResult result) {
		if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
			IBlockState state = this.world.getBlockState(result.getBlockPos());
			Block block = state.getBlock();
			if (block instanceof BlockBush || block instanceof BlockVine || block.isPassable(this.world, result.getBlockPos())) {
				return;
			}
			if (!this.world.isRemote) {
				this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 1.0F, 0.8F + this.rand.nextFloat() * 0.3F);
				if (this.world instanceof WorldServer) {
					((WorldServer) this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX, this.posY, this.posZ, 20, 0.5D, 0.5D, 0.5D, 0.15D);
					((WorldServer) this.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY, this.posZ, 10, 0.3D, 0.3D, 0.3D, 0.05D);
				}
				this.setDead();
			}
			return;
		}

		if (result.entityHit != null && result.entityHit instanceof EntityLivingBase) {
			if (result.entityHit == this.getThrower() || this.isOwner(result.entityHit) || result.entityHit instanceof EntityGoblin) {
				return;
			}
			int id = result.entityHit.getEntityId();
			if (this.hitEntities.contains(id)) {
				return;
			}
			this.hitEntities.add(id);

			if (!this.world.isRemote) {
				EntityLivingBase target = (EntityLivingBase) result.entityHit;
				target.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, this.getThrower()), (float) Settings.miscSettings.shamanVoidSlashDamage);

				this.world.playSound(null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.HOSTILE, 1.0F, 0.6F + this.rand.nextFloat() * 0.3F);
				this.world.playSound(null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_WITHER_HURT, SoundCategory.HOSTILE, 0.8F, 1.2F);

				if (this.world instanceof WorldServer) {
					((WorldServer) this.world).spawnParticle(EnumParticleTypes.SPELL_WITCH, target.posX, target.posY + target.height * 0.5D, target.posZ, 25, 0.5D, 0.5D, 0.5D, 0.2D);
					((WorldServer) this.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, target.posX, target.posY + target.height * 0.5D, target.posZ, 2, 0.2D, 0.2D, 0.2D, 0.0D);
				}

				// If it has pierced 4 enemies already, dissipate
				if (this.hitEntities.size() >= 4) {
					this.setDead();
				}
			}
		}
	}

	public boolean isOwner(Entity entityIn) {
		if (entityIn == null) return false;
		if (entityIn == this.getThrower()) return true;
		if (this.dataManager.get(THROWER_ID) == entityIn.getEntityId()) return true;
		if (this.getThrower() instanceof com.windanesz.tracesofthefallen.entity.EntityGoblinShaman) {
			return com.windanesz.tracesofthefallen.entity.shaman.ShamanSpells.isAlly((com.windanesz.tracesofthefallen.entity.EntityGoblinShaman) this.getThrower(), entityIn);
		}
		if (this.getThrower() instanceof EntityGoblin && entityIn instanceof EntityGoblin) {
			return ((EntityGoblin) this.getThrower()).isOwner(entityIn);
		}
		return false;
	}
}
