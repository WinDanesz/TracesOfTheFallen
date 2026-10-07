package com.windanesz.tracesofthefallen.client.model;

import com.windanesz.tracesofthefallen.entity.EntitySlug;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

/**
 * Segmented slug body with three thin sensory spikes near the front ("senser_right/left/down", the drooping
 * tendril reaching toward prey) and a pair of dorsal fin spikes along the mid segment.
 * <p>
 * "torax" is deliberately NOT parented to "body" (see {@link #render}) so its crawl animation can stretch
 * it (and the tail/fins riding on it) along its own length like an inchworm, rather than just tilting in place.
 * Geometry below matches Blockbench's own "Export Java Entity" convention (cross-checked against the
 * dungeon slug's export, which shares the same body/torax/tail layout).
 */
public class ModelInkSlug extends ModelBase {

	// torax's original offset from body, applied manually in render() (unscaled) so the stretch scale only
	// affects torax's own length, not its attachment point to the body segment.
	private static final float TORAX_OFFSET_X = 0.0F;
	private static final float TORAX_OFFSET_Y = -0.9268F;
	private static final float TORAX_OFFSET_Z = -2.975F;

	private final ModelRenderer body;
	private final ModelRenderer senserRight;
	private final ModelRenderer senserRightTip;
	private final ModelRenderer senserLeft;
	private final ModelRenderer senserLeftTip;
	private final ModelRenderer senserDown;
	private final ModelRenderer senserDownTip;
	private final ModelRenderer torax;
	private final ModelRenderer dorsalSpikeA;
	private final ModelRenderer dorsalSpikeB;
	private final ModelRenderer tail;

	public ModelInkSlug() {
		textureWidth = 64;
		textureHeight = 64;

		body = new ModelRenderer(this);
		body.setRotationPoint(0.0F, 19.5732F, 1.975F);
		body.cubeList.add(new ModelBox(body, 0, 0, -4.0F, -2.5732F, -9.975F, 8, 7, 7, 0.0F, false));

		senserRight = new ModelRenderer(this);
		senserRight.setRotationPoint(-2.7782F, -1.4519F, -9.975F);
		body.addChild(senserRight);

		senserRightTip = new ModelRenderer(this);
		senserRightTip.setRotationPoint(7.7782F, -2.1213F, 14.0F);
		senserRight.addChild(senserRightTip);
		setRotationAngle(senserRightTip, 0.0F, 0.0F, -0.7854F);
		senserRightTip.cubeList.add(new ModelBox(senserRightTip, 29, 5, -8.0F, -4.0F, -21.0F, 2, 0, 9, 0.0F, false));

		senserLeft = new ModelRenderer(this);
		senserLeft.setRotationPoint(2.7782F, -1.4519F, -9.975F);
		body.addChild(senserLeft);

		senserLeftTip = new ModelRenderer(this);
		senserLeftTip.setRotationPoint(-7.7782F, -2.1213F, 14.0F);
		senserLeft.addChild(senserLeftTip);
		setRotationAngle(senserLeftTip, 0.0F, 0.0F, 0.7854F);
		senserLeftTip.cubeList.add(new ModelBox(senserLeftTip, 29, 5, 6.0F, -4.0F, -21.0F, 2, 0, 9, 0.0F, true));

		senserDown = new ModelRenderer(this);
		senserDown.setRotationPoint(-0.0218F, 3.5481F, -9.975F);
		body.addChild(senserDown);

		senserDownTip = new ModelRenderer(this);
		senserDownTip.setRotationPoint(0.0F, 0.0F, -2.5F);
		senserDown.addChild(senserDownTip);
		setRotationAngle(senserDownTip, 0.0F, 0.0F, -1.5708F);
		senserDownTip.cubeList.add(new ModelBox(senserDownTip, 29, 5, -1.0F, 0.0F, -4.5F, 2, 0, 9, 0.0F, true));

		torax = new ModelRenderer(this);
		torax.setRotationPoint(0.0F, 0.0F, 0.0F);
		torax.cubeList.add(new ModelBox(torax, 0, 16, -5.0F, -2.6464F, 0.0F, 10, 8, 12, 0.0F, false));

		dorsalSpikeA = new ModelRenderer(this);
		dorsalSpikeA.setRotationPoint(5.0F, -2.6464F, 6.0F);
		torax.addChild(dorsalSpikeA);
		setRotationAngle(dorsalSpikeA, 0.0F, 0.0F, -0.7854F);
		dorsalSpikeA.cubeList.add(new ModelBox(dorsalSpikeA, 23, 2, 0.0F, 0.0F, -6.0F, 2, 0, 12, 0.0F, false));

		dorsalSpikeB = new ModelRenderer(this);
		dorsalSpikeB.setRotationPoint(-5.0F, -2.6464F, 6.0F);
		torax.addChild(dorsalSpikeB);
		setRotationAngle(dorsalSpikeB, 0.0F, 0.0F, 0.7854F);
		dorsalSpikeB.cubeList.add(new ModelBox(dorsalSpikeB, 23, 2, -2.0F, 0.0F, -6.0F, 2, 0, 12, 0.0F, true));

		tail = new ModelRenderer(this);
		tail.setRotationPoint(0.0F, 2.3536F, 12.0F);
		torax.addChild(tail);
		tail.cubeList.add(new ModelBox(tail, 0, 38, -3.0F, -3.0F, 0.0F, 6, 6, 9, 0.0F, false));
	}

	private float stretch = 1.0F;

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch, float scale) {
		body.render(scale);

		// Stretch the mid segment (and the tail/fins riding on it) along its own length for an inchworm-like
		// crawl, instead of just tilting the rigid segments in place. The body->torax offset is applied here
		// (unscaled) rather than via torax's own rotation point, so the stretch only affects torax's length
		// and not where it attaches to the body segment.
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
			senserDown.rotateAngleX = 0.9F;
		} else if (attack <= 0.0F) {
			this.stretch = 1.0F + crawl * 0.32F * MathHelper.sin(wave);
			tail.rotateAngleX += crawl * 0.25F * MathHelper.sin(wave - 1.2F);

			float reach = MathHelper.sin(ageInTicks * 0.045F) * 0.10F;
			senserDown.rotateAngleX += 0.15F + reach;
			senserDown.rotateAngleZ += MathHelper.cos(ageInTicks * 0.037F) * 0.08F;
			float idleSway = MathHelper.sin(ageInTicks * 0.05F) * 0.06F;
			senserRight.rotateAngleZ += idleSway;
			senserLeft.rotateAngleZ -= idleSway;
		}

		applyAttackPose(attack);
	}

	private void applyAttackPose(float attack) {
		if (attack <= 0.0F) return;
		float windup = MathHelper.clamp(attack / 0.64F, 0.0F, 1.0F);
		float strike = MathHelper.clamp((attack - 0.64F) / 0.36F, 0.0F, 1.0F);
		float lunge = MathHelper.sin(strike * ((float) Math.PI * 0.5F));
		float crouch = windup * (1.0F - lunge);

		// Freeze the feelers during the attack: crouch first, then make one unmistakable forward shove.
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
		senserRight.rotateAngleX = 0.0F;
		senserRight.rotateAngleZ = 0.0F;
		senserLeft.rotateAngleX = 0.0F;
		senserLeft.rotateAngleZ = 0.0F;
		senserDown.rotateAngleX = 0.0F;
		senserDown.rotateAngleZ = 0.0F;
	}
}
