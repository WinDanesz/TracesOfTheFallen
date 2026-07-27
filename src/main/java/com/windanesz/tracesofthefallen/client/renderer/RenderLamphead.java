package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.client.model.ModelLamphead;
import com.windanesz.tracesofthefallen.entity.EntityLamphead;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderLamphead extends RenderLiving<EntityLamphead> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/lamphead.png");
    private static final ResourceLocation TEXTURE_ANGRY = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/lamphead_angry.png");

    public RenderLamphead(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelLamphead(), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityLamphead entity) {
        return entity.isAngry() ? TEXTURE_ANGRY : TEXTURE;
    }
}
