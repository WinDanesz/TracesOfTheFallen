package com.windanesz.tracesofthefallen.capability;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.Utils;
import com.windanesz.tracesofthefallen.entity.EntitySpecter;
import com.windanesz.tracesofthefallen.network.PacketHandler;
import com.windanesz.tracesofthefallen.packet.PacketPlayerSync;
import net.minecraft.advancements.Advancement;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber
public class HauntingCapability implements INBTSerializable<NBTTagCompound> {

	// This annotation does some crazy Forge magic behind the scenes and assigns this field a value.
	@CapabilityInject(HauntingCapability.class)
	private static final Capability<HauntingCapability> PLAYER_CAPABILITY = null;

	// Cache for haunting block values
	private static Map<String, Integer> hauntingBlockCache = null;

	private static Map<String, Integer> getHauntingBlocks() {
		if (hauntingBlockCache != null) {
			return hauntingBlockCache;
		}

		hauntingBlockCache = new HashMap<>();

		for (String entry : Settings.hauntingSettings.hauntingBlocks) {
			if (entry == null || entry.trim().isEmpty()) continue;

			String[] parts = entry.split(":");
			if (parts.length < 3) continue;

			try {
				// parts: [modid, blockname, (meta?), amount]
				boolean hasMeta = parts.length == 4;

				String key = hasMeta
						? parts[0] + ":" + parts[1] + ":" + parts[2]
						: parts[0] + ":" + parts[1];

				int amount = Integer.parseInt(hasMeta ? parts[3] : parts[2]);

				hauntingBlockCache.put(key, amount);

			} catch (NumberFormatException e) {
				TracesOfTheFallen.LOGGER.warn("Invalid haunting block entry: " + entry);
			}
		}

		return hauntingBlockCache;
	}

	private static Map<String, int[]> hauntingKillItemCache = null;

	private static Map<String, int[]> getHauntingKillItems() {
		if (hauntingKillItemCache != null) return hauntingKillItemCache;
		hauntingKillItemCache = new HashMap<>();
		for (String entry : Settings.hauntingSettings.hauntingKillItemReductions) {
			if (entry == null || entry.trim().isEmpty()) continue;
			String[] parts = entry.split("\\|");
			if (parts.length != 3) continue;
			try {
				int reduction = Integer.parseInt(parts[1]);
				int damage = Integer.parseInt(parts[2]);
				hauntingKillItemCache.put(parts[0], new int[]{reduction, damage});
			} catch (NumberFormatException e) {
				TracesOfTheFallen.LOGGER.warn("Invalid haunting kill item entry: " + entry);
			}
		}
		return hauntingKillItemCache;
	}

	public static void clearHauntingBlockCache() {
		hauntingBlockCache = null;
		hauntingKillItemCache = null;
	}

	private final EntityPlayer player;

	public int getHauntingProgress() {
		return hauntingProgress;
	}

	public void setHauntingProgress(int hauntingProgress) {
		int oldProgress = this.hauntingProgress;
		this.hauntingProgress = Math.max(0, Math.min(100, hauntingProgress));

		if (this.hauntingProgress > 0) {
			if (player instanceof EntityPlayerMP) {
				EntityPlayerMP playerMP = (EntityPlayerMP) player;
				Advancement advancement = playerMP.getServer().getAdvancementManager().getAdvancement(new ResourceLocation(TracesOfTheFallen.MODID, "haunted"));
				if (advancement != null) {
					if (!playerMP.getAdvancements().getProgress(advancement).isDone()) {
						playerMP.getAdvancements().grantCriterion(advancement, "haunted");
					}
				}
			}
		}

		if(oldProgress != this.hauntingProgress) {
			sync();
		}
	}

	public void addHauntingProgress(int amount) {
		setHauntingProgress(this.hauntingProgress + amount);
	}

	public void reduceHauntingProgress(int amount) {
		addHauntingProgress(-amount);
	}

	public int hauntingProgress = 0;
	public int hauntingSpawnCooldown = 0;

	public HauntingCapability() {
		this(null); // Nullary constructor for the registration method factory parameter
	}

	public HauntingCapability(EntityPlayer player) {
		this.player = player;
	}

	/**
	 * Called from preInit
	 */
	public static void register() {

		CapabilityManager.INSTANCE.register(HauntingCapability.class, new IStorage<HauntingCapability>() {
			// Unused but necessary...
			@Override
			public NBTBase writeNBT(Capability<HauntingCapability> capability, HauntingCapability instance, EnumFacing side) {
				return null;
			}

			@Override
			public void readNBT(Capability<HauntingCapability> capability, HauntingCapability instance, EnumFacing side, NBTBase nbt) {
			}

		}, HauntingCapability::new);
	}

	/**
	 * Returns the WizardData instance for the specified player.
	 */
	public static HauntingCapability get(EntityPlayer player) {
		return player.getCapability(PLAYER_CAPABILITY, null);
	}

	/**
	 * Called from the event handler each time the associated player entity is cloned, i.e. on respawn or when
	 * travelling to a different dimension. Used to copy over any data that should persist over player death. This
	 * is the inverse of the old onPlayerDeath method, which reset the data that shouldn't persist.
	 *
	 * @param data    The old WizardData whose data is to be copied over.
	 * @param respawn True if the player died and is respawning, false if they are just travelling between dimensions.
	 */
	public void copyFrom(HauntingCapability data, boolean respawn) {
		this.hauntingProgress = data.hauntingProgress;
		this.hauntingSpawnCooldown = data.hauntingSpawnCooldown;
	}

	/**
	 * Sends a packet to this player's client to synchronise necessary information. Only called server side.
	 */
	public void sync() {
		if (this.player instanceof EntityPlayerMP) {
			IMessage msg = new PacketPlayerSync.Message(this.hauntingProgress);
			PacketHandler.net.sendTo(msg, (EntityPlayerMP) this.player);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public NBTTagCompound serializeNBT() {

		NBTTagCompound properties = new NBTTagCompound();
		properties.setInteger("hauntingProgress", hauntingProgress);
		properties.setInteger("hauntingSpawnCooldown", hauntingSpawnCooldown);
		return properties;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {

		if (nbt != null) {
			this.hauntingProgress = nbt.getInteger("hauntingProgress");
			this.hauntingSpawnCooldown = nbt.getInteger("hauntingSpawnCooldown");
		}
	}

	// ============================================== Event Handlers ==============================================

	@SubscribeEvent
	// The type parameter here has to be Entity, not EntityPlayer, or the event won't get fired.
	public static void onCapabilityLoad(AttachCapabilitiesEvent<Entity> event) {

		if (event.getObject() instanceof EntityPlayer)
			event.addCapability(new ResourceLocation(TracesOfTheFallen.MODID, TracesOfTheFallen.MODNAME + "Data"), new HauntingCapability.Provider((EntityPlayer) event.getObject()));
	}

	@SubscribeEvent
	public static void onPlayerCloneEvent(PlayerEvent.Clone event) {

		HauntingCapability newData = HauntingCapability.get(event.getEntityPlayer());
		HauntingCapability oldData = HauntingCapability.get(event.getOriginal());

		newData.copyFrom(oldData, event.isWasDeath());

		newData.sync();
	}

	@SubscribeEvent
	public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
		if (!event.getEntity().world.isRemote && event.getEntity() instanceof EntityPlayerMP) {
			HauntingCapability data = HauntingCapability.get((EntityPlayer) event.getEntity());
			if (data != null) data.sync();
		}
	}

	@SubscribeEvent
	public static void onLivingUpdateEvent(TickEvent.PlayerTickEvent event) {

		if (event.phase == TickEvent.Phase.END && event.player.ticksExisted % 20 == 0 && !event.player.world.isRemote && event.player.world.getDifficulty() != EnumDifficulty.PEACEFUL) {
			EntityPlayer player = event.player;
			HauntingCapability cap = HauntingCapability.get(player);
			if (cap != null) {
				if (cap.hauntingSpawnCooldown > 0) {
					cap.hauntingSpawnCooldown -= 20;
					if (cap.hauntingSpawnCooldown < 0) cap.hauntingSpawnCooldown = 0;
				}
				
				if (!player.capabilities.isCreativeMode) {
					int hauntingProg = cap.hauntingProgress;
					if (hauntingProg > 50 && cap.hauntingSpawnCooldown == 0) {
						if (player.world.rand.nextInt(20) == 0) {
							List<EntitySpecter> specters = player.world.getEntitiesWithinAABB(EntitySpecter.class, new AxisAlignedBB(player.getPosition()).grow(30));

							if (specters.isEmpty()) {
								BlockPos pos = Utils.findNearbyAirSpace(player.world, player.getPosition(), 6);
								if (pos != null) {
									EntitySpecter specter;
									if (hauntingProg >= 85 && player.world.rand.nextBoolean()) {
										specter = new com.windanesz.tracesofthefallen.entity.EntitySpecterGrasper(player.world);
									} else if (hauntingProg >= 95) {
										specter = new com.windanesz.tracesofthefallen.entity.EntitySpecterGrasper(player.world);
									} else {
										specter = new EntitySpecter(player.world);
									}
									specter.setPosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
									specter.getEntityData().setBoolean("HauntingSpawned", true);
									player.world.spawnEntity(specter);
									specter.setAttackTarget(player);
									
									// 5 minute cooldown if a Grasper spawns
									if (specter instanceof com.windanesz.tracesofthefallen.entity.EntitySpecterGrasper) {
										cap.hauntingSpawnCooldown = 6000;
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event) {
		if (!event.getWorld().isRemote && event.getPlayer() != null && !event.getPlayer().capabilities.isCreativeMode) {
			Block block = event.getState().getBlock();
			int meta = block.getMetaFromState(event.getState());
			Map<String, Integer> hauntingBlocks = getHauntingBlocks();
			
			// Check for specific meta first
			String blockId = block.getRegistryName().toString();
			String keyWithMeta = blockId + ":" + meta;
			Integer amount = hauntingBlocks.get(keyWithMeta);
			
			// Fall back to any meta if specific meta not found
			if (amount == null) {
				amount = hauntingBlocks.get(blockId);
			}
			
			if (amount != null) {
				EntityPlayer player = event.getPlayer();
				HauntingCapability haunting = HauntingCapability.get(player);
				if (haunting != null) {
					if (amount > 0) {
						haunting.addHauntingProgress(amount);
					} else if (amount < 0) {
						haunting.reduceHauntingProgress(-amount);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onLivingDeath(LivingDeathEvent event) {
		if (event.getEntityLiving().world.isRemote) return;

		// 1. If player is killed by haunting mob
		if (event.getEntityLiving() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();
			DamageSource source = event.getSource();
			if (source != null && source.getTrueSource() != null) {
				Entity killer = source.getTrueSource();
				if (killer.getEntityData().getBoolean("HauntingSpawned")) {
					HauntingCapability cap = HauntingCapability.get(player);
					if (cap != null) {
						cap.hauntingSpawnCooldown = Settings.hauntingSettings.hauntingSpawnCooldownAfterDeath;
					}
				}
			}
		}

		// 2. If haunting mob is killed by player
		if (event.getEntityLiving().getEntityData().getBoolean("HauntingSpawned")) {
			DamageSource source = event.getSource();
			if (source != null && source.getTrueSource() instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) source.getTrueSource();
				
				if (event.getEntityLiving() instanceof com.windanesz.tracesofthefallen.entity.EntitySpecterGrasper) {
					HauntingCapability cap = HauntingCapability.get(player);
					if (cap != null) {
						cap.hauntingSpawnCooldown = Math.max(cap.hauntingSpawnCooldown, 12000); // 10 mins
					}
				}
				
				ItemStack mainHand = player.getHeldItemMainhand();
				ItemStack offHand = player.getHeldItemOffhand();
				
				Map<String, int[]> reductions = getHauntingKillItems();
				
				boolean applied = tryApplyHauntingKillReduction(player, mainHand, reductions);
				if (!applied) {
					tryApplyHauntingKillReduction(player, offHand, reductions);
				}
			}
		}
	}

	private static boolean tryApplyHauntingKillReduction(EntityPlayer player, ItemStack stack, Map<String, int[]> reductions) {
		if (stack.isEmpty()) return false;
		String itemId = stack.getItem().getRegistryName().toString();
		String keyWithMeta = itemId + ":" + stack.getMetadata();
		
		int[] params = reductions.get(keyWithMeta);
		if (params == null) {
			params = reductions.get(itemId);
		}
		
		if (params != null) {
			int reduction = params[0];
			int damage = params[1];
			
			if (stack.isItemStackDamageable()) {
				int remaining = stack.getMaxDamage() - stack.getItemDamage();
				if (remaining >= damage) {
					stack.damageItem(damage, player);
					HauntingCapability cap = HauntingCapability.get(player);
					if (cap != null) {
						cap.reduceHauntingProgress(reduction);
					}
					return true;
				}
			}
		}
		return false;
	}

	public static class Provider implements ICapabilitySerializable<NBTTagCompound> {

		private final HauntingCapability data;

		public Provider(EntityPlayer player) {
			data = new HauntingCapability(player);
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == PLAYER_CAPABILITY;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {

			if (capability == PLAYER_CAPABILITY) {
				return PLAYER_CAPABILITY.cast(data);
			}

			return null;
		}

		@Override
		public NBTTagCompound serializeNBT() {
			return data.serializeNBT();
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			data.deserializeNBT(nbt);
		}

	}

}
