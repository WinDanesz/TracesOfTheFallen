package com.windanesz.tracesofthefallen.client.model;

import com.windanesz.tracesofthefallen.entity.EntityLost;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.DoubleBuffer;

public class ModelLost extends ModelBase {
	private final ModelRenderer body;
	private final ModelRenderer head_body;
	private final ModelRenderer eyes_closed;
	private final ModelRenderer eyes_open;
	private final ModelRenderer mouth_closed;
	private final ModelRenderer mouth_open;
	
	private final ModelRenderer arm_right;
	private final ModelRenderer forearm_right;
	private final ModelRenderer arm_left;
	private final ModelRenderer forearm_left;
	private final ModelRenderer leg_right;
	private final ModelRenderer leg_left;

	public ModelLost() {
		textureWidth = 48;
		textureHeight = 48;

		body = new ModelRenderer(this);
		body.setRotationPoint(0.0F, 26.0F, 1.0F);

		// head_body acts as the torso. It holds the torso box, eyes/head, arms.
		head_body = new ModelRenderer(this);
		head_body.setRotationPoint(0.0F, 0.0F, 0.0F);
		body.addChild(head_body);
		
		// Torso box
		head_body.cubeList.add(new ModelBox(head_body, 28, 0, -3.0F, -19.0F, -3.0F, 6, 12, 4, 0.0F, false));

		// Eyes Closed variant (contains closed face UVs and closed mouth)
		eyes_closed = new ModelRenderer(this);
		eyes_closed.setRotationPoint(0.0F, -1.0F, 0.0F);
		eyes_closed.cubeList.add(new ModelBox(eyes_closed, 21, 40, -3.0F, -20.0F, -3.1F, 6, 2, 6, 0.0F, false));
		eyes_closed.cubeList.add(new ModelBox(eyes_closed, 20, 16, -4.0F, -24.0F, -3.1F, 8, 4, 6, 0.0F, false));

		// Eyes Open variant (contains open face UVs and closed mouth)
		eyes_open = new ModelRenderer(this);
		eyes_open.setRotationPoint(0.0F, -1.0F, 0.0F);
		eyes_open.cubeList.add(new ModelBox(eyes_open, 21, 40, -3.0F, -20.0F, -3.1F, 6, 2, 6, 0.0F, false));
		eyes_open.cubeList.add(new ModelBox(eyes_open, 20, 28, -4.0F, -24.0F, -3.1F, 8, 4, 6, 0.0F, false));

		// Mouth Closed variant (the small 4-pixel tall mouth)
		mouth_closed = new ModelRenderer(this);
		mouth_closed.setRotationPoint(0.0F, 0.0F, 0.0F);
		mouth_closed.cubeList.add(new ModelBox(mouth_closed, 0, 9, -4.0F, -19.0F, -3.1F, 8, 4, 0, 0.0F, false));

		// Mouth Open variant (the elongated 9-pixel tall mouth)
		mouth_open = new ModelRenderer(this);
		mouth_open.setRotationPoint(0.0F, -19.0F, -3.1F); // Pivot at the top of the mouth
		mouth_open.cubeList.add(new ModelBox(mouth_open, 0, 0, -4.0F, 0.0F, 0.0F, 8, 9, 0, 0.0F, false));

		arm_right = new ModelRenderer(this);
		arm_right.setRotationPoint(-4.0F, -14.0F, -0.5F);
		head_body.addChild(arm_right);
		setRotationAngle(arm_right, 0.7854F, 0.0F, 0.0F);
		arm_right.cubeList.add(new ModelBox(arm_right, 0, 38, -2.0F, -1.0F, -1.5F, 3, 7, 3, 0.0F, true));

		forearm_right = new ModelRenderer(this);
		forearm_right.setRotationPoint(0.0F, 6.0F, -1.5F);
		arm_right.addChild(forearm_right);
		setRotationAngle(forearm_right, -1.5708F, 0.0F, 0.0F);
		forearm_right.cubeList.add(new ModelBox(forearm_right, 0, 30, -2.0F, 0.0F, -3.0F, 3, 5, 3, 0.0F, true));
		forearm_right.cubeList.add(new ModelBox(forearm_right, 0, 24, -2.0F, 5.0F, -3.0F, 3, 3, 3, 0.0F, true));

		arm_left = new ModelRenderer(this);
		arm_left.setRotationPoint(4.0F, -14.0F, -0.5F);
		head_body.addChild(arm_left);
		setRotationAngle(arm_left, 0.7854F, 0.0F, 0.0F);
		arm_left.cubeList.add(new ModelBox(arm_left, 0, 38, -1.0F, -1.0F, -1.5F, 3, 7, 3, 0.0F, false));

		forearm_left = new ModelRenderer(this);
		forearm_left.setRotationPoint(0.0F, 6.0F, 0.5F);
		arm_left.addChild(forearm_left);
		setRotationAngle(forearm_left, -1.5708F, 0.0F, 0.0F);
		forearm_left.cubeList.add(new ModelBox(forearm_left, 0, 30, -1.0F, 2.0F, -3.0F, 3, 5, 3, 0.0F, false));
		forearm_left.cubeList.add(new ModelBox(forearm_left, 0, 24, -1.0F, 7.0F, -3.0F, 3, 3, 3, 0.0F, false));

		// Cleaned up leg pivots (attached at the hip at y = -7.0F)
		leg_right = new ModelRenderer(this);
		leg_right.setRotationPoint(-1.5F, -7.0F, -1.0F);
		body.addChild(leg_right);
		leg_right.cubeList.add(new ModelBox(leg_right, 0, 30, -1.5F, 0.0F, -1.5F, 3, 5, 3, 0.0F, true));

		leg_left = new ModelRenderer(this);
		leg_left.setRotationPoint(1.5F, -7.0F, -1.0F);
		body.addChild(leg_left);
		leg_left.cubeList.add(new ModelBox(leg_left, 0, 30, -1.5F, 0.0F, -1.5F, 3, 5, 3, 0.0F, false));
	}

	private float transitionProgress;

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		body.render(f5);
		
		GlStateManager.pushMatrix();
		
		// Replicate the exact transform of the body and head_body
		this.body.postRender(f5);
		this.head_body.postRender(f5);
		
		// Always render the closed face as a solid base so the head never becomes transparent!
		GlStateManager.pushMatrix();
		this.eyes_closed.render(f5);
		this.mouth_closed.render(f5);
		GlStateManager.popMatrix();
		
		// Render open face wiping down over the top
		if (this.transitionProgress > 0.0F) {
			GlStateManager.pushMatrix();
			
			// Slightly scale up to completely engulf the closed face without any Z-fighting on the sides
			GlStateManager.scale(1.001F, 1.001F, 1.001F);
			// Minor translation to offset the scale pivot
			GlStateManager.translate(0.0F, -0.001F, 0.0F);
			
			// Bottom-up wipe for the eyes (reveals from Y = -18 UP to Y = -24)
			float eyesBottomY = -18.0F * f5;
			float eyesTopY = -24.0F * f5;
			float eyesD = eyesBottomY + (eyesTopY - eyesBottomY) * this.transitionProgress;
			
			// Plane equation: Y - D > 0 => B=1, D=-eyesD
			DoubleBuffer bufferEyes = BufferUtils.createDoubleBuffer(4);
			bufferEyes.put(0.0D).put(1.0D).put(0.0D).put((double) -eyesD).flip();
			
			GL11.glEnable(GL11.GL_CLIP_PLANE1);
			GL11.glClipPlane(GL11.GL_CLIP_PLANE1, bufferEyes);
			this.eyes_open.render(f5);
			
			// Top-down wipe for the mouth (reveals from Y = -19 DOWN to Y = -10)
			float mouthTopY = -19.0F * f5;
			float mouthBottomY = -10.0F * f5;
			float mouthD = mouthTopY + (mouthBottomY - mouthTopY) * this.transitionProgress;
			
			// Plane equation: -Y + D > 0 => B=-1, D=mouthD
			DoubleBuffer bufferMouth = BufferUtils.createDoubleBuffer(4);
			bufferMouth.put(0.0D).put(-1.0D).put(0.0D).put((double) mouthD).flip();
			
			GL11.glClipPlane(GL11.GL_CLIP_PLANE1, bufferMouth);
			this.mouth_open.render(f5);
			
			GL11.glDisable(GL11.GL_CLIP_PLANE1);
			
			GlStateManager.popMatrix();
		}
		
		GlStateManager.popMatrix();
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
		
		// Rotate the entire upper body (torso + head + arms) horizontally based on look direction.
		// (Vertical pitch bending is removed so the Lost stays upright)
		this.head_body.rotateAngleY = netHeadYaw * 0.017453292F;
		this.head_body.rotateAngleX = 0.0F;

		if (entityIn instanceof EntityLost) {
			EntityLost lost = (EntityLost) entityIn;
			
			// For arms: use raw attacking state
			boolean isAttacking = lost.isAttacking() && lost.hurtFaceTicks == 0;
			
			// Calculate smooth face transition progress
			float partialTicks = ageInTicks - entityIn.ticksExisted;
			this.transitionProgress = lost.prevMouthOpenProgress + (lost.mouthOpenProgress - lost.prevMouthOpenProgress) * partialTicks;
			
			// We no longer toggle visibility here since we handle it smoothly in render()
			// (The parts were removed from the head_body children list so they won't automatically render)
			
			// Arm animation
			if (isAttacking) {
				if (entityIn instanceof EntityLost && !entityIn.getPassengers().isEmpty()) {
					int grabTicks = ((EntityLost) entityIn).grabTicks;
					float progress;
					if (grabTicks <= 30) {
						progress = 0.0F;
					} else {
						progress = Math.min(1.0F, (grabTicks - 30) / 40.0F);
					}
					// Arms point forward (-0.7854F), and swing INWARDS towards the body
					this.arm_right.rotateAngleX = -0.7854F;
					this.arm_left.rotateAngleX = -0.7854F;
					
					this.arm_right.rotateAngleY = -(0.5F * progress);
					this.arm_left.rotateAngleY = (0.5F * progress);
				} else {
					// Screaming / attacking: arms 45 degrees upwards
					this.arm_right.rotateAngleX = -0.7854F;
					this.arm_left.rotateAngleX = -0.7854F;
					
					// Apply melee slash animation (double-handed downward strike)
					if (this.swingProgress > 0.0F) {
						float swingMath = MathHelper.sin(this.swingProgress * (float)Math.PI);
						this.arm_right.rotateAngleX += swingMath * 1.8F;
						this.arm_left.rotateAngleX += swingMath * 1.8F;
						
						// Add a slight inward twist during the slash
						this.arm_right.rotateAngleY = -(0.2F * swingMath);
						this.arm_left.rotateAngleY = (0.2F * swingMath);
					} else if (lost.isExcited()) {
						// Faster variant of the inward arm motion to show excitement for a meal
						float exciteProgress = (MathHelper.sin(ageInTicks * 0.4F) + 1.0F) / 2.0F; // 0.0 to 1.0
						this.arm_right.rotateAngleY = -(0.3F * exciteProgress);
						this.arm_left.rotateAngleY = (0.3F * exciteProgress);
					} else {
						this.arm_right.rotateAngleY = 0.0F;
						this.arm_left.rotateAngleY = 0.0F;
					}
				}
			} else {
				// Resting lowered position
				this.arm_right.rotateAngleX = 0.7854F;
				this.arm_left.rotateAngleX = 0.7854F;
				this.arm_right.rotateAngleY = 0.0F;
				this.arm_left.rotateAngleY = 0.0F;
			}

			// Leg animation (standard walking)
			this.leg_right.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
			this.leg_left.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
		}
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}