package com.windanesz.tracesofthefallen;

import com.windanesz.tracesofthefallen.network.PacketHandler;
import com.windanesz.tracesofthefallen.packet.PacketSidheDiscoveries;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Tracks which Sidhe trades a player has already seen the outcome of. Trades are keyed by the input pattern of their
 * config entry. The server keeps the list in the player's persisted data and mirrors it to the client, where recipe
 * viewers use it to decide whether to reveal a trade's result.
 */
@Mod.EventBusSubscriber(modid = TracesOfTheFallen.MODID)
public final class SidheDiscoveries {

	private static final String TAG = "totf_sidhe_discoveries";

	/** Client-side mirror of the local player's discoveries. */
	private static final Set<String> clientDiscovered = new HashSet<>();

	private SidheDiscoveries() {}

	public static void discover(EntityPlayer player, String key) {
		if (player.world.isRemote) return;
		NBTTagList list = getList(player);
		for (NBTBase tag : list) {
			if (((NBTTagString) tag).getString().equals(key)) return;
		}
		list.appendTag(new NBTTagString(key));
		sync(player);
	}

	public static boolean isDiscovered(String key) {
		return clientDiscovered.contains(key);
	}

	public static void setClientDiscovered(Collection<String> keys) {
		clientDiscovered.clear();
		clientDiscovered.addAll(keys);
	}

	private static NBTTagList getList(EntityPlayer player) {
		NBTTagCompound data = player.getEntityData();
		if (!data.hasKey(EntityPlayer.PERSISTED_NBT_TAG, Constants.NBT.TAG_COMPOUND)) {
			data.setTag(EntityPlayer.PERSISTED_NBT_TAG, new NBTTagCompound());
		}
		NBTTagCompound persisted = data.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
		if (!persisted.hasKey(TAG, Constants.NBT.TAG_LIST)) {
			persisted.setTag(TAG, new NBTTagList());
		}
		return persisted.getTagList(TAG, Constants.NBT.TAG_STRING);
	}

	private static void sync(EntityPlayer player) {
		if (!(player instanceof EntityPlayerMP)) return;
		Set<String> keys = new LinkedHashSet<>();
		for (NBTBase tag : getList(player)) {
			keys.add(((NBTTagString) tag).getString());
		}
		PacketHandler.net.sendTo(new PacketSidheDiscoveries(keys), (EntityPlayerMP) player);
	}

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		sync(event.player);
	}

	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		sync(event.player);
	}

	@SubscribeEvent
	public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		sync(event.player);
	}
}
