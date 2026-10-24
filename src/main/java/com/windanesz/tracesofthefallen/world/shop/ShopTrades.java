package com.windanesz.tracesofthefallen.world.shop;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.block.TileEntityBalance;
import com.windanesz.tracesofthefallen.entity.living.EntitySubterfuge;
import com.windanesz.tracesofthefallen.init.ModItems;
import com.windanesz.tracesofthefallen.item.ItemTallieCollector;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * What a Subterfuge's shop sells and how a sale goes: each shop balance holds one item (left pan) and its
 * price in Tallies (right pan, a Tallie Collector carrying the price as its count).
 */
public final class ShopTrades {

	private static final long TICKS_PER_DAY = 24000L;

	/** One configured offer. */
	public static final class Trade {
		public final ItemStack stack;
		public final long price;
		final int weight;

		Trade(ItemStack stack, long price, int weight) {
			this.stack = stack;
			this.price = price;
			this.weight = weight;
		}
	}

	private static String[] cachedSource;
	private static List<Trade> cachedTrades = Collections.emptyList();

	private ShopTrades() {}

	/** The configured trades, parsed again only when the config changes. */
	public static List<Trade> getTrades() {
		String[] source = Settings.worldgenSettings.subterfugeShopTrades;
		if (cachedSource == null || !Arrays.equals(cachedSource, source)) {
			cachedSource = source.clone();
			cachedTrades = parse(source);
		}
		return cachedTrades;
	}

	private static List<Trade> parse(String[] source) {
		List<Trade> trades = new ArrayList<>();
		for (String entry : source) {
			Trade trade = parseEntry(entry);
			if (trade != null) {
				trades.add(trade);
			}
		}
		return trades;
	}

	private static Trade parseEntry(String entry) {
		if (entry == null || entry.trim().isEmpty()) {
			return null;
		}
		String[] parts = entry.split(";");
		if (parts.length < 3) {
			TracesOfTheFallen.LOGGER.warn("Ignoring Subterfuge shop trade '{}': expected 'item;amount;price[;weight]'", entry);
			return null;
		}
		try {
			String itemName = parts[0].trim();
			int meta = 0;
			int metaSeparator = itemName.indexOf('@');
			if (metaSeparator >= 0) {
				meta = Integer.parseInt(itemName.substring(metaSeparator + 1).trim());
				itemName = itemName.substring(0, metaSeparator).trim();
			}
			Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
			if (item == null) {
				TracesOfTheFallen.LOGGER.warn("Ignoring Subterfuge shop trade '{}': unknown item '{}'", entry, itemName);
				return null;
			}
			int amount = Math.max(1, Math.min(64, Integer.parseInt(parts[1].trim())));
			long price = Math.max(1L, Long.parseLong(parts[2].trim()));
			int weight = parts.length > 3 ? Math.max(1, Integer.parseInt(parts[3].trim())) : 1;
			return new Trade(new ItemStack(item, amount, meta), price, weight);
		} catch (NumberFormatException e) {
			TracesOfTheFallen.LOGGER.warn("Ignoring Subterfuge shop trade '{}': bad number", entry);
			return null;
		}
	}

	/** Picks up to {@code count} different trades, favouring the heavier ones. */
	public static List<Trade> roll(Random random, int count) {
		List<Trade> pool = new ArrayList<>(getTrades());
		List<Trade> picked = new ArrayList<>();
		for (int index = 0; index < count && !pool.isEmpty(); index++) {
			int totalWeight = 0;
			for (Trade trade : pool) {
				totalWeight += trade.weight;
			}
			int roll = random.nextInt(totalWeight);
			for (Iterator<Trade> iterator = pool.iterator(); iterator.hasNext(); ) {
				Trade trade = iterator.next();
				roll -= trade.weight;
				if (roll < 0) {
					picked.add(trade);
					iterator.remove();
					break;
				}
			}
		}
		return picked;
	}

	public static ItemStack priceStack(long tallies) {
		ItemStack price = new ItemStack(ModItems.tally_collector);
		ItemTallieCollector.setTallieCount(price, tallies);
		return price;
	}

	/** Turns a balance into a shop pedestal offering the given trade. */
	public static void stock(TileEntityBalance balance, Trade trade, boolean notify) {
		balance.setShopStock(trade.stack, priceStack(trade.price), notify);
	}

	/** Stocks a sold-out shop balance again. */
	public static void restock(TileEntityBalance balance) {
		List<Trade> trade = roll(balance.getWorld().rand, 1);
		if (trade.isEmpty()) {
			balance.setRestockAt(0L);
			return;
		}
		stock(balance, trade.get(0), true);
	}

	/** Right-click on a shop balance: buys what it offers, or with sneak shows the price. */
	public static void handleInteraction(World world, BlockPos pos, EntityPlayer player, TileEntityBalance balance) {
		EntitySubterfuge keeper = ShopProtectionHandler.findKeeper(world, pos);
		if (keeper == null || !keeper.isShopOpen()) {
			player.sendStatusMessage(new TextComponentTranslation("totf:shop.closed"), true);
			return;
		}

		ItemStack goods = balance.getLeftStack();
		ItemStack priceStack = balance.getRightStack();
		if (goods.isEmpty() || !(priceStack.getItem() instanceof ItemTallieCollector)) {
			player.sendStatusMessage(new TextComponentTranslation("totf:shop.sold_out"), true);
			return;
		}

		long price = ItemTallieCollector.getTallieCount(priceStack);
		if (player.isSneaking()) {
			player.sendStatusMessage(new TextComponentTranslation("totf:shop.price", goods.getCount(), goods.getDisplayName(), price), true);
			return;
		}

		boolean free = player.capabilities.isCreativeMode;
		long owned = countTallies(player);
		if (!free && owned < price) {
			player.sendStatusMessage(new TextComponentTranslation("totf:shop.not_enough", price, owned), true);
			keeper.setExpressionState(3, 40);
			keeper.say(player, "dialogue.totf.subterfuge.poor" + (world.rand.nextInt(2) + 1));
			return;
		}

		if (!free) {
			takeTallies(player, price);
		}
		ItemStack purchase = goods.copy();
		balance.setLeftStack(ItemStack.EMPTY);
		balance.setRightStack(ItemStack.EMPTY);
		int restockDays = Settings.worldgenSettings.subterfugeShopRestockDays;
		balance.setRestockAt(restockDays > 0 ? world.getTotalWorldTime() + restockDays * TICKS_PER_DAY : 0L);

		if (!player.addItemStackToInventory(purchase)) {
			player.dropItem(purchase, false);
		}
		world.playSound(null, pos, SoundEvents.ENTITY_VILLAGER_YES, SoundCategory.NEUTRAL, 0.6F, 0.8F);
		keeper.setExpressionState(2, 40);
		if (keeper.isSoldOut()) {
			keeper.say(player, "dialogue.totf.subterfuge.soldout" + (world.rand.nextInt(2) + 1));
		} else {
			keeper.say(player, "dialogue.totf.subterfuge.sale" + (world.rand.nextInt(3) + 1));
		}
	}

	private static long countTallies(EntityPlayer player) {
		long total = 0L;
		for (ItemStack stack : player.inventory.mainInventory) {
			if (stack.getItem() instanceof ItemTallieCollector) {
				total += ItemTallieCollector.getTallieCount(stack);
			}
		}
		for (ItemStack stack : player.inventory.offHandInventory) {
			if (stack.getItem() instanceof ItemTallieCollector) {
				total += ItemTallieCollector.getTallieCount(stack);
			}
		}
		return total;
	}

	private static void takeTallies(EntityPlayer player, long amount) {
		long remaining = amount;
		remaining = takeFrom(player.inventory.mainInventory, remaining);
		takeFrom(player.inventory.offHandInventory, remaining);
	}

	private static long takeFrom(List<ItemStack> stacks, long amount) {
		long remaining = amount;
		for (ItemStack stack : stacks) {
			if (remaining <= 0L) {
				break;
			}
			if (!(stack.getItem() instanceof ItemTallieCollector)) {
				continue;
			}
			long have = ItemTallieCollector.getTallieCount(stack);
			long taken = Math.min(have, remaining);
			remaining -= taken;
			if (have - taken <= 0L) {
				stack.shrink(1);
			} else {
				ItemTallieCollector.setTallieCount(stack, have - taken);
			}
		}
		return remaining;
	}
}
