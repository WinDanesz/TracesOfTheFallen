package com.windanesz.tracesofthefallen.init;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(TracesOfTheFallen.MODID)
@Mod.EventBusSubscriber(modid = TracesOfTheFallen.MODID)
public class ModSounds {
	private ModSounds() {}

	public static final SoundEvent SPECTER_HURT = createSound("entity.specter_hurt");
	public static final SoundEvent GOBLIN_HURT = createSound("entity.goblin_hurt");
	public static final SoundEvent GOBLIN_IDLE = createSound("entity.goblin_idle");
	public static final SoundEvent GOBLIN_AGGRO = createSound("entity.goblin_aggro");
	public static final SoundEvent GOBLIN_DIE = createSound("entity.goblin_die");
	public static final SoundEvent IDOL_ACTIVATE = createSound("idol_activate");
	public static final SoundEvent DIOPTRA = createSound("dioptra");

	public static SoundEvent createSound(String name) {
		return createSound(TracesOfTheFallen.MODID, name);
	}
	/**
	 * Creates a sound with the given name, to be read from {@code assets/[modID]/sounds.json}.
	 */
	public static SoundEvent createSound(String modID, String name) {
		// All the setRegistryName methods delegate to this one, it doesn't matter which you use.
		return new SoundEvent(new ResourceLocation(modID, name)).setRegistryName(name);
	}

	@SubscribeEvent
	public static void register(RegistryEvent.Register<SoundEvent> event) {
		event.getRegistry().register(SPECTER_HURT);
		event.getRegistry().register(GOBLIN_HURT);
		event.getRegistry().register(GOBLIN_IDLE);
		event.getRegistry().register(GOBLIN_AGGRO);
		event.getRegistry().register(GOBLIN_DIE);
		event.getRegistry().register(IDOL_ACTIVATE);
		event.getRegistry().register(DIOPTRA);
	}
}
