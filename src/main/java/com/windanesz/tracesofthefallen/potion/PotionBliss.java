package com.windanesz.tracesofthefallen.potion;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.client.Utils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PotionBliss extends Potion {

	private final ResourceLocation texture;

	public PotionBliss(String name, boolean isBadEffectIn, int liquidColorIn, ResourceLocation texture) {
		super(isBadEffectIn, liquidColorIn);
		this.setPotionName("potion." + TracesOfTheFallen.MODID + ":" + name);
		this.texture = texture;

	}

	@Override
	public boolean isReady(int duration, int amplifier) {
		int k = 50 >> amplifier;
		if (k > 0) {
			return duration % k == 0;
		} else {
			return true;
		}
	}

	public void performEffect(EntityLivingBase entityLivingBaseIn, int amplifier) {
        if (entityLivingBaseIn.getHealth() < entityLivingBaseIn.getMaxHealth()) {
            // Use the value from settings for healing.
            entityLivingBaseIn.heal((float) Settings.miscSettings.blissHealingAmount);
        }
    }

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventoryEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc) {
		drawIcon(x + 6, y + 7, effect, mc);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderHUDEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc, float alpha) {
		net.minecraft.client.renderer.GlStateManager.color(1, 1, 1, alpha);
		drawIcon(x + 3, y + 3, effect, mc);
	}

	@SideOnly(Side.CLIENT)
	protected void drawIcon(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc) {
		mc.renderEngine.bindTexture(texture);
		Utils.drawTexturedRect(x, y, 0, 0, 18, 18, 18, 18);
	}
}
