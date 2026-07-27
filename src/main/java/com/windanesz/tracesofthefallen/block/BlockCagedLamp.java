package com.windanesz.tracesofthefallen.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

public class BlockCagedLamp extends BlockLampBase {
    public BlockCagedLamp() {
        super(Material.IRON);
        this.setHardness(3.5F);
        this.setSoundType(SoundType.METAL);
    }

    @Override
    public int getLightValue(IBlockState state) {
        return 12;
    }
}

