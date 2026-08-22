package org.shengxi.common.container;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.shengxi.common.blockentity.ElectricForgeBlockEntity;
import org.shengxi.common.upgrade.ForgeUpgradeType;
import org.shengxi.registry.ModMenus;

/**
 * 自动锻造机容器菜单 (ElectricForgeMenu)
 * 管理槽位布局、Mekanism 升级插槽、Shift 快捷转移逻辑与服务端状态同步。
 */
public class ElectricForgeMenu extends AbstractContainerMenu {

    private final Container container;
    private final ContainerData data;
    public final ElectricForgeBlockEntity blockEntity;

    // 客户端构造器（由 NetworkHooks.openScreen 调用）
    public ElectricForgeMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(containerId, playerInv, getBlockEntity(playerInv, extraData), new SimpleContainerData(14));
    }

    // 服务端构造器
    public ElectricForgeMenu(int containerId, Inventory playerInv, Container container, ContainerData data) {
        super(ModMenus.ELECTRIC_FORGE.get(), containerId);
        checkContainerSize(container, ElectricForgeBlockEntity.TOTAL_SLOTS);
        this.container = container;
        this.data = data;
        this.blockEntity = container instanceof ElectricForgeBlockEntity entity ? entity : null;

        // 1. 机器内部核心槽位 (像素级对齐 TFC 经典贴图 + Mekanism 升级管理双槽)
        // 主工件槽 (SLOT_MAIN = 0, 位于 x: 31, y: 68 带锭水印处)
        this.addSlot(new Slot(container, ElectricForgeBlockEntity.SLOT_MAIN, 31, 68));
        // 副工件槽 (SLOT_SECOND = 1, 位于 x: 13, y: 68 焊接副槽)
        this.addSlot(new Slot(container, ElectricForgeBlockEntity.SLOT_SECOND, 13, 68));
        // 铁砧模具槽 (SLOT_ANVIL = 2, 位于 x: 129, y: 68 锤子水印处)
        this.addSlot(new Slot(container, ElectricForgeBlockEntity.SLOT_ANVIL, 129, 68));
        // 助焊剂槽 (SLOT_CATALYST = 3, 位于 x: 147, y: 68 助焊剂水印处)
        this.addSlot(new Slot(container, ElectricForgeBlockEntity.SLOT_CATALYST, 147, 68));

        // 升级输入槽 (SLOT_UPGRADE_INPUT = 4, 虚拟槽位，由升级窗口独立接管渲染与交互，避免在主界面露出白框)
        this.addSlot(new Slot(container, ElectricForgeBlockEntity.SLOT_UPGRADE_INPUT, -1000, -1000) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return ForgeUpgradeType.fromItemStack(stack) != null;
            }

            @Override
            public boolean isActive() {
                return false;
            }
        });

        // 升级输出槽 (SLOT_UPGRADE_OUTPUT = 5, 虚拟槽位，只允许取出)
        this.addSlot(new Slot(container, ElectricForgeBlockEntity.SLOT_UPGRADE_OUTPUT, -1000, -1000) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }

            @Override
            public boolean isActive() {
                return false;
            }
        });



        // 2. 玩家背包槽位 (3行9列，y 从 127 开始)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 127 + row * 18));
            }
        }

        // 3. 玩家快捷栏槽位 (1行9列，y 为 185)
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 185));
        }

        this.addDataSlots(data);
    }

    private static ElectricForgeBlockEntity getBlockEntity(Inventory playerInv, FriendlyByteBuf extraData) {
        if (extraData != null) {
            BlockEntity entity = playerInv.player.level().getBlockEntity(extraData.readBlockPos());
            if (entity instanceof ElectricForgeBlockEntity forgeEntity) {
                return forgeEntity;
            }
        }
        return null;
    }

    public int getEnergyStored() {
        return this.data.get(0);
    }

    public int getMaxEnergyStored() {
        return this.data.get(1);
    }

    public int getTier() {
        return this.data.get(2);
    }

    public boolean isAutoForgeEnabled() {
        return this.data.get(3) == 1;
    }

    public int getCurrentState() {
        return this.data.get(4);
    }

    public int getMainItemTemperature() {
        return this.data.get(6);
    }

    public int getMainItemWorkingTemperature() {
        return this.data.get(7);
    }

    public boolean isRecipeLocked() {
        return this.data.get(8) == 1;
    }

    public int getUpgradeTicks() {
        return this.data.get(9);
    }

    public int getUpgradeCount(ForgeUpgradeType type) {
        return switch (type) {
            case SPEED -> this.data.get(10);
            case ENERGY -> this.data.get(11);
            case MUFFLING -> this.data.get(12);
            case PERFECT_FORGING -> this.data.get(13);
        };
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack current = slot.getItem();
            itemstack = current.copy();

            int machineSlotsCount = ElectricForgeBlockEntity.TOTAL_SLOTS;

            if (index < machineSlotsCount) {
                // 从机器槽位转移到玩家背包
                if (!this.moveItemStackTo(current, machineSlotsCount, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // 从玩家背包转移到机器槽位
                // 1. 若为支持的升级物品，优先尝试转移到升级输入槽
                if (ForgeUpgradeType.fromItemStack(current) != null) {
                    if (!this.moveItemStackTo(current, ElectricForgeBlockEntity.SLOT_UPGRADE_INPUT, ElectricForgeBlockEntity.SLOT_UPGRADE_INPUT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(current, ElectricForgeBlockEntity.SLOT_MAIN, ElectricForgeBlockEntity.SLOT_MAIN + 1, false)
                        && !this.moveItemStackTo(current, ElectricForgeBlockEntity.SLOT_ANVIL, ElectricForgeBlockEntity.SLOT_ANVIL + 1, false)
                        && !this.moveItemStackTo(current, ElectricForgeBlockEntity.SLOT_CATALYST, ElectricForgeBlockEntity.SLOT_CATALYST + 1, false)
                        && !this.moveItemStackTo(current, ElectricForgeBlockEntity.SLOT_SECOND, ElectricForgeBlockEntity.SLOT_SECOND + 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (current.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (current.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, current);
        }

        return itemstack;
    }
}

