package com.windanesz.tracesofthefallen.client.render.entity;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.client.model.ModelSidhe;
import com.windanesz.tracesofthefallen.client.render.entity.layers.LayerSidheEyes;
import com.windanesz.tracesofthefallen.entity.EntitySidhe;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderSidhe extends RenderLiving<EntitySidhe> {

	private static final ResourceLocation TEXTURE = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/sidhe.png");

	public RenderSidhe(RenderManager rendermanagerIn) {
		super(rendermanagerIn, new ModelSidhe(), 0.5F);
		this.addLayer(new LayerSidheEyes(this));
	}

	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(EntitySidhe entity) {
		return TEXTURE;
	}
}
