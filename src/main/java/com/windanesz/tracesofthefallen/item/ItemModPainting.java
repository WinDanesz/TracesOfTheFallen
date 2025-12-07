package com.windanesz.tracesofthefallen.item;

import com.windanesz.tracesofthefallen.entity.EntityModPainting;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemModPainting extends Item {

	public enum EnumPainting {
		PAINTING_IN_THE_WOODS("painting_in_the_woods", 32, 32, 0, 0, true),
		PAINTING_PORTRAIT("painting_portrait", 32, 48, 32, 32, true),
		PAINTING_THE_BLOODCURLING("painting_the_bloodcurling", 16, 32, 32, 0, true),
		PAINTING_WHEEL("painting_wheel", 16, 32, 48, 0, true),
		PAINTING_WIZARDRY("painting_wizardry", 32, 48, 32, 80, true);

		public final String name;
		public final int sizeX;
		public final int sizeY;
		public final int u;
		public final int v;
		public final boolean renderPlayer;

		private static final Map<String, EnumPainting> BY_NAME = Stream.of(values()).collect(Collectors.toMap(e -> e.name, Function.identity()));


		EnumPainting(String name, int sizeX, int sizeY, int u, int v, boolean renderPlayer) {
			this.name = name;
			this.sizeX = sizeX;
			this.sizeY = sizeY;
			this.u = u;
			this.v = v;
			this.renderPlayer = renderPlayer;
		}

		@Nullable
		public static EnumPainting getByName(String name) {
			return BY_NAME.get(name);
		}
	}

	private final EnumPainting painting;

	public ItemModPainting(EnumPainting painting) {
		this.painting = painting;
	}

	/**
	 * Called when a Block is right-clicked with this Item
	 */
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack itemstack = player.getHeldItem(hand);
		BlockPos blockpos = pos.offset(facing);

		if (facing != EnumFacing.DOWN && facing != EnumFacing.UP && player.canPlayerEdit(blockpos, facing, itemstack)) {
			EntityModPainting painting = new EntityModPainting(worldIn, blockpos, facing);

			if (painting.onValidSurface()) {
				if (!worldIn.isRemote) {
					painting.playPlaceSound();
					painting.setProperties(facing.getHorizontalAngle(), this.painting.sizeX, this.painting.sizeY, this.painting.name);
					NBTTagCompound ownerTag = itemstack.getSubCompound("Owner");
					if (ownerTag != null && ownerTag.hasKey("UUID", 8)) {
						painting.setOwnerId(java.util.UUID.fromString(ownerTag.getString("UUID")));
					} else {
						painting.setOwnerId(player.getUniqueID());
						painting.setOwnerName(player.getName());
					}
					worldIn.spawnEntity(painting);
					itemstack.shrink(1);
				}

			}

			return EnumActionResult.SUCCESS;
		} else {
			return EnumActionResult.FAIL;
		}
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt != null && nbt.hasKey("Owner")) {
			NBTTagCompound ownerTag = nbt.getCompoundTag("Owner");
			if (ownerTag.hasKey("PlayerName", 8)) {
				String ownerName = ownerTag.getString("PlayerName");
				tooltip.add(ownerName);
			}
		}
	}
}
