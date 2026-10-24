package com.windanesz.tracesofthefallen.client.model;

import com.windanesz.tracesofthefallen.entity.living.EntitySlug;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

/**
 * Segmented slug body: a head/front segment (rendered directly on the root part), a mid segment ("torax")
 * carrying the tail, and two small horn-like sensory spikes ("eye_left"/"eye_right") near the front.
 * <p>
 * "torax" is deliberately NOT parented to "body" (see {@link #render}) so its crawl animation can stretch
 * it (and the tail riding on it) along its own length like an inchworm, rather than just tilting in place.
 * Geometry below is taken from Blockbench's own "Export Java Entity" output for this model, not hand-derived.
 */
public class ModelDungeonSlug extends ModelBase {

	// torax's original offset from body, applied manually in render() (unscaled) so the stretch scale only
	// affects torax's own length, not its attachment point to the body segment.
	private static final float TORAX_OFFSET_X = 0.0F;
	private static final float TORAX_OFFSET_Y = -0.9268F;
	private static final float TORAX_OFFSET_Z = -2.975F;

	private final ModelRenderer body;
	private final ModelRenderer torax;
	private final ModelRenderer tail;
	private final ModelRenderer eyeLeft;
	private final ModelRenderer eyeLeftTip;
	private final ModelRenderer eyeRight;
	private final ModelRenderer eyeRightTip;

	public ModelDungeonSlug() {
		textureWidth = 64;
		textureHeight = 64;

		body = new ModelRenderer(this);
		body.setRotationPoint(0.0F, 19.5732F, 1.975F);
		body.cubeList.add(new ModelBox(body, 0, 0, -4.0F, -1.5732F, -9.975F, 8, 6, 7, 0.0F, false));

		torax = new ModelRenderer(this);
		torax.setRotationPoint(0.0F, 0.0F, 0.0F);
		torax.cubeList.add(new ModelBox(torax, 1, 17, -5.0F, -1.6464F, 0.0F, 10, 7, 11, 0.0F, false));

		tail = new ModelRenderer(this);
		tail.setRotationPoint(0.0F, 2.3536F, 12.0F);
		torax.addChild(tail);
		tail.cubeList.add(new ModelBox(tail, 0, 38, -3.0F, -2.0F, -1.0F, 6, 5, 9, 0.0F, false));

		eyeLeft = new ModelRenderer(this);
		eyeLeft.setRotationPoint(2.0F, -0.8659F, -10.2679F);
		body.addChild(eyeLeft);
		setRotationAngle(eyeLeft, 0.7777F, -0.1231F, 0.1240F);

		eyeLeftTip = new ModelRenderer(this);
		eyeLeftTip.setRotationPoint(1.0F, -5.0002F, 0.0F);
		eyeLeft.addChild(eyeLeftTip);
		setRotationAngle(eyeLeftTip, 0.0F, 0.7854F, 0.0F);
		eyeLeftTip.cubeList.add(new ModelBox(eyeLeftTip, 33, 5, -2.0F, -3.0F, 0.0F, 3, 9, 0, 0.0F, true));

		eyeRight = new ModelRenderer(this);
		eyeRight.setRotationPoint(-2.0F, -0.8659F, -10.2679F);
		body.addChild(eyeRight);
		setRotationAngle(eyeRight, 0.7777F, 0.1231F, -0.1240F);

		eyeRightTip = new ModelRenderer(this);
		eyeRightTip.setRotationPoint(-1.0F, -5.0002F, 0.0F);
		eyeRight.addChild(eyeRightTip);
		setRotationAngle(eyeRightTip, 0.0F, -0.7854F, 0.0F);
		eyeRightTip.cubeList.add(new ModelBox(eyeRightTip, 33, 5, -1.0F, -3.0F, 0.0F, 3, 9, 0, 0.0F, false));
	}

	private float stretch = 1.0F;

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch, float scale) {
		body.render(scale);

		// Stretch the mid segment (and the tail riding on it) along its own length for an inchworm-like
		// crawl, instead of just tilting the rigid segments in place. The body->torax offset is applied
		// here (unscaled) rather than via torax's own rotation point, so the stretch only affects torax's
		// length and not where it attaches to the body segment.
		GlStateManager.pushMatrix();
		GlStateManager.translate(body.rotationPointX * scale, body.rotationPointY * scale, body.rotationPointZ * scale);
		GlStateManager.translate(TORAX_OFFSET_X * scale, TORAX_OFFSET_Y * scale, TORAX_OFFSET_Z * scale);
		GlStateManager.scale(1.0F, 1.0F, this.stretch);
		torax.render(scale);
		GlStateManager.popMatrix();
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch, float scaleFactor, Entity entityIn) {
		resetAnimatedPose();

		boolean attached = entityIn instanceof EntitySlug && ((EntitySlug) entityIn).isAttached();
		float attack = entityIn instanceof EntitySlug
				? ((EntitySlug) entityIn).getAttackAnimationProgress(ageInTicks - entityIn.ticksExisted) : 0.0F;
		float crawl = Math.min(1.0F, limbSwingAmount);
		float wave = limbSwing * 0.6F;

		if (attached) {
			this.stretch = 1.0F;
			torax.rotateAngleX = 0.32F;
			tail.rotateAngleX = 0.55F;
		} else if (attack <= 0.0F) {
			this.stretch = 1.0F + crawl * 0.32F * MathHelper.sin(wave);
			tail.rotateAngleX += crawl * 0.25F * MathHelper.sin(wave - 1.2F);

			float idleSway = MathHelper.sin(ageInTicks * 0.05F) * 0.06F;
			eyeLeft.rotateAngleZ += idleSway;
			eyeRight.rotateAngleZ -= idleSway;
		}

		applyAttackPose(attack);
	}

	private void applyAttackPose(float attack) {
		if (attack <= 0.0F) return;
		float windup = MathHelper.clamp(attack / 0.64F, 0.0F, 1.0F);
		float strike = MathHelper.clamp((attack - 0.64F) / 0.36F, 0.0F, 1.0F);
		float lunge = MathHelper.sin(strike * ((float) Math.PI * 0.5F));
		float crouch = windup * (1.0F - lunge);

		// A deliberate crouch and a single forward shove read much more clearly than rotating each segment.
		this.stretch = 1.0F - crouch * 0.24F + lunge * 0.12F;
		body.rotationPointY += crouch * 0.90F;
		body.rotationPointZ -= lunge * 3.2F;
		// The front extends along negative Z; negative pitch lifts the head and its feelers.
		body.rotateAngleX -= 0.2618F * windup;
	}

	private void resetAnimatedPose() {
		this.stretch = 1.0F;
		body.rotationPointY = 19.5732F;
		body.rotationPointZ = 1.975F;
		body.rotateAngleX = 0.0F;
		torax.rotateAngleX = 0.0F;
		tail.rotateAngleX = 0.0F;
		eyeLeft.rotateAngleX = 0.7777F;
		eyeLeft.rotateAngleY = -0.1231F;
		eyeLeft.rotateAngleZ = 0.1240F;
		eyeRight.rotateAngleX = 0.7777F;
		eyeRight.rotateAngleY = 0.1231F;
		eyeRight.rotateAngleZ = -0.1240F;
	}
}
