package com.windanesz.tracesofthefallen.item;

import com.windanesz.tracesofthefallen.entity.EntityGoblinNest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemGoblinNest extends Item {

	public ItemGoblinNest() {
		this.setMaxStackSize(64);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (facing != EnumFacing.UP) {
			return EnumActionResult.FAIL;
		}

		ItemStack itemstack = player.getHeldItem(hand);
		BlockPos spawnPos = pos.up();

		if (!player.canPlayerEdit(spawnPos, facing, itemstack)) {
			return EnumActionResult.FAIL;
		}

		if (!worldIn.isRemote) {
			EntityGoblinNest nest = new EntityGoblinNest(worldIn, spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);
			nest.setFacing(player.getHorizontalFacing().getOpposite());
			worldIn.spawnEntity(nest);
			worldIn.playSound(null, nest.posX, nest.posY, nest.posZ, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1.0F, 0.8F);
			if (!player.isCreative()) {
				itemstack.shrink(1);
			}
		}

		return EnumActionResult.SUCCESS;
	}
}
