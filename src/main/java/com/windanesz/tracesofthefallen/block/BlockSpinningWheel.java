package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.init.ModItems;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockSpinningWheel extends BlockDecoration {

	public BlockSpinningWheel(Material material) {
		super(material);
		setHardness(2.0F);
		setResistance(5.0F);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntitySpinningWheel();
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (hand != EnumHand.MAIN_HAND) {
			return true;
		}

		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (!(tileEntity instanceof TileEntitySpinningWheel)) {
			return true;
		}
		TileEntitySpinningWheel te = (TileEntitySpinningWheel) tileEntity;

		ItemStack heldItem = playerIn.getHeldItem(hand);

		if (!worldIn.isRemote) {
			if (heldItem.getItem() == Items.STICK) {
				ItemStack remaining = te.insertStick(heldItem.copy());
				if (remaining.getCount() != heldItem.getCount()) {
					if (!playerIn.capabilities.isCreativeMode) {
						playerIn.setHeldItem(hand, remaining);
					}
					worldIn.playSound(null, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
				}
			} else if (heldItem.getItem() == Items.STRING) {
				if (te.isSpinning()) {
					if (heldItem.getCount() >= 32 && te.hasStick()) {
						if (!playerIn.capabilities.isCreativeMode) {
							heldItem.shrink(32);
						}
						te.consumeStick();
						ItemStack spindle = new ItemStack(ModItems.silk_spindle, 1);
						if (!playerIn.addItemStackToInventory(spindle)) {
							playerIn.dropItem(spindle, false);
						}
						worldIn.playSound(null, pos, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.BLOCKS, 1.0F, 1.0F);
					}
				}
			} else if (heldItem.isEmpty()) {
				if (playerIn.isSneaking()) {
					playerIn.openGui(com.windanesz.tracesofthefallen.TracesOfTheFallen.instance, com.windanesz.tracesofthefallen.network.ModGuiHandler.GUI_SPINNING_WHEEL, worldIn, pos.getX(), pos.getY(), pos.getZ());
				} else if (!te.isSpinning()) {
					te.startSpinning(60); // Spin for 3 seconds (60 ticks)
					worldIn.playSound(null, pos, SoundEvents.BLOCK_WOOD_BUTTON_CLICK_ON, SoundCategory.BLOCKS, 1.0F, 0.5F);
				}
			} else {
				playerIn.openGui(com.windanesz.tracesofthefallen.TracesOfTheFallen.instance, com.windanesz.tracesofthefallen.network.ModGuiHandler.GUI_SPINNING_WHEEL, worldIn, pos.getX(), pos.getY(), pos.getZ());
			}
		}

		return true;
	}
}
