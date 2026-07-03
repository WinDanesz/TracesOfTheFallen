package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;

public class BlockGlassFloatLanded extends BlockDecoration {

	private final BlockGlassFloat.FloatVariant variant;

	public BlockGlassFloatLanded(Material material, BlockGlassFloat.FloatVariant variant) {
		super(material);
		this.variant = variant;
	}

	@Override
	protected boolean canSilkHarvest() {
		return false;
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		return Collections.singletonList(BlockGlassFloat.createStackForVariant(variant));
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
}
