package com.windanesz.tracesofthefallen.potion;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.client.Utils;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class PotionTotf extends Potion {

	private final ResourceLocation texture;

	protected PotionTotf(String name, boolean isBadEffectIn, int liquidColorIn, ResourceLocation texture) {
		super(isBadEffectIn, liquidColorIn);
		this.setPotionName("potion." + TracesOfTheFallen.MODID + ":" + name);
		this.texture = texture;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventoryEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc) {
		drawIcon(x + 6, y + 7, mc);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderHUDEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc, float alpha) {
		net.minecraft.client.renderer.GlStateManager.color(1, 1, 1, alpha);
		drawIcon(x + 3, y + 3, mc);
	}

	@SideOnly(Side.CLIENT)
	protected void drawIcon(int x, int y, net.minecraft.client.Minecraft mc) {
		mc.renderEngine.bindTexture(texture);
		Utils.drawTexturedRect(x, y, 0, 0, 18, 18, 18, 18);
	}
}
