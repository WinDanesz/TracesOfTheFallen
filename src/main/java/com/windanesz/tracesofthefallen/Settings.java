package com.windanesz.tracesofthefallen;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Config(modid = TracesOfTheFallen.MODID, name = "TracesOfTheFallen")
public class Settings {

    // These are set after config load, not part of config fields
    public List<ResourceLocation> lostCargoBiomeWhitelist = Arrays.asList(toResourceLocations(worldgenSettings.lostCargoBiomeWhitelist));
    public List<ResourceLocation> lostCargoBiomeBlacklist = Arrays.asList(toResourceLocations(worldgenSettings.lostCargoBiomeBlacklist));
    public List<ResourceLocation> stoneCircleBiomeWhitelist = Arrays.asList(toResourceLocations(worldgenSettings.stoneCircleBiomeWhitelist));
    public List<ResourceLocation> stoneCircleBiomeBlacklist = Arrays.asList(toResourceLocations(worldgenSettings.stoneCircleBiomeBlacklist));
    public List<ResourceLocation> remainsBiomeWhitelist = Arrays.asList(toResourceLocations(worldgenSettings.remainsBiomeWhitelist));
    public List<ResourceLocation> remainsBiomeBlacklist = Arrays.asList(toResourceLocations(worldgenSettings.remainsBiomeBlacklist));
    public List<ResourceLocation> potionCrateBiomeWhitelist = Arrays.asList(toResourceLocations(worldgenSettings.potionCrateBiomeWhitelist));
    public List<ResourceLocation> potionCrateBiomeBlacklist = Arrays.asList(toResourceLocations(worldgenSettings.potionCrateBiomeBlacklist));
    public List<ResourceLocation> tentBiomeWhitelist = Arrays.asList(toResourceLocations(worldgenSettings.tentBiomeWhitelist));
    public List<ResourceLocation> tentBiomeBlacklist = Arrays.asList(toResourceLocations(worldgenSettings.tentBiomeBlacklist));
    public List<ResourceLocation> abandonedTentBiomeWhitelist = Arrays.asList(toResourceLocations(worldgenSettings.abandonedTentBiomeWhitelist));
    public List<ResourceLocation> abandonedTentBiomeBlacklist = Arrays.asList(toResourceLocations(worldgenSettings.abandonedTentBiomeBlacklist));
    public List<ResourceLocation> abandonedTentWithTotemBiomeWhitelist = Arrays.asList(toResourceLocations(worldgenSettings.abandonedTentWithTotemBiomeWhitelist));
    public List<ResourceLocation> abandonedTentWithTotemBiomeBlacklist = Arrays.asList(toResourceLocations(worldgenSettings.abandonedTentWithTotemBiomeBlacklist));

	public static ResourceLocation[] toResourceLocations(String... strings) {
        return Arrays.stream(strings).filter(s -> s != null && !s.trim().isEmpty()).map(s -> new ResourceLocation(s.toLowerCase(Locale.ROOT).trim())).toArray(ResourceLocation[]::new);
    }

    @Config.Name("Worldgen Settings")
    @Config.LangKey("settings.totf:general_settings")
    public static WorldgenSettings worldgenSettings = new WorldgenSettings();

	@Config.Name("Misc Settings")
	@Config.LangKey("settings.totf:general_settings")
	public static MiscSettings miscSettings = new MiscSettings();

    @Config.Name("Client Settings")
    @Config.LangKey("settings.totf:client_settings")
    public static ClientSettings clientSettings = new ClientSettings();

    public static class WorldgenSettings {

        @Config.Name("Lost Loot Dimensions")
        @Config.Comment("[Server-only] List of dimension ids where loot spawns.")
        @Config.RequiresMcRestart
        public int[] dimensionList = {0};

        @Config.Name("Lost Cargo Frequency")
        @Config.Comment("How many Lost Cargo blocks to generate per chunk (default: 650)")
        public int lostCargoFrequency = 650;

        @Config.Name("Lost Cargo Biome Whitelist")
        @Config.Comment("Biomes where Lost Cargo can generate (empty = all biomes allowed)")
        public String[] lostCargoBiomeWhitelist = new String[0];

        @Config.Name("Lost Cargo Biome Blacklist")
        @Config.Comment("Biomes where Lost Cargo cannot generate")
        public String[] lostCargoBiomeBlacklist = new String[0];

        @Config.Name("Stone Circle Chance")
        @Config.Comment("Chance for a Stone Circle to generate in a chunk. 1 in X chance. Set to 0 to disable. Default: 1000")
		@Config.RangeInt(min = 100)
		public int stoneCircleChance = 1000;

        @Config.Name("Stone Circle Biome Whitelist")
        @Config.Comment("Biomes where Stone Circle can generate (empty = all biomes allowed)")
        public String[] stoneCircleBiomeWhitelist = new String[0];

        @Config.Name("Stone Circle Biome Blacklist")
        @Config.Comment("Biomes where Stone Circle cannot generate")
        public String[] stoneCircleBiomeBlacklist = new String[0];

        @Config.Name("Remains Chance")
        @Config.Comment("Chance for (skeletal) Remains to generate in a chunk. 1 in X chance. Set to 0 to disable. Default: 800")
        public int remainsChance = 800;

        @Config.Name("Remains Biome Whitelist")
        @Config.Comment("Biomes where Remains can generate (empty = all biomes allowed)")
        public String[] remainsBiomeWhitelist = new String[0];

        @Config.Name("Remains Biome Blacklist")
        @Config.Comment("Biomes where Remains cannot generate")
        public String[] remainsBiomeBlacklist = new String[0];

        @Config.Name("Potion Crate Frequency")
        @Config.Comment("How many Potion Crate blocks to generate per chunk (default: 700)")
        public int potionCrateFrequency = 700;

        @Config.Name("Potion Crate Biome Whitelist")
        @Config.Comment("Biomes where Potion Crate can generate (empty = all biomes allowed)")
        public String[] potionCrateBiomeWhitelist = new String[0];

        @Config.Name("Potion Crate Biome Blacklist")
        @Config.Comment("Biomes where Potion Crate cannot generate")
        public String[] potionCrateBiomeBlacklist = new String[0];

        @Config.Name("Tent Frequency")
        @Config.Comment("How many Tent blocks to generate per chunk (default: 950)")
        public int tentFrequency = 950;

        @Config.Name("Tent Biome Whitelist")
        @Config.Comment("Biomes where Tent can generate (empty = all biomes allowed)")
        public String[] tentBiomeWhitelist = new String[0];

        @Config.Name("Tent Biome Blacklist")
        @Config.Comment("Biomes where Tent cannot generate")
        public String[] tentBiomeBlacklist = new String[0];

        @Config.Name("Abandoned Tent Frequency")
        @Config.Comment("How many Abandoned Tent blocks to generate per chunk (default: 850)")
        public int abandonedTentFrequency = 850;

        @Config.Name("Abandoned Tent Biome Whitelist")
        @Config.Comment("Biomes where Abandoned Tent can generate (empty = all biomes allowed)")
        public String[] abandonedTentBiomeWhitelist = new String[0];

        @Config.Name("Abandoned Tent Biome Blacklist")
        @Config.Comment("Biomes where Abandoned Tent cannot generate")
        public String[] abandonedTentBiomeBlacklist = new String[0];

        @Config.Name("Abandoned Tent With Totem Frequency")
        @Config.Comment("How many Abandoned Tent With Totem blocks to generate per chunk (default: 850)")
        public int abandonedTentWithTotemFrequency = 850;

        @Config.Name("Abandoned Tent With Totem Biome Whitelist")
        @Config.Comment("Biomes where Abandoned Tent With Totem can generate (empty = all biomes allowed)")
        public String[] abandonedTentWithTotemBiomeWhitelist = new String[0];

        @Config.Name("Abandoned Tent With Totem Biome Blacklist")
        @Config.Comment("Biomes where Abandoned Tent With Totem cannot generate")
        public String[] abandonedTentWithTotemBiomeBlacklist = new String[0];

        public int flatSurfaceTolerance = 0;

        @Config.Name("Surface Search Range")
        @Config.Comment("How many blocks to search vertically when finding a suitable surface for structures. Default: 32")
        @Config.RangeInt(min = 8, max = 128)
        public int surfaceSearchRange = 32;

        @Config.Name("Reject Liquid in 3x3 Area")
        @Config.Comment("If true, structures won't spawn if any liquid blocks are present in the 3x3 placement area. Default: true")
        public boolean rejectLiquidIn3x3 = true;
    }

    public static class MiscSettings {

		@Config.Name("Grave Rose Chance")
		@Config.Comment("The chance of a rose turning into a grave rose when it is placed in a grave marker.")
		@Config.RequiresMcRestart
		public double graveRoseChance = 0.05D;

		@Config.Name("Rune of Skimming Max Teleport Distance")
		@Config.Comment("Maximum distance (in blocks) the Rune of Skimming can teleport the player. Default: 150")
		public int runeOfSkimmingMaxDistance = 150;

		@Config.Name("Rune of Skimming Cooldown")
		@Config.Comment("Cooldown (in ticks) after using the Rune of Skimming. Default: 100 (5 seconds)")
		public int runeOfSkimmingCooldown = 100;

		@Config.Name("Bliss Healing Amount")
		@Config.Comment("The amount of health restored per bliss-tick (0.0 - 1.0, where 1.0 = is one heart).")
		public double blissHealingAmount = 0.33D;

		@Config.Name("Bliss Duration (ticks) for placing a flower on a grave")
		@Config.Comment("The duration of the bliss effect in ticks.")
		public double blissDurationForFlower = 3600;

		@Config.Name("Bliss Duration (ticks) for burying remains")
		@Config.Comment("The duration of the bliss effect in ticks.")
		public double blissDurationForBurying = 1600;

		@Config.Name("Haunting Gained by Breaking Remains")
		@Config.Comment("The amount of haunting gained by breaking remains.")
		public int hauntingGainedByBreakingRemains = 5;

		@Config.Name("Haunting Gained by Breaking Grave")
		@Config.Comment("The amount of haunting gained by breaking a grave.")
		public int hauntingGainedByBreakingGrave = 2;

		@Config.Name("Haunting Reduced by Burying Remains")
		@Config.Comment("The amount of haunting reduced by burying remains.")
		public int hauntingReducedByBuryingRemains = 5;

		@Config.Name("Haunting Reduced by Placing a Flower on a Grave")
		@Config.Comment("The amount of haunting reduced by placing a flower on a grave.")
		public int hauntingReducedByPlacingFlowerOnGrave = 2;

		@Config.Name("Goblin Group Hostility Threshold")
		@Config.Comment("When this many or more goblins are nearby, they will ignore active goblin idols and become hostile. Set to 0 to disable group behavior. Default: 6")
		public int goblinGroupHostilityThreshold = 6;

		@Config.Name("Bundle of Lost Letters XP Amount")
		@Config.Comment("The amount of XP granted when using a Bundle of Lost Letters. Default: 300")
		public int bundleOfLostLettersXP = 300;

		@Config.Name("Bundle of Lost Letters Bliss Duration")
		@Config.Comment("The duration of the bliss effect in ticks when using a Bundle of Lost Letters. Default: 600 (30 seconds)")
		public int bundleOfLostLettersBlissDuration = 600;

		@Config.Name("Bundle of Lost Letters Cooldown")
		@Config.Comment("Cooldown (in ticks) after using a Bundle of Lost Letters. Default: 6000 (5 minutes)")
		public int bundleOfLostLettersCooldown = 6000;

		@Config.Name("Wonder Fertilizer Radius")
		@Config.Comment("The radius in blocks for the Wonder Fertilizer's bonemeal effect. Default: 3 (creates a 7x7 area)")
		public int wonderFertilizerRadius = 3;

		@Config.Name("Wonder Fertilizer Durability")
		@Config.Comment("The number of uses for the Wonder Fertilizer. Default: 2")
		@Config.RequiresMcRestart
		public int wonderFertilizerDurability = 2;

		@Config.Name("Old World Tinker's Kit Durability Restore Percent")
		@Config.Comment("The percentage of max durability restored by the Old World Tinker's Kit (0.0 - 1.0, where 1.0 = 100%). Default: 0.10 (10%)")
		public double oldWorldTinkersKitRestorePercent = 0.10D;

		@Config.Name("Silk Spindle String Amount")
		@Config.Comment("The amount of string given when using a Silk Spindle. Default: 32")
		public int silkSpindleStringAmount = 32;

		@Config.Name("Tent Goblin Detection Range")
		@Config.Comment("The range in blocks for tents to detect nearby players and spawn goblins. Default: 4")
		public double tentGoblinDetectionRange = 4.0D;

		@Config.Name("Tent Goblin Min Count")
		@Config.Comment("Minimum number of goblins spawned by tents. Default: 1")
		public int tentGoblinMinCount = 1;

		@Config.Name("Tent Goblin Max Count")
		@Config.Comment("Maximum number of goblins spawned by tents. Default: 3")
		public int tentGoblinMaxCount = 3;

		@Config.Name("Bush Crate Goblin Spawn Chance")
		@Config.Comment("Chance (0.0 - 1.0) for bush crates to spawn goblins when a player is nearby. 0.0 = never, 1.0 = always. Default: 0.5 (50%)")
		public double bushCrateGoblinSpawnChance = 0.5D;

		@Config.Name("Veiled Mask Durability")
		@Config.Comment("The total durability of the Veiled Mask. Default: 500")
		@Config.RequiresMcRestart
		public int veiledMaskDurability = 500;

		@Config.Name("Veiled Mask Haunting Amount")
		@Config.Comment("The amount of haunting gained each time while wearing the Veiled Mask. Default: 1")
		public int veiledMaskHauntingAmount = 1;

		@Config.Name("Veiled Mask Haunting Tick Rate")
		@Config.Comment("How often (in ticks) the Veiled Mask gains haunting and consumes durability. Default: 60 (3 seconds)")
		public int veiledMaskHauntingTickRate = 60;
    }

    public static class ClientSettings {
    }

    @SuppressWarnings("unused")
    @Mod.EventBusSubscriber(modid = TracesOfTheFallen.MODID)
    private static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(TracesOfTheFallen.MODID)) {
                ConfigManager.sync(TracesOfTheFallen.MODID, Config.Type.INSTANCE);
            }
        }
    }
}