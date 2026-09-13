package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.entity.EntitySlug;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.EnumFacing;

/**
 * Shared renderer for the slug family: on top of the usual yaw rotation, it tips the model to lie flat
 * against whatever wall or ceiling the slug is currently clinging to.
 */
public abstract class RenderSlug<T extends EntitySlug> extends RenderLiving<T> {

	/** How far (in blocks) to nudge the render towards whatever surface it's clinging to; 3 pixels. */
	private static final float CLOSER_OFFSET = 3.0F / 16.0F;

	protected RenderSlug(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
		super(renderManagerIn, modelBaseIn, shadowSizeIn);
	}

	@Override
	protected void applyRotations(T entity, float ageInTicks, float rotationYaw, float partialTicks) {
		EnumFacing facing = entity.getClimbFacing();
		if (facing == EnumFacing.UP || entity.deathTime > 0) {
			super.applyRotations(entity, ageInTicks, rotationYaw, partialTicks);
			return;
		}

		// Collision only guarantees the hitbox touches the block; nudge the visible model a few pixels
		// further towards the surface so it doesn't read as visibly detached from it.
		GlStateManager.translate(-facing.getXOffset() * CLOSER_OFFSET, -facing.getYOffset() * CLOSER_OFFSET,
				-facing.getZOffset() * CLOSER_OFFSET);

		// Tip the (already yaw-oriented) body so its dorsal side points along the surface's outward normal,
		// then apply the usual yaw rotation on the raw model, same as the ground case.
		switch (facing) {
			case DOWN:
				// The model's belly sits right at the render anchor (posY, the hitbox's bottom), but ceiling
				// adhesion collision naturally rests the hitbox's TOP against the ceiling instead - shift the
				// whole render up by the hitbox height so the belly actually touches the ceiling.
				GlStateManager.translate(0.0F, entity.height, 0.0F);
				GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
				break;
			case NORTH:
				GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
				break;
			case SOUTH:
				GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
				break;
			case EAST:
				GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
				break;
			case WEST:
				GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
				break;
			default:
				break;
		}
		GlStateManager.rotate(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);
	}
}
