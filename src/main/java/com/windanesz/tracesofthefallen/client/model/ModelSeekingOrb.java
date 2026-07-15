package com.windanesz.tracesofthefallen.client.model;// Made with Blockbench 5.1.4

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelSeekingOrb extends ModelBase {
    private final ModelRenderer layer1;
    private final ModelRenderer nucleus;
    private final ModelRenderer trail1;
    private final ModelRenderer cube_r1;
    private final ModelRenderer trail2;

    public ModelSeekingOrb() {
        textureWidth = 64;
        textureHeight = 64;

        layer1 = new ModelRenderer(this);
        layer1.setRotationPoint(0.0F, 24.0F, 0.0F);
        setRotationAngle(layer1, 0.9553F, 0.5236F, 0.6155F);
        layer1.cubeList.add(new ModelBox(layer1, 0, 16, -3.0F, -3.0F, -3.0F, 6, 6, 6, 0.0F, false));

        nucleus = new ModelRenderer(this);
        nucleus.setRotationPoint(0.0F, 24.0F, 0.0F);
        nucleus.cubeList.add(new ModelBox(nucleus, 0, 28, -2.0F, -2.0F, -2.0F, 4, 4, 4, 0.0F, false));

        trail1 = new ModelRenderer(this);
        trail1.setRotationPoint(0.0F, 24.0F, 0.0F);


        cube_r1 = new ModelRenderer(this);
        cube_r1.setRotationPoint(0.0F, 0.0F, 0.0F);
        trail1.addChild(cube_r1);
        setRotationAngle(cube_r1, 3.1416F, 0.0F, 0.0F);
        cube_r1.cubeList.add(new ModelBox(cube_r1, -16, 0, -8.0F, 0.0F, -8.0F, 16, 0, 16, 0.0F, false));

        trail2 = new ModelRenderer(this);
        trail2.setRotationPoint(0.0F, 24.0F, 0.0F);

        ModelRenderer trail2_mesh = new ModelRenderer(this);
        trail2_mesh.setRotationPoint(0.0F, 0.0F, 0.0F);
        trail2.addChild(trail2_mesh);
        setRotationAngle(trail2_mesh, 0.0F, 0.0F, 1.5708F);
        trail2_mesh.cubeList.add(new ModelBox(trail2_mesh, -16, 0, -8.0F, 0.0F, -8.0F, 16, 0, 16, 0.0F, false));
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        this.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        layer1.render(f5);
        nucleus.render(f5);
        trail1.render(f5);
        trail2.render(f5);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        float coreAngle = ageInTicks * 0.1178F; // ~6.75 degrees per tick (3x original speed)
        float trailAngle = ageInTicks * 0.4712F; // ~27 degrees per tick (12x original speed)
        nucleus.rotateAngleZ = coreAngle;
        layer1.rotateAngleZ = 0.6155F + coreAngle;
        trail1.rotateAngleX = 0.0F;
        trail1.rotateAngleY = -trailAngle; // rotating on Y axis
        trail1.rotateAngleZ = 0.0F;
        trail2.rotateAngleX = -trailAngle;
        trail2.rotateAngleY = 0.0F;
        trail2.rotateAngleZ = 0.0F;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}