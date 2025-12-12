package com.windanesz.tracesofthefallen.item;

import com.windanesz.tracesofthefallen.Settings;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemWardOfBlades extends Item {

	public ItemWardOfBlades() {
		super();
		this.maxStackSize = 1;
		this.setMaxDamage(Settings.miscSettings.idolOfBladesDurability);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack itemstack = player.getHeldItem(hand);
		
		if (!world.isRemote) {
			// Check if player is on cooldown
			if (player.getCooldownTracker().hasCooldown(this)) {
				return new ActionResult<>(EnumActionResult.FAIL, itemstack);
			}

			// Get all living entities in a 9x9 radius (4.5 blocks from center)
			double radius = 4.5D;
			List<EntityLivingBase> nearbyEntities = world.getEntitiesWithinAABB(
					EntityLivingBase.class,
					player.getEntityBoundingBox().grow(radius, radius, radius)
			);

			// Apply effects to all entities in radius
			for (EntityLivingBase entity : nearbyEntities) {
				// Apply slowness II for configured duration
				entity.addPotionEffect(new PotionEffect(
						MobEffects.SLOWNESS,
						Settings.miscSettings.idolOfBladesSlownessDuration,
						1 // Level 2 (0-indexed, so 1 = level II)
				));

				// Deal damage
				entity.attackEntityFrom(
						DamageSource.causePlayerDamage(player),
						(float) Settings.miscSettings.idolOfBladesDamage
				);
			}

			// Set cooldown
			player.getCooldownTracker().setCooldown(this, Settings.miscSettings.idolOfBladesCooldown);

			// Damage the item
			itemstack.damageItem(1, player);
		}

		return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		tooltip.add(TextFormatting.BOLD + "" + TextFormatting.GRAY + I18n.format("item.totf:idol_of_blades.desc"));
		tooltip.add(TextFormatting.DARK_GRAY + I18n.format("item.totf:idol_of_blades.desc2"));
	}
}
