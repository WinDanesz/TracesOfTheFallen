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
	public static final SoundEvent SPECTER_AMBIENT = createSound("entity.specter_ambient");
	public static final SoundEvent SPECTER_HOSTILE = createSound("entity.specter_hostile");
	public static final SoundEvent SPECTER_HURT = createSound("entity.specter_hurt");
	public static final SoundEvent GOBLIN_HURT = createSound("entity.goblin_hurt");
	public static final SoundEvent GOBLIN_IDLE = createSound("entity.goblin_idle");
	public static final SoundEvent GOBLIN_AGGRO = createSound("entity.goblin_aggro");
	public static final SoundEvent GOBLIN_DIE = createSound("entity.goblin_die");
	public static final SoundEvent IDOL_ACTIVATE = createSound("idol_activate");
	public static final SoundEvent DIOPTRA = createSound("dioptra");
	public static final SoundEvent BONE_PILE = createSound("bone_pile");
	public static final SoundEvent LIGHTNING_ZAP = createSound("lightning_zap");
	public static final SoundEvent RECORD_GNOSSIENE_1 = createSound("record.gnossiene_1");
	public static final SoundEvent FABRICATOR_WORKING = createSound("fabricator_working");
	public static final SoundEvent LOST_AMBIENT = createSound("entity.lost_ambient");
	public static final SoundEvent LOST_HURT = createSound("entity.lost_hurt");
	public static final SoundEvent LOST_DEATH = createSound("entity.lost_death");
	public static final SoundEvent STONE_SLIDE = createSound("stone_slide");
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
		event.getRegistry().register(SPECTER_AMBIENT);
		event.getRegistry().register(SPECTER_HOSTILE);
		event.getRegistry().register(SPECTER_HURT);
		event.getRegistry().register(GOBLIN_HURT);
		event.getRegistry().register(GOBLIN_IDLE);
		event.getRegistry().register(GOBLIN_AGGRO);
		event.getRegistry().register(GOBLIN_DIE);
		event.getRegistry().register(IDOL_ACTIVATE);
		event.getRegistry().register(DIOPTRA);
		event.getRegistry().register(BONE_PILE);
		event.getRegistry().register(LIGHTNING_ZAP);
		event.getRegistry().register(RECORD_GNOSSIENE_1);
		event.getRegistry().register(FABRICATOR_WORKING);
		event.getRegistry().register(LOST_AMBIENT);
		event.getRegistry().register(LOST_HURT);
		event.getRegistry().register(LOST_DEATH);
		event.getRegistry().register(STONE_SLIDE);
	}
}
