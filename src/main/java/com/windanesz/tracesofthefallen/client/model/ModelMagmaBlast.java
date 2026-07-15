package com.windanesz.tracesofthefallen.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelMagmaBlast extends ModelBase {
	private final ModelRenderer core;
	private final ModelRenderer bulgeX;
	private final ModelRenderer bulgeY;
	private final ModelRenderer bulgeZ;
	private final ModelRenderer crustDiag1;
	private final ModelRenderer crustDiag2;

	public ModelMagmaBlast() {
		this.textureWidth = 16;
		this.textureHeight = 16;

		// 3D Core Sphere Center
		this.core = new ModelRenderer(this);
		this.core.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.core.cubeList.add(new ModelBox(this.core, 0, 0, -4.0F, -4.0F, -4.0F, 8, 8, 8, 0.0F, false));

		// X-axis bulge (thicker along X)
		this.bulgeX = new ModelRenderer(this);
		this.bulgeX.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.bulgeX.cubeList.add(new ModelBox(this.bulgeX, 0, 0, -5.0F, -3.0F, -3.0F, 10, 6, 6, 0.0F, false));

		// Y-axis bulge (thicker along Y)
		this.bulgeY = new ModelRenderer(this);
		this.bulgeY.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.bulgeY.cubeList.add(new ModelBox(this.bulgeY, 0, 0, -3.0F, -5.0F, -3.0F, 6, 10, 6, 0.0F, false));

		// Z-axis bulge (thicker along Z)
		this.bulgeZ = new ModelRenderer(this);
		this.bulgeZ.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.bulgeZ.cubeList.add(new ModelBox(this.bulgeZ, 0, 0, -3.0F, -3.0F, -5.0F, 6, 6, 10, 0.0F, false));

		// Diagonal crust chunks rotated around angles to eliminate any flat look
		this.crustDiag1 = new ModelRenderer(this);
		this.crustDiag1.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.crustDiag1.rotateAngleX = 0.7854F; // 45 degrees
		this.crustDiag1.rotateAngleY = 0.7854F;
		this.crustDiag1.cubeList.add(new ModelBox(this.crustDiag1, 0, 0, -3.5F, -3.5F, -3.5F, 7, 7, 7, 0.0F, false));

		this.crustDiag2 = new ModelRenderer(this);
		this.crustDiag2.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.crustDiag2.rotateAngleX = -0.7854F;
		this.crustDiag2.rotateAngleZ = 0.7854F;
		this.crustDiag2.cubeList.add(new ModelBox(this.crustDiag2, 0, 0, -3.5F, -3.5F, -3.5F, 7, 7, 7, 0.0F, false));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float scale) {
		this.core.render(scale);
		this.bulgeX.render(scale);
		this.bulgeY.render(scale);
		this.bulgeZ.render(scale);
		this.crustDiag1.render(scale);
		this.crustDiag2.render(scale);
	}
}
