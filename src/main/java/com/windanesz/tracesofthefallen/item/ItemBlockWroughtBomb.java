package com.windanesz.tracesofthefallen.item;

import com.windanesz.tracesofthefallen.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class ItemBlockWroughtBomb extends ItemBlock {

	public ItemBlockWroughtBomb(Block block) {
		super(block);
	}

	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {
		if (!entityItem.world.isRemote && entityItem.onGround && !entityItem.isDead) {
			BlockPos pos = new BlockPos(entityItem.posX, entityItem.posY + 0.1D, entityItem.posZ);
			if (entityItem.world.isAirBlock(pos) || entityItem.world.getBlockState(pos).getBlock().isReplaceable(entityItem.world, pos)) {
				entityItem.world.setBlockState(pos, ModBlocks.wrought_bomb.getDefaultState());
				ItemStack stack = entityItem.getItem();
				stack.shrink(1);
				if (stack.isEmpty()) {
					entityItem.setDead();
				}
				return true;
			}
		}
		return false;
	}
}
