package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.entity.EntityGoblin;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderGoblinVariant extends RenderBiped<EntityGoblin> {
	private final ResourceLocation texture;

	public RenderGoblinVariant(RenderManager renderManager, ModelBiped model, String textureName) {
		super(renderManager, model, 0.2F);
		this.texture = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/" + textureName + ".png");
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityGoblin entity) {
		return texture;
	}
}
