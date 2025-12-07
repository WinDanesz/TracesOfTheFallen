package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.client.model.ModelGoblin;
import com.windanesz.tracesofthefallen.entity.EntityGoblin;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderGoblin extends RenderBiped<EntityGoblin> {

	public static final ResourceLocation TEXTURE = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/goblin.png");

	public RenderGoblin(RenderManager rendermanagerIn) {
		super(rendermanagerIn, new ModelGoblin(), 0.2F);
		LayerBipedArmor layerbipedarmor = new LayerBipedArmor(this)
		{
			protected void initArmor()
			{
				this.modelLeggings = new ModelGoblin();
				this.modelArmor = new ModelGoblin();
			}
		};
		this.addLayer(layerbipedarmor);
	}

	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(EntityGoblin entity) {
		return TEXTURE;
	}

	@Override
	public void doRender(EntityGoblin entity, double x, double y, double z, float entityYaw, float partialTicks) {
		// Ensure setLivingAnimations is called before render
		((ModelGoblin)this.mainModel).setLivingAnimations(entity, entity.limbSwing, entity.limbSwingAmount, partialTicks);
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	@Override
	protected void preRenderCallback(EntityGoblin entitylivingbaseIn, float partialTickTime) {
		super.preRenderCallback(entitylivingbaseIn, partialTickTime);
	}
}