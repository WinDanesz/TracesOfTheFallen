package com.windanesz.tracesofthefallen.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelWroughtBomb extends ModelBase {
	private final ModelRenderer bb_main;
	private final ModelRenderer fuse_r1;

	public ModelWroughtBomb() {
		textureWidth = 32;
		textureHeight = 32;

		bb_main = new ModelRenderer(this);
		bb_main.setRotationPoint(0.0F, 24.0F, 0.0F);
		bb_main.cubeList.add(new ModelBox(bb_main, 0, 0, -4.0766F, -8.0F, -3.95F, 8, 8, 8, 0.0F, false));
		bb_main.cubeList.add(new ModelBox(bb_main, 0, 16, -2.0766F, -9.0F, -1.95F, 4, 1, 4, 0.0F, false));

		fuse_r1 = new ModelRenderer(this);
		fuse_r1.setRotationPoint(-1.0766F, -9.0F, 0.05F);
		bb_main.addChild(fuse_r1);
		setRotationAngle(fuse_r1, 0.0F, 0.0F, 0.3927F);
		fuse_r1.cubeList.add(new ModelBox(fuse_r1, 16, 16, 0.0F, -3.0F, -1.0F, 2, 3, 2, 0.0F, false));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		bb_main.render(f5);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
