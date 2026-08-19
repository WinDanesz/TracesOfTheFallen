package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.init.ModSounds;
import com.windanesz.tracesofthefallen.network.ModGuiHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class TileEntityStoneChest extends TileEntityLockableLoot implements ITickable {

	private NonNullList<ItemStack> chestContents = NonNullList.<ItemStack>withSize(27, ItemStack.EMPTY);
	public int numPlayersUsing;
	private int openTimer = 0;
	private List<EntityPlayer> playersWaiting = new ArrayList<>();
	
	public float lidAngle;
	public float prevLidAngle;
	
	private boolean isLidOpen = false;

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
		return oldState.getBlock() != newSate.getBlock();
	}

	@Override
	public int getSizeInventory() {
		return 27;
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : this.chestContents) {
			if (!itemstack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String getName() {
		return this.hasCustomName() ? this.customName : "container.stone_chest";
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.chestContents = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
		if (!this.checkLootAndRead(compound)) {
			ItemStackHelper.loadAllItems(compound, this.chestContents);
		}
		if (compound.hasKey("CustomName", 8)) {
			this.customName = compound.getString("CustomName");
		}
		if (compound.hasKey("ForcedOpen")) {
			this.isLidOpen = compound.getBoolean("ForcedOpen");
			if (this.isLidOpen) {
				this.openTimer = 40;
				this.lidAngle = 1.0F;
				this.prevLidAngle = 1.0F;
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		if (!this.checkLootAndWrite(compound)) {
			ItemStackHelper.saveAllItems(compound, this.chestContents);
		}
		if (this.hasCustomName()) {
			compound.setString("CustomName", this.customName);
		}
		compound.setBoolean("ForcedOpen", this.isLidOpen);
		return compound;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public String getGuiID() {
		return "minecraft:chest";
	}

	@Override
	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
		this.fillWithLoot(playerIn);
		return new ContainerChest(playerInventory, this, playerIn);
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.chestContents;
	}

	@Override
	public void update() {
		this.prevLidAngle = this.lidAngle;
		this.lidAngle = this.openTimer / 40.0F;

		boolean isUsed = this.isLidOpen;

		if (isUsed && this.openTimer == 0) {
			if (!this.world.isRemote) {
				this.world.playSound(null, this.pos, ModSounds.STONE_SLIDE, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
				this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).withProperty(BlockStoneChest.OPEN, true), 3);
			}
		}

		if (isUsed && this.openTimer < 40) {
			this.openTimer++;
			if (this.openTimer == 40) {
				if (!this.world.isRemote) {
					for (EntityPlayer player : this.playersWaiting) {
						if (!player.isDead) {
							player.openGui(TracesOfTheFallen.instance, ModGuiHandler.GUI_STONE_CHEST, this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());
						}
					}
				}
				this.playersWaiting.clear();
			}
		} else if (!isUsed && this.openTimer > 0) {
			this.openTimer--;
			if (this.openTimer == 5) {
				if (!this.world.isRemote) {
					this.world.playSound(null, this.pos, ModSounds.STONE_SLIDE, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
				}
			}
			if (this.openTimer == 0) {
				if (!this.world.isRemote) {
					this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).withProperty(BlockStoneChest.OPEN, false), 3);
				}
			}
		}

		if (this.world.isRemote) return;

		// Cleanup ghost usages occasionally (Server only)
		if (this.numPlayersUsing != 0 && (this.world.getTotalWorldTime() % 100L) == 0L) {
			int oldPlayers = this.numPlayersUsing;
			this.numPlayersUsing = 0;
			for (EntityPlayer entityplayer : this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(pos).grow(5.0D))) {
				if (entityplayer.openContainer instanceof ContainerChest) {
					IInventory iinventory = ((ContainerChest)entityplayer.openContainer).getLowerChestInventory();
					if (iinventory == this) {
						++this.numPlayersUsing;
					}
				}
			}
			if (oldPlayers != this.numPlayersUsing) {
				this.world.addBlockEvent(this.pos, this.getBlockType(), 1, this.numPlayersUsing);
			}
		}
	}

	public void requestOpen(EntityPlayer player) {
		if (!player.isSpectator()) {
			if (this.openTimer >= 40) {
				player.openGui(TracesOfTheFallen.instance, ModGuiHandler.GUI_STONE_CHEST, this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());
			} else if (!this.playersWaiting.contains(player)) {
				this.playersWaiting.add(player);
			}
		}
	}

	@Override
	public void openInventory(EntityPlayer player) {
		if (!player.isSpectator()) {
			if (this.numPlayersUsing < 0) {
				this.numPlayersUsing = 0;
			}
			++this.numPlayersUsing;
			this.world.addBlockEvent(this.pos, this.getBlockType(), 1, this.numPlayersUsing);
		}
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		if (!player.isSpectator()) {
			--this.numPlayersUsing;
			this.world.addBlockEvent(this.pos, this.getBlockType(), 1, this.numPlayersUsing);
			this.playersWaiting.remove(player);
		}
	}

	@Override
	public boolean receiveClientEvent(int id, int type) {
		if (id == 1) {
			this.numPlayersUsing = type;
			return true;
		}
		return super.receiveClientEvent(id, type);
	}

	public boolean isLidOpen() {
		return this.isLidOpen;
	}

	public void setLidOpen(boolean lidOpen) {
		this.isLidOpen = lidOpen;
		this.markDirty();
		if (this.world != null) {
			IBlockState state = this.world.getBlockState(this.pos);
			this.world.notifyBlockUpdate(this.pos, state, state, 3);
		}
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbt = super.getUpdateTag();
		nbt.setBoolean("ForcedOpen", this.isLidOpen);
		return nbt;
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		super.handleUpdateTag(tag);
		this.isLidOpen = tag.getBoolean("ForcedOpen");
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setBoolean("ForcedOpen", this.isLidOpen);
		return new SPacketUpdateTileEntity(this.pos, 1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		this.isLidOpen = pkt.getNbtCompound().getBoolean("ForcedOpen");
	}
}
