package com.windanesz.tracesofthefallen.init;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber
public class ModEntities {

	private ModEntities() {}


	/**
	 * Most entity trackers fall into one of a few categories, so they are defined here for convenience. This
	 * generally follows the values used in vanilla for each entity type.
	 */
	enum TrackingType {

		LIVING(80, 3, true),
		PROJECTILE(64, 1, true),
		CONSTRUCT(160, 10, false);

		int range;
		int interval;
		boolean trackVelocity;

		TrackingType(int range, int interval, boolean trackVelocity) {
			this.range = range;
			this.interval = interval;
			this.trackVelocity = trackVelocity;
		}
	}

	/**
	 * Incrementing index for the mod-specific entity network ID.
	 */
	private static int id = 0;

	@SubscribeEvent
	public static void register(RegistryEvent.Register<EntityEntry> event) {

		IForgeRegistry<EntityEntry> registry = event.getRegistry();

		// projectile entities
		registry.register(createEntry(EntityAncestralSifter.class, "ancestral_sifter", TrackingType.CONSTRUCT).build());
		registry.register(createEntry(EntityDioptraSeat.class, "dioptra_seat", TrackingType.CONSTRUCT).build());
		registry.register(createEntry(EntityTelescopeSeat.class, "telescope_seat", TrackingType.CONSTRUCT).build());
		registry.register(createEntry(EntityGlassFloat.class, "glass_float", TrackingType.CONSTRUCT).build());
		registry.register(createEntry(EntitySpecter.class, "specter", TrackingType.LIVING).egg(0xebf2ff,0x6aeba6).build());
		registry.register(createEntry(EntityFamiliarSpecter.class, "specter_familiar", TrackingType.LIVING).egg(0xebf2ff,0x6aeba6).build());
		registry.register(createEntry(EntityModPainting.class, "forest_painting", TrackingType.LIVING).build());
		registry.register(createEntry(EntityGoblin.class, "goblin", TrackingType.LIVING).egg(0x0d2e11,0x3eb049).build());
		registry.register(createEntry(EntityGoblinStarved.class, "goblin_starved", TrackingType.LIVING).egg(0x1a2e0d, 0x5ab03e).build());
		registry.register(createEntry(EntityGoblinWayfarer.class, "goblin_wayfarer", TrackingType.LIVING).egg(0x2e1d0d, 0xb0653e).build());
		registry.register(createEntry(EntityGoblinEngineer.class, "goblin_engineer", TrackingType.LIVING).egg(0x2e270d, 0xb08f3e).build());
		registry.register(createEntry(EntityGoblinSapper.class, "goblin_sapper", TrackingType.LIVING).egg(0x2e2b0d, 0xb09f3e).build());
		registry.register(createEntry(EntityGoblinTunneler.class, "goblin_tunneler", TrackingType.LIVING).egg(0x2e2c0d, 0xb0af3e).build());
		registry.register(createEntry(EntityGoblinTrapper.class, "goblin_trapper", TrackingType.LIVING).egg(0x2e2d0d, 0xb0bf3e).build());
		registry.register(createEntry(EntityGoblinShaman.class, "goblin_shaman", TrackingType.LIVING).egg(0x0d1a2e, 0x3e68b0).build());
		registry.register(createEntry(EntityGoblinSkeleton.class, "goblin_skeleton", TrackingType.LIVING).egg(0xdedede, 0x5a5a5a).build());
		registry.register(createEntry(EntityTinybones.class, "tinybones", TrackingType.LIVING).egg(0xeaeaea, 0x990000).build());
		registry.register(createEntry(EntityGoblinBrute.class, "goblin_brute", TrackingType.LIVING).egg(0x2e0d0d, 0xb03e3e).build());
		registry.register(createEntry(EntityGoblinWarrior.class, "goblin_warrior", TrackingType.LIVING).egg(0x1e2e3e, 0x7aa0b0).build());
		registry.register(createEntry(EntityMinecrawler.class, "minecrawler", TrackingType.LIVING).egg(0x2d241a, 0x8b5a2b).build());
		registry.register(createEntry(EntityWroughtBomb.class, "wrought_bomb", TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntityFetidDagger.class, "fetid_dagger", TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntityFireOrb.class, "fire_orb", TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntityWillOWisp.class, "will_o_wisp", TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntitySeekingOrb.class, "seeking_orb", TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntityVoidSlash.class, "void_slash", TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntityMagmaBlast.class, "magma_blast", TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntityMagmaPool.class, "magma_pool", TrackingType.LIVING).build());
		registry.register(createEntry(EntityJawTrap.class, "jaw_trap", TrackingType.LIVING).build());
		registry.register(createEntry(EntityBloodTotem.class, "blood_totem", TrackingType.LIVING).build());
		registry.register(createEntry(EntityGoblinNest.class, "goblin_nest", TrackingType.LIVING).build());
		registry.register(createEntry(EntitySidhe.class, "sidhe", TrackingType.LIVING).egg(0x1a1a1a, 0x8b008b).build());
		registry.register(createEntry(EntityLamphead.class, "lamphead", TrackingType.LIVING).egg(0x0e4749, 0xffff00).build());
		registry.register(createEntry(EntityFrostling.class, "frostling", TrackingType.LIVING).egg(0x5a8fb3, 0xbfd7e8).build());
		registry.register(createEntry(EntityFrostlingMask.class, "frostling_mask", TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntityZapLightning.class, "zap_lightning", TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntityGnossic.class, "gnossic", TrackingType.LIVING).egg(0x2a2a2a, 0x6e1b1b).build());
	}

	/**
	 * Private helper method that simplifies the parts of an {@link EntityEntry} that are common to all entities.
	 * This automatically assigns a network id, and accepts a {@link TrackingType} for automatic tracker assignment.
	 *
	 * @param entityClass The entity class to use.
	 * @param name        The name of the entity. This will form the path of a {@code ResourceLocation} with domain
	 *                    {@code ebwizardry}, which in turn will be used as both the registry name and the 'command' name.
	 * @param tracking    The {@link TrackingType} to use for this entity.
	 * @param <T>         The type of entity.
	 * @return The (part-built) builder instance, allowing other builder methods to be added as necessary.
	 */
	private static <T extends Entity> EntityEntryBuilder<T> createEntry(Class<T> entityClass, String name, TrackingType tracking) {
		return createEntry(entityClass, name).tracker(tracking.range, tracking.interval, tracking.trackVelocity);
	}

	/**
	 * Private helper method that simplifies the parts of an {@link EntityEntry} that are common to all entities.
	 * This automatically assigns a network id.
	 *
	 * @param entityClass The entity class to use.
	 * @param name        The name of the entity. This will form the path of a {@code ResourceLocation} with domain
	 *                    {@code ebwizardry}, which in turn will be used as both the registry name and the 'command' name.
	 * @param <T>         The type of entity.
	 * @return The (part-built) builder instance, allowing other builder methods to be added as necessary.
	 */
	private static <T extends Entity> EntityEntryBuilder<T> createEntry(Class<T> entityClass, String name) {
		ResourceLocation registryName = new ResourceLocation(TracesOfTheFallen.MODID, name);
		return EntityEntryBuilder.<T>create().entity(entityClass).id(registryName, id++).name(registryName.toString());
	}

	/**
	 * Private helper method that simplifies the parts of an {@link EntityEntry} that are common to all entities.
	 * This automatically assigns a network id, and accepts a {@link TrackingType} for automatic tracker assignment.
	 *
	 * @param entityClass The entity class to use.
	 * @param name        The name of the entity. This will form the path of a {@code ResourceLocation} with domain
	 *                    {@code ebwizardry}, which in turn will be used as both the registry name and the 'command' name.
	 * @param tracking    The {@link TrackingType} to use for this entity.
	 * @param <T>         The type of entity.
	 * @return The (part-built) builder instance, allowing other builder methods to be added as necessary.
	 */
	private static <T extends Entity> EntityEntryBuilder<T> createEntry(Class<T> entityClass, String name, String modid, TrackingType tracking) {
		return createEntry(entityClass, name, modid).tracker(tracking.range, tracking.interval, tracking.trackVelocity);
	}

	/**
	 * Private helper method that simplifies the parts of an {@link EntityEntry} that are common to all entities.
	 * This automatically assigns a network id.
	 *
	 * @param entityClass The entity class to use.
	 * @param name        The name of the entity. This will form the path of a {@code ResourceLocation} with domain
	 *                    {@code ebwizardry}, which in turn will be used as both the registry name and the 'command' name.
	 * @param <T>         The type of entity.
	 * @return The (part-built) builder instance, allowing other builder methods to be added as necessary.
	 */
	private static <T extends Entity> EntityEntryBuilder<T> createEntry(Class<T> entityClass, String name, String modid) {
		ResourceLocation registryName = new ResourceLocation(modid, name);
		return EntityEntryBuilder.<T>create().entity(entityClass).id(registryName, id++).name(registryName.toString());
	}
}
