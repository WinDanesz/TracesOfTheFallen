package com.windanesz.tracesofthefallen.client.model;

import com.windanesz.tracesofthefallen.entity.EntityGoblinShaman;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelGoblinShaman extends ModelGoblinBase {
	private final ModelRenderer featherBase;
	public ModelRenderer featherLeft;
	public ModelRenderer featherRight;
	public ModelRenderer featherCenter;
	public ModelRenderer featherFarLeft;
	public ModelRenderer featherFarRight;
	public ModelRenderer featherFarFarLeft;

	public ModelGoblinShaman() {
		textureWidth = 48;
		textureHeight = 48;

		body = new ModelRenderer(this);
		body.setRotationPoint(0.0F, 25.0F, 0.0F);
		body.cubeList.add(new ModelBox(body, 27, 12, -2.9503F, -15.0F, -1.9929F, 6, 8, 3, 0.01F, false));

		head = new ModelRenderer(this);
		head.setRotationPoint(0.0F, -15.0F, -0.5F);
		body.addChild(head);
		head.cubeList.add(new ModelBox(head, 0, 0, -2.9503F, -6.0F, -2.9929F, 6, 6, 6, 0.0F, false));
		head.cubeList.add(new ModelBox(head, 22, 26, -0.9503F, -3.0F, -5.9929F, 2, 3, 3, 0.0F, false));

		featherBase = new ModelRenderer(this);
		featherBase.setRotationPoint(-0.2503F, -3.3F, 3.7071F);
		head.addChild(featherBase);
		featherBase.cubeList.add(new ModelBox(featherBase, 30, 23, -2.6912F, -2.8224F, 1.491F, 6, 9, 0, 0.0F, false));

		featherLeft = new ModelRenderer(this);
		featherLeft.setRotationPoint(-0.1912F, -2.8224F, 1.491F);
		featherBase.addChild(featherLeft);
		setRotationAngle(featherLeft, 0.0436F, 0.0F, -0.3054F);
		featherLeft.cubeList.add(new ModelBox(featherLeft, 40, 0, -2.0F, -11.0F, 0.0F, 4, 11, 0, 0.0F, false));

		featherRight = new ModelRenderer(this);
		featherRight.setRotationPoint(-0.1912F, -2.8224F, 1.491F);
		featherBase.addChild(featherRight);
		setRotationAngle(featherRight, -0.0436F, 0.0F, 0.48F);
		featherRight.cubeList.add(new ModelBox(featherRight, 32, 0, -2.0F, -11.0F, 0.0F, 4, 11, 0, 0.0F, false));

		featherCenter = new ModelRenderer(this);
		featherCenter.setRotationPoint(-0.1912F, -2.8224F, 1.491F);
		featherBase.addChild(featherCenter);
		setRotationAngle(featherCenter, 0.0F, 0.0F, 0.0873F);
		featherCenter.cubeList.add(new ModelBox(featherCenter, 12, 12, -2.0F, -11.0F, 0.0F, 4, 11, 0, 0.0F, false));

		featherFarLeft = new ModelRenderer(this);
		featherFarLeft.setRotationPoint(-0.1912F, -2.8224F, 1.491F);
		featherBase.addChild(featherFarLeft);
		setRotationAngle(featherFarLeft, 0.08F, 0.0F, -0.65F);
		featherFarLeft.cubeList.add(new ModelBox(featherFarLeft, 40, 0, -2.0F, -11.0F, 0.0F, 4, 11, 0, 0.0F, false));

		featherFarRight = new ModelRenderer(this);
		featherFarRight.setRotationPoint(-0.1912F, -2.8224F, 1.491F);
		featherBase.addChild(featherFarRight);
		setRotationAngle(featherFarRight, -0.08F, 0.0F, 0.85F);
		featherFarRight.cubeList.add(new ModelBox(featherFarRight, 32, 0, -2.0F, -11.0F, 0.0F, 4, 11, 0, 0.0F, false));

		featherFarFarLeft = new ModelRenderer(this);
		featherFarFarLeft.setRotationPoint(-0.1912F, -2.8224F, 1.491F);
		featherBase.addChild(featherFarFarLeft);
		setRotationAngle(featherFarFarLeft, 0.12F, 0.0F, -0.95F);
		featherFarFarLeft.cubeList.add(new ModelBox(featherFarFarLeft, 12, 12, -2.0F, -11.0F, 0.0F, 4, 11, 0, 0.0F, false));

		ModelRenderer headpiece = new ModelRenderer(this);
		headpiece.setRotationPoint(0.7F, -0.1F, 0.0F);
		featherBase.addChild(headpiece);
		setRotationAngle(headpiece, -0.48F, 0.0F, 0.0F);
		headpiece.cubeList.add(new ModelBox(headpiece, 16, 32, -4.3912F, -3.1037F, -7.9336F, 8, 8, 8, 0.0F, false));

		ModelRenderer leftEar = new ModelRenderer(this);
		leftEar.setRotationPoint(3.0497F, -3.0F, -1.9929F);
		head.addChild(leftEar);
		setRotationAngle(leftEar, -0.3927F, 0.0F, 0.0F);

		ModelRenderer leftEarFlap = new ModelRenderer(this);
		leftEarFlap.setRotationPoint(0.0F, -1.0F, 0.0F);
		leftEar.addChild(leftEarFlap);
		setRotationAngle(leftEarFlap, 0.0F, 0.48F, 0.0F);
		leftEarFlap.cubeList.add(new ModelBox(leftEarFlap, 18, -6, 0.0F, -1.0F, 0.0F, 0, 4, 6, 0.0F, false));

		ModelRenderer rightEar = new ModelRenderer(this);
		rightEar.setRotationPoint(-2.9503F, -3.0F, -1.9929F);
		head.addChild(rightEar);
		setRotationAngle(rightEar, -0.3927F, 0.0F, 0.0F);

		ModelRenderer rightEarFlap = new ModelRenderer(this);
		rightEarFlap.setRotationPoint(0.0F, -1.0F, 0.0F);
		rightEar.addChild(rightEarFlap);
		setRotationAngle(rightEarFlap, 0.0F, -0.48F, 0.0F);
		rightEarFlap.cubeList.add(new ModelBox(rightEarFlap, 18, -6, 0.0F, -1.0F, 0.0F, 0, 4, 6, 0.0F, true));

		leg_left = new ModelRenderer(this);
		leg_left.setRotationPoint(1.5F, -7.0F, -0.5F);
		body.addChild(leg_left);
		leg_left.cubeList.add(new ModelBox(leg_left, 0, 22, -1.4503F, 0.0F, -1.4929F, 3, 6, 3, 0.0F, false));

		leg_right = new ModelRenderer(this);
		leg_right.setRotationPoint(-1.5F, -7.0F, 0.0F);
		body.addChild(leg_right);
		leg_right.cubeList.add(new ModelBox(leg_right, 0, 22, -1.4503F, 0.0F, -1.9929F, 3, 6, 3, 0.0F, true));

		arm_left = new ModelRenderer(this);
		arm_left.setRotationPoint(3.0497F, -14.0F, 0.0071F);
		body.addChild(arm_left);
		arm_left.cubeList.add(new ModelBox(arm_left, 0, 12, 0.0F, -1.0F, -2.0F, 3, 7, 3, 0.0F, false));

		arm_right = new ModelRenderer(this);
		arm_right.setRotationPoint(-2.9503F, -14.0F, 0.0071F);
		body.addChild(arm_right);
		arm_right.cubeList.add(new ModelBox(arm_right, 0, 12, -3.0F, -1.0F, -2.0F, 3, 7, 3, 0.0F, true));

		initBiped();
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
		if (this.head != null) {
			this.head.rotateAngleZ = 0.0F;
		}
		if (this.body != null) {
			this.body.rotateAngleX = 0.0F;
			this.body.rotateAngleZ = 0.0F;
		}
		if (this.featherBase != null) {
			this.featherBase.rotateAngleX = 0.0F;
			this.featherBase.rotateAngleY = 0.0F;
			this.featherBase.rotateAngleZ = 0.0F;
		}

		if (entityIn instanceof EntityGoblinShaman) {
			int count = ((EntityGoblinShaman) entityIn).getFeatherCount();
			if (this.featherCenter != null) this.featherCenter.showModel = (count == 1 || count == 3 || count >= 5);
			if (this.featherLeft != null) this.featherLeft.showModel = (count >= 2);
			if (this.featherRight != null) this.featherRight.showModel = (count >= 2);
			if (this.featherFarLeft != null) this.featherFarLeft.showModel = (count >= 4);
			if (this.featherFarRight != null) this.featherFarRight.showModel = (count >= 4);
			if (this.featherFarFarLeft != null) this.featherFarFarLeft.showModel = (count >= 6);
		}

		if (entityIn instanceof EntityGoblinShaman && ((EntityGoblinShaman) entityIn).getSpellCastingTimer() > 0) {
			int castTimer = ((EntityGoblinShaman) entityIn).getSpellCastingTimer();
			int castType = ((EntityGoblinShaman) entityIn).getSpellCastType();
			if (castType == 3) {
				float raiseProgress = Math.min(1.0F, (40.0F - (float)castTimer) / 6.0F);
				float armPitch = -2.0F * raiseProgress;
				float wave = MathHelper.sin(ageInTicks * 0.5F) * 0.15F;
				if (this.arm_right != null) {
					this.arm_right.rotateAngleX = armPitch + wave;
					this.arm_right.rotateAngleZ = -0.8F - wave;
					this.arm_right.rotateAngleY = -0.3F;
				}
				if (this.arm_left != null) {
					this.arm_left.rotateAngleX = armPitch + wave;
					this.arm_left.rotateAngleZ = 0.8F + wave;
					this.arm_left.rotateAngleY = 0.3F;
				}
				if (this.head != null) {
					this.head.rotateAngleX = -0.3F;
				}
			} else if (castType == 2 || (castType == 1 && castTimer > 10)) {
				float maxTimer = (castType == 2) ? 70.0F : 30.0F;
				float raiseProgress = Math.min(1.0F, (maxTimer - (float)castTimer) / 6.0F);
				float armPitch = -2.8F * raiseProgress;
				float wave = MathHelper.sin(ageInTicks * 0.4F) * 0.1F;
				if (this.arm_right != null) {
					this.arm_right.rotateAngleX = armPitch + wave;
					this.arm_right.rotateAngleZ = -0.3F - wave;
					this.arm_right.rotateAngleY = 0.0F;
				}
				if (this.arm_left != null) {
					this.arm_left.rotateAngleX = armPitch + wave;
					this.arm_left.rotateAngleZ = 0.3F + wave;
					this.arm_left.rotateAngleY = 0.0F;
				}
				if (this.head != null) {
					this.head.rotateAngleX = -0.4F;
				}
			} else if (castType == 1) {
				float pointPitch = -1.5F;
				if (this.arm_right != null) {
					this.arm_right.rotateAngleX = pointPitch;
					this.arm_right.rotateAngleZ = -0.15F;
					this.arm_right.rotateAngleY = -0.2F;
				}
				if (this.arm_left != null) {
					this.arm_left.rotateAngleX = pointPitch;
					this.arm_left.rotateAngleZ = 0.15F;
					this.arm_left.rotateAngleY = 0.2F;
				}
			} else if (castType == 4) {
				float rattleX = -1.2F + MathHelper.cos(ageInTicks * 0.8F) * 0.25F;
										float rattleZ = MathHelper.sin(ageInTicks) * 0.35F;
				if (this.arm_right != null) {
					this.arm_right.rotateAngleX = rattleX;
					this.arm_right.rotateAngleZ = rattleZ;
					this.arm_right.rotateAngleY = 0.0F;
				}
				if (this.arm_left != null) {
					this.arm_left.rotateAngleX = -0.5F;
					this.arm_left.rotateAngleZ = 0.2F;
				}
				if (this.head != null) {
					this.head.rotateAngleX = -0.2F + MathHelper.sin(ageInTicks * 0.8F) * 0.1F;
				}
			} else if (castType == 5 && castTimer > 10) {
				float floatX = -1.3F + MathHelper.sin(ageInTicks * 0.2F) * 0.15F;
				if (this.arm_right != null) {
					this.arm_right.rotateAngleX = floatX;
					this.arm_right.rotateAngleZ = -0.2F;
					this.arm_right.rotateAngleY = -0.1F;
				}
				if (this.arm_left != null) {
					this.arm_left.rotateAngleX = floatX;
					this.arm_left.rotateAngleZ = 0.2F;
					this.arm_left.rotateAngleY = 0.1F;
				}
				if (this.head != null) {
					this.head.rotateAngleX = -0.3F;
				}
			} else if (castType == 5) {
				float pointPitch = -1.5F;
				if (this.arm_right != null) {
					this.arm_right.rotateAngleX = pointPitch;
					this.arm_right.rotateAngleZ = -0.15F;
					this.arm_right.rotateAngleY = -0.2F;
				}
				if (this.arm_left != null) {
					this.arm_left.rotateAngleX = pointPitch;
					this.arm_left.rotateAngleZ = 0.15F;
					this.arm_left.rotateAngleY = 0.2F;
				}
			} else if (castType == 7) {
				if (castTimer > 15) {
					float raiseProgress = Math.min(1.0F, (45.0F - (float)castTimer) / 10.0F);
					float armPitch = -2.8F * raiseProgress;
					float wave = MathHelper.sin(ageInTicks * 0.6F) * 0.1F;
					if (this.arm_right != null) {
						this.arm_right.rotateAngleX = armPitch + wave;
						this.arm_right.rotateAngleZ = -0.3F - wave;
						this.arm_right.rotateAngleY = 0.0F;
					}
					if (this.arm_left != null) {
						this.arm_left.rotateAngleX = armPitch + wave;
						this.arm_left.rotateAngleZ = 0.3F + wave;
						this.arm_left.rotateAngleY = 0.0F;
					}
					if (this.head != null) {
						this.head.rotateAngleX = -0.35F + wave * 0.5F;
					}
				} else {
					float slamProgress = Math.min(1.0F, (15.0F - (float)castTimer) / 3.0F);
					if (this.body != null) {
						this.body.rotateAngleX = 0.85F * slamProgress;
					}
					if (this.head != null) {
						this.head.rotateAngleX = -0.5F * slamProgress;
					}
					float rumble = MathHelper.sin(ageInTicks * 1.5F) * 0.04F;
					if (this.arm_right != null) {
						this.arm_right.rotateAngleX = (-0.5F + rumble) * slamProgress;
						this.arm_right.rotateAngleZ = -0.35F * slamProgress;
						this.arm_right.rotateAngleY = 0.0F;
					}
					if (this.arm_left != null) {
						this.arm_left.rotateAngleX = (-0.5F + rumble) * slamProgress;
						this.arm_left.rotateAngleZ = 0.35F * slamProgress;
						this.arm_left.rotateAngleY = 0.0F;
					}
				}
			} else if (castType == 8) {
				if (castTimer > 8) {
					// Channeling: raise both hands up overhead while the growing magma blob forms above
					float raiseProgress = Math.min(1.0F, (50.0F - (float)castTimer) / 10.0F);
					float armPitch = -2.85F * raiseProgress;
					float wave = MathHelper.sin(ageInTicks * 0.45F) * 0.12F;
					if (this.arm_right != null) {
						this.arm_right.rotateAngleX = armPitch + wave;
						this.arm_right.rotateAngleZ = -0.35F - wave;
						this.arm_right.rotateAngleY = 0.0F;
					}
					if (this.arm_left != null) {
						this.arm_left.rotateAngleX = armPitch + wave;
						this.arm_left.rotateAngleZ = 0.35F + wave;
						this.arm_left.rotateAngleY = 0.0F;
					}
					if (this.head != null) {
						this.head.rotateAngleX = -0.5F * raiseProgress + wave * 0.5F;
					}
				} else {
					// Throwing: hurl both hands forward as the magma blast is launched
					float throwProgress = Math.min(1.0F, (8.0F - (float)castTimer) / 4.0F);
					float armPitch = -2.85F + 2.0F * throwProgress;
					if (this.body != null) {
						this.body.rotateAngleX = 0.2F * throwProgress;
					}
					if (this.head != null) {
						this.head.rotateAngleX = -0.15F + 0.25F * throwProgress;
					}
					if (this.arm_right != null) {
						this.arm_right.rotateAngleX = armPitch;
						this.arm_right.rotateAngleZ = -0.15F;
						this.arm_right.rotateAngleY = 0.0F;
					}
					if (this.arm_left != null) {
						this.arm_left.rotateAngleX = armPitch;
						this.arm_left.rotateAngleZ = 0.15F;
						this.arm_left.rotateAngleY = 0.0F;
					}
				}
			} else if (castType == 9) {
				if (castTimer > 10) {
					// Phase 1: First, raise hands to the side while charging
					float raiseProgress = Math.min(1.0F, (35.0F - (float)castTimer) / 10.0F);
					float armPitch = -0.3F * raiseProgress;
					float sideAngle = 1.35F * raiseProgress;
					float wave = MathHelper.sin(ageInTicks * 0.4F) * 0.12F;
					if (this.arm_right != null) {
						this.arm_right.rotateAngleX = armPitch + wave;
						this.arm_right.rotateAngleZ = -sideAngle - wave;
						this.arm_right.rotateAngleY = 0.0F;
					}
					if (this.arm_left != null) {
						this.arm_left.rotateAngleX = armPitch + wave;
						this.arm_left.rotateAngleZ = sideAngle + wave;
						this.arm_left.rotateAngleY = 0.0F;
					}
					if (this.head != null) {
						this.head.rotateAngleX = -0.4F * raiseProgress + wave * 0.5F;
					}
				} else {
					// Phase 2: When charged, one hand does a clean top-to-down vertical chop without side leaning
					float slamProgress = Math.min(1.0F, (10.0F - (float)castTimer) / 4.0F);
					if (this.body != null) {
						this.body.rotateAngleX = 0.15F * slamProgress;
						this.body.rotateAngleY = 0.0F;
						this.body.rotateAngleZ = 0.0F;
					}
					if (this.head != null) {
						this.head.rotateAngleX = -0.2F + 0.3F * slamProgress;
						this.head.rotateAngleY = 0.0F;
					}
					if (this.arm_right != null) {
						this.arm_right.rotateAngleX = -2.5F + 1.9F * slamProgress; // Top-to-down chop from overhead (-2.5F) down to straight forward (-0.6F)
						this.arm_right.rotateAngleY = 0.0F;
						this.arm_right.rotateAngleZ = -1.35F + 1.25F * slamProgress; // Brings arm in from side raise (-1.35F) to straight forward (-0.1F)
					}
					if (this.arm_left != null) {
						this.arm_left.rotateAngleX = -0.3F * (1.0F - slamProgress);
						this.arm_left.rotateAngleZ = 1.35F * (1.0F - slamProgress) + 0.2F * slamProgress;
						this.arm_left.rotateAngleY = 0.0F;
					}
				}
			} else if (castType == 10 || castType == 11) {
				float maxTimer = (castType == 10) ? 40.0F : 70.0F;
				float raiseProgress = Math.min(1.0F, (maxTimer - (float)castTimer) / maxTimer);
				float wave = MathHelper.sin(ageInTicks * 0.3F) * 0.08F;
				float armPitch = -2.75F * raiseProgress;
				float sideSpread = MathHelper.sin(raiseProgress * (float)Math.PI) * 1.1F + raiseProgress * 0.45F;
				if (this.arm_right != null) {
					this.arm_right.rotateAngleX = armPitch + wave;
					this.arm_right.rotateAngleZ = -sideSpread - wave;
					this.arm_right.rotateAngleY = 0.0F;
				}
				if (this.arm_left != null) {
					this.arm_left.rotateAngleX = armPitch + wave;
					this.arm_left.rotateAngleZ = sideSpread + wave;
					this.arm_left.rotateAngleY = 0.0F;
				}
				if (this.head != null) {
					this.head.rotateAngleX = -0.35F * raiseProgress + wave * 0.5F;
				}
			}
		} else if (entityIn instanceof EntityGoblinShaman && ((EntityGoblinShaman) entityIn).isDancing()) {
			int danceType = ((EntityGoblinShaman) entityIn).getDanceType();
			if (danceType == 2) {
				float phaseTime = ageInTicks % 360.0F; // 18 second cycle (3s per phase)
				float shake = MathHelper.sin(ageInTicks * 0.35F);
				float shakeCos = MathHelper.cos(ageInTicks * 0.35F);

				float bendWave = MathHelper.sin(ageInTicks * 0.05F);
				if (bendWave > 0.0F && this.body != null) {
					this.body.rotateAngleX = bendWave * 0.4F;
					if (this.head != null) {
						this.head.rotateAngleX -= bendWave * 0.25F;
					}
				}

				float phase = phaseTime % 60.0F;
				float weight = 1.0F;
				if (phase < 12.0F) {
					weight = phase / 12.0F;
				} else if (phase > 48.0F) {
					weight = (60.0F - phase) / 12.0F;
				}

				if (phaseTime < 60.0F) {
					// Phase 0: Right Arm
					if (this.arm_right != null) {
						this.arm_right.rotateAngleX += (-1.0F + shake * 0.5F) * weight;
						this.arm_right.rotateAngleZ += (-0.3F + shakeCos * 0.25F) * weight;
					}
				} else if (phaseTime < 120.0F) {
					// Phase 1: Left Arm
					if (this.arm_left != null) {
						this.arm_left.rotateAngleX += (-1.0F - shake * 0.5F) * weight;
						this.arm_left.rotateAngleZ += (0.3F + shakeCos * 0.25F) * weight;
					}
				} else if (phaseTime < 180.0F) {
					// Phase 2: Antagonistic Pair - Both Arms Together
					if (this.arm_right != null) {
						this.arm_right.rotateAngleX += (-1.2F + shake * 0.6F) * weight;
						this.arm_right.rotateAngleZ += (-0.4F - shakeCos * 0.2F) * weight;
					}
					if (this.arm_left != null) {
						this.arm_left.rotateAngleX += (-1.2F - shake * 0.6F) * weight;
						this.arm_left.rotateAngleZ += (0.4F + shakeCos * 0.2F) * weight;
					}
				} else if (phaseTime < 240.0F) {
					// Phase 3: Right Leg & Hip
					if (this.leg_right != null) {
						this.leg_right.rotateAngleX += (shake * 0.4F) * weight;
						this.leg_right.rotateAngleZ += (shakeCos * 0.2F) * weight;
					}
					if (this.body != null) this.body.rotateAngleZ = (shake * 0.12F) * weight;
				} else if (phaseTime < 300.0F) {
					// Phase 4: Left Leg & Hip
					if (this.leg_left != null) {
						this.leg_left.rotateAngleX += (-shake * 0.4F) * weight;
						this.leg_left.rotateAngleZ += (shakeCos * 0.2F) * weight;
					}
					if (this.body != null) this.body.rotateAngleZ = (-shake * 0.12F) * weight;
				} else {
					// Phase 5: Antagonistic Diagonal Pairs (Right Arm + Left Leg vs Left Arm + Right Leg)
					if (this.arm_right != null) {
						this.arm_right.rotateAngleX += (-0.8F + shake * 0.5F) * weight;
					}
					if (this.leg_left != null) {
						this.leg_left.rotateAngleX += (-shake * 0.4F) * weight;
					}
					if (this.arm_left != null) {
						this.arm_left.rotateAngleX += (-0.8F - shake * 0.5F) * weight;
					}
					if (this.leg_right != null) {
						this.leg_right.rotateAngleX += (shake * 0.4F) * weight;
					}
					if (this.body != null) this.body.rotateAngleZ = (shakeCos * 0.15F) * weight;
				}

				if (this.body != null) {
					if (this.leg_left != null) {
						this.leg_left.rotateAngleX -= this.body.rotateAngleX;
						this.leg_left.rotateAngleZ -= this.body.rotateAngleZ;
					}
					if (this.leg_right != null) {
						this.leg_right.rotateAngleX -= this.body.rotateAngleX;
						this.leg_right.rotateAngleZ -= this.body.rotateAngleZ;
					}
				}
			} else if (danceType == 3) {
				float throwPhase = ageInTicks % 40.0F;
							float armPitch;
				if (throwPhase < 20.0F) {
					armPitch = -1.2F - (throwPhase / 20.0F) * 1.8F; // Raises up to -3.0F over head
				} else if (throwPhase < 26.0F) {
					float progress = (throwPhase - 20.0F) / 6.0F;
					armPitch = -3.0F + progress * 1.8F; // Snaps forward to -1.2F
				} else {
					armPitch = -1.2F + MathHelper.sin(ageInTicks * 0.5F) * 0.1F; // Holds forward
				}

				if (this.arm_right != null) {
					this.arm_right.rotateAngleX = armPitch;
					this.arm_right.rotateAngleZ = -0.2F;
				}
				if (this.arm_left != null) {
					this.arm_left.rotateAngleX = -0.8F + MathHelper.sin(ageInTicks * 0.2F) * 0.2F;
				}
				if (this.body != null) {
					this.body.rotateAngleY = MathHelper.sin(ageInTicks * 0.2F) * 0.2F;
				}
				if (this.head != null) {
								this.head.rotateAngleX -= 0.2F;
				}
			} else {
				float danceSpeed = 0.4F;
				float jig = MathHelper.sin(ageInTicks * danceSpeed);
				float jigCos = MathHelper.cos(ageInTicks * danceSpeed);

				if (this.arm_right != null) {
					this.arm_right.rotateAngleX = -2.0F + jig * 0.8F;
					this.arm_right.rotateAngleZ = -0.4F + jigCos * 0.3F;
				}
				if (this.arm_left != null) {
					this.arm_left.rotateAngleX = -2.0F - jig * 0.8F;
					this.arm_left.rotateAngleZ = 0.4F + jigCos * 0.3F;
				}
				if (this.leg_right != null) {
					this.leg_right.rotateAngleX = jig * 0.6F;
				}
				if (this.leg_left != null) {
					this.leg_left.rotateAngleX = -jig * 0.6F;
				}
				if (this.body != null) {
					this.body.rotateAngleY = jigCos * 0.25F;
				}
				if (this.head != null) {
					this.head.rotateAngleX += MathHelper.abs(jig) * 0.2F;
					this.head.rotateAngleY += jigCos * 0.2F;
					this.head.rotateAngleZ = jig * 0.18F;
				}
			}
		} else if (this.swingProgress == 0.0F) {
			float idleWeight = 1.0F - Math.min(1.0F, limbSwingAmount * 2.5F);
			if (idleWeight > 0.0F) {
				float cycle = ageInTicks % 280.0F; // 14 second cycle (7s raising hands, 7s cooldown)
				if (cycle < 140.0F) {
					float chantWeight = idleWeight;
					if (cycle < 15.0F) {
						chantWeight *= (cycle / 15.0F);
					} else if (cycle > 125.0F) {
						chantWeight *= ((140.0F - cycle) / 15.0F);
					}

					float chantSpeed = 0.08F;
					float wave = MathHelper.sin(ageInTicks * chantSpeed);
					float waveCos = MathHelper.cos(ageInTicks * chantSpeed);

					if (this.arm_right != null) {
						this.arm_right.rotateAngleX = (this.arm_right.rotateAngleX * (1.0F - chantWeight)) + (-2.5F + wave * 0.2F) * chantWeight;
						this.arm_right.rotateAngleZ = (this.arm_right.rotateAngleZ * (1.0F - chantWeight)) + (-0.7F + waveCos * 0.15F) * chantWeight;
						this.arm_right.rotateAngleY = (this.arm_right.rotateAngleY * (1.0F - chantWeight)) + (-0.2F) * chantWeight;
					}
					if (this.arm_left != null) {
						this.arm_left.rotateAngleX = (this.arm_left.rotateAngleX * (1.0F - chantWeight)) + (-2.5F - wave * 0.2F) * chantWeight;
						this.arm_left.rotateAngleZ = (this.arm_left.rotateAngleZ * (1.0F - chantWeight)) + (0.7F - waveCos * 0.15F) * chantWeight;
						this.arm_left.rotateAngleY = (this.arm_left.rotateAngleY * (1.0F - chantWeight)) + (0.2F) * chantWeight;
					}

					if (this.head != null) {
						this.head.rotateAngleZ = waveCos * 0.08F * chantWeight;
						this.head.rotateAngleX += MathHelper.sin(ageInTicks * chantSpeed * 2.0F) * 0.05F * chantWeight;
					}
				}
			}
		} else if (this.swingProgress > 0.0F && entityIn instanceof EntityGoblinShaman) {
			// Top-to-down arm swing follow-through upon releasing Void Slash
			float progress = 1.0F - this.swingProgress;
			if (this.arm_right != null) {
				this.arm_right.rotateAngleX = -0.6F * (1.0F - progress * 0.5F);
				this.arm_right.rotateAngleY = 0.0F;
				this.arm_right.rotateAngleZ = -0.1F * (1.0F - progress);
			}
			if (this.body != null) {
				this.body.rotateAngleX = 0.15F * (1.0F - progress);
				this.body.rotateAngleY = 0.0F;
				this.body.rotateAngleZ = 0.0F;
			}
		}
	}
}
