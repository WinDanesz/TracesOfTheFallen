package com.windanesz.tracesofthefallen.item;

import com.windanesz.tracesofthefallen.block.BlockAncestralSifter;
import com.windanesz.tracesofthefallen.entity.EntityAncestralSifter;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemBlockAncestralSifter extends ItemBlock {

	private final BlockAncestralSifter sifterBlock;

	public ItemBlockAncestralSifter(Block block) {
		super(block);
		this.sifterBlock = (BlockAncestralSifter) block;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		IBlockState state = worldIn.getBlockState(pos);
		BlockPos placementPos = state.getBlock().isReplaceable(worldIn, pos) ? pos : pos.offset(facing);
		EnumFacing entityFacing = player.getHorizontalFacing().getOpposite();

		if (!canPlaceAt(worldIn, placementPos, entityFacing) || !player.canPlayerEdit(placementPos, facing, player.getHeldItem(hand))) {
			return EnumActionResult.FAIL;
		}

		if (!worldIn.isRemote) {
			EntityAncestralSifter sifter = new EntityAncestralSifter(worldIn, placementPos, sifterBlock.getVariant(), entityFacing);
			worldIn.spawnEntity(sifter);
		}

		SoundType soundType = block.getSoundType(block.getDefaultState(), worldIn, placementPos, player);
		worldIn.playSound(player, placementPos, soundType.getPlaceSound(), SoundCategory.BLOCKS,
				(soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);

		if (!player.capabilities.isCreativeMode) {
			player.getHeldItem(hand).shrink(1);
		}

		return EnumActionResult.SUCCESS;
	}

	private boolean canPlaceAt(World world, BlockPos pos, EnumFacing facing) {
		BlockPos supportPos = pos.down();
		if (!world.getBlockState(supportPos).isSideSolid(world, supportPos, EnumFacing.UP)) {
			return false;
		}

		AxisAlignedBB bounds = EntityAncestralSifter.getPlacementBounds(pos, facing);
		return world.getCollisionBoxes(null, bounds).isEmpty()
				&& world.getEntitiesWithinAABB(EntityAncestralSifter.class, bounds, entity -> entity != null && !entity.isDead).isEmpty();
	}
}
