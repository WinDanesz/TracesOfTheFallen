package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.IncenseEffects;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;

public class BlockIncenseBurner extends BlockDecoration {

	private static final int BURNING_LIGHT_LEVEL = 2;

	private final MultiblockAABBHelper aabbHelper;
	// When false the blockstate carries no y-rotation, so AABBs must never be rotated.
	private final boolean rotating;

	public BlockIncenseBurner(Material material, List<AxisAlignedBB> masterAABBs, boolean rotating) {
		super(material);
		this.rotating = rotating;
		this.aabbHelper = new MultiblockAABBHelper(masterAABBs, -1, 1, 0, 1, -1, 1);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntityIncenseBurner incenseBurner = getIncenseBurner(world, pos);
		return incenseBurner != null && incenseBurner.isActivelyBurning() ? BURNING_LIGHT_LEVEL : 0;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityIncenseBurner();
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		EnumFacing facing = rotating ? state.getValue(FACING) : EnumFacing.NORTH;
		return aabbHelper.get(facing, pos, pos);
	}

	@Override
	public AxisAlignedBB getProxyCellAABB(IBlockState state, IBlockAccess world, BlockPos mainPos, BlockPos proxyPos) {
		EnumFacing facing = rotating ? state.getValue(FACING) : EnumFacing.NORTH;
		return aabbHelper.get(facing, mainPos, proxyPos);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		TileEntityIncenseBurner incenseBurner = getIncenseBurner(world, pos);
		if (incenseBurner == null || !incenseBurner.isLit()) {
			return;
		}

		if (rand.nextInt(3) == 0) {
			return;
		}

		int particleCount = 2 + rand.nextInt(2);
		double spreadRadius = incenseBurner.getAuraRadius();
		double spreadDiameter = spreadRadius * 2.0D;

		for (int i = 0; i < particleCount; i++) {
			double x = pos.getX() + 0.5D + (rand.nextDouble() - 0.5D) * spreadDiameter;
			double y = pos.getY() + 0.02D + rand.nextDouble() * 0.04D;
			double z = pos.getZ() + 0.5D + (rand.nextDouble() - 0.5D) * spreadDiameter;
			double motionX = (rand.nextDouble() - 0.5D) * 0.0006D;
			double motionY = rand.nextDouble() * 0.00015D;
			double motionZ = (rand.nextDouble() - 0.5D) * 0.0006D;

			TracesOfTheFallen.proxy.spawnIncenseFloorMistParticle(world, x, y, z, motionX, motionY, motionZ, incenseBurner.getAuraParticleColor());
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {

		TileEntityIncenseBurner incenseBurner = getIncenseBurner(worldIn, pos);
		if (incenseBurner == null) {
			return false;
		}

		ItemStack heldItem = playerIn.getHeldItem(hand);
		if (heldItem.isEmpty()) {
			if (!worldIn.isRemote) {
				sendRemainingBurnStatus(playerIn, incenseBurner);
			}
			return true;
		}

		if (incenseBurner.canApplyPotion(heldItem)) {
			if (worldIn.isRemote) {
				return true;
			}

			if (!incenseBurner.applyPotionEffects(heldItem)) {
				return false;
			}

			consumePotionItem(playerIn, hand, heldItem);
			return true;
		}

		if (incenseBurner.canAcceptFuel(heldItem)) {
			if (worldIn.isRemote) {
				return true;
			}

			if (!incenseBurner.addFuel(heldItem)) {
				return false;
			}

			if (!playerIn.capabilities.isCreativeMode) {
				heldItem.shrink(1);
			}
			return true;
		}

		if (!IncenseEffects.isFireStarter(heldItem) || !incenseBurner.canIgnite()) {
			return false;
		}

		if (worldIn.isRemote) {
			return true;
		}

		if (!incenseBurner.tryLight()) {
			return false;
		}

		worldIn.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F,
				worldIn.rand.nextFloat() * 0.4F + 0.8F);

		damageFireStarter(playerIn, heldItem);
		return true;
	}

	private int[] getProxyBounds(IBlockState state) {
		return rotating ? new int[]{-1, 1, -1, 1} : new int[]{0, 0, 0, 0};
	}

	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		if (!hasSupport(worldIn, pos) || !super.canPlaceBlockAt(worldIn, pos)) {
			return false;
		}
		int[] b = getProxyBounds(this.getDefaultState()); // Facing is not yet known before placement, but usually block can't rotate before placed, so we check default bounds (0,1,0,1)
		for (int x = b[0]; x <= b[1]; x++) {
			for (int y = 0; y <= 1; y++) {
				for (int z = b[2]; z <= b[3]; z++) {
					if (x == 0 && y == 0 && z == 0) continue;
					BlockPos checkPos = pos.add(x, y, z);
					IBlockState checkState = worldIn.getBlockState(checkPos);
					if (!checkState.getBlock().isReplaceable(worldIn, checkPos) && !worldIn.isAirBlock(checkPos)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		super.onBlockAdded(worldIn, pos, state);
		if (!worldIn.isRemote) {
			int[] b = getProxyBounds(state);
			for (int x = b[0]; x <= b[1]; x++) {
				for (int y = 0; y <= 1; y++) {
					for (int z = b[2]; z <= b[3]; z++) {
						if (x == 0 && y == 0 && z == 0) continue;
						BlockPos proxyPos = pos.add(x, y, z);
						if (worldIn.getBlockState(proxyPos).getBlock().isReplaceable(worldIn, proxyPos) || worldIn.isAirBlock(proxyPos)) {
							worldIn.setBlockState(proxyPos, com.windanesz.tracesofthefallen.init.ModBlocks.technical_block.getDefaultState(), 3);
						}
					}
				}
			}
		}
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		int[] b = getProxyBounds(state);
		for (int x = b[0]; x <= b[1]; x++) {
			for (int y = 0; y <= 1; y++) {
				for (int z = b[2]; z <= b[3]; z++) {
					if (x == 0 && y == 0 && z == 0) continue;
					BlockPos proxyPos = pos.add(x, y, z);
					if (worldIn.getBlockState(proxyPos).getBlock() == com.windanesz.tracesofthefallen.init.ModBlocks.technical_block) {
						worldIn.setBlockToAir(proxyPos);
					}
				}
			}
		}
		super.breakBlock(worldIn, pos, state);
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, net.minecraft.block.Block blockIn, BlockPos fromPos) {
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos);

		if (!worldIn.isRemote) {
			if (!hasSupport(worldIn, pos)) {
				dropBlockAsItem(worldIn, pos, state, 0);
				worldIn.setBlockToAir(pos);
				return;
			}
			int[] b = getProxyBounds(state);
			for (int x = b[0]; x <= b[1]; x++) {
				for (int y = 0; y <= 1; y++) {
					for (int z = b[2]; z <= b[3]; z++) {
						if (x == 0 && y == 0 && z == 0) continue;
						BlockPos proxyPos = pos.add(x, y, z);
						if (worldIn.getBlockState(proxyPos).getBlock() != com.windanesz.tracesofthefallen.init.ModBlocks.technical_block) {
							if (worldIn.getBlockState(proxyPos).getBlock().isReplaceable(worldIn, proxyPos) || worldIn.isAirBlock(proxyPos)) {
								worldIn.setBlockState(proxyPos, com.windanesz.tracesofthefallen.init.ModBlocks.technical_block.getDefaultState(), 3);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public boolean isMainBlockForProxy(net.minecraft.world.IBlockAccess world, BlockPos mainPos, BlockPos proxyPos) {
		IBlockState state = world.getBlockState(mainPos);
		if (state.getBlock() != this) return false;
		int[] b = getProxyBounds(state);
		int dx = proxyPos.getX() - mainPos.getX();
		int dy = proxyPos.getY() - mainPos.getY();
		int dz = proxyPos.getZ() - mainPos.getZ();
		return dx >= b[0] && dx <= b[1] && dy >= 0 && dy <= 1 && dz >= b[2] && dz <= b[3];
	}

	private boolean hasSupport(World world, BlockPos pos) {
		BlockPos belowPos = pos.down();
		IBlockState belowState = world.getBlockState(belowPos);
		if (belowState.isSideSolid(world, belowPos, EnumFacing.UP)) {
			return true;
		}

		BlockPos abovePos = pos.up();
		IBlockState aboveState = world.getBlockState(abovePos);
		return aboveState.isSideSolid(world, abovePos, EnumFacing.DOWN);
	}

	private void sendRemainingBurnStatus(EntityPlayer player, TileEntityIncenseBurner incenseBurner) {
		int remainingTicks = Math.max(0, incenseBurner.getRemainingBurnTime());
		int potionTicks = Math.max(0, incenseBurner.getRemainingPotionDurationTicks());

		if (!incenseBurner.isLit() && remainingTicks == 0 && potionTicks == 0 && !incenseBurner.isBurnedOut()) {
			player.sendStatusMessage(new TextComponentTranslation("totf:incense_burner.empty"), true);
			return;
		}

		ITextComponent fuelComponent = getDurationValueComponent(remainingTicks);

		if (potionTicks <= 0) {
			if (remainingTicks < 1200) {
				player.sendStatusMessage(new TextComponentTranslation("totf:incense_burner.duration_nearly_spent"), true);
			} else {
				int minutes = Math.max(1, Math.round(remainingTicks / 1200.0F));
				player.sendStatusMessage(new TextComponentTranslation("totf:incense_burner.duration_minutes", minutes), true);
			}
			return;
		}

		ITextComponent potionComponent = getDurationValueComponent(potionTicks);
		player.sendStatusMessage(new TextComponentTranslation("totf:incense_burner.duration_combined", fuelComponent, potionComponent), true);
	}

	private ITextComponent getDurationValueComponent(int ticks) {
		if (ticks < 1200) {
			return new TextComponentTranslation("totf:incense_burner.duration_value_nearly_spent");
		}
		int minutes = Math.max(1, Math.round(ticks / 1200.0F));
		return new TextComponentTranslation("totf:incense_burner.duration_value_minutes", minutes);
	}

	private TileEntityIncenseBurner getIncenseBurner(IBlockAccess world, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);
		return tileEntity instanceof TileEntityIncenseBurner ? (TileEntityIncenseBurner) tileEntity : null;
	}

	private void consumePotionItem(EntityPlayer player, EnumHand hand, ItemStack stack) {
		if (player.capabilities.isCreativeMode) {
			return;
		}

		if (stack.getItem() == Items.POTIONITEM) {
			stack.shrink(1);
			if (stack.isEmpty()) {
				player.setHeldItem(hand, new ItemStack(Items.GLASS_BOTTLE));
			} else if (!player.inventory.addItemStackToInventory(new ItemStack(Items.GLASS_BOTTLE))) {
				player.dropItem(new ItemStack(Items.GLASS_BOTTLE), false);
			}
			return;
		}

		stack.shrink(1);
	}

	private void damageFireStarter(EntityPlayer player, ItemStack stack) {
		if (player.capabilities.isCreativeMode) {
			return;
		}

		if (stack.getItem() == Items.FIRE_CHARGE) {
			stack.shrink(1);
		} else if (stack.isItemStackDamageable()) {
			stack.damageItem(1, player);
		} else {
			stack.shrink(1);
		}
	}
}
