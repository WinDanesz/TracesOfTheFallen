package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import com.windanesz.tracesofthefallen.init.ModItems;
import com.windanesz.tracesofthefallen.network.ModGuiHandler;
import net.minecraft.block.Block;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.function.Consumer;

public class BlockSpinningWheel extends BlockDecoration {

	// NORTH-canonical master AABB from OBJ vertex extents (all sub-models combined).
	private static final MultiblockAABBHelper AABB_HELPER = new MultiblockAABBHelper(
			Arrays.asList(new AxisAlignedBB(0.0977, 0.0, -0.9848, 0.6915, 1.9486, 1.4111)),
			0, 0, 0, 1, -1, 1);

	public BlockSpinningWheel(Material material) {
		super(material);
		setHardness(2.0F);
		setResistance(5.0F);
		setLightOpacity(0);
	}

	// --- proxy positions -------------------------------------------------

	private void forEachProxyOffset(IBlockState state, Consumer<BlockPos> action) {
		BlockPos fwd = new BlockPos(state.getValue(FACING).getDirectionVec());
		BlockPos bwd = new BlockPos(state.getValue(FACING).getOpposite().getDirectionVec());
		action.accept(new BlockPos(0, 1, 0));
		action.accept(fwd);
		action.accept(fwd.up());
		action.accept(bwd);
		action.accept(bwd.up());
	}

	@Override
	public boolean isMainBlockForProxy(IBlockAccess world, BlockPos mainPos, BlockPos proxyPos) {
		IBlockState state = world.getBlockState(mainPos);
		if (state.getBlock() != this) return false;
		boolean[] found = {false};
		forEachProxyOffset(state, off -> { if (mainPos.add(off).equals(proxyPos)) found[0] = true; });
		return found[0];
	}

	// --- AABB -------------------------------------------------------------

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return AABB_HELPER.get(state.getValue(FACING), pos, pos);
	}

	@Override
	public AxisAlignedBB getProxyCellAABB(IBlockState state, IBlockAccess world, BlockPos mainPos, BlockPos proxyPos) {
		return AABB_HELPER.get(state.getValue(FACING), mainPos, proxyPos);
	}

	// --- placement / lifecycle --------------------------------------------

	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		if (!super.canPlaceBlockAt(worldIn, pos)) return false;
		boolean[] ok = {true};
		forEachProxyOffset(getDefaultState(), off -> {
			BlockPos p = pos.add(off);
			if (!worldIn.getBlockState(p).getBlock().isReplaceable(worldIn, p) && !worldIn.isAirBlock(p)) ok[0] = false;
		});
		return ok[0];
	}

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		super.onBlockAdded(worldIn, pos, state);
		if (!worldIn.isRemote) {
			forEachProxyOffset(state, off -> {
				BlockPos p = pos.add(off);
				if (worldIn.getBlockState(p).getBlock().isReplaceable(worldIn, p) || worldIn.isAirBlock(p)) {
					worldIn.setBlockState(p, ModBlocks.technical_block.getDefaultState(), 3);
				}
			});
		}
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		if (!worldIn.isRemote) {
			forEachProxyOffset(state, off -> {
				BlockPos p = pos.add(off);
				if (worldIn.getBlockState(p).getBlock() == ModBlocks.technical_block) {
					worldIn.setBlockToAir(p);
				}
			});
		}
		super.breakBlock(worldIn, pos, state);
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
		if (!worldIn.isRemote) {
			forEachProxyOffset(state, off -> {
				BlockPos p = pos.add(off);
				if (worldIn.getBlockState(p).getBlock() != ModBlocks.technical_block) {
					if (worldIn.getBlockState(p).getBlock().isReplaceable(worldIn, p) || worldIn.isAirBlock(p)) {
						worldIn.setBlockState(p, ModBlocks.technical_block.getDefaultState(), 3);
					}
				}
			});
		}
	}

	// --- tile entity / interaction ----------------------------------------

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
					playerIn.openGui(TracesOfTheFallen.instance, ModGuiHandler.GUI_SPINNING_WHEEL, worldIn, pos.getX(), pos.getY(), pos.getZ());
				} else if (!te.isSpinning()) {
					te.startSpinning(60); // Spin for 3 seconds (60 ticks)
					worldIn.playSound(null, pos, SoundEvents.BLOCK_WOOD_BUTTON_CLICK_ON, SoundCategory.BLOCKS, 1.0F, 0.5F);
				}
			} else {
				playerIn.openGui(TracesOfTheFallen.instance, ModGuiHandler.GUI_SPINNING_WHEEL, worldIn, pos.getX(), pos.getY(), pos.getZ());
			}
		}

		return true;
	}
}

