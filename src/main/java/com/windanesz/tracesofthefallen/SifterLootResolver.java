package com.windanesz.tracesofthefallen;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.IFluidBlock;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class SifterLootResolver {

	private static final ResourceLocation DEFAULT_AIR = new ResourceLocation(TracesOfTheFallen.MODID, "sifters/air/default");
	private static final ResourceLocation DEFAULT_WATER = new ResourceLocation(TracesOfTheFallen.MODID, "sifters/water/default");
	private static final ResourceLocation DEFAULT_LAVA = new ResourceLocation(TracesOfTheFallen.MODID, "sifters/lava/default");
	private static final Map<String, ResourceLocation> RESOLVED_TABLE_CACHE = new ConcurrentHashMap<>();
	private static final Map<ResourceLocation, Boolean> LOOT_TABLE_EXISTS_CACHE = new ConcurrentHashMap<>();

	private SifterLootResolver() {
	}

	public static List<ItemStack> generateLoot(WorldServer world, BlockPos pos) {
		ResourceLocation lootTableLocation = resolveLootTable(world, pos);
		LootTable lootTable = world.getLootTableManager().getLootTableFromLocation(lootTableLocation);
		if (lootTable == LootTable.EMPTY_LOOT_TABLE) {
			return Collections.emptyList();
		}
		return lootTable.generateLootForPools(world.rand, new LootContext.Builder(world).build());
	}

	private static ResourceLocation resolveLootTable(WorldServer world, BlockPos pos) {
		SifterEnvironment environment = SifterEnvironment.detect(world, pos);
		String cacheKey = environment.createCacheKey(world, pos);
		ResourceLocation cached = RESOLVED_TABLE_CACHE.get(cacheKey);
		if (cached != null) {
			return cached;
		}

		for (ResourceLocation candidate : environment.buildCandidates(world, pos)) {
			if (lootTableExists(candidate)) {
				RESOLVED_TABLE_CACHE.put(cacheKey, candidate);
				return candidate;
			}
		}

		ResourceLocation fallback = environment.getFallbackTable();
		RESOLVED_TABLE_CACHE.put(cacheKey, fallback);
		return fallback;
	}

	private static boolean lootTableExists(ResourceLocation location) {
		Boolean cached = LOOT_TABLE_EXISTS_CACHE.get(location);
		if (cached != null) {
			return cached;
		}

		String resourcePath = "assets/" + location.getNamespace() + "/loot_tables/" + location.getPath() + ".json";
		boolean exists = SifterLootResolver.class.getClassLoader().getResource(resourcePath) != null;
		LOOT_TABLE_EXISTS_CACHE.put(location, exists);
		return exists;
	}

	private static final class SifterEnvironment {

		private final EnvironmentType type;
		@Nullable
		private final ResourceLocation fluidName;

		private SifterEnvironment(EnvironmentType type, @Nullable ResourceLocation fluidName) {
			this.type = type;
			this.fluidName = fluidName;
		}

		private static SifterEnvironment detect(World world, BlockPos pos) {
			DetectedFluid detectedFluid = findFluid(world, pos);
			if (detectedFluid == null) {
				detectedFluid = findFluid(world, pos.up());
			}
			if (detectedFluid == null) {
				return new SifterEnvironment(EnvironmentType.AIR, null);
			}

			Fluid fluid = detectedFluid.fluid;
			ResourceLocation fluidName = detectedFluid.name;
			if (fluid == FluidRegistry.WATER || (fluidName != null && "minecraft".equals(fluidName.getNamespace()) && "water".equals(fluidName.getPath()))) {
				return new SifterEnvironment(EnvironmentType.WATER, null);
			}
			if (fluid == FluidRegistry.LAVA || (fluidName != null && "minecraft".equals(fluidName.getNamespace()) && "lava".equals(fluidName.getPath()))) {
				return new SifterEnvironment(EnvironmentType.LAVA, null);
			}
			return new SifterEnvironment(EnvironmentType.MODDED_FLUID, fluidName);
		}

		private List<ResourceLocation> buildCandidates(WorldServer world, BlockPos pos) {
			int dimension = world.provider.getDimension();
			ResourceLocation biomeName = getBiomeName(world, pos);
			List<ResourceLocation> candidates = new ArrayList<>();
			String mediumPath = type == EnvironmentType.MODDED_FLUID && fluidName != null
					? "fluid/" + fluidName.getNamespace() + "/" + fluidName.getPath()
					: type.basePath;

			addCandidateChain(candidates, "sifters/" + mediumPath, dimension, biomeName, getBiomeTypes(world, pos));
			candidates.add(type.fallbackTable);
			return candidates;
		}

		private ResourceLocation getFallbackTable() {
			return type.fallbackTable;
		}

		private String createCacheKey(WorldServer world, BlockPos pos) {
			ResourceLocation biomeName = getBiomeName(world, pos);
			String fluidKey = fluidName == null ? "-" : fluidName.toString();
			return type.name() + "|" + fluidKey + "|" + world.provider.getDimension() + "|" + biomeName + "|" + getBiomeTypes(world, pos);
		}

		private static void addCandidateChain(List<ResourceLocation> candidates, String basePath, int dimension,
				ResourceLocation biomeName, List<String> biomeTypes) {
			String biomePath = biomeName.getNamespace() + "/" + biomeName.getPath();
			candidates.add(new ResourceLocation(TracesOfTheFallen.MODID, basePath + "/dimension/" + dimension + "/biome/" + biomePath));
			for (String biomeType : biomeTypes) {
				candidates.add(new ResourceLocation(TracesOfTheFallen.MODID, basePath + "/dimension/" + dimension + "/biometype/" + biomeType));
			}
			candidates.add(new ResourceLocation(TracesOfTheFallen.MODID, basePath + "/dimension/" + dimension));
			candidates.add(new ResourceLocation(TracesOfTheFallen.MODID, basePath + "/biome/" + biomePath));
			for (String biomeType : biomeTypes) {
				candidates.add(new ResourceLocation(TracesOfTheFallen.MODID, basePath + "/biometype/" + biomeType));
			}
			candidates.add(new ResourceLocation(TracesOfTheFallen.MODID, basePath + "/default"));
		}

		@Nullable
		private static DetectedFluid findFluid(World world, BlockPos pos) {
			Block block = world.getBlockState(pos).getBlock();
			ResourceLocation blockName = block.getRegistryName();
			Fluid fluid = FluidRegistry.lookupFluidForBlock(block);
			if (fluid != null) {
				return new DetectedFluid(fluid, blockName != null ? blockName : toResourceLocation(fluid.getName()));
			}
			if (block instanceof IFluidBlock) {
				Fluid fluidBlock = ((IFluidBlock) block).getFluid();
				return new DetectedFluid(fluidBlock, blockName != null ? blockName : toResourceLocation(fluidBlock.getName()));
			}
			Material material = world.getBlockState(pos).getMaterial();
			if (material == Material.WATER) {
				return new DetectedFluid(FluidRegistry.WATER, new ResourceLocation("minecraft", "water"));
			}
			if (material == Material.LAVA) {
				return new DetectedFluid(FluidRegistry.LAVA, new ResourceLocation("minecraft", "lava"));
			}
			return null;
		}

		private static ResourceLocation toResourceLocation(String name) {
			return name.contains(":") ? new ResourceLocation(name) : new ResourceLocation(TracesOfTheFallen.MODID, name);
		}

		private static ResourceLocation getBiomeName(World world, BlockPos pos) {
			Biome biome = world.getBiome(pos);
			return biome.getRegistryName() == null ? new ResourceLocation("minecraft", "plains") : biome.getRegistryName();
		}

		private static List<String> getBiomeTypes(World world, BlockPos pos) {
			List<String> biomeTypes = new ArrayList<>();
			for (BiomeDictionary.Type type : BiomeDictionary.getTypes(world.getBiome(pos))) {
				biomeTypes.add(type.getName().toLowerCase());
			}
			biomeTypes.sort(Comparator.naturalOrder());
			return biomeTypes;
		}
	}

	private static final class DetectedFluid {

		private final Fluid fluid;
		private final ResourceLocation name;

		private DetectedFluid(Fluid fluid, ResourceLocation name) {
			this.fluid = fluid;
			this.name = name;
		}
	}

	private enum EnvironmentType {
		AIR("air", DEFAULT_AIR),
		WATER("water", DEFAULT_WATER),
		LAVA("lava", DEFAULT_LAVA),
		MODDED_FLUID("fluid", DEFAULT_WATER);

		private final String basePath;
		private final ResourceLocation fallbackTable;

		EnvironmentType(String basePath, ResourceLocation fallbackTable) {
			this.basePath = basePath;
			this.fallbackTable = fallbackTable;
		}
	}
}
