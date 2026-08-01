package com.windanesz.tracesofthefallen.client.gui;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.entity.EntityGnossic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = TracesOfTheFallen.MODID, value = Side.CLIENT)
public class GnossicVignetteOverlay {

    private static final ResourceLocation VIGNETTE_TEX_PATH = new ResourceLocation(TracesOfTheFallen.MODID, "textures/misc/gnossic_vignette.png");

    private static float currentAlpha = 0.0F;

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null) return;

        boolean isBeingDrained = false;

        // Check if any nearby Gnossic is draining this player
        for (Entity entity : mc.world.loadedEntityList) {
            if (entity instanceof EntityGnossic) {
                EntityGnossic gnossic = (EntityGnossic) entity;
                if (gnossic.getDrainTargetId() == player.getEntityId() && gnossic.getAttackState() > 0) {
                    isBeingDrained = true;
                    break;
                }
            }
        }

        if (isBeingDrained) {
            currentAlpha += 0.02F; // Gradual fade in
            if (currentAlpha > 1.0F) currentAlpha = 1.0F;
        } else {
            currentAlpha -= 0.04F; // Gradual fade out
            if (currentAlpha < 0.0F) currentAlpha = 0.0F;
        }

        if (currentAlpha > 0.0F) {
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.color(1.0F, 1.0F, 1.0F, currentAlpha);
            GlStateManager.disableAlpha();
            mc.getTextureManager().bindTexture(VIGNETTE_TEX_PATH);
            
            ScaledResolution res = event.getResolution();
            Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, res.getScaledWidth(), res.getScaledHeight(), res.getScaledWidth(), res.getScaledHeight());
            // Draw a second time to compound the alpha, significantly increasing the darkness and claustrophobia of the edges
            Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, res.getScaledWidth(), res.getScaledHeight(), res.getScaledWidth(), res.getScaledHeight());
            
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableAlpha();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
