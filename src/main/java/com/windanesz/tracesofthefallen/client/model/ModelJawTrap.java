package com.windanesz.tracesofthefallen.client.model;

import com.windanesz.tracesofthefallen.entity.EntityJawTrap;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelJawTrap extends ModelBase {
	private final ModelRenderer jaw_trap;
	private final ModelRenderer a;
	private final ModelRenderer b;

	public ModelJawTrap() {
		textureWidth = 32;
		textureHeight = 32;

		jaw_trap = new ModelRenderer(this);
		jaw_trap.setRotationPoint(0.0F, 24.0F, 0.0F);
		

		a = new ModelRenderer(this);
		a.setRotationPoint(0.0F, 0.0F, -4.0F);
		jaw_trap.addChild(a);
		a.cubeList.add(new ModelBox(a, 5, 4, -2.0F, -0.9F, 0.0F, 4, 1, 4, 0.0F, false));

		b = new ModelRenderer(this);
		b.setRotationPoint(0.0F, 0.0F, 4.0F);
		jaw_trap.addChild(b);
		setRotationAngle(b, 3.1416F, 0.0F, 3.1416F);
		b.cubeList.add(new ModelBox(b, 5, 4, -2.0F, -0.9F, 0.0F, 4, 1, 4, 0.01F, false));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		this.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		jaw_trap.render(f5);
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		if (entityIn instanceof EntityJawTrap && ((EntityJawTrap) entityIn).isSnapped()) {
			this.a.rotateAngleX = 0.0F;
			this.b.rotateAngleX = 3.1416F;
		} else {
			this.a.rotateAngleX = 1.35F;
			this.b.rotateAngleX = 3.1416F - 1.35F;
		}
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}