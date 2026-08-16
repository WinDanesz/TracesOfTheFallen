package com.windanesz.tracesofthefallen.client.renderer;

import com.windanesz.tracesofthefallen.TracesOfTheFallen;
import com.windanesz.tracesofthefallen.client.model.ModelLost;
import com.windanesz.tracesofthefallen.entity.EntityLost;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderLost extends RenderLiving<EntityLost> {

    private static final ResourceLocation LOST_TEXTURES = new ResourceLocation(TracesOfTheFallen.MODID, "textures/entity/lost.png");

    public RenderLost(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelLost(), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityLost entity) {
        return LOST_TEXTURES;
    }
}
