package com.windanesz.tracesofthefallen.client.model;// Made with Blockbench 5.1.4

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;

public class ModelGoblinTrapper extends ModelGoblinBase {
	private final ModelRenderer earring;
	private final ModelRenderer cube_r1;
	private final ModelRenderer ear_left;
	private final ModelRenderer ear_left_r1;
	private final ModelRenderer ear_right;
	private final ModelRenderer ear_right_r1;

	public ModelGoblinTrapper() {
		textureWidth = 48;
		textureHeight = 48;

		body = new ModelRenderer(this);
		body.setRotationPoint(0.0F, 25.0F, 0.0F);
		body.cubeList.add(new ModelBox(body, 27, 12, -3.0F, -15.0F, -2.0F, 6, 8, 3, 0.0F, false));

		head = new ModelRenderer(this);
		head.setRotationPoint(0.0F, -15.0F, -0.5F);
		body.addChild(head);
		head.cubeList.add(new ModelBox(head, 0, 0, -3.0F, -6.0F, -3.0F, 6, 6, 6, 0.0F, false));
		head.cubeList.add(new ModelBox(head, 22, 26, -0.9503F, -3.0F, -5.9929F, 2, 3, 3, 0.0F, false));

		earring = new ModelRenderer(this);
		earring.setRotationPoint(-5.077F, -3.0F, 1.992F);
		head.addChild(earring);
		

		cube_r1 = new ModelRenderer(this);
		cube_r1.setRotationPoint(-2.1144F, 4.0F, 6.2273F);
		earring.addChild(cube_r1);
		setRotationAngle(cube_r1, 0.0F, -0.48F, 0.0F);
		cube_r1.cubeList.add(new ModelBox(cube_r1, 14, 13, -1.0F, -4.0F, -8.0F, 0, 3, 3, 0.0F, false));

		ear_left = new ModelRenderer(this);
		ear_left.setRotationPoint(3.0497F, -3.0F, -1.9929F);
		head.addChild(ear_left);
		

		ear_left_r1 = new ModelRenderer(this);
		ear_left_r1.setRotationPoint(-0.0497F, 0.0F, -0.0071F);
		ear_left.addChild(ear_left_r1);
		setRotationAngle(ear_left_r1, 0.0F, 0.48F, 0.0F);
		ear_left_r1.cubeList.add(new ModelBox(ear_left_r1, 18, -7, 0.0F, -2.0F, 0.0F, 0, 4, 7, 0.0F, false));

		ear_right = new ModelRenderer(this);
		ear_right.setRotationPoint(-2.9503F, -3.0F, -1.9929F);
		head.addChild(ear_right);
		

		ear_right_r1 = new ModelRenderer(this);
		ear_right_r1.setRotationPoint(-0.0488F, -1.0F, -0.0067F);
		ear_right.addChild(ear_right_r1);
		setRotationAngle(ear_right_r1, 0.0F, -0.48F, 0.0F);
		ear_right_r1.cubeList.add(new ModelBox(ear_right_r1, 34, -7, 0.0F, -1.0F, 0.0F, 0, 4, 7, 0.0F, true));

		leg_left = new ModelRenderer(this);
		leg_left.setRotationPoint(1.5F, -7.0F, -0.5F);
		body.addChild(leg_left);
		leg_left.cubeList.add(new ModelBox(leg_left, 0, 22, -1.5F, 0.0F, -1.5F, 3, 6, 3, 0.0F, false));

		leg_right = new ModelRenderer(this);
		leg_right.setRotationPoint(-1.5F, -7.0F, -0.5F);
		body.addChild(leg_right);
		leg_right.cubeList.add(new ModelBox(leg_right, 0, 22, -1.5F, 0.0F, -1.5F, 3, 6, 3, 0.0F, true));

		arm_left = new ModelRenderer(this);
		arm_left.setRotationPoint(4.0F, -14.0F, -0.5F);
		body.addChild(arm_left);
		arm_left.cubeList.add(new ModelBox(arm_left, 0, 12, -1.0F, -1.0F, -1.5F, 3, 7, 3, 0.0F, false));

		arm_right = new ModelRenderer(this);
		arm_right.setRotationPoint(-4.0F, -14.0F, -0.5F);
		body.addChild(arm_right);
		arm_right.cubeList.add(new ModelBox(arm_right, 0, 31, -2.0F, -1.0F, -1.5F, 3, 7, 3, 0.0F, true));

		initBiped();
	}
}