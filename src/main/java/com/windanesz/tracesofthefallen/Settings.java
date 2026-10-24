package com.windanesz.tracesofthefallen;

import com.windanesz.tracesofthefallen.block.TileEntityBrassFabricator;
import com.windanesz.tracesofthefallen.block.TileEntityPorcelainVessel;
import com.windanesz.tracesofthefallen.capability.HauntingCapability;
import com.windanesz.tracesofthefallen.entity.ai.GoblinAITunnelerDig;
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
    public List<ResourceLocation> bushWithCrateBiomeWhitelist = Arrays.asList(toResourceLocations(worldgenSettings.bushWithCrateBiomeWhitelist));
    public List<ResourceLocation> bushWithCrateBiomeBlacklist = Arrays.asList(toResourceLocations(worldgenSettings.bushWithCrateBiomeBlacklist));
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

	@Config.Name("Goblin Settings")
	@Config.LangKey("settings.totf:general_settings")
	public static GoblinSettings goblinSettings = new GoblinSettings();

	@Config.Name("Mob Settings")
	@Config.LangKey("settings.totf:general_settings")
	public static MobSettings mobSettings = new MobSettings();

	@Config.Name("Haunting Settings")
	@Config.LangKey("settings.totf:general_settings")
	public static HauntingSettings hauntingSettings = new HauntingSettings();

    public static class WorldgenSettings {

        @Config.Name("Lost Loot Dimensions")
        @Config.Comment("[Server-only] List of dimension ids where loot spawns.")
        @Config.RequiresMcRestart
        public int[] dimensionList = {0};

        @Config.Name("Lost Cargo Frequency")
        @Config.Comment("How many Lost Cargo blocks to generate per chunk (default: 100)")
        public int lostCargoFrequency = 100;

        @Config.Name("Lost Cargo Biome Whitelist")
        @Config.Comment("Biomes where Lost Cargo can generate (empty = all biomes allowed)")
        public String[] lostCargoBiomeWhitelist = new String[0];

        @Config.Name("Lost Cargo Biome Blacklist")
        @Config.Comment("Biomes where Lost Cargo cannot generate")
        public String[] lostCargoBiomeBlacklist = new String[0];

        @Config.Name("Stone Circle Chance")
        @Config.Comment("Chance for a Stone Circle to generate in a chunk. 1 in X chance. Set to 0 to disable. Default: 600")
		@Config.RangeInt(min = 100)
		public int stoneCircleChance = 600;

        @Config.Name("Stone Circle Biome Whitelist")
        @Config.Comment("Biomes where Stone Circle can generate (empty = all biomes allowed)")
        public String[] stoneCircleBiomeWhitelist = new String[0];

        @Config.Name("Stone Circle Biome Blacklist")
        @Config.Comment("Biomes where Stone Circle cannot generate")
        public String[] stoneCircleBiomeBlacklist = new String[0];

        @Config.Name("Remains Chance")
        @Config.Comment("Chance for (skeletal) Remains to generate in a chunk. 1 in X chance. Set to 0 to disable. Default: 300")
        public int remainsChance = 300;

        @Config.Name("Remains Biome Whitelist")
        @Config.Comment("Biomes where Remains can generate (empty = all biomes allowed)")
        public String[] remainsBiomeWhitelist = new String[0];

        @Config.Name("Remains Biome Blacklist")
        @Config.Comment("Biomes where Remains cannot generate")
        public String[] remainsBiomeBlacklist = new String[0];

        @Config.Name("Potion Crate Frequency")
        @Config.Comment("How many Potion Crate blocks to generate per chunk (default: 300)")
        public int potionCrateFrequency = 300;

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

        @Config.Name("Bush With Crate Frequency")
        @Config.Comment("How many Bush With Crate blocks to generate per chunk (default: 850)")
        public int bushWithCrateFrequency = 850;

        @Config.Name("Bush With Crate Biome Whitelist")
        @Config.Comment("Biomes where Bush With Crate can generate (empty = all biomes allowed)")
        public String[] bushWithCrateBiomeWhitelist = new String[0];

        @Config.Name("Bush With Crate Biome Blacklist")
        @Config.Comment("Biomes where Bush With Crate cannot generate")
        public String[] bushWithCrateBiomeBlacklist = new String[0];

        @Config.Name("Beached Glass Float Chance")
        @Config.Comment("1 in N chance per chunk for a beached glass float to generate on sand/gravel in beach biomes (0 = disabled, default: 6)")
        public int beachedGlassFloatChance = 6;

        @Config.Name("Abandoned Tent Frequency")
        @Config.Comment("How many Abandoned Tent blocks to generate per chunk (default: 250)")
        public int abandonedTentFrequency = 250;

        @Config.Name("Abandoned Tent Biome Whitelist")
        @Config.Comment("Biomes where Abandoned Tent can generate (empty = all biomes allowed)")
        public String[] abandonedTentBiomeWhitelist = new String[0];

        @Config.Name("Abandoned Tent Biome Blacklist")
        @Config.Comment("Biomes where Abandoned Tent cannot generate")
        public String[] abandonedTentBiomeBlacklist = new String[0];

        @Config.Name("Abandoned Tent With Totem Frequency")
        @Config.Comment("How many Abandoned Tent With Totem blocks to generate per chunk (default: 300)")
        public int abandonedTentWithTotemFrequency = 300;

        @Config.Name("Abandoned Tent With Totem Biome Whitelist")
        @Config.Comment("Biomes where Abandoned Tent With Totem can generate (empty = all biomes allowed)")
        public String[] abandonedTentWithTotemBiomeWhitelist = new String[0];

        @Config.Name("Abandoned Tent With Totem Biome Blacklist")
        @Config.Comment("Biomes where Abandoned Tent With Totem cannot generate")
        public String[] abandonedTentWithTotemBiomeBlacklist = new String[0];

        @Config.Name("Surface Search Range")
        @Config.Comment("How many blocks to search vertically when finding a suitable surface for structures. Default: 32")
        @Config.RangeInt(min = 8, max = 128)
        public int surfaceSearchRange = 32;

        @Config.Name("Reject Liquid in 3x3 Area")
        @Config.Comment("If true, structures won't spawn if any liquid blocks are present in the 3x3 placement area. Default: true")
        public boolean rejectLiquidIn3x3 = true;

		@Config.Name("Multipart Structure Min Y")
		@Config.Comment("Lowest Y level for the multipart structure's floor. Default: 20")
		@Config.RangeInt(min = 1, max = 245)
		public int multipartStructureMinY = 20;

		@Config.Name("Multipart Structure Max Y")
		@Config.Comment("Highest Y level for the multipart structure's floor. Default: 45")
		@Config.RangeInt(min = 1, max = 245)
		public int multipartStructureMaxY = 40;

		@Config.Name("Trap Room Chance")
		@Config.Comment("Chance (0.0 - 1.0) for an eligible corridor piece in the multipart dungeon to generate a row of Guillotine traps triggered by hidden pressure plates in the floor. Default: 0.2 (20%)")
		@Config.RangeDouble(min = 0.0, max = 1.0)
		public double trapRoomChance = 0.2D;

		@Config.Name("Dungeon Gnossic Spawn Chance")
		@Config.Comment("Chance (0.0 - 1.0) for each eligible multipart dungeon room to contain one persistent Gnossic. Default: 0.1 (10%)")
		@Config.RangeDouble(min = 0.0, max = 1.0)
		public double dungeonGnossicSpawnChance = 0.1D;

		@Config.Name("Dungeon Gnossic Mask Chance")
		@Config.Comment("Chance (0.0 - 1.0) for each gate of the pots room in the multipart dungeon to have a Gnossic Mask hanging on its outer side. Default: 0.5 (50%)")
		@Config.RangeDouble(min = 0.0, max = 1.0)
		public double dungeonGnossicMaskChance = 0.5D;

		@Config.Name("Minecrawler Mineshaft Frequency")
		@Config.Comment("1-in-N chance for a new vanilla mineshaft to gain an infested shaft and nest room when space permits; a successful one has a 1-in-3 chance of getting a second. Uses Lost Loot Dimensions. 0 disables; 1 attempts every mineshaft. Default: 2")
		@Config.RangeInt(min = 0)
		public int minecrawlerMineshaftFrequency = 2;

		@Config.Name("Minecrawler Brood Shaft Frequency")
		@Config.Comment("1-in-N chance for a new vanilla mineshaft to gain a brood shaft: a passage ending at a deep vertical shaft with a spiral ledge down to a Minecrawler brood chamber. Rolled independently of the nest. Uses Lost Loot Dimensions. 0 disables; 1 attempts every mineshaft. Default: 4")
		@Config.RangeInt(min = 0)
		public int minecrawlerBroodShaftFrequency = 4;

		@Config.Name("Minecrawler Lair Frequency")
		@Config.Comment("1-in-N chance per chunk for a small underground Minecrawler lair to generate. Lower is more common. Default: 360")
		public int minecrawlerLairFrequency = 360;

		@Config.Name("Lost Pits Enabled")
		public boolean lostPitsEnabled = true;

		@Config.Name("Lost Pit Frequency")
		@Config.Comment("1-in-N generation attempt per new Overworld chunk. Unsuitable sites are skipped. 0 disables. Does not change natural Lost spawning.")
		@Config.RangeInt(min = 0)
		public int lostPitFrequency = 600;

		@Config.Name("Lost Pit Min Y")
		@Config.RangeInt(min = 4, max = 240)
		public int lostPitMinY = 4;

		@Config.Name("Lost Pit Max Y")
		@Config.Comment("Inclusive floor depth limits; chambers must also fit beneath local terrain.")
		@Config.RangeInt(min = 4, max = 240)
		public int lostPitMaxY = 240;

		@Config.Name("Lost Pit Surface Chance")
		@Config.RangeDouble(min = 0, max = 1)
		public double lostPitSurfaceChance = 0.1D;

		@Config.Name("Lost Pit Ledge Chance")
		@Config.Comment("Chance an ordinary pit has a walkable descent instead of a sheer drop.")
		@Config.RangeDouble(min = 0, max = 1)
		public double lostPitLedgeChance = 0.5D;

		@Config.Name("Lost Pit Pair Chance")
		@Config.Comment("Chance of two persistent Lost instead of one. No special respawning or AI changes.")
		@Config.RangeDouble(min = 0, max = 1)
		public double lostPitPairChance = 0.2D;

		@Config.Name("Minecrawler Lair Min Y")
		@Config.Comment("Lowest Y level a Minecrawler lair's floor can generate at. Default: 15")
		@Config.RangeInt(min = 1, max = 245)
		public int minecrawlerLairMinY = 15;

		@Config.Name("Minecrawler Lair Max Y")
		@Config.Comment("Highest Y level a Minecrawler lair's floor can generate at. Default: 45")
		@Config.RangeInt(min = 1, max = 245)
		public int minecrawlerLairMaxY = 45;

		@Config.Name("Minecrawler Lair Min Crawlers")
		@Config.Comment("Minimum number of persistent Minecrawlers spawned in a lair. Default: 2")
		@Config.RangeInt(min = 0, max = 16)
		public int minecrawlerLairMinCrawlers = 2;

		@Config.Name("Minecrawler Lair Max Crawlers")
		@Config.Comment("Maximum number of persistent Minecrawlers spawned in a lair. Default: 3")
		@Config.RangeInt(min = 0, max = 16)
		public int minecrawlerLairMaxCrawlers = 3;

		@Config.Name("Minecrawler Lair Min Cocoons")
		@Config.Comment("Minimum number of Minecrawler Cocoon loot props placed per lair room when space permits. Default: 3")
		@Config.RangeInt(min = 0, max = 16)
		public int minecrawlerLairMinCocoons = 3;

		@Config.Name("Minecrawler Lair Max Cocoons")
		@Config.Comment("Maximum number of Minecrawler Cocoon loot props placed per lair room. Default: 5")
		@Config.RangeInt(min = 0, max = 16)
		public int minecrawlerLairMaxCocoons = 5;

		@Config.Name("Goblin Cavern Spacing")
		@Config.Comment("Average spacing, in chunks, between underground goblin caverns: one generates per spacing x spacing chunk cell. Higher is rarer; 0 disables. Uses Lost Loot Dimensions. Default: 24")
		@Config.RangeInt(min = 0)
		public int goblinCavernSpacing = 24;

		@Config.Name("Goblin Cavern Min Y")
		@Config.Comment("Lowest Y level for the bottom of a goblin cavern's stone shell. The shell is 11 blocks tall. Default: 12")
		@Config.RangeInt(min = 1, max = 245)
		public int goblinCavernMinY = 12;

		@Config.Name("Goblin Cavern Max Y")
		@Config.Comment("Highest Y level for the bottom of a goblin cavern's stone shell. Keep this low enough that the shell stays below the surface. Default: 32")
		@Config.RangeInt(min = 1, max = 245)
		public int goblinCavernMaxY = 32;

		@Config.Name("Goblin Cavern Min Nests")
		@Config.Comment("Minimum number of goblin nests placed in a goblin cavern when space permits. Default: 2")
		@Config.RangeInt(min = 0, max = 16)
		public int goblinCavernMinNests = 2;

		@Config.Name("Goblin Cavern Max Nests")
		@Config.Comment("Maximum number of goblin nests placed in a goblin cavern. Default: 3")
		@Config.RangeInt(min = 0, max = 16)
		public int goblinCavernMaxNests = 3;

		@Config.Name("Goblin Warren Spacing")
		@Config.Comment("Average spacing, in chunks, between underground goblin warrens: one generates per spacing x spacing chunk cell. A warren is an alternative to the goblin cavern: a hall with a works, a vault and a den off its walls, joined by tunnels. Higher is rarer; 0 disables. Default: 32")
		@Config.RangeInt(min = 0)
		public int goblinWarrenSpacing = 32;

		@Config.Name("Goblin Warren Min Y")
		@Config.Comment("Lowest Y level for the bottom of a goblin warren's hall. The vault sits up to 8 blocks lower, so keep this above 16. Default: 20")
		@Config.RangeInt(min = 1, max = 245)
		public int goblinWarrenMinY = 20;

		@Config.Name("Goblin Warren Max Y")
		@Config.Comment("Highest Y level for the bottom of a goblin warren's hall. Keep this low enough that the 11 block tall hall stays below the surface. Default: 32")
		@Config.RangeInt(min = 1, max = 245)
		public int goblinWarrenMaxY = 32;

		@Config.Name("Goblin Warren Min Nests")
		@Config.Comment("Minimum number of goblin nests placed in a goblin warren's hall when space permits. Default: 2")
		@Config.RangeInt(min = 0, max = 16)
		public int goblinWarrenMinNests = 2;

		@Config.Name("Goblin Warren Max Nests")
		@Config.Comment("Maximum number of goblin nests placed in a goblin warren's hall. Default: 3")
		@Config.RangeInt(min = 0, max = 16)
		public int goblinWarrenMaxNests = 3;

		@Config.Name("Goblin Warren Den Chance")
		@Config.Comment("Percent chance a goblin warren has a minecrawler den off its hall. When it has none, that doorway becomes a second way in. Default: 50")
		@Config.RangeInt(min = 0, max = 100)
		public int goblinWarrenDenChance = 50;

		@Config.Name("Goblin Warren Bore Room")
		@Config.Comment("Whether a goblin warren has a diggers' camp, with a tunneler, between its hall and its works. Default: true")
		public boolean goblinWarrenBoreEnabled = true;

		@Config.Name("Goblin Warren Vault")
		@Config.Comment("Whether a goblin warren has an idol vault beyond its works: a room with a brute, a rich stone chest and the idol. Default: true")
		public boolean goblinWarrenVaultEnabled = true;

		@Config.Name("Subterfuge Shop Frequency")
		@Config.Comment("1-in-N chance per chunk to attempt a Subterfuge's shop: a small room dug into the ground, reached by stairs. Lower is more common; 0 disables. Default: 1000")
		@Config.RangeInt(min = 0)
		public int subterfugeShopFrequency = 1000;

		@Config.Name("Subterfuge Shop Presence Chance")
		@Config.Comment({
				"Chance, per presence period, that Subterfuge is in her shop. While she is away the shop is closed and unprotected,",
				"and destroying it (breaking over half of its balances) means she never returns. Default: 0.75"
		})
		@Config.RangeDouble(min = 0.0, max = 1.0)
		public double subterfugeShopPresenceChance = 0.75D;

		@Config.Name("Subterfuge Shop Presence Days")
		@Config.Comment("Length, in in-game days, of one presence period. Whether she is in her shop is decided once per period. Default: 3")
		@Config.RangeInt(min = 1, max = 1000)
		public int subterfugeShopPresenceDays = 3;

		@Config.Name("Subterfuge Shop Protection")
		@Config.Comment("Whether blocks inside a Subterfuge's shop can't be broken, placed or blown up while she is present. Creative players are exempt. Default: true")
		public boolean subterfugeShopProtection = true;

		@Config.Name("Subterfuge Shop Restock Days")
		@Config.Comment("In-game days until a sold-out pedestal in her shop is stocked with a new item. 0 means sold items are never replaced. Default: 2")
		@Config.RangeInt(min = 0, max = 1000)
		public int subterfugeShopRestockDays = 2;

		@Config.Name("Subterfuge Shop Trades")
		@Config.Comment({
				"Items Subterfuge can offer, paid for in Tallies (the Tallie Collector item).",
				"Format: 'modid:item[@meta];amount;price;weight'. Price is in Tallies, weight is the relative chance of the trade being picked.",
				"Entries naming unknown items are skipped. Example: 'minecraft:golden_apple@1;1;12;2'"
		})
		public String[] subterfugeShopTrades = {
				"totf:incense;4;2;10",
				"totf:spirited_away_tea_leaves;3;2;10",
				"totf:pemmican;4;2;8",
				"totf:silk_rope;4;3;8",
				"totf:chilled_gel;2;3;6",
				"totf:wonder_fertilizer;2;4;6",
				"totf:subtorch;4;3;6",
				"totf:zap_charge;1;6;4",
				"totf:glass_float_clear_blue;1;6;4",
				"totf:glass_float_wine_green;1;6;4",
				"totf:glass_float_blood_red;1;8;3",
				"totf:wrought_caged_lamp;1;8;3",
				"totf:living_silver_nugget;2;8;4",
				"totf:incense_burner_gold;1;12;3",
				"totf:bone_rattle;1;10;3",
				"totf:silk_spindle;1;10;3",
				"totf:rune_of_skimming;1;16;3",
				"totf:old_world_tinkers_kit;1;18;2",
				"totf:painting_subterfuge;1;14;2",
				"totf:record_gnossiene_1;1;20;1",
				"totf:ward_of_blades;1;24;2",
				"totf:veiled_mask;1;30;1"
		};
	}

    public static class MiscSettings {
		@Config.Name("Dan Coil Trap Damage")
		@Config.Comment("The amount of damage the Dan Coil trap deals. Default: 3.0")
		@Config.RangeDouble(min = 0.0, max = 10000.0)
		public double dancoilTrapDamage = 3.0D;

		@Config.Name("Dan Coil Ignored Entities")
		@Config.Comment({
				"List of entities that are ignored by the Dan Coil trap.",
				"Format: 'modid:entity_name'"
		})
		public String[] dancoilIgnoredEntities = {
				"totf:lamphead"
		};

		@Config.Name("Bonepile Base Item Chance")
		@Config.Comment("The base chance (0.0 - 1.0) per layer of dropping a bone or bonemeal when breaking a bonepile. Default: 0.10 (10% per layer)")
		@Config.RangeDouble(min = 0.0, max = 1.0)
		public double bonepileBaseItemChance = 0.10D;

		@Config.Name("Bonepile Item Fortune Multiplier")
		@Config.Comment("The relative chance increase per Fortune level for dropping a bone or bonemeal from a bonepile. Default: 0.1333")
		public double bonepileItemFortuneMultiplier = 0.1333D;

		@Config.Name("Bonepile Bonemeal Chance")
		@Config.Comment("When a bonepile drops an item, the chance (0.0 - 1.0) that it is bonemeal instead of a bone. Default: 0.95 (95% bonemeal, 5% bone)")
		@Config.RangeDouble(min = 0.0, max = 1.0)
		public double bonepileBonemealChance = 0.95D;

		@Config.Name("Bonepile Skull Chance Per Layer")
		@Config.Comment("The chance (0.0 - 1.0) per layer of dropping a skeleton skull when breaking a bonepile. Default: 0.00125 (0.125% per layer)")
		@Config.RangeDouble(min = 0.0, max = 1.0)
		public double bonepileSkullChancePerLayer = 0.00125D;

		@Config.Name("Bonepile Skull Fortune Bonus")
		@Config.Comment("The additive chance (0.0 - 1.0) per Fortune level for dropping a skeleton skull from a bonepile. Default: 0.01 (1% per level)")
		@Config.RangeDouble(min = 0.0, max = 1.0)
		public double bonepileSkullFortuneBonus = 0.01D;

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

		@Config.Name("Dioptra Max Distance")
		@Config.Comment("Maximum distance in blocks that the Dioptra can measure along the player's look direction. Default: 512")
		@Config.RangeInt(min = 1, max = 4096)
		public int dioptraMaxDistance = 512;

		@Config.Name("Bliss Healing Amount")
		@Config.Comment("The amount of health restored per bliss-tick (0.0 - 1.0, where 1.0 = is one heart).")
		public double blissHealingAmount = 0.33D;

		@Config.Name("Bliss Duration (ticks) for placing a flower on a grave")
		@Config.Comment("The duration of the bliss effect in ticks.")
		public double blissDurationForFlower = 3600;

		@Config.Name("Bliss Duration (ticks) for burying remains")
		@Config.Comment("The duration of the bliss effect in ticks.")
		public double blissDurationForBurying = 1600;


		@Config.Name("Goblin Group Hostility Threshold")
		@Config.Comment("When this many or more goblins are nearby, they will ignore active goblin idols and become hostile. Set to 0 to disable group behavior. Default: 6")
		public int goblinGroupHostilityThreshold = 6;

		@Config.Name("Goblin Nest Health")
		@Config.Comment("The amount of health the goblin nest has before it is destroyed. Default: 50")
		public int nestHealth = 50;

		@Config.Name("Bundle of Lost Letters XP Amount")
		@Config.Comment("The amount of XP granted when using a Bundle of Lost Letters. Default: 300")
		public int bundleOfLostLettersXP = 300;

		@Config.Name("Bundle of Lost Letters Bliss Duration")
		@Config.Comment("The duration of the bliss effect in ticks when using a Bundle of Lost Letters. Default: 600 (30 seconds)")
		public int bundleOfLostLettersBlissDuration = 600;

		@Config.Name("Bundle of Lost Letters Cooldown")
		@Config.Comment("Cooldown (in ticks) after using a Bundle of Lost Letters. Default: 6000 (5 minutes)")
		public int bundleOfLostLettersCooldown = 6000;

		@Config.Name("Incense Serenity Radius")
		@Config.Comment("Horizontal radius in blocks for burning incense to apply Serenity. Default: 2 (creates a 5x5 area)")
		@Config.RangeInt(min = 0, max = 16)
		public int incenseSerenityRadius = 2;

		@Config.Name("Incense Burn Duration Min")
		@Config.Comment("Minimum burn duration in ticks for incense. Default: 6000 (5 minutes)")
		@Config.RangeInt(min = 20, max = 72000)
		public int incenseBurnDurationMin = 6000;

		@Config.Name("Incense Burn Duration Max")
		@Config.Comment("Maximum burn duration in ticks for incense. Default: 12000 (10 minutes)")
		@Config.RangeInt(min = 20, max = 72000)
		public int incenseBurnDurationMax = 12000;

		@Config.Name("Loose Incense Burn Duration")
		@Config.Comment("Burn time in ticks added to censers and incense burners by one loose incense item. Default: 6000 (5 minutes)")
		@Config.RangeInt(min = 20, max = 72000)
		public int incenseItemBurnDuration = 6000;

		@Config.Name("Incense Stick Burn Duration")
		@Config.Comment("Burn time in ticks added to censers and incense burners by one incense stick. Default: 12000 (10 minutes)")
		@Config.RangeInt(min = 20, max = 72000)
		public int incenseStickBurnDuration = 12000;

		@Config.Name("Skeleton Flower Aura Effects")
		@Config.Comment({
				"List of potion effects applied every second to living entities within radius of a totf:skeleton_flower block.",
				"Format per entry:",
				"<potion_effect>|<amplifier>|<duration_ticks>",
				"Examples:",
				"minecraft:poison|1|120",
				"totf:serenity|2|60"
		})
		public String[] skeletonFlowerAuraEffects = {
				"minecraft:poison|1|120"
		};

		@Config.Name("Incense Potion Effect Whitelist")
		@Config.Comment("Potion registry names that can be infused into burning censers and incense burners.")
		public String[] incensePotionEffectWhitelist = {
				"totf:bliss",
				"totf:serenity",
				"minecraft:speed",
				"minecraft:night_vision",
				"minecraft:regeneration",
				"minecraft:strength",
				"minecraft:poison",
				"minecraft:water_breathing",
				"minecraft:slowness",
				"minecraft:invisibility",
				"minecraft:jump_boost"
		};

		@Config.Name("Porcelain Pot Tea Leaves Required")
		@Config.Comment("How many Spirited Away Tea Leaves are required to brew a pot of tea. Default: 1")
		@Config.RangeInt(min = 1, max = 64)
		public int porcelainPotTeaLeavesRequired = 1;

		@Config.Name("Porcelain Pot Tea Servings")
		@Config.Comment("How many cups of tea a brewed porcelain pot can pour before emptying. Default: 3")
		@Config.RangeInt(min = 1, max = 16)
		public int porcelainPotTeaServings = 3;

		@Config.Name("Porcelain Liquid Definitions")
		@Config.Comment({
				"List of custom porcelain liquids.",
				"Format per entry:",
				"<liquid_type>|<accepted_item>|<forge_fluid>|<color_hex>|<potion_effect>|<amplifier>|<duration_ticks>",
				"Examples:",
				"tea|totf:spirited_away_tea_leaves|water|#891516|minecraft:regeneration|0|300",
				"hot_chocolate|minecraft:dye@3|milk|#5A341A|minecraft:speed|0|900",
				"Note: hot_chocolate additionally requires milk bucket in the pot."
		})
		public String[] porcelainLiquidDefinitions = {
				"tea|totf:spirited_away_tea_leaves|water|#891516|minecraft:regeneration|0|300",
				"hot_chocolate|minecraft:dye@3|milk|#5A341A|minecraft:speed|0|900",
				"living_silver_tea|totf:living_silver_nugget|water|#A9B2C3|minecraft:haste|0|1200"
		};

		@Config.Name("Porcelain Heat Sources Map")
		@Config.Comment("Map entries of block registry names (or domain/path substrings) to their active boolean property state (or '*' for any). Format: 'modid:regname,state'. Examples: 'exsartagine:hearth,heated', 'exsartagine:range,heated', 'pyrotech:campfire,burning'")
		public String[] porcelainHeatSources = {
				"exsartagine:hearth,heated",
				"exsartagine:range,heated"
		};

		@Config.Name("Wonder Fertilizer Radius")
		@Config.Comment("The radius in blocks for the Wonder Fertilizer's bonemeal effect. Default: 3 (creates a 7x7 area)")
		@Config.RangeInt(min = 1, max = 16)
		public int wonderFertilizerRadius = 3;

		@Config.Name("Wonder Fertilizer Durability")
		@Config.Comment("The number of uses for the Wonder Fertilizer. Default: 2")
		@Config.RangeInt(min = 1, max = 256)
		@Config.RequiresMcRestart
		public int wonderFertilizerDurability = 2;

		@Config.Name("Old World Tinker's Kit Durability Restore Percent")
		@Config.Comment("The percentage of max durability restored by the Old World Tinker's Kit (0.0 - 1.0, where 1.0 = 100%). Default: 0.10 (10%)")
		@Config.RangeDouble(min = 0.01D, max = 1.0D)
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

		@Config.Name("Nest Goblin Detection Range")
		@Config.Comment("The range in blocks for goblin nests to detect nearby players and spawn goblins. Default: 5")
		public double nestGoblinDetectionRange = 5.0D;

		@Config.Name("Nest Goblin Min Count")
		@Config.Comment("Minimum number of goblins spawned by goblin nests. Default: 2")
		public int nestGoblinMinCount = 2;

		@Config.Name("Nest Goblin Max Count")
		@Config.Comment("Maximum number of goblins spawned by goblin nests. Default: 4")
		public int nestGoblinMaxCount = 4;

		@Config.Name("Nest Rock Attack Range")
		@Config.Comment("The range in blocks for goblin nests to shoot rocks at nearby players. Default: 12")
		public double nestRockAttackRange = 12.0D;

		@Config.Name("Glass Float Max Rope Length")
		@Config.Comment("Maximum number of rope segments that can be added to a glass float. Default: 6")
		@Config.RangeInt(min = 1, max = 64)
		public int glassFloatMaxRopeLength = 6;

		@Config.Name("Glass Float Min Catch Time")
		@Config.Comment("Minimum time in SECONDS before an empty hook on a glass float catches something. Default: 20")
		@Config.RangeInt(min = 1, max = 3600)
		public int glassFloatMinCatchSeconds = 20;

		@Config.Name("Glass Float Max Catch Time")
		@Config.Comment("Maximum time in SECONDS before an empty hook on a glass float catches something. Default: 40")
		@Config.RangeInt(min = 1, max = 3600)
		public int glassFloatMaxCatchSeconds = 40;

		@Config.Name("Glass Float Furred Trout Chance")
		@Config.Comment("Chance (0.0 - 1.0) that a glass float catch is a Furred Trout instead of regular fishing loot. This is the only way to obtain it. Default: 0.01 (as rare as an enchanted book from vanilla fishing: 5% treasure x 1/5)")
		@Config.RangeDouble(min = 0.0D, max = 1.0D)
		public double glassFloatFurredTroutChance = 0.01D;

		@Config.Name("Bush Crate Goblin Spawn Chance")
		@Config.Comment("Chance (0.0 - 1.0) for bush crates to spawn goblins when a player is nearby. 0.0 = never, 1.0 = always. Default: 0.5 (50%)")
		public double bushCrateGoblinSpawnChance = 0.5D;

		@Config.Name("Abandoned Tent Lost Chance")
		@Config.Comment("Chance (0.0 - 1.0) for an abandoned tent to spawn a single Lost instead of its starved goblins when a player approaches. 0.0 = never. Default: 0.1 (10%)")
		@Config.RangeDouble(min = 0.0D, max = 1.0D)
		public double abandonedTentLostChance = 0.1D;

		@Config.Name("Veiled Mask Durability")
		@Config.Comment("The total durability of the Veiled Mask. Default: 500")
		@Config.RequiresMcRestart
		public int veiledMaskDurability = 500;


		@Config.Name("Chitin Armor Durability")
		@Config.Comment("Durability multiplier for the Chitin armor material. Iron is 15, Diamond is 33. Default: 24")
		@Config.RequiresMcRestart
		public int chitinArmorDurability = 24;

		@Config.Name("Chitin Armor Helmet Protection")
		@Config.Comment("Armor points for the Chitin Helmet. Iron is 2, Diamond is 3. Default: 2")
		@Config.RequiresMcRestart
		public int chitinArmorProtectionHelmet = 2;

		@Config.Name("Chitin Armor Chestplate Protection")
		@Config.Comment("Armor points for the Chitin Chestplate. Iron is 6, Diamond is 8. Default: 7")
		@Config.RequiresMcRestart
		public int chitinArmorProtectionChestplate = 7;

		@Config.Name("Chitin Armor Leggings Protection")
		@Config.Comment("Armor points for the Chitin Leggings. Iron is 5, Diamond is 6. Default: 6")
		@Config.RequiresMcRestart
		public int chitinArmorProtectionLeggings = 6;

		@Config.Name("Chitin Armor Boots Protection")
		@Config.Comment("Armor points for the Chitin Boots. Iron is 2, Diamond is 3. Default: 2")
		@Config.RequiresMcRestart
		public int chitinArmorProtectionBoots = 2;

		@Config.Name("Chitin Armor Enchantability")
		@Config.Comment("Enchantability for the Chitin armor material. Iron is 9, Diamond is 10. Default: 10")
		@Config.RequiresMcRestart
		public int chitinArmorEnchantability = 10;

		@Config.Name("Chitin Armor Toughness")
		@Config.Comment("Armor toughness for the Chitin armor material. Iron is 0.0, Diamond is 2.0. Default: 1.0")
		@Config.RequiresMcRestart
		public double chitinArmorToughness = 1.0D;

		@Config.Name("Chitin Armor Minecrawler Stealth Radius")
		@Config.Comment("Distance in blocks where full Chitin armor starts hiding players from minecrawler detection. 0 disables this behavior. Default: 8")
		@Config.RangeInt(min = 0)
		public int chitinArmorMinecrawlerStealthRadius = 8;

		@Config.Name("Idol of Blades Slowness Duration")
		@Config.Comment("Duration (in ticks) of the slowness effect applied by the Idol of Blades. Default: 100 (5 seconds)")
		public int idolOfBladesSlownessDuration = 100;

		@Config.Name("Idol of Blades Damage")
		@Config.Comment("Damage dealt by the Idol of Blades to entities in range. Default: 1.0 (half a heart)")
		public double idolOfBladesDamage = 1.0D;

		@Config.Name("Idol of Blades Cooldown")
		@Config.Comment("Cooldown (in ticks) after using the Idol of Blades. Default: 2400 (2 minutes)")
		public int idolOfBladesCooldown = 2400;

		@Config.Name("Idol of Blades Durability")
		@Config.Comment("Maximum durability of the Idol of Blades. Default: 500")
		@Config.RequiresMcRestart
		public int idolOfBladesDurability = 500;

		@Config.Name("Sifter Inventory Slots")
		@Config.Comment("How many inventory slots each ancestral sifter has. Default: 8")
		@Config.RangeInt(min = 1, max = 54)
		public int sifterInventorySlots = 8;

		@Config.Name("Sifter Durability")
		@Config.Comment("How many successful sifted loot stacks an ancestral sifter can produce before it becomes broken. Default: 16")
		@Config.RangeInt(min = 1)
		public int sifterDurability = 16;

		@Config.Name("Sifter Health")
		@Config.Comment("How many hits an ancestral sifter can take before it drops itself. Default: 3")
		@Config.RangeInt(min = 1)
		public int sifterHealth = 3;

		@Config.Name("Sifter Loot Interval")
		@Config.Comment("How often an ancestral sifter rolls its environment-based loot table, in ticks. Default: 600")
		@Config.RangeInt(min = 20)
		public int sifterLootInterval = 600;

		@Config.Name("Sifters Per Chunk")
		@Config.Comment("Maximum number of ancestral sifters that can be placed in a single chunk. Default: 1")
		@Config.RangeInt(min = 1)
		public int siftersPerChunk = 1;


		@Config.Name("Wrought Bomb Fuse Time")
		@Config.Comment("The fuse duration (in ticks) for the Wrought Bomb when primed or lit. Default: 200 (10 seconds)")
		@Config.RangeInt(min = 1, max = 6000)
		public int wroughtBombFuseTime = 200;

		@Config.Name("Tunneler Global Block Break Time")
		@Config.Comment("Base block break time (in ticks) per unit of block hardness when mined by a goblin tunneler. Default: 30")
		@Config.RangeInt(min = 1, max = 10000)
		public int tunnelerGlobalBlockBreakTime = 30;

		@Config.Name("Tunneler Block Break Time Overrides")
		@Config.Comment("List of per-block break time overrides (in ticks) for goblin tunnelers. Format: 'modid:blockname:ticks' or 'modid:blockname:meta:ticks'. Examples: 'minecraft:obsidian:200', 'minecraft:stone:0:15'. If set, overrides the base hardness time.")
		public String[] tunnelerBlockBreakTimeOverrides = new String[0];

		@Config.Name("Tunneler Tool Progression Max Mining Level")
		@Config.Comment("If set (e.g. '3'), goblin tunnelers will only be allowed to break blocks with a mining/harvest level less than or equal to this value (e.g. <= 3). When Tool Progression mod is installed, this checks the block mining levels configured by Tool Progression. Leave empty ('') to disable this restriction and allow breaking any mineable block.")
		public String tunnelerToolProgressionMaxMiningLevel = "";

		@Config.Name("Fetid Dagger Melee Damage")
		@Config.Comment("Base melee attack damage dealt by the Fetid Dagger. Default: 4.0")
		@Config.RangeDouble(min = 0.0D, max = 1000.0D)
		public double fetidDaggerMeleeDamage = 4.0D;

		@Config.Name("Fetid Dagger Thrown Damage")
		@Config.Comment("Base projectile damage dealt by the Fetid Dagger when thrown. Default: 4.0")
		@Config.RangeDouble(min = 0.0D, max = 1000.0D)
		public double fetidDaggerThrownDamage = 4.0D;

		@Config.Name("Shaman Fire Orb Damage")
		@Config.Comment("Base damage dealt by the Goblin Shaman's Fire Orb spell. Default: 4.0")
		@Config.RangeDouble(min = 0.0D, max = 1000.0D)
		public double shamanFireOrbDamage = 4.0D;

		@Config.Name("Shaman Fire Orb Ignition Duration")
		@Config.Comment("The duration (in seconds) that targets are set on fire by the Goblin Shaman's Fire Orb spell. Default: 4")
		@Config.RangeInt(min = 0, max = 1000)
		public int shamanFireOrbIgnitionDuration = 4;

		@Config.Name("Shaman Heat Wave Damage")
		@Config.Comment("Base fire damage dealt by the Goblin Shaman's Heat Wave spell. Default: 3.0")
		@Config.RangeDouble(min = 0.0D, max = 1000.0D)
		public double shamanHeatWaveDamage = 3.0D;

		@Config.Name("Shaman Heat Wave Ignition Duration")
		@Config.Comment("The duration (in seconds) that targets are set on fire by the Goblin Shaman's Heat Wave spell. Default: 4")
		@Config.RangeInt(min = 0, max = 1000)
		public int shamanHeatWaveIgnitionDuration = 4;

		@Config.Name("Shaman Chill Pulse Damage")
		@Config.Comment("Base frost/magic damage dealt by the Goblin Shaman's Chill Pulse spell. Default: 5.0")
		@Config.RangeDouble(min = 0.0D, max = 1000.0D)
		public double shamanChillPulseDamage = 5.0D;

		@Config.Name("Shaman Will O' Wisp Damage")
		@Config.Comment("Base damage dealt by the Goblin Shaman's Will O' Wisp spell. Default: 6.0")
		@Config.RangeDouble(min = 0.0D, max = 1000.0D)
		public double shamanWillOWispDamage = 7.0D;

		@Config.Name("Shaman Seeking Orb Damage")
		@Config.Comment("Base damage dealt when the Goblin Shaman's Seeking Orb shocks nearby targets. Default: 8.0")
		@Config.RangeDouble(min = 0.0D, max = 1000.0D)
		public double shamanSeekingOrbDamage = 8.0D;

		@Config.Name("Shaman Void Slash Damage")
		@Config.Comment("Base damage dealt when the Goblin Shaman's Void Slash projectile hits targets. Default: 12.0")
		@Config.RangeDouble(min = 0.0D, max = 1000.0D)
		public double shamanVoidSlashDamage = 12.0D;

		@Config.Name("Shaman Magma Blast Damage")
		@Config.Comment("Base explosion damage dealt by the Goblin Shaman's Magma Blast spell. Default: 8.0")
		@Config.RangeDouble(min = 0.0D, max = 1000.0D)
		public double shamanMagmaBlastDamage = 8.0D;

		@Config.Name("Shaman Magma Blast Contact Damage")
		@Config.Comment("Continuous contact damage dealt while standing in the Magma Blast pool. Default: 3.0")
		@Config.RangeDouble(min = 0.0D, max = 1000.0D)
		public double shamanMagmaBlastContactDamage = 3.0D;

		@Config.Name("Shaman Magma Blast Ignition Duration")
		@Config.Comment("The duration (in seconds) that targets are set on fire by the Goblin Shaman's Magma Blast explosion and pool. Default: 5")
		@Config.RangeInt(min = 0, max = 1000)
		public int shamanMagmaBlastIgnitionDuration = 5;

		@Config.Name("Bone Rattle Max Uses")
		@Config.Comment("Maximum durability (uses) for the Bone Rattle charm. Default: 16")
		@Config.RangeInt(min = 1, max = 10000)
		public int boneRattleMaxUses = 16;

		@Config.Name("Crooked Bone Melee Damage")
		@Config.Comment("Base melee attack damage dealt by the Crooked Bone. Default: 2.0")
		@Config.RangeDouble(min = 0.0D, max = 1000.0D)
		public double crookedBoneDamage = 2.0D;

		@Config.Name("Primitive Mace Durability")
		@Config.Comment("Max durability (uses) for the Primitive Mace. Default: 50")
		@Config.RangeInt(min = 1, max = 10000)
		public int primitiveMaceDurability = 50;

		@Config.Name("Primitive Mace Melee Damage")
		@Config.Comment("Base melee attack damage dealt by the Primitive Mace. Default: 4.0")
		@Config.RangeDouble(min = 0.0D, max = 1000.0D)
		public double primitiveMaceDamage = 4.0D;

		@Config.Name("Brass Club Durability")
		@Config.Comment("Max durability (uses) for the Brass Club. Default: 180")
		@Config.RangeInt(min = 1, max = 10000)
		public int brassClubDurability = 180;

		@Config.Name("Brass Club Melee Damage")
		@Config.Comment("Base melee attack damage dealt by the Brass Club. Default: 4.5")
		@Config.RangeDouble(min = 0.0D, max = 1000.0D)
		public double brassClubDamage = 4.5D;

		@Config.Name("Cleaner Durability")
		@Config.Comment("The maximum durability (uses) for the Cleaner. Default: 16")
		@Config.RangeInt(min = 1, max = 10000)
		@Config.RequiresMcRestart
		public int cleanerDurability = 16;

		@Config.Name("Cleaner Block Mappings")
		@Config.Comment({
				"List of block and meta pairs that the Cleaner can clean up.",
				"Format per entry: 'modid:source_block:source_meta|modid:target_block:target_meta' (meta is optional, defaults to 0 if omitted, or -1 for any meta if using *).",
				"Examples: 'minecraft:mossy_cobblestone:0|minecraft:cobblestone:0', 'totf:bricks_stone_dirty|totf:bricks_stone'"
		})
		public String[] cleanerBlockMappings = {
				"minecraft:mossy_cobblestone:0|minecraft:cobblestone:0",
				"minecraft:stonebrick:1|minecraft:stonebrick:0",
				"minecraft:cobblestone_wall:1|minecraft:cobblestone_wall:0",
				"totf:bricks_stone_dirty:0|totf:bricks_stone:0",
				"totf:bricks_stone_mossy:0|totf:bricks_stone:0",
				"totf:bricks_stone_carved_dirty:0|totf:bricks_stone_carved:0",
				"totf:bricks_stone_carved_mossy:0|totf:bricks_stone_carved:0",
				"totf:mozaic_pink_washed:0|totf:mozaic_pink:0",
				"totf:mozaic_red_washed:0|totf:mozaic_red:0",
				"totf:mozaic_teal_washed:0|totf:mozaic_teal:0",
				"totf:mozaic_yellow_washed:0|totf:mozaic_yellow:0",
				"totf:stone_circle:0|totf:stone_circle_clean:0",
				"totf:stone_circle:1|totf:stone_circle_clean:1",
				"totf:stone_circle:2|totf:stone_circle_clean:2",
				"totf:stone_circle:3|totf:stone_circle_clean:3",
				"totf:stone_circle:4|totf:stone_circle_clean:4",
				"totf:stone_circle:5|totf:stone_circle_clean:5",
				"totf:stone_circle:6|totf:stone_circle_clean:6",
				"totf:stone_circle:7|totf:stone_circle_clean:7",
				"totf:stone_circle:8|totf:stone_circle_clean:8",
				"totf:stone_circle:9|totf:stone_circle_clean:9",
				"totf:stone_circle:10|totf:stone_circle_clean:10",
				"totf:stone_circle:11|totf:stone_circle_clean:11",
				"totf:stone_circle:12|totf:stone_circle_clean:12",
				"totf:stone_circle:13|totf:stone_circle_clean:13",
				"totf:stone_circle:14|totf:stone_circle_clean:14",
				"totf:stone_circle:15|totf:stone_circle_clean:15"
		};
		@Config.Name("Sidhe Interactions")
		@Config.Comment({
				"List of items that the Sidhe can interact with.",
				"Format: 'modid:item_name:meta|action'",
				"Meta is optional (defaults to 0 or any if *). NBT can be appended as JSON string if needed, e.g. modid:item:meta:{NBT}.",
				"Action can be 'attack' to make the Sidhe retaliate, or an item stack to return as a reward (e.g. 'minecraft:diamond:0*1').",
				"Several rewards can be listed separated by ';', in which case one of them is picked at random every time (e.g. 'minecraft:diamond:0*1;minecraft:emerald:0*2').",
				"Examples: 'minecraft:fish:*|attack', 'totf:furred_trout:0|minecraft:diamond:0*1'"
		})
		public String[] sidheInteractItems = {
				"minecraft:fish:*|attack",
				"minecraft:cooked_fish:*|attack",
				"totf:furred_trout:0|totf:ancestral_claw_necklace:0*1"
		};

		@Config.Name("Randomize Sidhe Barter")
		@Config.Comment({
				"If enabled, the outcomes of the items in 'Sidhe Interactions' are mixed up for every player, good and bad alike: about half of the items",
				"become rewards and half make the Sidhe angry, no matter what the list says for them. Rewards come from the rewards in the list",
				"plus 'Sidhe Random Rewards'. Items in 'Sidhe Guaranteed Reward Items' never anger the Sidhe, they always give a (random) reward.",
				"The result is derived from the world seed and the player's UUID, so every player gets their own, stable, set of outcomes.",
				"If disabled, the 'Sidhe Interactions' list is used exactly as written for everyone."
		})
		public boolean randomizeSidheBarter = true;

		@Config.Name("Sidhe Guaranteed Reward Items")
		@Config.Comment({
				"Items that must never anger the Sidhe when 'Randomize Sidhe Barter' is enabled. They always give a randomly picked reward.",
				"Format: 'modid:item_name:meta' (like the input of 'Sidhe Interactions', meta is optional and * means any). Default: the furred trout."
		})
		public String[] sidheGuaranteedRewardItems = {
				"totf:furred_trout:0"
		};

		@Config.Name("Sidhe Random Rewards")
		@Config.Comment({
				"Extra pool of rewards the Sidhe can give for randomized items, besides the ones in 'Sidhe Interactions'. Format: 'modid:item_name:meta*count' (meta and count are optional).",
				"Every randomized item that turns out to be a reward gets its own set of 'Sidhe Random Reward Choices' items from this pool, per player.",
				"Only used if 'Randomize Sidhe Barter' is enabled. If this list is empty (or has no valid items), randomized items always anger the Sidhe."
		})
		public String[] sidheRandomRewards = {
				"totf:mysterious_fur:0*1",
				"totf:pemmican:0*2",
				"totf:silk_rope:0*1",
				"totf:incense:0*2",
				"totf:spirited_away_tea_leaves:0*2",
				"totf:living_silver_speck:0*3"
		};

		@Config.Name("Sidhe Random Reward Choices")
		@Config.Comment({
				"How many different rewards each randomized item can give. For every trade the Sidhe picks one of them at random,",
				"and all of them are shown in JEI once the trade is discovered. Capped by the size of 'Sidhe Random Rewards'. Default: 3"
		})
		@Config.RangeInt(min = 1, max = 16)
		public int sidheRandomRewardChoices = 3;

		@Config.Name("Brass Fabricator Custom Fuels")
		@Config.Comment({
				"List of custom fuels (or overrides) for the Brass Fabricator.",
				"Format per entry: 'modid:item_name|burn_time_ticks' or 'modid:item_name:meta|burn_time_ticks'.",
				"Examples: 'minecraft:redstone|1600', 'minecraft:coal:1|3200'"
		})
		public String[] fabricatorCustomFuels = {
				"minecraft:redstone|1600"
		};

		@Config.Name("Stone Pressure Plate Search Radius")
		@Config.Comment("The radius in blocks that the Stone Pressure Plate searches for a Stone Receiver when activated. Default: 12")
		@Config.RangeInt(min = 1, max = 64)
		public int stonePressurePlateSearchRadius = 12;

		@Config.Name("Stone Receiver Pulse Duration")
		@Config.Comment("The duration (in ticks) that the Stone Receiver emits a redstone signal when triggered. Default: 60 (3 seconds)")
		@Config.RangeInt(min = 1, max = 1200)
		public int stoneReceiverPulseDurationTicks = 60;

		@Config.Name("Ancestral Claw Necklace Durability")
		@Config.Comment("The maximum durability (uses) for the Ancestral Claw Necklace. Default: 16")
		@Config.RangeInt(min = 1, max = 10000)
		@Config.RequiresMcRestart
		public int ancestralClawNecklaceDurability = 16;

		@Config.Name("Enable White Floater")
		@Config.Comment("If true, the white variant of the Floater is enabled and registered. Default: false")
		@Config.RequiresMcRestart
		public boolean enableWhiteFloater = false;

		@Config.Name("Baubles Integration")
		@Config.Comment("If true and Baubles is installed, the arcane skulls can be worn in charm slots and activated with a keybind. Default: true")
		public boolean baublesIntegration = true;
    }

    public static class ClientSettings {
		@Config.Name("Dioptra Camera Height")
		@Config.Comment("First-person camera height above the dioptra block center while mounted. Default: 1.75")
		@Config.RangeDouble(min = -4.0D, max = 4.0D)
		public double dioptraCameraHeight = 1.5465D;

		@Config.Name("Dioptra Camera Back Offset")
		@Config.Comment("How far back from the dioptra center the first-person camera sits while mounted. Default: 0.24")
		@Config.RangeDouble(min = -1.0D, max = 1.0D)
		public double dioptraCameraBackOffset = -0.765432D;
    }

	public static class GoblinSettings {
		@Config.Name("Goblin Brood Max Health")
		@Config.Comment("Max health for the Goblin Brood (and base goblins). Default: 12.0")
		@Config.RangeDouble(min = 1.0D, max = 10000.0D)
		public double goblinBroodMaxHealth = 12.0D;

		@Config.Name("Goblin Brood Attack Damage")
		@Config.Comment("Base attack damage for the Goblin Brood (and base goblins). Default: 1.0")
		@Config.RangeDouble(min = 0.0D, max = 10000.0D)
		public double goblinBroodAttackDamage = 1.0D;

		@Config.Name("Goblin Brute Max Health")
		@Config.Comment("Max health for the Goblin Brute. Default: 12.0")
		@Config.RangeDouble(min = 1.0D, max = 10000.0D)
		public double goblinBruteMaxHealth = 12.0D;

		@Config.Name("Goblin Brute Attack Damage")
		@Config.Comment("Base attack damage for the Goblin Brute. Default: 6.0")
		@Config.RangeDouble(min = 0.0D, max = 10000.0D)
		public double goblinBruteAttackDamage = 6.0D;

		@Config.Name("Goblin Engineer Max Health")
		@Config.Comment("Max health for the Goblin Engineer. Default: 20.0")
		@Config.RangeDouble(min = 1.0D, max = 10000.0D)
		public double goblinEngineerMaxHealth = 20.0D;

		@Config.Name("Goblin Engineer Attack Damage")
		@Config.Comment("Base attack damage for the Goblin Engineer. Default: 3.0")
		@Config.RangeDouble(min = 0.0D, max = 10000.0D)
		public double goblinEngineerAttackDamage = 3.0D;

		@Config.Name("Goblin Engineer Bomb Follow Range")
		@Config.Comment("Follow/detect range for the Goblin Engineer when carrying a bomb. Default: 45.0")
		@Config.RangeDouble(min = 1.0D, max = 256.0D)
		public double goblinEngineerBombFollowRange = 45.0D;

		@Config.Name("Goblin Sapper Max Health")
		@Config.Comment("Max health for the Goblin Sapper. Default: 20.0")
		@Config.RangeDouble(min = 1.0D, max = 10000.0D)
		public double goblinSapperMaxHealth = 20.0D;

		@Config.Name("Goblin Sapper Attack Damage")
		@Config.Comment("Base attack damage for the Goblin Sapper. Default: 3.0")
		@Config.RangeDouble(min = 0.0D, max = 10000.0D)
		public double goblinSapperAttackDamage = 3.0D;

		@Config.Name("Goblin Shaman Max Health")
		@Config.Comment("Max health for the Goblin Shaman. Default: 30.0")
		@Config.RangeDouble(min = 1.0D, max = 10000.0D)
		public double goblinShamanMaxHealth = 30.0D;

		@Config.Name("Goblin Shaman Attack Damage")
		@Config.Comment("Base attack damage for the Goblin Shaman. Default: 2.0")
		@Config.RangeDouble(min = 0.0D, max = 10000.0D)
		public double goblinShamanAttackDamage = 2.0D;

		@Config.Name("Goblin Starved Max Health")
		@Config.Comment("Max health for the Starved Goblin. Default: 8.0")
		@Config.RangeDouble(min = 1.0D, max = 10000.0D)
		public double goblinStarvedMaxHealth = 8.0D;

		@Config.Name("Goblin Starved Attack Damage")
		@Config.Comment("Base attack damage for the Starved Goblin. Default: 2.0")
		@Config.RangeDouble(min = 0.0D, max = 10000.0D)
		public double goblinStarvedAttackDamage = 2.0D;

		@Config.Name("Goblin Trapper Max Health")
		@Config.Comment("Max health for the Goblin Trapper. Default: 20.0")
		@Config.RangeDouble(min = 1.0D, max = 10000.0D)
		public double goblinTrapperMaxHealth = 20.0D;

		@Config.Name("Goblin Trapper Attack Damage")
		@Config.Comment("Base attack damage for the Goblin Trapper. Default: 3.0")
		@Config.RangeDouble(min = 0.0D, max = 10000.0D)
		public double goblinTrapperAttackDamage = 3.0D;

		@Config.Name("Goblin Tunneler Max Health")
		@Config.Comment("Max health for the Goblin Tunneler. Default: 20.0")
		@Config.RangeDouble(min = 1.0D, max = 10000.0D)
		public double goblinTunnelerMaxHealth = 20.0D;

		@Config.Name("Goblin Tunneler Attack Damage")
		@Config.Comment("Base attack damage for the Goblin Tunneler. Default: 3.0")
		@Config.RangeDouble(min = 0.0D, max = 10000.0D)
		public double goblinTunnelerAttackDamage = 3.0D;

		@Config.Name("Goblin Warrior Max Health")
		@Config.Comment("Max health for the Goblin Warrior. Default: 30.0")
		@Config.RangeDouble(min = 1.0D, max = 10000.0D)
		public double goblinWarriorMaxHealth = 30.0D;

		@Config.Name("Goblin Warrior Attack Damage")
		@Config.Comment("Base attack damage for the Goblin Warrior. Default: 5.0")
		@Config.RangeDouble(min = 0.0D, max = 10000.0D)
		public double goblinWarriorAttackDamage = 5.0D;

		@Config.Name("Goblin Wayfarer Max Health")
		@Config.Comment("Max health for the Goblin Wayfarer. Default: 20.0")
		@Config.RangeDouble(min = 1.0D, max = 10000.0D)
		public double goblinWayfarerMaxHealth = 20.0D;

		@Config.Name("Goblin Wayfarer Attack Damage")
		@Config.Comment("Base attack damage for the Goblin Wayfarer. Default: 3.0")
		@Config.RangeDouble(min = 0.0D, max = 10000.0D)
		public double goblinWayfarerAttackDamage = 3.0D;

		@Config.Name("Goblin Skeleton Max Health")
		@Config.Comment("Max health for the Reanimated Goblin Skeleton. Default: 10.0")
		@Config.RangeDouble(min = 1.0D, max = 10000.0D)
		public double goblinSkeletonMaxHealth = 10.0D;

		@Config.Name("Goblin Skeleton Attack Damage")
		@Config.Comment("Base attack damage for the Reanimated Goblin Skeleton. Default: 2.0")
		@Config.RangeDouble(min = 0.0D, max = 10000.0D)
		public double goblinSkeletonAttackDamage = 2.0D;

		@Config.Name("Tinybones Max Health")
		@Config.Comment("Max health for the Tinybones skeleton variant. Default: 25.0")
		@Config.RangeDouble(min = 1.0D, max = 10000.0D)
		public double tinybonesMaxHealth = 25.0D;

		@Config.Name("Tinybones Life Leech Attack Damage")
		@Config.Comment("Base attack damage per tick tick interval (every 10 ticks) for Tinybones Life Leech. Default: 0.6 (0.3 hearts)")
		@Config.RangeDouble(min = 0.0D, max = 10000.0D)
		public double tinybonesAttackDamage = 0.6D;

		@Config.Name("Specter Max Health")
		@Config.Comment("Max health for Specters (and Familiar Specters). Default: 20.0")
		@Config.RangeDouble(min = 1.0D, max = 10000.0D)
		public double specterMaxHealth = 20.0D;

		@Config.Name("Specter Attack Damage")
		@Config.Comment("Base attack damage for Specters (and Familiar Specters). Default: 4.0")
		@Config.RangeDouble(min = 0.0D, max = 10000.0D)
		public double specterAttackDamage = 4.0D;

		@Config.Name("Lost Edible Entities")
		@Config.Comment("List of entity registry names that the Lost mob can grab, hold, and eat. Format: 'modid:entityname'")
		public String[] lostEdibleEntities = {
				"minecraft:chicken",
				"minecraft:pig",
				"minecraft:sheep",
				"minecraft:rabbit",
				"totf:goblin_starved",
				"totf:goblin"
		};
	}

	public static class MobSettings {
		@Config.Name("Minecrawler Spawn Weight")
		@Config.Comment("Natural-spawn weight for Minecrawlers. They spawn freely in mineshafts and only very rarely in other underground areas. Default: 1 (0 to disable)")
		public int minecrawlerSpawnWeight = 1;

		@Config.Name("Minecrawler Underground Spawn Chance")
		@Config.Comment("Chance for a natural Minecrawler spawn outside a mineshaft after normal monster checks succeed. 1 means every attempt; higher values are rarer. Default: 240")
		@Config.RangeInt(min = 1, max = 100000)
		public int minecrawlerUndergroundSpawnChance = 240;

		@Config.Name("Sidhe Spawn Weight")
		@Config.Comment("Spawn weight for the Sidhe. Default: 5 (0 to disable)")
		public int sidheSpawnWeight = 5;

		@Config.Name("Sidhe Spawn Min Group")
		@Config.Comment("Minimum group size for the Sidhe. Default: 1")
		public int sidheSpawnMinGroup = 1;

		@Config.Name("Sidhe Spawn Max Group")
		@Config.Comment("Maximum group size for the Sidhe. Default: 1")
		public int sidheSpawnMaxGroup = 1;

		@Config.Name("Sidhe Biome Whitelist")
		@Config.Comment("Biomes where Sidhe can spawn (empty = all allowed). Format: modid:biome")
		public String[] sidheBiomeWhitelist = new String[0];

		@Config.Name("Sidhe Biome Blacklist")
		@Config.Comment("Biomes where Sidhe cannot spawn. Format: modid:biome")
		public String[] sidheBiomeBlacklist = new String[0];

		@Config.Name("Frostling Spawn Weight")
		@Config.Comment("Spawn weight for the Frostling. Default: 15 (0 to disable)")
		public int frostlingSpawnWeight = 15;

		@Config.Name("Frostling Spawn Min Group")
		@Config.Comment("Minimum group size for the Frostling. Default: 1")
		public int frostlingSpawnMinGroup = 1;

		@Config.Name("Frostling Spawn Max Group")
		@Config.Comment("Maximum group size for the Frostling. Default: 3")
		public int frostlingSpawnMaxGroup = 3;

		@Config.Name("Frostling Biome Whitelist")
		@Config.Comment("Biomes where Frostling can spawn (empty = all allowed). Format: modid:biome")
		public String[] frostlingBiomeWhitelist = new String[0];

		@Config.Name("Frostling Biome Blacklist")
		@Config.Comment("Biomes where Frostling cannot spawn. Format: modid:biome")
		public String[] frostlingBiomeBlacklist = new String[0];

		@Config.Name("Gnossic Burns In Sun")
		@Config.Comment("Whether the Gnossic mob burns in sunlight. Default: false")
		public boolean gnossicBurnsInSun = false;

		@Config.Name("Gnossic Mask Trigger Radius")
		@Config.Comment("The range in blocks within which a player wakes a Gnossic Mask, turning it into a Gnossic. Default: 6")
		@Config.RangeDouble(min = 1.0, max = 64.0)
		public double gnossicMaskTriggerRadius = 6.0D;

		@Config.Name("Lamphead Knockback Multiplier")
		@Config.Comment("The knockback multiplier for Lamphead's attack. Default: 0.8")
		@Config.RangeDouble(min = 0.0D, max = 100.0D)
		public double lampheadKnockbackMultiplier = 0.8D;

		@Config.Name("Lamphead Attack Damage")
		@Config.Comment("Base attack damage for Lamphead. Default: 5.0")
		@Config.RangeDouble(min = 0.0D, max = 10000.0D)
		public double lampheadAttackDamage = 6.0D;

		@Config.Name("Lamphead Max Health")
		@Config.Comment("Max health for Lamphead. Default: 35.0")
		@Config.RangeDouble(min = 1.0D, max = 10000.0D)
		public double lampheadMaxHealth = 35.0D;

		@Config.Name("Subterfuge Attack Damage")
		@Config.Comment("Base attack damage for Subterfuge. Default: 300.0")
		@Config.RangeDouble(min = 0.0D, max = 10000.0D)
		public double subterfugeAttackDamage = 300.0D;

		@Config.Name("Subterfuge Max Health")
		@Config.Comment("Max health for Subterfuge. Default: 1000.0")
		@Config.RangeDouble(min = 1.0D, max = 10000.0D)
		public double subterfugeMaxHealth = 1000.0D;

		@Config.Name("Subterfuge Hardcore Teleport")
		@Config.Comment("In hardcore worlds Subterfuge randomly teleports a player away instead of slapping them to death. If false, she kills them as usual. Default: true")
		public boolean subterfugeHardcoreTeleport = true;

		@Config.Name("Subterfuge Hardcore Teleport Min Distance")
		@Config.Comment("Minimum distance, in blocks, Subterfuge teleports a player away in hardcore worlds. At least 1000. Default: 1000")
		@Config.RangeInt(min = 1000, max = 100000)
		public int subterfugeHardcoreTeleportMin = 1000;

		@Config.Name("Subterfuge Hardcore Teleport Max Distance")
		@Config.Comment("Maximum distance, in blocks, Subterfuge teleports a player away in hardcore worlds. At least 1000; if lower than the minimum, the minimum is used. Default: 3000")
		@Config.RangeInt(min = 1000, max = 100000)
		public int subterfugeHardcoreTeleportMax = 3000;

		@Config.Name("Dungeon Slug Spawn Weight")
		@Config.Comment("Spawn weight for the Dungeon Slug. Default: 2 (0 to disable)")
		public int dungeonSlugSpawnWeight = 2;

		@Config.Name("Dungeon Slug Spawn Min Group")
		@Config.Comment("Minimum group size for the Dungeon Slug. Default: 1")
		public int dungeonSlugSpawnMinGroup = 1;

		@Config.Name("Dungeon Slug Spawn Max Group")
		@Config.Comment("Maximum group size for the Dungeon Slug. Default: 2")
		public int dungeonSlugSpawnMaxGroup = 2;

		@Config.Name("Dungeon Slug Biome Whitelist")
		@Config.Comment("Biomes where Dungeon Slug can spawn (empty = all allowed). Format: modid:biome")
		public String[] dungeonSlugBiomeWhitelist = new String[0];

		@Config.Name("Dungeon Slug Biome Blacklist")
		@Config.Comment("Biomes where Dungeon Slug cannot spawn. Format: modid:biome")
		public String[] dungeonSlugBiomeBlacklist = new String[0];

		@Config.Name("Dungeon Slug Max Health")
		@Config.Comment("Max health for the Dungeon Slug. Default: 16.0")
		@Config.RangeDouble(min = 1.0D, max = 10000.0D)
		public double dungeonSlugMaxHealth = 16.0D;

		@Config.Name("Dungeon Slug Attack Damage")
		@Config.Comment("Melee attack damage for the Dungeon Slug once revealed. Default: 4.0")
		@Config.RangeDouble(min = 0.0D, max = 10000.0D)
		public double dungeonSlugAttackDamage = 4.0D;

		@Config.Name("Lost Spawn Weight")
		@Config.Comment("Spawn weight for the Lost, a rare ambusher of deep cave miners. Default: 1 (0 to disable)")
		public int lostSpawnWeight = 1;

		@Config.Name("Lost Spawn Min Group")
		@Config.Comment("Minimum group size for the Lost. Default: 1")
		public int lostSpawnMinGroup = 1;

		@Config.Name("Lost Spawn Max Group")
		@Config.Comment("Maximum group size for the Lost. Default: 1")
		public int lostSpawnMaxGroup = 1;

		@Config.Name("Lost Spawn Min Y")
		@Config.Comment("Lowest Y level the Lost can naturally spawn at. Default: 1")
		@Config.RangeInt(min = 0, max = 255)
		public int lostSpawnMinY = 1;

		@Config.Name("Lost Spawn Max Y")
		@Config.Comment("Highest Y level the Lost can naturally spawn at. Default: 16 (diamond depth)")
		@Config.RangeInt(min = 0, max = 255)
		public int lostSpawnMaxY = 16;

		@Config.Name("Lost Biome Whitelist")
		@Config.Comment("Biomes where the Lost can spawn (empty = all allowed). Format: modid:biome")
		public String[] lostBiomeWhitelist = new String[0];

		@Config.Name("Lost Biome Blacklist")
		@Config.Comment("Biomes where the Lost cannot spawn. Format: modid:biome")
		public String[] lostBiomeBlacklist = new String[0];

		@Config.Name("Ink Slug Max Health")
		@Config.Comment("Max health for the Ink Slug. Default: 20.0")
		@Config.RangeDouble(min = 1.0D, max = 10000.0D)
		public double inkSlugMaxHealth = 20.0D;

		@Config.Name("Ink Slug Attack Damage")
		@Config.Comment("Melee attack damage for the Ink Slug once revealed. Default: 5.0")
		@Config.RangeDouble(min = 0.0D, max = 10000.0D)
		public double inkSlugAttackDamage = 5.0D;

		@Config.Name("Slug Attach Damage Per Tick")
		@Config.Comment("Damage dealt to the victim on each attach-damage tick while a Slug is latched onto their back. Default: 2.0")
		@Config.RangeDouble(min = 0.0D, max = 1000.0D)
		public double slugAttachDamageAmount = 2.0D;

		@Config.Name("Slug Attach Damage Interval")
		@Config.Comment("Ticks between each attach-damage hit while a Slug is latched onto a victim's back. Default: 20 (1 second)")
		public int slugAttachDamageInterval = 20;

		@Config.Name("Slug Max Attach Duration")
		@Config.Comment("Maximum ticks a Slug can stay latched onto a victim before automatically letting go and fighting normally. Default: 300 (15 seconds)")
		public int slugMaxAttachDuration = 300;

		@Config.Name("Slug Climb Speed")
		@Config.Comment("Movement speed while a Slug is crawling on walls or ceilings. Default: 0.04")
		@Config.RangeDouble(min = 0.01D, max = 5.0D)
		public double slugClimbSpeed = 0.04D;
	}
	public static class HauntingSettings {
		@Config.Name("Haunting Reduced by Burying Remains")
		@Config.Comment("The amount of haunting reduced by burying remains. Default: 3")
		public int hauntingReducedByBuryingRemains = 3;

		@Config.Name("Haunting Reduced by Placing a Flower on a Grave")
		@Config.Comment("The amount of haunting reduced by placing a flower on a grave.")
		public int hauntingReducedByPlacingFlowerOnGrave = 2;

		@Config.Name("Veiled Mask Haunting Amount")
		@Config.Comment("The amount of haunting gained each time while wearing the Veiled Mask. Default: 1")
		public int veiledMaskHauntingAmount = 1;

		@Config.Name("Veiled Mask Haunting Tick Rate")
		@Config.Comment("How often (in ticks) the Veiled Mask gains haunting and consumes durability. Default: 60 (3 seconds)")
		public int veiledMaskHauntingTickRate = 60;

		@Config.Name("Haunting Blocks")
		@Config.Comment("List of blocks that affect haunting when mined. Format: 'modid:blockname:amount' or 'modid:blockname:meta:amount'. Negative values reduce haunting. Examples: 'totf:skeleton_crate:3', 'minecraft:wool:0:5', 'totf:grave_marker:-2'")
		public String[] hauntingBlocks = {
				"totf:skeleton_crate:3",
				"totf:grave_marker:2"
		};

		@Config.Name("Haunting Spawn Cooldown After Death")
		@Config.Comment("How long (in ticks) haunting mobs are prevented from spawning after they kill a player. Default: 24000 (20 minutes)")
		public int hauntingSpawnCooldownAfterDeath = 24000;

		@Config.Name("Subtorch Radius")
		@Config.Comment("Radius (in blocks) around a Subtorch within which players are protected from haunting. 2 covers a 5x5 area. Set to 0 to disable the effect. Default: 2")
		@Config.RangeInt(min = 0, max = 16)
		public int subtorchRadius = 2;

		@Config.Name("Haunting Kill Item Reductions")
		@Config.Comment("List of items that reduce haunting when used to land a killing blow on a haunting mob. Format: 'modid:item_name|haunting_reduction|item_damage' or 'modid:item_name:meta|haunting_reduction|item_damage'.")
		public String[] hauntingKillItemReductions = {
				"minecraft:golden_sword|1|5",
				"minecraft:golden_pickaxe|1|5",
				"minecraft:golden_axe|1|5",
				"minecraft:golden_shovel|1|5",
				"minecraft:golden_hoe|1|5"
		};
	}

    @SuppressWarnings("unused")
    @Mod.EventBusSubscriber(modid = TracesOfTheFallen.MODID)
    private static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(TracesOfTheFallen.MODID)) {
                ConfigManager.sync(TracesOfTheFallen.MODID, Config.Type.INSTANCE);
                HauntingCapability.clearHauntingBlockCache();
                TileEntityPorcelainVessel.clearHeatSourcesCache();
                GoblinAITunnelerDig.clearBreakOverridesCache();
                TileEntityBrassFabricator.clearFuelCache();
            }
        }
    }
}

