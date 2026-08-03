package com.windanesz.tracesofthefallen.item;

import com.windanesz.tracesofthefallen.entity.EntityZapLightning;
import com.windanesz.tracesofthefallen.init.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemZapCharge extends Item {
    public ItemZapCharge() {
        // Default stack size is 64
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        
        if (playerIn.isPotionActive(com.windanesz.tracesofthefallen.init.ModPotions.static_vulnerability)) {
            return new ActionResult<>(EnumActionResult.FAIL, itemstack);
        }
        
        if (!worldIn.isRemote) {
            Vec3d start = playerIn.getPositionEyes(1.0F);
            Vec3d look = playerIn.getLook(1.0F);
            Vec3d end = start.add(look.x * 30.0D, look.y * 30.0D, look.z * 30.0D);
            RayTraceResult raytraceresult = worldIn.rayTraceBlocks(start, end, false, true, false);
            
            Vec3d hitVec = end;
            if (raytraceresult != null) {
                hitVec = raytraceresult.hitVec;
            }
            
            double maxDot = -1.0;
            net.minecraft.util.math.BlockPos targetCoil = null;

            for (net.minecraft.tileentity.TileEntity te : worldIn.loadedTileEntityList) {
                if (te instanceof com.windanesz.tracesofthefallen.block.TileEntityDanCoil) {
                    net.minecraft.util.math.BlockPos pos = te.getPos();
                    Vec3d coilVec = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                    double dist = start.distanceTo(coilVec);
                    if (dist <= 20.0D) {
                        RayTraceResult sight = worldIn.rayTraceBlocks(start, coilVec, false, true, false);
                        if (sight == null || sight.getBlockPos().equals(pos) || sight.typeOfHit == RayTraceResult.Type.MISS) {
                            Vec3d dirToCoil = coilVec.subtract(start).normalize();
                            double dot = look.dotProduct(dirToCoil);
                            if (dot > maxDot && dot > 0) {
                                maxDot = dot;
                                targetCoil = pos;
                            }
                        }
                    }
                }
            }
            
            if (targetCoil != null) {
                hitVec = new Vec3d(targetCoil.getX() + 0.5, targetCoil.getY() + 0.5, targetCoil.getZ() + 0.5);
            }
            
            EntityZapLightning lightning = new EntityZapLightning(worldIn, start.x, start.y, start.z, hitVec.x, hitVec.y, hitVec.z, playerIn);
            lightning.singleDamageInstance = true;
            worldIn.spawnEntity(lightning);
            
            playerIn.addPotionEffect(new net.minecraft.potion.PotionEffect(com.windanesz.tracesofthefallen.init.ModPotions.static_vulnerability, 60, 0));
        }
        
        playerIn.getCooldownTracker().setCooldown(this, 60);
        
        if (!playerIn.capabilities.isCreativeMode) {
            itemstack.shrink(1);
            if (itemstack.isEmpty()) {
                return new ActionResult<>(EnumActionResult.SUCCESS, new ItemStack(ModItems.zap_charge_empty));
            } else {
                if (!playerIn.inventory.addItemStackToInventory(new ItemStack(ModItems.zap_charge_empty))) {
                    playerIn.dropItem(new ItemStack(ModItems.zap_charge_empty), false);
                }
                return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
    }

    @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @javax.annotation.Nullable World worldIn, java.util.List<String> tooltip, net.minecraft.client.util.ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        com.windanesz.tracesofthefallen.client.ClientProxy.addMultiLineDescription(tooltip, net.minecraft.util.text.TextFormatting.BOLD + "" + net.minecraft.util.text.TextFormatting.GRAY + net.minecraft.client.resources.I18n.format("item.totf:zap_charge.desc"));
        com.windanesz.tracesofthefallen.client.ClientProxy.addMultiLineDescription(tooltip, net.minecraft.util.text.TextFormatting.DARK_GRAY + net.minecraft.client.resources.I18n.format("item.totf:zap_charge.desc2"));
    }
}
