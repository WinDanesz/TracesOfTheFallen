package com.windanesz.tracesofthefallen.world.structure;

import com.windanesz.tracesofthefallen.Settings;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.template.TemplateManager;

import java.util.Random;

public class StructureMultipartStart extends StructureStart {

    public StructureMultipartStart() {
    }

    public StructureMultipartStart(World worldIn, Random random, int chunkX, int chunkZ) {
        super(chunkX, chunkZ);
        this.create(worldIn, random, chunkX, chunkZ);
    }

    private void create(World worldIn, Random random, int chunkX, int chunkZ) {
        TemplateManager templateManager = worldIn.getSaveHandler().getStructureTemplateManager();
        
        // Pick an origin
        String originTemplate = "x21_10_origin_teleporter_1";

        int configuredMinY = Settings.worldgenSettings.multipartStructureMinY;
        int configuredMaxY = Settings.worldgenSettings.multipartStructureMaxY;
        int minY = Math.min(configuredMinY, configuredMaxY);
        int maxY = Math.max(configuredMinY, configuredMaxY);
        int y = minY + random.nextInt(maxY - minY + 1);

        BlockPos pos = new BlockPos(chunkX * 16 + 8, y, chunkZ * 16 + 8);
        Rotation rotation = Rotation.values()[random.nextInt(Rotation.values().length)];
        
        ComponentNbtPiece startPiece = new ComponentNbtPiece(templateManager, originTemplate, pos, rotation, 0);
        this.components.add(startPiece);
        
        startPiece.buildComponent(startPiece, this.components, random, templateManager);
        
        this.updateBoundingBox();
    }
}
