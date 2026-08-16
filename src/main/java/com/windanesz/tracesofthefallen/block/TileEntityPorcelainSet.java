package com.windanesz.tracesofthefallen.block;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;
import java.util.Locale;

public class TileEntityPorcelainSet extends TileEntity {

	private static final String CUP_0_CONTENT_KEY = "Cup0Content";
	private static final String CUP_1_CONTENT_KEY = "Cup1Content";

	private String cup0Content = "";
	private String cup1Content = "";

	public String getCupContent(int slot) {
		return slot == 0 ? cup0Content : slot == 1 ? cup1Content : "";
	}

	public void setCupContent(int slot, String content) {
		String normalized = normalize(content);
		if (slot == 0) {
			cup0Content = normalized;
		} else if (slot == 1) {
			cup1Content = normalized;
		}
		sync();
	}

	public int getFilledCupCount(BlockPorcelainSet.Arrangement arrangement) {
		int count = 0;
		for (int slot : arrangement.getCupSlots()) {
			if (!getCupContent(slot).isEmpty()) {
				count++;
			}
		}
		return count;
	}

	public String getFirstAvailableCupContent() {
		return !cup0Content.isEmpty() ? cup0Content : cup1Content;
	}

	public void copyFrom(TileEntityPorcelainSet other, BlockPorcelainSet.Arrangement arrangement) {
		cup0Content = normalize(other.cup0Content);
		cup1Content = normalize(other.cup1Content);
		if (arrangement.getCupCount() < 2) {
			cup1Content = "";
		}
		sync();
	}

	public void applyTransition(TileEntityPorcelainSet previous, BlockPorcelainSet.RemovalResult removal) {
		cup0Content = "";
		cup1Content = "";
		if (removal.replacementState == null || !(removal.replacementState.getBlock() instanceof BlockPorcelainSet)) {
			sync();
			return;
		}

		BlockPorcelainSet.Arrangement newArrangement = removal.replacementState.getValue(BlockPorcelainSet.ARRANGEMENT);
		if (newArrangement == BlockPorcelainSet.Arrangement.CUPS) {
			cup0Content = previous.cup0Content;
			cup1Content = previous.cup1Content;
		} else if (newArrangement == BlockPorcelainSet.Arrangement.POT_AND_CUP) {
			if (removal.replacementCupSourceSlot >= 0) {
				cup0Content = previous.getCupContent(removal.replacementCupSourceSlot);
			} else {
				cup0Content = !previous.cup0Content.isEmpty() ? previous.cup0Content : previous.cup1Content;
			}
		}
		sync();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setString(CUP_0_CONTENT_KEY, cup0Content);
		compound.setString(CUP_1_CONTENT_KEY, cup1Content);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		cup0Content = normalize(compound.getString(CUP_0_CONTENT_KEY));
		cup1Content = normalize(compound.getString(CUP_1_CONTENT_KEY));
	}

	@Nullable
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
		if (world != null) {
			world.markBlockRangeForRenderUpdate(pos, pos);
		}
	}

	private void sync() {
		markDirty();
		if (world != null) {
			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
			world.markBlockRangeForRenderUpdate(pos, pos);
		}
	}

	private static String normalize(String content) {
		return content == null ? "" : content.trim().toLowerCase(Locale.ROOT);
	}
}
