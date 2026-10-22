package com.windanesz.tracesofthefallen.compat.baubles;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilities;
import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.item.ItemArcaneRelic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all integration with the optional <i>Baubles</i> mod. Only this class (and its nested classes) may
 * reference {@code baubles.api}; everything else must go through {@link #enabled()} first.
 */
public final class TotfBaublesIntegration {

	public static final String BAUBLES_MOD_ID = "baubles";

	private static boolean baublesLoaded;

	private TotfBaublesIntegration() {
	}

	public static void init() {
		baublesLoaded = Loader.isModLoaded(BAUBLES_MOD_ID);
	}

	public static boolean enabled() {
		return Settings.miscSettings.baublesIntegration && baublesLoaded;
	}

	/**
	 * Returns the arcane relic stacks worn in the player's charm slots. Must only be called if {@link #enabled()}.
	 */
	public static List<ItemStack> getEquippedRelics(EntityPlayer player) {
		List<ItemStack> relics = new ArrayList<>();

		for (int slot : BaubleType.CHARM.getValidSlots()) {
			ItemStack stack = BaublesApi.getBaublesHandler(player).getStackInSlot(slot);
			if (stack.getItem() instanceof ItemArcaneRelic && ((ItemArcaneRelic) stack.getItem()).isCharm()) {
				relics.add(stack);
			}
		}

		return relics;
	}

	public static ICapabilityProvider createRelicProvider() {
		return new RelicBaubleProvider();
	}

	@SuppressWarnings("unchecked")
	private static final class RelicBaubleProvider implements ICapabilityProvider {

		@Override
		public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
			return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE;
		}

		@Override
		public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
			return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE ? (T) (IBauble) itemStack -> BaubleType.CHARM : null;
		}
	}
}
