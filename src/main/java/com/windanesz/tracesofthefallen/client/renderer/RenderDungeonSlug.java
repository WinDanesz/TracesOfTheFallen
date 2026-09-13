package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.client.model.ModelDungeonSlug;
import com.windanesz.tracesofthefallen.entity.EntityDungeonSlug;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderDungeonSlug extends RenderSlug<EntityDungeonSlug> {

	private static final ResourceLocation TEXTURE = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/dungeon_slug.png");

	public RenderDungeonSlug(RenderManager rendermanagerIn) {
		super(rendermanagerIn, new ModelDungeonSlug(), 0.4F);
	}

	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(EntityDungeonSlug entity) {
		return TEXTURE;
	}
}
