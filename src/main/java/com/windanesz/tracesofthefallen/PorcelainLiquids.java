package com.windanesz.tracesofthefallen;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class PorcelainLiquids {

	private static final String DEFAULT_TEA_TYPE = "tea";
	private static final int DEFAULT_TEA_COLOR = 0xC07A3A;
	private static final String DEFAULT_TEA_ITEM = "totf:spirited_away_tea_leaves";
	private static final String DEFAULT_TEA_EFFECT = "minecraft:regeneration";
	private static final int DEFAULT_TEA_AMPLIFIER = 0;
	private static final int DEFAULT_TEA_DURATION = 300;

	private PorcelainLiquids() {
	}

	public static final class LiquidDefinition {
		public final String type;
		public final String acceptedItemId;
		public final int acceptedMeta;
		public final String forgeFluidName;
		public final int color;
		public final Potion potion;
		public final int amplifier;
		public final int durationTicks;

		private LiquidDefinition(String type, String acceptedItemId, int acceptedMeta, String forgeFluidName, int color, Potion potion, int amplifier,
				int durationTicks) {
			this.type = type;
			this.acceptedItemId = acceptedItemId;
			this.acceptedMeta = acceptedMeta;
			this.forgeFluidName = normalize(forgeFluidName);
			this.color = color;
			this.potion = potion;
			this.amplifier = Math.max(0, amplifier);
			this.durationTicks = Math.max(1, durationTicks);
		}

		public boolean matchesIngredient(ItemStack stack) {
			if (stack.isEmpty()) {
				return false;
			}
			ResourceLocation itemName = stack.getItem().getRegistryName();
			if (itemName == null || !itemName.toString().equals(acceptedItemId)) {
				return false;
			}
			return acceptedMeta < 0 || acceptedMeta == stack.getMetadata();
		}

	}

	public static Collection<LiquidDefinition> getDefinitions() {
		return buildDefinitions().values();
	}

	@Nullable
	public static LiquidDefinition getByType(@Nullable String type) {
		if (type == null) {
			return null;
		}
		return buildDefinitions().get(normalize(type));
	}

	@Nullable
	public static LiquidDefinition getForIngredient(ItemStack stack) {
		for (LiquidDefinition definition : buildDefinitions().values()) {
			if (definition.matchesIngredient(stack)) {
				return definition;
			}
		}
		return null;
	}

	public static int getColor(@Nullable String type) {
		LiquidDefinition definition = getByType(type);
		return definition == null ? DEFAULT_TEA_COLOR : definition.color;
	}

	private static Map<String, LiquidDefinition> buildDefinitions() {
		Map<String, LiquidDefinition> definitions = new LinkedHashMap<>();
		if (Settings.miscSettings.porcelainLiquidDefinitions != null) {
			for (String entry : Settings.miscSettings.porcelainLiquidDefinitions) {
				LiquidDefinition definition = parseDefinition(entry);
				if (definition != null) {
					definitions.put(definition.type, definition);
				}
			}
		}

		if (!definitions.containsKey(DEFAULT_TEA_TYPE)) {
			Potion teaPotion = getPotion(DEFAULT_TEA_EFFECT);
			if (teaPotion != null) {
				definitions.put(DEFAULT_TEA_TYPE, new LiquidDefinition(
						DEFAULT_TEA_TYPE,
						DEFAULT_TEA_ITEM,
						-1,
						"water",
						DEFAULT_TEA_COLOR,
						teaPotion,
						DEFAULT_TEA_AMPLIFIER,
						DEFAULT_TEA_DURATION
				));
			}
		}
		return definitions;
	}

	@Nullable
	private static LiquidDefinition parseDefinition(@Nullable String entry) {
		if (entry == null) {
			return null;
		}

		String trimmed = entry.trim();
		if (trimmed.isEmpty()) {
			return null;
		}

		String[] parts = trimmed.split("\\|");
		if (parts.length != 6 && parts.length != 7) {
			return null;
		}

		String type = normalize(parts[0]);
		String acceptedItemRaw = parts[1].trim();
		String forgeFluidName = parts.length == 7 ? parts[2].trim() : "";
		int color = parseColor(parts.length == 7 ? parts[3] : parts[2]);
		Potion potion = getPotion(parts.length == 7 ? parts[4] : parts[3]);
		int amplifier = parseInt(parts.length == 7 ? parts[5] : parts[4], 0);
		int durationTicks = parseInt(parts.length == 7 ? parts[6] : parts[5], DEFAULT_TEA_DURATION);
		if (type.isEmpty() || acceptedItemRaw.isEmpty() || potion == null) {
			return null;
		}

		ResourceLocation acceptedItemLocation = toResourceLocation(parseItemId(acceptedItemRaw));
		if (acceptedItemLocation == null) {
			return null;
		}
		Item item = Item.REGISTRY.getObject(acceptedItemLocation);
		if (item == null) {
			return null;
		}

		return new LiquidDefinition(type, parseItemId(acceptedItemRaw), parseMeta(acceptedItemRaw), forgeFluidName, color, potion, amplifier,
				durationTicks);
	}

	private static String parseItemId(String acceptedItemRaw) {
		int atIndex = acceptedItemRaw.indexOf('@');
		return atIndex < 0 ? acceptedItemRaw.trim() : acceptedItemRaw.substring(0, atIndex).trim();
	}

	private static int parseMeta(String acceptedItemRaw) {
		int atIndex = acceptedItemRaw.indexOf('@');
		if (atIndex < 0 || atIndex >= acceptedItemRaw.length() - 1) {
			return -1;
		}
		return parseInt(acceptedItemRaw.substring(atIndex + 1), -1);
	}

	private static int parseColor(String colorRaw) {
		String cleaned = colorRaw.trim();
		if (cleaned.startsWith("#")) {
			cleaned = cleaned.substring(1);
		}
		try {
			return Integer.parseInt(cleaned, 16) & 0xFFFFFF;
		} catch (NumberFormatException e) {
			return DEFAULT_TEA_COLOR;
		}
	}

	@Nullable
	private static Potion getPotion(String potionName) {
		String cleaned = potionName == null ? "" : potionName.trim();
		if (cleaned.isEmpty()) {
			return null;
		}
		ResourceLocation potionLocation = toResourceLocation(cleaned);
		return potionLocation == null ? null : Potion.REGISTRY.getObject(potionLocation);
	}

	private static int parseInt(String value, int fallback) {
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	private static String normalize(String value) {
		return value.trim().toLowerCase(Locale.ROOT);
	}

	@Nullable
	private static ResourceLocation toResourceLocation(String value) {
		try {
			return new ResourceLocation(value);
		} catch (IllegalArgumentException ignored) {
			return null;
		}
	}
}
