package com.windanesz.tracesofthefallen.client.model;

import com.windanesz.tracesofthefallen.entity.EntityGoblin;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;

public abstract class ModelGoblinBase extends ModelBiped {
	protected ModelRenderer body;
	protected ModelRenderer head;
	protected ModelRenderer arm_left;
	protected ModelRenderer arm_right;
	protected ModelRenderer leg_left;
	protected ModelRenderer leg_right;

	protected void initBiped() {
		this.bipedHead = this.head;
		this.bipedBody = this.body;
		this.bipedRightArm = this.arm_right;
		this.bipedLeftArm = this.arm_left;
		this.bipedRightLeg = this.leg_right;
		this.bipedLeftLeg = this.leg_left;
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		this.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		GlStateManager.pushMatrix();
		if (entity.isSneaking()) {
			GlStateManager.translate(0.0F, 0.2F, 0.0F);
		}
		if (this.body != null) {
			this.body.render(f5);
		}
		GlStateManager.popMatrix();
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		if (this.head != null) {
			this.head.rotateAngleY = netHeadYaw * 0.017453292F;
			this.head.rotateAngleX = headPitch * 0.017453292F;
		}

		float f = 1.0F;
		if (this.arm_right != null) {
			this.arm_right.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F;
			this.arm_right.rotateAngleZ = 0.0F;
			this.arm_right.rotateAngleY = 0.0F;
		}
		if (this.arm_left != null) {
			this.arm_left.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
			this.arm_left.rotateAngleZ = 0.0F;
			this.arm_left.rotateAngleY = 0.0F;
		}
		if (this.leg_right != null) {
			this.leg_right.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
			this.leg_right.rotateAngleY = 0.0F;
			this.leg_right.rotateAngleZ = 0.0F;
		}
		if (this.leg_left != null) {
			this.leg_left.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
			this.leg_left.rotateAngleY = 0.0F;
			this.leg_left.rotateAngleZ = 0.0F;
		}

		if (this.isRiding) {
			if (this.arm_right != null) this.arm_right.rotateAngleX += -((float) Math.PI / 5F);
			if (this.arm_left != null) this.arm_left.rotateAngleX += -((float) Math.PI / 5F);
			if (this.leg_right != null) {
				this.leg_right.rotateAngleX = -1.4137167F;
				this.leg_right.rotateAngleY = ((float) Math.PI / 10F);
				this.leg_right.rotateAngleZ = 0.07853982F;
			}
			if (this.leg_left != null) {
				this.leg_left.rotateAngleX = -1.4137167F;
				this.leg_left.rotateAngleY = -((float) Math.PI / 10F);
				this.leg_left.rotateAngleZ = -0.07853982F;
			}
		}

		if (this.swingProgress > 0.0F && this.body != null) {
			EnumHandSide enumhandside = this.getMainHand(entityIn);
			ModelRenderer modelrenderer = this.getArmForSide(enumhandside);
			if (modelrenderer != null) {
				float f1 = this.swingProgress;
				this.body.rotateAngleY = MathHelper.sin(MathHelper.sqrt(f1) * ((float) Math.PI * 2F)) * 0.2F;

				if (enumhandside == EnumHandSide.LEFT) {
					this.body.rotateAngleY *= -1.0F;
				}

				f1 = 1.0F - this.swingProgress;
				f1 = f1 * f1;
				f1 = f1 * f1;
				f1 = 1.0F - f1;
				float f2 = MathHelper.sin(f1 * (float) Math.PI);
				float f3 = MathHelper.sin(this.swingProgress * (float) Math.PI) * -(this.head != null ? (this.head.rotateAngleX - 0.7F) : 0.0F) * 0.75F;
				modelrenderer.rotateAngleX = (float) ((double) modelrenderer.rotateAngleX - ((double) f2 * 1.2D + (double) f3));
				modelrenderer.rotateAngleY += this.body.rotateAngleY * 2.0F;
				modelrenderer.rotateAngleZ += MathHelper.sin(this.swingProgress * (float) Math.PI) * -0.4F;
			}
		} else if (this.body != null) {
			this.body.rotateAngleY = 0.0F;
		}

		if (this.isSneak) {
			if (this.body != null) this.body.rotateAngleX = 0.5F;
			if (this.arm_right != null) this.arm_right.rotateAngleX += 0.4F;
			if (this.arm_left != null) this.arm_left.rotateAngleX += 0.4F;
		} else {
			if (this.body != null) this.body.rotateAngleX = 0.0F;
		}

		if (this.arm_right != null) {
			this.arm_right.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
			this.arm_right.rotateAngleX += MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
		}
		if (this.arm_left != null) {
			this.arm_left.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
			this.arm_left.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
		}

		if (entityIn.getClass() == EntityGoblin.class && ((EntityGoblin) entityIn).isCarryingBomb()) {
			if (this.arm_right != null) {
				this.arm_right.rotateAngleX = (float) -Math.PI;
				this.arm_right.rotateAngleY = 0.0F;
				this.arm_right.rotateAngleZ = 0.05F;
				this.arm_right.offsetY = -0.125F;
			}
			if (this.arm_left != null) {
				this.arm_left.rotateAngleX = (float) -Math.PI;
				this.arm_left.rotateAngleY = 0.0F;
				this.arm_left.rotateAngleZ = -0.05F;
				this.arm_left.offsetY = -0.125F;
			}
		} else {
			if (this.arm_right != null) {
				this.arm_right.offsetY = 0.0F;
			}
			if (this.arm_left != null) {
				this.arm_left.offsetY = 0.0F;
			}
		}
	}

	@Override
	public void postRenderArm(float scale, EnumHandSide side) {
		if (this.body != null) {
			this.body.postRender(scale);
		}
		ModelRenderer arm = this.getArmForSide(side);
		if (arm != null) {
			arm.postRender(scale);
		}
		GlStateManager.translate(0.0F, -0.1875F, 0.0F);
	}

	@Override
	protected ModelRenderer getArmForSide(EnumHandSide side) {
		return side == EnumHandSide.LEFT ? this.arm_left : this.arm_right;
	}

	protected EnumHandSide getMainHand(Entity entityIn) {
		if (entityIn instanceof EntityLivingBase) {
			EntityLivingBase entitylivingbase = (EntityLivingBase) entityIn;
			EnumHandSide enumhandside = entitylivingbase.getPrimaryHand();
			return entitylivingbase.swingingHand == EnumHand.MAIN_HAND ? enumhandside : enumhandside.opposite();
		} else {
			return EnumHandSide.RIGHT;
		}
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
