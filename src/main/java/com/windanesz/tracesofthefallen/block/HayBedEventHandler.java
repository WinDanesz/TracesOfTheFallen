package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.entity.EntityGoblin;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = TracesOfTheFallen.MODID)
public final class HayBedEventHandler {

	private HayBedEventHandler() {}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.END || event.player.world.isRemote) {
			return;
		}

		EntityPlayer player = event.player;
		if (player.isPlayerSleeping() && player.getSleepTimer() == 40 && player.bedLocation != null) {
			World world = player.world;
			if (!world.isBlockLoaded(player.bedLocation)) {
				return;
			}

			BlockPos pos = player.bedLocation;
			IBlockState state = world.getBlockState(pos);
			Block block = state.getBlock();

			if (block == ModBlocks.technical_block) {
				for (EnumFacing facing : EnumFacing.HORIZONTALS) {
					BlockPos candidate = pos.offset(facing);
					if (world.getBlockState(candidate).getBlock() instanceof BlockHayBed) {
						pos = candidate;
						state = world.getBlockState(pos);
						block = state.getBlock();
						break;
					}
				}
			}

			if (block instanceof BlockHayBed) {
				BlockPos proxyPos = pos.offset(state.getValue(BlockHayBed.FACING).getOpposite());
				boolean exposedToSky = world.canSeeSky(pos.up()) || (world.isBlockLoaded(proxyPos) && world.canSeeSky(proxyPos.up()));

				if (exposedToSky) {
					player.wakeUpPlayer(true, false, false);
					player.sendMessage(new TextComponentTranslation("tile.bed.notSafeSky"));

					int monsterCount = 1 + world.rand.nextInt(2);
					for (int i = 0; i < monsterCount; i++) {
						EntityLiving monster = world.rand.nextBoolean() ? new EntityGoblin(world) : new EntityZombie(world);
						BlockPos spawnPos = null;
						for (int attempts = 0; attempts < 15; attempts++) {
							int dx = (world.rand.nextInt(5) - 2);
							int dz = (world.rand.nextInt(5) - 2);
							if (Math.abs(dx) < 2 && Math.abs(dz) < 2) continue;
							BlockPos candidate = pos.add(dx, 0, dz);
							candidate = world.getTopSolidOrLiquidBlock(candidate);
							if (world.isAirBlock(candidate) && world.isAirBlock(candidate.up()) && world.getBlockState(candidate.down()).isSideSolid(world, candidate.down(), EnumFacing.UP)) {
								spawnPos = candidate;
								break;
							}
						}
						if (spawnPos == null) {
							spawnPos = pos.up();
						}
						monster.setPosition(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);
						world.spawnEntity(monster);
						monster.setAttackTarget(player);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerSetSpawn(PlayerSetSpawnEvent event) {
		BlockPos pos = event.getNewSpawn();
		if (pos == null) return;
		EntityPlayer player = event.getEntityPlayer();
		World world = player.world;
		if (world == null || !world.isBlockLoaded(pos)) return;

		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (block instanceof BlockHayBed) {
			event.setCanceled(true);
		} else if (block == ModBlocks.technical_block) {
			for (EnumFacing facing : EnumFacing.HORIZONTALS) {
				BlockPos candidate = pos.offset(facing);
				if (world.getBlockState(candidate).getBlock() instanceof BlockHayBed) {
					event.setCanceled(true);
					break;
				}
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
		EntityPlayer player = event.getEntityPlayer();
		World world = player.world;
		if (world.isRemote || player.bedLocation == null || !world.isBlockLoaded(player.bedLocation)) return;

		BlockPos mainPos = player.bedLocation;
		IBlockState state = world.getBlockState(mainPos);
		Block block = state.getBlock();

		if (block == ModBlocks.technical_block) {
			for (EnumFacing facing : EnumFacing.HORIZONTALS) {
				BlockPos candidate = mainPos.offset(facing);
				if (world.getBlockState(candidate).getBlock() instanceof BlockHayBed) {
					mainPos = candidate;
					block = world.getBlockState(mainPos).getBlock();
					break;
				}
			}
		}

		if (block instanceof BlockHayBed) {
			if (event.updateWorld()) {
				player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 600, 0));
				world.destroyBlock(mainPos, false);
			}
		}
	}
}
