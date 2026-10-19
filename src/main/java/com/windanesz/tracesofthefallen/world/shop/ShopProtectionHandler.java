package com.windanesz.tracesofthefallen.world.shop;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.entity.EntitySubterfuge;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * While Subterfuge is in her shop, nothing inside it can be broken, placed or blown up. When she is away the
 * shop is just a room, and a player who tears it down for good means she never comes back.
 */
@Mod.EventBusSubscriber(modid = TracesOfTheFallen.MODID)
public final class ShopProtectionHandler {

	/** A shop is at most a chunk across, so its keeper is always well within this of any block it covers. */
	private static final double SEARCH_RADIUS = 32.0D;

	private ShopProtectionHandler() {}

	/** The shopkeeper whose shop covers this position, whether she is in it or not. */
	@Nullable
	public static EntitySubterfuge findKeeper(World world, BlockPos pos) {
		if (world.isRemote) {
			return null;
		}
		AxisAlignedBB area = new AxisAlignedBB(pos).grow(SEARCH_RADIUS);
		for (EntitySubterfuge keeper : world.getEntitiesWithinAABB(EntitySubterfuge.class, area)) {
			if (keeper.isShopkeeper() && !keeper.isDead && keeper.isInShop(pos)) {
				return keeper;
			}
		}
		return null;
	}

	public static boolean isProtected(World world, BlockPos pos) {
		if (!Settings.worldgenSettings.subterfugeShopProtection) {
			return false;
		}
		EntitySubterfuge keeper = findKeeper(world, pos);
		return keeper != null && keeper.isShopOpen();
	}

	private static void deny(@Nullable EntityPlayer player, World world, BlockPos pos) {
		if (player == null) {
			return;
		}
		player.sendStatusMessage(new TextComponentTranslation("totf:shop.protected"), true);
		EntitySubterfuge keeper = findKeeper(world, pos);
		if (keeper != null) {
			keeper.setExpressionState(3, 40);
		}
	}

	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event) {
		EntityPlayer player = event.getPlayer();
		if (player != null && player.capabilities.isCreativeMode) {
			return;
		}
		if (isProtected(event.getWorld(), event.getPos())) {
			event.setCanceled(true);
			deny(player, event.getWorld(), event.getPos());
		}
	}

	@SubscribeEvent
	public static void onBlockPlace(BlockEvent.PlaceEvent event) {
		EntityPlayer player = event.getPlayer();
		if (player != null && player.capabilities.isCreativeMode) {
			return;
		}
		if (isProtected(event.getWorld(), event.getPos())) {
			event.setCanceled(true);
			deny(player, event.getWorld(), event.getPos());
		}
	}

	@SubscribeEvent
	public static void onBucket(FillBucketEvent event) {
		RayTraceResult target = event.getTarget();
		if (target == null || target.typeOfHit != RayTraceResult.Type.BLOCK) {
			return;
		}
		EntityPlayer player = event.getEntityPlayer();
		if (player != null && player.capabilities.isCreativeMode) {
			return;
		}
		World world = event.getWorld();
		BlockPos hit = target.getBlockPos();
		if (isProtected(world, hit) || (target.sideHit != null && isProtected(world, hit.offset(target.sideHit)))) {
			event.setCanceled(true);
			deny(player, world, hit);
		}
	}

	@SubscribeEvent
	public static void onMobGrief(LivingDestroyBlockEvent event) {
		if (isProtected(event.getEntityLiving().world, event.getPos())) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onExplosion(ExplosionEvent.Detonate event) {
		World world = event.getWorld();
		if (world.isRemote || !Settings.worldgenSettings.subterfugeShopProtection || event.getAffectedBlocks().isEmpty()) {
			return;
		}

		// One entity query for the whole blast instead of one per block.
		BlockPos centre = new BlockPos(event.getExplosion().getPosition());
		List<EntitySubterfuge> open = new ArrayList<>();
		for (EntitySubterfuge keeper : world.getEntitiesWithinAABB(EntitySubterfuge.class, new AxisAlignedBB(centre).grow(SEARCH_RADIUS + 16.0D))) {
			if (keeper.isShopkeeper() && keeper.isShopOpen()) {
				open.add(keeper);
			}
		}
		if (open.isEmpty()) {
			return;
		}
		event.getAffectedBlocks().removeIf(pos -> {
			for (EntitySubterfuge keeper : open) {
				if (keeper.isInShop(pos)) {
					return true;
				}
			}
			return false;
		});
	}
}
