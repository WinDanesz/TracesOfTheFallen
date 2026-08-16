package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.PorcelainLiquids;
import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class TileEntityPorcelainVessel extends TileEntity implements ITickable {

	// ==== NBT keys / constants ====
	public static final String STACK_DATA_KEY = "PorcelainData";

	private static final int MAX_FLUID_LEVEL = 3;
	private static final int HEAT_TIME_TICKS = 20 * 15;

	private static final String FLUID_LEVEL_KEY = "FluidLevel";
	private static final String BASE_FLUID_KEY = "BaseFluid";
	private static final String HEATED_KEY = "Heated";
	private static final String HEATING_TICKS_KEY = "HeatingTicks";
	private static final String BREW_READY_KEY = "BrewReady";
	private static final String BREW_SERVINGS_KEY = "BrewServings";
	private static final String BREW_TICKS_REMAINING_KEY = "BrewTicksRemaining";
	private static final String BREW_LIQUID_TYPE_KEY = "BrewLiquidType";
	private static final String CUP_CONTENT_KEY = "CupContent";

	// ==== Runtime state ====
	private int fluidLevel;
	private String baseFluidType = "";
	private int heatingTicks;
	private boolean heated;
	private boolean brewReady;
	private int brewServings;
	private int brewTicksRemaining;
	private String brewedLiquidType = "";
	private String cupContent = "";

	// ==== Tick / heating ====
	@Override
	public void update() {
		if (world == null || world.isRemote || !isPotBlock()) {
			return;
		}

		if (brewReady) {
			return;
		}

		if (fluidLevel < MAX_FLUID_LEVEL) {
			if (heatingTicks > 0 || heated) {
				heatingTicks = 0;
				heated = false;
				onContentsChanged();
			}
			return;
		}

		if (heated) {
			return;
		}

		if (isHeatedByActiveFurnace()) {
			heatingTicks = Math.min(HEAT_TIME_TICKS, heatingTicks + 1);
			if (heatingTicks >= HEAT_TIME_TICKS) {
				heated = true;
				onContentsChanged();
			} else if (heatingTicks % 20 == 0) {
				markDirty();
			}
		} else if (heatingTicks > 0) {
			heatingTicks = 0;
			onContentsChanged();
		}
	}

	// ==== Pot fluid / brew API ====
	public boolean canAcceptFluid(String fluidName) {
		String normalizedFluid = normalizeFluid(fluidName);
		return isPotBlock()
				&& !brewReady
				&& !normalizedFluid.isEmpty()
				&& fluidLevel < MAX_FLUID_LEVEL
				&& (baseFluidType.isEmpty() || baseFluidType.equals(normalizedFluid));
	}

	public boolean addFluid(String fluidName, int units) {
		if (!canAcceptFluid(fluidName)) {
			return false;
		}
		String normalizedFluid = normalizeFluid(fluidName);
		fluidLevel = Math.min(MAX_FLUID_LEVEL, fluidLevel + Math.max(1, units));
		baseFluidType = normalizedFluid;
		resetHeating();
		onContentsChanged();
		return true;
	}

	public boolean fillWithFluid(String fluidName) {
		if (!canAcceptFluid(fluidName)) {
			return false;
		}
		String normalizedFluid = normalizeFluid(fluidName);
		fluidLevel = MAX_FLUID_LEVEL;
		baseFluidType = normalizedFluid;
		resetHeating();
		onContentsChanged();
		return true;
	}

	public boolean canBrewLiquid(ItemStack ingredientStack) {
		PorcelainLiquids.LiquidDefinition definition = PorcelainLiquids.getForIngredient(ingredientStack);
		return isPotBlock()
				&& heated
				&& !brewReady
				&& fluidLevel >= MAX_FLUID_LEVEL
				&& !ingredientStack.isEmpty()
				&& definition != null
				&& hasRequiredBaseFluid(definition);
	}

	public boolean brewLiquid(ItemStack ingredientStack) {
		if (!canBrewLiquid(ingredientStack)) {
			return false;
		}

		PorcelainLiquids.LiquidDefinition definition = PorcelainLiquids.getForIngredient(ingredientStack);
		if (definition == null) {
			return false;
		}

		return beginBrew(definition.type, definition.durationTicks);
	}

	public boolean canPourBrew() {
		return isPotBlock() && brewReady && brewServings > 0;
	}

	public boolean pourBrewIntoCup() {
		if (!canPourBrew()) {
			return false;
		}

		brewServings--;
		if (brewServings <= 0) {
			clearPotContents();
		} else {
			onContentsChanged();
		}
		return true;
	}

	// ==== Cup ====
	public boolean hasCupContent() {
		return !cupContent.isEmpty();
	}

	public String getCupContent() {
		return cupContent;
	}

	public void setCupContent(String content) {
		cupContent = normalizeId(content);
		onContentsChanged();
	}

	// ==== Pot/cup state getters ====
	public int getFluidLevel() {
		return fluidLevel;
	}

	public String getBaseFluidType() {
		return baseFluidType;
	}

	public int getHeatingTicks() {
		return heatingTicks;
	}

	public boolean isHeated() {
		return heated;
	}

	public boolean isBrewReady() {
		return brewReady && brewServings > 0;
	}

	public int getBrewServings() {
		return brewServings;
	}

	public String getBrewedLiquidType() {
		return brewedLiquidType;
	}

	public boolean isHeatingInProgress() {
		return !heated && fluidLevel >= MAX_FLUID_LEVEL && heatingTicks > 0;
	}

	public int getRequiredHeatTime() {
		return HEAT_TIME_TICKS;
	}

	public boolean isSteaming() {
		return isPotBlock() && fluidLevel >= MAX_FLUID_LEVEL && (heated || isBrewReady());
	}

	public boolean isActivelyHeated() {
		return isPotBlock() && isHeatedByActiveFurnace();
	}

	public boolean hasStoredPotData() {
		return fluidLevel > 0
				|| heatingTicks > 0
				|| heated
				|| brewReady
				|| brewServings > 0
				|| brewTicksRemaining > 0
				|| !baseFluidType.isEmpty()
				|| !brewedLiquidType.isEmpty();
	}

	public boolean hasStoredCupData() {
		return hasCupContent();
	}

	// ==== Stack conversion (TE <-> ItemStack) ====
	public ItemStack createStack(Block block) {
		Item item = Item.getItemFromBlock(block);
		if (item == null) {
			return ItemStack.EMPTY;
		}

		ItemStack stack = new ItemStack(item);
		writeStackData(stack);
		return stack;
	}

	public void readStackData(ItemStack stack) {
		NBTTagCompound vesselData = getStackData(stack);
		if (vesselData == null) {
			clearAllData(false);
			syncVisualState();
			return;
		}

		readCommonData(vesselData);
		syncVisualState();
	}

	public void writeStackData(ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}

		if (!hasStoredDataForStack(stack)) {
			if (stack.hasTagCompound()) {
				stack.getTagCompound().removeTag(STACK_DATA_KEY);
			}
			return;
		}

		NBTTagCompound stackTag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
		NBTTagCompound vesselData = new NBTTagCompound();
		writeCommonData(vesselData);
		stackTag.setTag(STACK_DATA_KEY, vesselData);
		stack.setTagCompound(stackTag);
	}

	// ==== Static stack queries / actions ====
	public static boolean isPlainPorcelainCup(ItemStack stack) {
		return isStackForBlock(stack, ModBlocks.porcelain_cup) && !hasStoredData(stack);
	}

	public static boolean isPlainPorcelainPot(ItemStack stack) {
		return isStackForBlock(stack, ModBlocks.porcelain_pot) && !hasStoredData(stack);
	}

	public static boolean hasStoredCupContent(ItemStack stack) {
		return isStackForBlock(stack, ModBlocks.porcelain_cup) && !getStoredCupContent(stack).isEmpty();
	}

	public static String getStoredCupContentType(ItemStack stack) {
		return getStoredCupContent(stack);
	}

	public static boolean hasStoredData(ItemStack stack) {
		NBTTagCompound data = getStackData(stack);
		return data != null
				&& (data.getInteger(FLUID_LEVEL_KEY) > 0
				|| data.getInteger(HEATING_TICKS_KEY) > 0
				|| !normalizeFluid(data.getString(BASE_FLUID_KEY)).isEmpty()
				|| data.getBoolean(HEATED_KEY)
				|| data.getBoolean(BREW_READY_KEY)
				|| data.getInteger(BREW_SERVINGS_KEY) > 0
				|| data.getInteger(BREW_TICKS_REMAINING_KEY) > 0
				|| !getStoredCupContent(data).isEmpty());
	}

	public static int getStoredFluidLevel(ItemStack stack) {
		NBTTagCompound data = getStackData(stack);
		return data == null ? 0 : data.getInteger(FLUID_LEVEL_KEY);
	}

	public static String getStoredBaseFluidType(ItemStack stack) {
		NBTTagCompound data = getStackData(stack);
		return data == null ? "" : normalizeFluid(data.getString(BASE_FLUID_KEY));
	}

	public static boolean isStoredHeated(ItemStack stack) {
		NBTTagCompound data = getStackData(stack);
		return data != null && data.getBoolean(HEATED_KEY) && !data.getBoolean(BREW_READY_KEY);
	}

	public static boolean isStoredBrewReady(ItemStack stack) {
		NBTTagCompound data = getStackData(stack);
		return data != null && data.getBoolean(BREW_READY_KEY) && data.getInteger(BREW_SERVINGS_KEY) > 0;
	}

	public static int getStoredBrewServings(ItemStack stack) {
		NBTTagCompound data = getStackData(stack);
		return isStoredBrewReady(stack) && data != null ? data.getInteger(BREW_SERVINGS_KEY) : 0;
	}

	public static String getStoredBrewLiquidType(ItemStack stack) {
		return getStoredBrewLiquidType(getStackData(stack));
	}

	public static boolean canStoredPotPourLiquid(ItemStack stack) {
		return isStackForBlock(stack, ModBlocks.porcelain_pot)
				&& isStoredBrewReady(stack)
				&& getStoredBrewServings(stack) > 0
				&& !getStoredBrewLiquidType(stack).isEmpty();
	}

	public static boolean pourLiquidFromStoredPot(ItemStack stack) {
		NBTTagCompound data = getStackData(stack);
		if (!canStoredPotPourLiquid(stack) || data == null) {
			return false;
		}

		int servings = Math.max(0, data.getInteger(BREW_SERVINGS_KEY) - 1);
		if (servings <= 0) {
			clearStoredBrewData(stack, data);
			return true;
		}

		data.setInteger(BREW_SERVINGS_KEY, servings);
		return true;
	}

	public static ItemStack createCupStack(String content) {
		Item item = Item.getItemFromBlock(ModBlocks.porcelain_cup);
		if (item == null) {
			return ItemStack.EMPTY;
		}

		ItemStack stack = new ItemStack(item);
		NBTTagCompound stackTag = new NBTTagCompound();
		NBTTagCompound vesselData = new NBTTagCompound();
		vesselData.setString(CUP_CONTENT_KEY, normalizeId(content));
		stackTag.setTag(STACK_DATA_KEY, vesselData);
		stack.setTagCompound(stackTag);
		return stack;
	}

	// ==== NBT / networking ====
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		writeCommonData(compound);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		readCommonData(compound);
		syncVisualState();
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
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		readFromNBT(tag);
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}

	// ==== Internal helpers ====
	private void writeCommonData(NBTTagCompound tag) {
		tag.setInteger(FLUID_LEVEL_KEY, fluidLevel);
		tag.setString(BASE_FLUID_KEY, baseFluidType);
		tag.setInteger(HEATING_TICKS_KEY, heatingTicks);
		tag.setBoolean(HEATED_KEY, heated);
		tag.setBoolean(BREW_READY_KEY, brewReady);
		tag.setInteger(BREW_SERVINGS_KEY, brewServings);
		tag.setInteger(BREW_TICKS_REMAINING_KEY, brewTicksRemaining);
		tag.setString(BREW_LIQUID_TYPE_KEY, brewedLiquidType);
		tag.setString(CUP_CONTENT_KEY, cupContent);
	}

	private void readCommonData(NBTTagCompound tag) {
		fluidLevel = tag.getInteger(FLUID_LEVEL_KEY);
		baseFluidType = normalizeFluid(tag.getString(BASE_FLUID_KEY));
		heatingTicks = tag.getInteger(HEATING_TICKS_KEY);
		heated = tag.getBoolean(HEATED_KEY);
		brewReady = tag.getBoolean(BREW_READY_KEY);
		brewServings = tag.getInteger(BREW_SERVINGS_KEY);
		brewTicksRemaining = tag.getInteger(BREW_TICKS_REMAINING_KEY);
		brewedLiquidType = getStoredBrewLiquidType(tag);
		cupContent = getStoredCupContent(tag);
	}

	private static void clearStoredBrewData(ItemStack stack, NBTTagCompound data) {
		data.removeTag(FLUID_LEVEL_KEY);
		data.removeTag(BASE_FLUID_KEY);
		data.removeTag(HEATED_KEY);
		data.removeTag(BREW_READY_KEY);
		data.removeTag(BREW_SERVINGS_KEY);
		data.removeTag(BREW_TICKS_REMAINING_KEY);
		data.removeTag(HEATING_TICKS_KEY);
		data.removeTag(BREW_LIQUID_TYPE_KEY);
		if (data.getSize() == 0 && stack.hasTagCompound()) {
			stack.getTagCompound().removeTag(STACK_DATA_KEY);
			if (stack.getTagCompound().getSize() == 0) {
				stack.setTagCompound(null);
			}
		}
	}

	private static String getStoredCupContent(ItemStack stack) {
		return getStoredCupContent(getStackData(stack));
	}

	private static String getStoredCupContent(@Nullable NBTTagCompound data) {
		if (data == null) {
			return "";
		}
		return normalizeId(data.getString(CUP_CONTENT_KEY));
	}

	private static String getStoredBrewLiquidType(@Nullable NBTTagCompound data) {
		if (data == null) {
			return "";
		}
		return normalizeId(data.getString(BREW_LIQUID_TYPE_KEY));
	}

	private void resetHeating() {
		heatingTicks = 0;
		heated = false;
		brewReady = false;
		brewServings = 0;
		brewTicksRemaining = 0;
		brewedLiquidType = "";
	}

	private void clearPotContents() {
		fluidLevel = 0;
		baseFluidType = "";
		resetHeating();
		onContentsChanged();
	}

	private void clearAllData(boolean sync) {
		fluidLevel = 0;
		baseFluidType = "";
		heatingTicks = 0;
		heated = false;
		brewReady = false;
		brewServings = 0;
		brewTicksRemaining = 0;
		brewedLiquidType = "";
		cupContent = "";
		if (sync) {
			onContentsChanged();
		}
	}

	private boolean beginBrew(String liquidType, int durationTicks) {
		brewReady = true;
		brewServings = Math.max(1, Settings.miscSettings.porcelainPotTeaServings);
		brewTicksRemaining = Math.max(1, durationTicks);
		brewedLiquidType = normalizeId(liquidType);
		onContentsChanged();
		return true;
	}

	private boolean hasRequiredBaseFluid(PorcelainLiquids.LiquidDefinition definition) {
		String requiredFluid = normalizeFluid(definition.forgeFluidName);
		return requiredFluid.isEmpty() || requiredFluid.equals(baseFluidType);
	}

	private boolean isPotBlock() {
		return world != null && world.getBlockState(pos).getBlock() == ModBlocks.porcelain_pot;
	}

	private boolean hasStoredDataForStack(ItemStack stack) {
		return isStackForBlock(stack, ModBlocks.porcelain_pot) ? hasStoredPotData() : hasStoredCupData();
	}

	private static boolean isStackForBlock(ItemStack stack, Block block) {
		return !stack.isEmpty() && stack.getItem() == Item.getItemFromBlock(block);
	}

	@Nullable
	private static NBTTagCompound getStackData(ItemStack stack) {
		return stack.hasTagCompound() && stack.getTagCompound().hasKey(STACK_DATA_KEY, 10)
				? stack.getTagCompound().getCompoundTag(STACK_DATA_KEY)
				: null;
	}

	private boolean isHeatedByActiveFurnace() {
		if (world == null) {
			return false;
		}

		IBlockState belowState = world.getBlockState(pos.down());
		Block belowBlock = belowState.getBlock();
		if (belowBlock == Blocks.LIT_FURNACE) {
			TileEntity tileEntity = world.getTileEntity(pos.down());
			if (tileEntity instanceof TileEntityFurnace) {
				TileEntityFurnace furnace = (TileEntityFurnace) tileEntity;
				return !furnace.getStackInSlot(0).isEmpty();
			}
			return true;
		}

		ResourceLocation regName = belowBlock.getRegistryName();
		if (regName != null) {
			String fullReg = regName.toString().toLowerCase(Locale.ROOT);
			for (Map.Entry<String, String> entry : getHeatSourcesMap().entrySet()) {
				String matcher = entry.getKey();
				String reqState = entry.getValue();

				if (!matcher.isEmpty() && fullReg.contains(matcher)) {
					if ("*".equals(reqState) || reqState.isEmpty() || "any".equalsIgnoreCase(reqState)) {
						return true;
					}
					String[] validStates = reqState.split("[,|]");
					for (String stateName : validStates) {
						String trimmed = stateName.trim();
						if ("*".equals(trimmed) || "any".equalsIgnoreCase(trimmed)) {
							return true;
						}
						for (IProperty<?> prop : belowState.getPropertyKeys()) {
							if (prop.getName().equalsIgnoreCase(trimmed)) {
								if (prop instanceof PropertyBool) {
									return belowState.getValue((PropertyBool) prop);
								}
							}
						}
					}
					return false;
				}
			}
		}

		return false;
	}

	private static Map<String, String> heatSourcesCache = null;

	public static void clearHeatSourcesCache() {
		heatSourcesCache = null;
	}

	public static Map<String, String> getHeatSourcesMap() {
		if (heatSourcesCache == null) {
			heatSourcesCache = new LinkedHashMap<>();
			for (String entry : Settings.miscSettings.porcelainHeatSources) {
				if (entry != null && (entry.contains(",") || entry.contains("="))) {
					String[] parts = entry.split("[,=]", 2);
					if (parts.length == 2) {
						heatSourcesCache.put(parts[0].trim().toLowerCase(Locale.ROOT), parts[1].trim().toLowerCase(Locale.ROOT));
					}
				} else if (entry != null && !entry.trim().isEmpty()) {
					heatSourcesCache.put(entry.trim().toLowerCase(Locale.ROOT), "*");
				}
			}
		}
		return heatSourcesCache;
	}

	public static boolean isConfiguredHeatSource(ResourceLocation regName) {
		if (regName == null) {
			return false;
		}
		String fullReg = regName.toString().toLowerCase(Locale.ROOT);
		for (String matcher : getHeatSourcesMap().keySet()) {
			if (!matcher.isEmpty() && fullReg.contains(matcher)) {
				return true;
			}
		}
		return false;
	}

	private void onContentsChanged() {
		markDirty();
		syncVisualState();
		syncToClient();
	}

	private void syncVisualState() {
		if (world == null) {
			return;
		}
		BlockPorcelainPiece.syncFilledState(world, pos, this);
	}

	private void syncToClient() {
		if (world == null || world.isRemote) {
			return;
		}
		world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
	}

	private static String normalizeId(@Nullable String value) {
		return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
	}

	private static String normalizeFluid(@Nullable String fluidName) {
		return normalizeId(fluidName);
	}
}
