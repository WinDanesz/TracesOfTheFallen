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
}
