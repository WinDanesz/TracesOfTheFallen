package com.windanesz.tracesofthefallen.client.model;// Made with Blockbench 5.1.6

import com.windanesz.tracesofthefallen.entity.EntityGnossic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ModelGnossic extends ModelBase {
	// Centre of the mask in its final pose, in model pixels relative to the model origin (see cube_r1 / head)
	private static final float MASK_FINAL_X = 1.8F;
	private static final float MASK_FINAL_Y = -35.15F;
	private static final float MASK_FINAL_Z = -2.0F;
	// The mask is tilted by cube_r1's Z rotation (0.5934 rad) when worn; the mask block models are not
	private static final float MASK_TILT_DEGREES = 34.0F;

	private final ModelRenderer main;
	private final ModelRenderer head;
	private final ModelRenderer cube_r1;
	private final ModelRenderer body;
	private final ModelRenderer body2;
	private final ModelRenderer body3;
	private final ModelRenderer body4;
	private final ModelRenderer cube_r2;
	private final ModelRenderer draperyLeft;
	private final ModelRenderer draperyLeft2;
	private final ModelRenderer draperyLeft3;
	private final ModelRenderer draperyLeft4;
    private final ModelRenderer draperyRight;
	private final ModelRenderer draperyRight2;
	private final ModelRenderer draperyRight3;
	private final ModelRenderer draperyRight4;

    public ModelGnossic() {
		textureWidth = 128;
		textureHeight = 128;

		main = new ModelRenderer(this);
		main.setRotationPoint(0.0F, 24.0F, 0.0F);
		

		head = new ModelRenderer(this);
		head.setRotationPoint(-1.0F, 0.0F, 0.0F);
		main.addChild(head);
		

		cube_r1 = new ModelRenderer(this);
		cube_r1.setRotationPoint(0.0F, -31.0F, 0.0F);
		head.addChild(cube_r1);
		setRotationAngle(cube_r1, 0.0F, 0.0F, 0.5934F);
		cube_r1.cubeList.add(new ModelBox(cube_r1, 60, 6, -3.0F, -10.0F, -2.0F, 6, 10, 0, 0.0F, false));
		cube_r1.cubeList.add(new ModelBox(cube_r1, 42, 4, -3.0F, -10.0F, -3.0F, 6, 10, 2, 0.0F, false));

		body = new ModelRenderer(this);
		body.setRotationPoint(0.0F, -28.0F, 0.0F);
		main.addChild(body);
		body.cubeList.add(new ModelBox(body, 0, 0, -6.0F, 0.0F, -4.0F, 11, 7, 8, 0.2F, false)); // 0.2F inflation to hide gaps

		body2 = new ModelRenderer(this);
		body2.setRotationPoint(0.0F, 7.0F, -4.0F); // Hinge at the front face
		body.addChild(body2);
		body2.cubeList.add(new ModelBox(body2, 0, 7, -6.0F, 0.0F, 0.0F, 11, 7, 8, 0.2F, false)); // 0.2F inflation

		body3 = new ModelRenderer(this);
		body3.setRotationPoint(0.0F, 7.0F, 0.0F); // Already on front face
		body2.addChild(body3);
		body3.cubeList.add(new ModelBox(body3, 0, 14, -6.0F, 0.0F, 0.0F, 11, 7, 8, 0.2F, false)); // 0.2F inflation

		body4 = new ModelRenderer(this);
		body4.setRotationPoint(0.0F, 7.0F, 0.0F); // Already on front face
		body3.addChild(body4);
		body4.cubeList.add(new ModelBox(body4, 0, 21, -6.0F, 0.0F, 0.0F, 11, 7, 8, 0.2F, false)); // 0.2F inflation

		draperyLeft = new ModelRenderer(this);
		draperyLeft.setRotationPoint(5.0F, 2.0F, 4.0F);
		body.addChild(draperyLeft);
		draperyLeft.cubeList.add(new ModelBox(draperyLeft, 16, 36, 0.0F, 0.0F, 0.0F, 0, 6, 8, 0.0F, false));

		draperyLeft2 = new ModelRenderer(this);
		draperyLeft2.setRotationPoint(5.0F, -0.5F, 8.01F);
		body2.addChild(draperyLeft2);
		draperyLeft2.cubeList.add(new ModelBox(draperyLeft2, 16, 41, 0.0F, 0.0F, 0.0F, 0, 8, 8, 0.0F, false));

		draperyLeft3 = new ModelRenderer(this);
		draperyLeft3.setRotationPoint(5.0F, -0.5F, 8.02F);
		body3.addChild(draperyLeft3);
		draperyLeft3.cubeList.add(new ModelBox(draperyLeft3, 16, 48, 0.0F, 0.0F, 0.0F, 0, 8, 8, 0.0F, false));

		draperyLeft4 = new ModelRenderer(this);
		draperyLeft4.setRotationPoint(5.0F, -0.5F, 8.03F);
		body4.addChild(draperyLeft4);
		draperyLeft4.cubeList.add(new ModelBox(draperyLeft4, 16, 55, 0.0F, 0.0F, 0.0F, 0, 8, 8, 0.0F, false));

		draperyRight = new ModelRenderer(this);
		draperyRight.setRotationPoint(-6.0F, 2.0F, 4.0F);
		body.addChild(draperyRight);
		draperyRight.cubeList.add(new ModelBox(draperyRight, 0, 36, 0.0F, 0.0F, 0.0F, 0, 6, 8, 0.0F, false));

		draperyRight2 = new ModelRenderer(this);
		draperyRight2.setRotationPoint(-6.0F, -0.5F, 8.01F);
		body2.addChild(draperyRight2);
		draperyRight2.cubeList.add(new ModelBox(draperyRight2, 0, 41, 0.0F, 0.0F, 0.0F, 0, 8, 8, 0.0F, false));

		draperyRight3 = new ModelRenderer(this);
		draperyRight3.setRotationPoint(-6.0F, -0.5F, 8.02F);
		body3.addChild(draperyRight3);
		draperyRight3.cubeList.add(new ModelBox(draperyRight3, 0, 48, 0.0F, 0.0F, 0.0F, 0, 8, 8, 0.0F, false));

		draperyRight4 = new ModelRenderer(this);
		draperyRight4.setRotationPoint(-6.0F, -0.5F, 8.03F);
		body4.addChild(draperyRight4);
		draperyRight4.cubeList.add(new ModelBox(draperyRight4, 0, 55, 0.0F, 0.0F, 0.0F, 0, 8, 8, 0.0F, false));

		cube_r2 = new ModelRenderer(this);
		cube_r2.setRotationPoint(-1.0F, -3.9F, 0.0F);
		body.addChild(cube_r2);
		setRotationAngle(cube_r2, 0.0F, 0.0F, 0.5934F);
		cube_r2.cubeList.add(new ModelBox(cube_r2, 32, 36, 4.0F, -10.0F, -4.0F, 4, 12, 8, 0.0F, false));
		cube_r2.cubeList.add(new ModelBox(cube_r2, 38, 16, -3.0F, 1.0F, -4.0F, 7, 5, 8, 0.0F, false));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		if (entity instanceof EntityGnossic) {
			EntityGnossic gnossic = (EntityGnossic) entity;
			if (gnossic.isMaterializing()) {
				this.renderMaterializing(gnossic, f5);
				return;
			}
			this.body.showModel = gnossic.deathTime == 0;
		}
		main.render(f5);
	}

	/**
	 * A Gnossic Mask turning into a Gnossic: the head starts out exactly where the mask block was, floats up to its
	 * place on the body and tilts into its worn pose, and then the body unfurls from beneath it, fading in.
	 */
	private void renderMaterializing(EntityGnossic gnossic, float scale) {
		float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
		float ticks = gnossic.getMaterializeTicks(partialTicks);
		float rise = gnossic.getRiseProgress(partialTicks);
		float robe = gnossic.getRobeProgress(partialTicks);
		float ease = rise * rise * (3.0F - 2.0F * rise);

		// Where the mask started, as an offset from the feet in this model's (flipped, pixel) space
		Vec3d start = gnossic.getMaskStartPos();
		double dx = start.x - (gnossic.prevPosX + (gnossic.posX - gnossic.prevPosX) * partialTicks);
		double dy = start.y - (gnossic.prevPosY + (gnossic.posY - gnossic.prevPosY) * partialTicks);
		double dz = start.z - (gnossic.prevPosZ + (gnossic.posZ - gnossic.prevPosZ) * partialTicks);
		float bodyYaw = gnossic.prevRenderYawOffset + MathHelper.wrapDegrees(gnossic.renderYawOffset - gnossic.prevRenderYawOffset) * partialTicks;
		double angle = Math.toRadians(180.0F - bodyYaw);
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		float startX = (float) -(dx * cos - dz * sin) * 16.0F;
		float startY = (float) -dy * 16.0F;
		float startZ = (float) (dx * sin + dz * cos) * 16.0F;

		float x = startX + (MASK_FINAL_X - startX) * ease;
		// Floats a little higher than its final spot mid-flight, then settles
		float y = startY + (MASK_FINAL_Y - startY) * ease - MathHelper.sin(rise * (float) Math.PI) * 4.0F;
		float z = startZ + (MASK_FINAL_Z - startZ) * ease;

		// A floor mask lies face up, tipping forwards as it rises. Both kinds start untilted and settle into the worn tilt
		float pitch = gnossic.isFloorMask() ? -90.0F * (1.0F - ease) : 0.0F;
		float roll = -MASK_TILT_DEGREES * (1.0F - ease) + MathHelper.sin(ticks * 0.45F) * 5.0F * MathHelper.sin(rise * (float) Math.PI);

		GlStateManager.pushMatrix();
		GlStateManager.translate(main.rotationPointX * scale, main.rotationPointY * scale, main.rotationPointZ * scale);

		GlStateManager.pushMatrix();
		GlStateManager.translate(x * scale, y * scale, z * scale);
		GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(roll, 0.0F, 0.0F, 1.0F);
		GlStateManager.translate(-MASK_FINAL_X * scale, -MASK_FINAL_Y * scale, -MASK_FINAL_Z * scale);
		this.head.render(scale);
		GlStateManager.popMatrix();

		if (robe > 0.0F) {
			// The robes hang from the mask: grow them downwards while fading them in
			float reveal = robe * robe * (3.0F - 2.0F * robe);
			float alpha = MathHelper.clamp(robe * 1.8F, 0.0F, 1.0F);
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0F, this.body.rotationPointY * scale, 0.0F);
			GlStateManager.scale(1.0F, Math.max(reveal, 0.02F), 1.0F);
			GlStateManager.translate(0.0F, -this.body.rotationPointY * scale, 0.0F);
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
			this.body.showModel = true;
			this.body.render(scale);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}

		GlStateManager.popMatrix();
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
		if (entityIn instanceof EntityGnossic) {
			EntityGnossic gnossic = (EntityGnossic) entityIn;
			
			float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
			float currentDraperyYaw = gnossic.prevDraperyYaw + (gnossic.draperyYaw - gnossic.prevDraperyYaw) * partialTicks;
			float currentBodyYaw = gnossic.prevRenderYawOffset + MathHelper.wrapDegrees(gnossic.renderYawOffset - gnossic.prevRenderYawOffset) * partialTicks;
			
			float relativeYaw = MathHelper.wrapDegrees(currentDraperyYaw - currentBodyYaw);
			relativeYaw = MathHelper.clamp(relativeYaw, -30.0F, 30.0F); // Prevent ugly gaps from over-swinging
			
			float rad = (float) Math.toRadians(relativeYaw);
			
			// Each pair has its own body parent, so apply turn lag to every panel.
			// The parents still supply the existing flight lean and rippling motion.
			this.draperyLeft.rotateAngleY = rad;
			this.draperyRight.rotateAngleY = rad;
			this.draperyLeft2.rotateAngleY = rad;
			this.draperyRight2.rotateAngleY = rad;
			this.draperyLeft3.rotateAngleY = rad;
			this.draperyRight3.rotateAngleY = rad;
			this.draperyLeft4.rotateAngleY = rad;
			this.draperyRight4.rotateAngleY = rad;
			this.draperyLeft2.rotateAngleX = 0;
			this.draperyRight2.rotateAngleX = 0;

			// Add life to the sideways drapery pieces near the body (cube_r2)
			float headWind = MathHelper.sin(ageInTicks * 0.07F) * 0.05F; // Reduced amplitude
			this.cube_r1.rotateAngleZ = 0.5934F; // Keep head still
			this.cube_r2.rotateAngleZ = 0.5934F + (headWind * 0.8F);

			// Flight lean
			float currentFlightPitch = gnossic.prevFlightPitch + (gnossic.flightPitch - gnossic.prevFlightPitch) * partialTicks;
			this.body.rotateAngleX = currentFlightPitch;
            
            // Subdivided sine wave for the body (rippling top to bottom)
            float currentRippleIntensity = gnossic.prevRippleIntensity + (gnossic.rippleIntensity - gnossic.prevRippleIntensity) * partialTicks;
            float rippleAmp = 0.06F * currentRippleIntensity;
            
            // Drapery is now directly attached to body segments, so it inherits the snaking motion automatically!
            float time = ageInTicks * 0.15F;

            this.body2.rotateAngleX = MathHelper.cos(time - 1.0F) * rippleAmp;
            this.body3.rotateAngleX = MathHelper.cos(time - 2.0F) * rippleAmp;
            this.body4.rotateAngleX = MathHelper.cos(time - 3.0F) * rippleAmp;
            
            this.body2.rotateAngleZ = MathHelper.sin(time - 1.0F) * rippleAmp;
            this.body3.rotateAngleZ = MathHelper.sin(time - 2.0F) * rippleAmp;
            this.body4.rotateAngleZ = MathHelper.sin(time - 3.0F) * rippleAmp;
			
			// Head tracking
			this.head.rotateAngleY = netHeadYaw * 0.017453292F;
			this.head.rotateAngleX = headPitch * 0.017453292F;
			this.head.rotateAngleZ = 0.0F;

            // Intense shaking while stealing life
            if (gnossic.getAttackState() == 2) {
                this.head.rotateAngleY += MathHelper.sin(ageInTicks * 0.5F) * 0.01F;
                this.head.rotateAngleX += MathHelper.sin(ageInTicks * 0.6F + 1.0F) * 0.01F;
                this.head.rotateAngleZ += MathHelper.sin(ageInTicks * 0.4F + 2.0F) * 0.01F;
            }
		}
	}
}
