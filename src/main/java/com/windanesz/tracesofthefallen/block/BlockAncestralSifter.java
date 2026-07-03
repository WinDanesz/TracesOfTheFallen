package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.init.ModBlocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;

public class BlockAncestralSifter extends BlockDecoration {

	public static final PropertyBool BROKEN = PropertyBool.create("broken");

	private final SifterVariant variant;

	public BlockAncestralSifter(Material material, SifterVariant variant) {
		super(material);
		this.variant = variant;
		setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(BROKEN, false));
		setHardness(1.0F);
		setResistance(2.0F);
	}

	public SifterVariant getVariant() {
		return variant;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int meta = state.getValue(FACING).getHorizontalIndex();
		if (state.getValue(BROKEN)) {
			meta |= 4;
		}
		return meta;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3))
				.withProperty(BROKEN, (meta & 4) != 0);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, BROKEN);
	}

	public enum SifterVariant implements IStringSerializable {
		GOLDEN(0, "golden", "ancestral_sifter_golden"),
		RED(1, "red", "ancestral_sifter_red");

		private static final SifterVariant[] VALUES = values();

		private final int metadata;
		private final String name;
		private final String registryName;

		SifterVariant(int metadata, String name, String registryName) {
			this.metadata = metadata;
			this.name = name;
			this.registryName = registryName;
		}

		public int getMetadata() {
			return metadata;
		}

		public String getRegistryName() {
			return registryName;
		}

		public String getRenderModelName(boolean broken) {
			return broken ? registryName + "_broken" : registryName;
		}

		public ItemStack createStack() {
			switch (this) {
				case RED:
					return new ItemStack(Item.getItemFromBlock(ModBlocks.ancestral_sifter_red));
				case GOLDEN:
				default:
					return new ItemStack(Item.getItemFromBlock(ModBlocks.ancestral_sifter_golden));
			}
		}

		@Override
		public String getName() {
			return name;
		}

		public static SifterVariant byMetadata(int metadata) {
			if (metadata < 0 || metadata >= VALUES.length) {
				return GOLDEN;
			}
			return VALUES[metadata];
		}
	}
}
