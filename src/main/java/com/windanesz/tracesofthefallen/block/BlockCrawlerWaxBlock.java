package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.init.ModCreativeTab;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockCrawlerWaxBlock extends Block {
    public BlockCrawlerWaxBlock() {
        super(Material.WOOD);
        setHardness(2.0F);
        setResistance(5.0F);
        setSoundType(net.minecraft.block.SoundType.WOOD);
        setCreativeTab(ModCreativeTab.TOTF_TAB);
    }
}
