package com.windanesz.tracesofthefallen.client.model;// Made with Blockbench 5.1.6
// Exported for Minecraft version 1.7 - 1.12
// Paste this class into your mod and generate all required imports


import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelFrostlingStand extends ModelBase {
	private final ModelRenderer bone;
	private final ModelRenderer neck;
	private final ModelRenderer head_upper;
	private final ModelRenderer eye_r_r1;
	private final ModelRenderer eye_l_r1;
	private final ModelRenderer mask;
	private final ModelRenderer craneum;
	private final ModelRenderer lower_jaw;
	private final ModelRenderer tusks_r1;
	private final ModelRenderer body;
	private final ModelRenderer leg_right;
	private final ModelRenderer leg_left;
	private final ModelRenderer arm_right;
	private final ModelRenderer shield_right;
	private final ModelRenderer arm_left;
	private final ModelRenderer shield_left;

	public ModelFrostlingStand() {
		textureWidth = 64;
		textureHeight = 64;

		bone = new ModelRenderer(this);
		bone.setRotationPoint(0.0F, 6.0F, -8.5F);
		

		neck = new ModelRenderer(this);
		neck.setRotationPoint(0.0F, 5.4F, 7.772F);
		bone.addChild(neck);
		setRotationAngle(neck, -1.1781F, 0.0F, 0.0F);
		neck.cubeList.add(new ModelBox(neck, 39, 31, 2.0F, -10.9008F, -1.3901F, 0, 13, 3, 0.0F, false));
		neck.cubeList.add(new ModelBox(neck, 39, 31, -2.0F, -10.9008F, -1.3901F, 0, 13, 3, 0.0F, false));

		head_upper = new ModelRenderer(this);
		head_upper.setRotationPoint(0.0F, -7.8953F, -0.0433F);
		neck.addChild(head_upper);
		setRotationAngle(head_upper, 1.1781F, 0.0F, 0.0F);
		head_upper.cubeList.add(new ModelBox(head_upper, 0, 46, -6.0F, -3.9245F, -11.5222F, 12, 4, 13, -0.1F, false));

		eye_r_r1 = new ModelRenderer(this);
		eye_r_r1.setRotationPoint(-5.9747F, -4.5809F, -11.095F);
		head_upper.addChild(eye_r_r1);
		setRotationAngle(eye_r_r1, 0.2618F, -0.3491F, 0.0F);
		eye_r_r1.cubeList.add(new ModelBox(eye_r_r1, 37, 26, 0.138F, 2.3934F, -0.1724F, 2, 2, 5, 0.0F, true));

		eye_l_r1 = new ModelRenderer(this);
		eye_l_r1.setRotationPoint(5.9747F, -4.5809F, -11.095F);
		head_upper.addChild(eye_l_r1);
		setRotationAngle(eye_l_r1, 0.2618F, 0.3491F, 0.0F);
		eye_l_r1.cubeList.add(new ModelBox(eye_l_r1, 37, 26, -2.1722F, 2.4177F, -0.0816F, 2, 2, 5, 0.0F, false));

		mask = new ModelRenderer(this);
		mask.setRotationPoint(0.0F, 0.0F, 0.0F);
		head_upper.addChild(mask);
		mask.cubeList.add(new ModelBox(mask, 35, 48, -4.0F, -3.9F, -10.0F, 8, 0, 10, 0.0F, false));

		craneum = new ModelRenderer(this);
		craneum.setRotationPoint(0.0F, -1.8997F, -11.4495F);
		head_upper.addChild(craneum);
		craneum.cubeList.add(new ModelBox(craneum, 0, 0, -6.0F, -2.0247F, -0.0727F, 12, 4, 13, 0.0F, false));

		lower_jaw = new ModelRenderer(this);
		lower_jaw.setRotationPoint(0.0F, 0.106F, -6.6034F);
		head_upper.addChild(lower_jaw);
		lower_jaw.cubeList.add(new ModelBox(lower_jaw, 26, 17, -6.0F, -0.0304F, -4.9188F, 12, 2, 5, 0.0F, false));

		tusks_r1 = new ModelRenderer(this);
		tusks_r1.setRotationPoint(0.5821F, 1.977F, -4.867F);
		lower_jaw.addChild(tusks_r1);
		setRotationAngle(tusks_r1, 0.1745F, 0.0F, 0.0F);
		tusks_r1.cubeList.add(new ModelBox(tusks_r1, 11, 33, -4.4821F, -5.0859F, -0.8023F, 2, 3, 1, 0.1F, true));
		tusks_r1.cubeList.add(new ModelBox(tusks_r1, 11, 33, 1.3178F, -5.0859F, -0.8023F, 2, 3, 1, 0.1F, false));

		body = new ModelRenderer(this);
		body.setRotationPoint(0.0F, 9.0F, 8.5F);
		bone.addChild(body);
		body.cubeList.add(new ModelBox(body, 0, 17, -4.0F, -4.0F, -4.0F, 8, 6, 10, 0.0F, false));

		leg_right = new ModelRenderer(this);
		leg_right.setRotationPoint(-4.0F, 10.0F, 8.5F);
		bone.addChild(leg_right);
		leg_right.cubeList.add(new ModelBox(leg_right, 37, 0, -2.0F, 0.0F, -1.0F, 3, 8, 3, 0.0F, false));

		leg_left = new ModelRenderer(this);
		leg_left.setRotationPoint(4.0F, 10.0F, 8.5F);
		bone.addChild(leg_left);
		leg_left.cubeList.add(new ModelBox(leg_left, 37, 0, -1.1F, 0.0F, -1.0F, 3, 8, 3, 0.0F, true));

		arm_right = new ModelRenderer(this);
		arm_right.setRotationPoint(-4.0F, 6.0F, 11.0F);
		bone.addChild(arm_right);
		setRotationAngle(arm_right, 0.0F, 0.0F, 0.3927F);
		arm_right.cubeList.add(new ModelBox(arm_right, 1, 0, -2.0F, 0.0F, -1.5F, 2, 7, 3, 0.0F, true));

		shield_right = new ModelRenderer(this);
		shield_right.setRotationPoint(-1.0F, 7.0F, 0.5F);
		arm_right.addChild(shield_right);
		shield_right.cubeList.add(new ModelBox(shield_right, 44, 27, 0.0F, 0.0F, -4.0F, 0, 5, 7, 0.0F, true));

		arm_left = new ModelRenderer(this);
		arm_left.setRotationPoint(4.0F, 6.0F, 11.0F);
		bone.addChild(arm_left);
		setRotationAngle(arm_left, 0.0F, 0.0F, -0.3927F);
		arm_left.cubeList.add(new ModelBox(arm_left, 1, 0, 0.0F, 0.0F, -1.5F, 2, 7, 3, 0.0F, false));

		shield_left = new ModelRenderer(this);
		shield_left.setRotationPoint(1.0F, 7.0F, 1.5F);
		arm_left.addChild(shield_left);
		shield_left.cubeList.add(new ModelBox(shield_left, 44, 27, 0.0F, 0.0F, -5.0F, 0, 5, 7, 0.0F, false));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		bone.render(f5);
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		// Head tracking is disabled because the complex pre-rotated neck and head joints cause severe gimbal lock/tilting when applying standard yaw/pitch.
		
		float bite = 0.0F;
		float shoot = 0.0F;
		float aggro = 0.0F;
		float slash = 0.0F;
		boolean noMask = false;
		boolean shooting = false;
		if (entityIn instanceof com.windanesz.tracesofthefallen.entity.EntityFrostling) {
			com.windanesz.tracesofthefallen.entity.EntityFrostling frostling = (com.windanesz.tracesofthefallen.entity.EntityFrostling) entityIn;
			float partialTicks = ageInTicks - entityIn.ticksExisted;
			bite = frostling.getBiteProgress(partialTicks);
			shoot = frostling.getShootProgress(partialTicks);
			aggro = frostling.getAggroProgress(partialTicks);
			slash = frostling.getSlashProgress(partialTicks);
			noMask = frostling.isNoMask();
			shooting = frostling.isShooting();
		}
		
		this.mask.showModel = !noMask;
		
		if (shooting || noMask) {
			this.neck.rotateAngleX = -1.1781F + (0.6981F * shoot);
			this.head_upper.rotateAngleX = 1.1781F;
			this.craneum.rotateAngleX = -2.1817F * shoot;
			this.arm_right.rotateAngleZ = 0.3927F * shoot;
			this.arm_left.rotateAngleZ = -0.3927F * shoot;
			
			// Stay motionless
			this.leg_left.rotationPointZ = 8.5F;
			this.leg_right.rotationPointZ = 8.5F;
			this.arm_left.rotateAngleX = -1.5708F;
			this.arm_right.rotateAngleX = -1.5708F;
		} else {
			this.craneum.rotateAngleX = 0.0F;
			this.arm_left.rotateAngleY = 0.0F;
			
			float swingArc = net.minecraft.util.math.MathHelper.sin(slash * (float)Math.PI);
			
			// Right arm horizontal sweep forward
			float rightArmX = -0.45F + (-1.2F - -0.45F) * swingArc;
			float rightArmZ = 0.45F + (-1.6F - 0.45F) * swingArc;
			float rightArmY = 0.0F;
			
			this.arm_right.rotateAngleZ = rightArmZ;
			this.arm_right.rotateAngleY = rightArmY;
			
			// Left arm stays resting
			this.arm_left.rotateAngleZ = -0.45F;
			
			// Bite animation (smoothly interpolate to ModelFrostlingBite pose)
			this.neck.rotateAngleX = -1.1781F + (2.3562F * bite);
			this.head_upper.rotateAngleX = 1.1781F - (2.3562F * bite);
			this.lower_jaw.rotateAngleX = 1.2F * bite;
			
			// Horizontally slide legs back and forth
			this.leg_left.rotationPointZ = 8.5F + net.minecraft.util.math.MathHelper.cos(limbSwing * 0.6662F) * 2.5F * limbSwingAmount;
			this.leg_right.rotationPointZ = 8.5F + net.minecraft.util.math.MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.5F * limbSwingAmount;
			
			this.arm_left.rotateAngleX = net.minecraft.util.math.MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 0.4F * limbSwingAmount - 0.45F;
			this.arm_right.rotateAngleX = net.minecraft.util.math.MathHelper.cos(limbSwing * 0.6662F) * 0.4F * limbSwingAmount + rightArmX;
		}
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}