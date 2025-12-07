package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class TileEntityGraveMarker extends TileEntity implements ITickable {
	private Item flowerPotItem;
	private int flowerPotData;
	private boolean flowerPlaced;

	// 0 = can turn, 1 = turned, -1 = can't turn/removed
	private int turnsIntoGraveRose = 0;
	// amount of time it takes for the rose to transform in ticks
	private int transformProgress = 0;
	private static final int TRANSFORM_DURATION = 60;


	public TileEntityGraveMarker() {
	}

	public TileEntityGraveMarker(Item potItem, int potData) {
		this.flowerPotItem = potItem;
		this.flowerPotData = potData;
	}

	@Override
	public void update() {
		// Common logic for client and server
		if (this.transformProgress > 0) {
			this.transformProgress++;
			// Client-side particles
			if (this.world.isRemote) {
				if (this.world.rand.nextInt(4) == 0) {
					double x = (double) this.pos.getX() + 0.5D;
					double y = (double) this.pos.getY() + 0.4D;
					double z = (double) this.pos.getZ() + 0.5D;
					this.world.spawnParticle(EnumParticleTypes.SPELL_MOB, x, y, z, 0.0D, 0.917D, 1.0D);

				}
			}
		}

		// Server-side state management
		if (!this.world.isRemote) {
			// Start transformation
			if (this.turnsIntoGraveRose == 0 && this.transformProgress == 0) {
				if (this.getFlowerPotItem() == Item.getItemFromBlock(ModBlocks.rose)) {
					if (this.world.rand.nextFloat() < Settings.miscSettings.graveRoseChance) {
						this.transformProgress = 1;
					} else {
						this.turnsIntoGraveRose = -1;
					}
					this.markDirty();
					IBlockState state = world.getBlockState(pos);
					world.notifyBlockUpdate(pos, state, state, 3);
				}
			}
			// End transformation
			else if (this.transformProgress >= TRANSFORM_DURATION) {
				this.setFlowerItemStack(new ItemStack(ModBlocks.grave_rose));
				// transformProgress is reset inside setFlowerItemStack
				this.markDirty();
				IBlockState state = world.getBlockState(pos);
				world.notifyBlockUpdate(pos, state, state, 3);
			}
		}

		if (this.world.isRemote && this.getFlowerPotItem() == Item.getItemFromBlock(ModBlocks.grave_rose)) {
			if (this.world.getTotalWorldTime() % 15 == 0 && this.world.rand.nextInt(1) == 0) { // Spawn particles less frequently
				double x = (double) this.pos.getX() + 0.5D;
				double y = (double) this.pos.getY() + 0.4D;
				double z = (double) this.pos.getZ() + 0.5D;
				double xOffset = this.world.rand.nextDouble() * 0.6D - 0.3D;
				double zOffset = this.world.rand.nextDouble() * 0.6D - 0.3D;
				this.world.spawnParticle(EnumParticleTypes.SPELL_WITCH, x + xOffset, y, z + zOffset, 0.0D, 0.917D, 1.0D);
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		ResourceLocation resourcelocation = Item.REGISTRY.getNameForObject(this.flowerPotItem);
		compound.setString("Item", resourcelocation == null ? "" : resourcelocation.toString());
		compound.setInteger("Data", this.flowerPotData);
		compound.setBoolean("flowerPlaced", this.flowerPlaced);
		compound.setInteger("turnsIntoGraveRose", this.turnsIntoGraveRose);
		compound.setInteger("transformProgress", this.transformProgress);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);

		if (compound.hasKey("Item", 8)) {
			this.flowerPotItem = Item.getByNameOrId(compound.getString("Item"));
		} else {
			this.flowerPotItem = Item.getItemById(compound.getInteger("Item"));
		}

		this.flowerPotData = compound.getInteger("Data");

		if (compound.hasKey("flowerPlaced")) {
			this.flowerPlaced = compound.getBoolean("flowerPlaced");
		}

		if (compound.hasKey("turnsIntoGraveRose")) {
			this.turnsIntoGraveRose = compound.getInteger("turnsIntoGraveRose");
		}
		if (compound.hasKey("transformProgress")) {
			this.transformProgress = compound.getInteger("transformProgress");
		}
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		this.readFromNBT(tag);
	}

	@Override
	public final NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, getBlockMetadata(), this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		this.handleUpdateTag(pkt.getNbtCompound());
	}

	public void setFlowerItemStack(ItemStack stack) {
		this.flowerPotItem = stack.getItem();
		this.flowerPotData = stack.getMetadata();
		this.transformProgress = 0;
		if (stack.getItem() == Item.getItemFromBlock(ModBlocks.rose)) {
			this.turnsIntoGraveRose = 0;
		} else if (stack.getItem() == Item.getItemFromBlock(ModBlocks.grave_rose)) {
			this.turnsIntoGraveRose = 1;
		} else {
			this.turnsIntoGraveRose = -1;
		}
	}

	public ItemStack getFlowerItemStack() {
		return this.flowerPotItem == null ? ItemStack.EMPTY : new ItemStack(this.flowerPotItem, 1, this.flowerPotData);
	}

	@Nullable
	public Item getFlowerPotItem() {
		return this.flowerPotItem;
	}

	public int getFlowerPotData() {
		return this.flowerPotData;
	}

	public boolean getFlowerPlaced() {
		return flowerPlaced;
	}

	public void setFlowerPlaced(boolean flowerPlaced) {
		this.flowerPlaced = flowerPlaced;
	}
}