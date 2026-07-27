package com.windanesz.tracesofthefallen.client.gui;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.block.TileEntityBrassFabricator;
import com.windanesz.tracesofthefallen.inventory.ContainerBrassFabricator;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiBrassFabricator extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(TracesOfTheFallen.MODID, "textures/gui/gui_brass_fabricator.png");
    private final TileEntityBrassFabricator te;

    public GuiBrassFabricator(EntityPlayer player, TileEntityBrassFabricator te) {
        super(new ContainerBrassFabricator(player, te));
        this.te = te;
        this.xSize = 176;
        this.ySize = 197;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // Optional: draw string names here
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);

        if (te.burnTime > 0 && te.maxBurnTime > 0 && te.speedLevel >= 1 && te.speedLevel <= 6) {
            int progressWidth = (int) (152.0F * ((float) te.burnTime / (float) te.maxBurnTime));
            
            // Texture coordinates for the overlays
            int texX = 12;
            int texY = 199 + ((te.speedLevel - 1) * 5); // 199, 204, 209, etc.
            
            // Draw the overlay bar
            this.drawTexturedModalRect(x + 11, y + 109, texX, texY, progressWidth, 4);
        }

        if (te.craftProgress > 0 && te.speedLevel >= 1 && te.speedLevel <= 6) {
            int targetTicks = 180 - (te.speedLevel * 20);
            int arrowWidth = (int) (24.0F * ((float) te.craftProgress / (float) targetTicks));
            if (arrowWidth > 24) arrowWidth = 24;
            
            // Draw the progress arrow (texture at 176, 0) (screen at 89, 35)
            this.drawTexturedModalRect(x + 89, y + 35, 176, 0, arrowWidth, 17);
        }
    }
}
