package com.windanesz.tracesofthefallen.item;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.client.ClientProxy;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemWonderFertilizer extends Item {

	public ItemWonderFertilizer() {
		this.setMaxStackSize(64);
		this.setMaxDamage(Settings.miscSettings.wonderFertilizerDurability);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		IBlockState clickedState = world.getBlockState(pos);
		boolean applied = false;

		// Normal radius for IGrowable blocks
		int radius = Settings.miscSettings.wonderFertilizerRadius;

		for (int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= radius; z++) {
				for (int y = 0; y <= 1; y++) {
					BlockPos targetPos = pos.add(x, y, z);
					IBlockState state = world.getBlockState(targetPos);

					if (state.getBlock() instanceof IGrowable) {
						IGrowable growable = (IGrowable) state.getBlock();

						if (growable.canGrow(world, targetPos, state, world.isRemote)) {
							if (!world.isRemote) {
								if (growable.canUseBonemeal(world, world.rand, targetPos, state)) {
									growable.grow(world, world.rand, targetPos, state);
									applied = true;
								}
							} else {
								// Show bonemeal particles on client
								world.playEvent(2005, targetPos, 0);
							}
						}
					}
				}
			}
		}

		if (applied) {
			if (!player.isCreative()) {
				stack.damageItem(1, player);
			}
			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.PASS;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		ClientProxy.addMultiLineDescription(tooltip, TextFormatting.BOLD + "" + TextFormatting.GRAY + I18n.format("item.totf:wonder_fertilizer.desc"));
	}
}
