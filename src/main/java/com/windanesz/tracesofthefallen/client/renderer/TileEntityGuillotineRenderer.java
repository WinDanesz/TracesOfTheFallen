package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.block.BlockGuillotine;
import com.windanesz.tracesofthefallen.block.TileEntityGuillotine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;

public class TileEntityGuillotineRenderer extends TileEntitySpecialRenderer<TileEntityGuillotine> {

    @Override
    public void render(TileEntityGuillotine te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (!te.isActive() || te.getExtensionLength() == 0) return;

        IBlockState state = te.getWorld().getBlockState(te.getPos());
        if (!(state.getBlock() instanceof BlockGuillotine)) return;

        EnumFacing facing = state.getValue(BlockGuillotine.FACING);
        int length = te.getExtensionLength();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);

        // Apply rotation based on facing to match the blockstate rotations
        switch (facing) {
            case UP:
                GlStateManager.rotate(180, 1, 0, 0);
                break;
            case NORTH:
                GlStateManager.rotate(90, 1, 0, 0);
                break;
            case SOUTH:
                GlStateManager.rotate(90, 1, 0, 0);
                GlStateManager.rotate(180, 0, 1, 0);
                break;
            case WEST:
                GlStateManager.rotate(90, 1, 0, 0);
                GlStateManager.rotate(270, 0, 1, 0);
                break;
            case EAST:
                GlStateManager.rotate(90, 1, 0, 0);
                GlStateManager.rotate(90, 0, 1, 0);
                break;
            case DOWN:
            default:
                break; // No rotation needed for DOWN (unrotated model)
        }
        
        // Translate back to origin after center-rotation
        GlStateManager.translate(-0.5, -0.5, -0.5);

        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.disableLighting();
        
        BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();

        // 1. Render the pole repeating downwards
        IBakedModel poleModel = blockRenderer.getBlockModelShapes().getModelManager().getModel(new ModelResourceLocation(TracesOfTheFallen.MODID + ":guillotine_pole", "normal"));
        
        float distanceBlocks = length;
        int numPoles = (int) Math.ceil(distanceBlocks * 16.0 / 6.0);
        for (int i = 0; i <= numPoles; i++) {
            GlStateManager.pushMatrix();
            float offset = i * 0.375f;
            if (offset > distanceBlocks) {
                offset = distanceBlocks;
            }
            GlStateManager.translate(0, -offset, 0);
            blockRenderer.getBlockModelRenderer().renderModelBrightnessColor(poleModel, 1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }

        // 2. Render the blade at the end
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, -distanceBlocks, 0);
        IBakedModel bladeModel = blockRenderer.getBlockModelShapes().getModelManager().getModel(new ModelResourceLocation(TracesOfTheFallen.MODID + ":guillotine_blade", "normal"));
        blockRenderer.getBlockModelRenderer().renderModelBrightnessColor(bladeModel, 1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
