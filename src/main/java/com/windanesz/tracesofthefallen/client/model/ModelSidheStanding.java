package com.windanesz.tracesofthefallen.client.model;// Made with Blockbench 5.1.5
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelSidheStanding extends ModelBase {
	private final ModelRenderer bone;
	private final ModelRenderer head;
	private final ModelRenderer body;
	private final ModelRenderer fur;
	private final ModelRenderer front_left_leg;
	private final ModelRenderer front_right_leg;
	private final ModelRenderer back_left_leg;
	private final ModelRenderer back_right_leg;
	private final ModelRenderer tail;

	public ModelSidheStanding() {
		textureWidth = 64;
		textureHeight = 32;

		bone = new ModelRenderer(this);
		bone.setRotationPoint(0.0143F, 12.8355F, 0.6104F);
		setRotationAngle(bone, -1.5708F, 0.0F, 0.0F);
		

		head = new ModelRenderer(this);
		head.setRotationPoint(-0.0143F, 2.2529F, -6.7643F);
		bone.addChild(head);
		setRotationAngle(head, 1.5708F, 0.0F, 0.0F);
		head.cubeList.add(new ModelBox(head, 0, 0, -2.5F, -3.995F, -2.625F, 5, 4, 5, 0.0F, false));
		head.cubeList.add(new ModelBox(head, 0, 24, -1.5F, -2.015F, -3.625F, 3, 2, 2, 0.0F, false));
		head.cubeList.add(new ModelBox(head, 51, 14, -4.5F, -6.995F, 0.375F, 4, 6, 0, 0.0F, false));
		head.cubeList.add(new ModelBox(head, 51, 14, 0.5F, -6.995F, 0.375F, 4, 6, 0, 0.0F, true));

		body = new ModelRenderer(this);
		body.setRotationPoint(-0.0143F, 2.2578F, 0.1107F);
		bone.addChild(body);
		setRotationAngle(body, 1.5708F, 0.0F, 0.0F);
		body.cubeList.add(new ModelBox(body, 21, 2, -2.0F, -7.0F, -2.0F, 4, 11, 4, 0.0F, false));

		fur = new ModelRenderer(this);
		fur.setRotationPoint(0.0F, -5.0F, -2.0F);
		body.addChild(fur);
		setRotationAngle(fur, -0.3927F, 0.0F, 0.0F);
		fur.cubeList.add(new ModelBox(fur, 16, 24, -2.0F, 0.0F, 0.0F, 4, 3, 0, 0.0F, false));

		front_left_leg = new ModelRenderer(this);
		front_left_leg.setRotationPoint(2.0857F, 2.3579F, -4.8893F);
		bone.addChild(front_left_leg);
		setRotationAngle(front_left_leg, 1.2217F, 0.0F, -0.9163F);
		front_left_leg.cubeList.add(new ModelBox(front_left_leg, 38, 4, -1.0F, -1.0F, -1.0F, 2, 9, 2, 0.0F, false));

		front_right_leg = new ModelRenderer(this);
		front_right_leg.setRotationPoint(-2.1143F, 2.3579F, -4.8893F);
		bone.addChild(front_right_leg);
		setRotationAngle(front_right_leg, 1.5244F, -0.3487F, 0.0159F);
		front_right_leg.cubeList.add(new ModelBox(front_right_leg, 38, 4, -1.0F, -1.0F, -1.0F, 2, 9, 2, 0.0F, true));

		back_left_leg = new ModelRenderer(this);
		back_left_leg.setRotationPoint(1.5857F, 2.2578F, 3.1107F);
		bone.addChild(back_left_leg);
		setRotationAngle(back_left_leg, 1.5708F, 0.0F, 0.0F);
		back_left_leg.cubeList.add(new ModelBox(back_left_leg, 8, 13, -1.1F, 3.0F, -1.0F, 2, 5, 2, 0.0F, true));
		back_left_leg.cubeList.add(new ModelBox(back_left_leg, 47, 4, -1.5F, -1.0F, -1.5F, 3, 5, 3, 0.0F, false));

		back_right_leg = new ModelRenderer(this);
		back_right_leg.setRotationPoint(-1.5143F, 2.2578F, 3.1107F);
		bone.addChild(back_right_leg);
		setRotationAngle(back_right_leg, 1.5708F, 0.0F, 0.0F);
		back_right_leg.cubeList.add(new ModelBox(back_right_leg, 8, 13, -1.1F, 3.0F, -1.0F, 2, 5, 2, 0.0F, false));
		back_right_leg.cubeList.add(new ModelBox(back_right_leg, 47, 4, -1.6F, -1.0F, -1.4F, 3, 5, 3, 0.0F, true));

		tail = new ModelRenderer(this);
		tail.setRotationPoint(-0.0143F, 0.2578F, 3.2107F);
		bone.addChild(tail);
		setRotationAngle(tail, 1.5708F, 0.0F, -3.1416F);
		tail.cubeList.add(new ModelBox(tail, 24, 7, 0.0F, -5.6F, -11.5F, 0, 8, 12, 0.0F, true));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		bone.render(f5);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}