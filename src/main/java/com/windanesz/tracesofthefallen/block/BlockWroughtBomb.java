package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.entity.EntityWroughtBomb;
import com.windanesz.tracesofthefallen.init.ModCreativeTab;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockWroughtBomb extends BlockFalling {

	private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 0.75D, 0.75D);

	public BlockWroughtBomb() {
		super(Material.IRON);
		setHardness(2.0F);
		setResistance(10.0F);
		setCreativeTab(ModCreativeTab.TOTF_TAB);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return BOUNDING_BOX;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		super.onBlockAdded(worldIn, pos, state);
		if (worldIn.isBlockPowered(pos)) {
			this.explode(worldIn, pos, state, null);
			worldIn.setBlockToAir(pos);
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
		if (worldIn.isBlockPowered(pos)) {
			this.explode(worldIn, pos, state, null);
			worldIn.setBlockToAir(pos);
		}
	}

	@Override
	public void onBlockExploded(World worldIn, BlockPos pos, Explosion explosionIn) {
		if (!worldIn.isRemote) {
			EntityWroughtBomb entity = new EntityWroughtBomb(worldIn, (double)pos.getX() + 0.5D, (double)pos.getY(), (double)pos.getZ() + 0.5D, explosionIn.getExplosivePlacedBy());
			entity.setFuse((short)(worldIn.rand.nextInt(entity.getFuse() / 4) + entity.getFuse() / 8));
			worldIn.spawnEntity(entity);
		}
		super.onBlockExploded(worldIn, pos, explosionIn);
	}

	public void explode(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase igniter) {
		if (!worldIn.isRemote) {
			EntityWroughtBomb entity = new EntityWroughtBomb(worldIn, (double)pos.getX() + 0.5D, (double)pos.getY(), (double)pos.getZ() + 0.5D, igniter);
			worldIn.spawnEntity(entity);
			worldIn.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack itemstack = playerIn.getHeldItem(hand);

		if (!itemstack.isEmpty() && (itemstack.getItem() == Items.FLINT_AND_STEEL || itemstack.getItem() == Items.FIRE_CHARGE)) {
			this.explode(worldIn, pos, state, playerIn);
			worldIn.setBlockToAir(pos);

			if (itemstack.getItem() == Items.FLINT_AND_STEEL) {
				itemstack.damageItem(1, playerIn);
			} else if (!playerIn.capabilities.isCreativeMode) {
				itemstack.shrink(1);
			}

			return true;
		} else {
			return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
		}
	}
}
