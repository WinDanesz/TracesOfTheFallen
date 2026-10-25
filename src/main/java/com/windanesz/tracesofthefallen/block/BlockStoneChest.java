package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.tile.TileEntityStoneChest;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class BlockStoneChest extends BlockContainer {

	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public static final PropertyBool OPEN = PropertyBool.create("open");
	protected AxisAlignedBB boundingBoxClosed = new AxisAlignedBB(0.0D, 0.0D, 0.25D, 1.0D, 0.7D, 0.75D);
	protected AxisAlignedBB boundingBoxOpen = new AxisAlignedBB(0.0D, 0.0D, 0.25D, 1.0D, 0.5625D, 0.75D);

	public BlockStoneChest() {
		super(Material.ROCK);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(OPEN, false));
		this.setHardness(2.5F);
		this.setResistance(10.0F);
		this.setSoundType(SoundType.STONE);
	}

	public BlockStoneChest setBoundingBox(AxisAlignedBB boundingBox) {
		this.boundingBoxClosed = boundingBox;
		return this;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return rotateBoundingBox(state.getValue(OPEN) ? this.boundingBoxOpen : this.boundingBoxClosed, state.getValue(FACING));
	}

	private AxisAlignedBB rotateBoundingBox(AxisAlignedBB box, EnumFacing facing) {
		switch (facing) {
			case EAST:
				return new AxisAlignedBB(1.0D - box.maxZ, box.minY, box.minX, 1.0D - box.minZ, box.maxY, box.maxX);
			case SOUTH:
				return new AxisAlignedBB(1.0D - box.maxX, box.minY, 1.0D - box.maxZ, 1.0D - box.minX, box.maxY, 1.0D - box.minZ);
			case WEST:
				return new AxisAlignedBB(box.minZ, box.minY, 1.0D - box.maxX, box.maxZ, box.maxY, 1.0D - box.minX);
			case NORTH:
			default:
				return box;
		}
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
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof TileEntityStoneChest) {
			if (placer instanceof EntityPlayer && ((EntityPlayer) placer).isCreative() && placer.isSneaking()) {
				((TileEntityStoneChest) te).setLidOpen(true);
			}
		}
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getHorizontalIndex() | (state.getValue(OPEN) ? 4 : 0);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing facing = EnumFacing.byHorizontalIndex(meta & 3);
		boolean open = (meta & 4) != 0;
		return this.getDefaultState().withProperty(FACING, facing).withProperty(OPEN, open);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, OPEN);
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityStoneChest();
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) {
			return true;
		}
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof TileEntityStoneChest) {
			TileEntityStoneChest chest = (TileEntityStoneChest) te;
			if (playerIn.isSneaking()) {
				chest.setLidOpen(!chest.isLidOpen());
			} else {
				if (chest.isLidOpen()) {
					chest.requestOpen(playerIn);
				} else {
					playerIn.sendStatusMessage(new TextComponentString("You need to open the chest first (Sneak-right click)"), true);
				}
			}
		}
		return true;
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if (tileentity instanceof TileEntityStoneChest) {
			InventoryHelper.dropInventoryItems(worldIn, pos, (TileEntityStoneChest) tileentity);
			worldIn.updateComparatorOutputLevel(pos, this);
		}
		super.breakBlock(worldIn, pos, state);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		if (Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.isCreative()) {
			tooltip.add(TextFormatting.GRAY + "Placing the chest while sneaking keeps it open");
		}
	}
}
