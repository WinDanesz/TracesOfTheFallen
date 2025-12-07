package com.windanesz.tracesofthefallen.client.model;

import com.windanesz.tracesofthefallen.entity.EntitySpecter;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelSpecter extends ModelBase {
	private final ModelRenderer head;
	private final ModelRenderer headwear;
	private final ModelRenderer body;
	private final ModelRenderer chain;
	private final ModelRenderer bone2;
	private final ModelRenderer chain_r1;
	private final ModelRenderer bone3;
	private final ModelRenderer chain_r2;
	private final ModelRenderer bone4;
	private final ModelRenderer chain_r3;
	private final ModelRenderer bone6;
	private final ModelRenderer chain_r4;
	private final ModelRenderer left_arm;
	private final ModelRenderer right_arm;
	private final ModelRenderer legs;

	public ModelSpecter() {
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

		chain = new ModelRenderer(this);
		chain.setRotationPoint(0.25F, 2.9263F, -2.0314F);
		body.addChild(chain);
		setRotationAngle(chain, -0.4363F, 0.0F, 0.0F);
		chain.cubeList.add(new ModelBox(chain, 40, 28, -1.25F, 0.0F, 0.0F, 3, 4, 0, 0.0F, false));

		bone2 = new ModelRenderer(this);
		bone2.setRotationPoint(0.25F, 3.0F, 0.0F);
		chain.addChild(bone2);
		setRotationAngle(bone2, -0.1309F, 0.0F, 0.0F);


		chain_r1 = new ModelRenderer(this);
		chain_r1.setRotationPoint(0.0F, 1.0F, 0.0F);
		bone2.addChild(chain_r1);
		setRotationAngle(chain_r1, 0.0F, -1.309F, 0.0F);
		chain_r1.cubeList.add(new ModelBox(chain_r1, 40, 28, -1.5F, -2.0F, 0.0F, 3, 4, 0, 0.0F, false));

		bone3 = new ModelRenderer(this);
		bone3.setRotationPoint(0.0F, 2.0F, 0.0F);
		bone2.addChild(bone3);
		setRotationAngle(bone3, -0.0873F, 0.0F, 0.0F);


		chain_r2 = new ModelRenderer(this);
		chain_r2.setRotationPoint(0.0F, 1.0F, 0.0F);
		bone3.addChild(chain_r2);
		setRotationAngle(chain_r2, 0.0F, -0.2618F, 0.0F);
		chain_r2.cubeList.add(new ModelBox(chain_r2, 40, 28, -1.5F, -2.0F, 0.0F, 3, 4, 0, 0.0F, false));

		bone4 = new ModelRenderer(this);
		bone4.setRotationPoint(0.0F, 2.0F, 0.0F);
		bone3.addChild(bone4);
		setRotationAngle(bone4, -0.0873F, 0.0F, 0.0F);


		chain_r3 = new ModelRenderer(this);
		chain_r3.setRotationPoint(0.0F, 1.0F, 0.0F);
		bone4.addChild(chain_r3);
		setRotationAngle(chain_r3, 0.0F, -1.8326F, 0.0F);
		chain_r3.cubeList.add(new ModelBox(chain_r3, 40, 28, -1.5F, -2.0F, 0.0F, 3, 4, 0, 0.0F, false));

		bone6 = new ModelRenderer(this);
		bone6.setRotationPoint(0.0F, 2.0F, 0.0F);
		bone4.addChild(bone6);
		setRotationAngle(bone6, -0.0873F, 0.0F, 0.0F);


		chain_r4 = new ModelRenderer(this);
		chain_r4.setRotationPoint(0.0F, 1.0F, 0.0F);
		bone6.addChild(chain_r4);
		setRotationAngle(chain_r4, 0.0F, 0.2618F, 0.0F);
		chain_r4.cubeList.add(new ModelBox(chain_r4, 46, 28, -1.5F, -2.0F, 0.0F, 3, 4, 0, 0.0F, false));

		left_arm = new ModelRenderer(this);
		left_arm.setRotationPoint(5.0F, 2.0F, 0.0F);
		left_arm.cubeList.add(new ModelBox(left_arm, 0, 16, -1.0F, -2.0F, -1.0F, 2, 14, 2, 0.0F, true));

		right_arm = new ModelRenderer(this);
		right_arm.setRotationPoint(-5.0F, 2.0F, 0.0F);
		right_arm.cubeList.add(new ModelBox(right_arm, 0, 16, -1.0F, -2.0F, -1.0F, 2, 14, 2, 0.0F, false));

		legs = new ModelRenderer(this);
		legs.setRotationPoint(0.0F, 10.0F, 2.1F);
		setRotationAngle(legs, 0.3927F, 0.0F, 0.0F);
		legs.cubeList.add(new ModelBox(legs, 40, 16, -4.0F, 0.0F, -4.1F, 8, 8, 4, 0.0F, false));
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