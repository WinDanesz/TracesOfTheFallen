package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.block.BlockDecoration;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.List;

public class EntityTelescopeSeat extends Entity {

	private static final DataParameter<BlockPos> ANCHOR_POS = EntityDataManager.createKey(EntityTelescopeSeat.class, DataSerializers.BLOCK_POS);
	private static final double VIEW_CENTER_X = 0.5D;
	private static final double VIEW_CENTER_Z = 0.5D;
	private static final double SEAT_BACK_OFFSET = 0.48D;
	private static final double SEAT_Y_OFFSET = 0.0D;
	private static final double SUN_GLARE_DOT_THRESHOLD = 0.99D;
	private static final int SUN_BLINDNESS_DURATION = 60;
	private static final double SUN_TRACE_DISTANCE = 256.0D;
	private static final double SUN_TRACE_START_OFFSET = 0.2D;

	private BlockPos anchorPos = BlockPos.ORIGIN;

	public EntityTelescopeSeat(World worldIn) {
		super(worldIn);
		this.noClip = true;
		this.setSize(0.01F, 0.01F);
	}

	public EntityTelescopeSeat(World worldIn, BlockPos anchorPos, IBlockState state) {
		this(worldIn);
		this.setAnchorPos(anchorPos);
		Vec3d seatPos = getSeatPosition(state, anchorPos);
		this.setPosition(seatPos.x, seatPos.y, seatPos.z);
	}

	public static EntityTelescopeSeat getOrCreate(World world, BlockPos pos, IBlockState state) {
		AxisAlignedBB searchBox = new AxisAlignedBB(pos).grow(0.6D);
		List<EntityTelescopeSeat> seats = world.getEntitiesWithinAABB(EntityTelescopeSeat.class, searchBox,
				seat -> seat != null && !seat.isDead && pos.equals(seat.anchorPos));

		if (!seats.isEmpty()) {
			return seats.get(0);
		}

		EntityTelescopeSeat seat = new EntityTelescopeSeat(world, pos, state);
		world.spawnEntity(seat);
		return seat;
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(ANCHOR_POS, BlockPos.ORIGIN);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		this.anchorPos = this.dataManager.get(ANCHOR_POS);

		IBlockState state = this.world.getBlockState(this.anchorPos);
		if (state.getBlock() != ModBlocks.telescope) {
			if (!this.world.isRemote) {
				this.setDead();
			}
			return;
		}

		Vec3d seatPos = getSeatPosition(state, this.anchorPos);
		this.motionX = 0.0D;
		this.motionY = 0.0D;
		this.motionZ = 0.0D;
		this.setPosition(seatPos.x, seatPos.y, seatPos.z);
		this.updateSunBlindness();

		if (!this.world.isRemote && this.getPassengers().isEmpty()) {
			this.setDead();
		}
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		this.setAnchorPos(new BlockPos(compound.getInteger("AnchorX"), compound.getInteger("AnchorY"), compound.getInteger("AnchorZ")));
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		compound.setInteger("AnchorX", this.anchorPos.getX());
		compound.setInteger("AnchorY", this.anchorPos.getY());
		compound.setInteger("AnchorZ", this.anchorPos.getZ());
	}

	@Override
	protected boolean canFitPassenger(Entity passenger) {
		return this.getPassengers().isEmpty();
	}

	@Override
	public void updatePassenger(Entity passenger) {
		if (this.isPassenger(passenger)) {
			Vec3d passengerPos = getPassengerPosition(this.world.getBlockState(this.anchorPos), this.anchorPos);
			passenger.setPosition(passengerPos.x, passengerPos.y, passengerPos.z);
		}
	}

	@Override
	public double getMountedYOffset() {
		return 0.0D;
	}

	@Override
	public boolean shouldRiderSit() {
		return false;
	}

	@Override
	public boolean canBeCollidedWith() {
		return false;
	}

	@Override
	public boolean canBePushed() {
		return false;
	}

	@Override
	public void applyEntityCollision(Entity entityIn) {}

	@Override
	public boolean isInvisible() {
		return true;
	}

	@Override
	public boolean isInvisibleToPlayer(EntityPlayer player) {
		return true;
	}

	public BlockPos getAnchorPos() {
		return this.anchorPos;
	}

	private void updateSunBlindness() {
		if (this.world.isRemote || this.getPassengers().isEmpty() || !(this.getPassengers().get(0) instanceof EntityPlayer)) {
			return;
		}

		EntityPlayer player = (EntityPlayer) this.getPassengers().get(0);
		if ((player.ticksExisted + this.getEntityId()) % 5 != 0) {
			return;
		}

		if (!this.isLookingIntoSun(player)) {
			return;
		}

		PotionEffect currentEffect = player.getActivePotionEffect(MobEffects.BLINDNESS);
		if (currentEffect == null || currentEffect.getDuration() < SUN_BLINDNESS_DURATION - 2) {
			player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, SUN_BLINDNESS_DURATION, 0, false, false));
		}
	}

	private boolean isLookingIntoSun(EntityPlayer player) {
		if (!this.world.isDaytime()) {
			return false;
		}

		Vec3d sunDirection = this.getSunDirection();
		if (sunDirection.y <= 0.0D) {
			return false;
		}

		Vec3d look = player.getLook(1.0F).normalize();
		if (look.dotProduct(sunDirection) < SUN_GLARE_DOT_THRESHOLD) {
			return false;
		}

		Vec3d viewStart = player.getPositionEyes(1.0F).add(look.scale(SUN_TRACE_START_OFFSET));
		BlockPos viewBlockPos = new BlockPos(viewStart);
		if (!this.world.canSeeSky(viewBlockPos)) {
			return false;
		}

		Vec3d sunTraceEnd = viewStart.add(sunDirection.scale(SUN_TRACE_DISTANCE));
		return this.rayTraceLongDistance(viewStart, sunTraceEnd) == null;
	}

	private Vec3d getSunDirection() {
		float celestialAngle = this.world.getCelestialAngleRadians(1.0F);
		double x = -MathHelper.sin(celestialAngle);
		double y = MathHelper.cos(celestialAngle);
		return new Vec3d(x, y, 0.0D).normalize();
	}

	private RayTraceResult rayTraceLongDistance(Vec3d start, Vec3d end) {
		if (Double.isNaN(start.x) || Double.isNaN(start.y) || Double.isNaN(start.z)
				|| Double.isNaN(end.x) || Double.isNaN(end.y) || Double.isNaN(end.z)) {
			return null;
		}

		int x = MathHelper.floor(start.x);
		int y = MathHelper.floor(start.y);
		int z = MathHelper.floor(start.z);
		int endX = MathHelper.floor(end.x);
		int endY = MathHelper.floor(end.y);
		int endZ = MathHelper.floor(end.z);

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);
		RayTraceResult hit = this.collisionRayTrace(pos, start, end);
		if (hit != null) {
			return hit;
		}

		int steps = (int) (SUN_TRACE_DISTANCE * 3.0D);
		Vec3d current = start;

		while (steps-- >= 0) {
			if (x == endX && y == endY && z == endZ) {
				return null;
			}

			double nextBoundaryX = endX > x ? x + 1.0D : x;
			double nextBoundaryY = endY > y ? y + 1.0D : y;
			double nextBoundaryZ = endZ > z ? z + 1.0D : z;
			double percentX = endX == x ? Double.POSITIVE_INFINITY : (nextBoundaryX - current.x) / (end.x - current.x);
			double percentY = endY == y ? Double.POSITIVE_INFINITY : (nextBoundaryY - current.y) / (end.y - current.y);
			double percentZ = endZ == z ? Double.POSITIVE_INFINITY : (nextBoundaryZ - current.z) / (end.z - current.z);

			if (percentX == -0.0D) {
				percentX = -1.0E-4D;
			}
			if (percentY == -0.0D) {
				percentY = -1.0E-4D;
			}
			if (percentZ == -0.0D) {
				percentZ = -1.0E-4D;
			}

			EnumFacing sideHit;
			if (percentX < percentY && percentX < percentZ) {
				sideHit = endX > x ? EnumFacing.WEST : EnumFacing.EAST;
				current = new Vec3d(nextBoundaryX, current.y + (end.y - current.y) * percentX, current.z + (end.z - current.z) * percentX);
			} else if (percentY < percentZ) {
				sideHit = endY > y ? EnumFacing.DOWN : EnumFacing.UP;
				current = new Vec3d(current.x + (end.x - current.x) * percentY, nextBoundaryY, current.z + (end.z - current.z) * percentY);
			} else {
				sideHit = endZ > z ? EnumFacing.NORTH : EnumFacing.SOUTH;
				current = new Vec3d(current.x + (end.x - current.x) * percentZ, current.y + (end.y - current.y) * percentZ, nextBoundaryZ);
			}

			x = MathHelper.floor(current.x) - (sideHit == EnumFacing.EAST ? 1 : 0);
			y = MathHelper.floor(current.y) - (sideHit == EnumFacing.UP ? 1 : 0);
			z = MathHelper.floor(current.z) - (sideHit == EnumFacing.SOUTH ? 1 : 0);
			pos.setPos(x, y, z);

			hit = this.collisionRayTrace(pos, start, end);
			if (hit != null) {
				return hit;
			}
		}

		return null;
	}

	private RayTraceResult collisionRayTrace(BlockPos pos, Vec3d start, Vec3d end) {
		IBlockState state = this.world.getBlockState(pos);
		Block block = state.getBlock();

		if (!block.canCollideCheck(state, false) || state.getCollisionBoundingBox(this.world, pos) == Block.NULL_AABB) {
			return null;
		}

		return state.collisionRayTrace(this.world, pos, start, end);
	}

	private void setAnchorPos(BlockPos anchorPos) {
		this.anchorPos = anchorPos.toImmutable();
		this.dataManager.set(ANCHOR_POS, this.anchorPos);
	}

	private static Vec3d getSeatPosition(IBlockState state, BlockPos anchorPos) {
		return getPassengerPosition(state, anchorPos);
	}

	private static Vec3d getPassengerPosition(IBlockState state, BlockPos anchorPos) {
		Vec3d back = new Vec3d(state.getValue(BlockDecoration.FACING).getOpposite().getXOffset(), 0.0D, state.getValue(BlockDecoration.FACING).getOpposite().getZOffset());
		double x = anchorPos.getX() + VIEW_CENTER_X + back.x * SEAT_BACK_OFFSET;
		double y = anchorPos.getY() + SEAT_Y_OFFSET;
		double z = anchorPos.getZ() + VIEW_CENTER_Z + back.z * SEAT_BACK_OFFSET;
		return new Vec3d(x, y, z);
	}
}
