package com.windanesz.tracesofthefallen.entity.ai;

import com.windanesz.tracesofthefallen.api.ILampheadInteractable;
import com.windanesz.tracesofthefallen.entity.EntityLamphead;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class EntityAILampheadInteract extends EntityAIBase {
    private final EntityLamphead lamphead;
    private final double speed;
    
    private BlockPos targetPos = null;
    private BlockPos interactPos = null;
    private ILampheadInteractable targetInteractable = null;
    
    private int searchCooldown = 0;
    private int interactTimer = 0;

    public EntityAILampheadInteract(EntityLamphead lamphead, double speed) {
        this.lamphead = lamphead;
        this.speed = speed;
        this.setMutexBits(3); // Movement + Look
    }

    @Override
    public boolean shouldExecute() {
        if (this.lamphead.getAttackTarget() != null) return false;
        
        int mode = this.lamphead.getMode();
        if (mode == 0) return false; // Wandering mode doesn't work
        
        if (this.searchCooldown > 0) {
            this.searchCooldown--;
            return false;
        }

        this.searchCooldown = 20 + this.lamphead.getRNG().nextInt(20);
        
        BlockPos currentPos = new BlockPos(this.lamphead);
        ILampheadInteractable bestInteractable = null;
        BlockPos bestPos = null;
        int bestPriority = -1;

        int rangeXZ = (mode == 2) ? 2 : 8; // Static mode only checks immediately around it
        int rangeY = (mode == 2) ? 1 : 4;

        for (int x = -rangeXZ; x <= rangeXZ; x++) {
            for (int y = -rangeY; y <= rangeY; y++) {
                for (int z = -rangeXZ; z <= rangeXZ; z++) {
                    BlockPos checkPos = currentPos.add(x, y, z);
                    TileEntity te = this.lamphead.world.getTileEntity(checkPos);
                    
                    if (te instanceof ILampheadInteractable) {
                        ILampheadInteractable interactable = (ILampheadInteractable) te;
                        if (interactable.canLampheadInteract(this.lamphead)) {
                            int priority = interactable.getInteractionPriority();
                            if (priority > bestPriority) {
                                BlockPos interactPos = interactable.getInteractionPosition();
                                // Check if interactPos is accessible
                                if (this.lamphead.world.isAirBlock(interactPos) && 
                                    this.lamphead.world.isAirBlock(interactPos.up()) && 
                                    this.lamphead.world.getBlockState(interactPos.down()).isOpaqueCube()) {
                                    
                                    bestInteractable = interactable;
                                    bestPos = checkPos;
                                    bestPriority = priority;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (bestInteractable != null) {
            this.targetInteractable = bestInteractable;
            this.targetPos = bestPos;
            this.interactPos = bestInteractable.getInteractionPosition();
            return true;
        }

        return false;
    }

    private int pathingTimer = 0;
    private boolean wasSitting = false;

    @Override
    public void startExecuting() {
        this.interactTimer = 0;
        this.pathingTimer = 0;
        
        this.wasSitting = this.lamphead.isSitting();
        if (this.wasSitting) {
            this.lamphead.setSitting(false);
        }
        
        if (this.targetInteractable != null) {
            this.targetInteractable.startInteraction(this.lamphead);
        }
        this.lamphead.getNavigator().tryMoveToXYZ(this.interactPos.getX() + 0.5D, this.interactPos.getY(), this.interactPos.getZ() + 0.5D, this.speed);
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (this.lamphead.getMode() == 0) return false; // Stop if changed to Wandering
        if (this.lamphead.getAttackTarget() != null) return false;
        if (this.targetPos == null) return false;
        
        TileEntity te = this.lamphead.world.getTileEntity(this.targetPos);
        if (!(te instanceof ILampheadInteractable)) return false;
        
        this.targetInteractable = (ILampheadInteractable) te;
        if (this.targetInteractable.isInteractionComplete(this.lamphead)) return false;
        
        return this.interactTimer < 600 && this.pathingTimer < 200; // Timeouts for safety
    }

    @Override
    public void updateTask() {
        if (this.lamphead.getDistanceSqToCenter(this.interactPos) < 1.0D) {
            this.lamphead.getNavigator().clearPath();
            
            this.lamphead.motionX = 0;
            this.lamphead.motionZ = 0;
            
            float targetYaw = this.targetInteractable.getInteractionFacing();
            this.lamphead.rotationYaw = targetYaw;
            this.lamphead.rotationYawHead = targetYaw;
            this.lamphead.renderYawOffset = targetYaw;
            
            // Adjust position slightly forward towards the block (0.03125 = half a pixel)
            double radianYaw = Math.toRadians(-targetYaw);
            double offsetX = Math.sin(radianYaw) * 0.03125D;
            double offsetZ = Math.cos(radianYaw) * 0.03125D;
            
            this.lamphead.setPositionAndUpdate(this.interactPos.getX() + 0.5D + offsetX, this.interactPos.getY(), this.interactPos.getZ() + 0.5D + offsetZ);
            
            this.lamphead.getDataManager().set(EntityLamphead.FABRICATING, true);
            this.targetInteractable.onLampheadInteractTick(this.lamphead);
            
            this.interactTimer++;
            this.pathingTimer = 0; // Reset pathing timer while working
        } else {
            this.lamphead.getDataManager().set(EntityLamphead.FABRICATING, false);
            this.pathingTimer++;
            if (this.lamphead.getNavigator().noPath()) {
                this.lamphead.getNavigator().tryMoveToXYZ(this.interactPos.getX() + 0.5D, this.interactPos.getY(), this.interactPos.getZ() + 0.5D, this.speed);
            }
        }
    }

    @Override
    public void resetTask() {
        this.lamphead.getDataManager().set(EntityLamphead.FABRICATING, false);
        if (this.targetInteractable != null) {
            this.targetInteractable.stopInteraction(this.lamphead);
        }
        this.targetInteractable = null;
        this.targetPos = null;
        this.interactPos = null;
    }
}
