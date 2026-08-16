package com.windanesz.tracesofthefallen.client.model;// Made with Blockbench 5.1.6
// Exported for Minecraft version 1.7 - 1.12
// Paste this class into your mod and generate all required imports


import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelSpecterGrasper extends ModelBase {
	private final ModelRenderer base;
	private final ModelRenderer head;
	private final ModelRenderer body;
	private final ModelRenderer right_cloth_r1;
	private final ModelRenderer left_cloth_r1;
	private final ModelRenderer left_cloth_r2;
	private final ModelRenderer right_cloth_r2;
	private final ModelRenderer body_r1;
	private final ModelRenderer chain;
	private final ModelRenderer bone2;
	private final ModelRenderer chain_r1;
	private final ModelRenderer bone3;
	private final ModelRenderer chain_r2;
	private final ModelRenderer bone4;
	private final ModelRenderer chain_r3;
	private final ModelRenderer bone6;
	private final ModelRenderer chain_r4;
	private final ModelRenderer bone5;
	private final ModelRenderer chain_r5;
	private final ModelRenderer bone7;
	private final ModelRenderer chain_r6;
	private final ModelRenderer right_arm;
	private final ModelRenderer right_arm_r1;
	private final ModelRenderer right_forearm;
	private final ModelRenderer right_arm_r2;
	private final ModelRenderer left_arm;
	private final ModelRenderer left_arm_r1;
	private final ModelRenderer left_forearm;
	private final ModelRenderer left_arm_r2;

	public ModelSpecterGrasper() {
		textureWidth = 64;
		textureHeight = 64;

		base = new ModelRenderer(this);
		base.setRotationPoint(0.0F, 15.0F, 0.0F);
		

		head = new ModelRenderer(this);
		head.setRotationPoint(0.0F, -22.0F, 0.0F);
		base.addChild(head);
		head.cubeList.add(new ModelBox(head, 0, 0, -5.0F, -10.0F, -4.5F, 10, 10, 9, 0.0F, false));

		body = new ModelRenderer(this);
		body.setRotationPoint(0.0F, -22.0F, 0.0F);
		base.addChild(body);
		body.cubeList.add(new ModelBox(body, 12, 44, -5.0F, 0.0F, -2.0F, 10, 5, 4, 0.0F, false));

		right_cloth_r1 = new ModelRenderer(this);
		right_cloth_r1.setRotationPoint(-9.2895F, 6.3199F, 5.9673F);
		body.addChild(right_cloth_r1);
		setRotationAngle(right_cloth_r1, 1.3587F, 0.8013F, 2.1756F);
		right_cloth_r1.cubeList.add(new ModelBox(right_cloth_r1, 15, 19, -3.0F, -7.0F, -3.0F, 6, 17, 6, 0.0F, true));

		left_cloth_r1 = new ModelRenderer(this);
		left_cloth_r1.setRotationPoint(9.2895F, 6.3199F, 5.9673F);
		body.addChild(left_cloth_r1);
		setRotationAngle(left_cloth_r1, 1.3587F, -0.8013F, -2.1756F);
		left_cloth_r1.cubeList.add(new ModelBox(left_cloth_r1, 15, 19, -3.0F, -7.0F, -3.0F, 6, 17, 6, 0.0F, false));

		left_cloth_r2 = new ModelRenderer(this);
		left_cloth_r2.setRotationPoint(7.0F, -0.3F, -5.0F);
		body.addChild(left_cloth_r2);
		setRotationAngle(left_cloth_r2, -0.7582F, -0.147F, -0.7582F);
		left_cloth_r2.cubeList.add(new ModelBox(left_cloth_r2, 15, 19, -6.0F, -2.0F, 1.0F, 6, 17, 6, 0.0F, false));

		right_cloth_r2 = new ModelRenderer(this);
		right_cloth_r2.setRotationPoint(-7.0F, -0.3F, -5.0F);
		body.addChild(right_cloth_r2);
		setRotationAngle(right_cloth_r2, -0.7582F, 0.147F, 0.7582F);
		right_cloth_r2.cubeList.add(new ModelBox(right_cloth_r2, 15, 19, 0.0F, -2.0F, 1.0F, 6, 17, 6, 0.0F, true));

		body_r1 = new ModelRenderer(this);
		body_r1.setRotationPoint(4.0F, 8.1827F, 2.3529F);
		body.addChild(body_r1);
		setRotationAngle(body_r1, 0.7854F, 0.0F, 0.0F);
		body_r1.cubeList.add(new ModelBox(body_r1, 34, 53, -5.0F, -2.5F, 2.0F, 2, 9, 0, 0.0F, false));

		chain = new ModelRenderer(this);
		chain.setRotationPoint(0.25F, 2.9263F, -2.0314F);
		body.addChild(chain);
		setRotationAngle(chain, -0.2618F, 0.0F, 0.0F);
		chain.cubeList.add(new ModelBox(chain, 40, 56, -1.25F, 0.0F, 0.0F, 3, 4, 0, 0.0F, false));

		bone2 = new ModelRenderer(this);
		bone2.setRotationPoint(0.25F, 3.0F, 0.0F);
		chain.addChild(bone2);
		setRotationAngle(bone2, -0.1309F, 0.0F, 0.0F);
		

		chain_r1 = new ModelRenderer(this);
		chain_r1.setRotationPoint(0.0F, 1.0F, 0.0F);
		bone2.addChild(chain_r1);
		setRotationAngle(chain_r1, 0.0F, -1.309F, 0.0F);
		chain_r1.cubeList.add(new ModelBox(chain_r1, 40, 56, -1.5F, -2.0F, 0.0F, 3, 4, 0, 0.0F, false));

		bone3 = new ModelRenderer(this);
		bone3.setRotationPoint(0.0F, 2.0F, 0.0F);
		bone2.addChild(bone3);
		setRotationAngle(bone3, 0.3054F, 0.0F, 0.0F);
		

		chain_r2 = new ModelRenderer(this);
		chain_r2.setRotationPoint(0.0F, 1.0F, 0.0F);
		bone3.addChild(chain_r2);
		setRotationAngle(chain_r2, 0.0F, -0.2618F, 0.0F);
		chain_r2.cubeList.add(new ModelBox(chain_r2, 40, 56, -1.5F, -2.0F, 0.0F, 3, 4, 0, 0.0F, false));

		bone4 = new ModelRenderer(this);
		bone4.setRotationPoint(0.0F, 2.0F, 0.0F);
		bone3.addChild(bone4);
		setRotationAngle(bone4, 0.3054F, 0.0F, 0.0F);
		

		chain_r3 = new ModelRenderer(this);
		chain_r3.setRotationPoint(0.0F, 1.0F, 0.0F);
		bone4.addChild(chain_r3);
		setRotationAngle(chain_r3, 0.0F, -1.8326F, 0.0F);
		chain_r3.cubeList.add(new ModelBox(chain_r3, 40, 56, -1.5F, -2.0F, 0.0F, 3, 4, 0, 0.0F, false));

		bone6 = new ModelRenderer(this);
		bone6.setRotationPoint(0.0F, 2.0F, 0.0F);
		bone4.addChild(bone6);
		setRotationAngle(bone6, 0.3054F, 0.0F, 0.0F);
		

		chain_r4 = new ModelRenderer(this);
		chain_r4.setRotationPoint(0.0F, 1.0F, 0.0F);
		bone6.addChild(chain_r4);
		setRotationAngle(chain_r4, 0.0F, 0.2618F, 0.0F);
		chain_r4.cubeList.add(new ModelBox(chain_r4, 40, 56, -1.5F, -2.0F, 0.0F, 3, 4, 0, 0.0F, false));

		bone5 = new ModelRenderer(this);
		bone5.setRotationPoint(0.0F, 1.9924F, -0.1743F);
		bone6.addChild(bone5);
		setRotationAngle(bone5, 0.2696F, -0.7816F, 0.0869F);
		

		chain_r5 = new ModelRenderer(this);
		chain_r5.setRotationPoint(0.0F, 1.0F, 0.0F);
		bone5.addChild(chain_r5);
		setRotationAngle(chain_r5, 0.0F, 0.2618F, 0.0F);
		chain_r5.cubeList.add(new ModelBox(chain_r5, 40, 56, -1.5F, -2.0F, 0.0F, 3, 4, 0, 0.0F, false));

		bone7 = new ModelRenderer(this);
		bone7.setRotationPoint(-0.0158F, 2.0118F, 0.0479F);
		bone5.addChild(bone7);
		setRotationAngle(bone7, 0.2983F, 0.3911F, -0.0361F);
		

		chain_r6 = new ModelRenderer(this);
		chain_r6.setRotationPoint(0.0F, 1.0F, 0.0F);
		bone7.addChild(chain_r6);
		setRotationAngle(chain_r6, 0.0F, 0.2618F, 0.0F);
		chain_r6.cubeList.add(new ModelBox(chain_r6, 46, 56, -1.5F, -2.0F, 0.0F, 3, 4, 0, 0.0F, false));

		right_arm = new ModelRenderer(this);
		right_arm.setRotationPoint(-5.0F, -21.5245F, -0.6913F);
		base.addChild(right_arm);
		setRotationAngle(right_arm, -0.3927F, 0.0F, 0.0F);
		

		right_arm_r1 = new ModelRenderer(this);
		right_arm_r1.setRotationPoint(-1.0F, 0.8889F, 0.9417F);
		right_arm.addChild(right_arm_r1);
		setRotationAngle(right_arm_r1, -0.3927F, 1.1781F, 0.0F);
		right_arm_r1.cubeList.add(new ModelBox(right_arm_r1, 0, 40, -1.0F, -1.2409F, -1.3708F, 2, 18, 2, 0.0F, false));

		right_forearm = new ModelRenderer(this);
		right_forearm.setRotationPoint(-8.521F, 15.8186F, -1.0912F);
		right_arm.addChild(right_forearm);
		setRotationAngle(right_forearm, -0.9426F, -0.3614F, 0.4215F);
		

		right_arm_r2 = new ModelRenderer(this);
		right_arm_r2.setRotationPoint(0.0542F, 0.0F, -0.0042F);
		right_forearm.addChild(right_arm_r2);
		setRotationAngle(right_arm_r2, 0.0F, 0.0F, 0.0F);
		right_arm_r2.cubeList.add(new ModelBox(right_arm_r2, 0, 40, 0.0F, 0.0091F, -1.9958F, 2, 18, 2, -0.02F, false));

		left_arm = new ModelRenderer(this);
		left_arm.setRotationPoint(5.0F, -21.5245F, -0.6913F);
		base.addChild(left_arm);
		setRotationAngle(left_arm, -0.3927F, 0.0F, 0.0F);
		

		left_arm_r1 = new ModelRenderer(this);
		left_arm_r1.setRotationPoint(1.0F, 0.8889F, 0.9417F);
		left_arm.addChild(left_arm_r1);
		setRotationAngle(left_arm_r1, -0.3927F, -1.1781F, 0.0F);
		left_arm_r1.cubeList.add(new ModelBox(left_arm_r1, 0, 40, -1.0F, -1.2409F, -1.3708F, 2, 18, 2, 0.0F, true));

		left_forearm = new ModelRenderer(this);
		left_forearm.setRotationPoint(8.521F, 15.8186F, -1.0912F);
		left_arm.addChild(left_forearm);
		setRotationAngle(left_forearm, -0.9426F, 0.3614F, -0.4215F);
		

		left_arm_r2 = new ModelRenderer(this);
		left_arm_r2.setRotationPoint(-0.0542F, 0.0F, -0.0042F);
		left_forearm.addChild(left_arm_r2);
		setRotationAngle(left_arm_r2, 0.0F, 0.0F, 0.0F);
		left_arm_r2.cubeList.add(new ModelBox(left_arm_r2, 0, 40, -2.0F, 0.0091F, -1.9958F, 2, 18, 2, -0.02F, true));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
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

		this.head.rotateAngleX = headPitch * 0.017453292F;
		this.head.rotateAngleY = netHeadYaw * 0.017453292F;

		this.right_arm.rotateAngleZ = 0.0F;
		this.left_arm.rotateAngleZ = 0.0F;

		// Normal idle pose (bug-like)
		this.right_arm.rotateAngleX = 0.0F + MathHelper.cos(ageInTicks * 0.09F) * 0.05F;
		this.left_arm.rotateAngleX = 0.0F - MathHelper.cos(ageInTicks * 0.09F) * 0.05F;
		this.right_arm.rotateAngleZ = MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
		this.left_arm.rotateAngleZ = -MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
		this.right_arm.rotateAngleY = 0.0F;
		this.left_arm.rotateAngleY = 0.0F;
		
		float baseForearmX = -2.2F; // Highly flexed like a mantis
		this.right_forearm.rotateAngleX = baseForearmX;
		this.left_forearm.rotateAngleX = baseForearmX;

		if (this.swingProgress > 0.0F && entityIn instanceof net.minecraft.entity.EntityLivingBase) {
			net.minecraft.entity.EntityLivingBase living = (net.minecraft.entity.EntityLivingBase) entityIn;
			boolean isLeftHand = living.swingingHand == net.minecraft.util.EnumHand.OFF_HAND;
			
			float f = this.swingProgress;
			float raise = MathHelper.sin(MathHelper.sqrt(f) * (float)Math.PI);
			float sweep = -MathHelper.cos(f * (float)Math.PI); // Sweeps from -1 to +1
			
			if (isLeftHand) {
				// Lower, wider sideways slash (Left Arm - Mirrored)
				this.left_arm.rotateAngleX = 0.0F - raise * 0.8F; 
				this.left_arm.rotateAngleY = -sweep * raise * 1.5F; // Proper forehand left-to-right sweep
				this.left_arm.rotateAngleZ -= raise * 0.8F; // Flare out sideways to the left
				
				// Extend the forearm out during the slash
				this.left_forearm.rotateAngleX = baseForearmX + raise * 2.0F;
			} else {
				// Lower, wider sideways slash (Right Arm)
				this.right_arm.rotateAngleX = 0.0F - raise * 0.8F; 
				this.right_arm.rotateAngleY = sweep * raise * 1.5F; // Proper forehand right-to-left sweep
				this.right_arm.rotateAngleZ += raise * 0.8F; // Flare out sideways to the right
				
				// Extend the forearm out during the slash
				this.right_forearm.rotateAngleX = baseForearmX + raise * 2.0F;
			}
		}

		// Cloth animation
		float clothTime = ageInTicks * 0.05F;
		float clothSway1 = MathHelper.sin(clothTime) * 0.08F;
		float clothSway2 = MathHelper.cos(clothTime + 1.0F) * 0.08F;
		
		this.right_cloth_r1.rotateAngleX = 1.3587F + clothSway1;
		this.right_cloth_r1.rotateAngleY = 0.8013F;
		this.right_cloth_r1.rotateAngleZ = 2.1756F + clothSway2;
		
		this.left_cloth_r1.rotateAngleX = 1.3587F + clothSway1;
		this.left_cloth_r1.rotateAngleY = -0.8013F;
		this.left_cloth_r1.rotateAngleZ = -2.1756F - clothSway2;
		
		this.left_cloth_r2.rotateAngleX = -0.7582F + clothSway2;
		this.left_cloth_r2.rotateAngleY = -0.147F;
		this.left_cloth_r2.rotateAngleZ = -0.7582F - clothSway1;
		
		this.right_cloth_r2.rotateAngleX = -0.7582F + clothSway2;
		this.right_cloth_r2.rotateAngleY = 0.147F;
		this.right_cloth_r2.rotateAngleZ = 0.7582F + clothSway1;

		// Chain animation (ripple effect)
		float chainTime = ageInTicks * 0.1F;
		float chainSway = 0.1F;
		
		this.chain.rotateAngleX = -0.2618F + MathHelper.sin(chainTime) * chainSway;
		this.bone2.rotateAngleX = -0.1309F + MathHelper.sin(chainTime - 0.5F) * chainSway;
		this.bone3.rotateAngleX = 0.3054F + MathHelper.sin(chainTime - 1.0F) * chainSway;
		this.bone4.rotateAngleX = 0.3054F + MathHelper.sin(chainTime - 1.5F) * chainSway;
		this.bone6.rotateAngleX = 0.3054F + MathHelper.sin(chainTime - 2.0F) * chainSway;
		this.bone5.rotateAngleX = 0.2696F + MathHelper.sin(chainTime - 2.5F) * chainSway;
		this.bone7.rotateAngleX = 0.2983F + MathHelper.sin(chainTime - 3.0F) * chainSway;
		
		this.chain.rotateAngleZ = MathHelper.cos(chainTime) * (chainSway * 0.5F);
		this.bone2.rotateAngleZ = MathHelper.cos(chainTime - 0.5F) * (chainSway * 0.5F);
		this.bone3.rotateAngleZ = MathHelper.cos(chainTime - 1.0F) * (chainSway * 0.5F);
		this.bone4.rotateAngleZ = MathHelper.cos(chainTime - 1.5F) * (chainSway * 0.5F);
		this.bone6.rotateAngleZ = MathHelper.cos(chainTime - 2.0F) * (chainSway * 0.5F);
		
		this.bone5.rotateAngleY = -0.7816F;
		this.bone5.rotateAngleZ = 0.0869F + MathHelper.cos(chainTime - 2.5F) * (chainSway * 0.5F);
		this.bone7.rotateAngleY = 0.3911F;
		this.bone7.rotateAngleZ = -0.0361F + MathHelper.cos(chainTime - 3.0F) * (chainSway * 0.5F);
	}
}