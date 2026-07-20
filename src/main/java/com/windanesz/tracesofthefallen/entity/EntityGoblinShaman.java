package com.windanesz.tracesofthefallen.entity;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.entity.ai.GoblinAIRunBehindTarget;
import com.windanesz.tracesofthefallen.entity.ai.GoblinAIShamanCastSpell;
import com.windanesz.tracesofthefallen.entity.ai.GoblinAIShamanDance;
import com.windanesz.tracesofthefallen.entity.ai.GoblinAIShamanStandStill;
import com.windanesz.tracesofthefallen.entity.shaman.ShamanSpell;
import com.windanesz.tracesofthefallen.entity.shaman.ShamanSpells;
import com.windanesz.tracesofthefallen.entity.shaman.SpellSchool;
import com.windanesz.tracesofthefallen.init.ModItems;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityGoblinShaman extends EntityGoblin {
	private static final DataParameter<Integer> DANCE_TYPE = EntityDataManager.createKey(EntityGoblinShaman.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> SPELL_CASTING_TIMER = EntityDataManager.createKey(EntityGoblinShaman.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> SPELL_CAST_TYPE = EntityDataManager.createKey(EntityGoblinShaman.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> SHAMAN_VARIANT = EntityDataManager.createKey(EntityGoblinShaman.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> FEATHER_COUNT = EntityDataManager.createKey(EntityGoblinShaman.class, DataSerializers.VARINT);

	public EntityGoblinShaman(World worldIn) {
		super(worldIn);
		if (!worldIn.isRemote) {
			this.setShamanVariant(this.rand.nextInt(10));
			this.setFeatherCount(1 + this.rand.nextInt(3)); // 1, 2, or 3 feathers randomly
		}
	}

	@Override
	protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
		super.setEquipmentBasedOnDifficulty(difficulty);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(DANCE_TYPE, 0);
		this.dataManager.register(SPELL_CASTING_TIMER, 0);
		this.dataManager.register(SPELL_CAST_TYPE, 0);
		this.dataManager.register(SHAMAN_VARIANT, 0);
		this.dataManager.register(FEATHER_COUNT, 3);
	}

	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		EntityAIBase meleeTask = null;
		EntityAIBase runBehindTask = null;
		for (net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry entry : this.tasks.taskEntries) {
			if (entry.action instanceof EntityAIAttackMelee) {
				meleeTask = entry.action;
			}
			if (entry.action instanceof GoblinAIRunBehindTarget) {
				runBehindTask = entry.action;
			}
		}
		if (meleeTask != null) this.tasks.removeTask(meleeTask);
		if (runBehindTask != null) this.tasks.removeTask(runBehindTask);

		this.tasks.addTask(1, new GoblinAIShamanStandStill(this));
		this.tasks.addTask(2, new GoblinAIShamanCastSpell(this));
		this.tasks.addTask(3, new AIShamanStrafeAway(this));
		this.tasks.addTask(4, new GoblinAIShamanDance(this));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(Settings.goblinSettings.goblinShamanMaxHealth);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.18D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(Settings.goblinSettings.goblinShamanAttackDamage);
	}

	public int getDanceType() {
		return this.dataManager.get(DANCE_TYPE);
	}

	public void setDanceType(int type) {
		this.dataManager.set(DANCE_TYPE, type);
	}

	public int getSpellCastingTimer() {
		return this.dataManager.get(SPELL_CASTING_TIMER);
	}

	public void setSpellCastingTimer(int timer) {
		this.dataManager.set(SPELL_CASTING_TIMER, timer);
	}

	public int getSpellCastType() {
		return this.dataManager.get(SPELL_CAST_TYPE);
	}

	public void setSpellCastType(int type) {
		this.dataManager.set(SPELL_CAST_TYPE, type);
	}

	public int getShamanVariant() {
		return this.dataManager.get(SHAMAN_VARIANT);
	}

	public void setShamanVariant(int variant) {
		this.dataManager.set(SHAMAN_VARIANT, variant);
	}

	public int getFeatherCount() {
		return this.dataManager.get(FEATHER_COUNT);
	}

	public void setFeatherCount(int count) {
		this.dataManager.set(FEATHER_COUNT, count);
	}

	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
		livingdata = super.onInitialSpawn(difficulty, livingdata);
		if (!this.world.isRemote) {
			this.setShamanVariant(this.rand.nextInt(10));
			this.setFeatherCount(1 + this.rand.nextInt(3));

			StringBuilder schoolsStr = new StringBuilder();
			SpellSchool[] schools = FEATHER_SCHOOLS[Math.max(0, Math.min(9, this.getShamanVariant()))];
			for (int i = 0; i < this.getFeatherCount() && i < schools.length; i++) {
				if (i > 0) schoolsStr.append(", ");
				schoolsStr.append(schools[i].name());
			}

			StringBuilder spellsStr = new StringBuilder();
			for (ShamanSpell spell : ShamanSpells.getAllSpells()) {
				if (spell.canShamanCast(this)) {
					if (spellsStr.length() > 0) spellsStr.append(", ");
					spellsStr.append(spell.getSpellName());
				}
			}
			if (spellsStr.length() == 0) {
				spellsStr.append("None");
			}

			System.out.println("[DEBUG] Goblin Shaman Spawned (Variant: " + this.getShamanVariant() + ", Feathers: " + this.getFeatherCount() + ")");
			System.out.println("[DEBUG]   Feather Schools: [" + schoolsStr + "]");
			System.out.println("[DEBUG]   Known Spells: [" + spellsStr + "]");
		}
		return livingdata;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setInteger("ShamanVariant", this.getShamanVariant());
		compound.setInteger("FeatherCount", this.getFeatherCount());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		if (compound.hasKey("ShamanVariant")) {
			this.setShamanVariant(compound.getInteger("ShamanVariant"));
		}
		if (compound.hasKey("FeatherCount")) {
			this.setFeatherCount(compound.getInteger("FeatherCount"));
		}
	}

	public int getAnimismMastery() {
		return getMasteryForSchool(SpellSchool.ANIMISM);
	}

	public int getSoulFlameMastery() {
		return getMasteryForSchool(SpellSchool.SOUL_FLAME);
	}

	public int getPyromancyMastery() {
		return getMasteryForSchool(SpellSchool.PYROMANCY);
	}

	private static final SpellSchool[][] FEATHER_SCHOOLS = new SpellSchool[][] {
		{SpellSchool.ANIMISM, SpellSchool.ANIMISM, SpellSchool.ANIMISM}, // 0: AAA (Blue)
		{SpellSchool.ANIMISM, SpellSchool.ANIMISM, SpellSchool.SOUL_FLAME}, // 1: AAB
		{SpellSchool.ANIMISM, SpellSchool.ANIMISM, SpellSchool.PYROMANCY}, // 2: AAC
		{SpellSchool.ANIMISM, SpellSchool.SOUL_FLAME, SpellSchool.SOUL_FLAME}, // 3: ABB
		{SpellSchool.ANIMISM, SpellSchool.SOUL_FLAME, SpellSchool.PYROMANCY}, // 4: ABC
		{SpellSchool.ANIMISM, SpellSchool.PYROMANCY, SpellSchool.PYROMANCY}, // 5: ACC
		{SpellSchool.SOUL_FLAME, SpellSchool.SOUL_FLAME, SpellSchool.SOUL_FLAME}, // 6: BBB (Green)
		{SpellSchool.SOUL_FLAME, SpellSchool.SOUL_FLAME, SpellSchool.PYROMANCY}, // 7: BBC
		{SpellSchool.SOUL_FLAME, SpellSchool.PYROMANCY, SpellSchool.PYROMANCY}, // 8: BCC
		{SpellSchool.PYROMANCY, SpellSchool.PYROMANCY, SpellSchool.PYROMANCY}  // 9: CCC (Red)
	};

	public int getMasteryForSchool(SpellSchool school) {
		if (school == null) return 0;
		if (school == SpellSchool.SUPPORT) {
			return this.getFeatherCount();
		}
		int variant = Math.max(0, Math.min(9, this.getShamanVariant()));
		int count = this.getFeatherCount();
		if (count <= 0) return 0;
		SpellSchool[] schools = FEATHER_SCHOOLS[variant];
		int mastery = 0;
		if (count == 1) {
			if (schools[1] == school) mastery++;
		} else if (count == 2) {
			if (schools[0] == school) mastery++;
			if (schools[2] == school) mastery++;
		} else {
			if (schools[0] == school) mastery++;
			if (schools[1] == school) mastery++;
			if (schools[2] == school) mastery++;
			if (count > 3 && schools[0] == school) mastery += (count - 3);
		}
		return mastery;
	}

	public int danceCooldown = 0;
	public int globalSpellCooldown = 0;
	private final int[] slotCooldowns = new int[6];
	private ShamanSpell[] cachedSlots = null;
	private int lastSlotCacheFeatherCount = -1;
	private int lastSlotCacheVariant = -1;

	/**
	 * Returns the 6 spell slots for this Shaman.
	 * Slots 0..2 hold up to 3 base class spells corresponding to crest feathers.
	 * Slots 3..5 hold up to 3 support class spells (1 per feather).
	 */
	public ShamanSpell[] getSlotSpells() {
		int currentVariant = this.getShamanVariant();
		int currentFeatherCount = this.getFeatherCount();
		if (this.cachedSlots == null || this.lastSlotCacheVariant != currentVariant || this.lastSlotCacheFeatherCount != currentFeatherCount) {
			ShamanSpell[] slots = new ShamanSpell[6];
			int baseIdx = 0;
			int supportIdx = 3;
			for (ShamanSpell spell : ShamanSpells.getAllSpells()) {
				if (spell.getSchool() == SpellSchool.SUPPORT) {
					if (this.getMasteryForSchool(SpellSchool.SUPPORT) >= spell.getRequiredMastery() && supportIdx < 6) {
						slots[supportIdx++] = spell;
					}
				} else {
					if (this.getMasteryForSchool(spell.getSchool()) >= spell.getRequiredMastery() && baseIdx < 3) {
						slots[baseIdx++] = spell;
					}
				}
			}
			this.cachedSlots = slots;
			this.lastSlotCacheVariant = currentVariant;
			this.lastSlotCacheFeatherCount = currentFeatherCount;
		}
		return this.cachedSlots;
	}

	public ShamanSpell getSlotSpell(int slotIndex) {
		if (slotIndex < 0 || slotIndex >= 6) return null;
		return getSlotSpells()[slotIndex];
	}

	public int getSlotCooldown(int slotIndex) {
		if (slotIndex < 0 || slotIndex >= 6) return 0;
		return this.slotCooldowns[slotIndex];
	}

	public void setSlotCooldown(int slotIndex, int ticks) {
		if (slotIndex >= 0 && slotIndex < 6) {
			this.slotCooldowns[slotIndex] = Math.max(0, ticks);
		}
	}

	public int getCooldown(ShamanSpell spell) {
		if (spell == null) return 0;
		ShamanSpell[] slots = this.getSlotSpells();
		for (int i = 0; i < 6; i++) {
			if (slots[i] == spell) {
				return this.slotCooldowns[i];
			}
		}
		return 0;
	}

	public void setCooldown(ShamanSpell spell, int ticks) {
		if (spell == null) return;
		ShamanSpell[] slots = this.getSlotSpells();
		for (int i = 0; i < 6; i++) {
			if (slots[i] == spell) {
				this.slotCooldowns[i] = Math.max(0, ticks);
				return;
			}
		}
	}

	private int prevSpellCastingTimer = 0;
	private int prevSpellCastType = 0;

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (this.danceCooldown > 0) {
			this.danceCooldown--;
		}
		if (this.globalSpellCooldown > 0) {
			this.globalSpellCooldown--;
		}
		for (int i = 0; i < 6; i++) {
			if (this.slotCooldowns[i] > 0) {
				this.slotCooldowns[i]--;
			}
		}

		int castType = this.getSpellCastType();
		int castTimer = this.getSpellCastingTimer();

		if (!this.world.isRemote) {
			if (castType == ShamanSpells.BONE_RATTLE.getSpellId() && castTimer > 0) {
				if (this.getHeldItemMainhand().isEmpty() || this.getHeldItemMainhand().getItem() != ModItems.bone_rattle) {
					this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.bone_rattle));
				}
			} else if (!this.getHeldItemMainhand().isEmpty() && this.getHeldItemMainhand().getItem() == ModItems.bone_rattle) {
				this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
			}
		}
		if (castType > 0 && castTimer > 0) {
			ShamanSpell activeSpell = ShamanSpells.getSpellById(castType);
			if (activeSpell != null) {
				EntityLivingBase target = this.getAttackTarget();
				activeSpell.onCastingTick(this, target, castTimer);
				if (!this.world.isRemote) {
					if (castTimer == 1) {
						activeSpell.onCastCompleted(this, target);
					}
					this.setSpellCastingTimer(castTimer - 1);
					if (castTimer - 1 <= 0) {
						this.setSpellCastType(0);
						this.globalSpellCooldown = 40;
					}
				}
			} else if (!this.world.isRemote) {
				this.setSpellCastingTimer(0);
				this.setSpellCastType(0);
				this.globalSpellCooldown = 40;
			}
		} else if (this.world.isRemote && (castTimer == 1 || (castTimer == 0 && this.prevSpellCastingTimer > 1)) && this.prevSpellCastType > 0) {
			ShamanSpell completedSpell = ShamanSpells.getSpellById(this.prevSpellCastType);
			if (completedSpell != null && castTimer != 1) {
				completedSpell.onCastingTick(this, null, 1);
			}
		}
		if (this.world.isRemote) {
			this.prevSpellCastingTimer = castTimer;
			this.prevSpellCastType = castType > 0 ? castType : this.prevSpellCastType;
			if (castTimer == 0) {
				this.prevSpellCastType = 0;
			}
		}
	}

	public boolean isDancing() {
		return this.getDanceType() > 0;
	}

	public void setDancing(boolean dancing) {
		this.setDanceType(dancing ? 1 : 0);
	}

	@Override
	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
		super.dropFewItems(wasRecentlyHit, lootingModifier);
		if (wasRecentlyHit && this.rand.nextInt(Math.max(1, 10 - lootingModifier * 2)) == 0) {
			this.dropItem(ModItems.bone_rattle, 1);
		}
	}

	@Override
	public boolean attackEntityAsMob(net.minecraft.entity.Entity entityIn) {
		if (this.getSpellCastingTimer() > 0) {
			return false;
		}
		EntityLivingBase target = this.getAttackTarget();
		if (target == null && entityIn instanceof EntityLivingBase) {
			target = (EntityLivingBase) entityIn;
		}
		if (target != null && target.isEntityAlive()) {
			for (int i = 0; i < 6; i++) {
				ShamanSpell spell = this.getSlotSpell(i);
				if (spell != null && this.getSlotCooldown(i) <= 0 && spell.canStartCastingIgnoringRng(this, target)) {
					return false;
				}
			}
		}
		return super.attackEntityAsMob(entityIn);
	}

	public static class AIShamanStrafeAway extends EntityAIBase {
		private final EntityGoblinShaman shaman;
		private EntityLivingBase target;

		public AIShamanStrafeAway(EntityGoblinShaman shaman) {
			this.shaman = shaman;
			this.setMutexBits(3);
		}

		@Override
		public boolean shouldExecute() {
			this.target = this.shaman.getAttackTarget();
			if (this.target == null || !this.target.isEntityAlive()) {
				return false;
			}
			return this.shaman.getDistanceSq(this.target) < 81.0D;
		}

		@Override
		public boolean shouldContinueExecuting() {
			return this.shouldExecute();
		}

		@Override
		public void updateTask() {
			this.shaman.getNavigator().clearPath();
			this.shaman.getLookHelper().setLookPositionWithEntity(this.target, 30.0F, 30.0F);
			this.shaman.rotationYaw = this.shaman.rotationYawHead;

			double dx = this.shaman.posX - this.target.posX;
			double dz = this.shaman.posZ - this.target.posZ;
			double len = MathHelper.sqrt(dx * dx + dz * dz);
			if (len < 0.0001D) {
				dx = 1.0D;
				dz = 0.0D;
				len = 1.0D;
			}
			
			if (this.shaman.onGround) {
				this.shaman.motionX += (dx / len) * 0.045D;
				this.shaman.motionZ += (dz / len) * 0.045D;
			}
		}
	}
}
