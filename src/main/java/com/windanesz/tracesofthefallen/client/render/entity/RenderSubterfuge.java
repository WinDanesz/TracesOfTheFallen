package com.windanesz.tracesofthefallen.client.render.entity;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.client.model.ModelSubterFuge2;
import com.windanesz.tracesofthefallen.entity.EntitySubterfuge;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderSubterfuge extends RenderLiving<EntitySubterfuge> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/subterfuge.png");

    public RenderSubterfuge(RenderManager rendermanagerIn) {
        super(rendermanagerIn, new ModelSubterFuge2(), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySubterfuge entity) {
        return TEXTURE;
    }
}
