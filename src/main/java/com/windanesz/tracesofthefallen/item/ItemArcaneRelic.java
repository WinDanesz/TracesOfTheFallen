package com.windanesz.tracesofthefallen.item;

import com.windanesz.tracesofthefallen.client.ClientProxy;
import com.windanesz.tracesofthefallen.client.RelicKeyBindings;
import com.windanesz.tracesofthefallen.compat.baubles.TotfBaublesIntegration;
import com.windanesz.tracesofthefallen.init.ModPotions;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemArcaneRelic extends Item {

	private static final double EFFECT_RADIUS = 4.0D;
	private static final int SKULL_COOLDOWN_TICKS = 30 * 20;
	private static final int SKULL_STRENGTH_DURATION = 20 * 20;
	private static final int SKULL_MAX_USES = 20;

	private final RelicEffect effect;

	public ItemArcaneRelic(RelicEffect effect) {
		this.effect = effect;
		this.maxStackSize = 1;
		if (effect.usesCharges()) {
			setMaxDamage(SKULL_MAX_USES);
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);

		if (!canActivate(player, stack)) {
			return new ActionResult<>(EnumActionResult.FAIL, stack);
		}

		if (!world.isRemote) {
			activate(player, stack);
		}

		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	}

	private boolean canActivate(EntityPlayer player, ItemStack stack) {
		return !player.getCooldownTracker().hasCooldown(this) && !effect.isExhausted(stack);
	}

	/** Whether this relic can be worn in a Baubles charm slot and activated with the relic keybind. */
	public boolean isCharm() {
		return effect.isCharm();
	}

	/**
	 * Triggers the relic's ability. Server side only; does nothing if the relic is on cooldown or exhausted.
	 */
	public void activate(EntityPlayer player, ItemStack stack) {
		if (player.world.isRemote || !canActivate(player, stack)) {
			return;
		}

		effect.applyToPlayer(player);

		AxisAlignedBB area = player.getEntityBoundingBox().grow(EFFECT_RADIUS);
		List<EntityLivingBase> entities = player.world.getEntitiesWithinAABB(EntityLivingBase.class, area,
				entity -> entity != null && entity != player && entity.isEntityAlive());

		for (EntityLivingBase entity : entities) {
			effect.applyToEnemy(player, entity);
		}

		player.getCooldownTracker().setCooldown(this, effect.getCooldownTicks());
		effect.consumeUse(stack);
	}

	@Nullable
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
		return effect.isCharm() && TotfBaublesIntegration.enabled() ? TotfBaublesIntegration.createRelicProvider() : null;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		ClientProxy.addMultiLineDescription(tooltip, TextFormatting.BOLD + "" + TextFormatting.GRAY + I18n.format(effect.descriptionKey));
		ClientProxy.addMultiLineDescription(tooltip, TextFormatting.DARK_GRAY + I18n.format(effect.descriptionKey2));
		if (effect.usesCharges()) {
			int remainingUses = Math.max(0, SKULL_MAX_USES - stack.getItemDamage());
			tooltip.add(TextFormatting.GRAY + "Uses remaining: " + remainingUses + "/" + SKULL_MAX_USES);
		}
		if (effect.isCharm() && TotfBaublesIntegration.enabled()) {
			tooltip.add(TextFormatting.DARK_AQUA + I18n.format("item.totf:arcane_relic.charm_hint", RelicKeyBindings.activateRelic.getDisplayName()));
		}
	}

	public enum RelicEffect {
		MAW("item.totf:arcane_skull_maw.desc", "item.totf:arcane_skull_maw.desc2") {
			@Override
			void applyToPlayer(EntityPlayer player) {
				player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, SKULL_STRENGTH_DURATION, 0));
			}

			@Override
			void applyToEnemy(EntityPlayer player, EntityLivingBase entity) {
				entity.attackEntityFrom(DamageSource.causePlayerDamage(player), 6.0F);
			}

			@Override
			boolean usesCharges() {
				return true;
			}

			@Override
			boolean isCharm() {
				return true;
			}

			@Override
			int getCooldownTicks() {
				return SKULL_COOLDOWN_TICKS;
			}
		},
		SHIELD("item.totf:arcane_skull_shield.desc", "item.totf:arcane_skull_shield.desc2") {
			@Override
			void applyToPlayer(EntityPlayer player) {
				player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, SKULL_STRENGTH_DURATION, 0));
			}

			@Override
			void applyToEnemy(EntityPlayer player, EntityLivingBase entity) {
				entity.attackEntityFrom(DamageSource.causePlayerDamage(player), 6.0F);
			}

			@Override
			boolean usesCharges() {
				return true;
			}

			@Override
			boolean isCharm() {
				return true;
			}

			@Override
			int getCooldownTicks() {
				return SKULL_COOLDOWN_TICKS;
			}
		},
		SWIFT("item.totf:arcane_skull_swift.desc", "item.totf:arcane_skull_swift.desc2") {
			@Override
			void applyToPlayer(EntityPlayer player) {
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, SKULL_STRENGTH_DURATION, 0));
			}

			@Override
			void applyToEnemy(EntityPlayer player, EntityLivingBase entity) {
				entity.attackEntityFrom(DamageSource.causePlayerDamage(player), 6.0F);
			}

			@Override
			boolean usesCharges() {
				return true;
			}

			@Override
			boolean isCharm() {
				return true;
			}

			@Override
			int getCooldownTicks() {
				return SKULL_COOLDOWN_TICKS;
			}
		},
		FORBIDDEN_IVORY("item.totf:forbidden_ivory.desc", "item.totf:forbidden_ivory.desc2") {
			@Override
			void applyToPlayer(EntityPlayer player) {
				player.addPotionEffect(new PotionEffect(ModPotions.rage, 200, 0));
			}

			@Override
			void applyToEnemy(EntityPlayer player, EntityLivingBase entity) {
			}

			@Override
			boolean usesCharges() {
				return true;
			}

			@Override
			boolean isCharm() {
				return true;
			}
		};

		private final String descriptionKey;
		private final String descriptionKey2;

		RelicEffect(String descriptionKey, String descriptionKey2) {
			this.descriptionKey = descriptionKey;
			this.descriptionKey2 = descriptionKey2;
		}

		abstract void applyToPlayer(EntityPlayer player);

		abstract void applyToEnemy(EntityPlayer player, EntityLivingBase entity);

		boolean usesCharges() {
			return false;
		}

		boolean isCharm() {
			return false;
		}

		int getCooldownTicks() {
			return 60;
		}

		boolean isExhausted(ItemStack stack) {
			return usesCharges() && stack.getItemDamage() >= stack.getMaxDamage();
		}

		void consumeUse(ItemStack stack) {
			if (!usesCharges()) {
				return;
			}

			stack.setItemDamage(Math.min(stack.getMaxDamage(), stack.getItemDamage() + 1));
		}
	}
}
