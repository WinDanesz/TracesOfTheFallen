package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.client.model.ModelLost;
import com.windanesz.tracesofthefallen.entity.EntityLost;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderLost extends RenderLiving<EntityLost> {

    private static final ResourceLocation LOST_TEXTURES = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/lost.png");

    public RenderLost(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelLost(), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityLost entity) {
        return LOST_TEXTURES;
    }

    @Override
    protected void applyRotations(EntityLost entityLiving, float p_77043_2_, float rotationYaw, float partialTicks) {
        super.applyRotations(entityLiving, p_77043_2_, rotationYaw, partialTicks);
        float layProgress = entityLiving.prevLayDownProgress + (entityLiving.layDownProgress - entityLiving.prevLayDownProgress) * partialTicks;
        
        if (entityLiving.deathTime > 0) {
            layProgress = ((float)entityLiving.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
            layProgress = net.minecraft.util.math.MathHelper.sqrt(layProgress);
            if (layProgress > 1.0F) layProgress = 1.0F;
        }

        if (layProgress > 0.0F) {
            net.minecraft.client.renderer.GlStateManager.translate(0.0F, layProgress * 0.1425F, layProgress * -0.95F); 
            net.minecraft.client.renderer.GlStateManager.rotate(layProgress * 90.0F, 1.0F, 0.0F, 0.0F);
        }
    }

    @Override
    protected float getDeathMaxRotation(EntityLost entityLivingBaseIn) {
        return 0.0F;
    }
}
