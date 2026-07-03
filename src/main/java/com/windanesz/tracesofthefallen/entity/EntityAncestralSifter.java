package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.SifterLootResolver;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.block.BlockAncestralSifter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EntityAncestralSifter extends Entity {

	private static final float WIDTH = 2.0F;
	private static final float HEIGHT = 1.8F;
	private static final double THICKNESS = 5.0D / 16.0D;
	private static final int INTERNAL_SLOT_COUNT = 54;
	private static final int REPAIR_FEATHER_COST = 8;
	private static final int REPAIR_STRING_COST = 2;

	private static final DataParameter<BlockPos> ANCHOR_POS = EntityDataManager.createKey(EntityAncestralSifter.class, DataSerializers.BLOCK_POS);
	private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(EntityAncestralSifter.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> FACING = EntityDataManager.createKey(EntityAncestralSifter.class, DataSerializers.VARINT);
	private static final DataParameter<Boolean> BROKEN = EntityDataManager.createKey(EntityAncestralSifter.class, DataSerializers.BOOLEAN);

	private final SifterInventory inventory = new SifterInventory();
	private int durability = getConfiguredDurability();
	private int breakHits;
	private int healthRegenCooldown;
	private int lootCooldown;
	private int particleCooldown;

	public EntityAncestralSifter(World worldIn) {
		super(worldIn);
		this.isImmuneToFire = true;
		setSize(WIDTH, HEIGHT);
		lootCooldown = getConfiguredLootInterval();
	}

	public EntityAncestralSifter(World worldIn, BlockPos pos, BlockAncestralSifter.SifterVariant variant, EnumFacing facing) {
		this(worldIn);
		setAnchorPos(pos);
		setVariant(variant);
		setFacing(facing);
		setPosition(getPlacementCenterX(pos, facing), pos.getY(), getPlacementCenterZ(pos, facing));
	}

	@Override
	protected void entityInit() {
		dataManager.register(ANCHOR_POS, BlockPos.ORIGIN);
		dataManager.register(VARIANT, BlockAncestralSifter.SifterVariant.GOLDEN.ordinal());
		dataManager.register(FACING, EnumFacing.NORTH.getHorizontalIndex());
		dataManager.register(BROKEN, false);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		BlockPos anchorPos = getAnchorPos();
		setPosition(getPlacementCenterX(anchorPos, getFacing()), anchorPos.getY(), getPlacementCenterZ(anchorPos, getFacing()));
		motionX = 0.0D;
		motionY = 0.0D;
		motionZ = 0.0D;
		if (!world.isRemote) {
			updateHealthRegen();
		}
		if (!world.isRemote && !isBroken()) {
			updateAutoSifting();
		}
		if (world.isRemote && !isBroken()) {
			updateSifterParticles();
		}
	}

	@Override
	public void setPosition(double x, double y, double z) {
		super.setPosition(x, y, z);
		setEntityBoundingBox(getPlacementBoundsForCenter(x, y, z, getFacingOrDefault()));
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		setAnchorPos(new BlockPos(compound.getInteger("AnchorX"), compound.getInteger("AnchorY"), compound.getInteger("AnchorZ")));
		setVariant(BlockAncestralSifter.SifterVariant.byMetadata(compound.getInteger("Variant")));
		setFacing(EnumFacing.byHorizontalIndex(compound.getInteger("Facing") & 3));
		durability = compound.hasKey("Durability") ? compound.getInteger("Durability") : getConfiguredDurability();
		if (durability > getConfiguredDurability()) {
			durability = getConfiguredDurability();
		}
		breakHits = compound.getInteger("BreakHits");
		healthRegenCooldown = compound.getInteger("HealthRegenCooldown");
		lootCooldown = compound.hasKey("LootCooldown") ? compound.getInteger("LootCooldown") : getConfiguredLootInterval();
		inventory.deserializeNBT(compound.getCompoundTag("Inventory"));
		setBroken(durability <= 0);
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		compound.setInteger("AnchorX", getAnchorPos().getX());
		compound.setInteger("AnchorY", getAnchorPos().getY());
		compound.setInteger("AnchorZ", getAnchorPos().getZ());
		compound.setInteger("Variant", getVariant().getMetadata());
		compound.setInteger("Facing", getFacing().getHorizontalIndex());
		compound.setInteger("Durability", durability);
		compound.setInteger("BreakHits", breakHits);
		compound.setInteger("HealthRegenCooldown", healthRegenCooldown);
		compound.setInteger("LootCooldown", lootCooldown);
		compound.setTag("Inventory", inventory.serializeNBT());
	}

	@Override
	public boolean canBeCollidedWith() {
		return !isDead;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox() {
		return getEntityBoundingBox();
	}

	@Override
	public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
		if (!world.isRemote && hand == EnumHand.MAIN_HAND) {
			if (retrieveStoredItems(player)) {
				return true;
			}
			if (isBroken()) {
				if (canRepair(player)) {
					repair(player);
				} else {
					player.sendStatusMessage(new TextComponentTranslation("totf.ancestral_sifter.repair_requirements", REPAIR_FEATHER_COST, REPAIR_STRING_COST), true);
				}
			}
		}
		return true;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (isEntityInvulnerable(source)) {
			return false;
		}
		if (!source.isExplosion() && !(source.getTrueSource() instanceof EntityPlayer)) {
			return false;
		}

		if (!world.isRemote) {
			if (source.isExplosion()) {
				dropAndRemove();
				return true;
			}

			breakHits++;
			healthRegenCooldown = 40;
			if (breakHits >= getConfiguredHealth()) {
				dropAndRemove();
			}
		}

		return true;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}

	@Nullable
	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public ItemStack getPickedResult(net.minecraft.util.math.RayTraceResult target) {
		return getVariant().createStack();
	}

	public BlockAncestralSifter.SifterVariant getVariant() {
		return BlockAncestralSifter.SifterVariant.byMetadata(dataManager.get(VARIANT));
	}

	public BlockPos getAnchorPos() {
		return dataManager.get(ANCHOR_POS);
	}

	public void setAnchorPos(BlockPos pos) {
		dataManager.set(ANCHOR_POS, pos);
	}

	public void setVariant(BlockAncestralSifter.SifterVariant variant) {
		dataManager.set(VARIANT, variant.getMetadata());
	}

	public EnumFacing getFacing() {
		return EnumFacing.byHorizontalIndex(dataManager.get(FACING) & 3);
	}

	public void setFacing(EnumFacing facing) {
		dataManager.set(FACING, facing.getHorizontalIndex());
	}

	public boolean isBroken() {
		return dataManager.get(BROKEN);
	}

	public boolean isWeathered() {
		return !isBroken() && getConfiguredDurability() > 1 && durability == 1;
	}

	private void setBroken(boolean broken) {
		dataManager.set(BROKEN, broken);
	}

	private void dropAndRemove() {
		entityDropItem(getVariant().createStack(), 0.0F);
		for (ItemStack stack : inventory.getStoredStacks()) {
			if (!stack.isEmpty()) {
				entityDropItem(stack.copy(), 0.0F);
			}
		}
		setDead();
	}

	private static int getConfiguredDurability() {
		return Math.max(1, Settings.miscSettings.sifterDurability);
	}

	private static int getConfiguredHealth() {
		return Math.max(1, Settings.miscSettings.sifterHealth);
	}

	private static int getConfiguredLootInterval() {
		return Math.max(20, Settings.miscSettings.sifterLootInterval);
	}

	private static int getConfiguredSlots() {
		return Math.max(1, Math.min(INTERNAL_SLOT_COUNT, Settings.miscSettings.sifterInventorySlots));
	}

	public static AxisAlignedBB getPlacementBounds(BlockPos pos, EnumFacing facing) {
		return getPlacementBoundsForCenter(getPlacementCenterX(pos, facing), pos.getY(), getPlacementCenterZ(pos, facing), facing);
	}

	private static double getPlacementCenterX(BlockPos pos, EnumFacing facing) {
		return pos.getX() + 0.5D + (facing.rotateYCCW().getXOffset() * 0.5D);
	}

	private static double getPlacementCenterZ(BlockPos pos, EnumFacing facing) {
		return pos.getZ() + 0.5D + (facing.rotateYCCW().getZOffset() * 0.5D);
	}

	private EnumFacing getFacingOrDefault() {
		try {
			Integer facing = this.dataManager == null ? null : this.dataManager.get(FACING);
			return facing == null ? EnumFacing.NORTH : EnumFacing.byHorizontalIndex(facing & 3);
		} catch (Exception ignored) {
			return EnumFacing.NORTH;
		}
	}

	private static AxisAlignedBB getPlacementBoundsForCenter(double centerX, double minY, double centerZ, EnumFacing facing) {
		double halfWide = 1.0D;
		double halfThin = THICKNESS / 2.0D;

		if (facing == EnumFacing.EAST || facing == EnumFacing.WEST) {
			return new AxisAlignedBB(
					centerX - halfThin,
					minY,
					centerZ - halfWide,
					centerX + halfThin,
					minY + HEIGHT,
					centerZ + halfWide);
		}

		return new AxisAlignedBB(
				centerX - halfWide,
				minY,
				centerZ - halfThin,
				centerX + halfWide,
				minY + HEIGHT,
				centerZ + halfThin);
	}

	private void updateSifterParticles() {
		if (particleCooldown > 0) {
			particleCooldown--;
			return;
		}

		particleCooldown = 20 + rand.nextInt(41);
		int particleCount = 1 + rand.nextInt(2);
		for (int i = 0; i < particleCount; i++) {
			spawnSifterParticle();
		}
	}

	private void spawnSifterParticle() {
		EnumFacing facing = getFacing();
		double halfWide = 1.0D;
		double halfThin = THICKNESS / 2.0D;
		double velocityX = facing.getXOffset() * (0.045D + rand.nextDouble() * 0.015D);
		double velocityZ = facing.getZOffset() * (0.045D + rand.nextDouble() * 0.015D);
		double xOffset;
		double yOffset = 0.15D + rand.nextDouble() * (HEIGHT - 0.3D);
		double zOffset;

		if (facing == EnumFacing.EAST || facing == EnumFacing.WEST) {
			xOffset = -facing.getXOffset() * halfThin * 0.55D;
			zOffset = (rand.nextDouble() * 2.0D - 1.0D) * halfWide * 0.4D;
		} else {
			xOffset = (rand.nextDouble() * 2.0D - 1.0D) * halfWide * 0.4D;
			zOffset = -facing.getZOffset() * halfThin * 0.55D;
		}

		TracesOfTheFallen.proxy.spawnSifterCloudParticle(
				world,
				posX + xOffset,
				posY + yOffset,
				posZ + zOffset,
				velocityX,
				0.0D,
				velocityZ);
	}

	private void updateAutoSifting() {
		if (!(world instanceof WorldServer)) {
			return;
		}
		if (getFirstEmptyLootSlot() < 0) {
			return;
		}
		if (lootCooldown > 0) {
			lootCooldown--;
			return;
		}

		lootCooldown = getConfiguredLootInterval();
		insertGeneratedLoot(SifterLootResolver.generateLoot((WorldServer) world, getAnchorPos()));
	}

	private void insertGeneratedLoot(List<ItemStack> generatedLoot) {
		for (ItemStack stack : generatedLoot) {
			if (stack.isEmpty() || isBroken()) {
				continue;
			}
			int slot = getFirstEmptyLootSlot();
			if (slot < 0) {
				return;
			}
			inventory.setStackInSlot(slot, stack.copy());
			durability = Math.max(0, durability - 1);
			setBroken(durability <= 0);
		}
	}

	private void updateHealthRegen() {
		if (breakHits <= 0) {
			healthRegenCooldown = 0;
			return;
		}
		if (healthRegenCooldown > 0) {
			healthRegenCooldown--;
			return;
		}
		breakHits--;
		healthRegenCooldown = breakHits > 0 ? 40 : 0;
	}

	private boolean canRepair(EntityPlayer player) {
		return player.capabilities.isCreativeMode
				|| (countPlayerItem(player, Items.FEATHER) >= REPAIR_FEATHER_COST && countPlayerItem(player, Items.STRING) >= REPAIR_STRING_COST);
	}

	private void repair(EntityPlayer player) {
		if (!player.capabilities.isCreativeMode) {
			consumePlayerItem(player, Items.FEATHER, REPAIR_FEATHER_COST);
			consumePlayerItem(player, Items.STRING, REPAIR_STRING_COST);
		}
		durability = getConfiguredDurability();
		setBroken(false);
		lootCooldown = getConfiguredLootInterval();
	}

	private static int countPlayerItem(EntityPlayer player, Item item) {
		int count = 0;
		for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++) {
			ItemStack stack = player.inventory.getStackInSlot(slot);
			if (stack.getItem() == item) {
				count += stack.getCount();
			}
		}
		return count;
	}

	private static void consumePlayerItem(EntityPlayer player, Item item, int amount) {
		int remaining = amount;
		for (int slot = 0; slot < player.inventory.getSizeInventory() && remaining > 0; slot++) {
			ItemStack stack = player.inventory.getStackInSlot(slot);
			if (stack.getItem() != item || stack.isEmpty()) {
				continue;
			}
			int consumed = Math.min(remaining, stack.getCount());
			stack.shrink(consumed);
			remaining -= consumed;
		}
	}

	private boolean retrieveStoredItems(EntityPlayer player) {
		boolean retrievedAny = false;
		for (int slot = 0; slot < inventory.getSlots(); slot++) {
			ItemStack stack = inventory.getStackInSlot(slot);
			if (stack.isEmpty()) {
				continue;
			}

			giveItemToPlayer(player, stack.copy());
			inventory.setStackInSlot(slot, ItemStack.EMPTY);
			retrievedAny = true;
		}
		return retrievedAny;
	}

	private static void giveItemToPlayer(EntityPlayer player, ItemStack stack) {
		if (!player.inventory.addItemStackToInventory(stack)) {
			player.dropItem(stack, false);
		}
	}

	private int getFirstEmptyLootSlot() {
		for (int slot = 0; slot < inventory.getSlots(); slot++) {
			if (inventory.getStackInSlot(slot).isEmpty()) {
				return slot;
			}
		}
		return -1;
	}

	private class SifterInventory extends ItemStackHandler {

		private SifterInventory() {
			super(INTERNAL_SLOT_COUNT);
		}

		@Override
		public int getSlots() {
			return getConfiguredSlots();
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return isSlotEnabled(slot) ? super.getStackInSlot(slot) : ItemStack.EMPTY;
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			if (isSlotEnabled(slot)) {
				super.setStackInSlot(slot, stack);
			}
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			return isSlotEnabled(slot) ? super.insertItem(slot, stack, simulate) : stack;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return isSlotEnabled(slot) ? super.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
		}

		private boolean isSlotEnabled(int slot) {
			return slot >= 0 && slot < getConfiguredSlots();
		}

		private List<ItemStack> getStoredStacks() {
			List<ItemStack> stacks = new ArrayList<>();
			for (ItemStack stack : this.stacks) {
				if (!stack.isEmpty()) {
					stacks.add(stack);
				}
			}
			return stacks;
		}
	}
}
