package com.windanesz.tracesofthefallen.world.structure;

import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Registered on the terrain bus: ordinary @EventBusSubscriber handlers use the wrong bus. */
public class MinecrawlerMineshaftHandler {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onInitMapGen(InitMapGenEvent event) {
        if (event.getType() == InitMapGenEvent.EventType.MINESHAFT
                && event.getNewGen().getClass() == MapGenMineshaft.class) {
            // Keep other mods' mineshaft replacements, including subclasses, intact.
            event.setNewGen(new MapGenMinecrawlerMineshaft((MapGenMineshaft) event.getNewGen()));
        }
    }
}
