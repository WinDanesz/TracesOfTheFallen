package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.entity.ai.GoblinAISapperBuildPath;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class EntityGoblinSapper extends EntityGoblin {
	private static final DataParameter<Integer> BLOCKS_BUILT = EntityDataManager.createKey(EntityGoblinSapper.class, DataSerializers.VARINT);

	private final List<BlockPos> plannedBlocks = new ArrayList<>();

	public EntityGoblinSapper(World worldIn) {
		super(worldIn);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(BLOCKS_BUILT, 0);
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(1, new GoblinAISapperBuildPath(this));
	}

	public int getBlocksBuilt() {
		return this.dataManager.get(BLOCKS_BUILT);
	}

	public void setBlocksBuilt(int count) {
		this.dataManager.set(BLOCKS_BUILT, count);
	}

	public List<BlockPos> getPlannedBlocks() {
		return this.plannedBlocks;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setInteger("BlocksBuilt", this.getBlocksBuilt());

		NBTTagList plannedList = new NBTTagList();
		for (BlockPos pos : this.plannedBlocks) {
			NBTTagCompound posTag = new NBTTagCompound();
			posTag.setInteger("X", pos.getX());
			posTag.setInteger("Y", pos.getY());
			posTag.setInteger("Z", pos.getZ());
			plannedList.appendTag(posTag);
		}
		compound.setTag("PlannedBlocks", plannedList);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		if (compound.hasKey("BlocksBuilt")) {
			this.setBlocksBuilt(compound.getInteger("BlocksBuilt"));
		}

		this.plannedBlocks.clear();
		if (compound.hasKey("PlannedBlocks", 9)) {
			NBTTagList plannedList = compound.getTagList("PlannedBlocks", 10);
			for (int i = 0; i < plannedList.tagCount(); i++) {
				NBTTagCompound posTag = plannedList.getCompoundTagAt(i);
				this.plannedBlocks.add(new BlockPos(posTag.getInteger("X"), posTag.getInteger("Y"), posTag.getInteger("Z")));
			}
		}
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(Settings.goblinSettings.goblinSapperMaxHealth);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(Settings.goblinSettings.goblinSapperAttackDamage);
	}

	@Override
	protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
		super.setEquipmentBasedOnDifficulty(difficulty);
		this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_PICKAXE));
		this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, new ItemStack(Blocks.DIRT, 32));
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (!this.world.isRemote && this.getHeldItemMainhand().isEmpty() && this.ticksExisted < 20) {
			this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_PICKAXE));
		}
		if (!this.world.isRemote && this.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND).isEmpty() && this.getBlocksBuilt() == 0 && this.ticksExisted < 20) {
			this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, new ItemStack(Blocks.DIRT, 32));
		}
	}
}
