package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.entity.EntityGoblin;
import com.windanesz.tracesofthefallen.init.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockBonepile extends Block {

	public static final PropertyInteger LAYERS = PropertyInteger.create("layers", 1, 8);
	public static final SoundType SOUND_TYPE_BONEPILE = new SoundType(1.0F, 1.0F, ModSounds.BONE_PILE, ModSounds.BONE_PILE, ModSounds.BONE_PILE, ModSounds.BONE_PILE, ModSounds.BONE_PILE);

	protected static final AxisAlignedBB[] BONEPILE_AABB = new AxisAlignedBB[] {
			new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.0D, 1.0D),
			new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D),
			new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.25D, 1.0D),
			new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.375D, 1.0D),
			new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D),
			new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.625D, 1.0D),
			new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.75D, 1.0D),
			new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.875D, 1.0D),
			new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D)
	};

	public BlockBonepile() {
		super(Material.ROCK);
		this.setDefaultState(this.blockState.getBaseState().withProperty(LAYERS, 1));
		this.setHardness(0.5F);
		this.setResistance(2.0F);
		this.setSoundType(SOUND_TYPE_BONEPILE);
		this.setHarvestLevel("pickaxe", 0);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return BONEPILE_AABB[state.getValue(LAYERS)];
	}

	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		int i = blockState.getValue(LAYERS) - 1;
		AxisAlignedBB aabb = blockState.getBoundingBox(worldIn, pos);
		return new AxisAlignedBB(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, (double)(i * 0.125F), aabb.maxZ);
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return state.getValue(LAYERS) == 8;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return state.getValue(LAYERS) == 8;
	}

	@Override
	public boolean isTopSolid(IBlockState state) {
		return state.getValue(LAYERS) == 8;
	}

	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
		return worldIn.getBlockState(pos).getValue(LAYERS) < 8;
	}

	@Nullable
	@Override
	public PathNodeType getAiPathNodeType(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EntityLiving entity) {
		if (entity instanceof EntityGoblin) {
			return state.getValue(LAYERS) < 8 ? PathNodeType.OPEN : PathNodeType.BLOCKED;
		}
		if (entity != null && (state.getValue(LAYERS) - 1) * 0.125F > entity.stepHeight) {
			return PathNodeType.BLOCKED;
		}
		return state.getValue(LAYERS) < 8 ? PathNodeType.OPEN : PathNodeType.BLOCKED;
	}

	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		IBlockState stateBelow = worldIn.getBlockState(pos.down());
		return stateBelow.isTopSolid() || (stateBelow.getBlock() == this && stateBelow.getValue(LAYERS) == 8);
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if (!this.canPlaceBlockAt(worldIn, pos)) {
			worldIn.destroyBlock(pos, true);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		if (side == EnumFacing.UP) {
			return true;
		} else {
			IBlockState iblockstate = blockAccess.getBlockState(pos.offset(side));
			return iblockstate.getBlock() == this && iblockstate.getValue(LAYERS) >= blockState.getValue(LAYERS) ? false : super.shouldSideBeRendered(blockState, blockAccess, pos, side);
		}
	}

	@Override
	protected ItemStack getSilkTouchDrop(IBlockState state) {
		return new ItemStack(this, state.getValue(LAYERS));
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		int layer = state.getValue(LAYERS);
		World worldObj = world instanceof World ? (World) world : null;
		if (worldObj != null) {
			drops.addAll(getDrops(worldObj, layer, fortune));
		}
	}

	public static NonNullList<ItemStack> getDrops(World world, int layer, int fortune) {
		NonNullList<ItemStack> drops = NonNullList.create();
		Random rand = world.rand;

		// 10% base chance per layer, +13.33% relative per Fortune level
		double itemChance = Settings.miscSettings.bonepileBaseItemChance * (1.0D + (Settings.miscSettings.bonepileItemFortuneMultiplier * fortune));

		for (int i = 0; i < layer; i++) {
			if (rand.nextDouble() < itemChance) {
				// 95% bonemeal, 5% bone
				if (rand.nextDouble() < Settings.miscSettings.bonepileBonemealChance) {
					// Bonemeal
					drops.add(new ItemStack(Items.DYE, 1, 15));
				} else {
					drops.add(new ItemStack(Items.BONE));
				}
			}
		}

		// 0.125% per layer, +1% additive per Fortune level
		double skullChance = (Settings.miscSettings.bonepileSkullChancePerLayer * layer) + (Settings.miscSettings.bonepileSkullFortuneBonus * fortune);

		if (rand.nextDouble() < skullChance) {
			// Skeleton skull
			drops.add(new ItemStack(Items.SKULL, 1, 0));
		}

		return drops;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, LAYERS);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(LAYERS, (meta & 7) + 1);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(LAYERS) - 1;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return face == EnumFacing.DOWN ? BlockFaceShape.SOLID : (face == EnumFacing.UP && state.getValue(LAYERS) == 8 ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED);
	}

	@Override
	public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		if (entityIn instanceof EntityGoblin) {
			return;
		}
		entityIn.motionX *= 0.8D;
		entityIn.motionZ *= 0.8D;

		if (!worldIn.isRemote && entityIn instanceof EntityLivingBase && (Math.abs(entityIn.motionX) > 0.01D || Math.abs(entityIn.motionZ) > 0.01D)) {
			if (entityIn.ticksExisted % 8 == 0) {
				worldIn.playSound(null, pos, ModSounds.BONE_PILE, SoundCategory.BLOCKS, 0.7F, worldIn.rand.nextFloat() * 0.2F + 0.9F);
			}
		}
	}
}
