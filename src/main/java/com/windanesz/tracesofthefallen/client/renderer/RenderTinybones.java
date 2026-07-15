package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.client.model.ModelTinyBones;
import com.windanesz.tracesofthefallen.entity.EntityTinybones;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderTinybones extends RenderLiving<EntityTinybones> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/tinybones.png");

	public RenderTinybones(RenderManager renderManager) {
		super(renderManager, new ModelTinyBones(), 0.3F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityTinybones entity) {
		return TEXTURE;
	}
}
