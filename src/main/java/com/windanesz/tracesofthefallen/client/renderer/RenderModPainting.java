package com.windanesz.tracesofthefallen.client.renderer;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.entity.EntityModPainting;
import com.windanesz.tracesofthefallen.item.ItemModPainting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

public class RenderModPainting extends Render<EntityModPainting> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(TracesOfTheFallen.MODID, "textures/blocks/paintings.png");

    public RenderModPainting(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public boolean shouldRender(EntityModPainting livingEntity, ICamera camera, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public void doRender(EntityModPainting painting, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(180.0F - painting.getRotation(), 0.0F, 1.0F, 0.0F);
        GlStateManager.enableRescaleNormal();
        this.bindEntityTexture(painting);
        //	GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        //	GlStateManager.translate(0, -2, 0);
        float f = 0.0625F;
        GlStateManager.scale(0.0625F, 0.0625F, 0.0625F);

        if (this.renderOutlines) {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(painting));
        }

        ItemModPainting.EnumPainting p = ItemModPainting.EnumPainting.getByName(painting.getPainting());
        if (p == null) {
            // shouldn't happen but better to NPE
            p = ItemModPainting.EnumPainting.PAINTING_IN_THE_WOODS;
        }
        int textureV = p.v;
        if (p == ItemModPainting.EnumPainting.PAINTING_IN_THE_WOODS) {
            int hauntingProgress = painting.getHauntingProgress();
            if (hauntingProgress >= 70) {
                textureV = 96;
            } else if (hauntingProgress >= 40) {
                textureV = 64;
            } else if (hauntingProgress >= 20) {
                textureV = 32;
            }
        } else if (p == ItemModPainting.EnumPainting.PAINTING_RESTING_MISCHIEF) {
            int hauntingProgress = painting.getHauntingProgress();
            if (hauntingProgress >= 40) {
                textureV = 192;
            } else if (hauntingProgress >= 20) {
                textureV = 160;
            }
        }

        this.renderPainting(painting, p.sizeX, p.sizeY, p.u, textureV);
        if (p.renderPlayer) {
            // Render player
            GlStateManager.pushMatrix();

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            if (p == ItemModPainting.EnumPainting.PAINTING_IN_THE_WOODS) {
                int hauntingProgress = painting.getHauntingProgress();
                float brightness = Math.max(0.0f, 1.0f - (hauntingProgress / 160.0f));
                GlStateManager.color(brightness, brightness, brightness);
            }

            GlStateManager.translate(0, 0, -0.01); //Slightly offset to prevent z-fighting
            GlStateManager.translate(0, -10, 0); // Move down by 16 pixels
            ResourceLocation skin = DefaultPlayerSkin.getDefaultSkinLegacy();
            //ResourceLocation skin = Minecraft.getMinecraft().player.getLocationSkin();
            bindTexture(skin);
            GameProfile profile = painting.getPlayerProfile();
            if (profile != null) {
                Minecraft minecraft = Minecraft.getMinecraft();
                Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().loadSkinFromCache(profile);

                if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                    skin = minecraft.getSkinManager().loadSkin(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
                } else {
                    UUID uuid = EntityPlayer.getUUID(profile);
                    skin = DefaultPlayerSkin.getDefaultSkin(uuid);
                }
            }

            this.bindTexture(skin);
            renderPlayerSkin(p);
            GlStateManager.popMatrix();
        }


        if (this.renderOutlines) {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }

        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        super.doRender(painting, x, y, z, entityYaw, partialTicks);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityModPainting entity) {
        return TEXTURE;
    }

    private void renderPainting(EntityModPainting painting, int width, int height, int textureU, int textureV) {
        float canvasSize = 256f;
        float f = (float) (-width) / 2.0F;
        float f1 = (float) (-height) / 2.0F;
        float f2 = 0.5F;
        float f3 = 0.75F;
        float f4 = 0.8125F;
        float f5 = 0.0F;
        float f6 = 0.0625F;
        float f7 = 0.75F;
        float f8 = 0.8125F;
        float f9 = 0.001953125F;
        float f10 = 0.001953125F;
        float f11 = 0.7519531F;
        float f12 = 0.7519531F;
        float f13 = 0.0F;
        float f14 = 0.0625F;

        for (int i = 0; i < width / 16; ++i) {
            for (int j = 0; j < height / 16; ++j) {
                float f15 = f + (float) ((i + 1) * 16);
                float f16 = f + (float) (i * 16);
                float f17 = f1 + (float) ((j + 1) * 16);
                float f18 = f1 + (float) (j * 16);
                //this.setLightmap(painting, (f15 + f16) / 2.0F, (f17 + f18) / 2.0F);
                float f19 = (float) (textureU + width - i * 16) / canvasSize;
                float f20 = (float) (textureU + width - (i + 1) * 16) / canvasSize;
                float f21 = (float) (textureV + height - j * 16) / canvasSize;
                float f22 = (float) (textureV + height - (j + 1) * 16) / canvasSize;
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuffer();
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
                bufferbuilder.pos((double) f15, (double) f18, -0.5D).tex((double) f20, (double) f21).normal(0.0F, 0.0F, -1.0F).endVertex();
                bufferbuilder.pos((double) f16, (double) f18, -0.5D).tex((double) f19, (double) f21).normal(0.0F, 0.0F, -1.0F).endVertex();
                bufferbuilder.pos((double) f16, (double) f17, -0.5D).tex((double) f19, (double) f22).normal(0.0F, 0.0F, -1.0F).endVertex();
                bufferbuilder.pos((double) f15, (double) f17, -0.5D).tex((double) f20, (double) f22).normal(0.0F, 0.0F, -1.0F).endVertex();
                bufferbuilder.pos((double) f15, (double) f17, 0.5D).tex(0.75D, 0.0D).normal(0.0F, 0.0F, 1.0F).endVertex();
                bufferbuilder.pos((double) f16, (double) f17, 0.5D).tex(0.8125D, 0.0D).normal(0.0F, 0.0F, 1.0F).endVertex();
                bufferbuilder.pos((double) f16, (double) f18, 0.5D).tex(0.8125D, 0.0625D).normal(0.0F, 0.0F, 1.0F).endVertex();
                bufferbuilder.pos((double) f15, (double) f18, 0.5D).tex(0.75D, 0.0625D).normal(0.0F, 0.0F, 1.0F).endVertex();
                bufferbuilder.pos((double) f15, (double) f17, -0.5D).tex(0.75D, 0.001953125D).normal(0.0F, 1.0F, 0.0F).endVertex();
                bufferbuilder.pos((double) f16, (double) f17, -0.5D).tex(0.8125D, 0.001953125D).normal(0.0F, 1.0F, 0.0F).endVertex();
                bufferbuilder.pos((double) f16, (double) f17, 0.5D).tex(0.8125D, 0.001953125D).normal(0.0F, 1.0F, 0.0F).endVertex();
                bufferbuilder.pos((double) f15, (double) f17, 0.5D).tex(0.75D, 0.001953125D).normal(0.0F, 1.0F, 0.0F).endVertex();
                bufferbuilder.pos((double) f15, (double) f18, 0.5D).tex(0.75D, 0.001953125D).normal(0.0F, -1.0F, 0.0F).endVertex();
                bufferbuilder.pos((double) f16, (double) f18, 0.5D).tex(0.8125D, 0.001953125D).normal(0.0F, -1.0F, 0.0F).endVertex();
                bufferbuilder.pos((double) f16, (double) f18, -0.5D).tex(0.8125D, 0.001953125D).normal(0.0F, -1.0F, 0.0F).endVertex();
                bufferbuilder.pos((double) f15, (double) f18, -0.5D).tex(0.75D, 0.001953125D).normal(0.0F, -1.0F, 0.0F).endVertex();
                bufferbuilder.pos((double) f15, (double) f17, 0.5D).tex(0.751953125D, 0.0D).normal(-1.0F, 0.0F, 0.0F).endVertex();
                bufferbuilder.pos((double) f15, (double) f18, 0.5D).tex(0.751953125D, 0.0625D).normal(-1.0F, 0.0F, 0.0F).endVertex();
                bufferbuilder.pos((double) f15, (double) f18, -0.5D).tex(0.751953125D, 0.0625D).normal(-1.0F, 0.0F, 0.0F).endVertex();
                bufferbuilder.pos((double) f15, (double) f17, -0.5D).tex(0.751953125D, 0.0D).normal(-1.0F, 0.0F, 0.0F).endVertex();
                bufferbuilder.pos((double) f16, (double) f17, -0.5D).tex(0.751953125D, 0.0D).normal(1.0F, 0.0F, 0.0F).endVertex();
                bufferbuilder.pos((double) f16, (double) f18, -0.5D).tex(0.751953125D, 0.0625D).normal(1.0F, 0.0F, 0.0F).endVertex();
                bufferbuilder.pos((double) f16, (double) f18, 0.5D).tex(0.751953125D, 0.0625D).normal(1.0F, 0.0F, 0.0F).endVertex();
                bufferbuilder.pos((double) f16, (double) f17, 0.5D).tex(0.751953125D, 0.0D).normal(1.0F, 0.0F, 0.0F).endVertex();
                tessellator.draw();
            }
        }
    }

    private void renderPlayerSkin(ItemModPainting.EnumPainting painting) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        if (painting == ItemModPainting.EnumPainting.PAINTING_IN_THE_WOODS) {
            float scale = 1.0f;
            float headSize = 8 * scale;
            float bodyWidth = 8 * scale;
            float bodyHeight = 10 * scale;
            float armWidth = 4 * scale;
            float overlayScale = 1.0f;

            float x0 = -bodyWidth / 2;
            float x1 = bodyWidth / 2;

            float bodyY0 = -bodyHeight / 2;
            float bodyY1 = bodyHeight / 2;

            float headY0 = bodyY1;
            float headY1 = bodyY1 + headSize;

            float skinWidth = 64f;
            float skinHeight = 64f;

            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
            // Body
            bufferbuilder.pos(x1, bodyY0, -0.5D).tex(28 / skinWidth, 30 / skinHeight).endVertex();
            bufferbuilder.pos(x0, bodyY0, -0.5D).tex(20 / skinWidth, 30 / skinHeight).endVertex();
            bufferbuilder.pos(x0, bodyY1, -0.5D).tex(20 / skinWidth, 20 / skinHeight).endVertex();
            bufferbuilder.pos(x1, bodyY1, -0.5D).tex(28 / skinWidth, 20 / skinHeight).endVertex();

            // Right Arm
            bufferbuilder.pos(x0, bodyY0, -0.5D).tex(48 / skinWidth, 30 / skinHeight).endVertex();
            bufferbuilder.pos(x0 - armWidth, bodyY0, -0.5D).tex(44 / skinWidth, 30 / skinHeight).endVertex();
            bufferbuilder.pos(x0 - armWidth, bodyY1, -0.5D).tex(44 / skinWidth, 20 / skinHeight).endVertex();
            bufferbuilder.pos(x0, bodyY1, -0.5D).tex(48 / skinWidth, 20 / skinHeight).endVertex();

            // Left Arm
            bufferbuilder.pos(x1 + armWidth, bodyY0, -0.5D).tex(40 / skinWidth, 62 / skinHeight).endVertex();
            bufferbuilder.pos(x1, bodyY0, -0.5D).tex(36 / skinWidth, 62 / skinHeight).endVertex();
            bufferbuilder.pos(x1, bodyY1, -0.5D).tex(36 / skinWidth, 52 / skinHeight).endVertex();
            bufferbuilder.pos(x1 + armWidth, bodyY1, -0.5D).tex(40 / skinWidth, 52 / skinHeight).endVertex();

            // Head
            bufferbuilder.pos(x1, headY0, -0.5D).tex(16 / skinWidth, 16 / skinHeight).endVertex();
            bufferbuilder.pos(x0, headY0, -0.5D).tex(8 / skinWidth, 16 / skinHeight).endVertex();
            bufferbuilder.pos(x0, headY1, -0.5D).tex(8 / skinWidth, 8 / skinHeight).endVertex();
            bufferbuilder.pos(x1, headY1, -0.5D).tex(16 / skinWidth, 8 / skinHeight).endVertex();

            // --- Overlay Layers ---

            // Body Overlay
            bufferbuilder.pos(x1 * overlayScale, bodyY0 * overlayScale, -0.51D).tex(28 / skinWidth, 46 / skinHeight).endVertex();
            bufferbuilder.pos(x0 * overlayScale, bodyY0 * overlayScale, -0.51D).tex(20 / skinWidth, 46 / skinHeight).endVertex();
            bufferbuilder.pos(x0 * overlayScale, bodyY1 * overlayScale, -0.51D).tex(20 / skinWidth, 36 / skinHeight).endVertex();
            bufferbuilder.pos(x1 * overlayScale, bodyY1 * overlayScale, -0.51D).tex(28 / skinWidth, 36 / skinHeight).endVertex();

            // Right Arm Overlay
            bufferbuilder.pos(x0 * overlayScale, bodyY0 * overlayScale, -0.51D).tex(48 / skinWidth, 46 / skinHeight).endVertex();
            bufferbuilder.pos((x0 - armWidth) * overlayScale, bodyY0 * overlayScale, -0.51D).tex(44 / skinWidth, 46 / skinHeight).endVertex();
            bufferbuilder.pos((x0 - armWidth) * overlayScale, bodyY1 * overlayScale, -0.51D).tex(44 / skinWidth, 36 / skinHeight).endVertex();
            bufferbuilder.pos(x0 * overlayScale, bodyY1 * overlayScale, -0.51D).tex(48 / skinWidth, 36 / skinHeight).endVertex();

            // Left Arm Overlay
            bufferbuilder.pos((x1 + armWidth) * overlayScale, bodyY0 * overlayScale, -0.51D).tex(56 / skinWidth, 62 / skinHeight).endVertex();
            bufferbuilder.pos(x1 * overlayScale, bodyY0 * overlayScale, -0.51D).tex(52 / skinWidth, 62 / skinHeight).endVertex();
            bufferbuilder.pos(x1 * overlayScale, bodyY1 * overlayScale, -0.51D).tex(52 / skinWidth, 52 / skinHeight).endVertex();
            bufferbuilder.pos((x1 + armWidth) * overlayScale, bodyY1 * overlayScale, -0.51D).tex(56 / skinWidth, 52 / skinHeight).endVertex();

            // Head Overlay
            bufferbuilder.pos(x1 * overlayScale, headY0 * overlayScale, -0.51D).tex(48 / skinWidth, 16 / skinHeight).endVertex();
            bufferbuilder.pos(x0 * overlayScale, headY0 * overlayScale, -0.51D).tex(40 / skinWidth, 16 / skinHeight).endVertex();
            bufferbuilder.pos(x0 * overlayScale, headY1 * overlayScale, -0.51D).tex(40 / skinWidth, 8 / skinHeight).endVertex();
            bufferbuilder.pos(x1 * overlayScale, headY1 * overlayScale, -0.51D).tex(48 / skinWidth, 8 / skinHeight).endVertex();

            tessellator.draw();
        } else if (painting == ItemModPainting.EnumPainting.PAINTING_PORTRAIT) {
            float scale = 1.0f;
            float headSize = 8 * scale;
            float bodyWidth = 4 * scale;
            float bodyHeight = 12 * scale;
            float armWidth = 4 * scale;
            float legWidth = 4 * scale;
            float legHeight = 12 * scale;
            float overlayScale = 1.0f;

            float x0 = -bodyWidth / 2;
            float x1 = bodyWidth / 2;

            float headX0 = -headSize / 2;
            float headX1 = headSize / 2;

            float legY0 = -(legHeight + bodyHeight) / 2;
            float legY1 = legY0 + legHeight;

            float bodyY0 = legY1;
            float bodyY1 = bodyY0 + bodyHeight;

            float headY0 = bodyY1;
            float headY1 = headY0 + headSize;

            float skinWidth = 64f;
            float skinHeight = 64f;

            float xOffset = -3.0f;
            float yOffset = 3.0f;

            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
            // Body
            bufferbuilder.pos(x1 + xOffset, 0 + yOffset, -0.5D).tex(20 / skinWidth, 32 / skinHeight).endVertex();
            bufferbuilder.pos(x0 + xOffset, 0 + yOffset, -0.5D).tex(16 / skinWidth, 32 / skinHeight).endVertex();
            bufferbuilder.pos(x0 + xOffset, 8 + yOffset, -0.5D).tex(16 / skinWidth, 24 / skinHeight).endVertex();
            bufferbuilder.pos(x1 + xOffset, 8 + yOffset, -0.5D).tex(20 / skinWidth, 24 / skinHeight).endVertex();

            // Arm
            float armLength = 12 * scale;
            float armX0 = x1 - 4 * scale;
            float armX1 = armX0 + armLength;
            float armY0 = bodyY1 - armWidth;
            float armY1 = bodyY1;
            bufferbuilder.pos(armX1 + xOffset, armY0 + yOffset, -0.5D).tex(44 / skinWidth, 32 / skinHeight).endVertex(); // Bottom-right
            bufferbuilder.pos(armX0 + xOffset, armY0 + yOffset, -0.5D).tex(44 / skinWidth, 20 / skinHeight).endVertex(); // Bottom-left
            bufferbuilder.pos(armX0 + xOffset, armY1 + yOffset, -0.5D).tex(40 / skinWidth, 20 / skinHeight).endVertex(); // Top-left
            bufferbuilder.pos(armX1 + xOffset, armY1 + yOffset, -0.5D).tex(40 / skinWidth, 32 / skinHeight).endVertex(); // Top-right


            // Leg
            bufferbuilder.pos(x1 + xOffset, legY0 + yOffset, -0.5D).tex(8 / skinWidth, 32 / skinHeight).endVertex(); // Bottom-right
            bufferbuilder.pos(x0 + xOffset, legY0 + yOffset, -0.5D).tex(4 / skinWidth, 32 / skinHeight).endVertex(); // Bottom-left
            bufferbuilder.pos(x0 + xOffset, legY1 + yOffset, -0.5D).tex(4 / skinWidth, 20 / skinHeight).endVertex(); // Top-left
            bufferbuilder.pos(x1 + xOffset, legY1 + yOffset, -0.5D).tex(8 / skinWidth, 20 / skinHeight).endVertex(); // Top-right

            // Head
            bufferbuilder.pos(headX1 + xOffset, headY0 + yOffset, -0.5D).tex(16 / skinWidth, 16 / skinHeight).endVertex();
            bufferbuilder.pos(headX0 + xOffset, headY0 + yOffset, -0.5D).tex(8 / skinWidth, 16 / skinHeight).endVertex();
            bufferbuilder.pos(headX0 + xOffset, headY1 + yOffset, -0.5D).tex(8 / skinWidth, 8 / skinHeight).endVertex();
            bufferbuilder.pos(headX1 + xOffset, headY1 + yOffset, -0.5D).tex(16 / skinWidth, 8 / skinHeight).endVertex();

            // --- Overlay Layers ---
            // Head Overlay
            bufferbuilder.pos(headX1 * overlayScale + xOffset, headY0 * overlayScale + yOffset, -0.51D).tex(48 / skinWidth, 16 / skinHeight).endVertex();
            bufferbuilder.pos(headX0 * overlayScale + xOffset, headY0 * overlayScale + yOffset, -0.51D).tex(40 / skinWidth, 16 / skinHeight).endVertex();
            bufferbuilder.pos(headX0 * overlayScale + xOffset, headY1 * overlayScale + yOffset, -0.51D).tex(40 / skinWidth, 8 / skinHeight).endVertex();
            bufferbuilder.pos(headX1 * overlayScale + xOffset, headY1 * overlayScale + yOffset, -0.51D).tex(48 / skinWidth, 8 / skinHeight).endVertex();

            tessellator.draw();

        } else if (painting == ItemModPainting.EnumPainting.PAINTING_WIZARDRY) {
            float scale = 1.0f;
            float headSize = 8 * scale;
            float bodyWidth = 8 * scale;
            float bodyHeight = 12 * scale;
            float armWidth = 4 * scale;
            float armLength = 12 * scale;
            float legWidth = 4 * scale;
            float legHeight = 12 * scale;
            float overlayScale = 1.00f; // Slightly larger to avoid z-fighting

            // Centers the character
            float xOffset = -4f;
            float yOffset = 9f;

            float bodyX0 = -bodyWidth / 2 + xOffset;
            float bodyX1 = bodyWidth / 2 + xOffset;
            float bodyY0 = -bodyHeight / 2 + yOffset;
            float bodyY1 = bodyHeight / 2 + yOffset;

            float headX0 = -headSize / 2 + xOffset;
            float headX1 = headSize / 2 + xOffset;
            float headY0 = bodyY1;
            float headY1 = headY0 + headSize;

            // Right Leg (viewer's left)
            float legX0 = -bodyWidth / 2 + xOffset;
            float legX1 = legX0 + legWidth;
            float legY0 = bodyY0 - legHeight;
            float legY1 = bodyY0;

            // Left Leg (viewer's right)
            float rLegX0 = 0 + xOffset;
            float rLegX1 = rLegX0 + legWidth;

            float skinWidth = 64f;
            float skinHeight = 64f;

            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);

            // --- Base Layer ---

            // Body
            bufferbuilder.pos(bodyX1, bodyY0, -0.5D).tex(28 / skinWidth, 32 / skinHeight).endVertex();
            bufferbuilder.pos(bodyX0, bodyY0, -0.5D).tex(20 / skinWidth, 32 / skinHeight).endVertex();
            bufferbuilder.pos(bodyX0, bodyY1, -0.5D).tex(20 / skinWidth, 20 / skinHeight).endVertex();
            bufferbuilder.pos(bodyX1, bodyY1, -0.5D).tex(28 / skinWidth, 20 / skinHeight).endVertex();

            // Right Leg (viewer's left) - MIRRORED
            bufferbuilder.pos(legX1, legY0, -0.5D).tex(4 / skinWidth, 32 / skinHeight).endVertex();
            bufferbuilder.pos(legX0, legY0, -0.5D).tex(8 / skinWidth, 32 / skinHeight).endVertex();
            bufferbuilder.pos(legX0, legY1, -0.5D).tex(8 / skinWidth, 20 / skinHeight).endVertex();
            bufferbuilder.pos(legX1, legY1, -0.5D).tex(4 / skinWidth, 20 / skinHeight).endVertex();

            // Left Leg (viewer's right)
            bufferbuilder.pos(rLegX1, legY0, -0.5D).tex(8 / skinWidth, 32 / skinHeight).endVertex();
            bufferbuilder.pos(rLegX0, legY0, -0.5D).tex(4 / skinWidth, 32 / skinHeight).endVertex();
            bufferbuilder.pos(rLegX0, legY1, -0.5D).tex(4 / skinWidth, 20 / skinHeight).endVertex();
            bufferbuilder.pos(rLegX1, legY1, -0.5D).tex(8 / skinWidth, 20 / skinHeight).endVertex();

            // Left Arm (viewer's right, extended)
            float rArmX0 = bodyX1;
            float rArmX1 = rArmX0 + armLength;
            float rArmY0 = bodyY1 - armWidth;
            float rArmY1 = bodyY1;
            bufferbuilder.pos(rArmX1, rArmY0, -0.5D).tex(44 / skinWidth, 32 / skinHeight).endVertex(); // Bottom-right
            bufferbuilder.pos(rArmX0, rArmY0, -0.5D).tex(44 / skinWidth, 20 / skinHeight).endVertex(); // Bottom-left
            bufferbuilder.pos(rArmX0, rArmY1, -0.5D).tex(40 / skinWidth, 20 / skinHeight).endVertex(); // Top-left
            bufferbuilder.pos(rArmX1, rArmY1, -0.5D).tex(40 / skinWidth, 32 / skinHeight).endVertex(); // Top-right

            // Right Arm (viewer's left, fist)
            float lArmX0 = -armWidth / 2 + xOffset - 6;
            float lArmX1 = lArmX0 + armWidth;
            float lArmY0 = bodyY0 + 2 + 6;
            float lArmY1 = lArmY0 + armWidth;
            bufferbuilder.pos(lArmX1, lArmY0, -0.5D).tex(52 / skinWidth, 20 / skinHeight).endVertex(); // Bottom Right
            bufferbuilder.pos(lArmX0, lArmY0, -0.5D).tex(48 / skinWidth, 20 / skinHeight).endVertex(); // Bottom Left
            bufferbuilder.pos(lArmX0, lArmY1, -0.5D).tex(48 / skinWidth, 16 / skinHeight).endVertex(); // Top Left
            bufferbuilder.pos(lArmX1, lArmY1, -0.5D).tex(52 / skinWidth, 16 / skinHeight).endVertex(); // Top Right

            // Head
            bufferbuilder.pos(headX1, headY0, -0.5D).tex(16 / skinWidth, 16 / skinHeight).endVertex();
            bufferbuilder.pos(headX0, headY0, -0.5D).tex(8 / skinWidth, 16 / skinHeight).endVertex();
            bufferbuilder.pos(headX0, headY1, -0.5D).tex(8 / skinWidth, 8 / skinHeight).endVertex();
            bufferbuilder.pos(headX1, headY1, -0.5D).tex(16 / skinWidth, 8 / skinHeight).endVertex();

            tessellator.draw();

            // --- Overlay Layers ---
            float zOffset = -0.51f;
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);

            // Body
            bufferbuilder.pos(bodyX1 * overlayScale, bodyY0 * overlayScale, zOffset).tex(28 / skinWidth, 48 / skinHeight).endVertex();
            bufferbuilder.pos(bodyX0 * overlayScale, bodyY0 * overlayScale, zOffset).tex(20 / skinWidth, 48 / skinHeight).endVertex();
            bufferbuilder.pos(bodyX0 * overlayScale, bodyY1 * overlayScale, zOffset).tex(20 / skinWidth, 36 / skinHeight).endVertex();
            bufferbuilder.pos(bodyX1 * overlayScale, bodyY1 * overlayScale, zOffset).tex(28 / skinWidth, 36 / skinHeight).endVertex();

            // Right Leg (viewer's left) Overlay - MIRRORED
            bufferbuilder.pos(legX1 * overlayScale, legY0 * overlayScale, zOffset).tex(4 / skinWidth, 48 / skinHeight).endVertex();
            bufferbuilder.pos(legX0 * overlayScale, legY0 * overlayScale, zOffset).tex(8 / skinWidth, 48 / skinHeight).endVertex();
            bufferbuilder.pos(legX0 * overlayScale, legY1 * overlayScale, zOffset).tex(8 / skinWidth, 36 / skinHeight).endVertex();
            bufferbuilder.pos(legX1 * overlayScale, legY1 * overlayScale, zOffset).tex(4 / skinWidth, 36 / skinHeight).endVertex();

            // Left Leg Overlay
            bufferbuilder.pos(rLegX1 * overlayScale, legY0 * overlayScale, zOffset).tex(8 / skinWidth, 48 / skinHeight).endVertex();
            bufferbuilder.pos(rLegX0 * overlayScale, legY0 * overlayScale, zOffset).tex(4 / skinWidth, 48 / skinHeight).endVertex();
            bufferbuilder.pos(rLegX0 * overlayScale, legY1 * overlayScale, zOffset).tex(4 / skinWidth, 36 / skinHeight).endVertex();
            bufferbuilder.pos(rLegX1 * overlayScale, legY1 * overlayScale, zOffset).tex(8 / skinWidth, 36 / skinHeight).endVertex();

            // Right Arm Overlay
//            bufferbuilder.pos(rArmX1 * overlayScale, rArmY0 * overlayScale, zOffset).tex(60 / skinWidth, 32 / skinHeight).endVertex(); // Bottom-right
//            bufferbuilder.pos(rArmX0 * overlayScale, rArmY0 * overlayScale, zOffset).tex(60 / skinWidth, 20 / skinHeight).endVertex(); // Bottom-left
//            bufferbuilder.pos(rArmX0 * overlayScale, rArmY1 * overlayScale, zOffset).tex(56 / skinWidth, 20 / skinHeight).endVertex(); // Top-left
//            bufferbuilder.pos(rArmX1 * overlayScale, rArmY1 * overlayScale, zOffset).tex(56 / skinWidth, 32 / skinHeight).endVertex(); // Top-right

            // Left Arm Overlay
            bufferbuilder.pos(lArmX1 * overlayScale, lArmY0 * overlayScale, zOffset).tex(52 / skinWidth, 36 / skinHeight).endVertex();
            bufferbuilder.pos(lArmX0 * overlayScale, lArmY0 * overlayScale, zOffset).tex(48 / skinWidth, 36 / skinHeight).endVertex();
            bufferbuilder.pos(lArmX0 * overlayScale, lArmY1 * overlayScale, zOffset).tex(48 / skinWidth, 32 / skinHeight).endVertex();
            bufferbuilder.pos(lArmX1 * overlayScale, lArmY1 * overlayScale, zOffset).tex(52 / skinWidth, 32 / skinHeight).endVertex();


            // Head
            bufferbuilder.pos(headX1 * overlayScale, headY0 * overlayScale, zOffset).tex(48 / skinWidth, 16 / skinHeight).endVertex();
            bufferbuilder.pos(headX0 * overlayScale, headY0 * overlayScale, zOffset).tex(40 / skinWidth, 16 / skinHeight).endVertex();
            bufferbuilder.pos(headX0 * overlayScale, headY1 * overlayScale, zOffset).tex(40 / skinWidth, 8 / skinHeight).endVertex();
            bufferbuilder.pos(headX1 * overlayScale, headY1 * overlayScale, zOffset).tex(48 / skinWidth, 8 / skinHeight).endVertex();

            tessellator.draw();
        }
    }
}

