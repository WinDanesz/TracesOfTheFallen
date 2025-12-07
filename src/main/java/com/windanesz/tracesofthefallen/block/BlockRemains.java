package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import com.windanesz.tracesofthefallen.init.ModPotions;
import com.windanesz.tracesofthefallen.capability.HauntingCapability;
import net.minecraft.advancements.Advancement;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockRemains extends BlockTOFT {
	public BlockRemains(Material materialIn) {
		super(materialIn);
	}

	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
		super.onBlockHarvested(worldIn, pos, state, player);
		if (player instanceof EntityPlayerMP) {
			EntityPlayerMP playerMP = (EntityPlayerMP) player;
			// Grant advancement
			Advancement advancement = playerMP.getServer().getAdvancementManager().getAdvancement(new ResourceLocation(TracesOfTheFallen.MODID, "loot_skeleton_crate"));
			if (advancement != null) {
				if (!playerMP.getAdvancements().getProgress(advancement).isDone()) {
					playerMP.getAdvancements().grantCriterion(advancement, "loot_skeleton_crate");
				}
			}
			// Add haunting for breaking remains
			if (!worldIn.isRemote) {
				HauntingCapability haunting = HauntingCapability.get(playerMP);
				if (haunting != null) {
					double toAdd = Settings.miscSettings.hauntingGainedByBreakingRemains;
					haunting.addHauntingProgress((int) toAdd);
				}
			}
		}
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityRemains();
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack heldItemStack = playerIn.getHeldItem(hand);

		if (!heldItemStack.isEmpty() && heldItemStack.getItem() instanceof ItemSpade) {
			TileEntity tileEntity = worldIn.getTileEntity(pos);

			if (tileEntity instanceof TileEntityRemains) {
				playerIn.swingArm(hand);

				if (worldIn.isRemote) {
					IBlockState blockUnder = worldIn.getBlockState(pos.down());
					if (!blockUnder.getBlock().isAir(blockUnder, worldIn, pos.down())) {
						for (int i = 0; i < 10; ++i) {
							double d0 = worldIn.rand.nextGaussian() * 0.1D;
							double d1 = 0.1D + worldIn.rand.nextFloat() * 0.1D;
							double d2 = worldIn.rand.nextGaussian() * 0.1D;
							worldIn.spawnParticle(EnumParticleTypes.BLOCK_DUST, pos.getX() + worldIn.rand.nextFloat(), pos.getY() + 0.5D, pos.getZ() + worldIn.rand.nextFloat(), d0, d1, d2, Block.getStateId(blockUnder));
						}
					}
				}

				if (!worldIn.isRemote) {
					TileEntityRemains remainsTile = (TileEntityRemains) tileEntity;
					remainsTile.incrementShovelClicks();

					if (remainsTile.getShovelClicks() < 10) {
						worldIn.playSound(null, pos, SoundEvents.BLOCK_GRAVEL_HIT, SoundCategory.BLOCKS, 0.5F, 1.5F);
						heldItemStack.damageItem(1, playerIn);
					} else {
						// Turn into grave marker
						IBlockState graveState = ModBlocks.grave_marker.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, 0, playerIn, hand);
						worldIn.setBlockState(pos, graveState, 3);
						worldIn.playSound(null, pos, SoundEvents.BLOCK_GRAVEL_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
						heldItemStack.damageItem(1, playerIn);
						playerIn.addPotionEffect(new PotionEffect(ModPotions.bliss, (int) Settings.miscSettings.blissDurationForBurying));

						if (playerIn instanceof EntityPlayerMP) {
							EntityPlayerMP player = (EntityPlayerMP) playerIn;
							HauntingCapability haunting = HauntingCapability.get(player);
							if (haunting != null) {
								int toReduce = Settings.miscSettings.hauntingReducedByBuryingRemains;
								haunting.reduceHauntingProgress(toReduce);
							}
							// Advancement granting for bury_remains has been removed.
						}
					}
				}
				return true;
			}
		}

		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
	}
}
