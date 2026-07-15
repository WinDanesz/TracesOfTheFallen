package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.client.model.ModelGoblinSkeleton;
import com.windanesz.tracesofthefallen.entity.EntityGoblinSkeleton;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderGoblinSkeleton extends RenderLiving<EntityGoblinSkeleton> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/goblin_skeleton.png");

	public RenderGoblinSkeleton(RenderManager renderManager) {
		super(renderManager, new ModelGoblinSkeleton(), 0.3F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityGoblinSkeleton entity) {
		return TEXTURE;
	}
}
