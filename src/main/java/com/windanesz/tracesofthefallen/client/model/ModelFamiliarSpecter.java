package com.windanesz.tracesofthefallen.client.model;

import com.windanesz.tracesofthefallen.entity.EntitySpecter;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelFamiliarSpecter extends ModelBase {

	private final ModelRenderer head;
	private final ModelRenderer headwear;
	private final ModelRenderer body;
	private final ModelRenderer cube_r1;
	private final ModelRenderer cube_r2;
	private final ModelRenderer left_arm;
	private final ModelRenderer right_arm;
	private final ModelRenderer legs;

	public ModelFamiliarSpecter() {
		textureWidth = 64;
		textureHeight = 32;

		head = new ModelRenderer(this);
		head.setRotationPoint(0.0F, 0.0F, 0.0F);
		head.cubeList.add(new ModelBox(head, 0, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F, false));

		headwear = new ModelRenderer(this);
		headwear.setRotationPoint(0.0F, 0.0F, 0.0F);
		headwear.cubeList.add(new ModelBox(headwear, 32, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, 0.5F, false));

		body = new ModelRenderer(this);
		body.setRotationPoint(0.0F, 0.0F, 0.0F);
		body.cubeList.add(new ModelBox(body, 16, 16, -4.0F, 0.0F, -2.0F, 8, 10, 4, 0.0F, false));

		cube_r1 = new ModelRenderer(this);
		cube_r1.setRotationPoint(0.0F, 0.0F, 2.0F);
		body.addChild(cube_r1);
		setRotationAngle(cube_r1, 3.0107F, 0.0F, 3.1416F);
		cube_r1.cubeList.add(new ModelBox(cube_r1, 24, 0, -4.0F, 0.0F, 0.0F, 8, 8, 0, 0.0F, false));

		cube_r2 = new ModelRenderer(this);
		cube_r2.setRotationPoint(0.0F, 0.0F, -2.0F);
		body.addChild(cube_r2);
		setRotationAngle(cube_r2, -0.1309F, 0.0F, 0.0F);
		cube_r2.cubeList.add(new ModelBox(cube_r2, 24, 0, -4.0F, 0.0F, 0.0F, 8, 8, 0, 0.0F, false));

		left_arm = new ModelRenderer(this);
		left_arm.setRotationPoint(5.0F, 2.0F, 0.0F);
		left_arm.cubeList.add(new ModelBox(left_arm, 0, 16, -1.0F, -2.0F, -1.0F, 2, 10, 2, 0.0F, true));

		right_arm = new ModelRenderer(this);
		right_arm.setRotationPoint(-5.0F, 2.0F, 0.0F);
		right_arm.cubeList.add(new ModelBox(right_arm, 0, 16, -1.0F, -2.0F, -1.0F, 2, 10, 2, 0.0F, false));

		legs = new ModelRenderer(this);
		legs.setRotationPoint(0.0F, 9.9538F, 2.0809F);
		setRotationAngle(legs, 0.3927F, 0.0F, 0.0F);
		legs.cubeList.add(new ModelBox(legs, 40, 16, -4.0F, 0.0117F, -4.0924F, 8, 8, 4, 0.0F, false));
	}

	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		head.render(scale);
		headwear.render(scale);
		body.render(scale);
		left_arm.render(scale);
		right_arm.render(scale);
		legs.render(scale);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);

		this.head.rotateAngleX = headPitch * 0.017453292F;
		this.head.rotateAngleY = netHeadYaw * 0.017453292F;

		this.right_arm.rotateAngleZ = 0.0F;
		this.left_arm.rotateAngleZ = 0.0F;

		if (entityIn instanceof EntitySpecter && ((EntitySpecter) entityIn).isAttacking()) {
			float angle = -1.5707964F; // -90 degrees
			this.right_arm.rotateAngleX = angle;
			this.left_arm.rotateAngleX = angle;
			this.right_arm.rotateAngleX += MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
			this.left_arm.rotateAngleX -= MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
			this.right_arm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
			this.left_arm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
		} else {
			this.right_arm.rotateAngleX = 0.0F;
			this.left_arm.rotateAngleX = 0.0F;
		}

		if (this.swingProgress > 0.0F) {
			this.right_arm.rotateAngleX = this.right_arm.rotateAngleX * 0.5F - ((float) Math.PI / 10F) * this.swingProgress;
			this.left_arm.rotateAngleX = this.left_arm.rotateAngleX * 0.5F - ((float) Math.PI / 10F) * this.swingProgress;
		}
	}
}