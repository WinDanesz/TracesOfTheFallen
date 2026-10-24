package com.windanesz.tracesofthefallen.eventhandler;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.block.BlockClayPot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = TracesOfTheFallen.MODID)
public final class ClayPotEventHandler {

	private ClayPotEventHandler() {}

	@SubscribeEvent
	public static void onProjectileImpact(ProjectileImpactEvent event) {
		RayTraceResult result = event.getRayTraceResult();
		if (result.typeOfHit != RayTraceResult.Type.BLOCK) {
			return;
		}

		World world = event.getEntity().world;
		if (world.isRemote) {
			return;
		}

		BlockPos pos = result.getBlockPos();
		if (world.getBlockState(pos).getBlock() instanceof BlockClayPot) {
			world.destroyBlock(pos, true);
		}
	}
}
