package com.windanesz.tracesofthefallen.world.structure;

import net.minecraft.world.gen.structure.MapGenStructureIO;

public class StructureRegistry {
    public static void registerStructures() {
        // Register the structure start
        MapGenStructureIO.registerStructure(StructureMultipartStart.class, "TOTF_MultipartDungeon");

        // Register the component pieces
        MapGenStructureIO.registerStructureComponent(ComponentNbtPiece.class, "TOTF_NbtPiece");
    }
}
