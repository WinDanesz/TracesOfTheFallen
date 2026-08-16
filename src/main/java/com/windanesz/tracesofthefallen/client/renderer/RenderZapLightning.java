package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.entity.EntityZapLightning;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class RenderZapLightning extends Render<EntityZapLightning> {

    public RenderZapLightning(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityZapLightning entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (!entity.isVisible) return;
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        
        int i = entity.getBrightnessForRender();
        int j = i % 65536;
        int k = i / 65536;
        net.minecraft.client.renderer.OpenGlHelper.setLightmapTextureCoords(net.minecraft.client.renderer.OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        
        Vec3d start = new Vec3d(entity.startX, entity.startY, entity.startZ);
        Vec3d end = new Vec3d(entity.endX, entity.endY, entity.endZ);
        Vec3d dir = end.subtract(start);
        double dist = dir.length();
        if (dist < 0.1) return;
        dir = dir.normalize();
        
        Vec3d perp1 = dir.crossProduct(new Vec3d(0, 1, 0)).normalize();
        if (perp1.lengthSquared() < 0.1) {
            perp1 = dir.crossProduct(new Vec3d(1, 0, 0)).normalize();
        }
        Vec3d perp2 = dir.crossProduct(perp1).normalize();

        int segments = Math.max(1, Math.min(100, (int)(dist * 2)));

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);

        for (int layer = 0; layer < 4; ++layer) {
            Random rand2 = new Random(entity.boltVertex);
            
            for (int branch = 0; branch < 3; ++branch) {
                int startSeg = 7;
                int endSeg = 0;
                
                if (branch > 0) {
                    startSeg = 7 - branch;
                }
                
                if (branch > 0) {
                    endSeg = startSeg - 2;
                }
                
                Vec3d currentPos = start;
                
                bufferbuilder.begin(5, DefaultVertexFormats.POSITION_COLOR);
                
                for (int seg = 0; seg <= segments; ++seg) {
                    double progress = (double)seg / segments;
                    
                    Vec3d pos = dir.scale(progress * dist);
                    
                    double noiseScale = entity.isReducedVisuals ? 0.025 : 0.05;
                    if (seg > 0 && seg < segments) {
                        pos = pos.add(perp1.scale((rand2.nextDouble() - 0.5) * dist * noiseScale));
                        pos = pos.add(perp2.scale((rand2.nextDouble() - 0.5) * dist * noiseScale));
                    }
                    
                    double dX = pos.x;
                    double dY = pos.y;
                    double dZ = pos.z;
                    
                    double baseW = entity.isReducedVisuals ? 0.025D : 0.05D;
                    double dW = baseW + Math.max(0.0D, baseW - progress * baseW);

                    if (layer == 0) {
                        bufferbuilder.pos(dX - dW, dY, dZ - dW).color(0.6F, 0.8F, 1.0F, 0.3F).endVertex();
                        bufferbuilder.pos(dX + dW, dY, dZ + dW).color(0.6F, 0.8F, 1.0F, 0.3F).endVertex();
                    } else if (layer == 1) {
                        bufferbuilder.pos(dX - dW, dY, dZ + dW).color(0.3F, 0.6F, 1.0F, 0.3F).endVertex();
                        bufferbuilder.pos(dX + dW, dY, dZ - dW).color(0.3F, 0.6F, 1.0F, 0.3F).endVertex();
                    } else if (layer == 2) {
                        bufferbuilder.pos(dX, dY - dW, dZ).color(0.1F, 0.4F, 1.0F, 0.3F).endVertex();
                        bufferbuilder.pos(dX, dY + dW, dZ).color(0.1F, 0.4F, 1.0F, 0.3F).endVertex();
                    } else {
                        bufferbuilder.pos(dX - dW, dY - dW, dZ - dW).color(0.0F, 0.2F, 1.0F, 0.3F).endVertex();
                        bufferbuilder.pos(dX + dW, dY + dW, dZ + dW).color(0.0F, 0.2F, 1.0F, 0.3F).endVertex();
                    }
                }
                
                tessellator.draw();
            }
        }
        
        GlStateManager.popMatrix();
        
        net.minecraft.client.renderer.OpenGlHelper.setLightmapTextureCoords(net.minecraft.client.renderer.OpenGlHelper.lightmapTexUnit, (float)j, (float)k);

        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityZapLightning entity) {
        return null;
    }
}
