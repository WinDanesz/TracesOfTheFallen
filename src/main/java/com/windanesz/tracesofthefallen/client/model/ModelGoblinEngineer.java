package com.windanesz.tracesofthefallen.client.model;

import com.windanesz.tracesofthefallen.entity.EntityGoblin;
import com.windanesz.tracesofthefallen.entity.EntityGoblinEngineer;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelGoblinEngineer extends ModelGoblinBase {
	private final ModelRenderer ear_left;
	private final ModelRenderer ear_left_r1;
	private final ModelRenderer ear_right;
	private final ModelRenderer ear_right_r1;

	public ModelGoblinEngineer() {
		textureWidth = 48;
		textureHeight = 48;

		body = new ModelRenderer(this);
		body.setRotationPoint(0.0F, 25.0F, 0.0F);
		body.cubeList.add(new ModelBox(body, 27, 12, -3.0F, -17.0F, -2.0F, 6, 9, 3, 0.0F, false));

		head = new ModelRenderer(this);
		head.setRotationPoint(0.0F, -17.0F, -0.5F);
		body.addChild(head);
		head.cubeList.add(new ModelBox(head, 0, 0, -3.0F, -6.0F, -3.0F, 6, 6, 6, 0.0F, false));
		head.cubeList.add(new ModelBox(head, 23, 27, -0.9503F, -3.0F, -4.9929F, 2, 3, 2, 0.0F, false));

		ear_left = new ModelRenderer(this);
		ear_left.setRotationPoint(3.0497F, -3.0F, -1.9929F);
		head.addChild(ear_left);

		ear_left_r1 = new ModelRenderer(this);
		ear_left_r1.setRotationPoint(0.0F, -1.0F, 0.0F);
		ear_left.addChild(ear_left_r1);
		setRotationAngle(ear_left_r1, 0.4369F, 0.4407F, 0.1966F);
		ear_left_r1.cubeList.add(new ModelBox(ear_left_r1, 18, -7, 0.0F, -1.0F, 0.0F, 0, 4, 7, 0.0F, false));

		ear_right = new ModelRenderer(this);
		ear_right.setRotationPoint(-2.9503F, -3.0F, -1.9929F);
		head.addChild(ear_right);

		ear_right_r1 = new ModelRenderer(this);
		ear_right_r1.setRotationPoint(0.0F, -1.0F, 0.0F);
		ear_right.addChild(ear_right_r1);
		setRotationAngle(ear_right_r1, 0.4369F, -0.4407F, -0.1966F);
		ear_right_r1.cubeList.add(new ModelBox(ear_right_r1, 18, -7, 0.0F, -1.0F, 0.0F, 0, 4, 7, 0.0F, true));

		leg_left = new ModelRenderer(this);
		leg_left.setRotationPoint(1.5F, -8.0F, -0.5F);
		body.addChild(leg_left);
		leg_left.cubeList.add(new ModelBox(leg_left, 0, 23, -1.5F, 0.0F, -1.5F, 3, 7, 3, 0.0F, false));

		leg_right = new ModelRenderer(this);
		leg_right.setRotationPoint(-1.5F, -8.0F, -0.5F);
		body.addChild(leg_right);
		leg_right.cubeList.add(new ModelBox(leg_right, 0, 23, -1.5F, 0.0F, -1.5F, 3, 7, 3, 0.0F, true));

		arm_left = new ModelRenderer(this);
		arm_left.setRotationPoint(3.0F, -16.0F, -0.5F);
		body.addChild(arm_left);
		arm_left.cubeList.add(new ModelBox(arm_left, 0, 12, 0.0F, -1.0F, -1.5F, 3, 8, 3, 0.0F, false));
		arm_left.cubeList.add(new ModelBox(arm_left, 33, -5, 3.0F, -2.0F, -0.5F, 0, 3, 5, 0.0F, false));

		arm_right = new ModelRenderer(this);
		arm_right.setRotationPoint(-3.0F, -16.0F, -0.5F);
		body.addChild(arm_right);
		arm_right.cubeList.add(new ModelBox(arm_right, 0, 12, -3.0F, -1.0F, -1.5F, 3, 8, 3, 0.0F, true));
		arm_right.cubeList.add(new ModelBox(arm_right, 33, -5, -2.999F, -2.0F, -0.5F, 0, 3, 5, 0.0F, true));

		initBiped();
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);

		if (entityIn instanceof EntityGoblin && ((EntityGoblin) entityIn).isCarryingBomb()) {
			if (this.arm_right != null) {
				this.arm_right.rotateAngleX = 0.6F;
				this.arm_right.rotateAngleY = 0.0F;
				this.arm_right.rotateAngleZ = 0.1F;
			}
			if (this.arm_left != null) {
				this.arm_left.rotateAngleX = 0.6F;
				this.arm_left.rotateAngleY = 0.0F;
				this.arm_left.rotateAngleZ = -0.1F;
			}
		}

		if (entityIn instanceof EntityGoblinEngineer && ((EntityGoblinEngineer) entityIn).isPointing()) {
			if (this.arm_right != null) {
				this.arm_right.rotateAngleX = -((float) Math.PI / 2F);
				this.arm_right.rotateAngleY = 0.0F;
				this.arm_right.rotateAngleZ = 0.0F;
			}
		}
	}
}
