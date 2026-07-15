package com.windanesz.tracesofthefallen.client.model;

import com.windanesz.tracesofthefallen.entity.EntityMagmaPool;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelMagmaPool extends ModelBase {
	private final ModelRenderer[] slabs;
	private final float[] phaseOffsets;
	private final float[] speeds;
	private final float[] amplitudes;

	public ModelMagmaPool() {
		this.textureWidth = 16;
		this.textureHeight = 16;

		this.slabs = new ModelRenderer[28];
		this.phaseOffsets = new float[28];
		this.speeds = new float[28];
		this.amplitudes = new float[28];

		// Slab 0: Center Core
		slabs[0] = new ModelRenderer(this);
		slabs[0].setRotationPoint(0.0F, 24.0F, 0.0F);
		slabs[0].cubeList.add(new ModelBox(slabs[0], 0, 0, -8.0F, -3.0F, -8.0F, 16, 3, 16, 0.0F, false));
		phaseOffsets[0] = 0.0F; speeds[0] = 0.25F; amplitudes[0] = 1.0F;

		// Slab 1: NW Inner Plate
		slabs[1] = new ModelRenderer(this);
		slabs[1].setRotationPoint(0.0F, 24.0F, 0.0F);
		slabs[1].cubeList.add(new ModelBox(slabs[1], 0, 0, -20.0F, -3.0F, -20.0F, 16, 3, 16, 0.0F, false));
		phaseOffsets[1] = 0.8F; speeds[1] = 0.30F; amplitudes[1] = 1.3F;

		// Slab 2: NE Inner Plate
		slabs[2] = new ModelRenderer(this);
		slabs[2].setRotationPoint(0.0F, 24.0F, 0.0F);
		slabs[2].cubeList.add(new ModelBox(slabs[2], 0, 0, 4.0F, -3.0F, -20.0F, 16, 3, 16, 0.0F, false));
		phaseOffsets[2] = 1.6F; speeds[2] = 0.22F; amplitudes[2] = 1.5F;

		// Slab 3: SW Inner Plate
		slabs[3] = new ModelRenderer(this);
		slabs[3].setRotationPoint(0.0F, 24.0F, 0.0F);
		slabs[3].cubeList.add(new ModelBox(slabs[3], 0, 0, -20.0F, -3.0F, 4.0F, 16, 3, 16, 0.0F, false));
		phaseOffsets[3] = 2.4F; speeds[3] = 0.28F; amplitudes[3] = 1.2F;

		// Slab 4: SE Inner Plate
		slabs[4] = new ModelRenderer(this);
		slabs[4].setRotationPoint(0.0F, 24.0F, 0.0F);
		slabs[4].cubeList.add(new ModelBox(slabs[4], 0, 0, 4.0F, -3.0F, 4.0F, 16, 3, 16, 0.0F, false));
		phaseOffsets[4] = 3.2F; speeds[4] = 0.26F; amplitudes[4] = 1.4F;

		// Slab 5: North Mid Overlay
		slabs[5] = new ModelRenderer(this);
		slabs[5].setRotationPoint(0.0F, 24.0F, 0.0F);
		slabs[5].cubeList.add(new ModelBox(slabs[5], 0, 0, -10.0F, -4.5F, -24.0F, 20, 3, 16, 0.0F, false));
		phaseOffsets[5] = 1.2F; speeds[5] = 0.35F; amplitudes[5] = 1.6F;

		// Slab 6: South Mid Overlay
		slabs[6] = new ModelRenderer(this);
		slabs[6].setRotationPoint(0.0F, 24.0F, 0.0F);
		slabs[6].cubeList.add(new ModelBox(slabs[6], 0, 0, -10.0F, -4.5F, 8.0F, 20, 3, 16, 0.0F, false));
		phaseOffsets[6] = 4.0F; speeds[6] = 0.32F; amplitudes[6] = 1.5F;

		// Slab 7: West Mid Overlay
		slabs[7] = new ModelRenderer(this);
		slabs[7].setRotationPoint(0.0F, 24.0F, 0.0F);
		slabs[7].cubeList.add(new ModelBox(slabs[7], 0, 0, -24.0F, -4.5F, -10.0F, 16, 3, 20, 0.0F, false));
		phaseOffsets[7] = 5.0F; speeds[7] = 0.29F; amplitudes[7] = 1.4F;

		// Slab 8: East Mid Overlay
		slabs[8] = new ModelRenderer(this);
		slabs[8].setRotationPoint(0.0F, 24.0F, 0.0F);
		slabs[8].cubeList.add(new ModelBox(slabs[8], 0, 0, 8.0F, -4.5F, -10.0F, 16, 3, 20, 0.0F, false));
		phaseOffsets[8] = 2.0F; speeds[8] = 0.33F; amplitudes[8] = 1.7F;

		// Slab 9: Far North Outer Ring
		slabs[9] = new ModelRenderer(this);
		slabs[9].setRotationPoint(0.0F, 24.0F, 0.0F);
		slabs[9].cubeList.add(new ModelBox(slabs[9], 0, 0, -14.0F, -3.0F, -42.0F, 28, 3, 20, 0.0F, false));
		phaseOffsets[9] = 0.5F; speeds[9] = 0.38F; amplitudes[9] = 1.8F;

		// Slab 10: Far South Outer Ring
		slabs[10] = new ModelRenderer(this);
		slabs[10].setRotationPoint(0.0F, 24.0F, 0.0F);
		slabs[10].cubeList.add(new ModelBox(slabs[10], 0, 0, -14.0F, -3.0F, 22.0F, 28, 3, 20, 0.0F, false));
		phaseOffsets[10] = 3.5F; speeds[10] = 0.40F; amplitudes[10] = 1.9F;

		// Slab 11: Far West Outer Ring
		slabs[11] = new ModelRenderer(this);
		slabs[11].setRotationPoint(0.0F, 24.0F, 0.0F);
		slabs[11].cubeList.add(new ModelBox(slabs[11], 0, 0, -42.0F, -3.0F, -14.0F, 20, 3, 28, 0.0F, false));
		phaseOffsets[11] = 4.5F; speeds[11] = 0.24F; amplitudes[11] = 1.4F;

		// Slab 12: Far East Outer Ring
		slabs[12] = new ModelRenderer(this);
		slabs[12].setRotationPoint(0.0F, 24.0F, 0.0F);
		slabs[12].cubeList.add(new ModelBox(slabs[12], 0, 0, 22.0F, -3.0F, -14.0F, 20, 3, 28, 0.0F, false));
		phaseOffsets[12] = 1.0F; speeds[12] = 0.27F; amplitudes[12] = 1.5F;

		// Slab 13: Far North-West Corner Block
		slabs[13] = new ModelRenderer(this);
		slabs[13].setRotationPoint(0.0F, 24.0F, 0.0F);
		slabs[13].cubeList.add(new ModelBox(slabs[13], 0, 0, -38.0F, -3.0F, -38.0F, 22, 3, 22, 0.0F, false));
		phaseOffsets[13] = 2.2F; speeds[13] = 0.31F; amplitudes[13] = 1.6F;

		// Slab 14: Far North-East Corner Block
		slabs[14] = new ModelRenderer(this);
		slabs[14].setRotationPoint(0.0F, 24.0F, 0.0F);
		slabs[14].cubeList.add(new ModelBox(slabs[14], 0, 0, 16.0F, -3.0F, -38.0F, 22, 3, 22, 0.0F, false));
		phaseOffsets[14] = 3.8F; speeds[14] = 0.29F; amplitudes[14] = 1.7F;

		// Slab 15: Far South-West Corner Block
		slabs[15] = new ModelRenderer(this);
		slabs[15].setRotationPoint(0.0F, 24.0F, 0.0F);
		slabs[15].cubeList.add(new ModelBox(slabs[15], 0, 0, -38.0F, -3.0F, 16.0F, 22, 3, 22, 0.0F, false));
		phaseOffsets[15] = 5.2F; speeds[15] = 0.34F; amplitudes[15] = 1.5F;

		// Slab 16: Far South-East Corner Block
		slabs[16] = new ModelRenderer(this);
		slabs[16].setRotationPoint(0.0F, 24.0F, 0.0F);
		slabs[16].cubeList.add(new ModelBox(slabs[16], 0, 0, 16.0F, -3.0F, 16.0F, 22, 3, 22, 0.0F, false));
		phaseOffsets[16] = 1.5F; speeds[16] = 0.26F; amplitudes[16] = 1.6F;

		// Slab 17: Top Center Crust A
		slabs[17] = new ModelRenderer(this);
		slabs[17].setRotationPoint(0.0F, 24.0F, 0.0F);
		slabs[17].cubeList.add(new ModelBox(slabs[17], 0, 0, -12.0F, -4.8F, -6.0F, 24, 3, 12, 0.0F, false));
		phaseOffsets[17] = 0.3F; speeds[17] = 0.36F; amplitudes[17] = 1.8F;

		// Slab 18: Top Center Crust B
		slabs[18] = new ModelRenderer(this);
		slabs[18].setRotationPoint(0.0F, 24.0F, 0.0F);
		slabs[18].cubeList.add(new ModelBox(slabs[18], 0, 0, -6.0F, -5.2F, -12.0F, 12, 3, 24, 0.0F, false));
		phaseOffsets[18] = 4.2F; speeds[18] = 0.39F; amplitudes[18] = 1.9F;

		// Slab 19: North-West Crust Patch
		slabs[19] = new ModelRenderer(this);
		slabs[19].setRotationPoint(0.0F, 24.0F, 0.0F);
		slabs[19].cubeList.add(new ModelBox(slabs[19], 0, 0, -28.0F, -4.2F, -28.0F, 16, 3, 16, 0.0F, false));
		phaseOffsets[19] = 2.8F; speeds[19] = 0.33F; amplitudes[19] = 1.4F;

		// Slab 20: North-East Crust Patch
		slabs[20] = new ModelRenderer(this);
		slabs[20].setRotationPoint(0.0F, 24.0F, 0.0F);
		slabs[20].cubeList.add(new ModelBox(slabs[20], 0, 0, 12.0F, -4.2F, -28.0F, 16, 3, 16, 0.0F, false));
		phaseOffsets[20] = 3.3F; speeds[20] = 0.28F; amplitudes[20] = 1.5F;

		// Slab 21: South-West Crust Patch
		slabs[21] = new ModelRenderer(this);
		slabs[21].setRotationPoint(0.0F, 24.0F, 0.0F);
		slabs[21].cubeList.add(new ModelBox(slabs[21], 0, 0, -28.0F, -4.2F, 12.0F, 16, 3, 16, 0.0F, false));
		phaseOffsets[21] = 0.9F; speeds[21] = 0.35F; amplitudes[21] = 1.6F;

		// Slab 22: South-East Crust Patch
		slabs[22] = new ModelRenderer(this);
		slabs[22].setRotationPoint(0.0F, 24.0F, 0.0F);
		slabs[22].cubeList.add(new ModelBox(slabs[22], 0, 0, 12.0F, -4.2F, 12.0F, 16, 3, 16, 0.0F, false));
		phaseOffsets[22] = 5.6F; speeds[22] = 0.31F; amplitudes[22] = 1.4F;

		// Slab 23: Extreme North Edge Slab
		slabs[23] = new ModelRenderer(this);
		slabs[23].setRotationPoint(0.0F, 24.0F, 0.0F);
		slabs[23].cubeList.add(new ModelBox(slabs[23], 0, 0, -18.0F, -2.0F, -48.0F, 36, 2, 10, 0.0F, false));
		phaseOffsets[23] = 1.8F; speeds[23] = 0.25F; amplitudes[23] = 1.2F;

		// Slab 24: Extreme South Edge Slab
		slabs[24] = new ModelRenderer(this);
		slabs[24].setRotationPoint(0.0F, 24.0F, 0.0F);
		slabs[24].cubeList.add(new ModelBox(slabs[24], 0, 0, -18.0F, -2.0F, 38.0F, 36, 2, 10, 0.0F, false));
		phaseOffsets[24] = 4.8F; speeds[24] = 0.27F; amplitudes[24] = 1.3F;

		// Slab 25: Extreme West Edge Slab
		slabs[25] = new ModelRenderer(this);
		slabs[25].setRotationPoint(0.0F, 24.0F, 0.0F);
		slabs[25].cubeList.add(new ModelBox(slabs[25], 0, 0, -48.0F, -2.0F, -18.0F, 10, 2, 36, 0.0F, false));
		phaseOffsets[25] = 3.0F; speeds[25] = 0.24F; amplitudes[25] = 1.2F;

		// Slab 26: Extreme East Edge Slab
		slabs[26] = new ModelRenderer(this);
		slabs[26].setRotationPoint(0.0F, 24.0F, 0.0F);
		slabs[26].cubeList.add(new ModelBox(slabs[26], 0, 0, 38.0F, -2.0F, -18.0F, 10, 2, 36, 0.0F, false));
		phaseOffsets[26] = 2.1F; speeds[26] = 0.26F; amplitudes[26] = 1.3F;

		// Slab 27: Center Bubbling Cap
		slabs[27] = new ModelRenderer(this);
		slabs[27].setRotationPoint(0.0F, 24.0F, 0.0F);
		slabs[27].cubeList.add(new ModelBox(slabs[27], 0, 0, -4.0F, -5.8F, -4.0F, 8, 3, 8, 0.0F, false));
		phaseOffsets[27] = 0.1F; speeds[27] = 0.42F; amplitudes[27] = 2.1F;
	}

	@Override
	public void render(Entity entity, float f, float f1, float ageInTicks, float f3, float f4, float scale) {
		int timer = 0;
		if (entity instanceof EntityMagmaPool) {
			timer = ((EntityMagmaPool) entity).getTimer();
		} else {
			timer = (int) ageInTicks;
		}
		float exactTime = timer + (ageInTicks - (int) ageInTicks);

		for (int i = 0; i < slabs.length; i++) {
			if (exactTime < 10.0F) {
				// Phase 1: Seeping lava, rising up and rumbling before exploding (0.5 seconds / 10 ticks)
				float progress = Math.min(1.0F, exactTime / 9.0F);
				float base = 28.0F - progress * 4.0F; // Rise from underground (Y=28) to ground level (Y=24)
				float rumble = MathHelper.sin(exactTime * 2.5F + i * 1.3F) * 0.4F;
				slabs[i].rotationPointY = base + rumble;
			} else if (exactTime <= 125.0F) {
				// Phase 2: Going up and down (+3 seconds duration increase, running until tick 125)
				float elapsed = exactTime - 10.0F;
				float wave = MathHelper.sin(elapsed * speeds[i] + phaseOffsets[i]) * amplitudes[i];
				slabs[i].rotationPointY = 24.0F + wave;
			} else {
				// Phase 3: Fading out / sinking back underground over the final 10 ticks (ticks 125-135)
				float elapsed = exactTime - 10.0F;
				float wave = MathHelper.sin(elapsed * speeds[i] + phaseOffsets[i]) * amplitudes[i];
				float sinkProgress = Math.min(1.0F, (exactTime - 125.0F) / 10.0F);
				slabs[i].rotationPointY = 24.0F + wave + sinkProgress * 8.0F;
			}
			slabs[i].render(scale);
		}
	}
}
