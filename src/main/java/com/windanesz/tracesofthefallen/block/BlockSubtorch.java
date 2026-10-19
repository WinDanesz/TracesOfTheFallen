package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.BlockTorch;
import net.minecraft.block.SoundType;

public class BlockSubtorch extends BlockTorch {

    public BlockSubtorch() {
        super();
        this.setHardness(0.0F);
        this.setLightLevel(0.9375F);
        this.setSoundType(SoundType.WOOD);
    }

    @Override
    @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
    public void addInformation(net.minecraft.item.ItemStack stack, @javax.annotation.Nullable net.minecraft.world.World worldIn, java.util.List<String> tooltip, net.minecraft.client.util.ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(net.minecraft.util.text.TextFormatting.GRAY + net.minecraft.client.resources.I18n.format("tile.totf:subtorch.desc"));
    }

    @Override
    @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
    public void randomDisplayTick(net.minecraft.block.state.IBlockState stateIn, net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, java.util.Random rand) {
        net.minecraft.util.EnumFacing enumfacing = stateIn.getValue(FACING);
        double d0 = (double)pos.getX() + 0.5D;
        double d1 = (double)pos.getY() + 0.7D;
        double d2 = (double)pos.getZ() + 0.5D;

        if (enumfacing.getAxis().isHorizontal()) {
            net.minecraft.util.EnumFacing enumfacing1 = enumfacing.getOpposite();
            com.windanesz.tracesofthefallen.TracesOfTheFallen.proxy.spawnSubtorchParticle(worldIn, d0 + 0.27D * (double)enumfacing1.getXOffset(), d1 + 0.22D, d2 + 0.27D * (double)enumfacing1.getZOffset(), 0.0D, 0.0D, 0.0D);
            worldIn.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL, d0 + 0.27D * (double)enumfacing1.getXOffset(), d1 + 0.22D, d2 + 0.27D * (double)enumfacing1.getZOffset(), 0.0D, 0.0D, 0.0D);
        } else {
            com.windanesz.tracesofthefallen.TracesOfTheFallen.proxy.spawnSubtorchParticle(worldIn, d0, d1, d2, 0.0D, 0.0D, 0.0D);
            worldIn.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }
    }
}
