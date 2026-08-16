package com.windanesz.tracesofthefallen.item;

import com.windanesz.tracesofthefallen.PorcelainLiquids;
import com.windanesz.tracesofthefallen.block.TileEntityPorcelainVessel;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

public class ItemBlockPorcelainPiece extends ItemBlock {

	public ItemBlockPorcelainPiece(Block block) {
		super(block);
		if (block == ModBlocks.porcelain_cup) {
			addPropertyOverride(new ResourceLocation("filled"),
					(stack, worldIn, entityIn) -> TileEntityPorcelainVessel.hasStoredCupContent(stack) ? 1.0F : 0.0F);
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		ItemStack stack = playerIn.getHeldItem(handIn);
		if (this.block == ModBlocks.porcelain_cup && TileEntityPorcelainVessel.hasStoredCupContent(stack)) {
			playerIn.setActiveHand(handIn);
			return new ActionResult<>(EnumActionResult.SUCCESS, stack);
		}

		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		if (side == EnumFacing.UP && !player.isSneaking()) {
			IBlockState clickedState = world.getBlockState(pos);
			Block clickedBlock = clickedState.getBlock();
			ResourceLocation regName = clickedBlock.getRegistryName();
			if (TileEntityPorcelainVessel.isConfiguredHeatSource(regName)) {
				EnumActionResult result = onItemUse(player, world, pos, hand, side, hitX, hitY, hitZ);
				if (result != EnumActionResult.PASS) {
					return result;
				}
			}
		}
		return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return this.block == ModBlocks.porcelain_cup && TileEntityPorcelainVessel.hasStoredCupContent(stack)
				? EnumAction.DRINK
				: super.getItemUseAction(stack);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return this.block == ModBlocks.porcelain_cup && TileEntityPorcelainVessel.hasStoredCupContent(stack)
				? 32
				: super.getMaxItemUseDuration(stack);
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
		if (this.block != ModBlocks.porcelain_cup || !TileEntityPorcelainVessel.hasStoredCupContent(stack)) {
			return super.onItemUseFinish(stack, worldIn, entityLiving);
		}

		String liquidType = TileEntityPorcelainVessel.getStoredCupContentType(stack);
		PorcelainLiquids.LiquidDefinition liquid = PorcelainLiquids.getByType(liquidType);
		ItemStack emptyCup = new ItemStack(this);
		if (entityLiving instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) entityLiving;
			if (!worldIn.isRemote && liquid != null) {
				player.addPotionEffect(new PotionEffect(liquid.potion, liquid.durationTicks, liquid.amplifier));
			}

			if (player.capabilities.isCreativeMode) {
				return stack;
			}
		}

		stack.shrink(1);
		if (stack.isEmpty()) {
			return emptyCup;
		}

		if (entityLiving instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) entityLiving;
			if (!player.inventory.addItemStackToInventory(emptyCup)) {
				player.dropItem(emptyCup, false);
			}
		}

		return stack;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		if (this.block == ModBlocks.porcelain_cup && TileEntityPorcelainVessel.hasStoredCupContent(stack)) {
			String liquidType = TileEntityPorcelainVessel.getStoredCupContentType(stack);
			String liquidKey = "item.totf:porcelain_liquid." + liquidType;
			String liquidName = new TextComponentTranslation(liquidKey).getUnformattedText();
			if (liquidName.equals(liquidKey)) {
				liquidName = liquidType.replace('_', ' ');
			}
			return new TextComponentTranslation("item.totf:porcelain_cup.of", liquidName).getUnformattedText();
		}
		return super.getItemStackDisplayName(stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);

		if (this.block == ModBlocks.porcelain_pot) {
			addPotInformation(stack, tooltip);
			return;
		}

		if (this.block == ModBlocks.porcelain_cup && TileEntityPorcelainVessel.hasStoredCupContent(stack)) {
			tooltip.add(TextFormatting.GRAY + I18n.format("item.totf:porcelain_cup.filled"));
			tooltip.add(TextFormatting.DARK_GRAY + I18n.format("item.totf:porcelain_cup.drink"));
		}
	}

	@SideOnly(Side.CLIENT)
	private void addPotInformation(ItemStack stack, List<String> tooltip) {
		tooltip.add(TextFormatting.DARK_GRAY + I18n.format("item.totf:porcelain_pot.tip.place"));
		tooltip.add(TextFormatting.DARK_GRAY + I18n.format("item.totf:porcelain_pot.tip.water"));
		tooltip.add(TextFormatting.DARK_GRAY + I18n.format("item.totf:porcelain_pot.tip.brew"));

		boolean brewReady = TileEntityPorcelainVessel.isStoredBrewReady(stack);
		int fluidLevel = TileEntityPorcelainVessel.getStoredFluidLevel(stack);
		if (!brewReady && fluidLevel > 0) {
			tooltip.add(TextFormatting.GRAY + I18n.format("item.totf:porcelain_pot.water",
					getFluidDisplayName(TileEntityPorcelainVessel.getStoredBaseFluidType(stack)), fluidLevel, 3));
		}

		if (brewReady) {
			String liquidType = TileEntityPorcelainVessel.getStoredBrewLiquidType(stack);
			tooltip.add(TextFormatting.DARK_GRAY + I18n.format(
					"item.totf:porcelain_pot.brew_ready",
					getLiquidDisplayName(liquidType),
					Math.max(1, TileEntityPorcelainVessel.getStoredBrewServings(stack))));
		} else if (TileEntityPorcelainVessel.isStoredHeated(stack)) {
			tooltip.add(TextFormatting.DARK_GRAY + I18n.format("item.totf:porcelain_pot.heated"));
		}
	}

	@SideOnly(Side.CLIENT)
	private String getLiquidDisplayName(String liquidType) {
		String normalized = liquidType == null ? "" : liquidType.trim().toLowerCase(Locale.ROOT);
		if (normalized.isEmpty()) {
			normalized = "tea";
		}
		String key = "item.totf:porcelain_liquid." + normalized;
		String localized = I18n.format(key);
		return key.equals(localized) ? normalized.replace('_', ' ') : localized;
	}

	@SideOnly(Side.CLIENT)
	private String getFluidDisplayName(String fluidType) {
		String normalized = fluidType == null ? "" : fluidType.trim().toLowerCase(Locale.ROOT);
		if (normalized.isEmpty()) {
			return I18n.format("item.totf:porcelain_fluid.generic");
		}
		String key = "item.totf:porcelain_fluid." + normalized;
		String localized = I18n.format(key);
		return key.equals(localized) ? normalized.replace('_', ' ') : localized;
	}
}
