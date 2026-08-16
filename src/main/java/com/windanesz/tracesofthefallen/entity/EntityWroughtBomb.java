package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityWroughtBomb extends Entity implements IEntityMultiPart {

	private static final DataParameter<Integer> FUSE = EntityDataManager.createKey(EntityWroughtBomb.class, DataSerializers.VARINT);

	private final MultiPartEntityPart casePart;
	private final MultiPartEntityPart fusePart;
	private final MultiPartEntityPart[] bombParts;
	private EntityLivingBase igniter;

	public EntityWroughtBomb(World worldIn) {
		super(worldIn);
		this.casePart = new BombPart(this, "case", 0.5F, 0.5F);
		this.fusePart = new BombPart(this, "fuse", 0.25F, 0.25F);
		this.bombParts = new MultiPartEntityPart[] { this.casePart, this.fusePart };
		this.setSize(0.5F, 0.75F);
	}

	public EntityWroughtBomb(World worldIn, double x, double y, double z, EntityLivingBase igniter) {
		this(worldIn);
		this.setPosition(x, y, z);
		float f = (float)(worldIn.rand.nextDouble() * (Math.PI * 2D));
		this.motionX = (double)(-((float)Math.sin((double)f)) * 0.02F);
		this.motionY = 0.2D;
		this.motionZ = (double)(-((float)Math.cos((double)f)) * 0.02F);
		this.setFuse(Settings.miscSettings.wroughtBombFuseTime);
		this.prevPosX = x;
		this.prevPosY = y;
		this.prevPosZ = z;
		this.igniter = igniter;
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(FUSE, Settings.miscSettings.wroughtBombFuseTime);
	}

	@Override
	public Entity[] getParts() {
		return this.bombParts;
	}

	@Override
	public World getWorld() {
		return this.world;
	}

	@Override
	public boolean attackEntityFromPart(MultiPartEntityPart part, DamageSource source, float damage) {
		if (this.isEntityInvulnerable(source)) return false;

		boolean isFire = source.isFireDamage() || (source.getImmediateSource() != null && source.getImmediateSource().isBurning());

		if (this.getFuse() <= 0) {
			if (isFire) {
				if (!this.world.isRemote) {
					this.setFuse(Settings.miscSettings.wroughtBombFuseTime);
					this.world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
				}
			} else {
				this.convertToBlock(false);
			}
			return true;
		}

		if (part == this.fusePart) {
			if (this.getFuse() > 0 && !isFire) {
				this.convertToBlock(true);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isEntityInvulnerable(source)) return false;
		boolean isFire = source.isFireDamage() || (source.getImmediateSource() != null && source.getImmediateSource().isBurning());
		if (this.getFuse() <= 0) {
			if (isFire) {
				if (!this.world.isRemote) {
					this.setFuse(Settings.miscSettings.wroughtBombFuseTime);
					this.world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
				}
			} else {
				this.convertToBlock(false);
			}
			return true;
		}
		return false;
	}

	public void convertToBlock(boolean playExtinguish) {
		if (this.isDead) return;
		Entity riding = this.getRidingEntity();
		this.setDead();
		if (!this.world.isRemote) {
			if (playExtinguish) {
				this.world.playSound(null, posX, posY, posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.8F);
			}
			if (riding != null || !this.onGround) {
				this.entityDropItem(new ItemStack(ModBlocks.wrought_bomb), 0.0F);
			} else {
				BlockPos pos = new BlockPos(this.posX, this.posY + 0.1D, this.posZ);
				if (this.world.isAirBlock(pos) || this.world.getBlockState(pos).getBlock().isReplaceable(this.world, pos)) {
					this.world.setBlockState(pos, ModBlocks.wrought_bomb.getDefaultState());
				} else {
					this.entityDropItem(new ItemStack(ModBlocks.wrought_bomb), 0.0F);
				}
			}
		}
	}

	public void extinguishAndConvertToBlock() {
		this.convertToBlock(true);
	}

	@Override
	public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (!stack.isEmpty() && (stack.getItem() == Items.FLINT_AND_STEEL || stack.getItem() == Items.FIRE_CHARGE)) {
			if (this.getFuse() <= 0) {
				if (!this.world.isRemote) {
					this.setFuse(Settings.miscSettings.wroughtBombFuseTime);
					this.world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
				}
				if (stack.getItem() == Items.FLINT_AND_STEEL) {
					stack.damageItem(1, player);
				} else if (!player.capabilities.isCreativeMode) {
					stack.shrink(1);
				}
				return true;
			}
		}
		return super.processInitialInteract(player, hand);
	}

	@Override
	public boolean canBeCollidedWith() {
		return false;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		if (!this.isRiding()) {
			if (!this.hasNoGravity()) {
				this.motionY -= 0.04D;
			}

			this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
			this.motionX *= 0.98D;
			this.motionY *= 0.98D;
			this.motionZ *= 0.98D;
			if (this.onGround) {
				this.motionX *= 0.7D;
				this.motionZ *= 0.7D;
				this.motionY *= -0.5D;
			}
		} else {
			this.motionX = 0;
			this.motionY = 0;
			this.motionZ = 0;
		}

		if (this.getRidingEntity() instanceof EntityGoblinEngineer) {
			EntityGoblinEngineer eng = (EntityGoblinEngineer) this.getRidingEntity();
			float yaw = eng.renderYawOffset * (float)(Math.PI / 180.0);
			double xOffset = (double)MathHelper.sin(yaw) * 0.35D;
			double zOffset = -(double)MathHelper.cos(yaw) * 0.35D;
			this.casePart.setLocationAndAngles(this.posX + xOffset, this.posY, this.posZ + zOffset, 0.0F, 0.0F);
			this.fusePart.setLocationAndAngles(this.posX + xOffset, this.posY + 0.5D, this.posZ + zOffset, 0.0F, 0.0F);
		} else {
			this.casePart.setLocationAndAngles(this.posX, this.posY, this.posZ, 0.0F, 0.0F);
			this.fusePart.setLocationAndAngles(this.posX, this.posY + 0.5D, this.posZ, 0.0F, 0.0F);
		}

		if (this.getFuse() > 0) {
			int fuse = this.getFuse() - 1;
			this.setFuse(fuse);

			if (fuse <= 0) {
				this.setDead();
				if (!this.world.isRemote) {
					this.explode();
				}
			} else {
				this.handleWaterMovement();
				if (this.world.isRemote) {
					this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + 0.7D, this.posZ, 0.0D, 0.05D, 0.0D);
					this.world.spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + 0.7D, this.posZ, 0.0D, 0.0D, 0.0D);
				}
			}
		} else {
			this.handleWaterMovement();
		}
	}

	private void explode() {
		float explosionPower = 6.0F; // 1.5x standard TNT (4.0F)
		this.world.createExplosion(this, this.posX, this.posY + (double)(this.height / 16.0F), this.posZ, explosionPower, true);
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		compound.setShort("Fuse", (short)this.getFuse());
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		this.setFuse(compound.getShort("Fuse"));
	}

	public void setFuse(int fuseIn) {
		this.dataManager.set(FUSE, fuseIn);
	}

	public int getFuse() {
		return this.dataManager.get(FUSE);
	}

	public EntityLivingBase getIgniter() {
		return this.igniter;
	}

	private class BombPart extends MultiPartEntityPart {
		public BombPart(IEntityMultiPart parent, String partName, float width, float height) {
			super(parent, partName, width, height);
		}

		@Override
		public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
			return EntityWroughtBomb.this.processInitialInteract(player, hand);
		}
	}
}
