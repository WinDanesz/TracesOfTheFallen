package com.windanesz.tracesofthefallen;

import com.windanesz.tracesofthefallen.entity.EntitySidhe;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Decides what a Sidhe does with an offered item. By default (see the 'Randomize Sidhe Barter' setting) the outcomes of
 * all configured items are thoroughly mixed, good and bad alike: roughly half of the items become rewards and half
 * angering, whatever the config says for them, differently for every player. The outcome is derived from the world seed
 * and the player's UUID, so it is stable for that player in that world but not shared.
 * Items listed as guaranteed rewards (the furred trout by default) are never angering; they give a random reward.
 */
public final class SidheBarter {

	public static final String ATTACK = "attack";
	/** Separates the alternative rewards of an action. */
	public static final String REWARD_SEPARATOR = ";";

	private static final long SALT = 0x51D4EBA77E2L;

	private SidheBarter() {}

	/**
	 * @param entry the matching config entry of the item, as returned by {@link EntitySidhe#getInteractEntry}
	 * @return the action ('attack' or a reward stack) the Sidhe takes when this player offers the item. Server side only,
	 * as the world seed is not known to the client.
	 */
	public static String resolveAction(EntityPlayer player, ItemStack stack, String[] entry) {
		String configured = entry[1];
		if (!Settings.miscSettings.randomizeSidheBarter) return configured;
		return getAssignments(player).getOrDefault(EntitySidhe.getDiscoveryKey(stack), configured);
	}

	/** Outcome of every configured item for the player, keyed by discovery key. */
	private static Map<String, String> getAssignments(EntityPlayer player) {
		// Sorted so the result only depends on the seed and UUID, never on config or registry ordering
		Map<String, ItemStack> candidates = new TreeMap<>();
		for (String entry : Settings.miscSettings.sidheInteractItems) {
			String[] parts = entry.split("\\|");
			if (parts.length < 2) continue;
			for (ItemStack variant : expandInputs(parts[0])) {
				candidates.putIfAbsent(EntitySidhe.getDiscoveryKey(variant), variant);
			}
		}

		Set<String> guaranteed = new HashSet<>();
		for (String pattern : Settings.miscSettings.sidheGuaranteedRewardItems) {
			for (ItemStack variant : expandInputs(pattern)) {
				guaranteed.add(EntitySidhe.getDiscoveryKey(variant));
			}
		}
		guaranteed.retainAll(candidates.keySet());

		// The rewards in play are the configured ones plus the extra pool, so good and bad outcomes all get mixed up
		Set<String> pool = new TreeSet<>();
		List<String> rewardSources = new ArrayList<>(Arrays.asList(Settings.miscSettings.sidheRandomRewards));
		for (String entry : Settings.miscSettings.sidheInteractItems) {
			String[] parts = entry.split("\\|");
			if (parts.length >= 2) rewardSources.add(parts[1]);
		}
		for (String source : rewardSources) {
			if (source.equals(ATTACK)) continue;
			for (String reward : source.split(REWARD_SEPARATOR)) {
				if (!EntitySidhe.parseRewardItem(reward.trim()).isEmpty()) pool.add(reward.trim());
			}
		}
		List<String> rewardPool = new ArrayList<>(pool);

		UUID uuid = player.getUniqueID();
		long h = mix(player.world.getSeed() * 0x9E3779B97F4A7C15L + uuid.getMostSignificantBits());
		h = mix(h ^ uuid.getLeastSignificantBits()) ^ SALT;
		Random rand = new Random(mix(h));

		// An exact split rather than a coin flip per item, so a player can't end up with every item angering.
		// Guaranteed items count towards the rewarding half.
		List<String> shuffled = new ArrayList<>();
		for (String key : candidates.keySet()) {
			if (!guaranteed.contains(key)) shuffled.add(key);
		}
		Collections.shuffle(shuffled, rand);
		int rewarding = rewardPool.isEmpty() ? 0 : Math.max(0, candidates.size() / 2 - guaranteed.size());

		int choices = Math.max(1, Math.min(Settings.miscSettings.sidheRandomRewardChoices, rewardPool.size()));
		Map<String, String> assignments = new TreeMap<>();
		List<String> rewardingKeys = new ArrayList<>(guaranteed);
		Collections.sort(rewardingKeys);
		for (int i = 0; i < shuffled.size(); i++) {
			if (i < rewarding) {
				rewardingKeys.add(shuffled.get(i));
			} else {
				assignments.put(shuffled.get(i), ATTACK);
			}
		}
		if (!rewardPool.isEmpty()) {
			for (String key : rewardingKeys) {
				// This item gives one of a few rewards, picked at random on every trade (see EntitySidhe#pickReward)
				Collections.shuffle(rewardPool, rand);
				assignments.put(key, String.join(REWARD_SEPARATOR, rewardPool.subList(0, choices)));
			}
		}
		return assignments;
	}

	/** splitmix64 finalizer */
	private static long mix(long z) {
		z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
		z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
		return z ^ (z >>> 31);
	}

	/**
	 * Every concrete item a config pattern stands for: a wildcard meta expands to all variants of the item.
	 * Mirrors the matching rules of {@link EntitySidhe#getInteractEntry}; a missing meta means 0 and '*' means any.
	 */
	public static List<ItemStack> expandInputs(String pattern) {
		String[] itemParts = pattern.split(":", 4);
		if (itemParts.length < 2) return Collections.emptyList();
		Item item = Item.REGISTRY.getObject(new ResourceLocation(itemParts[0], itemParts[1]));
		if (item == null) return Collections.emptyList();

		List<ItemStack> stacks = new ArrayList<>();
		if (itemParts.length >= 3 && itemParts[2].equals("*")) {
			try {
				NonNullList<ItemStack> variants = NonNullList.create();
				item.getSubItems(CreativeTabs.SEARCH, variants);
				for (ItemStack variant : variants) {
					if (variant.getItem() == item) stacks.add(variant.copy());
				}
			} catch (Exception ignored) {
			}
			if (stacks.isEmpty()) stacks.add(new ItemStack(item));
		} else {
			int meta = 0;
			if (itemParts.length >= 3) {
				try {
					meta = Integer.parseInt(itemParts[2]);
				} catch (NumberFormatException ignored) {
				}
			}
			stacks.add(new ItemStack(item, 1, meta));
		}

		if (itemParts.length >= 4) {
			try {
				NBTTagCompound tag = JsonToNBT.getTagFromJson(itemParts[3]);
				for (ItemStack stack : stacks) stack.setTagCompound(tag.copy());
			} catch (Exception ignored) {
			}
		}
		return stacks;
	}
}
