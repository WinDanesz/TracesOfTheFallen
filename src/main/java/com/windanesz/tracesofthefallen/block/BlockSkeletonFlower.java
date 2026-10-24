package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.misc.IncenseEffects;
import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.init.ModPotions;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A simple decorative skeleton-with-flowers block: just the facing rotation inherited from
 * {@link BlockDecoration}, an ambient particle + potion effect aura, and a loot table. No tile entity,
 * no special rendering - the model itself handles everything visually.
 */
public class BlockSkeletonFlower extends BlockDecoration {

	// Same radius as TileEntityCenser's aura (a 5x5 area) - reusing the incense mist effect's own scale.
	private static final int AURA_RADIUS = 2;
	private static final int AURA_TICK_RATE = 20;

	public ResourceLocation lootTable;

	public BlockSkeletonFlower(Material materialIn) {
		super(materialIn);
	}

	public BlockSkeletonFlower setLootTable(ResourceLocation lootTable) {
		this.lootTable = lootTable;
		return this;
	}

	// Material alone isn't reliable for "breakable without a tool" (see BlockSkeletonCorpse) - state it
	// explicitly instead of hoping the material grants it.
	@Override
	public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
		return true;
	}

	// Walk-through: BlockDecoration's default collision box matches its (now 1/3-tall) selection box,
	// which would otherwise let entities stand on it like a low platform.
	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return NULL_AABB;
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess iBlockAccess, BlockPos pos, IBlockState state, int fortune) {
		if (!(iBlockAccess instanceof WorldServer) || ((WorldServer) iBlockAccess).isRemote || this.lootTable == null) {
			return Collections.emptyList();
		}
		World realWorld = (World) iBlockAccess;
		LootTable table = realWorld.getLootTableManager().getLootTableFromLocation(this.lootTable);
		LootContext context = new LootContext.Builder((WorldServer) realWorld).withLuck(0).build();
		return table.generateLootForPools(realWorld.rand, context);
	}

	// No tile entity to drive a per-instance tick, so this block schedules its own recurring update tick
	// (same self-perpetuating pattern as BlockStonePressurePlate: reschedule at the end of updateTick),
	// started once when the block enters the world.
	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		super.onBlockAdded(worldIn, pos, state);
		if (!worldIn.isRemote) {
			worldIn.scheduleUpdate(new BlockPos(pos), this, tickRate(worldIn));
		}
	}

	@Override
	public int tickRate(World worldIn) {
		return AURA_TICK_RATE;
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		if (worldIn.isRemote) {
			return;
		}

		List<PotionEffect> effects = parseAuraEffects();
		if (!effects.isEmpty()) {
			AxisAlignedBB area = IncenseEffects.getAuraArea(pos, AURA_RADIUS);
			List<EntityLivingBase> nearby = worldIn.getEntitiesWithinAABB(EntityLivingBase.class, area,
					entity -> entity != null && entity.isEntityAlive());
			for (EntityLivingBase entity : nearby) {
				for (PotionEffect effect : effects) {
					// Re-applying the full configured duration each tick keeps it topped up while the
					// entity stays in range, and lets it decay naturally on its own once they leave -
					// unlike IncenseEffects.applyAuraEffects, which hardcodes its own fixed refresh
					// duration and would silently ignore a configured duration here.
					entity.addPotionEffect(new PotionEffect(effect.getPotion(), effect.getDuration(), effect.getAmplifier(), false, true));
				}
			}
		}

		worldIn.scheduleUpdate(new BlockPos(pos), this, tickRate(worldIn));
	}

	/**
	 * Parses {@link Settings.MiscSettings#skeletonFlowerAuraEffects}. Not cached - like
	 * {@code PorcelainLiquids.getDefinitions()}, this list is tiny and only re-read once a second, so
	 * re-parsing it directly off the live config picks up in-game config edits for free.
	 */
	private static List<PotionEffect> parseAuraEffects() {
		List<PotionEffect> effects = new ArrayList<>();
		for (String entry : Settings.miscSettings.skeletonFlowerAuraEffects) {
			PotionEffect effect = parseAuraEffect(entry);
			if (effect != null) {
				effects.add(effect);
			}
		}
		return effects;
	}

	@Nullable
	private static PotionEffect parseAuraEffect(String entry) {
		if (entry == null) {
			return null;
		}
		String[] parts = entry.split("\\|");
		if (parts.length != 3) {
			return null;
		}
		Potion potion = getPotion(parts[0]);
		if (potion == null) {
			return null;
		}
		int amplifier = Math.max(0, parseInt(parts[1], 0));
		int duration = parseInt(parts[2], 0);
		return duration > 0 ? new PotionEffect(potion, duration, amplifier, false, true) : null;
	}

	@Nullable
	private static Potion getPotion(String potionName) {
		String cleaned = potionName == null ? "" : potionName.trim();
		if (cleaned.isEmpty()) {
			return null;
		}
		try {
			return Potion.REGISTRY.getObject(new ResourceLocation(cleaned));
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private static int parseInt(String value, int fallback) {
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	// Same pattern as BlockIncenseBurner/BlockCenser's randomDisplayTick, just unconditional (no lit/fuel
	// state to check since there's no tile entity) and using the incense mist particle's default color.
	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if (rand.nextInt(3) == 0) {
			return;
		}

		int particleCount = 2 + rand.nextInt(2);
		double spreadDiameter = AURA_RADIUS * 2.0D;
		int color = ModPotions.serenity != null ? ModPotions.serenity.getLiquidColor() : 0xFFFFFF;

		for (int i = 0; i < particleCount; i++) {
			double x = pos.getX() + 0.5D + (rand.nextDouble() - 0.5D) * spreadDiameter;
			double y = pos.getY() + 0.02D + rand.nextDouble() * 0.04D;
			double z = pos.getZ() + 0.5D + (rand.nextDouble() - 0.5D) * spreadDiameter;
			double motionX = (rand.nextDouble() - 0.5D) * 0.0006D;
			double motionY = rand.nextDouble() * 0.00015D;
			double motionZ = (rand.nextDouble() - 0.5D) * 0.0006D;

			TracesOfTheFallen.proxy.spawnIncenseFloorMistParticle(worldIn, x, y, z, motionX, motionY, motionZ, color);
		}
	}
}
