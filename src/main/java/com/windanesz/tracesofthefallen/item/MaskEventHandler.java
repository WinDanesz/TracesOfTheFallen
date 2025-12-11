package com.windanesz.tracesofthefallen.item;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.init.ModItems;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = TracesOfTheFallen.MODID)
public class MaskEventHandler {

    @SubscribeEvent
    public static void onSetAttackTarget(LivingSetAttackTargetEvent event) {
        // Check if a skeleton is trying to target a player
        if (event.getEntity() instanceof AbstractSkeleton && event.getTarget() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getTarget();
            
            // Check if player is wearing the Veiled Mask in the head slot
            ItemStack headItem = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
            
            if (!headItem.isEmpty() && headItem.getItem() == ModItems.veiled_mask) {
                // Cancel the targeting by setting target to null
                AbstractSkeleton skeleton = (AbstractSkeleton) event.getEntity();
                skeleton.setAttackTarget(null);
            }
        }
    }
}
