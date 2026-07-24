package com.windanesz.tracesofthefallen.item;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.client.ClientProxy;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemRuneOfSkimming extends ItemRune {

	private static final String TAG_DIM = "dim";
	private static final String TAG_POS_X = "posX";
	private static final String TAG_POS_Y = "posY";
	private static final String TAG_POS_Z = "posZ";

	public ItemRuneOfSkimming() {
		super();
		maxStackSize = 1;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 72000;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BOW;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack itemstack = player.getHeldItem(hand);
		player.setActiveHand(hand);
		return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
		if (player.world.isRemote || !(player instanceof EntityPlayer)) {
			return;
		}

		int useDuration = this.getMaxItemUseDuration(stack) - count;

		if (useDuration >= 40) { // 2 seconds
			if (player.isSneaking()) {
				markLocation(stack, (EntityPlayer) player);
			}
			else {
				teleport(stack, (EntityPlayer) player);
			}
			player.stopActiveHand();
			com.windanesz.tracesofthefallen.block.TileEntityWroughtCagedLamp.triggerNearbyWroughtLamps(player.world, player.getPosition());
		}
	}

	private void markLocation(ItemStack stack, EntityPlayer player) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		NBTTagCompound nbt = stack.getTagCompound();
		BlockPos pos = player.getPosition();
		nbt.setInteger(TAG_POS_X, pos.getX());
		nbt.setInteger(TAG_POS_Y, pos.getY());
		nbt.setInteger(TAG_POS_Z, pos.getZ());
		nbt.setInteger(TAG_DIM, player.dimension);

		player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 0.5F, 1.2F);
		player.sendMessage(new TextComponentTranslation("totf.rune.location_set"));
	}

	private void teleport(ItemStack stack, EntityPlayer player) {
		if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey(TAG_DIM)) {
			player.sendMessage(new TextComponentTranslation("totf.rune.no_location"));
			return;
		}

		NBTTagCompound nbt = stack.getTagCompound();
		int dim = nbt.getInteger(TAG_DIM);
		BlockPos pos = new BlockPos(nbt.getInteger(TAG_POS_X), nbt.getInteger(TAG_POS_Y), nbt.getInteger(TAG_POS_Z));

		if (player.dimension != dim) {
			player.sendMessage(new TextComponentTranslation("totf.rune.wrong_dimension"));
			return;
		}

		double distanceSq = player.getDistanceSq(pos);
		double maxDistance = Settings.miscSettings.runeOfSkimmingMaxDistance;

		if (distanceSq > maxDistance * maxDistance) {
			player.sendMessage(new TextComponentTranslation("totf.rune.too_far"));
			return;
		}

		if (player instanceof EntityPlayerMP) {
			EntityPlayerMP playerMP = (EntityPlayerMP) player;

			// Before teleport
			player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
			((WorldServer)player.world).spawnParticle(EnumParticleTypes.PORTAL, player.posX, player.posY + player.height / 2, player.posZ, 50, 0.5, 1.0, 0.5, 0.0);

			playerMP.connection.setPlayerLocation(pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, player.rotationYaw, player.rotationPitch);
			
			// After teleport
			player.world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
			((WorldServer)player.world).spawnParticle(EnumParticleTypes.PORTAL, pos.getX() + 0.5, pos.getY() + 0.5 + player.height / 2, pos.getZ() + 0.5, 50, 0.5, 1.0, 0.5, 0.0);
			
			player.getCooldownTracker().setCooldown(this, Settings.miscSettings.runeOfSkimmingCooldown); // Cooldown from settings
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		ClientProxy.addMultiLineDescription(tooltip, TextFormatting.BOLD + "" + TextFormatting.GRAY + I18n.format("item.totf:rune_of_skimming.desc"));
		ClientProxy.addMultiLineDescription(tooltip, TextFormatting.DARK_GRAY + I18n.format("item.totf:rune_of_skimming.desc2"));

		if (stack.hasTagCompound() && stack.getTagCompound().hasKey(TAG_DIM)) {
			NBTTagCompound nbt = stack.getTagCompound();
			int x = nbt.getInteger(TAG_POS_X);
			int y = nbt.getInteger(TAG_POS_Y);
			int z = nbt.getInteger(TAG_POS_Z);
			int dim = nbt.getInteger(TAG_DIM);
			tooltip.add(TextFormatting.GRAY + I18n.format("totf.rune.bound_to", x, y, z, dim));
		}
	}
}