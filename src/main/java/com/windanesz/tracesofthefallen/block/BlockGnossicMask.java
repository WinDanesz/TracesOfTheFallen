package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.client.ClientProxy;
import com.windanesz.tracesofthefallen.entity.EntityGnossic;
import com.windanesz.tracesofthefallen.init.ModCreativeTab;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * A mask that lies on the ground or hangs on a wall. When a (non-creative) player comes within
 * the configured trigger radius ({@code gnossicMaskTriggerRadius}) in blocks it floats up and turns into a {@link EntityGnossic}; the animation itself is
 * driven by the entity (see {@link EntityGnossic#beginMaterializing}).
 * <p>
 * Blockstate: {@code floor=true} masks lie face-up with {@code facing} being the way their forehead points (the
 * direction the placer was looking), {@code floor=false} masks are mounted flush to a wall with {@code facing}
 * being the direction their face points, away from the wall.
 */
public class BlockGnossicMask extends Block {

	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public static final PropertyBool FLOOR = PropertyBool.create("floor");

	private static final int CHECK_INTERVAL = 10;
	/** How many blocks below a wall mask the Gnossic may look for ground to stand on. */
	private static final int MAX_GROUND_SEARCH = 6;

	private static final AxisAlignedBB FLOOR_AABB_Z = new AxisAlignedBB(5 / 16D, 0.0D, 3 / 16D, 11 / 16D, 2 / 16D, 13 / 16D);
	private static final AxisAlignedBB FLOOR_AABB_X = new AxisAlignedBB(3 / 16D, 0.0D, 5 / 16D, 13 / 16D, 2 / 16D, 11 / 16D);
	private static final AxisAlignedBB WALL_AABB_NORTH = new AxisAlignedBB(5 / 16D, 3 / 16D, 14 / 16D, 11 / 16D, 13 / 16D, 1.0D);
	private static final AxisAlignedBB WALL_AABB_SOUTH = new AxisAlignedBB(5 / 16D, 3 / 16D, 0.0D, 11 / 16D, 13 / 16D, 2 / 16D);
	private static final AxisAlignedBB WALL_AABB_EAST = new AxisAlignedBB(0.0D, 3 / 16D, 5 / 16D, 2 / 16D, 13 / 16D, 11 / 16D);
	private static final AxisAlignedBB WALL_AABB_WEST = new AxisAlignedBB(14 / 16D, 3 / 16D, 5 / 16D, 1.0D, 13 / 16D, 11 / 16D);

	public BlockGnossicMask() {
		super(Material.WOOD);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(FLOOR, false));
		setBlockUnbreakable(); // Can't be broken in survival; creative players can still remove it
		setCreativeTab(ModCreativeTab.TOTF_TAB);
		// Safety net: a random tick re-arms the proximity check should the scheduled tick ever get lost
		setTickRandomly(true);
	}

	/**
	 * Centre of a mask in the world.
	 *
	 * @param mountFacing {@link EnumFacing#UP} for a mask lying on the floor, otherwise the direction a wall mask faces
	 */
	public static Vec3d getMaskCenter(BlockPos pos, EnumFacing mountFacing) {
		if (mountFacing.getAxis() == EnumFacing.Axis.Y) {
			return new Vec3d(pos.getX() + 0.5D, pos.getY() + 1 / 16D, pos.getZ() + 0.5D);
		}
		// Wall masks sit against the support block behind them, i.e. opposite to the way they face
		return new Vec3d(
				pos.getX() + 0.5D - mountFacing.getXOffset() * (0.5D - 1 / 16D),
				pos.getY() + 0.5D,
				pos.getZ() + 0.5D - mountFacing.getZOffset() * (0.5D - 1 / 16D));
	}

	/** The facing a mask would be mounted with: UP when lying on the floor, the way it faces otherwise. */
	private static EnumFacing getMountFacing(IBlockState state) {
		return state.getValue(FLOOR) ? EnumFacing.UP : state.getValue(FACING);
	}

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		super.onBlockAdded(worldIn, pos, state);
		if (!worldIn.isRemote) {
			worldIn.scheduleUpdate(pos, this, CHECK_INTERVAL);
		}
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		if (worldIn.isRemote) {
			return;
		}
		if (worldIn.getDifficulty() != EnumDifficulty.PEACEFUL) {
			Vec3d center = getMaskCenter(pos, getMountFacing(state));
			// Like the tent goblins: ignores creative/spectator players, so builders can work around masks safely
			EntityPlayer player = worldIn.getClosestPlayer(center.x, center.y, center.z, Settings.mobSettings.gnossicMaskTriggerRadius, false);
			if (player != null && !player.isCreative() && !player.isSpectator()) {
				awaken(worldIn, pos, state);
				return;
			}
		}
		worldIn.scheduleUpdate(pos, this, CHECK_INTERVAL);
	}

	/** Removes the mask and spawns the Gnossic that rises from it. */
	private static void awaken(World world, BlockPos pos, IBlockState state) {
		boolean floor = state.getValue(FLOOR);
		EnumFacing facing = state.getValue(FACING);
		// The Gnossic faces where the mask's face points. For a mask lying on the floor that is up, and the mask
		// tips forwards as it rises, so the mask's forehead (= facing) ends up behind the Gnossic.
		EnumFacing front = floor ? facing.getOpposite() : facing;
		float yaw = front.getHorizontalAngle();

		EntityGnossic gnossic = new EntityGnossic(world);
		gnossic.rotationYaw = yaw;
		gnossic.prevRotationYaw = yaw;
		gnossic.rotationYawHead = yaw;
		gnossic.prevRotationYawHead = yaw;
		gnossic.renderYawOffset = yaw;
		gnossic.prevRenderYawOffset = yaw;

		Vec3d spawn = findSpawnPos(world, pos, front, floor, gnossic);
		gnossic.setLocationAndAngles(spawn.x, spawn.y, spawn.z, yaw, 0.0F);
		gnossic.rotationYawHead = yaw;
		gnossic.renderYawOffset = yaw;
		gnossic.prevRenderYawOffset = yaw;
		gnossic.beginMaterializing(pos, floor ? EnumFacing.UP : facing);

		world.setBlockToAir(pos);
		world.spawnEntity(gnossic);
	}

	/**
	 * Picks where the Gnossic's feet go. A mask on the floor simply uses its own spot; a wall mask may hang well
	 * above the floor, so the Gnossic stands on the ground below it, preferably in the mask's own column and
	 * otherwise out in front of the wall, wherever there is room.
	 */
	private static Vec3d findSpawnPos(World world, BlockPos pos, EnumFacing front, boolean floor, EntityGnossic gnossic) {
		if (!floor) {
			for (int out = 0; out <= 2; out++) {
				BlockPos column = pos.offset(front, out);
				gnossic.setPosition(column.getX() + 0.5D, groundLevel(world, column), column.getZ() + 0.5D);
				if (world.getCollisionBoxes(gnossic, gnossic.getEntityBoundingBox()).isEmpty()) {
					return new Vec3d(gnossic.posX, gnossic.posY, gnossic.posZ);
				}
			}
		}
		return new Vec3d(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
	}

	/** Y of the first solid surface at or below the given block, searching a few blocks down. */
	private static double groundLevel(World world, BlockPos from) {
		BlockPos cursor = from;
		for (int i = 0; i < MAX_GROUND_SEARCH; i++) {
			BlockPos below = cursor.down();
			if (world.getBlockState(below).isSideSolid(world, below, EnumFacing.UP)) {
				break;
			}
			cursor = below;
		}
		return cursor.getY();
	}

	@Override
	public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
		if (side == EnumFacing.DOWN || !super.canPlaceBlockOnSide(worldIn, pos, side)) {
			return false;
		}
		BlockPos supportPos = pos.offset(side.getOpposite());
		return worldIn.getBlockState(supportPos).isSideSolid(worldIn, supportPos, side);
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		if (facing.getAxis() == EnumFacing.Axis.Y) {
			// Forehead towards where the placer looks, so the face reads the right way up from where they stand
			return this.getDefaultState().withProperty(FLOOR, true).withProperty(FACING, placer.getHorizontalFacing());
		}
		return this.getDefaultState().withProperty(FLOOR, false).withProperty(FACING, facing);
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		EnumFacing side = getMountFacing(state);
		BlockPos supportPos = pos.offset(side.getOpposite());
		if (!worldIn.getBlockState(supportPos).isSideSolid(worldIn, supportPos, side)) {
			this.dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);
		}
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		EnumFacing facing = state.getValue(FACING);
		if (state.getValue(FLOOR)) {
			return facing.getAxis() == EnumFacing.Axis.X ? FLOOR_AABB_X : FLOOR_AABB_Z;
		}
		switch (facing) {
			case SOUTH:
				return WALL_AABB_SOUTH;
			case EAST:
				return WALL_AABB_EAST;
			case WEST:
				return WALL_AABB_WEST;
			case NORTH:
			default:
				return WALL_AABB_NORTH;
		}
	}

	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return NULL_AABB;
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
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		ClientProxy.addMultiLineDescription(tooltip, TextFormatting.GRAY + I18n.format("tile.totf:gnossic_mask.desc"));
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
		return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getHorizontalIndex() | (state.getValue(FLOOR) ? 4 : 0);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState()
				.withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3))
				.withProperty(FLOOR, (meta & 4) != 0);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, FLOOR);
	}
}
