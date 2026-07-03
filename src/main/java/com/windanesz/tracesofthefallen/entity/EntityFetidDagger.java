package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.init.ModItems;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityFetidDagger extends EntityArrow {

	private static final DataParameter<Boolean> CAN_BE_PICKED_UP = EntityDataManager.createKey(EntityFetidDagger.class, DataSerializers.BOOLEAN);
	private ItemStack daggerStack = new ItemStack(ModItems.fetid_dagger);

	public EntityFetidDagger(World worldIn) {
		super(worldIn);
		this.setDamage(Settings.miscSettings.fetidDaggerThrownDamage);
	}

	public EntityFetidDagger(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
		this.setDamage(Settings.miscSettings.fetidDaggerThrownDamage);
	}

	public EntityFetidDagger(World worldIn, EntityLivingBase shooter, ItemStack stack) {
		super(worldIn, shooter);
		this.daggerStack = stack.copy();
		this.daggerStack.setCount(1);
		this.setDamage(Settings.miscSettings.fetidDaggerThrownDamage);
		boolean canPickup = false;
		if (shooter instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) shooter;
			if (player.capabilities.isCreativeMode) {
				this.pickupStatus = PickupStatus.CREATIVE_ONLY;
			} else {
				this.pickupStatus = PickupStatus.ALLOWED;
			}
			canPickup = true;
		} else {
			this.pickupStatus = PickupStatus.DISALLOWED;
		}
		this.setCanBePickedUp(canPickup);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(CAN_BE_PICKED_UP, true);
	}

	public boolean canBePickedUp() {
		return this.dataManager.get(CAN_BE_PICKED_UP);
	}

	public void setCanBePickedUp(boolean canBePickedUp) {
		this.dataManager.set(CAN_BE_PICKED_UP, canBePickedUp);
		if (canBePickedUp) {
			if (this.pickupStatus == PickupStatus.DISALLOWED) {
				this.pickupStatus = PickupStatus.ALLOWED;
			}
		} else {
			this.pickupStatus = PickupStatus.DISALLOWED;
		}
	}

	@Override
	protected ItemStack getArrowStack() {
		return this.daggerStack.isEmpty() ? new ItemStack(ModItems.fetid_dagger) : this.daggerStack.copy();
	}

	public ItemStack getDaggerStack() {
		return this.getArrowStack();
	}

	public boolean isInGround() {
		return this.inGround;
	}

	@Override
	protected void arrowHit(EntityLivingBase living) {
		super.arrowHit(living);
		living.addPotionEffect(new PotionEffect(MobEffects.POISON, 80, 0));
	}

	@Override
	protected void onHit(RayTraceResult raytraceResultIn) {
		if (raytraceResultIn.entityHit != null) {
			float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
			if (f > 0.0001D) {
				this.setDamage(Settings.miscSettings.fetidDaggerThrownDamage / (double)f);
			}
		}
		super.onHit(raytraceResultIn);
		this.setDamage(Settings.miscSettings.fetidDaggerThrownDamage);
		if (!this.world.isRemote && raytraceResultIn.entityHit != null && this.isDead && this.canBePickedUp() && this.pickupStatus == PickupStatus.ALLOWED) {
			this.entityDropItem(this.getDaggerStack(), 0.1F);
		}
	}

	@Override
	public void onCollideWithPlayer(EntityPlayer entityIn) {
		if (!this.world.isRemote && this.inGround && this.arrowShake <= 0) {
			if (this.canBePickedUp()) {
				boolean flag = this.pickupStatus == PickupStatus.ALLOWED || (this.pickupStatus == PickupStatus.CREATIVE_ONLY && entityIn.capabilities.isCreativeMode);
				if (flag && entityIn.inventory.addItemStackToInventory(this.getDaggerStack())) {
					this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
					entityIn.onItemPickup(this, 1);
					this.setDead();
				}
			}
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setBoolean("CanBePickedUp", this.canBePickedUp());
		if (this.daggerStack != null && !this.daggerStack.isEmpty()) {
			compound.setTag("DaggerStack", this.daggerStack.writeToNBT(new NBTTagCompound()));
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		if (compound.hasKey("CanBePickedUp")) {
			this.setCanBePickedUp(compound.getBoolean("CanBePickedUp"));
		}
		if (compound.hasKey("DaggerStack")) {
			this.daggerStack = new ItemStack(compound.getCompoundTag("DaggerStack"));
		}
	}
}
