package com.windanesz.tracesofthefallen.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelSidhe extends ModelBase {
	private final ModelRenderer bone;
	private final ModelRenderer head;
	private final ModelRenderer body;
	private final ModelRenderer fur;
	private final ModelRenderer front_left_leg;
	private final ModelRenderer front_right_leg;
	private final ModelRenderer back_left_leg;
	private final ModelRenderer back_right_leg;
	private final ModelRenderer tail;

	public ModelSidhe() {
		textureWidth = 64;
		textureHeight = 32;

		bone = new ModelRenderer(this);
		bone.setRotationPoint(0.0143F, 12.8355F, 0.6104F);
		

		head = new ModelRenderer(this);
		head.setRotationPoint(-0.0143F, 2.2529F, -6.7643F);
		bone.addChild(head);
		head.cubeList.add(new ModelBox(head, 0, 0, -2.5F, -3.995F, -2.625F, 5, 4, 5, 0.0F, false));
		head.cubeList.add(new ModelBox(head, 0, 24, -1.5F, -2.015F, -3.625F, 3, 2, 2, 0.0F, false));
		head.cubeList.add(new ModelBox(head, 51, 14, -4.5F, -6.995F, 0.375F, 4, 6, 0, 0.001F, false));
		head.cubeList.add(new ModelBox(head, 51, 14, 0.5F, -6.995F, 0.375F, 4, 6, 0, 0.001F, true));

		body = new ModelRenderer(this);
		body.setRotationPoint(-0.0143F, 2.2578F, 0.1107F);
		bone.addChild(body);
		setRotationAngle(body, 1.5708F, 0.0F, 0.0F);
		body.cubeList.add(new ModelBox(body, 21, 2, -2.0F, -7.0F, -2.0F, 4, 11, 4, 0.0F, false));

		fur = new ModelRenderer(this);
		fur.setRotationPoint(0.0F, -5.0F, -2.01F);
		body.addChild(fur);
		setRotationAngle(fur, -0.3927F, 0.0F, 0.0F);
		fur.cubeList.add(new ModelBox(fur, 16, 24, -2.0F, 0.0F, 0.0F, 4, 3, 0, 0.0F, false));

		front_left_leg = new ModelRenderer(this);
		front_left_leg.setRotationPoint(2.0857F, 2.3579F, -4.8893F);
		bone.addChild(front_left_leg);
		front_left_leg.cubeList.add(new ModelBox(front_left_leg, 38, 4, -1.0F, -1.0F, -1.0F, 2, 9, 2, 0.0F, false));

		front_right_leg = new ModelRenderer(this);
		front_right_leg.setRotationPoint(-2.1143F, 2.3579F, -4.8893F);
		bone.addChild(front_right_leg);
		front_right_leg.cubeList.add(new ModelBox(front_right_leg, 38, 4, -1.0F, -1.0F, -1.0F, 2, 9, 2, 0.0F, true));

		back_left_leg = new ModelRenderer(this);
		back_left_leg.setRotationPoint(1.5857F, 2.2578F, 3.1107F);
		bone.addChild(back_left_leg);
		back_left_leg.cubeList.add(new ModelBox(back_left_leg, 8, 13, -1.1F, 3.0F, -1.0F, 2, 5, 2, 0.0F, true));
		back_left_leg.cubeList.add(new ModelBox(back_left_leg, 47, 4, -1.5F, -1.0F, -1.5F, 3, 5, 3, 0.0F, false));

		back_right_leg = new ModelRenderer(this);
		back_right_leg.setRotationPoint(-1.5143F, 2.2578F, 3.1107F);
		bone.addChild(back_right_leg);
		back_right_leg.cubeList.add(new ModelBox(back_right_leg, 8, 13, -1.1F, 3.0F, -1.0F, 2, 5, 2, 0.0F, false));
		back_right_leg.cubeList.add(new ModelBox(back_right_leg, 47, 4, -1.6F, -1.0F, -1.4F, 3, 5, 3, 0.0F, true));

		tail = new ModelRenderer(this);
		tail.setRotationPoint(-0.01F, 0.2578F, 3.2107F);
		bone.addChild(tail);
		setRotationAngle(tail, 1.5708F, 0.0F, -3.1416F);
		tail.cubeList.add(new ModelBox(tail, 24, 7, 0.0F, -5.6F, -11.5F, 0, 8, 12, 0.0F, true));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		bone.render(f5);
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
		
		float progress = 0.0F;
		boolean isTilting = false;
		if (entityIn instanceof com.windanesz.tracesofthefallen.entity.EntitySidhe) {
			com.windanesz.tracesofthefallen.entity.EntitySidhe sidhe = (com.windanesz.tracesofthefallen.entity.EntitySidhe) entityIn;
			float partialTicks = net.minecraft.client.Minecraft.getMinecraft().getRenderPartialTicks();
			progress = sidhe.prevStandProgress + (sidhe.standProgress - sidhe.prevStandProgress) * partialTicks;
			isTilting = sidhe.isTilting();
		}

		// Base rotations (all 0) to Standing rotations
		this.bone.rotateAngleX = interpolate(0.0F, -1.5708F, progress);
		this.bone.rotateAngleY = 0.0F;
		this.bone.rotateAngleZ = 0.0F;

		this.head.rotateAngleX = interpolate(0.0F, 1.5708F, progress);
		// Head Y and Z are handled below based on progress

		this.front_left_leg.rotateAngleX = interpolate(0.0F, 1.2217F, progress);
		this.front_left_leg.rotateAngleY = 0.0F;
		this.front_left_leg.rotateAngleZ = interpolate(0.0F, -0.9163F, progress);

		this.front_right_leg.rotateAngleX = interpolate(0.0F, 1.5244F, progress);
		this.front_right_leg.rotateAngleY = interpolate(0.0F, -0.3487F, progress);
		this.front_right_leg.rotateAngleZ = interpolate(0.0F, 0.0159F, progress);

		this.back_left_leg.rotateAngleX = interpolate(0.0F, 1.5708F, progress);
		this.back_left_leg.rotateAngleY = 0.0F;
		this.back_left_leg.rotateAngleZ = 0.0F;
		
		this.back_right_leg.rotateAngleX = interpolate(0.0F, 1.5708F, progress);
		this.back_right_leg.rotateAngleY = 0.0F;
		this.back_right_leg.rotateAngleZ = 0.0F;
		
		// Mid-air spread pose during the jump
		if (!entityIn.onGround && progress > 0.8F) {
			// Symmetrical reverse V spread pose
			this.front_left_leg.rotateAngleX = 1.2F;
			this.front_left_leg.rotateAngleY = 0.6F;
			this.front_left_leg.rotateAngleZ = 0.0F;
			
			this.front_right_leg.rotateAngleX = 1.2F;
			this.front_right_leg.rotateAngleY = -0.6F;
			this.front_right_leg.rotateAngleZ = 0.0F;
			
			this.back_left_leg.rotateAngleX = 1.9F;
			this.back_left_leg.rotateAngleY = 0.6F;
			this.back_left_leg.rotateAngleZ = 0.0F;
			
			this.back_right_leg.rotateAngleX = 1.9F;
			this.back_right_leg.rotateAngleY = -0.6F;
			this.back_right_leg.rotateAngleZ = 0.0F;
		}
		
		// Slashing animation
		if (this.swingProgress > 0.0F) {
			float swingX = net.minecraft.util.math.MathHelper.sin(net.minecraft.util.math.MathHelper.sqrt(this.swingProgress) * (float)Math.PI);
			this.front_right_leg.rotateAngleX -= swingX * 2.0F;
			this.front_right_leg.rotateAngleZ += net.minecraft.util.math.MathHelper.sin(this.swingProgress * (float)Math.PI) * 0.4F;
		}

		// Walking animations for all limbs (handles both quadruped and bipedal smoothly)
		float frontSwingAmp = interpolate(1.4F, 1.0F, progress);
		this.front_left_leg.rotateAngleX += net.minecraft.util.math.MathHelper.cos(limbSwing * 0.6662F) * frontSwingAmp * limbSwingAmount;
		this.front_right_leg.rotateAngleX += net.minecraft.util.math.MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * frontSwingAmp * limbSwingAmount;
		
		this.back_left_leg.rotateAngleX += net.minecraft.util.math.MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
		this.back_right_leg.rotateAngleX += net.minecraft.util.math.MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		
		// Head tracking and tilting
		float yawAngle = netHeadYaw * 0.017453292F;
		float tiltAngle = isTilting ? 0.5F : 0.0F;
		
		// When on all fours (progress = 0): Y axis is Yaw, Z axis is Tilt
		// When standing (progress = 1): Z axis is Yaw, Y axis is Tilt (due to the -90 degree body pitch)
		this.head.rotateAngleY = interpolate(yawAngle, tiltAngle, progress);
		this.head.rotateAngleZ = interpolate(tiltAngle, yawAngle, progress);
		this.head.rotateAngleX += headPitch * 0.017453292F;
	}

	private float interpolate(float start, float end, float progress) {
		return start + (end - start) * progress;
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}