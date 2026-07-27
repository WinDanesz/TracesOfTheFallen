package com.windanesz.tracesofthefallen.network;

import com.windanesz.tracesofthefallen.block.TileEntityBrassFabricator;
import com.windanesz.tracesofthefallen.client.gui.GuiBrassFabricator;
import com.windanesz.tracesofthefallen.inventory.ContainerBrassFabricator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class ModGuiHandler implements IGuiHandler {
    
    public static final int BRASS_FABRICATOR = 1;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == BRASS_FABRICATOR) {
            net.minecraft.tileentity.TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
            if (te instanceof TileEntityBrassFabricator) {
                return new ContainerBrassFabricator(player, (TileEntityBrassFabricator) te);
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == BRASS_FABRICATOR) {
            net.minecraft.tileentity.TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
            if (te instanceof TileEntityBrassFabricator) {
                return new GuiBrassFabricator(player, (TileEntityBrassFabricator) te);
            }
        }
        return null;
    }
}
