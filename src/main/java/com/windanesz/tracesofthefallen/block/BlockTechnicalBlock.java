package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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
		super(Material.ROCK);
		setBlockUnbreakable();
		setResistance(6000000.0F);
	}

	private BlockPos getMainPos(IBlockAccess world, BlockPos pos) {
		BlockPos downPos = pos.down();
		IBlockState downState = world.getBlockState(downPos);
		if (isMainBlock(world, downState, downPos, pos)) {
			return downPos;
		}
		BlockPos upPos = pos.up();
		IBlockState upState = world.getBlockState(upPos);
		if (isMainBlock(world, upState, upPos, pos)) {
			return upPos;
		}

		// Search surrounding 3x3x3 cube for horizontal and 3D multiblocks
		for (int dx = -2; dx <= 2; dx++) {
			for (int dy = -2; dy <= 2; dy++) {
				for (int dz = -2; dz <= 2; dz++) {
					if (dx == 0 && dy == 0 && dz == 0) continue;
					BlockPos candidatePos = pos.add(dx, dy, dz);
					IBlockState candidateState = world.getBlockState(candidatePos);
					if (isMainBlock(world, candidateState, candidatePos, pos)) {
						return candidatePos;
					}
				}
			}
		}

		return downPos;
	}

	private boolean isMainBlock(IBlockAccess world, IBlockState state, BlockPos candidatePos, BlockPos proxyPos) {
		if (state.getBlock() instanceof IProxyMainBlock) {
			return ((IProxyMainBlock) state.getBlock()).isMainBlockForProxy(world, candidatePos, proxyPos);
		}
		if (state.getBlock() instanceof BlockDecoration) {
			return candidatePos.equals(proxyPos.down()) || candidatePos.equals(proxyPos.up());
		}
		return false;
	}

	private boolean isProxyTarget(IBlockState mainState) {
		return mainState.getBlock() instanceof IProxyMainBlock || mainState.getBlock() instanceof BlockDecoration;
	}

	@Override
	public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
		BlockPos mainPos = getMainPos(worldIn, pos);
		IBlockState mainState = worldIn.getBlockState(mainPos);
		if (isProxyTarget(mainState)) {
			return mainState.getBlock().getBlockHardness(mainState, worldIn, mainPos);
		}
		return 1.0F;
	}

	@SuppressWarnings("deprecation")
	@Override
	public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World worldIn, BlockPos pos) {
		BlockPos mainPos = getMainPos(worldIn, pos);
		IBlockState mainState = worldIn.getBlockState(mainPos);
		if (isProxyTarget(mainState)) {
			return mainState.getBlock().getPlayerRelativeBlockHardness(mainState, player, worldIn, mainPos);
		}
		return super.getPlayerRelativeBlockHardness(state, player, worldIn, pos);
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		BlockPos mainPos = getMainPos(world, pos);
		IBlockState mainState = world.getBlockState(mainPos);
		if (isProxyTarget(mainState)) {
			if (!player.capabilities.isCreativeMode && !world.isRemote) {
				boolean canHarvest = mainState.getBlock().canHarvestBlock(world, mainPos, player);
				if (canHarvest) {
					mainState.getBlock().dropBlockAsItem(world, mainPos, mainState, 0);
				}
			}
			world.setBlockToAir(mainPos);
			return true;
		}
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
		// Main block drops item when broken in removedByPlayer
	}

	@Override
	public void onBlockExploded(World worldIn, BlockPos pos, Explosion explosionIn) {
		BlockPos mainPos = getMainPos(worldIn, pos);
		IBlockState mainState = worldIn.getBlockState(mainPos);
		if (isProxyTarget(mainState)) {
			mainState.getBlock().onBlockExploded(worldIn, mainPos, explosionIn);
		}
		super.onBlockExploded(worldIn, pos, explosionIn);
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		BlockPos mainPos = getMainPos(worldIn, pos);
		IBlockState mainState = worldIn.getBlockState(mainPos);
		if (!isProxyTarget(mainState)) {
			worldIn.setBlockToAir(pos);
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		BlockPos mainPos = getMainPos(worldIn, pos);
		IBlockState mainState = worldIn.getBlockState(mainPos);
		if (isProxyTarget(mainState)) {
			float offsetY = mainPos.getY() - pos.getY();
			float offsetX = mainPos.getX() - pos.getX();
			float offsetZ = mainPos.getZ() - pos.getZ();
			return mainState.getBlock().onBlockActivated(worldIn, mainPos, mainState, playerIn, hand, facing, hitX + offsetX, hitY + offsetY, hitZ + offsetZ);
		}
		return false;
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		BlockPos mainPos = getMainPos(world, pos);
		IBlockState mainState = world.getBlockState(mainPos);
		if (isProxyTarget(mainState)) {
			return mainState.getBlock().getPickBlock(mainState, target, world, mainPos, player);
		}
		return ItemStack.EMPTY;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		BlockPos mainPos = getMainPos(source, pos);
		IBlockState mainState = source.getBlockState(mainPos);
		if (mainState.getBlock() instanceof IProxyMainBlock) {
			AxisAlignedBB aabb = ((IProxyMainBlock) mainState.getBlock()).getProxyCellAABB(mainState, source, mainPos, pos);
			return aabb != null ? aabb : NULL_AABB;
		}
		return FULL_BLOCK_AABB;
	}

	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		BlockPos mainPos = getMainPos(worldIn, pos);
		IBlockState mainState = worldIn.getBlockState(mainPos);
		if (isProxyTarget(mainState)) {
			AxisAlignedBB mainCol = mainState.getBlock().getCollisionBoundingBox(mainState, worldIn, mainPos);
			if (mainCol == NULL_AABB) {
				return NULL_AABB;
			}
			return getBoundingBox(state, worldIn, pos);
		}
		return super.getCollisionBoundingBox(state, worldIn, pos);
	}

	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
		BlockPos mainPos = getMainPos(worldIn, pos);
		IBlockState mainState = worldIn.getBlockState(mainPos);
		if (isProxyTarget(mainState)) {
			return mainState.getBlock().isPassable(worldIn, mainPos);
		}
		return super.isPassable(worldIn, pos);
	}

	@Nullable
	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
		AxisAlignedBB aabb = getBoundingBox(state, world, pos);
		return aabb == NULL_AABB ? null : rayTrace(pos, start, end, aabb);
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		BlockPos mainPos = getMainPos(world, pos);
		IBlockState mainState = world.getBlockState(mainPos);
		if (isProxyTarget(mainState)) {
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
