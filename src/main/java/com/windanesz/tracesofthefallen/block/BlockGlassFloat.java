package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.init.ModBlocks;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockGlassFloat extends BlockDecoration {

	public static final PropertyEnum<MountType> MOUNT = PropertyEnum.create("mount", MountType.class);

	private final FloatVariant variant;

	public BlockGlassFloat(Material material, FloatVariant variant) {
		super(material);
		this.variant = variant;
		setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(MOUNT, MountType.FLOOR));
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
			int meta, EntityLivingBase placer) {
		if (facing.getAxis().isHorizontal()) {
			BlockPos supportPos = pos.offset(facing.getOpposite());
			IBlockState supportState = worldIn.getBlockState(supportPos);
			if (supportState.isSideSolid(worldIn, supportPos, facing)) {
				return getDefaultState().withProperty(FACING, facing).withProperty(MOUNT, MountType.WALL);
			}
		}

		if (facing == EnumFacing.DOWN) {
			BlockPos supportPos = pos.up();
			IBlockState supportState = worldIn.getBlockState(supportPos);
			if (supportState.isSideSolid(worldIn, supportPos, EnumFacing.DOWN)) {
				return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite()).withProperty(MOUNT, MountType.CEILING);
			}
		}

		return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite()).withProperty(MOUNT, MountType.FLOOR);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getHorizontalIndex() | (state.getValue(MOUNT).getMetadata() << 2);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3))
				.withProperty(MOUNT, MountType.byMetadata((meta >> 2) & 3));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, MOUNT);
	}

	@Override
	protected ItemStack getSilkTouchDrop(IBlockState state) {
		return new ItemStack(Item.getItemFromBlock(this));
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(Item.getItemFromBlock(this));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}

	public static boolean isWaterSurface(World world, BlockPos waterPos) {
		IBlockState state = world.getBlockState(waterPos);
		return state.getMaterial() == Material.WATER
				&& state.getBlock() instanceof BlockLiquid
				&& state.getValue(BlockLiquid.LEVEL) == 0;
	}

	public static int getWaterDepth(World world, BlockPos waterPos) {
		int depth = 0;
		BlockPos currentPos = waterPos;

		while (world.getBlockState(currentPos).getMaterial() == Material.WATER) {
			depth++;
			currentPos = currentPos.down();
		}

		return depth;
	}

	public static int getRopeCapacity(World world, BlockPos waterPos) {
		return Math.max(0, getWaterDepth(world, waterPos) - 1);
	}

	public static ItemStack createStackForVariant(FloatVariant variant) {
		return new ItemStack(Item.getItemFromBlock(getBlockForVariant(variant)));
	}

	public FloatVariant getVariant() {
		return variant;
	}

	public static BlockGlassFloat getBlockForVariant(FloatVariant variant) {
		switch (variant) {
			case WINE_GREEN:
				return (BlockGlassFloat) ModBlocks.glass_float_wine_green;
			case LONGING_BLUE:
				return (BlockGlassFloat) ModBlocks.glass_float_longing_blue;
			case BLOOD_RED:
				return (BlockGlassFloat) ModBlocks.glass_float_blood_red;
			case CLEAR_BLUE:
			default:
				return (BlockGlassFloat) ModBlocks.glass_float_clear_blue;
		}
	}

	public enum FloatVariant implements IStringSerializable {
		CLEAR_BLUE(0, "clear_blue"),
		WINE_GREEN(1, "wine_green"),
		LONGING_BLUE(2, "longing_blue"),
		BLOOD_RED(3, "blood_red");

		private static final FloatVariant[] VALUES = values();

		private final int metadata;
		private final String name;
		private final String registryName;

		FloatVariant(int metadata, String name) {
			this.metadata = metadata;
			this.name = name;
			this.registryName = "glass_float_" + name;
		}

		public int getMetadata() {
			return metadata;
		}

		public String getRegistryName() {
			return registryName;
		}

		public String getModelName() {
			return registryName;
		}

		public String getWaterModelName() {
			return this == CLEAR_BLUE ? "glass_float_water" : registryName + "_water";
		}

		public String getWaterTopModelName() {
			return this == CLEAR_BLUE ? "glass_float_water_top" : registryName + "_water_top";
		}

		@Override
		public String getName() {
			return name;
		}

		public static FloatVariant byMetadata(int metadata) {
			if (metadata < 0 || metadata >= VALUES.length) {
				return CLEAR_BLUE;
			}
			return VALUES[metadata];
		}
	}

	public enum MountType implements IStringSerializable {
		FLOOR(0, "floor"),
		WALL(1, "wall"),
		CEILING(2, "ceiling");

		private static final MountType[] VALUES = values();

		private final int metadata;
		private final String name;

		MountType(int metadata, String name) {
			this.metadata = metadata;
			this.name = name;
		}

		public int getMetadata() {
			return metadata;
		}

		@Override
		public String getName() {
			return name;
		}

		public static MountType byMetadata(int metadata) {
			if (metadata < 0 || metadata >= VALUES.length) {
				return FLOOR;
			}
			return VALUES[metadata];
		}
	}
}
