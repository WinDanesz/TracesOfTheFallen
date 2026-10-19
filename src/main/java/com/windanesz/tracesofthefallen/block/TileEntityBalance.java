package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.world.shop.ShopTrades;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;

public class TileEntityBalance extends TileEntity implements ITickable {

	/** The name shown as the seller on a shop balance. */
	private static final String SHOP_OWNER = "Subterfuge";
	private static final float MAX_TILT = 18.0F;
	private static final float TILT_INTERPOLATION = 0.25F;

	private ItemStack leftStack = ItemStack.EMPTY;
	private ItemStack rightStack = ItemStack.EMPTY;
	private BalanceMode mode = BalanceMode.DEFAULT;
	private String ownerName = "";
	/** A shop balance belongs to a Subterfuge's shop: only she can restock it, and it only trades while she is present. */
	private boolean shop;
	/** World time at which a sold-out shop balance is stocked again; 0 means never. */
	private long restockAt;
	private float tilt;
	private float prevTilt;

	@Override
	public void update() {
		if (shop && restockAt > 0L && world != null && !world.isRemote && leftStack.isEmpty()
				&& world.getTotalWorldTime() >= restockAt && world.getTotalWorldTime() % 20L == 0L) {
			ShopTrades.restock(this);
		}

		prevTilt = tilt;
		float targetTilt = getTargetTilt();
		if (Math.abs(targetTilt - tilt) < 0.05F) {
			tilt = targetTilt;
		} else {
			tilt += (targetTilt - tilt) * TILT_INTERPOLATION;
		}
	}

	public ItemStack getLeftStack() {
		return leftStack;
	}

	public ItemStack getRightStack() {
		return rightStack;
	}

	public void setLeftStack(ItemStack stack) {
		leftStack = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
		onContentsChanged();
	}

	public void setRightStack(ItemStack stack) {
		rightStack = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
		onContentsChanged();
	}

	public BalanceMode getMode() {
		return mode;
	}

	public void setMode(BalanceMode mode) {
		this.mode = mode == null ? BalanceMode.DEFAULT : mode;
		onContentsChanged();
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName == null ? "" : ownerName;
		onContentsChanged();
	}

	public boolean isShop() {
		return shop;
	}

	public void setShop(boolean shop) {
		this.shop = shop;
		markDirty();
	}

	public long getRestockAt() {
		return restockAt;
	}

	public void setRestockAt(long restockAt) {
		this.restockAt = Math.max(0L, restockAt);
		markDirty();
	}

	/** Sets what a shop balance offers and asks for. Quiet while a structure is still being generated. */
	public void setShopStock(ItemStack goods, ItemStack price, boolean notify) {
		shop = true;
		mode = BalanceMode.VENDING;
		ownerName = SHOP_OWNER;
		leftStack = goods.isEmpty() ? ItemStack.EMPTY : goods.copy();
		rightStack = price.isEmpty() ? ItemStack.EMPTY : price.copy();
		restockAt = 0L;
		if (notify) {
			onContentsChanged();
		} else {
			markDirty();
		}
	}

	public float getInterpolatedTilt(float partialTicks) {
		return prevTilt + (tilt - prevTilt) * partialTicks;
	}

	public float getTargetTilt() {
		if (mode == BalanceMode.VENDING) {
			return leftStack.isEmpty() ? 0.0F : MAX_TILT;
		}
		float difference = getLeftWeight() - getRightWeight();
		float normalizedDifference = difference / 64.0F;
		normalizedDifference = Math.max(-1.0F, Math.min(1.0F, normalizedDifference));
		return normalizedDifference * MAX_TILT;
	}

	public float getLeftWeight() {
		return getNormalizedWeight(leftStack);
	}

	public float getRightWeight() {
		return getNormalizedWeight(rightStack);
	}

	public int getLeftSignalStrength() {
		return getSignalStrength(leftStack);
	}

	public int getRightSignalStrength() {
		return getSignalStrength(rightStack);
	}

	public int getComparatorStrength() {
		return Math.max(getLeftSignalStrength(), getRightSignalStrength());
	}

	public int getSignalForSide(EnumFacing querySide, EnumFacing blockFacing) {
		if (mode != BalanceMode.REDSTONE || querySide == null) {
			return 0;
		}

		EnumFacing leftSide = blockFacing.rotateYCCW();
		EnumFacing rightSide = blockFacing.rotateY();
		if (querySide == leftSide) {
			return getLeftSignalStrength();
		}
		if (querySide == rightSide) {
			return getRightSignalStrength();
		}
		return getComparatorStrength();
	}

	public void onContentsChanged() {
		markDirty();
		if (world != null) {
			if (!world.isRemote) {
				world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
				world.updateComparatorOutputLevel(pos, getBlockType());
				for (EnumFacing facing : EnumFacing.values()) {
					world.notifyNeighborsOfStateChange(pos.offset(facing), getBlockType(), false);
				}
				world.notifyNeighborsOfStateChange(pos, getBlockType(), false);
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setTag("LeftStack", leftStack.writeToNBT(new NBTTagCompound()));
		compound.setTag("RightStack", rightStack.writeToNBT(new NBTTagCompound()));
		compound.setString(BalanceMode.NBT_KEY, mode.getSerializedName());
		compound.setString("OwnerName", ownerName);
		compound.setBoolean("Shop", shop);
		compound.setLong("RestockAt", restockAt);
		compound.setFloat("Tilt", tilt);
		compound.setFloat("PrevTilt", prevTilt);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		leftStack = new ItemStack(compound.getCompoundTag("LeftStack"));
		rightStack = new ItemStack(compound.getCompoundTag("RightStack"));
		mode = BalanceMode.fromString(compound.getString(BalanceMode.NBT_KEY));
		ownerName = compound.getString("OwnerName");
		shop = compound.getBoolean("Shop");
		restockAt = compound.getLong("RestockAt");
		tilt = compound.getFloat("Tilt");
		prevTilt = compound.getFloat("PrevTilt");
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	@Nullable
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		readFromNBT(tag);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(pos).grow(0.5D, 0.5D, 0.5D);
	}

	@Override
	public double getMaxRenderDistanceSquared() {
		return 4096.0D;
	}

	public static float getNormalizedWeight(ItemStack stack) {
		if (stack.isEmpty()) {
			return 0.0F;
		}
		return (stack.getCount() / (float) stack.getMaxStackSize()) * 64.0F;
	}

	public static int getSignalStrength(ItemStack stack) {
		return Math.max(0, Math.min(15, (int) Math.floor((getNormalizedWeight(stack) / 64.0F) * 15.0F)));
	}
}
