package com.windanesz.tracesofthefallen.client.render.entity;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.client.model.ModelGnossic;
import com.windanesz.tracesofthefallen.entity.EntityGnossic;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderGnossic extends RenderLiving<EntityGnossic> {

    private static final ResourceLocation GNOSSIC_TEXTURE = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/gnossic.png");

    public RenderGnossic(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelGnossic(), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityGnossic entity) {
        return GNOSSIC_TEXTURE;
    }

    @Override
    protected float getDeathMaxRotation(EntityGnossic entityBug) {
        return 0.0F; // Disable vanilla death rotation
    }

    @Override
    protected void preRenderCallback(EntityGnossic entity, float partialTickTime) {
        if (entity.deathTime > 0) {
            float f = ((float)entity.deathTime + partialTickTime - 1.0F) / 40.0F;
            if (f > 1.0F) f = 1.0F;
            
            // In preRenderCallback, the Y-axis is inverted (scaled by -1). +Y is DOWN.
            // 1. Drop the mask straight down to the floor (+1.4 Y)
            GlStateManager.translate(0.0F, 1.4F * f, 0.0F);
            
            // 2. Pivot around the mask's actual center. Since +Y is DOWN, the mask is at -1.5 Y.
            GlStateManager.translate(0.0F, -1.5F, 0.0F);
            GlStateManager.rotate(f * -90.0F, 1.0F, 0.0F, 0.0F); // Pitch backward to lie face-up
            
            // 3. Shrink the mask in the last second of death (ticks 60-80)
            if (entity.deathTime > 60) {
                float shrinkTime = ((float)entity.deathTime + partialTickTime - 60.0F) / 20.0F;
                if (shrinkTime > 1.0F) shrinkTime = 1.0F;
                float scale = 1.0F - shrinkTime; // 1.0 down to 0.0
                GlStateManager.scale(scale, scale, scale);
            }
            
            GlStateManager.translate(0.0F, 1.5F, 0.0F);
        }
    }

    @Override
    protected boolean canRenderName(EntityGnossic entity) {
        return super.canRenderName(entity) && (entity.hasCustomName() || entity.getAlwaysRenderNameTagForRender());
    }
}
