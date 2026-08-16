package com.windanesz.tracesofthefallen.block;

import com.windanesz.tracesofthefallen.Settings;
import com.windanesz.tracesofthefallen.entity.EntityZapLightning;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class TileEntityDanCoil extends TileEntity implements ITickable {
    private int cooldown = 0;
    private int chargeUp = 0;
    private boolean wasPowered = false;

    @Override
    public void update() {
        if (this.world == null) return;

        if (this.cooldown > 0) {
            this.cooldown--;
        }

        if (this.chargeUp > 0) {
            this.chargeUp--;

            if (!this.world.isRemote && this.chargeUp % 4 == 0) {
                // Spawn a small visual lightning arc around the block
                double x1 = this.pos.getX() + 0.5;
                double y1 = this.pos.getY() + 0.5;
                double z1 = this.pos.getZ() + 0.5;
                double x2 = x1 + (this.world.rand.nextDouble() - 0.5) * 1.5;
                double y2 = y1 + (this.world.rand.nextDouble() - 0.5) * 1.5;
                double z2 = z1 + (this.world.rand.nextDouble() - 0.5) * 1.5;
                EntityZapLightning visual = new EntityZapLightning(this.world, x1, y1, z1, x2, y2, z2, null);
                visual.visualOnly = true;
                this.world.spawnEntity(visual);
            }

            if (this.chargeUp == 0 && !this.world.isRemote) {
                List<BlockPos> targets = findConnectedCoils(true);
                for (BlockPos target : targets) {
                    double x1 = this.pos.getX() + 0.5;
                    double y1 = this.pos.getY() + 0.5;
                    double z1 = this.pos.getZ() + 0.5;
                    double x2 = target.getX() + 0.5;
                    double y2 = target.getY() + 0.5;
                    double z2 = target.getZ() + 0.5;
                    
                    EntityZapLightning lightning = new EntityZapLightning(this.world, x1, y1, z1, x2, y2, z2, null);
                    lightning.setDamage((float) Settings.miscSettings.dancoilTrapDamage);
                    this.world.spawnEntity(lightning);

                    TileEntity te = this.world.getTileEntity(target);
                    if (te instanceof TileEntityDanCoil) {
                        ((TileEntityDanCoil) te).trigger();
                    }
                }

                this.setCooldown(600);
            }
        } else if (this.cooldown == 0 && !this.world.isRemote && this.world.getTotalWorldTime() % 5 == 0) {
            // Check for trigger
            IBlockState state = this.world.getBlockState(this.pos);
            if (state.getBlock() instanceof BlockDanCoil) {
                List<BlockPos> positiveTargets = findConnectedCoils(false);
                
                for (BlockPos foundTarget : positiveTargets) {
                    // Check for entities in the path
                    AxisAlignedBB aabb = new AxisAlignedBB(
                        Math.min(this.pos.getX(), foundTarget.getX()),
                        Math.min(this.pos.getY(), foundTarget.getY()),
                        Math.min(this.pos.getZ(), foundTarget.getZ()),
                        Math.max(this.pos.getX(), foundTarget.getX()) + 1.0,
                        Math.max(this.pos.getY(), foundTarget.getY()) + 1.0,
                        Math.max(this.pos.getZ(), foundTarget.getZ()) + 1.0
                    );
                    
                    List<EntityLivingBase> entities = this.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
                    boolean triggered = false;
                    for (EntityLivingBase e : entities) {
                        if (!isEntityIgnored(e)) {
                            triggered = true;
                            break;
                        }
                    }
                    
                    if (triggered) {
                        this.trigger();
                        TileEntity targetTe = this.world.getTileEntity(foundTarget);
                        if (targetTe instanceof TileEntityDanCoil) {
                            ((TileEntityDanCoil) targetTe).trigger();
                        }
                        break;
                    }
                }
            }
        }
    }

    public List<BlockPos> findConnectedCoils(boolean allDirections) {
        List<BlockPos> found = new ArrayList<>();
        for (EnumFacing facing : EnumFacing.values()) {
            if (!allDirections && facing.getAxisDirection() != EnumFacing.AxisDirection.POSITIVE) {
                continue;
            }
            BlockPos searchPos = this.pos.offset(facing);
            for (int i = 1; i <= 9; i++) {
                IBlockState targetState = this.world.getBlockState(searchPos);
                if (targetState.getBlock() instanceof BlockDanCoil) {
                    found.add(searchPos);
                    break; // Blocked by a coil
                } else if (targetState.isFullBlock() && targetState.isOpaqueCube()) {
                    break; // Blocked by solid block
                }
                searchPos = searchPos.offset(facing);
            }
        }
        return found;
    }

    public void trigger() {
        if (this.cooldown == 0 && this.chargeUp == 0) {
            this.chargeUp = 20;
            if (!this.world.isRemote) {
                this.world.addBlockEvent(this.pos, this.getBlockType(), 1, 20); // trigger client chargeup
            }
        }
    }

    public void updateRedstone(boolean isPowered) {
        if (isPowered && !this.wasPowered) {
            this.trigger();
        }
        this.wasPowered = isPowered;
    }

    private boolean isEntityIgnored(EntityLivingBase entity) {
        ResourceLocation regName = EntityList.getKey(entity);
        if (regName != null) {
            String name = regName.toString();
            for (String ignored : Settings.miscSettings.dancoilIgnoredEntities) {
                if (name.equals(ignored)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public int getCooldown() {
        return this.cooldown;
    }

    @Override
    public boolean receiveClientEvent(int id, int type) {
        if (id == 1) {
            this.chargeUp = type;
            return true;
        }
        return super.receiveClientEvent(id, type);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.cooldown = compound.getInteger("cooldown");
        this.wasPowered = compound.getBoolean("wasPowered");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("cooldown", this.cooldown);
        compound.setBoolean("wasPowered", this.wasPowered);
        return super.writeToNBT(compound);
    }
}
