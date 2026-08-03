package com.windanesz.tracesofthefallen.client.gui;

import com.windanesz.tracesofthefallen.block.TileEntitySpinningWheel;
import com.windanesz.tracesofthefallen.inventory.ContainerSpinningWheel;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiSpinningWheel extends GuiContainer {

    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation("totf", "textures/gui/gui_spinning_wheel.png");
    private final TileEntitySpinningWheel tileEntity;
    private final EntityPlayer player;

    public GuiSpinningWheel(EntityPlayer player, TileEntitySpinningWheel tileEntity) {
        super(new ContainerSpinningWheel(player, tileEntity));
        this.tileEntity = tileEntity;
        this.player = player;
        this.xSize = 176;
        this.ySize = 133;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String s = this.tileEntity.getDisplayName().getUnformattedText();
        this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 6, 4210752);
        this.fontRenderer.drawString(this.player.inventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GUI_TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
    }
}
