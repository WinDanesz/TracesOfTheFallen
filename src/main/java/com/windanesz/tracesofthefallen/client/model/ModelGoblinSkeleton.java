package com.windanesz.tracesofthefallen.client.model;

import com.windanesz.tracesofthefallen.entity.EntityGoblinSkeleton;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;


public class ModelGoblinSkeleton extends ModelBase {
	private final ModelRenderer body;
	private final ModelRenderer head;
	private final ModelRenderer cube_r1;
	private final ModelRenderer cube_r2;
	private final ModelRenderer leg_left;
	private final ModelRenderer leg_right;
	private final ModelRenderer arm_left;
	private final ModelRenderer arm_right;

	public ModelGoblinSkeleton() {
		textureWidth = 48;
		textureHeight = 48;

		body = new ModelRenderer(this);
		body.setRotationPoint(0.0F, 24.0F, 0.0F);
		body.cubeList.add(new ModelBox(body, 30, 0, -3.0F, -11.0F, -2.0F, 6, 6, 3, 0.0F, false));

		head = new ModelRenderer(this);
		head.setRotationPoint(0.0F, -11.0F, -0.5F);
		body.addChild(head);
		head.cubeList.add(new ModelBox(head, 0, 0, -3.0F, -6.0F, -3.0F, 6, 6, 6, 0.0F, false));

		cube_r1 = new ModelRenderer(this);
		cube_r1.setRotationPoint(3.0F, -4.0F, -2.0F);
		head.addChild(cube_r1);
		setRotationAngle(cube_r1, 0.0F, 0.3927F, 0.0F);
		cube_r1.cubeList.add(new ModelBox(cube_r1, 18, -6, 0.0F, -2.0F, 0.0F, 0, 4, 6, 0.0F, true));

		cube_r2 = new ModelRenderer(this);
		cube_r2.setRotationPoint(-3.0F, -4.0F, -2.0F);
		head.addChild(cube_r2);
		setRotationAngle(cube_r2, 0.0F, -0.3927F, 0.0F);
		cube_r2.cubeList.add(new ModelBox(cube_r2, 18, -6, 0.0F, -2.0F, 0.0F, 0, 4, 6, 0.0F, false));

		leg_left = new ModelRenderer(this);
		leg_left.setRotationPoint(2.0F, -5.0F, 0.0F);
		body.addChild(leg_left);
		leg_left.cubeList.add(new ModelBox(leg_left, 32, 32, -1.0F, 0.01F, -1.0F, 2, 5, 2, 0.0F, false));

		leg_right = new ModelRenderer(this);
		leg_right.setRotationPoint(-2.0F, -5.0F, 0.0F);
		body.addChild(leg_right);
		leg_right.cubeList.add(new ModelBox(leg_right, 32, 32, -1.0F, 0.01F, -1.0F, 2, 5, 2, 0.0F, true));

		arm_left = new ModelRenderer(this);
		arm_left.setRotationPoint(4.0F, -10.0F, 0.0F);
		body.addChild(arm_left);
		arm_left.cubeList.add(new ModelBox(arm_left, 1, 13, -1.0F, -1.0F, -1.0F, 2, 6, 2, 0.0F, false));

		arm_right = new ModelRenderer(this);
		arm_right.setRotationPoint(-4.0F, -10.0F, 0.0F);
		body.addChild(arm_right);
		arm_right.cubeList.add(new ModelBox(arm_right, 2, 13, -1.0F, -1.0F, -1.0F, 2, 6, 2, 0.0F, true));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		this.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		GlStateManager.pushMatrix();
		if (entity != null && entity.ticksExisted <= 60) {
			float emergeProgress = (float) entity.ticksExisted / 60.0F;
			float yOffset = (1.0F - emergeProgress) * 1.4F;
			float zOffset = MathHelper.sin(emergeProgress * (float)Math.PI) * 0.15F;
			GlStateManager.translate(0.0F, yOffset, zOffset);
		} else if (entity instanceof EntityGoblinSkeleton && ((EntityGoblinSkeleton) entity).getLungeState() == 2) {
			int ticks = ((EntityGoblinSkeleton) entity).lungeTicks;
			float pitchRotate, yOffset, zOffset;
			if (ticks <= 8) {
				float fallProgress = Math.min(1.0F, (float) ticks / 8.0F);
				float fallEase = fallProgress * fallProgress;
				pitchRotate = 55.0F + 35.0F * fallEase;
				yOffset = 1.2875F * fallEase;
				zOffset = -0.5F * fallEase;
			} else if (ticks <= 15) {
				pitchRotate = 90.0F;
				yOffset = 1.2875F;
				zOffset = -0.5F;
			} else {
				float getUpProgress = Math.min(1.0F, (float) (ticks - 15) / 15.0F);
				pitchRotate = 90.0F * (1.0F - getUpProgress);
				yOffset = 1.2875F * (1.0F - getUpProgress);
				zOffset = -0.5F * (1.0F - getUpProgress);
			}
			GlStateManager.translate(0.0F, yOffset, zOffset);
			GlStateManager.rotate(pitchRotate, 1.0F, 0.0F, 0.0F);
		} else if (entity instanceof EntityGoblinSkeleton && ((EntityGoblinSkeleton) entity).getLungeState() == 1) {
			GlStateManager.rotate(55.0F, 1.0F, 0.0F, 0.0F);
		}
		body.render(f5);
		GlStateManager.popMatrix();
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
		this.head.rotateAngleY = netHeadYaw * 0.017453292F;
		this.head.rotateAngleX = headPitch * 0.017453292F;
		this.body.rotateAngleX = 0.0F;
		this.body.rotateAngleY = 0.0F;
		this.body.rotateAngleZ = 0.0F;
		this.leg_right.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.leg_left.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
		this.leg_right.rotateAngleY = 0.0F;
		this.leg_left.rotateAngleY = 0.0F;
		this.arm_right.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.2F * limbSwingAmount;
		this.arm_left.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.2F * limbSwingAmount;
		this.arm_right.rotationPointY = -10.0F;
		this.arm_left.rotationPointY = -10.0F;
		if (entityIn != null && entityIn.ticksExisted <= 60) {
			this.leg_right.rotateAngleX = 0.0F;
			this.leg_left.rotateAngleX = 0.0F;
			float emergeProgress = (float) entityIn.ticksExisted / 60.0F;
			if (emergeProgress < 0.2F) {
				float phase = emergeProgress / 0.2F;
				float armY = -14.0F + (-5.0F * MathHelper.sin(phase * (float)Math.PI / 2.0F));
				this.arm_right.rotationPointY = armY;
				this.arm_left.rotationPointY = armY;
				this.body.rotateAngleX = 0.25F * phase;
				this.head.rotateAngleX = -0.2F * phase;
				this.arm_right.rotateAngleX = -((float)Math.PI);
				this.arm_left.rotateAngleX = -((float)Math.PI);
				this.arm_right.rotateAngleZ = -0.15F;
				this.arm_left.rotateAngleZ = 0.15F;
			} else if (emergeProgress < 0.4F) {
				float phase = (emergeProgress - 0.2F) / 0.2F;
				float armY = -19.0F + (9.0F * MathHelper.sin(phase * (float)Math.PI / 2.0F));
				this.arm_right.rotationPointY = armY;
				this.arm_left.rotationPointY = armY;
				this.body.rotateAngleX = 0.25F + (0.25F * phase);
				this.head.rotateAngleX = -0.2F - (0.2F * phase);
				float armReach = -((float)Math.PI) + (0.785F * phase);
				this.arm_right.rotateAngleX = armReach;
				this.arm_left.rotateAngleX = armReach;
				this.arm_right.rotateAngleZ = -0.15F * (1.0F - phase);
				this.arm_left.rotateAngleZ = 0.15F * (1.0F - phase);
			} else if (emergeProgress < 0.85F) {
				float phase = (emergeProgress - 0.4F) / 0.45F;
				this.body.rotateAngleX = 0.5F - (0.3F * MathHelper.sin(phase * (float)Math.PI / 2.0F));
				this.head.rotateAngleX = -0.4F * (1.0F - phase);
				float pushDownAngle = -2.356F + (1.4F * MathHelper.sin(phase * (float)Math.PI / 2.0F));
				this.arm_right.rotateAngleX = pushDownAngle;
				this.arm_left.rotateAngleX = pushDownAngle;
				this.arm_right.rotateAngleZ = 0.0F;
				this.arm_left.rotateAngleZ = 0.0F;
			} else {
				float phase = (emergeProgress - 0.85F) / 0.15F;
				float smoothFinish = MathHelper.sin(phase * (float)Math.PI / 2.0F);
				this.body.rotateAngleX = 0.2F * (1.0F - smoothFinish);
				this.head.rotateAngleX = headPitch * 0.017453292F * smoothFinish;
				float targetArmRight = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.2F * limbSwingAmount;
				float targetArmLeft = MathHelper.cos(limbSwing * 0.6662F) * 1.2F * limbSwingAmount;
				this.arm_right.rotateAngleX = -0.956F * (1.0F - smoothFinish) + targetArmRight * smoothFinish;
				this.arm_left.rotateAngleX = -0.956F * (1.0F - smoothFinish) + targetArmLeft * smoothFinish;
				this.arm_right.rotateAngleZ = 0.0F;
				this.arm_left.rotateAngleZ = 0.0F;
			}
		} else if (entityIn instanceof EntityGoblinSkeleton && ((EntityGoblinSkeleton) entityIn).getLungeState() == 1) {
			this.head.rotateAngleX = -0.6F;
			this.arm_right.rotateAngleX = -2.8F;
			this.arm_left.rotateAngleX = -2.8F;
			this.arm_right.rotateAngleZ = -0.5F;
			this.arm_left.rotateAngleZ = 0.5F;
			this.leg_right.rotateAngleX = 0.2F;
			this.leg_left.rotateAngleX = -0.2F;
		} else if (entityIn instanceof EntityGoblinSkeleton && ((EntityGoblinSkeleton) entityIn).getLungeState() == 2) {
			int ticks = ((EntityGoblinSkeleton) entityIn).lungeTicks;
			if (ticks <= 15) {
				this.head.rotateAngleX = -0.6F;
				this.arm_right.rotateAngleX = -2.8F;
				this.arm_left.rotateAngleX = -2.8F;
				this.arm_right.rotateAngleZ = -0.5F;
				this.arm_left.rotateAngleZ = 0.5F;
				this.leg_right.rotateAngleX = 0.1F;
				this.leg_left.rotateAngleX = -0.1F;
			} else {
				float getUpProgress = Math.min(1.0F, (float) (ticks - 15) / 15.0F);
				this.head.rotateAngleX = -0.6F * (1.0F - getUpProgress);
				this.arm_right.rotateAngleX = -2.8F * (1.0F - getUpProgress) + (-0.5F * getUpProgress);
				this.arm_left.rotateAngleX = -2.8F * (1.0F - getUpProgress) + (-0.5F * getUpProgress);
				this.arm_right.rotateAngleZ = -0.5F * (1.0F - getUpProgress);
				this.arm_left.rotateAngleZ = 0.5F * (1.0F - getUpProgress);
			}
		} else if (entityIn instanceof EntityLivingBase && ((EntityLivingBase)entityIn).isSwingInProgress) {
			float f = MathHelper.sin(this.swingProgress * (float)Math.PI);
			float f1 = MathHelper.sin((1.0F - (1.0F - this.swingProgress) * (1.0F - this.swingProgress)) * (float)Math.PI);
			this.arm_right.rotateAngleZ = 0.0F;
			this.arm_right.rotateAngleY = -(0.1F - f * 0.6F);
			this.arm_right.rotateAngleX = -((float)Math.PI / 2F) + f * 1.2F - f1 * 0.4F;
		} else {
			this.arm_right.rotateAngleY = 0.0F;
			this.arm_right.rotateAngleZ = 0.0F;
		}
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}