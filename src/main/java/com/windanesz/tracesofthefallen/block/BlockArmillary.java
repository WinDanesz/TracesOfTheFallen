package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.capability.HauntingCapability;
import com.windanesz.tracesofthefallen.init.ModCreativeTab;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class BlockArmillary extends Block {
	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public AxisAlignedBB boundingBox = new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 0.9375D, 0.75D);

	public BlockArmillary() {
		super(Material.IRON);
		setHardness(1.0F);
		setResistance(1.0F);
		setCreativeTab(ModCreativeTab.TOTF_TAB);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return this.boundingBox;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityArmillary();
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote) {
			// Calculate total world time in days
			long totalWorldTime = worldIn.getTotalWorldTime();
			long days = totalWorldTime / 24000L; // 24000 ticks = 1 day
			
			// Get moon phase (0-7)
			int moonPhase = worldIn.provider.getMoonPhase(totalWorldTime);
			String moonPhaseName = getMoonPhaseName(moonPhase);
			
			// Send message to player
			playerIn.sendMessage(new TextComponentString("Day " + days + " - Moon Phase: " + moonPhaseName));
			
			// Check haunting progress and display cryptic message if high (50%+)
			HauntingCapability hauntingCap = HauntingCapability.get(playerIn);
			int hauntingProgress = 0;
			if (hauntingCap != null) {
				hauntingProgress = hauntingCap.getHauntingProgress();
				
				if (hauntingProgress >= 50) {
					TextComponentTranslation msg = new TextComponentTranslation("totf:armillary.haunted");
					msg.getStyle().setColor(TextFormatting.DARK_PURPLE);
					playerIn.sendMessage(msg);
				}
			}

			TileEntity te = worldIn.getTileEntity(pos);
			if (te instanceof TileEntityArmillary) {
				((TileEntityArmillary) te).onRightClick(playerIn, hauntingProgress);
			}
		}
		return true;
	}
	
	private String getMoonPhaseName(int phase) {
		switch (phase) {
			case 0: return "Full Moon";
			case 1: return "Waning Gibbous Moon";
			case 2: return "Third Quarter Moon";
			case 3: return "Waning Crescent Moon";
			case 4: return "New Moon";
			case 5: return "Waxing Crescent Moon";
			case 6: return "First Quarter Moon";
			case 7: return "Waxing Gibbous Moon";
			default: return "Unknown";
		}
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		boolean isSnowy = worldIn.getBiome(pos).isSnowyBiome();
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
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
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int i = state.getValue(FACING).getHorizontalIndex();
		return i;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

}
