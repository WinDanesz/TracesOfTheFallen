package com.windanesz.tracesofthefallen.item;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = TracesOfTheFallen.MODID)
public class FloaterEventHandler {

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			EntityPlayer player = event.player;
			
			// Check legs slot for a floater
			ItemStack legs = player.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
			if (!legs.isEmpty() && legs.getItem() instanceof ItemFloater) {
			
				// Check if player is in water
				if (player.isInWater()) {
					// The floater ring renders roughly 0.65 to 0.9 blocks above the player's feet.
					// We use a 2-tier check to create a smooth equilibrium zone.
					// Adjusted +0.125 (2 pixels) to make the player sit 2px deeper in the water.
					BlockPos aboveWaistPos = new BlockPos(player.posX, player.posY + 1.025D, player.posZ);
					BlockPos atWaistPos = new BlockPos(player.posX, player.posY + 0.775D, player.posZ);

					boolean aboveWaist = player.world.getBlockState(aboveWaistPos).getMaterial().isLiquid();
					boolean atWaist = player.world.getBlockState(atWaistPos).getMaterial().isLiquid();

					if (aboveWaist) {
						// Deep in water. Push up towards the surface.
						player.motionY += 0.04D;
					} else if (atWaist) {
						player.motionY += 0.02D;
					}
					// Ensure they don't take fall damage when hitting the water
						player.fallDistance = 0.0F;
				}
		}
		}
	}
}
