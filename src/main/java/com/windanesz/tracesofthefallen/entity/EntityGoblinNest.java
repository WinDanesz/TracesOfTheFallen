package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;

import java.util.List;

public class EntityGoblinNest extends Entity {

	private static final DataParameter<Integer> FACING = EntityDataManager.createKey(EntityGoblinNest.class, DataSerializers.VARINT);

	private float nestHealth = Settings.miscSettings.nestHealth;
	private int broodsSpawned = 0;
	private int spawnTimer = 0;

	public EntityGoblinNest(World worldIn) {
		super(worldIn);
		this.setSize(2.0F, 1.6875F);
	}

	public EntityGoblinNest(World worldIn, double x, double y, double z) {
		this(worldIn);
		this.setPosition(x, y, z);
		this.prevPosX = x;
		this.prevPosY = y;
		this.prevPosZ = z;
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(FACING, EnumFacing.NORTH.getHorizontalIndex());
	}

	public EnumFacing getFacing() {
		return EnumFacing.byHorizontalIndex(this.dataManager.get(FACING));
	}

	public void setFacing(EnumFacing facing) {
		this.dataManager.set(FACING, facing.getHorizontalIndex());
	}

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	@Override
	public boolean canBePushed() {
		return false;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isEntityInvulnerable(source)) {
			return false;
		}
		if (!this.world.isRemote) {
			this.nestHealth -= amount;
			this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_STONE_HIT, SoundCategory.BLOCKS, 0.8F, 1.2F);
			if (this.nestHealth <= 0) {
				this.destroyNest();
			} else if (this.world instanceof WorldServer) {
				((WorldServer) this.world).spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX, this.posY + 0.5D, this.posZ, 10, 0.4D, 0.4D, 0.4D, 0.05D, Block.getStateId(Blocks.COBBLESTONE.getDefaultState()));
			}
		}
		return true;
	}

	private void destroyNest() {
		if (!this.world.isRemote) {
			this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 1.0F, 0.8F);
			if (this.world instanceof WorldServer) {
				((WorldServer) this.world).spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX, this.posY + 0.5D, this.posZ, 30, 0.6D, 0.6D, 0.6D, 0.05D, Block.getStateId(Blocks.COBBLESTONE.getDefaultState()));
				ResourceLocation lootTableLoc = new ResourceLocation(TracesOfTheFallen.MODID, "blocks/goblin_nest");
				LootTable table = this.world.getLootTableManager().getLootTableFromLocation(lootTableLoc);
				LootContext.Builder builder = new LootContext.Builder((WorldServer) this.world);
				for (ItemStack itemstack : table.generateLootForPools(this.rand, builder.build())) {
					this.entityDropItem(itemstack, 0.5F);
				}
			}
			this.setDead();
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if (!this.hasNoGravity()) {
			this.motionY -= 0.04D;
		}
		this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.8D;
		this.motionY *= 0.98D;
		this.motionZ *= 0.8D;
		if (this.onGround) {
			this.motionX = 0;
			this.motionZ = 0;
		}

		if (!this.world.isRemote) {
			if (this.spawnTimer > 0) {
				this.spawnTimer--;
			} else if (this.ticksExisted % 20 == 0) {
				this.trySpawnBrood();
			}
		}
	}

	private void trySpawnBrood() {
		double detectionRange = this.broodsSpawned == 0 ? Settings.miscSettings.nestGoblinDetectionRange : 20.0D;
		boolean validPlayerNearby = false;
		for (EntityPlayer player : this.world.playerEntities) {
			if (player.isEntityAlive() && !player.isCreative() && !player.isSpectator() && this.getDistanceSq(player) <= detectionRange * detectionRange) {
				validPlayerNearby = true;
				break;
			}
		}
		if (!validPlayerNearby) {
			return;
		}

		int broodCount = 0;
		List<EntityGoblin> nearbyGoblins = this.world.getEntitiesWithinAABB(EntityGoblin.class, this.getEntityBoundingBox().grow(20.0D, 10.0D, 20.0D));
		for (EntityGoblin goblin : nearbyGoblins) {
			if (goblin.isEntityAlive() && goblin.getClass() == EntityGoblin.class) {
				broodCount++;
			}
		}

		if (broodCount > 7) {
			return;
		}

		EntityGoblin brood = new EntityGoblin(this.world);
		brood.setPosition(this.posX, this.posY + 0.2D, this.posZ);
		this.world.spawnEntity(brood);
		this.broodsSpawned++;

		if (this.broodsSpawned < 3) {
			this.spawnTimer = 20; // 1 second delay between first 3
		} else {
			this.spawnTimer = 600; // 30 second delay for subsequent spawns
		}
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		this.setFacing(EnumFacing.byHorizontalIndex(compound.getInteger("Facing")));
		if (compound.hasKey("NestHealth")) {
			this.nestHealth = compound.getFloat("NestHealth");
		}
		if (compound.hasKey("BroodsSpawned")) {
			this.broodsSpawned = compound.getInteger("BroodsSpawned");
		}
		if (compound.hasKey("SpawnTimer")) {
			this.spawnTimer = compound.getInteger("SpawnTimer");
		}
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		compound.setInteger("Facing", this.getFacing().getHorizontalIndex());
		compound.setFloat("NestHealth", this.nestHealth);
		compound.setInteger("BroodsSpawned", this.broodsSpawned);
		compound.setInteger("SpawnTimer", this.spawnTimer);
	}
}
