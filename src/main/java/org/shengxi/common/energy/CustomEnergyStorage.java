package org.shengxi.common.energy;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.energy.EnergyStorage;

/**
 * 自定义能量存储容器
 * 支持能量存取、容量扩展、状态变化监听与 NBT 序列化。
 */
public class CustomEnergyStorage extends EnergyStorage {

    private final Runnable onContentsChanged;

    public CustomEnergyStorage(int capacity, int maxTransfer, Runnable onContentsChanged) {
        super(capacity, maxTransfer, maxTransfer, 0);
        this.onContentsChanged = onContentsChanged;
    }

    /**
     * 消耗能量（内部使用）
     *
     * @param amount 尝试消耗的电量
     * @return 实际消耗的电量
     */
    public int consumeEnergy(int amount) {
        int energyExtracted = Math.min(this.energy, amount);
        this.energy -= energyExtracted;
        if (energyExtracted > 0 && onContentsChanged != null) {
            onContentsChanged.run();
        }
        return energyExtracted;
    }

    /**
     * 设定当前能量（用于同步或调试）
     */
    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(capacity, energy));
        if (onContentsChanged != null) {
            onContentsChanged.run();
        }
    }

    /**
     * 动态设定能量容量（随升级扩展）
     */
    public void setCapacity(int capacity) {
        this.capacity = Math.max(0, capacity);
        if (this.energy > this.capacity) {
            this.energy = this.capacity;
        }
        if (onContentsChanged != null) {
            onContentsChanged.run();
        }
    }


    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = super.receiveEnergy(maxReceive, simulate);
        if (received > 0 && !simulate && onContentsChanged != null) {
            onContentsChanged.run();
        }
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = super.extractEnergy(maxExtract, simulate);
        if (extracted > 0 && !simulate && onContentsChanged != null) {
            onContentsChanged.run();
        }
        return extracted;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Energy", this.energy);
        tag.putInt("Capacity", this.capacity);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("Energy")) {
            this.energy = tag.getInt("Energy");
        }
        if (tag.contains("Capacity")) {
            this.capacity = tag.getInt("Capacity");
        }
    }
}
