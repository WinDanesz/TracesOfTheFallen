package com.windanesz.tracesofthefallen.misc;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.entity.living.EntitySidhe;
import com.windanesz.tracesofthefallen.item.SidheBarter;
import com.windanesz.tracesofthefallen.network.PacketHandler;
import com.windanesz.tracesofthefallen.packet.PacketSidheDiscoveries;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks which Sidhe trades a player has already seen the outcome of. Trades are keyed by the input pattern of their
 * config entry. The server keeps the list in the player's persisted data and mirrors it to the client, where recipe
 * viewers use it to decide whether to reveal a trade's result.
 */
@Mod.EventBusSubscriber(modid = TracesOfTheFallen.MODID)
public final class SidheDiscoveries {

	private static final String TAG = "totf_sidhe_discoveries";
	/**
	 * Entries are stored as 'key|status' or 'key|status|seen'. The status is 'attack', or the number of rewards the trade can
	 * give; 'seen' lists the rewards the player has actually received so far, separated by ';'. Older entries are just 'key'.
	 */
	private static final char SEPARATOR = '|';

	/** Client-side mirror of the local player's discoveries, with the outcome they saw; the value is null if it isn't known. */
	private static final Map<String, String> clientDiscovered = new HashMap<>();

	/** What a player knows about the outcome of a trade. */
	public static final class Info {
		public final boolean attack;
		/** How many different rewards the trade can give (0 for an attack). */
		public final int total;
		/** The rewards received so far, as reward actions, e.g. 'modid:item:meta*count'. */
		public final List<String> seen;

		Info(boolean attack, int total, List<String> seen) {
			this.attack = attack;
			this.total = total;
			this.seen = seen;
		}
	}

	private SidheDiscoveries() {}

	/**
	 * Records that the player has seen the outcome of a trade. The outcome is stored with the key because it can be
	 * randomized per player, which the client can't work out itself.
	 *
	 * @param action   the resolved action of the trade: 'attack', or its possible rewards separated by ';'
	 * @param received the reward the Sidhe actually gave this time, or null
	 */
	public static void discover(EntityPlayer player, String key, String action, String received) {
		if (player.world.isRemote) return;
		NBTTagList list = getList(player);
		for (int i = 0; i < list.tagCount(); i++) {
			String stored = list.getStringTagAt(i);
			if (!keyOf(stored).equals(key)) continue;

			// Update the entry: older ones have no outcome, the possible rewards can change with the config, and there may be a new reward
			String updated = buildEntry(key, action, received, parseInfo(actionOf(stored)));
			if (!updated.equals(stored)) {
				list.set(i, new NBTTagString(updated));
				sync(player);
			}
			return;
		}
		list.appendTag(new NBTTagString(buildEntry(key, action, received, null)));
		sync(player);
	}

	private static String buildEntry(String key, String action, String received, Info previous) {
		if (action == null) return key;
		if (action.equals(SidheBarter.ATTACK)) return key + SEPARATOR + SidheBarter.ATTACK;

		List<String> seen = new ArrayList<>();
		if (previous != null) seen.addAll(previous.seen);
		if (received != null && !seen.contains(received)) seen.add(received);
		StringBuilder entry = new StringBuilder(key).append(SEPARATOR).append(EntitySidhe.parseRewardItems(action).size());
		if (!seen.isEmpty()) entry.append(SEPARATOR).append(String.join(SidheBarter.REWARD_SEPARATOR, seen));
		return entry.toString();
	}

	/** @param value what follows the key in a stored entry, or null */
	private static Info parseInfo(String value) {
		if (value == null) return null;
		String[] parts = value.split("\\|", 2);
		List<String> seen = new ArrayList<>();
		if (parts.length > 1) {
			for (String reward : parts[1].split(SidheBarter.REWARD_SEPARATOR)) {
				if (!reward.isEmpty()) seen.add(reward);
			}
		}
		if (parts[0].equals(SidheBarter.ATTACK)) return new Info(true, 0, seen);
		try {
			return new Info(false, Integer.parseInt(parts[0]), seen);
		} catch (NumberFormatException e) {
			// An entry from before rewards were counted: it holds the list of possible rewards, none of them seen yet
			return new Info(false, parts[0].split(SidheBarter.REWARD_SEPARATOR).length, seen);
		}
	}

	public static boolean isDiscovered(String key) {
		return clientDiscovered.containsKey(key);
	}

	/** @return what the player knows about this trade, or null if undiscovered or unknown (discovered before outcomes were stored) */
	public static Info getInfo(String key) {
		return parseInfo(clientDiscovered.get(key));
	}

	public static void setClientDiscovered(Map<String, String> discovered) {
		clientDiscovered.clear();
		clientDiscovered.putAll(discovered);
	}

	private static String keyOf(String stored) {
		int split = stored.indexOf(SEPARATOR);
		return split < 0 ? stored : stored.substring(0, split);
	}

	private static String actionOf(String stored) {
		int split = stored.indexOf(SEPARATOR);
		return split < 0 ? null : stored.substring(split + 1);
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
		Map<String, String> discovered = new LinkedHashMap<>();
		for (NBTBase tag : getList(player)) {
			String stored = ((NBTTagString) tag).getString();
			discovered.put(keyOf(stored), actionOf(stored));
		}
		PacketHandler.net.sendTo(new PacketSidheDiscoveries(discovered), (EntityPlayerMP) player);
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
