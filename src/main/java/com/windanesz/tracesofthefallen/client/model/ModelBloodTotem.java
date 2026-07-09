package com.windanesz.tracesofthefallen.client.model;// Made with Blockbench 5.1.4

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelBloodTotem extends ModelBase {
	private final ModelRenderer blood_goblin;
	private final ModelRenderer cube_r1;

	public ModelBloodTotem() {
		textureWidth = 32;
		textureHeight = 32;

		blood_goblin = new ModelRenderer(this);
		blood_goblin.setRotationPoint(0.5F, 24.0F, 0.0F);
		blood_goblin.cubeList.add(new ModelBox(blood_goblin, 0, 0, -2.0F, -9.0F, -2.0F, 3, 9, 3, 0.0F, false));
		blood_goblin.cubeList.add(new ModelBox(blood_goblin, 0, 12, -4.0F, -7.0F, -1.0F, 2, 3, 2, 0.0F, false));
		blood_goblin.cubeList.add(new ModelBox(blood_goblin, 9, 0, -1.0F, -7.0F, -3.0F, 1, 2, 1, 0.0F, false));
		blood_goblin.cubeList.add(new ModelBox(blood_goblin, 17, 12, -1.0F, -3.0F, -4.0F, 2, 3, 2, 0.0F, false));

		cube_r1 = new ModelRenderer(this);
		cube_r1.setRotationPoint(2.0F, -5.0F, 0.0F);
		blood_goblin.addChild(cube_r1);
		setRotationAngle(cube_r1, -1.5708F, 0.0F, 0.0F);
		cube_r1.cubeList.add(new ModelBox(cube_r1, 8, 12, -1.0F, 0.0F, -1.0F, 2, 3, 2, 0.0F, false));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		blood_goblin.render(f5);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}