package com.windanesz.tracesofthefallen.item;

import com.windanesz.tracesofthefallen.client.model.ModelFloater;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemFloater extends ItemArmor {

	private static final ArmorMaterial FLOATER_MATERIAL = EnumHelper.addArmorMaterial(
			"FLOATER",
			"totf:floater",
			5, // Same as leather
			new int[]{1, 2, 3, 1},
			15,
			SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
			0.0F
	);

	@SideOnly(Side.CLIENT)
	private ModelBiped model;

	private final Block placedBlock;
	private final String textureName;

	public ItemFloater(Block block, String textureName) {
		super(FLOATER_MATERIAL, 0, EntityEquipmentSlot.LEGS);
		this.placedBlock = block;
		this.textureName = textureName;
	}

	@Override
	public int getMaxDamage(ItemStack stack) {
		return 80; // Explicitly 80 to match leather chestplate exactly
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		return "totf:textures/blocks/" + textureName + ".png";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped original) {
		if (model == null) {
			model = new ModelFloater();
		}

		model.setModelAttributes(original);
		model.setLivingAnimations(entityLiving, 0.0F, 0.0F, 0.0F);

		model.setVisible(false);
		// Render on the body (waist is part of the body model usually)
		model.bipedBody.showModel = true;

		model.isSneak = entityLiving.isSneaking();
		model.isRiding = entityLiving.isRiding();
		model.isChild = entityLiving.isChild();

		((ModelFloater)model).setStack(itemStack);

		return model;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		IBlockState iblockstate = worldIn.getBlockState(pos);
		Block block = iblockstate.getBlock();

		if (!block.isReplaceable(worldIn, pos)) {
			pos = pos.offset(facing);
		}

		ItemStack itemstack = player.getHeldItem(hand);

		if (!itemstack.isEmpty() && player.canPlayerEdit(pos, facing, itemstack) && worldIn.mayPlace(this.placedBlock, pos, false, facing, (Entity) null)) {
			int i = this.getMetadata(itemstack.getMetadata());
			IBlockState iblockstate1 = this.placedBlock.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, i, player);

			if (worldIn.setBlockState(pos, iblockstate1, 11)) {
				iblockstate1 = worldIn.getBlockState(pos);

				if (iblockstate1.getBlock() == this.placedBlock) {
					// this.placedBlock.onBlockPlacedBy(worldIn, pos, iblockstate1, player, itemstack); // Removed because BlockFloater doesn't need to store NBT
					
					SoundType soundtype = iblockstate1.getBlock().getSoundType(iblockstate1, worldIn, pos, player);
					worldIn.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
					itemstack.shrink(1);
				}
				return EnumActionResult.SUCCESS;
			}
		}

		return EnumActionResult.FAIL;
	}
}
