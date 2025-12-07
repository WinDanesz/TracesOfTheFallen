package com.windanesz.tracesofthefallen.item;

import com.windanesz.tracesofthefallen.entity.EntityFamiliarSpecter;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.UUID;

public class ItemGraveRose extends ItemBlock {
	public ItemGraveRose(Block block) {
		super(block);
		this.setMaxStackSize(1);
		this.setMaxDamage(100);
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return stack.getSubCompound("SpecterUUID") != null;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		ItemStack itemstack = playerIn.getHeldItem(handIn);

		if (playerIn.getCooldownTracker().hasCooldown(this)) {
			return new ActionResult<>(EnumActionResult.FAIL, itemstack);
		}

		// If a specter is already linked, unsummon it
		if (itemstack.getSubCompound("SpecterUUID") != null) {
			if (!worldIn.isRemote) {
				String uuidString = itemstack.getSubCompound("SpecterUUID").getString("UUID");
				if (!uuidString.isEmpty() && worldIn instanceof WorldServer) {
					UUID specterUUID = UUID.fromString(uuidString);
					Entity specter = ((WorldServer) worldIn).getEntityFromUuid(specterUUID);
					if (specter instanceof EntityFamiliarSpecter) {
						specter.setDead();
					}
				}
				itemstack.removeSubCompound("SpecterUUID");
				//itemstack.setItemDamage(itemstack.getMaxDamage());
				if (!playerIn.capabilities.isCreativeMode) {
					playerIn.getCooldownTracker().setCooldown(this, 3 * 60 * 20); // 3 minutes cooldown
				}
			}
			return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
		}

		if (!worldIn.isRemote) {
			// Summon an EntityFamiliarSpecter for the player
			EntityFamiliarSpecter specter = new EntityFamiliarSpecter(worldIn);
			specter.setOwner(playerIn); // Set the player as the owner
			specter.setPosition(playerIn.posX, playerIn.posY + 1.0D, playerIn.posZ);
			worldIn.spawnEntity(specter);

			// Store the UUID of the specter in the itemstack
			itemstack.getOrCreateSubCompound("SpecterUUID").setString("UUID", specter.getUniqueID().toString());
		}

		// The item is not consumed, but its state is now linked to the specter
		return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);

		if (worldIn.isRemote || !(entityIn instanceof EntityPlayer)) {
			return;
		}

		EntityPlayer player = (EntityPlayer) entityIn;

		if (stack.getSubCompound("SpecterUUID") == null) {
			if (stack.getItemDamage() != 0 && !player.getCooldownTracker().hasCooldown(this)) {
				stack.setItemDamage(0);
			}
			return;
		}

		String uuidString = stack.getSubCompound("SpecterUUID").getString("UUID");
		if (uuidString.isEmpty()) {
			stack.removeSubCompound("SpecterUUID");
			return;
		}

		UUID specterUUID = UUID.fromString(uuidString);

		Entity specter = ((WorldServer) worldIn).getEntityFromUuid(specterUUID);

		if (specter instanceof EntityFamiliarSpecter && ((EntityFamiliarSpecter) specter).getHealth() > 0) {
			EntityFamiliarSpecter familiar = (EntityFamiliarSpecter) specter;
			float healthPercent = familiar.getHealth() / familiar.getMaxHealth();
			int damage = (int) (stack.getMaxDamage() * (1.0F - healthPercent));

			if (damage >= stack.getMaxDamage()) {
				damage = stack.getMaxDamage() - 1;
			}
			stack.setItemDamage(damage);
		} else {
			// Specter is gone, don't destroy the item, but clear the UUID so it can summon again
			stack.removeSubCompound("SpecterUUID");
			stack.setItemDamage(stack.getMaxDamage()); // Set to fully broken to indicate cooldown

			if (!player.capabilities.isCreativeMode) {
				player.getCooldownTracker().setCooldown(this, 3 * 60 * 20); // 3 minutes cooldown
			}
		}
	}
}
