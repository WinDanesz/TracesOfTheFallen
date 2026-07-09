package com.windanesz.tracesofthefallen.item;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.client.ClientProxy;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemCleaner extends Item {

	private static Map<String, BlockMetaPair> cleanerMappingsCache = null;

	public static void clearCleanerMappingsCache() {
		cleanerMappingsCache = null;
	}

	private static Map<String, BlockMetaPair> getCleanerMappings() {
		if (cleanerMappingsCache != null) {
			return cleanerMappingsCache;
		}

		cleanerMappingsCache = new HashMap<>();

		if (Settings.miscSettings.cleanerBlockMappings != null) {
			for (String entry : Settings.miscSettings.cleanerBlockMappings) {
				if (entry == null || entry.trim().isEmpty()) continue;

				String[] parts;
				if (entry.contains("->")) {
					parts = entry.split("->");
				} else if (entry.contains("|")) {
					parts = entry.split("\\|");
				} else if (entry.contains(",")) {
					parts = entry.split(",");
				} else {
					continue;
				}

				if (parts.length < 2) continue;

				BlockMetaPair source = parseBlockMeta(parts[0].trim());
				BlockMetaPair target = parseBlockMeta(parts[1].trim());

				if (source != null && target != null) {
					String key = source.block.getRegistryName() != null ? source.block.getRegistryName().toString() : "";
					if (key.isEmpty()) continue;
					if (source.meta >= 0) {
						key += ":" + source.meta;
					}
					cleanerMappingsCache.put(key, target);
				}
			}
		}

		return cleanerMappingsCache;
	}

	@Nullable
	private static BlockMetaPair parseBlockMeta(String str) {
		String[] parts = str.split(":");
		if (parts.length < 2) return null;

		String modid = parts[0];
		String blockName = parts[1];
		int meta = -1;

		if (parts.length >= 3) {
			String metaStr = parts[2].trim();
			if (!metaStr.equals("*") && !metaStr.equalsIgnoreCase("any")) {
				try {
					meta = Integer.parseInt(metaStr);
				} catch (NumberFormatException e) {
					TracesOfTheFallen.LOGGER.warn("Invalid meta in cleaner mapping: " + str);
					return null;
				}
			}
		}

		ResourceLocation loc = new ResourceLocation(modid, blockName);
		Block block = Block.REGISTRY.getObject(loc);
		if (block == null || (block == Blocks.AIR && !loc.toString().equals("minecraft:air"))) {
			return null;
		}

		return new BlockMetaPair(block, meta);
	}

	public static class BlockMetaPair {
		public final Block block;
		public final int meta;

		public BlockMetaPair(Block block, int meta) {
			this.block = block;
			this.meta = meta;
		}

		public IBlockState getState() {
			try {
				return meta >= 0 ? block.getStateFromMeta(meta) : block.getDefaultState();
			} catch (Exception e) {
				return block.getDefaultState();
			}
		}
	}

	public ItemCleaner() {
		this.setMaxStackSize(1);
		this.setMaxDamage(Settings.miscSettings.cleanerDurability);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!player.canPlayerEdit(pos, facing, player.getHeldItem(hand))) {
			return EnumActionResult.FAIL;
		}

		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		int meta = block.getMetaFromState(state);
		String blockId = block.getRegistryName() != null ? block.getRegistryName().toString() : "";
		if (blockId.isEmpty()) {
			return EnumActionResult.PASS;
		}

		Map<String, BlockMetaPair> mappings = getCleanerMappings();
		String keyWithMeta = blockId + ":" + meta;
		BlockMetaPair targetPair = mappings.get(keyWithMeta);

		if (targetPair == null) {
			targetPair = mappings.get(blockId);
		}

		if (targetPair == null) {
			return EnumActionResult.PASS;
		}

		IBlockState targetState = targetPair.getState();
		if (targetState == state) {
			return EnumActionResult.PASS;
		}

		ItemStack stack = player.getHeldItem(hand);
		world.playEvent(player, 2001, pos, Block.getStateId(state));
		double centerX = pos.getX() + 0.5D + facing.getXOffset() * 0.75D;
		double centerY = pos.getY() + 0.5D + facing.getYOffset() * 0.75D;
		double centerZ = pos.getZ() + 0.5D + facing.getZOffset() * 0.75D;
		double xOffset = facing.getXOffset() != 0 ? 0.1D : 0.35D;
		double yOffset = facing.getYOffset() != 0 ? 0.1D : 0.35D;
		double zOffset = facing.getZOffset() != 0 ? 0.1D : 0.35D;

		if (world instanceof WorldServer) {
			WorldServer ws = (WorldServer) world;
			ws.spawnParticle(EnumParticleTypes.WATER_SPLASH, centerX, centerY, centerZ, 25, xOffset, yOffset, zOffset, 0.1D);
			ws.spawnParticle(EnumParticleTypes.CLOUD, centerX, centerY, centerZ, 8, xOffset, yOffset, zOffset, 0.05D);
			ws.spawnParticle(EnumParticleTypes.SPIT, centerX, centerY, centerZ, 12, xOffset, yOffset, zOffset, 0.05D);
		} else if (world.isRemote) {
			for (int i = 0; i < 15; i++) {
				double x = centerX + (world.rand.nextDouble() - 0.5D) * 2.0D * xOffset;
				double y = centerY + (world.rand.nextDouble() - 0.5D) * 2.0D * yOffset;
				double z = centerZ + (world.rand.nextDouble() - 0.5D) * 2.0D * zOffset;
				double mx = facing.getXOffset() * 0.05D + (world.rand.nextDouble() - 0.5D) * 0.05D;
				double my = facing.getYOffset() * 0.05D + (world.rand.nextDouble() - 0.5D) * 0.05D;
				double mz = facing.getZOffset() * 0.05D + (world.rand.nextDouble() - 0.5D) * 0.05D;
				world.spawnParticle(EnumParticleTypes.WATER_SPLASH, x, y, z, mx, my, mz);
				if (i % 3 == 0) {
					world.spawnParticle(EnumParticleTypes.CLOUD, x, y, z, mx * 0.5D, my * 0.5D, mz * 0.5D);
				}
				if (i % 2 == 0) {
					world.spawnParticle(EnumParticleTypes.SPIT, x, y, z, mx, my, mz);
				}
			}
		}
		if (!world.isRemote) {
			world.setBlockState(pos, targetState, 3);
			if (!player.capabilities.isCreativeMode) {
				stack.damageItem(1, player);
			}
		}

		return EnumActionResult.SUCCESS;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		ClientProxy.addMultiLineDescription(tooltip, TextFormatting.BOLD + "" + TextFormatting.GRAY + I18n.format("item.totf:cleaner.desc"));
	}
}
