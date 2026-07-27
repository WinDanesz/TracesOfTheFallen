package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.client.model.ModelFrostlingRoll;
import com.windanesz.tracesofthefallen.client.model.ModelFrostlingStand;
import com.windanesz.tracesofthefallen.entity.EntityFrostling;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderFrostling extends RenderLiving<EntityFrostling> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/frostling.png");
    private static final ResourceLocation TEXTURE_NO_MASK = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/frostling_no_mask.png");
    private final ModelFrostlingStand standModel = new ModelFrostlingStand();
    private final ModelFrostlingRoll rollModel = new ModelFrostlingRoll();

    public RenderFrostling(RenderManager renderManager) {
        super(renderManager, new ModelFrostlingStand(), 0.5F);
    }

    @Override
    public void doRender(EntityFrostling entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (entity.isRolling()) {
            this.mainModel = rollModel;
        } else {
            this.mainModel = standModel;
        }
        this.mainModel.swingProgress = this.getSwingProgress(entity, partialTicks);
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityFrostling entity) {
        if (entity.isNoMask()) {
            return TEXTURE_NO_MASK;
        }
        return TEXTURE;
    }
}
