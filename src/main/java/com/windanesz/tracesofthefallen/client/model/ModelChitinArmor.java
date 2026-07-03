package com.windanesz.tracesofthefallen.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;

/**
 * Custom chitin armor model authored in Blockbench.
 */
public class ModelChitinArmor extends ModelBiped {

	public ModelChitinArmor() {
		super(1.0F, 0.0F, 64, 64);

		ModelRenderer head = new ModelRenderer(this);
		head.setRotationPoint(0.0F, 0.0F, 0.0F);
		head.cubeList.add(new ModelBox(head, 0, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, 0.75F, false));

		ModelRenderer helmetFront = new ModelRenderer(this);
		helmetFront.setRotationPoint(0.0F, 0.0F, 0.0F);
		head.addChild(helmetFront);
		setRotationAngle(helmetFront, 0.3927F, 0.0F, 0.0F);
		helmetFront.cubeList.add(new ModelBox(helmetFront, 32, 0, -5.0F, -9.1008F, -3.3543F, 10, 2, 6, 0.0F, false));

		ModelRenderer helmetBack = new ModelRenderer(this);
		helmetBack.setRotationPoint(0.0F, 0.0F, 0.0F);
		head.addChild(helmetBack);
		setRotationAngle(helmetBack, -0.7854F, 0.0F, 0.0F);
		helmetBack.cubeList.add(new ModelBox(helmetBack, 32, 9, -2.0F, -9.4929F, -4.6787F, 4, 3, 4, 0.0F, false));

		ModelRenderer body = new ModelRenderer(this);
		body.setRotationPoint(0.0F, 0.0F, 0.0F);
		body.cubeList.add(new ModelBox(body, 16, 16, -4.0F, 0.0F, -2.0F, 8, 12, 4, 0.28F, false));
		body.cubeList.add(new ModelBox(body, 0, 41, -4.0F, 9.0F, -2.0F, 8, 3, 4, 0.26F, false));

		ModelRenderer bodyBackTop = new ModelRenderer(this);
		bodyBackTop.setRotationPoint(-2.0F, 2.8429F, 7.9686F);
		body.addChild(bodyBackTop);
		setRotationAngle(bodyBackTop, -0.3927F, 0.0F, 0.0F);
		bodyBackTop.cubeList.add(new ModelBox(bodyBackTop, 49, 33, 0.0F, 3.0F, -2.5F, 4, 1, 1, 0.26F, false));
		bodyBackTop.cubeList.add(new ModelBox(bodyBackTop, 49, 33, 0.0F, 0.0F, -2.5F, 4, 1, 1, 0.26F, false));

		ModelRenderer bodyBackMid = new ModelRenderer(this);
		bodyBackMid.setRotationPoint(0.0F, 0.8569F, 7.146F);
		body.addChild(bodyBackMid);
		setRotationAngle(bodyBackMid, -0.3927F, 0.0F, 0.0F);
		bodyBackMid.cubeList.add(new ModelBox(bodyBackMid, 49, 33, -2.0F, 0.0F, -2.5F, 4, 1, 1, 0.26F, false));

		ModelRenderer bodyBackBase = new ModelRenderer(this);
		bodyBackBase.setRotationPoint(0.0F, 4.8087F, 2.2619F);
		body.addChild(bodyBackBase);
		setRotationAngle(bodyBackBase, -0.3927F, 0.0F, 0.0F);
		bodyBackBase.cubeList.add(new ModelBox(bodyBackBase, 29, 32, -4.0F, -4.0F, -1.5F, 8, 8, 3, 0.27F, false));

		ModelRenderer rightArm = new ModelRenderer(this);
		rightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
		rightArm.cubeList.add(new ModelBox(rightArm, 48, 48, -4.16F, -4.18F, -2.0F, 3, 6, 4, 0.26F, true));
		rightArm.cubeList.add(new ModelBox(rightArm, 40, 16, -3.0F, -2.0F, -2.0F, 4, 12, 4, 0.28F, true));

		ModelRenderer leftArm = new ModelRenderer(this);
		leftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
		leftArm.cubeList.add(new ModelBox(leftArm, 40, 16, -1.0F, -2.0F, -2.0F, 4, 12, 4, 0.28F, false));
		leftArm.cubeList.add(new ModelBox(leftArm, 48, 48, 1.16F, -4.155F, -2.0F, 3, 6, 4, 0.26F, false));

		ModelRenderer rightLeg = new ModelRenderer(this);
		rightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
		rightLeg.cubeList.add(new ModelBox(rightLeg, 32, 55, -2.0F, 7.0F, -2.0F, 4, 5, 4, 0.28F, true));
		rightLeg.cubeList.add(new ModelBox(rightLeg, 16, 48, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.26F, true));

		ModelRenderer leftLeg = new ModelRenderer(this);
		leftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
		leftLeg.cubeList.add(new ModelBox(leftLeg, 16, 48, -2.0F, 0.0F, -2.0F, 4, 12, 4, 0.26F, false));
		leftLeg.cubeList.add(new ModelBox(leftLeg, 32, 55, -2.0F, 7.0F, -2.0F, 4, 5, 4, 0.28F, false));

		this.bipedHead = head;
		this.bipedHeadwear = new ModelRenderer(this);
		this.bipedBody = body;
		this.bipedRightArm = rightArm;
		this.bipedLeftArm = leftArm;
		this.bipedRightLeg = rightLeg;
		this.bipedLeftLeg = leftLeg;
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
