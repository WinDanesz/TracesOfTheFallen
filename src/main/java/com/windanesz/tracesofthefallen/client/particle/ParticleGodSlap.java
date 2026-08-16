package com.windanesz.tracesofthefallen.client.particle;

import com.windanesz.tracesofthefallen.client.ClientProxy;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleGodSlap extends Particle {

    public ParticleGodSlap(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z, 0, 0, 0);
        this.particleRed = 1.0F;
        this.particleGreen = 1.0F;
        this.particleBlue = 1.0F;
        this.particleMaxAge = 20;
        this.particleScale = 6.0F;
        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;
        this.particleGravity = 0;
        this.particleAlpha = 1.0F;
        this.canCollide = false;

        TextureAtlasSprite sprite = ClientProxy.getGodSlapSprite();
        if (sprite != null) {
            System.out.println("[Godslap] Successfully bound sprite: " + sprite.getIconName());
            this.setParticleTexture(sprite);
        } else {
            System.out.println("[Godslap] WARNING: Sprite was NULL!");
        }
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotX, float rotXZ, float rotZ, float rotYZ, float rotXY) {
        float ageRatio = (float) this.particleAge / (float) this.particleMaxAge;
        float alpha = 1.0F - ageRatio;
        this.particleAlpha = alpha;
        super.renderParticle(buffer, entityIn, partialTicks, rotX, rotXZ, rotZ, rotYZ, rotXY);
    }

    @Override
    public int getFXLayer() {
        return 1; // Particle atlas layer
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        }
    }
}
