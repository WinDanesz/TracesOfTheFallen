package com.windanesz.tracesofthefallen.api;

import com.windanesz.tracesofthefallen.entity.EntityLamphead;
import net.minecraft.util.math.BlockPos;

public interface ILampheadInteractable {
    /**
     * Checks if the block is currently in a state that allows interaction.
     */
    boolean canLampheadInteract(EntityLamphead lamphead);

    /**
     * The priority of this interaction. Higher value means higher priority.
     * e.g. Stone Compartment = 5, Brass Fabricator (fuel) = 4, Brass Fabricator (no fuel) = 3
     */
    int getInteractionPriority();

    /**
     * Called once when the Lamphead first arrives at the block to start the animation/process.
     */
    void startInteraction(EntityLamphead lamphead);

    /**
     * Called every tick while the Lamphead is actively interfacing with the block.
     */
    void onLampheadInteractTick(EntityLamphead lamphead);

    /**
     * Checks if the interaction process has finished successfully.
     */
    boolean isInteractionComplete(EntityLamphead lamphead);

    /**
     * Called if the interaction finishes successfully or is interrupted (e.g. Lamphead takes damage).
     */
    void stopInteraction(EntityLamphead lamphead);

    /**
     * Returns the exact BlockPos where the Lamphead should stand to interact.
     */
    BlockPos getInteractionPosition();

    /**
     * Returns the direction the Lamphead should face (yaw) while interacting.
     */
    float getInteractionFacing();
}
