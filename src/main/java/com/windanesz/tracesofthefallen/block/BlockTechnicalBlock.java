package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockTechnicalBlock extends Block {

	public BlockTechnicalBlock() {
		super(Material.AIR);
		setBlockUnbreakable();
		setResistance(6000000.0F);
	}

	private BlockPos getMainPos(IBlockAccess world, BlockPos pos) {
		BlockPos downPos = pos.down();
		IBlockState downState = world.getBlockState(downPos);
		if (downState.getBlock() instanceof BlockDecoration) {
			return downPos;
		}
		BlockPos upPos = pos.up();
		IBlockState upState = world.getBlockState(upPos);
		if (upState.getBlock() instanceof BlockDecoration) {
			return upPos;
		}
		return downPos;
	}

	@Override
	public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
		BlockPos mainPos = getMainPos(worldIn, pos);
		IBlockState mainState = worldIn.getBlockState(mainPos);
		if (mainState.getBlock() instanceof BlockDecoration) {
			return mainState.getBlock().getBlockHardness(mainState, worldIn, mainPos);
		}
		return 1.0F;
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		BlockPos mainPos = getMainPos(world, pos);
		IBlockState mainState = world.getBlockState(mainPos);
		if (mainState.getBlock() instanceof BlockDecoration) {
			if (!player.capabilities.isCreativeMode && !world.isRemote) {
				mainState.getBlock().dropBlockAsItem(world, mainPos, mainState, 0);
			}
			world.setBlockToAir(mainPos);
		}
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable net.minecraft.tileentity.TileEntity te, ItemStack stack) {
		// Main block drops item when broken in removedByPlayer
	}

	@Override
	public void onBlockExploded(World worldIn, BlockPos pos, Explosion explosionIn) {
		BlockPos mainPos = getMainPos(worldIn, pos);
		IBlockState mainState = worldIn.getBlockState(mainPos);
		if (mainState.getBlock() instanceof BlockDecoration) {
			mainState.getBlock().onBlockExploded(worldIn, mainPos, explosionIn);
		}
		super.onBlockExploded(worldIn, pos, explosionIn);
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		BlockPos mainPos = getMainPos(worldIn, pos);
		IBlockState mainState = worldIn.getBlockState(mainPos);
		if (!(mainState.getBlock() instanceof BlockDecoration)) {
			worldIn.setBlockToAir(pos);
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		BlockPos mainPos = getMainPos(worldIn, pos);
		IBlockState mainState = worldIn.getBlockState(mainPos);
		if (mainState.getBlock() instanceof BlockDecoration) {
			float offsetY = mainPos.getY() - pos.getY();
			return mainState.getBlock().onBlockActivated(worldIn, mainPos, mainState, playerIn, hand, facing, hitX, hitY + offsetY, hitZ);
		}
		return false;
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		BlockPos mainPos = getMainPos(world, pos);
		IBlockState mainState = world.getBlockState(mainPos);
		if (mainState.getBlock() instanceof BlockDecoration) {
			return mainState.getBlock().getPickBlock(mainState, target, world, mainPos, player);
		}
		return ItemStack.EMPTY;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		BlockPos mainPos = getMainPos(source, pos);
		IBlockState mainState = source.getBlockState(mainPos);
		if (mainState.getBlock() instanceof BlockDecoration) {
			AxisAlignedBB bb = mainState.getBlock().getBoundingBox(mainState, source, mainPos);
			return bb.offset(0, mainPos.getY() - pos.getY(), 0);
		}
		return FULL_BLOCK_AABB;
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
		BlockPos mainPos = getMainPos(worldIn, pos);
		IBlockState mainState = worldIn.getBlockState(mainPos);
		if (mainState.getBlock() instanceof BlockDecoration) {
			return mainState.getBlock().getSelectedBoundingBox(mainState, worldIn, mainPos);
		}
		return super.getSelectedBoundingBox(state, worldIn, pos);
	}

	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return NULL_AABB;
	}

	@Nullable
	@Override
	public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
		BlockPos mainPos = getMainPos(worldIn, pos);
		IBlockState mainState = worldIn.getBlockState(mainPos);
		if (mainState.getBlock() instanceof BlockDecoration) {
			RayTraceResult result = mainState.getBlock().collisionRayTrace(mainState, worldIn, mainPos, start, end);
			if (result != null) {
				return new RayTraceResult(result.hitVec, result.sideHit, pos);
			}
			return null;
		}
		return super.collisionRayTrace(blockState, worldIn, pos, start, end);
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		BlockPos mainPos = getMainPos(world, pos);
		IBlockState mainState = world.getBlockState(mainPos);
		if (mainState.getBlock() instanceof BlockDecoration) {
			return mainState.getBlock().getLightValue(mainState, world, mainPos);
		}
		return 0;
	}

	@Override
	public EnumPushReaction getPushReaction(IBlockState state) {
		return EnumPushReaction.BLOCK;
	}

	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return false;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}
}
