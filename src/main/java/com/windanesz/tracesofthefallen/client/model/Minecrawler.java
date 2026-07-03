package com.windanesz.tracesofthefallen.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

/**
 * Made with Blockbench 5.1.4 by Foreck
 */
public class Minecrawler extends ModelBase {
    private final ModelRenderer abdomen;
    private final ModelRenderer leg_right_1;
    private final ModelRenderer cube_r1;
    private final ModelRenderer leg_left_1;
    private final ModelRenderer cube_r2;
    private final ModelRenderer leg_right_2;
    private final ModelRenderer cube_r3;
    private final ModelRenderer leg_left_2;
    private final ModelRenderer cube_r4;
    private final ModelRenderer torax;
    private final ModelRenderer arm_left;
    private final ModelRenderer cube_r5;
    private final ModelRenderer arm_right;
    private final ModelRenderer cube_r6;
    private final ModelRenderer head;
    private final ModelRenderer cube_r7;
    private final ModelRenderer cube_r8;
    private final ModelRenderer cube_r9;
    private final ModelRenderer madible_left;
    private final ModelRenderer madible_right;

    public Minecrawler() {
        textureWidth = 128;
        textureHeight = 64;

        abdomen = new ModelRenderer(this);
        abdomen.setRotationPoint(0.0F, 16.0F, 15.0F);
        abdomen.cubeList.add(new ModelBox(abdomen, 0, 42, -6.0F, -10.0F, -16.0F, 12, 5, 17, 0.0F, false));
        abdomen.cubeList.add(new ModelBox(abdomen, 58, 40, -5.0F, -9.0F, -15.0F, 10, 9, 15, 0.0F, false));

        leg_right_1 = new ModelRenderer(this);
        leg_right_1.setRotationPoint(-6.0F, -3.0F, -13.3F);
        abdomen.addChild(leg_right_1);
        setRotationAngle(leg_right_1, 0.0F, -0.3927F, -0.3927F);
        leg_right_1.cubeList.add(new ModelBox(leg_right_1, 38, 36, -10.0F, -1.0F, -1.6F, 12, 3, 3, 0.0F, true));

        cube_r1 = new ModelRenderer(this);
        cube_r1.setRotationPoint(-10.7071F, 1.1213F, 7.9F);
        leg_right_1.addChild(cube_r1);
        setRotationAngle(cube_r1, 0.0F, 0.0F, 0.7854F);
        cube_r1.cubeList.add(new ModelBox(cube_r1, 58, 42, -1.0F, -2.0F, -9.0F, 2, 8, 2, 0.0F, true));

        leg_left_1 = new ModelRenderer(this);
        leg_left_1.setRotationPoint(6.0F, -3.0F, -13.3F);
        abdomen.addChild(leg_left_1);
        setRotationAngle(leg_left_1, 0.0F, 0.3927F, 0.3927F);
        leg_left_1.cubeList.add(new ModelBox(leg_left_1, 38, 36, -2.0F, -1.0F, -1.6F, 12, 3, 3, 0.0F, false));

        cube_r2 = new ModelRenderer(this);
        cube_r2.setRotationPoint(10.7071F, 1.1213F, 7.9F);
        leg_left_1.addChild(cube_r2);
        setRotationAngle(cube_r2, 0.0F, 0.0F, -0.7854F);
        cube_r2.cubeList.add(new ModelBox(cube_r2, 58, 42, -1.0F, -2.0F, -9.0F, 2, 8, 2, 0.0F, false));

        leg_right_2 = new ModelRenderer(this);
        leg_right_2.setRotationPoint(-6.0F, -3.0F, -2.6F);
        abdomen.addChild(leg_right_2);
        setRotationAngle(leg_right_2, 0.0F, 0.3927F, -0.3927F);
        leg_right_2.cubeList.add(new ModelBox(leg_right_2, 38, 36, -10.0F, -1.0F, -1.5F, 12, 3, 3, 0.0F, true));

        cube_r3 = new ModelRenderer(this);
        cube_r3.setRotationPoint(-10.7071F, 1.1213F, 8.0F);
        leg_right_2.addChild(cube_r3);
        setRotationAngle(cube_r3, 0.0F, 0.0F, 0.7854F);
        cube_r3.cubeList.add(new ModelBox(cube_r3, 58, 42, -1.0F, -2.0F, -9.0F, 2, 8, 2, 0.0F, true));

        leg_left_2 = new ModelRenderer(this);
        leg_left_2.setRotationPoint(6.0F, -3.0F, -2.6F);
        abdomen.addChild(leg_left_2);
        setRotationAngle(leg_left_2, 0.0F, -0.3927F, 0.3927F);
        leg_left_2.cubeList.add(new ModelBox(leg_left_2, 38, 36, -2.0F, -1.0F, -1.5F, 12, 3, 3, 0.0F, false));

        cube_r4 = new ModelRenderer(this);
        cube_r4.setRotationPoint(10.7071F, 1.1213F, 8.0F);
        leg_left_2.addChild(cube_r4);
        setRotationAngle(cube_r4, 0.0F, 0.0F, -0.7854F);
        cube_r4.cubeList.add(new ModelBox(cube_r4, 58, 42, -1.0F, -2.0F, -9.0F, 2, 8, 2, 0.0F, false));

        torax = new ModelRenderer(this);
        torax.setRotationPoint(0.0F, -2.0F, -15.0F);
        abdomen.addChild(torax);
        setRotationAngle(torax, -0.5236F, 0.0F, 0.0F);
        torax.cubeList.add(new ModelBox(torax, 0, 22, -4.0F, -7.0892F, -10.105F, 8, 8, 11, -0.5F, false));

        arm_left = new ModelRenderer(this);
        arm_left.setRotationPoint(2.6106F, -0.711F, -6.5118F);
        torax.addChild(arm_left);
        setRotationAngle(arm_left, -0.5002F, 0.8584F, -0.2327F);
        arm_left.cubeList.add(new ModelBox(arm_left, 38, 36, 0.2894F, -2.1534F, -1.4333F, 12, 3, 3, 0.0F, false));

        cube_r5 = new ModelRenderer(this);
        cube_r5.setRotationPoint(7.8646F, -5.2193F, 2.4463F);
        arm_left.addChild(cube_r5);
        setRotationAngle(cube_r5, 0.0F, 0.0F, -0.7854F);
        cube_r5.cubeList.add(new ModelBox(cube_r5, 67, 43, -1.0391F, 13.2967F, -2.4796F, 2, 3, 0, 0.0F, false));
        cube_r5.cubeList.add(new ModelBox(cube_r5, 58, 42, -1.0391F, 5.2967F, -3.4796F, 2, 8, 2, 0.0F, false));

        arm_right = new ModelRenderer(this);
        arm_right.setRotationPoint(-2.6106F, -0.711F, -6.5118F);
        torax.addChild(arm_right);
        setRotationAngle(arm_right, -0.5002F, -0.8584F, 0.2327F);
        arm_right.cubeList.add(new ModelBox(arm_right, 38, 36, -12.2894F, -2.1534F, -1.4333F, 12, 3, 3, 0.0F, true));

        cube_r6 = new ModelRenderer(this);
        cube_r6.setRotationPoint(-7.8646F, -5.2193F, 2.4463F);
        arm_right.addChild(cube_r6);
        setRotationAngle(cube_r6, 0.0F, 0.0F, 0.7854F);
        cube_r6.cubeList.add(new ModelBox(cube_r6, 67, 43, -0.9609F, 13.2967F, -2.4796F, 2, 3, 0, 0.0F, true));
        cube_r6.cubeList.add(new ModelBox(cube_r6, 58, 42, -0.9609F, 5.2967F, -3.4796F, 2, 8, 2, 0.0F, true));

        head = new ModelRenderer(this);
        head.setRotationPoint(0.0F, -4.4751F, -9.4023F);
        torax.addChild(head);
        setRotationAngle(head, -0.6109F, 0.0F, 0.0F);


        cube_r7 = new ModelRenderer(this);
        cube_r7.setRotationPoint(0.0F, 0.1819F, 1.6206F);
        head.addChild(cube_r7);
        setRotationAngle(cube_r7, -0.1309F, 0.0F, 0.0F);
        cube_r7.cubeList.add(new ModelBox(cube_r7, 46, 13, -2.0F, 1.7274F, -4.0706F, 4, 4, 3, 0.0F, false));

        cube_r8 = new ModelRenderer(this);
        cube_r8.setRotationPoint(0.0F, -11.0994F, 3.5172F);
        head.addChild(cube_r8);
        setRotationAngle(cube_r8, 0.2618F, 0.0F, 0.0F);
        cube_r8.cubeList.add(new ModelBox(cube_r8, 67, 11, -2.0F, 0.3468F, -10.8701F, 4, 5, 5, 0.0F, false));

        cube_r9 = new ModelRenderer(this);
        cube_r9.setRotationPoint(0.0F, -6.7181F, 2.6206F);
        head.addChild(cube_r9);
        setRotationAngle(cube_r9, 0.2618F, 0.0F, 0.0F);
        cube_r9.cubeList.add(new ModelBox(cube_r9, 13, 6, -4.0F, 1.3468F, -8.8701F, 8, 8, 5, 0.0F, false));

        madible_left = new ModelRenderer(this);
        madible_left.setRotationPoint(2.5936F, -1.0929F, -1.4409F);
        head.addChild(madible_left);
        setRotationAngle(madible_left, 0.0F, 0.0F, -0.3927F);
        madible_left.cubeList.add(new ModelBox(madible_left, 0, 0, -1.4483F, 0.5586F, -1.0F, 3, 7, 2, 0.0F, false));
        madible_left.cubeList.add(new ModelBox(madible_left, 11, 0, -2.4483F, 7.5586F, -1.0F, 3, 2, 2, 0.0F, false));

        madible_right = new ModelRenderer(this);
        madible_right.setRotationPoint(-2.5936F, -1.0929F, -1.4409F);
        head.addChild(madible_right);
        setRotationAngle(madible_right, 0.0F, 0.0F, 0.3927F);
        madible_right.cubeList.add(new ModelBox(madible_right, 0, 0, -1.5517F, 0.5586F, -1.0F, 3, 7, 2, 0.0F, true));
        madible_right.cubeList.add(new ModelBox(madible_right, 11, 0, -0.5517F, 7.5586F, -1.0F, 3, 2, 2, 0.0F, true));
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        abdomen.render(f5);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                                  float headPitch, float scaleFactor, Entity entityIn) {
        final float legSwingAmount = Math.min(1.0F, limbSwingAmount) * 0.75F;
        final float gaitFrequency = 0.9F;

        resetAnimatedPose();

        float groupA = MathHelper.cos(limbSwing * gaitFrequency) * legSwingAmount;
        float groupB = MathHelper.cos(limbSwing * gaitFrequency + (float) Math.PI) * legSwingAmount;

        // Tripod-like 6-leg gait: front-left/right-mid/rear-left alternate with front-right/left-mid/rear-right.
        arm_left.rotateAngleX += groupA * 0.3F;
        leg_right_1.rotateAngleX += groupA * 0.75F;
        leg_left_2.rotateAngleX += groupA * 0.75F;

        arm_right.rotateAngleX += groupB * 0.3F;
        leg_left_1.rotateAngleX += groupB * 0.75F;
        leg_right_2.rotateAngleX += groupB * 0.75F;

        // Subtle body bob for smoother movement.
        abdomen.rotationPointY = 16.0F + MathHelper.cos(limbSwing * gaitFrequency * 2.0F) * legSwingAmount * 0.5F;

        // Attack timeline:
        // 0.0 -> 0.5 : raise and spread frontal arms
        // 0.5 -> 1.0 : strike down while body leans forward
        float attack = this.swingProgress;
        if (attack > 0.0F) {
            float raisePhase = Math.min(attack * 2.0F, 1.0F);
            float strikePhase = Math.max((attack - 0.5F) * 2.0F, 0.0F);

            float raise = MathHelper.sin(raisePhase * ((float) Math.PI * 0.5F));
            float strike = MathHelper.sin(strikePhase * ((float) Math.PI * 0.5F));

            arm_left.rotateAngleX += -0.95F * raise + 1.4F * strike;
            arm_right.rotateAngleX += -0.95F * raise + 1.4F * strike;

            arm_left.rotateAngleY += 0.3F * raise - 0.1F * strike;
            arm_right.rotateAngleY -= 0.3F * raise - 0.1F * strike;

            torax.rotateAngleX += -0.15F * raise + 0.35F * strike;
            head.rotateAngleX += 0.08F * strike;
        }
    }

    private void resetAnimatedPose() {
        abdomen.rotationPointY = 16.0F;

        leg_right_1.rotateAngleX = 0.0F;
        leg_right_1.rotateAngleY = -0.3927F;
        leg_right_1.rotateAngleZ = -0.3927F;

        leg_left_1.rotateAngleX = 0.0F;
        leg_left_1.rotateAngleY = 0.3927F;
        leg_left_1.rotateAngleZ = 0.3927F;

        leg_right_2.rotateAngleX = 0.0F;
        leg_right_2.rotateAngleY = 0.3927F;
        leg_right_2.rotateAngleZ = -0.3927F;

        leg_left_2.rotateAngleX = 0.0F;
        leg_left_2.rotateAngleY = -0.3927F;
        leg_left_2.rotateAngleZ = 0.3927F;

        arm_left.rotateAngleX = 0.6586F;
        arm_left.rotateAngleY = 0.3132F;
        arm_left.rotateAngleZ = 0.9528F;

        arm_right.rotateAngleX = 0.6586F;
        arm_right.rotateAngleY = -0.3132F;
        arm_right.rotateAngleZ = -0.9528F;

        torax.rotateAngleX = -0.5236F;
        torax.rotateAngleY = 0.0F;
        torax.rotateAngleZ = 0.0F;

        head.rotateAngleX = -0.6109F;
    }
}