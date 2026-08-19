package com.windanesz.tracesofthefallen.client.model;

import com.windanesz.tracesofthefallen.entity.EntitySubterfuge;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelSubterFuge2 extends ModelBase {
	private final ModelRenderer body;
    private final ModelRenderer head;
    private final ModelRenderer rightArm;
	private final ModelRenderer rightForearm;

    public ModelSubterFuge2() {
		textureWidth = 64;
		textureHeight = 64;

		body = new ModelRenderer(this);
		body.setRotationPoint(0.0F, 24.0F, 0.0F);


        ModelRenderer torso = new ModelRenderer(this);
		torso.setRotationPoint(0.0F, -14.0F, 2.0F);
		body.addChild(torso);
		torso.cubeList.add(new ModelBox(torso, 8, 16, -4.0F, -11.0F, -4.0F, 8, 11, 4, 0.0F, false));

        ModelRenderer chest_r1 = new ModelRenderer(this);
		chest_r1.setRotationPoint(0.145F, -6.2824F, -4.2071F);
		torso.addChild(chest_r1);
		setRotationAngle(chest_r1, 0.0F, -0.7854F, 1.5708F);
		chest_r1.cubeList.add(new ModelBox(chest_r1, 31, 53, -2.15F, -3.4F, -0.5F, 3, 8, 4, 0.0F, false));

		head = new ModelRenderer(this);
		head.setRotationPoint(0.0F, -11.0F, -3.0F);
		torso.addChild(head);


        ModelRenderer mask_right_r1 = new ModelRenderer(this);
		mask_right_r1.setRotationPoint(0.0263F, -1.0F, 1.0F);
		head.addChild(mask_right_r1);
		setRotationAngle(mask_right_r1, 0.0F, 0.3927F, 0.0F);
		mask_right_r1.cubeList.add(new ModelBox(mask_right_r1, 32, 42, -3.6243F, -8.0F, -1.0101F, 4, 8, 0, 0.0F, true));

        ModelRenderer mask_left_r1 = new ModelRenderer(this);
		mask_left_r1.setRotationPoint(-0.0263F, -1.0F, 1.0F);
		head.addChild(mask_left_r1);
		setRotationAngle(mask_left_r1, 0.0F, -0.3927F, 0.0F);
		mask_left_r1.cubeList.add(new ModelBox(mask_left_r1, 44, 42, -0.4F, -8.0F, -1.0F, 4, 8, 0, 0.0F, false));

		rightArm = new ModelRenderer(this);
		rightArm.setRotationPoint(5.0F, -10.0F, -2.0F);
		torso.addChild(rightArm);
		setRotationAngle(rightArm, -0.3655F, 0.147F, 0.3655F);
		rightArm.cubeList.add(new ModelBox(rightArm, 19, 35, -1.0F, -1.0F, -1.0F, 3, 5, 3, 0.0F, true));

		rightForearm = new ModelRenderer(this);
		rightForearm.setRotationPoint(2.0F, 4.0F, 2.0F);
		rightArm.addChild(rightForearm);
		rightForearm.cubeList.add(new ModelBox(rightForearm, 19, 45, -3.0F, 0.0F, -3.0F, 3, 7, 3, 0.01F, true));

        ModelRenderer leftArm = new ModelRenderer(this);
		leftArm.setRotationPoint(-5.0F, -10.0F, -2.0F);
		torso.addChild(leftArm);
		setRotationAngle(leftArm, -0.3655F, -0.147F, -0.3655F);
		leftArm.cubeList.add(new ModelBox(leftArm, 19, 35, -2.0F, -1.0F, -1.0F, 3, 5, 3, 0.0F, false));

        ModelRenderer leftForearm = new ModelRenderer(this);
		leftForearm.setRotationPoint(-2.0F, 4.0F, 2.0F);
		leftArm.addChild(leftForearm);
		leftForearm.cubeList.add(new ModelBox(leftForearm, 19, 45, 0.0F, 0.0F, -3.0F, 3, 7, 3, 0.01F, false));

        ModelRenderer leftLeg = new ModelRenderer(this);
		leftLeg.setRotationPoint(-1.9F, -14.0F, 0.0F);
		body.addChild(leftLeg);
		leftLeg.cubeList.add(new ModelBox(leftLeg, 42, 20, -2.1F, 0.0F, -2.0F, 4, 7, 4, 0.0F, false));

        ModelRenderer leftPuff_r1 = new ModelRenderer(this);
		leftPuff_r1.setRotationPoint(-1.847F, 4.1746F, 0.0F);
		leftLeg.addChild(leftPuff_r1);
		setRotationAngle(leftPuff_r1, 0.0F, 0.0F, -0.3927F);
		leftPuff_r1.cubeList.add(new ModelBox(leftPuff_r1, 42, 20, -1.0F, -3.0F, -2.0F, 2, 5, 4, -0.01F, false));

        ModelRenderer leftKnee = new ModelRenderer(this);
		leftKnee.setRotationPoint(0.0F, 7.0F, -1.5F);
		leftLeg.addChild(leftKnee);
		leftKnee.cubeList.add(new ModelBox(leftKnee, 50, 10, -1.6F, 0.0F, 0.0F, 3, 7, 3, 0.0F, false));

        ModelRenderer rightLeg = new ModelRenderer(this);
		rightLeg.setRotationPoint(1.9F, -14.0F, 0.0F);
		body.addChild(rightLeg);
		rightLeg.cubeList.add(new ModelBox(rightLeg, 42, 20, -1.9F, 0.0F, -2.0F, 4, 7, 4, 0.0F, true));

        ModelRenderer rightPuff_r1 = new ModelRenderer(this);
		rightPuff_r1.setRotationPoint(1.847F, 4.1746F, 0.0F);
		rightLeg.addChild(rightPuff_r1);
		setRotationAngle(rightPuff_r1, 0.0F, 0.0F, 0.3927F);
		rightPuff_r1.cubeList.add(new ModelBox(rightPuff_r1, 42, 20, -1.0F, -3.0F, -2.0F, 2, 5, 4, -0.01F, true));

        ModelRenderer rightKnee = new ModelRenderer(this);
		rightKnee.setRotationPoint(0.0F, 7.0F, -1.5F);
		rightLeg.addChild(rightKnee);
		rightKnee.cubeList.add(new ModelBox(rightKnee, 50, 10, -1.4F, 0.0F, 0.0F, 3, 7, 3, 0.0F, true));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		if (entity instanceof EntitySubterfuge && ((EntitySubterfuge) entity).isHiddenState()) {
			return; // Do not render if hidden
		}
		setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		body.render(f5);
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);

		// Reset head
		head.rotateAngleX = 0.0F;
		head.rotateAngleY = 0.0F;
		head.rotateAngleZ = 0.0F;

		// Slow, subtle head tracking
		head.rotateAngleY = (netHeadYaw * 0.017453292F) * 0.25F;
		head.rotateAngleX = (headPitch * 0.017453292F) * 0.25F;

		// Default: arms use Blockbench pose (set in constructor)
		if (entityIn instanceof EntitySubterfuge) {
			EntitySubterfuge subterfuge = (EntitySubterfuge) entityIn;

			// Facial expressions
			int expression = subterfuge.getExpressionState();
			if (expression == 1) { // Welcome — slight upward tilt
				head.rotateAngleX -= 0.1F;
			} else if (expression == 2) { // Approval — slow nod
				head.rotateAngleX += MathHelper.sin(ageInTicks * 0.25F) * 0.15F;
			} else if (expression == 3) { // Disapproval — side tilt
				head.rotateAngleZ = 0.15F;
			} else if (expression == 4) { // Angry — slow head shake
				head.rotateAngleY += MathHelper.sin(ageInTicks * 0.8F) * 0.2F;
			}

			// Godslap: raise right arm in a dismissing gesture
			if (subterfuge.isAttackingState()) {
				rightArm.rotateAngleX = -((float) Math.PI / 2F); // Straight forward/up
				rightArm.rotateAngleY = 0.1F;
				rightArm.rotateAngleZ = 0.0F;
				rightForearm.rotateAngleX = 0.0F;
				rightForearm.rotateAngleY = 0.0F;
				rightForearm.rotateAngleZ = 0.0F;
			} else {
				rightArm.rotateAngleX = -0.3655F;
				rightArm.rotateAngleY = 0.147F;
				rightArm.rotateAngleZ = 0.3655F;
				rightForearm.rotateAngleX = 0.0F;
				rightForearm.rotateAngleY = 0.0F;
				rightForearm.rotateAngleZ = 0.0F;
			}
		}
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}