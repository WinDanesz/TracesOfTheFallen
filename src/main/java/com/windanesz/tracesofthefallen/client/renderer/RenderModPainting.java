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
            if (p != ItemModPainting.EnumPainting.PAINTING_SIFTING_WINDS) {
                GlStateManager.translate(0, -10, 0); // Move down by 16 pixels
            }
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
                
                bufferbuilder.pos((double) f15, (double) f17, 0.5D).tex((double) f20, (double) f22).normal(0.0F, 0.0F, 1.0F).endVertex();
                bufferbuilder.pos((double) f16, (double) f17, 0.5D).tex((double) f19, (double) f22).normal(0.0F, 0.0F, 1.0F).endVertex();
                bufferbuilder.pos((double) f16, (double) f18, 0.5D).tex((double) f19, (double) f21).normal(0.0F, 0.0F, 1.0F).endVertex();
                bufferbuilder.pos((double) f15, (double) f18, 0.5D).tex((double) f20, (double) f21).normal(0.0F, 0.0F, 1.0F).endVertex();
                
                float topV = f22 + f10;
                bufferbuilder.pos((double) f15, (double) f17, -0.5D).tex((double) f20, (double) topV).normal(0.0F, 1.0F, 0.0F).endVertex();
                bufferbuilder.pos((double) f16, (double) f17, -0.5D).tex((double) f19, (double) topV).normal(0.0F, 1.0F, 0.0F).endVertex();
                bufferbuilder.pos((double) f16, (double) f17, 0.5D).tex((double) f19, (double) topV).normal(0.0F, 1.0F, 0.0F).endVertex();
                bufferbuilder.pos((double) f15, (double) f17, 0.5D).tex((double) f20, (double) topV).normal(0.0F, 1.0F, 0.0F).endVertex();
                
                float bottomV = f21 - f10;
                bufferbuilder.pos((double) f15, (double) f18, 0.5D).tex((double) f20, (double) bottomV).normal(0.0F, -1.0F, 0.0F).endVertex();
                bufferbuilder.pos((double) f16, (double) f18, 0.5D).tex((double) f19, (double) bottomV).normal(0.0F, -1.0F, 0.0F).endVertex();
                bufferbuilder.pos((double) f16, (double) f18, -0.5D).tex((double) f19, (double) bottomV).normal(0.0F, -1.0F, 0.0F).endVertex();
                bufferbuilder.pos((double) f15, (double) f18, -0.5D).tex((double) f20, (double) bottomV).normal(0.0F, -1.0F, 0.0F).endVertex();
                
                float leftU = f20 + f10;
                bufferbuilder.pos((double) f15, (double) f17, 0.5D).tex((double) leftU, (double) f22).normal(-1.0F, 0.0F, 0.0F).endVertex();
                bufferbuilder.pos((double) f15, (double) f18, 0.5D).tex((double) leftU, (double) f21).normal(-1.0F, 0.0F, 0.0F).endVertex();
                bufferbuilder.pos((double) f15, (double) f18, -0.5D).tex((double) leftU, (double) f21).normal(-1.0F, 0.0F, 0.0F).endVertex();
                bufferbuilder.pos((double) f15, (double) f17, -0.5D).tex((double) leftU, (double) f22).normal(-1.0F, 0.0F, 0.0F).endVertex();
                
                float rightU = f19 - f10;
                bufferbuilder.pos((double) f16, (double) f17, -0.5D).tex((double) rightU, (double) f22).normal(1.0F, 0.0F, 0.0F).endVertex();
                bufferbuilder.pos((double) f16, (double) f18, -0.5D).tex((double) rightU, (double) f21).normal(1.0F, 0.0F, 0.0F).endVertex();
                bufferbuilder.pos((double) f16, (double) f18, 0.5D).tex((double) rightU, (double) f21).normal(1.0F, 0.0F, 0.0F).endVertex();
                bufferbuilder.pos((double) f16, (double) f17, 0.5D).tex((double) rightU, (double) f22).normal(1.0F, 0.0F, 0.0F).endVertex();
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
        } else if (painting == ItemModPainting.EnumPainting.PAINTING_SIFTING_WINDS) {
            // 3/8 scale player
            float halfW = painting.sizeX / 2.0f;
            float halfH = painting.sizeY / 2.0f;
            double z = -0.5D;
            double zFace = -0.505D;
            double zOverlay = -0.51D;
            double zFaceOverlay = -0.515D;

            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

            // Base Layer
            // Head (right side of the head, front towards the right edge)
            skinPixels(bufferbuilder, halfW, halfH, 80, 83, 23, 26, z, 0, 8, 8, 16);
            // Torso
            // (the torso skips the neck pixel at col 81, row 26, it is drawn with the face pixels)
            skinPixels(bufferbuilder, halfW, halfH, 80, 81, 26, 27, z, 20, 20, 24, 22);
            skinPixels(bufferbuilder, halfW, halfH, 80, 82, 27, 30, z, 20, 23, 28, 32);
            // right arm (viewer's left),
            skinPixels(bufferbuilder, halfW, halfH, 78, 80, 27, 30, z, 44, 20, 48, 32);
            skinPixels(bufferbuilder, halfW, halfH, 79, 80, 26, 27, z, 44, 20, 48, 24);
            // left arm (viewer's right), only 3 px long
            skinPixels(bufferbuilder, halfW, halfH, 82, 83, 27, 30, z, 36, 52, 40, 64);
            // ight leg (viewer's left)
            skinPixels(bufferbuilder, halfW, halfH, 79, 80, 30, 35, z, 4, 20, 8, 32);
            // left leg (viewer's right)
            skinPixels(bufferbuilder, halfW, halfH, 80, 82, 30, 35, z, 20, 52, 24, 64);
            // face pixels from the front of the head
            skinPixels(bufferbuilder, halfW, halfH, 82, 83, 24, 25, zFace, 14, 11, 15, 12);
            skinPixels(bufferbuilder, halfW, halfH, 82, 83, 25, 26, zFace, 14, 13, 15, 14);
            skinPixels(bufferbuilder, halfW, halfH, 81, 82, 25, 26, zFace, 11, 13, 12, 14);
            skinPixels(bufferbuilder, halfW, halfH, 81, 82, 26, 27, zFace, 45, 30, 46, 31);

            // overlay Layers
            skinPixels(bufferbuilder, halfW, halfH, 80, 83, 23, 26, zOverlay, 32, 8, 40, 16);
            skinPixels(bufferbuilder, halfW, halfH, 80, 81, 26, 27, zOverlay, 20, 36, 24, 38);
            skinPixels(bufferbuilder, halfW, halfH, 80, 82, 27, 30, zOverlay, 20, 35, 28, 44);
            skinPixels(bufferbuilder, halfW, halfH, 78, 80, 27, 30, zOverlay, 44, 36, 48, 48);
            skinPixels(bufferbuilder, halfW, halfH, 79, 80, 26, 27, zOverlay, 44, 36, 48, 40);
            skinPixels(bufferbuilder, halfW, halfH, 82, 83, 27, 30, zOverlay, 52, 52, 56, 64);
            skinPixels(bufferbuilder, halfW, halfH, 79, 80, 30, 35, zOverlay, 4, 36, 8, 48);
            skinPixels(bufferbuilder, halfW, halfH, 80, 82, 30, 35, zOverlay, 4, 52, 8, 64);
            skinPixels(bufferbuilder, halfW, halfH, 82, 83, 24, 25, zFaceOverlay, 46, 11, 47, 12);
            skinPixels(bufferbuilder, halfW, halfH, 82, 83, 25, 26, zFaceOverlay, 46, 13, 47, 14);
            skinPixels(bufferbuilder, halfW, halfH, 81, 82, 25, 26, zFaceOverlay, 43, 13, 44, 14);

            tessellator.draw();
        }
    }

    // Distant silhouette, the skin is darkened to blend with the painted sunset
    private static final float[] SIFTING_WINDS_TINT = {0.85F, 0.65F, 0.65F};
    private static final float SIFTING_WINDS_SHADOW = 0.7F;

    /**
     * The painting pixels on the shaded (far) side of the figure
     */
    private static boolean isSiftingWindsShadow(int col, int row) {
        switch (col) {
            case 82:
                return row <= 29;
            case 81:
                return row >= 25 && row <= 34;
            case 80:
                return row >= 27 && row <= 34;
            default:
                return false;
        }
    }

    /**
     * Fills the given area of the painting (in painting texture pixels, whole numbers) with one flat quad per painting pixel,
     * each showing the single skin texel (64x64 skin) at the center of that pixel within the given skin region. Unmirrored.
     * The pixels are tinted, and darkened if they are in the shadow.
     */
    private static void skinPixels(BufferBuilder bufferbuilder, float halfW, float halfH, int colL, int colR, int rowT, int rowB, double z, float u0, float v0, float u1, float v1) {
        float skinSize = 64f;
        for (int col = colL; col < colR; col++) {
            float u = (float) Math.floor(u0 + (col + 0.5f - colL) / (colR - colL) * (u1 - u0)) + 0.5f;
            for (int row = rowT; row < rowB; row++) {
                float v = (float) Math.floor(v0 + (row + 0.5f - rowT) / (rowB - rowT) * (v1 - v0)) + 0.5f;
                float brightness = isSiftingWindsShadow(col, row) ? SIFTING_WINDS_SHADOW : 1.0F;
                float r = SIFTING_WINDS_TINT[0] * brightness;
                float g = SIFTING_WINDS_TINT[1] * brightness;
                float b = SIFTING_WINDS_TINT[2] * brightness;
                float xLeft = halfW - col;
                float xRight = halfW - col - 1;
                float yTop = halfH - row;
                float yBottom = halfH - row - 1;
                bufferbuilder.pos(xLeft, yBottom, z).tex(u / skinSize, v / skinSize).color(r, g, b, 1.0F).endVertex();
                bufferbuilder.pos(xRight, yBottom, z).tex(u / skinSize, v / skinSize).color(r, g, b, 1.0F).endVertex();
                bufferbuilder.pos(xRight, yTop, z).tex(u / skinSize, v / skinSize).color(r, g, b, 1.0F).endVertex();
                bufferbuilder.pos(xLeft, yTop, z).tex(u / skinSize, v / skinSize).color(r, g, b, 1.0F).endVertex();
            }
        }
    }
}
