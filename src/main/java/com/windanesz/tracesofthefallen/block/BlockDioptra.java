package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.entity.EntityDioptraSeat;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockDioptra extends BlockDecoration {

	public BlockDioptra(Material material) {
		super(material);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityDioptra();
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {

		if (hand != EnumHand.MAIN_HAND) {
			return true;
		}

		if (!isPlayerBehind(state, pos, playerIn)) {
			if (!worldIn.isRemote) {
				playerIn.sendStatusMessage(new TextComponentTranslation("totf:dioptra.stand_behind"), true);
			}
			return true;
		}

		if (worldIn.isRemote) {
			return true;
		}

		EntityDioptraSeat seat = EntityDioptraSeat.getOrCreate(worldIn, pos, state);
		playerIn.startRiding(seat, true);
		return true;
	}

	private boolean isPlayerBehind(IBlockState state, BlockPos pos, EntityPlayer player) {
		EnumFacing back = state.getValue(FACING).getOpposite();
		Vec3d blockCenter = new Vec3d(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
		Vec3d playerOffset = new Vec3d(player.posX - blockCenter.x, 0.0D, player.posZ - blockCenter.z);
		double directionalDistance = playerOffset.dotProduct(new Vec3d(back.getXOffset(), 0.0D, back.getZOffset()));
		return directionalDistance > 0.2D;
	}
}
