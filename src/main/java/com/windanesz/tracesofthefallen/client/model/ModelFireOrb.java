package com.windanesz.tracesofthefallen.client.model;// Made with Blockbench 5.1.4

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelFireOrb extends ModelBase {
    private final ModelRenderer ball;

    public ModelFireOrb() {
        textureWidth = 16;
        textureHeight = 16;

        ball = new ModelRenderer(this);
        ball.setRotationPoint(0.0F, 24.5F, 0.0F);
        ball.cubeList.add(new ModelBox(ball, 0, 0, -2.0F, -2.5F, -2.5F, 4, 4, 4, 0.0F, false));
        ball.cubeList.add(new ModelBox(ball, 0, 0, 0.0F, -4.5F, -4.5F, 0, 8, 8, 0.0F, false));
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        ball.render(f5);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}