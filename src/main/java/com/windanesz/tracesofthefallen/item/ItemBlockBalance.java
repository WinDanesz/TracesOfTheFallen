package com.windanesz.tracesofthefallen.item;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.block.BalanceMode;
import com.windanesz.tracesofthefallen.client.ClientProxy;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockBalance extends ItemBlock {

	public ItemBlockBalance(Block block) {
		super(block);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		ItemStack stack = playerIn.getHeldItem(handIn);
		BalanceMode nextMode = BalanceMode.fromStack(stack).next();
		BalanceMode.applyToStack(stack, nextMode);

		if (worldIn.isRemote) {
			TracesOfTheFallen.proxy.renderItemActivation(stack.copy());
			String key = nextMode == BalanceMode.REDSTONE ? "totf.balance.toast.redstone" : "totf.balance.toast.vending";
			playerIn.sendStatusMessage(new TextComponentTranslation(key), true);
		}

		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		BalanceMode mode = BalanceMode.fromStack(stack);
		tooltip.add(TextFormatting.GRAY + I18n.format("item.totf:balance.mode", I18n.format(mode.getTranslationKey())));
		ClientProxy.addMultiLineDescription(tooltip, TextFormatting.DARK_GRAY + I18n.format("item.totf:balance.mode_switch"));
	}
}
