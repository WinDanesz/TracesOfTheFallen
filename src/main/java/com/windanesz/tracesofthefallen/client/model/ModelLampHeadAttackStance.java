package com.windanesz.tracesofthefallen.client.model;// Made with Blockbench 5.1.5
// Exported for Minecraft version 1.7 - 1.12
// Paste this class into your mod and generate all required imports


import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelLampHeadAttackStance extends ModelBase {
	private final ModelRenderer base;
	private final ModelRenderer body;
	private final ModelRenderer cube_r1;
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

	public ModelLampHeadAttackStance() {
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

		windup_key = new ModelRenderer(this);
		windup_key.setRotationPoint(0.0F, -4.5355F, 4.7782F);
		body.addChild(windup_key);
		setRotationAngle(windup_key, -0.7854F, 0.0F, 0.0F);
		windup_key.cubeList.add(new ModelBox(windup_key, 43, 5, -3.0F, -2.0F, -0.001F, 6, 4, 0, 0.0F, false));

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
		setRotationAngle(arm_left, -1.5708F, 0.0F, 0.0F);
		arm_left.cubeList.add(new ModelBox(arm_left, 36, 47, -1.0F, -9.5F, -1.0F, 2, 13, 2, 0.0F, false));

		arm_right = new ModelRenderer(this);
		arm_right.setRotationPoint(-5.0F, -5.2894F, -3.6927F);
		body.addChild(arm_right);
		setRotationAngle(arm_right, -1.5708F, 0.0F, 0.0F);
		arm_right.cubeList.add(new ModelBox(arm_right, 36, 47, -1.0F, -8.5F, -1.0F, 2, 13, 2, 0.0F, true));

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
		base.render(f5);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}