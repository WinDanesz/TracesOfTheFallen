package com.windanesz.tracesofthefallen.world.structure;

import com.windanesz.tracesofthefallen.Settings;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.template.TemplateManager;

import java.util.Random;

public class StructureGoblinCavernStart extends StructureStart {

    // Cavern templates a start can pick from; every one is expected to open onto its side walls.
    private static final String[] TEMPLATES = {"goblin_cavern_medium"};

    // Required by MapGenStructureIO when loading existing structures.
    public StructureGoblinCavernStart() {
    }

    public StructureGoblinCavernStart(World world, Random random, int chunkX, int chunkZ) {
        super(chunkX, chunkZ);
        TemplateManager templateManager = world.getSaveHandler().getStructureTemplateManager();

        int configuredMinY = Settings.worldgenSettings.goblinCavernMinY;
        int configuredMaxY = Settings.worldgenSettings.goblinCavernMaxY;
        int minY = Math.min(configuredMinY, configuredMaxY);
        int maxY = Math.max(configuredMinY, configuredMaxY);
        int y = minY + random.nextInt(maxY - minY + 1);

        BlockPos pos = new BlockPos(chunkX * 16 + 8, y, chunkZ * 16 + 8);
        Rotation rotation = Rotation.values()[random.nextInt(Rotation.values().length)];
        String template = TEMPLATES[random.nextInt(TEMPLATES.length)];

        this.components.add(new ComponentGoblinCavern(templateManager, template, pos, rotation, random.nextLong()));
        this.updateBoundingBox();
    }
}
