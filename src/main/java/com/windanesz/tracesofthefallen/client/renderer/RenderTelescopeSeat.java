package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.entity.EntityTelescopeSeat;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderTelescopeSeat extends Render<EntityTelescopeSeat> {

	public RenderTelescopeSeat(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityTelescopeSeat entity, double x, double y, double z, float entityYaw, float partialTicks) {}

	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(EntityTelescopeSeat entity) {
		return null;
	}
}
