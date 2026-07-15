package com.windanesz.tracesofthefallen.client.model;

import net.minecraft.client.model.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;

public class ModelVoidSlash extends ModelBase {
	private final ModelRenderer quad1Parent;
	private final ModelRenderer quad1Child;
	private final ModelRenderer quad2Parent;
	private final ModelRenderer quad2Child;

	public ModelVoidSlash() {
		textureWidth = 32;
		textureHeight = 192;

		quad1Parent = new ModelRenderer(this);
		quad1Parent.setRotationPoint(0.0F, -3.0F, -10.0F);
		setRotationAngle(quad1Parent, 0.0F, -0.7854F, 0.0F);

		quad1Child = new ModelRenderer(this);
		quad1Child.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotationAngle(quad1Child, 0.0F, 0.0F, 0.0F);
		quad1Child.cubeList.add(new QuadBox(quad1Child));
		quad1Parent.addChild(quad1Child);

		quad2Parent = new ModelRenderer(this);
		quad2Parent.setRotationPoint(0.0F, -3.0F, -10.0F);
		setRotationAngle(quad2Parent, 0.0F, 0.7854F, 0.0F);

		quad2Child = new ModelRenderer(this);
		quad2Child.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotationAngle(quad2Child, 0.0F, 0.0F, 0.0F);
		quad2Child.cubeList.add(new QuadBox(quad2Child));
		quad2Parent.addChild(quad2Child);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		quad1Parent.render(f5);
		quad2Parent.render(f5);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}

	public static class QuadBox extends ModelBox {
		private final TexturedQuad quad;

		public QuadBox(ModelRenderer renderer) {
			super(renderer, 0, 0, 0.0F, 0.0F, 0.0F, 0, 0, 0, 0.0F, false);
			PositionTextureVertex[] vertices = new PositionTextureVertex[4];
			float vMax = 32.0F / 192.0F;
			vertices[0] = new PositionTextureVertex(0.0F, -16.0F, -2.0F, 1.0F, 0.0F);
			vertices[1] = new PositionTextureVertex(0.0F, -16.0F, 30.0F, 0.0F, 0.0F);
			vertices[2] = new PositionTextureVertex(0.0F, 16.0F, 30.0F, 0.0F, vMax);
			vertices[3] = new PositionTextureVertex(0.0F, 16.0F, -2.0F, 1.0F, vMax);
			this.quad = new TexturedQuad(vertices);
		}

		@Override
		public void render(BufferBuilder buffer, float scale) {
			this.quad.draw(buffer, scale);
		}
	}
}
