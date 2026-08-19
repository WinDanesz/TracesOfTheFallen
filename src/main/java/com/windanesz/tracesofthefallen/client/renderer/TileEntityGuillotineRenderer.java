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

        BlockGuillotine.EnumAttachFace face = state.getValue(BlockGuillotine.FACE);
        EnumFacing facing = state.getValue(BlockGuillotine.FACING);

        // Calculate smooth drop and retract animation. Total animation lasts 30 seconds (600 ticks).
        float ticksLeft = te.getActiveTicks() - partialTicks;
        float ticksElapsed = 600 - ticksLeft;
        float dropSpeed = 1.0f; // blocks per tick
        float targetDistance = te.getExtensionLength();
        float currentDistance;
        
        if (ticksElapsed * dropSpeed < targetDistance) {
            // Extending phase
            currentDistance = ticksElapsed * dropSpeed;
        } else if (ticksLeft <= 60) {
            // Retracting phase (last 3 seconds / 60 ticks)
            currentDistance = targetDistance * (Math.max(0, ticksLeft) / 60.0f);
        } else {
            // Fully extended phase
            currentDistance = targetDistance;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);

        float rotY = 0;
        float rotX = 0;

        if (face == BlockGuillotine.EnumAttachFace.WALL) {
            rotX = 270;
            switch (facing) {
                case NORTH: rotY = 180; break;
                case SOUTH: rotY = 0; break;
                case WEST: rotY = 270; break;
                case EAST: rotY = 90; break;
            }
        } else if (face == BlockGuillotine.EnumAttachFace.CEILING) {
            rotX = 0;
            switch (facing) {
                case NORTH: rotY = 180; break;
                case SOUTH: rotY = 0; break;
                case WEST: rotY = 270; break;
                case EAST: rotY = 90; break;
            }
        } else if (face == BlockGuillotine.EnumAttachFace.FLOOR) {
            rotX = 180;
            switch (facing) {
                case NORTH: rotY = 0; break;
                case SOUTH: rotY = 180; break;
                case WEST: rotY = 90; break;
                case EAST: rotY = 270; break;
            }
        }

        if (rotY != 0) GlStateManager.rotate(rotY, 0, 1, 0);
        if (rotX != 0) GlStateManager.rotate(rotX, 1, 0, 0);
        
        // Translate back to origin after center-rotation
        GlStateManager.translate(-0.5, -0.5, -0.5);

        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.disableLighting();
        
        BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();

        // 1. Render the pole repeating downwards
        IBakedModel poleModel = blockRenderer.getBlockModelShapes().getModelManager().getModel(new ModelResourceLocation(TracesOfTheFallen.MODID + ":guillotine_pole", "normal"));
        
        int numPoles = (int) Math.ceil(currentDistance * 16.0 / 6.0);
        for (int i = 0; i <= numPoles; i++) {
            GlStateManager.pushMatrix();
            float offset = i * 0.375f;
            if (offset > currentDistance) {
                offset = currentDistance;
            }
            GlStateManager.translate(0, -offset, 0);
            blockRenderer.getBlockModelRenderer().renderModelBrightnessColor(poleModel, 1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }

        // 2. Render the blade at the end
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, -currentDistance, 0);
        IBakedModel bladeModel = blockRenderer.getBlockModelShapes().getModelManager().getModel(new ModelResourceLocation(TracesOfTheFallen.MODID + ":guillotine_blade", "normal"));
        blockRenderer.getBlockModelRenderer().renderModelBrightnessColor(bladeModel, 1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
