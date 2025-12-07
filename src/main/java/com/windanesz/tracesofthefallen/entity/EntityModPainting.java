package com.windanesz.tracesofthefallen.entity;

import com.google.common.base.Optional;
import com.mojang.authlib.GameProfile;
import com.windanesz.tracesofthefallen.capability.HauntingCapability;
import com.windanesz.tracesofthefallen.init.ModItems;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntityModPainting extends EntityHanging implements IEntityAdditionalSpawnData {

	protected static final DataParameter<Float> ROTATION = EntityDataManager.createKey(EntityModPainting.class, DataSerializers.FLOAT);
	protected static final DataParameter<String> PAINTING = EntityDataManager.createKey(EntityModPainting.class, DataSerializers.STRING);
	protected static final DataParameter<Integer> SIZE_X = EntityDataManager.createKey(EntityModPainting.class, DataSerializers.VARINT);
	protected static final DataParameter<Integer> SIZE_Y = EntityDataManager.createKey(EntityModPainting.class, DataSerializers.VARINT);
	protected static final DataParameter<Integer> HAUNTING_PROGRESS = EntityDataManager.createKey(EntityModPainting.class, DataSerializers.VARINT);
	protected static final DataParameter<Optional<UUID>> OWNER_UUID = EntityDataManager.<Optional<UUID>>createKey(EntityModPainting.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	protected static final DataParameter<String> OWNER_NAME = EntityDataManager.<String>createKey(EntityModPainting.class, DataSerializers.STRING);

	// Using vanilla skulls as a user profile cache
	private final TileEntitySkull skull = new TileEntitySkull();

	public EntityModPainting(World worldIn) {
		super(worldIn);
	}

	public EntityModPainting(World worldIn, BlockPos hangingPositionIn, EnumFacing facing) {
		super(worldIn, hangingPositionIn);
		this.updateFacingWithBoundingBox(facing);
	}

	@Override
	public void playPlaceSound() {
		this.playSound(SoundEvents.ENTITY_PAINTING_PLACE, 1.0F, 1.0F);
	}

	protected void entityInit() {
		this.dataManager.register(ROTATION, 0f);
		this.dataManager.register(PAINTING, "forest");
		this.dataManager.register(SIZE_X, 32);
		this.dataManager.register(SIZE_Y, 32);
		this.dataManager.register(HAUNTING_PROGRESS, 0);
		this.dataManager.register(OWNER_UUID, Optional.absent());
		this.dataManager.register(OWNER_NAME, "");
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	public void onUpdate() {
		super.onUpdate();
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if (this.ticksExisted % 20 == 0 && !this.world.isRemote) {
			if (getOwnerId().isPresent()) {
				EntityPlayer player = this.world.getPlayerEntityByUUID(getOwnerId().get());
				if (player != null) {
					HauntingCapability capability = HauntingCapability.get(player);
					setHauntingProgress(capability.hauntingProgress);
				}
			}
		}
	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	public void writeEntityToNBT(NBTTagCompound compound) {
		compound.setByte("Facing", (byte) EnumFacing.fromAngle(getRotation()).getHorizontalIndex());
		BlockPos blockpos = this.getHangingPosition();
		compound.setInteger("TileX", blockpos.getX());
		compound.setInteger("TileY", blockpos.getY());
		compound.setInteger("TileZ", blockpos.getZ());
		compound.setFloat("Rotation", this.getRotation());
		compound.setInteger("SizeX", this.getXSize());
		compound.setInteger("SizeY", this.getYSize());
		compound.setString("Painting", this.getPainting());
		if (this.getOwnerId().isPresent()) {
			compound.setString("OwnerUUID", this.getOwnerId().get().toString());
		}
		compound.setInteger("HauntingProgress", this.getHauntingProgress());
		compound.setString("PlayerName", this.getOwnerName());
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readEntityFromNBT(NBTTagCompound compound) {
		this.facingDirection = EnumFacing.byHorizontalIndex(compound.getByte("Facing"));
		this.hangingPosition = new BlockPos(compound.getInteger("TileX"), compound.getInteger("TileY"), compound.getInteger("TileZ"));
		this.updateFacingWithBoundingBox(EnumFacing.byHorizontalIndex(compound.getByte("Facing")));
		this.setProperties(compound.getFloat("Rotation"), compound.getInteger("SizeX"), compound.getInteger("SizeY"), compound.getString("Painting"));
		if (compound.hasKey("OwnerUUID", 8)) {
			UUID ownerUUID = UUID.fromString(compound.getString("OwnerUUID"));
			setOwnerId(ownerUUID);
		}
		if (compound.hasKey("PlayerName", 8)) {
			setOwnerName(compound.getString("PlayerName"));
		}
		this.setHauntingProgress(compound.getInteger("HauntingProgress"));
	}

	public int getWidthPixels() {
		return dataManager.get(SIZE_X);
	}

	public int getHeightPixels() {
		return dataManager.get(SIZE_Y);
	}


	@Override
	@SideOnly(Side.CLIENT)
	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
		this.setPosition((double) this.hangingPosition.getX(), (double) this.hangingPosition.getY(), (double) this.hangingPosition.getZ());
	}

	public void onBroken(@Nullable Entity brokenEntity) {
		if (this.world.getGameRules().getBoolean("doEntityDrops")) {
			this.playSound(SoundEvents.ENTITY_PAINTING_BREAK, 1.0F, 1.0F);

			if (brokenEntity instanceof EntityPlayer) {
				EntityPlayer entityplayer = (EntityPlayer) brokenEntity;

				if (entityplayer.capabilities.isCreativeMode) {
					return;
				}
				Item item = this.getPainting().equals("painting_the_haunting") ? ModItems.painting_in_the_woods : ModItems.painting_portrait;
				ItemStack stack = new ItemStack(item);
				if (getOwnerId().isPresent()) {
					stack.getOrCreateSubCompound("Owner").setString("UUID", getOwnerId().get().toString());
					stack.getOrCreateSubCompound("Owner").setString("PlayerName", getOwnerName());
				}
				this.entityDropItem(stack, 0.0F);
			}
		}
	}

	public void setProperties(float rotation, int xSize, int ySize, String painting) {
		this.dataManager.set(ROTATION, rotation);
		this.dataManager.set(SIZE_X, xSize);
		this.dataManager.set(SIZE_Y, ySize);
		this.dataManager.set(PAINTING, painting);
	}

	public float getRotation() {
		return this.dataManager.get(ROTATION);
	}

	public int getXSize() {
		return this.dataManager.get(SIZE_X);
	}

	public int getYSize() {
		return this.dataManager.get(SIZE_Y);
	}

	public String getPainting() {
		return this.dataManager.get(PAINTING);
	}

	public Optional<UUID> getOwnerId() {
		return this.dataManager.get(OWNER_UUID);
	}

	public void setOwnerId(@Nullable UUID uuid) {
		if (uuid != null) {
			this.dataManager.set(OWNER_UUID, Optional.of(uuid));
			this.skull.setPlayerProfile(new GameProfile(uuid, null));
		}
	}

	@Nullable
	public GameProfile getPlayerProfile() {
		this.skull.setPlayerProfile(new GameProfile(this.dataManager.get(OWNER_UUID).get(), "WinDanesz"));
		return this.skull.getPlayerProfile();
	}

	@Override
	public void writeSpawnData(ByteBuf buf) {
		buf.writeLong(this.hangingPosition.toLong());
		buf.writeBoolean(this.facingDirection != null);
		if (this.facingDirection != null) {
			buf.writeInt(this.facingDirection.getHorizontalIndex());
		}
	}

	@Override
	public void readSpawnData(ByteBuf buf) {
		this.hangingPosition = BlockPos.fromLong(buf.readLong());
		if (buf.readBoolean()) {
			this.facingDirection = EnumFacing.byHorizontalIndex(buf.readInt());
			this.updateFacingWithBoundingBox(this.facingDirection);
		}
	}

	public void setOwnerName(String ownerName) {
		this.dataManager.set(OWNER_NAME, ownerName);
	}

	public String getOwnerName() {
		return this.dataManager.get(OWNER_NAME);
	}

	public void setHauntingProgress(int progress) {
		this.dataManager.set(HAUNTING_PROGRESS, progress);
	}

	public int getHauntingProgress() {
		return this.dataManager.get(HAUNTING_PROGRESS);
	}

	@Override
	public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) {
		this.setPosition(x, y, z);
	}
}