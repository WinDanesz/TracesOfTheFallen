package com.windanesz.tracesofthefallen.item;

import com.windanesz.tracesofthefallen.block.BlockGlassFloat;
import com.windanesz.tracesofthefallen.entity.EntityGlassFloat;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class ItemBlockGlassFloat extends ItemBlock {

	private final BlockGlassFloat glassFloat;

	public ItemBlockGlassFloat(Block block) {
		super(block);
		this.glassFloat = (BlockGlassFloat) block;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (facing == EnumFacing.UP && BlockGlassFloat.isWaterSurface(world, pos)) {
			return placeOnWater(player, world, pos, hand, facing);
		}

		return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		RayTraceResult result = this.rayTrace(world, player, true);

		if (result == null || result.typeOfHit != RayTraceResult.Type.BLOCK) {
			return new ActionResult<>(EnumActionResult.PASS, stack);
		}

		BlockPos pos = result.getBlockPos();
		if (!world.isBlockModifiable(player, pos) || !player.canPlayerEdit(pos.up(), EnumFacing.UP, stack)) {
			return new ActionResult<>(EnumActionResult.FAIL, stack);
		}

		if (BlockGlassFloat.isWaterSurface(world, pos)) {
			return new ActionResult<>(placeOnWater(player, world, pos, hand, EnumFacing.UP), stack);
		}

		return new ActionResult<>(EnumActionResult.PASS, stack);
	}

	private EnumActionResult placeOnWater(EntityPlayer player, World world, BlockPos waterPos, EnumHand hand, EnumFacing facing) {
		ItemStack stack = player.getHeldItem(hand);
		BlockPos placePos = waterPos.up();

		if (!world.isAirBlock(placePos) || !player.canPlayerEdit(placePos, facing, stack)) {
			return EnumActionResult.FAIL;
		}

		if (!world.isRemote) {
			EntityGlassFloat entity = new EntityGlassFloat(world, placePos, glassFloat.getVariant(), BlockGlassFloat.getRopeCapacity(world, waterPos));
			world.spawnEntity(entity);
		}

		SoundType soundtype = this.block.getSoundType(this.block.getDefaultState(), world, placePos, player);
		world.playSound(player, placePos, soundtype.getPlaceSound(), SoundCategory.BLOCKS,
				(soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

		if (!player.capabilities.isCreativeMode) {
			stack.shrink(1);
		}

		return EnumActionResult.SUCCESS;
	}
}
