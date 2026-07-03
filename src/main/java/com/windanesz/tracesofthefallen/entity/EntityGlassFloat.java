package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.block.BlockGlassFloat;
import com.windanesz.tracesofthefallen.init.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EntityGlassFloat extends Entity {

	private static final float COLLISION_SIZE = 0.75F;
	private static final double COLLISION_Y_OFFSET = -4.0D / 16.0D;
	private static final double WEIGHT_SINK_OFFSET = 0.5D;
	private static final double WEIGHT_SINK_SPEED = 0.2D;
	private static final int FISHING_ROLL_INTERVAL = 40;

	private static final DataParameter<BlockPos> ANCHOR_POS = EntityDataManager.createKey(EntityGlassFloat.class, DataSerializers.BLOCK_POS);
	private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(EntityGlassFloat.class, DataSerializers.VARINT);
	private static final DataParameter<Float> TARGET_SINK_OFFSET = EntityDataManager.createKey(EntityGlassFloat.class, DataSerializers.FLOAT);
	private static final DataParameter<Integer> ROPE_COUNT = EntityDataManager.createKey(EntityGlassFloat.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> HOOK_COUNT = EntityDataManager.createKey(EntityGlassFloat.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> ROPE_CAPACITY = EntityDataManager.createKey(EntityGlassFloat.class, DataSerializers.VARINT);
	private static final DataParameter<String> ROPE_ITEM_ID = EntityDataManager.createKey(EntityGlassFloat.class, DataSerializers.STRING);
	private static final DataParameter<String> HOOK_LOOT_DATA = EntityDataManager.createKey(EntityGlassFloat.class, DataSerializers.STRING);

	private BlockPos anchorPos = BlockPos.ORIGIN;
	private double sinkOffset;
	private final List<ItemStack> hookLootStacks = new ArrayList<>();
	private final List<Boolean> hookSquidSlots = new ArrayList<>();
	private final List<ItemStack> clientHookLootStacks = new ArrayList<>();
	private final List<Boolean> clientHookSquidSlots = new ArrayList<>();
	private String clientHookLootData = "";

	public EntityGlassFloat(World worldIn) {
		super(worldIn);
		this.noClip = false;
		this.setSize(COLLISION_SIZE, COLLISION_SIZE);
	}

	public EntityGlassFloat(World worldIn, BlockPos anchorPos, BlockGlassFloat.FloatVariant variant) {
		this(worldIn, anchorPos, variant, Settings.miscSettings.glassFloatMaxRopeLength);
	}

	public EntityGlassFloat(World worldIn, BlockPos anchorPos, BlockGlassFloat.FloatVariant variant, int ropeCapacity) {
		this(worldIn);
		setAnchorPos(anchorPos);
		setVariant(variant);
		setPlacedRopeCapacity(ropeCapacity);
		updateAnchorPosition();
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(ANCHOR_POS, BlockPos.ORIGIN);
		this.dataManager.register(VARIANT, 0);
		this.dataManager.register(TARGET_SINK_OFFSET, 0.0F);
		this.dataManager.register(ROPE_COUNT, 0);
		this.dataManager.register(HOOK_COUNT, 0);
		this.dataManager.register(ROPE_CAPACITY, getConfiguredMaxRopeCount());
		this.dataManager.register(ROPE_ITEM_ID, getDefaultRopeItemId());
		this.dataManager.register(HOOK_LOOT_DATA, new NBTTagCompound().toString());
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		this.anchorPos = this.dataManager.get(ANCHOR_POS);

		if (!this.world.isRemote) {
			if (this.dataManager.get(ROPE_COUNT) != getRopeCount()) {
				setRopeCount(getRopeCount());
			}
			if (this.dataManager.get(HOOK_COUNT) != getHookCount()) {
				setHookCount(getHookCount());
			}
		}

		this.motionX = 0.0D;
		this.motionY = 0.0D;
		this.motionZ = 0.0D;

		if (!BlockGlassFloat.isWaterSurface(world, anchorPos.down())) {
			if (!world.isRemote) {
				entityDropItem(BlockGlassFloat.createStackForVariant(getVariant()), 0.0F);
				if (getRopeCount() > 0) {
					entityDropItem(createRopeItemStack(getRopeCount()), 0.0F);
				}
				if (getHookCount() > 0) {
					entityDropItem(new ItemStack(Items.IRON_NUGGET, getHookCount()), 0.0F);
				}
				dropHookLoot();
				setDead();
			}
			return;
		}

		if (!this.world.isRemote) {
			tryCatchSquid();
			tryFillEmptyHooks();
		}

		updateSinkOffset();
		updateAnchorPosition();
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		setAnchorPos(new BlockPos(compound.getInteger("AnchorX"), compound.getInteger("AnchorY"), compound.getInteger("AnchorZ")));
		setVariant(BlockGlassFloat.FloatVariant.byMetadata(compound.getInteger("Variant")));
		int savedRopeCount = compound.getInteger("RopeCount");
		if (compound.hasKey("RopeCapacity")) {
			setPlacedRopeCapacity(compound.getInteger("RopeCapacity"));
		} else {
			setPlacedRopeCapacity(Math.max(savedRopeCount, BlockGlassFloat.getRopeCapacity(this.world, getAnchorPos().down())));
		}
		if (compound.hasKey("RopeItem")) {
			setRopeItem(Item.getByNameOrId(compound.getString("RopeItem")));
		} else {
			setRopeItem(ModItems.silk_rope);
		}
		setRopeCount(savedRopeCount);
		setHookCount(compound.getInteger("HookCount"));
		readHookLootFromNBT(compound);
		syncHookLootData();
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		compound.setInteger("AnchorX", this.anchorPos.getX());
		compound.setInteger("AnchorY", this.anchorPos.getY());
		compound.setInteger("AnchorZ", this.anchorPos.getZ());
		compound.setInteger("Variant", getVariant().getMetadata());
		compound.setInteger("RopeCount", getRopeCount());
		compound.setInteger("HookCount", getHookCount());
		compound.setInteger("RopeCapacity", getPlacedRopeCapacity());
		compound.setString("RopeItem", getRopeItemId());
		writeHookLootToNBT(compound);
	}

	@Override
	public boolean canBeCollidedWith() {
		return !this.isDead;
	}

	@Override
	public boolean canBePushed() {
		return true;
	}

	@Override
	public void applyEntityCollision(Entity entityIn) {
	}

	@Override
	public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
		ItemStack heldItem = player.getHeldItem(hand);

		if (player.isSneaking()) {
			if (!this.world.isRemote) {
				if (getRopeCount() > 0) {
					rollUpRope(player);
				} else {
					giveItemToPlayer(player, BlockGlassFloat.createStackForVariant(getVariant()));
					setDead();
				}
			}
			return true;
		}

		if (heldItem.getItem() == ModItems.silk_rope && getRopeCount() < getMaxRopeCount()) {
			if (!this.world.isRemote) {
				setRopeItem(heldItem.getItem());
				setRopeCount(getRopeCount() + 1);
				if (!player.capabilities.isCreativeMode) {
					heldItem.shrink(1);
				}
			}
			return true;
		}

		if (heldItem.getItem() == Items.IRON_NUGGET && getRopeCount() > 0 && getHookCount() < getMaxHookCount()) {
			if (!this.world.isRemote) {
				setHookCount(getHookCount() + 1);
				if (!player.capabilities.isCreativeMode) {
					heldItem.shrink(1);
				}
			}
			return true;
		}

		if (hasStoredHookCatch()) {
			if (!this.world.isRemote) {
				harvestHookLoot(player, hand);
			}
			return true;
		}

		return false;
	}

	@Override
	protected void doWaterSplashEffect() {
	}

	@Nullable
	@Override
	public AxisAlignedBB getCollisionBox(Entity entityIn) {
		return entityIn.canBePushed() ? entityIn.getEntityBoundingBox() : null;
	}

	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox() {
		return this.getEntityBoundingBox();
	}

	@Override
	public boolean isInvisibleToPlayer(EntityPlayer player) {
		return false;
	}

	public BlockPos getAnchorPos() {
		return anchorPos;
	}

	public void setAnchorPos(BlockPos anchorPos) {
		this.anchorPos = anchorPos;
		this.dataManager.set(ANCHOR_POS, anchorPos);
	}

	public BlockGlassFloat.FloatVariant getVariant() {
		return BlockGlassFloat.FloatVariant.byMetadata(this.dataManager.get(VARIANT));
	}

	public void setVariant(BlockGlassFloat.FloatVariant variant) {
		this.dataManager.set(VARIANT, variant.getMetadata());
	}

	public boolean hasRope() {
		return getRopeCount() > 0;
	}

	public int getRopeCount() {
		return Math.max(0, Math.min(getMaxRopeCount(), this.dataManager.get(ROPE_COUNT)));
	}

	public void setRopeCount(int ropeCount) {
		this.dataManager.set(ROPE_COUNT, Math.max(0, Math.min(getMaxRopeCount(), ropeCount)));
		if (getHookCount() > getMaxHookCount()) {
			setHookCount(getMaxHookCount());
		}
	}

	public int getHookCount() {
		return Math.max(0, Math.min(getMaxHookCount(), this.dataManager.get(HOOK_COUNT)));
	}

	public void setHookCount(int hookCount) {
		this.dataManager.set(HOOK_COUNT, Math.max(0, Math.min(getMaxHookCount(), hookCount)));
	}

	public int getMaxHookCount() {
		return getRopeCount() / 2;
	}

	public int getMaxRopeCount() {
		return Math.min(getConfiguredMaxRopeCount(), getPlacedRopeCapacity());
	}

	public int getPlacedRopeCapacity() {
		return Math.max(0, this.dataManager.get(ROPE_CAPACITY));
	}

	public void setPlacedRopeCapacity(int ropeCapacity) {
		this.dataManager.set(ROPE_CAPACITY, Math.max(0, ropeCapacity));
		if (getRopeCount() > getMaxRopeCount()) {
			setRopeCount(getMaxRopeCount());
		} else if (getHookCount() > getMaxHookCount()) {
			setHookCount(getMaxHookCount());
		}
	}

	private static int getConfiguredMaxRopeCount() {
		return Math.max(1, Settings.miscSettings.glassFloatMaxRopeLength);
	}

	public Item getRopeItem() {
		Item item = Item.getByNameOrId(getRopeItemId());
		return item != null ? item : ModItems.silk_rope;
	}

	public String getRopeItemId() {
		String ropeItemId = this.dataManager.get(ROPE_ITEM_ID);
		return ropeItemId == null || ropeItemId.isEmpty() ? getDefaultRopeItemId() : ropeItemId;
	}

	public void setRopeItem(Item item) {
		ResourceLocation registryName = item != null ? item.getRegistryName() : null;
		this.dataManager.set(ROPE_ITEM_ID, registryName != null ? registryName.toString() : getDefaultRopeItemId());
	}

	private ItemStack createRopeItemStack(int count) {
		return new ItemStack(getRopeItem(), count);
	}

	private void rollUpRope(EntityPlayer player) {
		int ropeCount = getRopeCount();
		boolean returningHook = isHookedSegment(ropeCount);

		if (returningHook) {
			if (removeHookSquid(getHookSlotForSegment(ropeCount))) {
				giveItemToPlayer(player, createInkSacStack());
			}
			ItemStack hookedLoot = removeHookLoot(getHookSlotForSegment(ropeCount));
			if (!hookedLoot.isEmpty()) {
				giveItemToPlayer(player, hookedLoot);
			}
			setHookCount(getHookCount() - 1);
			giveItemToPlayer(player, new ItemStack(Items.IRON_NUGGET));
		}

		setRopeCount(ropeCount - 1);
		giveItemToPlayer(player, createRopeItemStack(1));
	}

	private void giveItemToPlayer(EntityPlayer player, ItemStack stack) {
		if (!player.inventory.addItemStackToInventory(stack)) {
			player.dropItem(stack, false);
		}
	}

	private void giveItemToPlayerHandOrInventory(EntityPlayer player, EnumHand hand, ItemStack stack) {
		ItemStack heldItem = player.getHeldItem(hand);
		if (heldItem.isEmpty()) {
			player.setHeldItem(hand, stack);
			return;
		}

		if (ItemStack.areItemsEqual(heldItem, stack) && ItemStack.areItemStackTagsEqual(heldItem, stack)
				&& heldItem.getCount() + stack.getCount() <= heldItem.getMaxStackSize()) {
			heldItem.grow(stack.getCount());
			return;
		}

		giveItemToPlayer(player, stack);
	}

	private static String getDefaultRopeItemId() {
		ResourceLocation registryName = ModItems.silk_rope.getRegistryName();
		return registryName != null ? registryName.toString() : "minecraft:string";
	}

	private void tryFillEmptyHooks() {
		if (!(this.world instanceof WorldServer) || this.ticksExisted % FISHING_ROLL_INTERVAL != 0 || getHookCount() <= 0) {
			return;
		}

		int emptyHookSlot = getFirstEmptyHookSlot();
		if (emptyHookSlot < 0) {
			return;
		}

		ItemStack lootStack = rollFishingLoot((WorldServer) this.world);
		if (!lootStack.isEmpty()) {
			setHookLoot(emptyHookSlot, lootStack);
		}
	}

	private void tryCatchSquid() {
		if (!(this.world instanceof WorldServer) || getHookCount() <= 0) {
			return;
		}

		int emptyHookSlot = getFirstEmptyHookSlot();
		if (emptyHookSlot < 0) {
			return;
		}

		AxisAlignedBB ropeBounds = this.getEntityBoundingBox().grow(0.25D);
		ropeBounds = new AxisAlignedBB(
				ropeBounds.minX,
				ropeBounds.minY - getRopeCount() - 1.0D,
				ropeBounds.minZ,
				ropeBounds.maxX,
				ropeBounds.maxY,
				ropeBounds.maxZ);

		List<EntitySquid> squids = this.world.getEntitiesWithinAABB(EntitySquid.class, ropeBounds);
		for (EntitySquid squid : squids) {
			if (!squid.isDead) {
				setHookSquid(emptyHookSlot, true);
				squid.setDead();
				return;
			}
		}
	}

	private ItemStack rollFishingLoot(WorldServer worldServer) {
		LootTable lootTable = worldServer.getLootTableManager().getLootTableFromLocation(LootTableList.GAMEPLAY_FISHING);
		LootContext.Builder builder = new LootContext.Builder(worldServer);
		List<ItemStack> generatedLoot = lootTable.generateLootForPools(worldServer.rand, builder.build());

		for (ItemStack stack : generatedLoot) {
			if (!stack.isEmpty()) {
				return stack;
			}
		}

		return ItemStack.EMPTY;
	}

	private int getFirstEmptyHookSlot() {
		for (int i = 0; i < getHookCount(); i++) {
			if (getHookLoot(i).isEmpty() && !hasHookSquid(i)) {
				return i;
			}
		}
		return -1;
	}

	private int getFirstFilledHookSlot() {
		for (int i = 0; i < getHookCount(); i++) {
			if (!getHookLoot(i).isEmpty() || hasHookSquid(i)) {
				return i;
			}
		}
		return -1;
	}

	private boolean hasStoredHookCatch() {
		return getFirstFilledHookSlot() >= 0;
	}

	private void harvestHookLoot(EntityPlayer player, EnumHand hand) {
		int filledHookSlot = getFirstFilledHookSlot();
		if (filledHookSlot < 0) {
			return;
		}

		if (removeHookSquid(filledHookSlot)) {
			giveItemToPlayerHandOrInventory(player, hand, createInkSacStack());
			return;
		}

		ItemStack harvestedStack = removeHookLoot(filledHookSlot);
		if (!harvestedStack.isEmpty()) {
			giveItemToPlayerHandOrInventory(player, hand, harvestedStack);
		}
	}

	private int getHookSlotForSegment(int segment) {
		return (segment / 2) - 1;
	}

	private ItemStack getHookLoot(int hookSlot) {
		ensureHookLootSize(hookSlot + 1);
		return this.hookLootStacks.get(hookSlot);
	}

	public ItemStack getHookLootForRender(int hookSlot) {
		if (!this.world.isRemote) {
			return getHookLoot(hookSlot);
		}

		updateClientHookLoot();
		if (hookSlot < 0 || hookSlot >= this.clientHookLootStacks.size()) {
			return ItemStack.EMPTY;
		}
		return this.clientHookLootStacks.get(hookSlot);
	}

	public boolean hasSquidOnHookForRender(int hookSlot) {
		if (!this.world.isRemote) {
			return hasHookSquid(hookSlot);
		}

		updateClientHookLoot();
		return hookSlot >= 0 && hookSlot < this.clientHookSquidSlots.size() && this.clientHookSquidSlots.get(hookSlot);
	}

	private void setHookLoot(int hookSlot, ItemStack stack) {
		ensureHookLootSize(hookSlot + 1);
		this.hookLootStacks.set(hookSlot, stack.copy());
		this.hookSquidSlots.set(hookSlot, false);
		syncHookLootData();
	}

	private ItemStack removeHookLoot(int hookSlot) {
		if (hookSlot < 0) {
			return ItemStack.EMPTY;
		}
		ensureHookLootSize(hookSlot + 1);
		ItemStack removedStack = this.hookLootStacks.get(hookSlot);
		this.hookLootStacks.set(hookSlot, ItemStack.EMPTY);
		syncHookLootData();
		return removedStack;
	}

	private boolean hasHookSquid(int hookSlot) {
		ensureHookLootSize(hookSlot + 1);
		return this.hookSquidSlots.get(hookSlot);
	}

	private void setHookSquid(int hookSlot, boolean hasSquid) {
		ensureHookLootSize(hookSlot + 1);
		this.hookSquidSlots.set(hookSlot, hasSquid);
		if (hasSquid) {
			this.hookLootStacks.set(hookSlot, ItemStack.EMPTY);
		}
		syncHookLootData();
	}

	private boolean removeHookSquid(int hookSlot) {
		if (hookSlot < 0) {
			return false;
		}
		ensureHookLootSize(hookSlot + 1);
		boolean hadSquid = this.hookSquidSlots.get(hookSlot);
		this.hookSquidSlots.set(hookSlot, false);
		if (hadSquid) {
			syncHookLootData();
		}
		return hadSquid;
	}

	private void ensureHookLootSize(int size) {
		while (this.hookLootStacks.size() < size) {
			this.hookLootStacks.add(ItemStack.EMPTY);
			this.hookSquidSlots.add(false);
		}
	}

	private void dropHookLoot() {
		for (int i = 0; i < getHookCount(); i++) {
			if (removeHookSquid(i)) {
				entityDropItem(createInkSacStack(), 0.0F);
			}
			ItemStack stack = getHookLoot(i);
			if (!stack.isEmpty()) {
				entityDropItem(stack, 0.0F);
				this.hookLootStacks.set(i, ItemStack.EMPTY);
			}
		}
		syncHookLootData();
	}

	private void readHookLootFromNBT(NBTTagCompound compound) {
		this.hookLootStacks.clear();
		NBTTagList hookLootList = compound.getTagList("HookLoot", 10);
		for (int i = 0; i < hookLootList.tagCount(); i++) {
			NBTTagCompound hookLootCompound = hookLootList.getCompoundTagAt(i);
			int slot = hookLootCompound.getInteger("Slot");
			boolean squid = hookLootCompound.getBoolean("Squid");
			if (slot >= 0) {
				if (squid) {
					setHookSquid(slot, true);
				}
				ItemStack stack = new ItemStack(hookLootCompound.getCompoundTag("Stack"));
				if (!stack.isEmpty()) {
					setHookLoot(slot, stack);
				}
			}
		}
	}

	private void writeHookLootToNBT(NBTTagCompound compound) {
		NBTTagList hookLootList = new NBTTagList();
		for (int i = 0; i < this.hookLootStacks.size(); i++) {
			ItemStack stack = this.hookLootStacks.get(i);
			if (!stack.isEmpty()) {
				NBTTagCompound hookLootCompound = new NBTTagCompound();
				hookLootCompound.setInteger("Slot", i);
				hookLootCompound.setTag("Stack", stack.writeToNBT(new NBTTagCompound()));
				hookLootCompound.setBoolean("Squid", false);
				hookLootList.appendTag(hookLootCompound);
			} else if (i < this.hookSquidSlots.size() && this.hookSquidSlots.get(i)) {
				NBTTagCompound hookLootCompound = new NBTTagCompound();
				hookLootCompound.setInteger("Slot", i);
				hookLootCompound.setBoolean("Squid", true);
				hookLootList.appendTag(hookLootCompound);
			}
		}
		compound.setTag("HookLoot", hookLootList);
	}

	private void syncHookLootData() {
		this.dataManager.set(HOOK_LOOT_DATA, serializeHookLootData());
	}

	private String serializeHookLootData() {
		NBTTagCompound compound = new NBTTagCompound();
		writeHookLootToNBT(compound);
		return compound.toString();
	}

	private void updateClientHookLoot() {
		String serializedHookLoot = this.dataManager.get(HOOK_LOOT_DATA);
		if (serializedHookLoot == null) {
			serializedHookLoot = "";
		}

		if (serializedHookLoot.equals(this.clientHookLootData)) {
			return;
		}

		this.clientHookLootData = serializedHookLoot;
		this.clientHookLootStacks.clear();
		this.clientHookSquidSlots.clear();

		if (serializedHookLoot.isEmpty()) {
			return;
		}

		try {
			NBTTagCompound compound = JsonToNBT.getTagFromJson(serializedHookLoot);
			NBTTagList hookLootList = compound.getTagList("HookLoot", 10);
			for (int i = 0; i < hookLootList.tagCount(); i++) {
				NBTTagCompound hookLootCompound = hookLootList.getCompoundTagAt(i);
				int slot = hookLootCompound.getInteger("Slot");
				ItemStack stack = new ItemStack(hookLootCompound.getCompoundTag("Stack"));
				while (this.clientHookLootStacks.size() <= slot) {
					this.clientHookLootStacks.add(ItemStack.EMPTY);
					this.clientHookSquidSlots.add(false);
				}
				this.clientHookSquidSlots.set(slot, hookLootCompound.getBoolean("Squid"));
				this.clientHookLootStacks.set(slot, stack);
			}
		} catch (NBTException e) {
			this.clientHookLootStacks.clear();
			this.clientHookSquidSlots.clear();
		}
	}

	private ItemStack createInkSacStack() {
		return new ItemStack(Items.DYE, 2 + this.rand.nextInt(3), 0);
	}

	public boolean isHookedSegment(int segment) {
		return segment % 2 == 0 && segment / 2 <= getHookCount();
	}

	public float getWavePitch(float partialTicks) {
		float time = this.ticksExisted + partialTicks;
		float phase = (anchorPos.getX() * 7 + anchorPos.getZ() * 13) & 31;
		return (float) Math.sin((time + phase) * 0.08F) * 4.0F;
	}

	public float getWaveRoll(float partialTicks) {
		float time = this.ticksExisted + partialTicks;
		float phase = (anchorPos.getX() * 7 + anchorPos.getZ() * 13) & 31;
		return (float) Math.cos((time + phase) * 0.06F) * 3.0F;
	}

	public float getBobOffset(float partialTicks) {
		float time = this.ticksExisted + partialTicks;
		float phase = (anchorPos.getX() * 7 + anchorPos.getZ() * 13) & 31;
		return (float) Math.sin((time + phase) * 0.04F) * 0.03F;
	}

	private void updateAnchorPosition() {
		AxisAlignedBB previousBoundingBox = this.getEntityBoundingBox();
		List<Entity> standingEntities = getStandingEntities(previousBoundingBox);
		double previousY = this.posY;

		this.setPosition(anchorPos.getX() + 0.5D, anchorPos.getY() - 0.35D - sinkOffset, anchorPos.getZ() + 0.5D);
		updateCollisionBox();

		double deltaY = this.posY - previousY;
		if (deltaY != 0.0D) {
			moveStandingEntities(standingEntities, deltaY);
		}
	}

	private void updateSinkOffset() {
		double targetSinkOffset;

		if (this.world.isRemote) {
			targetSinkOffset = this.dataManager.get(TARGET_SINK_OFFSET);
		} else {
			targetSinkOffset = hasStandingLoad() ? WEIGHT_SINK_OFFSET : 0.0D;
			this.dataManager.set(TARGET_SINK_OFFSET, (float) targetSinkOffset);
		}

		this.sinkOffset += (targetSinkOffset - this.sinkOffset) * WEIGHT_SINK_SPEED;

		if (Math.abs(targetSinkOffset - this.sinkOffset) < 0.01D) {
			this.sinkOffset = targetSinkOffset;
		}
	}

	private boolean hasStandingLoad() {
		return !getStandingEntities(this.getEntityBoundingBox()).isEmpty();
	}

	private List<Entity> getStandingEntities(AxisAlignedBB boundingBox) {
		List<Entity> standingEntities = new ArrayList<>();
		AxisAlignedBB loadBox = new AxisAlignedBB(
				boundingBox.minX + 0.03125D,
				boundingBox.maxY - 0.0625D,
				boundingBox.minZ + 0.03125D,
				boundingBox.maxX - 0.03125D,
				boundingBox.maxY + 0.25D,
				boundingBox.maxZ - 0.03125D);

		for (Entity entity : this.world.getEntitiesWithinAABBExcludingEntity(this, loadBox)) {
			if (isStandingEntity(entity, boundingBox, loadBox)) {
				standingEntities.add(entity);
			}
		}

		return standingEntities;
	}

	private boolean isStandingEntity(Entity entity, AxisAlignedBB boundingBox, AxisAlignedBB loadBox) {
		if (!(entity instanceof EntityLivingBase) || entity.isDead || entity.noClip) {
			return false;
		}

		AxisAlignedBB entityBoundingBox = entity.getEntityBoundingBox();
		return entity.posX > loadBox.minX
				&& entity.posX < loadBox.maxX
				&& entity.posZ > loadBox.minZ
				&& entity.posZ < loadBox.maxZ
				&& entityBoundingBox.minY >= boundingBox.maxY - 0.05D
				&& entityBoundingBox.minY <= boundingBox.maxY + 0.25D;
	}

	private void moveStandingEntities(List<Entity> standingEntities, double deltaY) {
		for (Entity entity : standingEntities) {
			entity.setPosition(entity.posX, entity.posY + deltaY, entity.posZ);
			entity.motionY = 0.0D;
			entity.fallDistance = 0.0F;
			entity.onGround = true;
		}
	}

	private void updateCollisionBox() {
		double halfWidth = this.width / 2.0D;
		double minY = this.posY + COLLISION_Y_OFFSET;
		this.setEntityBoundingBox(new AxisAlignedBB(
				this.posX - halfWidth,
				minY,
				this.posZ - halfWidth,
				this.posX + halfWidth,
				minY + this.height,
				this.posZ + halfWidth));
	}
}
