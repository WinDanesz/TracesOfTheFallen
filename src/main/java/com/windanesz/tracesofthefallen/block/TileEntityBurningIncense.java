package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.IncenseEffects;
import com.windanesz.tracesofthefallen.init.ModPotions;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class TileEntityBurningIncense extends TileEntity implements ITickable {

	private static final int DEFAULT_MIST_COLOR = 0xD7E3E5;
	public static final String ITEM_DATA_TAG = "BurningIncenseData";
	private int remainingBurnTime = -1;
	private int totalBurnTime = -1;
	private int effectRefreshCooldown = 0;
	private int particleCooldown = 0;
	private boolean lit;
	private boolean burnedOut = false;
	private final List<IncenseAuraEffect> auraEffects = new ArrayList<>();

	protected TileEntityBurningIncense() {
		this(true);
	}

	protected TileEntityBurningIncense(boolean lit) {
		this.lit = lit;
	}

	@Override
	public void update() {
		if (world == null) {
			return;
		}

		if (world.isRemote) {
			updateClient();
			return;
		}

		if (burnedOut || !lit) {
			return;
		}

		if (remainingBurnTime <= 0) {
			finishBurning();
			return;
		}

		remainingBurnTime--;
		boolean auraEffectsChanged = tickAuraEffects();
		onBurnProgressUpdated();

		if (effectRefreshCooldown <= 0) {
			IncenseEffects.applyAuraEffects(world, getAuraArea(), getAuraPotions(), getSerenityAmplifier());
			effectRefreshCooldown = IncenseEffects.EFFECT_REFRESH_INTERVAL - 1;
			markDirty();
		} else {
			effectRefreshCooldown--;
		}

		if (auraEffectsChanged && auraEffects.isEmpty()) {
			effectRefreshCooldown = 0;
			markDirty();
		}

		if (remainingBurnTime == 0) {
			finishBurning();
		}
	}

	private void updateClient() {
		if (burnedOut || !lit || remainingBurnTime <= 0) {
			return;
		}

		remainingBurnTime--;

		if (particleCooldown > 0) {
			particleCooldown--;
			return;
		}

		particleCooldown = 7 + world.rand.nextInt(3);
		spawnBurningParticle();
	}

	protected abstract void spawnBurningParticle();

	protected int getInitialBurnTime() {
		return IncenseEffects.getRandomBurnDuration(world);
	}

	protected boolean supportsFuelItems() {
		return false;
	}

	protected boolean getDefaultLitState() {
		return true;
	}

	protected void onBurnProgressUpdated() {
	}

	protected void onBurnFinished() {
	}

	protected void spawnSmokeParticle(double x, double y, double z, double motionX, double motionY, double motionZ) {
		IncenseEffects.spawnSmokeParticle(world, x, y, z, motionX, motionY, motionZ, getAuraParticleColor());
	}

	protected AxisAlignedBB getAuraArea() {
		return IncenseEffects.getAuraArea(pos);
	}

	protected int getSerenityAmplifier() {
		return 0;
	}

	public int getAuraParticleColor() {
		List<PotionEffect> effects = getAuraPotions();
		if (!effects.isEmpty()) {
			Potion potion = effects.get(0).getPotion();
			if (potion != null) {
				return potion.getLiquidColor();
			}
		}
		return ModPotions.serenity != null ? ModPotions.serenity.getLiquidColor() : DEFAULT_MIST_COLOR;
	}

	public int getRemainingPotionDurationTicks() {
		int maxDuration = 0;
		for (IncenseAuraEffect effect : auraEffects) {
			if (effect.potion != null) {
				maxDuration = Math.max(maxDuration, effect.duration);
			}
		}
		return maxDuration;
	}

	public boolean isActivelyBurning() {
		return lit && !burnedOut && remainingBurnTime > 0;
	}

	public void applyAuraAt(World auraWorld, AxisAlignedBB area) {
		IncenseEffects.applyAuraEffects(auraWorld, area, getAuraPotions(), getSerenityAmplifier());
	}

	public void tickHeldItemBurningState() {
		if (!isActivelyBurning()) {
			return;
		}

		remainingBurnTime--;
		tickAuraEffects();
		if (remainingBurnTime <= 0) {
			lit = false;
			auraEffects.clear();
		}
	}

	public NBTTagCompound writeBurningData(NBTTagCompound compound) {
		compound.setInteger("RemainingBurnTime", remainingBurnTime);
		compound.setInteger("TotalBurnTime", totalBurnTime);
		compound.setInteger("EffectRefreshCooldown", effectRefreshCooldown);
		compound.setBoolean("Lit", lit);
		compound.setBoolean("BurnedOut", burnedOut);
		NBTTagList auraEffectList = new NBTTagList();
		for (IncenseAuraEffect effect : auraEffects) {
			auraEffectList.appendTag(effect.serializeNBT());
		}
		compound.setTag("AuraEffects", auraEffectList);
		return compound;
	}

	public void readBurningData(NBTTagCompound compound) {
		if (compound == null) {
			return;
		}

		remainingBurnTime = compound.hasKey("RemainingBurnTime") ? compound.getInteger("RemainingBurnTime") : -1;
		totalBurnTime = compound.hasKey("TotalBurnTime") ? compound.getInteger("TotalBurnTime") : remainingBurnTime;
		effectRefreshCooldown = compound.getInteger("EffectRefreshCooldown");
		lit = compound.hasKey("Lit") ? compound.getBoolean("Lit") : (remainingBurnTime > 0 || getDefaultLitState());
		burnedOut = compound.getBoolean("BurnedOut");
		auraEffects.clear();
		NBTTagList auraEffectList = compound.getTagList("AuraEffects", 10);
		for (int i = 0; i < auraEffectList.tagCount(); i++) {
			IncenseAuraEffect effect = IncenseAuraEffect.deserializeNBT(auraEffectList.getCompoundTagAt(i));
			if (effect != null) {
				auraEffects.add(effect);
			}
		}
	}

	protected int getRemainingBurnTime() {
		return remainingBurnTime;
	}

	protected int getTotalBurnTime() {
		return totalBurnTime;
	}

	protected final void setBurnedOut(boolean burnedOut) {
		this.burnedOut = burnedOut;
	}

	protected final void setLit(boolean lit) {
		this.lit = lit;
	}

	public final boolean isLit() {
		return lit;
	}

	public final boolean isBurnedOut() {
		return burnedOut;
	}

	public final boolean canIgnite() {
		if (burnedOut || lit) {
			return false;
		}

		if (supportsFuelItems()) {
			return remainingBurnTime > 0;
		}

		return remainingBurnTime != 0;
	}

	public final boolean tryLight() {
		return ignite();
	}

	public final boolean canAcceptFuel(ItemStack stack) {
		return supportsFuelItems() && !burnedOut && IncenseEffects.getFuelBurnDuration(stack) > 0;
	}

	public final boolean addFuel(ItemStack stack) {
		if (!canAcceptFuel(stack)) {
			return false;
		}

		int addedBurnTime = IncenseEffects.getFuelBurnDuration(stack);
		if (addedBurnTime <= 0) {
			return false;
		}

		remainingBurnTime = Math.max(0, remainingBurnTime) + addedBurnTime;
		totalBurnTime = Math.max(totalBurnTime, remainingBurnTime);
		markDirty();
		onBurnProgressUpdated();
		syncToClient();
		return true;
	}

	public final boolean canApplyPotion(ItemStack stack) {
		return lit && remainingBurnTime > 0 && !IncenseEffects.getWhitelistedPotionEffects(stack).isEmpty();
	}

	public final boolean applyPotionEffects(ItemStack stack) {
		List<PotionEffect> effects = IncenseEffects.getWhitelistedPotionEffects(stack);
		if (!lit || remainingBurnTime <= 0 || effects.isEmpty()) {
			return false;
		}

		auraEffects.clear();
		for (PotionEffect effect : effects) {
			auraEffects.add(new IncenseAuraEffect(effect));
		}

		effectRefreshCooldown = 0;
		markDirty();
		syncToClient();
		return true;
	}

	protected final boolean ignite() {
		if (!canIgnite()) {
			return false;
		}

		if (supportsFuelItems()) {
			totalBurnTime = Math.max(totalBurnTime, remainingBurnTime);
		} else {
			remainingBurnTime = getInitialBurnTime();
			totalBurnTime = remainingBurnTime;
		}

		effectRefreshCooldown = 0;
		particleCooldown = 0;
		lit = true;
		markDirty();
		onBurnProgressUpdated();
		syncToClient();
		return true;
	}

	protected final void syncToClient() {
		if (world == null || world.isRemote) {
			return;
		}

		world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		return writeBurningData(compound);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		readBurningData(compound);
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	@Nullable
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
		if (world != null) {
			world.markBlockRangeForRenderUpdate(pos, pos);
		}
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		readFromNBT(tag);
		if (world != null) {
			world.markBlockRangeForRenderUpdate(pos, pos);
		}
	}

	private void finishBurning() {
		lit = false;
		auraEffects.clear();
		onBurnFinished();
		markDirty();
		syncToClient();
	}

	private boolean tickAuraEffects() {
		boolean changed = false;
		Iterator<IncenseAuraEffect> iterator = auraEffects.iterator();
		while (iterator.hasNext()) {
			IncenseAuraEffect effect = iterator.next();
			effect.duration--;
			if (effect.duration <= 0) {
				iterator.remove();
			}
			changed = true;
		}
		return changed;
	}

	private List<PotionEffect> getAuraPotions() {
		if (auraEffects.isEmpty()) {
			return Collections.emptyList();
		}

		List<PotionEffect> effects = new ArrayList<>(auraEffects.size());
		for (IncenseAuraEffect effect : auraEffects) {
			Potion potion = effect.potion;
			if (potion == null) {
				continue;
			}

			effects.add(new PotionEffect(potion, effect.duration, effect.amplifier, effect.ambient, effect.showParticles));
		}
		return effects;
	}

	private static final class IncenseAuraEffect {
		private final Potion potion;
		private int duration;
		private final int amplifier;
		private final boolean ambient;
		private final boolean showParticles;

		private IncenseAuraEffect(PotionEffect effect) {
			this(effect.getPotion(), effect.getDuration(), effect.getAmplifier(), effect.getIsAmbient(), effect.doesShowParticles());
		}

		private IncenseAuraEffect(Potion potion, int duration, int amplifier, boolean ambient, boolean showParticles) {
			this.potion = potion;
			this.duration = duration;
			this.amplifier = amplifier;
			this.ambient = ambient;
			this.showParticles = showParticles;
		}

		private NBTTagCompound serializeNBT() {
			NBTTagCompound compound = new NBTTagCompound();
			if (potion != null && potion.getRegistryName() != null) {
				compound.setString("Potion", potion.getRegistryName().toString());
			}
			compound.setInteger("Duration", duration);
			compound.setInteger("Amplifier", amplifier);
			compound.setBoolean("Ambient", ambient);
			compound.setBoolean("ShowParticles", showParticles);
			return compound;
		}

		@Nullable
		private static IncenseAuraEffect deserializeNBT(NBTTagCompound compound) {
			if (!compound.hasKey("Potion")) {
				return null;
			}

			Potion potion = Potion.getPotionFromResourceLocation(compound.getString("Potion"));
			if (potion == null) {
				return null;
			}

			return new IncenseAuraEffect(potion,
					compound.getInteger("Duration"),
					compound.getInteger("Amplifier"),
					compound.getBoolean("Ambient"),
					compound.getBoolean("ShowParticles"));
		}
	}
}
