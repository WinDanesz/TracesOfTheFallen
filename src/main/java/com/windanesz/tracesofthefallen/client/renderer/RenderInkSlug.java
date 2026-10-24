package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.client.model.ModelInkSlug;
import com.windanesz.tracesofthefallen.entity.living.EntityInkSlug;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderInkSlug extends RenderSlug<EntityInkSlug> {

	private static final ResourceLocation TEXTURE = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/inkslug.png");

	public RenderInkSlug(RenderManager rendermanagerIn) {
		super(rendermanagerIn, new ModelInkSlug(), 0.4F);
	}

	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(EntityInkSlug entity) {
		return TEXTURE;
	}
}
