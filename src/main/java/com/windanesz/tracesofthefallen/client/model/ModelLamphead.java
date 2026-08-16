package com.windanesz.tracesofthefallen.client.model;

import com.windanesz.tracesofthefallen.entity.EntityLamphead;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelLamphead extends ModelBase {
	private final ModelRenderer base;
	private final ModelRenderer body;
	private final ModelRenderer cube_r1;
	private final ModelRenderer windup_key_pivot;
	private final ModelRenderer windup_key;
	private final ModelRenderer neck;
	private final ModelRenderer head;
	private final ModelRenderer cube_r2;
	private final ModelRenderer cube_r3;
	private final ModelRenderer cube_r4;
	private final ModelRenderer cube_r5;
	private final ModelRenderer cube_r6;
	private final ModelRenderer arm_left;
	private final ModelRenderer arm_right;
	private final ModelRenderer leg_left;
	private final ModelRenderer cube_r7;
	private final ModelRenderer leg_right;
	private final ModelRenderer cube_r8;

	public ModelLamphead() {
		textureWidth = 64;
		textureHeight = 64;

		base = new ModelRenderer(this);
		base.setRotationPoint(0.0F, 12.0F, 0.0F);

		body = new ModelRenderer(this);
		body.setRotationPoint(0.0F, 0.0F, 0.0F);
		base.addChild(body);

		cube_r1 = new ModelRenderer(this);
		cube_r1.setRotationPoint(0.0F, -1.0F, -3.0F);
		body.addChild(cube_r1);
		setRotationAngle(cube_r1, -0.7854F, 0.0F, 0.0F);
		cube_r1.cubeList.add(new ModelBox(cube_r1, 0, 22, -4.0F, -6.0F, -5.0F, 8, 6, 10, 0.0F, false));

		windup_key_pivot = new ModelRenderer(this);
		windup_key_pivot.setRotationPoint(0.0F, -4.5355F, 4.7782F);
		setRotationAngle(windup_key_pivot, -0.7854F, 0.0F, 0.0F);
		body.addChild(windup_key_pivot);

		windup_key = new ModelRenderer(this);
		windup_key.setRotationPoint(0.0F, 0.0F, 0.0F);
		windup_key.cubeList.add(new ModelBox(windup_key, 43, 5, -3.0F, -2.0F, -0.001F, 6, 4, 0, 0.0F, false));
		windup_key_pivot.addChild(windup_key);

		neck = new ModelRenderer(this);
		neck.setRotationPoint(0.0F, -7.8478F, -2.2346F);
		body.addChild(neck);
		setRotationAngle(neck, -0.3927F, 0.0F, 0.0F);
		neck.cubeList.add(new ModelBox(neck, 0, 9, -1.0F, -11.0F, -1.0F, 2, 12, 2, 0.0F, false));

		head = new ModelRenderer(this);
		head.setRotationPoint(0.0F, -11.5239F, -0.3827F);
		neck.addChild(head);
		setRotationAngle(head, 0.3927F, 0.0F, 0.0F);
		head.cubeList.add(new ModelBox(head, 32, 16, -4.0F, -6.0F, -4.0F, 8, 7, 8, 0.0F, false));
		head.cubeList.add(new ModelBox(head, 0, 0, -2.0F, -10.0F, -2.0F, 4, 2, 4, 0.0F, false));
		head.cubeList.add(new ModelBox(head, 12, 0, -5.0F, -8.0F, -5.0F, 10, 2, 10, 0.0F, false));

		cube_r2 = new ModelRenderer(this);
		cube_r2.setRotationPoint(1.1522F, -4.2346F, 0.0F);
		head.addChild(cube_r2);
		setRotationAngle(cube_r2, 0.0F, 0.0F, -1.5708F);
		cube_r2.cubeList.add(new ModelBox(cube_r2, 36, 30, -3.2346F, -5.1522F, -4.0F, 5, 8, 8, -0.1F, false));

		cube_r3 = new ModelRenderer(this);
		cube_r3.setRotationPoint(-5.0F, -7.0F, 0.0F);
		head.addChild(cube_r3);
		setRotationAngle(cube_r3, 0.0F, 1.5708F, 0.7854F);
		cube_r3.cubeList.add(new ModelBox(cube_r3, -3, 6, -3.0F, 0.0F, -3.0F, 6, 0, 3, 0.0F, true));

		cube_r4 = new ModelRenderer(this);
		cube_r4.setRotationPoint(5.0F, -7.0F, 0.0F);
		head.addChild(cube_r4);
		setRotationAngle(cube_r4, 0.0F, -1.5708F, -0.7854F);
		cube_r4.cubeList.add(new ModelBox(cube_r4, -3, 6, -3.0F, 0.0F, -3.0F, 6, 0, 3, 0.0F, true));

		cube_r5 = new ModelRenderer(this);
		cube_r5.setRotationPoint(0.0F, -6.9993F, 5.0007F);
		head.addChild(cube_r5);
		setRotationAngle(cube_r5, 2.3562F, 0.0F, -3.1416F);
		cube_r5.cubeList.add(new ModelBox(cube_r5, -3, 6, -3.0F, 0.0F, -3.0F, 6, 0, 3, 0.0F, true));

		cube_r6 = new ModelRenderer(this);
		cube_r6.setRotationPoint(0.0F, -7.0F, -5.0F);
		head.addChild(cube_r6);
		setRotationAngle(cube_r6, -0.7854F, 0.0F, 0.0F);
		cube_r6.cubeList.add(new ModelBox(cube_r6, -3, 6, -3.0F, 0.0F, -3.0F, 6, 0, 3, 0.0F, true));

		arm_left = new ModelRenderer(this);
		arm_left.setRotationPoint(5.0F, -5.2894F, -3.6927F);
		body.addChild(arm_left);
		arm_left.cubeList.add(new ModelBox(arm_left, 36, 47, -1.0F, -0.5F, -1.0F, 2, 13, 2, 0.0F, false));

		arm_right = new ModelRenderer(this);
		arm_right.setRotationPoint(-5.0F, -5.2894F, -3.6927F);
		body.addChild(arm_right);
		arm_right.cubeList.add(new ModelBox(arm_right, 36, 47, -1.0F, -0.5F, -1.0F, 2, 13, 2, 0.0F, true));

		leg_left = new ModelRenderer(this);
		leg_left.setRotationPoint(2.0F, 0.0F, 2.0F);
		base.addChild(leg_left);
		leg_left.cubeList.add(new ModelBox(leg_left, 0, 47, 0.0F, 2.0F, 0.0F, 3, 10, 3, 0.0F, false));

		cube_r7 = new ModelRenderer(this);
		cube_r7.setRotationPoint(0.0F, -2.0F, -4.0F);
		leg_left.addChild(cube_r7);
		setRotationAngle(cube_r7, 0.3927F, 0.0F, 0.0F);
		cube_r7.cubeList.add(new ModelBox(cube_r7, 12, 43, -1.0F, 0.0F, 0.0F, 5, 7, 5, 0.0F, false));

		leg_right = new ModelRenderer(this);
		leg_right.setRotationPoint(-2.0F, 0.0F, 2.0F);
		base.addChild(leg_right);
		leg_right.cubeList.add(new ModelBox(leg_right, 0, 47, -3.0F, 2.0F, 0.0F, 3, 10, 3, 0.0F, true));

		cube_r8 = new ModelRenderer(this);
		cube_r8.setRotationPoint(0.0F, -2.0F, -4.0F);
		leg_right.addChild(cube_r8);
		setRotationAngle(cube_r8, 0.3927F, 0.0F, 0.0F);
		cube_r8.cubeList.add(new ModelBox(cube_r8, 12, 43, -4.0F, 0.0F, 0.0F, 5, 7, 5, 0.0F, true));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		this.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		base.render(f5);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);

		// Head rotations
		this.head.rotateAngleY = netHeadYaw * 0.017453292F;
		this.head.rotateAngleX = headPitch * 0.017453292F + 0.3927F; // 0.3927F is base rotation

		this.body.rotateAngleX = 0.0F;
		this.neck.rotateAngleX = -0.3927F;

		float slam = 0.0F;
		float squeeze = 0.0F;
		if (entityIn instanceof EntityLamphead) {
			EntityLamphead lamphead = (EntityLamphead) entityIn;
			slam = lamphead.headSlamProgress;
			float partialTicks = ageInTicks - (float)entityIn.ticksExisted;
			squeeze = lamphead.prevSqueezeProgress + (lamphead.squeezeProgress - lamphead.prevSqueezeProgress) * partialTicks;
		}

		if (slam > 0.0F) {
			float slamAngle = MathHelper.sin(slam * (float)Math.PI / 2.0F); // Smooth curve to peak
			this.body.rotateAngleX += slamAngle * 1.0F; // Pitch body heavily forward
			this.neck.rotateAngleX += slamAngle * 0.5F; // Pitch neck forward
			this.head.rotateAngleX += slamAngle * 0.5F; // Pitch head down
		}
		
		if (squeeze > 0.0F) {
			float squeezeAngle = MathHelper.sin(squeeze * (float)Math.PI / 2.0F);
			this.neck.rotateAngleX -= squeezeAngle * 1.1345F; // Tilt neck back by 65 degrees
			this.head.rotateAngleX += squeezeAngle * 0.5672F; // Tilt head forward slightly to keep it level (32.5 degrees)
		}

		// Leg animations
		this.leg_left.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.leg_right.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;

		// Windup key animation (spins constantly)
		// Windup keys spin around their stem, which is the Y axis for this model
		this.windup_key.rotateAngleY = ageInTicks * 0.2F;
		this.windup_key.rotateAngleZ = 0.0F;

		// Attack / Walk animations for arms
		boolean attackStance = false;
		float interpolatedAttackProgress = 0.0F;

		if (entityIn instanceof EntityLamphead) {
			EntityLamphead lamphead = (EntityLamphead) entityIn;
			attackStance = lamphead.isInAttackStance();
			
			float partialTicks = ageInTicks - (float)entityIn.ticksExisted;
			interpolatedAttackProgress = lamphead.prevAttackProgress + (lamphead.attackProgress - lamphead.prevAttackProgress) * partialTicks;
		}

		if (attackStance || interpolatedAttackProgress > 0.0F) {
			// Attack stance: arms point forward horizontally (-90 deg X rotation)
			this.arm_left.rotateAngleX = -1.5708F;
			this.arm_right.rotateAngleX = -1.5708F;

			float attackPhase = interpolatedAttackProgress * (float)Math.PI * 2.0F; // 0 to 2PI
			
			// Use Math.max to prevent the arms from punching backwards into the body
			float leftPunch = Math.max(0.0F, MathHelper.sin(attackPhase));
			float rightPunch = Math.max(0.0F, MathHelper.sin(attackPhase + (float)Math.PI));

			// If not swinging, punch is 0
			if (interpolatedAttackProgress == 0.0F) {
				leftPunch = 0.0F;
				rightPunch = 0.0F;
			}

			// Move the arms forward/backward along the Z axis for the punch
			// Base Z is 3.3073F (moved back by 7 units from original). 
			this.arm_left.rotationPointZ = 3.3073F + (leftPunch * -9.0F);
			this.arm_right.rotationPointZ = 3.3073F + (rightPunch * -9.0F);
		} else {
			// Walk stance: arms hanging, no swing
			this.arm_left.rotateAngleX = 0.0F;
			this.arm_right.rotateAngleX = 0.0F;

			// Reset translation
			this.arm_left.rotationPointZ = -3.6927F;
			this.arm_right.rotationPointZ = -3.6927F;
		}

		// Fabricating animation
		float fabricate = 0.0F;
		if (entityIn instanceof EntityLamphead) {
			EntityLamphead lamphead = (EntityLamphead) entityIn;
			float partialTicks = ageInTicks - (float)entityIn.ticksExisted;
			fabricate = lamphead.prevFabricateProgress + (lamphead.fabricateProgress - lamphead.prevFabricateProgress) * partialTicks;
		}

		if (fabricate > 0.0F) {
			float bend = MathHelper.sin(fabricate * (float)Math.PI / 2.0F); // Smooth curve to peak
			this.body.rotateAngleX += bend * 0.8F; 
			this.neck.rotateAngleX -= bend * 0.6F; // Flex back heavily
			this.head.rotateAngleX -= bend * 0.2F; // Counteract body (0.8) and neck (-0.6) to stay horizontal (0.2)
			
			// Override arms: to keep arms completely horizontal (-1.5708 rads) while the body is bent by 0.8 rads,
			// they must be rotated by -1.5708 - 0.8 = -2.3708 rads relative to the body.
			this.arm_left.rotateAngleX = -2.3708F * bend;
			this.arm_right.rotateAngleX = -2.3708F * bend;
			
			// Removed outward shift (moving them 2px closer to center compared to before)
			this.arm_left.rotationPointX = 5.0F; 
			this.arm_right.rotationPointX = -5.0F; 
			
			// Mathematically precise offsets to keep arms exactly at Y=-2.2894 and Z=-2.6927 in world space when fully bent
			this.arm_left.rotationPointZ = -3.6927F + (bend * 3.46F);
			this.arm_right.rotationPointZ = -3.6927F + (bend * 3.46F);
			
			this.arm_left.rotationPointY = -5.2894F + (bend * 3.36F);
			this.arm_right.rotationPointY = -5.2894F + (bend * 3.36F);
		} else {
			this.arm_left.rotationPointX = 5.0F;
			this.arm_right.rotationPointX = -5.0F;
			
			this.arm_left.rotationPointY = -5.2894F;
			this.arm_right.rotationPointY = -5.2894F;
		}
	}
}
