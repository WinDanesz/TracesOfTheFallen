package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.entity.EntityDioptraSeat;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderDioptraSeat extends Render<EntityDioptraSeat> {

	public RenderDioptraSeat(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityDioptraSeat entity, double x, double y, double z, float entityYaw, float partialTicks) {}

	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(EntityDioptraSeat entity) {
		return null;
	}
}
