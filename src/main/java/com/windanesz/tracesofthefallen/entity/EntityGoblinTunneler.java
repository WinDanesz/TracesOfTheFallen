package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.entity.ai.GoblinAITunnelerDig;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.player.EntityPlayer;
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

public class EntityGoblinTunneler extends EntityGoblin {
	private static final DataParameter<Integer> BLOCKS_DUG = EntityDataManager.createKey(EntityGoblinTunneler.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> TORCHES_IN_STASH = EntityDataManager.createKey(EntityGoblinTunneler.class, DataSerializers.VARINT);

	private final List<BlockPos> dugBlocks = new ArrayList<>();
	private final List<BlockPos> plannedBlocks = new ArrayList<>();

	public EntityGoblinTunneler(World worldIn) {
		super(worldIn);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(BLOCKS_DUG, 0);
		this.dataManager.register(TORCHES_IN_STASH, 2);
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(1, new GoblinAITunnelerDig(this));
		this.targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, false));
	}

	public int getBlocksDug() {
		return this.dataManager.get(BLOCKS_DUG);
	}

	public void setBlocksDug(int count) {
		this.dataManager.set(BLOCKS_DUG, count);
	}

	public int getTorchesInStash() {
		return this.dataManager.get(TORCHES_IN_STASH);
	}

	public void setTorchesInStash(int count) {
		this.dataManager.set(TORCHES_IN_STASH, count);
	}

	public List<BlockPos> getDugBlocks() {
		return this.dugBlocks;
	}

	public List<BlockPos> getPlannedBlocks() {
		return this.plannedBlocks;
	}

	public void addDugBlock(BlockPos pos) {
		if (!this.dugBlocks.contains(pos)) {
			this.dugBlocks.add(pos);
		}
		this.setBlocksDug(this.dugBlocks.size());
	}

	public void addPlannedBlock(BlockPos pos) {
		if (!this.plannedBlocks.contains(pos) && !this.dugBlocks.contains(pos)) {
			this.plannedBlocks.add(pos);
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setInteger("BlocksDug", this.getBlocksDug());
		compound.setInteger("TorchesInStash", this.getTorchesInStash());

		NBTTagList dugList = new NBTTagList();
		for (BlockPos pos : this.dugBlocks) {
			NBTTagCompound posTag = new NBTTagCompound();
			posTag.setInteger("X", pos.getX());
			posTag.setInteger("Y", pos.getY());
			posTag.setInteger("Z", pos.getZ());
			dugList.appendTag(posTag);
		}
		compound.setTag("DugBlocks", dugList);

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
		if (compound.hasKey("BlocksDug")) {
			this.setBlocksDug(compound.getInteger("BlocksDug"));
		}
		if (compound.hasKey("TorchesInStash")) {
			this.setTorchesInStash(compound.getInteger("TorchesInStash"));
		}

		this.dugBlocks.clear();
		if (compound.hasKey("DugBlocks", 9)) {
			NBTTagList dugList = compound.getTagList("DugBlocks", 10);
			for (int i = 0; i < dugList.tagCount(); i++) {
				NBTTagCompound posTag = dugList.getCompoundTagAt(i);
				this.dugBlocks.add(new BlockPos(posTag.getInteger("X"), posTag.getInteger("Y"), posTag.getInteger("Z")));
			}
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
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0D);
	}

	@Override
	protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
		super.setEquipmentBasedOnDifficulty(difficulty);
		this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.WOODEN_PICKAXE));
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (!this.world.isRemote && this.getBlocksDug() < 50 && this.getHeldItemMainhand().isEmpty()) {
			this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.WOODEN_PICKAXE));
		}
	}
}
