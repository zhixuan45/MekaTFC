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
import org.jetbrains.annotations.Nullable;
import org.shengxi.common.blockentity.ElectricForgeBlockEntity;
import org.shengxi.registry.ModMenus;

import java.util.function.Supplier;

/**
 * 自动锻造机容器菜单 (ElectricForgeMenu)
 * 适配 NeoForge 1.21.1 架构，管理工件槽、铁砧模具槽、助焊剂槽、升级槽与玩家背包槽位及数据同步。
 */
public class ElectricForgeMenu extends AbstractContainerMenu {

    public final ElectricForgeBlockEntity blockEntity;
    private final ContainerData data;

    // 客户端构造器 (由网络打开 GUI 时自动调用)
    public ElectricForgeMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(containerId, playerInv, getBlockEntity(playerInv, extraData), new SimpleContainerData(14));
    }

    // 控制主交互槽位在弹窗/覆盖层打开时是否激活（由 Screen 动态注入）
    private Supplier<Boolean> mainSlotsActiveSupplier = () -> true;
    private Supplier<Boolean> upgradeWindowActiveSupplier = () -> false;

    public void setMainSlotsActiveSupplier(Supplier<Boolean> supplier) {
        this.mainSlotsActiveSupplier = supplier;
    }

    public void setUpgradeWindowActiveSupplier(Supplier<Boolean> supplier) {
        this.upgradeWindowActiveSupplier = supplier;
    }

    public boolean areMainSlotsActive() {
        return mainSlotsActiveSupplier == null || mainSlotsActiveSupplier.get();
    }

    public boolean isUpgradeWindowActive() {
        return upgradeWindowActiveSupplier != null && upgradeWindowActiveSupplier.get();
    }

    // 服务端构造器
    public ElectricForgeMenu(int containerId, Inventory playerInv, Container container, ContainerData data) {
        super(ModMenus.ELECTRIC_FORGE.get(), containerId);
        checkContainerSize(container, ElectricForgeBlockEntity.TOTAL_SLOTS);
        checkContainerDataCount(data, 14);
        this.data = data;
        this.blockEntity = container instanceof ElectricForgeBlockEntity forge ? forge : null;

        // 1. 机器内部核心槽位 (100% 严格对齐 1.21 TFC 官方 AnvilContainer 坐标规范)
        // 主工件槽 (SLOT_MAIN = 0, 位于左下方 x: 22, y: 76 铁砧夹紧主槽，限制堆叠上限为 1)
        this.addSlot(new Slot(container, ElectricForgeBlockEntity.SLOT_MAIN, 22, 76) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public int getMaxStackSize(@NotNull ItemStack stack) {
                return 1;
            }

            @Override
            public boolean isActive() {
                return areMainSlotsActive();
            }
        });
        // 副工件槽 (SLOT_SECOND = 1, 位于左上方 x: 22, y: 20 焊接副槽，限制堆叠上限为 1)
        this.addSlot(new Slot(container, ElectricForgeBlockEntity.SLOT_SECOND, 22, 20) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public int getMaxStackSize(@NotNull ItemStack stack) {
                return 1;
            }

            @Override
            public boolean isActive() {
                return areMainSlotsActive();
            }
        });
        // 铁砧模具槽 (SLOT_ANVIL = 2, 位于右下方 x: 138, y: 76 锤子水印处)
        this.addSlot(new Slot(container, ElectricForgeBlockEntity.SLOT_ANVIL, 138, 76) {
            @Override
            public boolean isActive() {
                return areMainSlotsActive();
            }
        });
        // 助焊剂槽 (SLOT_CATALYST = 3, 位于左侧中间 x: 22, y: 38 助焊剂水印处)
        this.addSlot(new Slot(container, ElectricForgeBlockEntity.SLOT_CATALYST, 22, 38) {
            @Override
            public boolean isActive() {
                return areMainSlotsActive();
            }
        });

        // 升级输入槽 (SLOT_UPGRADE_INPUT = 4, 位于升级子窗口右侧 x: 114, y: 51)
        this.addSlot(new Slot(container, ElectricForgeBlockEntity.SLOT_UPGRADE_INPUT, 114, 51) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return org.shengxi.common.upgrade.ForgeUpgradeType.fromItemStack(stack) != null;
            }

            @Override
            public boolean isActive() {
                return isUpgradeWindowActive();
            }
        });
        // 升级输出槽 (SLOT_UPGRADE_OUTPUT = 5, 虚拟槽位)
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

        // 2. 玩家背包槽位 (9x3 矩阵，标准原版 y 轴起始 126)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 126 + row * 18));
            }
        }

        // 3. 玩家快捷栏槽位 (1x9，标准原版 y 轴起始 184)
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 184));
        }

        this.addDataSlots(data);
    }

    private static Container getBlockEntity(Inventory playerInv, FriendlyByteBuf extraData) {
        if (playerInv.player.level() != null && extraData != null) {
            BlockEntity be = playerInv.player.level().getBlockEntity(extraData.readBlockPos());
            if (be instanceof ElectricForgeBlockEntity forge) {
                return forge;
            }
        }
        return new SimpleContainer(ElectricForgeBlockEntity.TOTAL_SLOTS);
    }

    // 常用数据访问便捷方法
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

    public int getAutoForgeTimer() {
        return this.data.get(5);
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

    public int getUpgradeCount(org.shengxi.common.upgrade.ForgeUpgradeType type) {
        return switch (type) {
            case SPEED -> this.data.get(10);
            case ENERGY -> this.data.get(11);
            case MUFFLING -> this.data.get(12);
            case PERFECT_FORGING -> this.data.get(13);
        };
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < ElectricForgeBlockEntity.TOTAL_SLOTS) {
                // 1. 从机器内部向玩家背包转移
                if (!this.moveItemStackTo(itemstack1, ElectricForgeBlockEntity.TOTAL_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // 2. 从玩家背包向机器转移
                // 2.1 若升级窗口激活且是升级物品，优先移入升级插槽 (Slot 4)
                if (isUpgradeWindowActive() && org.shengxi.common.upgrade.ForgeUpgradeType.fromItemStack(itemstack1) != null) {
                    if (!this.moveItemStackTo(itemstack1, ElectricForgeBlockEntity.SLOT_UPGRADE_INPUT, ElectricForgeBlockEntity.SLOT_UPGRADE_INPUT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // 2.2 若是助焊剂 (Slot 3)，可批量堆叠移入
                else if (itemstack1.is(net.dries007.tfc.common.TFCTags.Items.WELDING_FLUX)) {
                    if (!this.moveItemStackTo(itemstack1, ElectricForgeBlockEntity.SLOT_CATALYST, ElectricForgeBlockEntity.SLOT_CATALYST + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // 2.3 否则作为工件移入主槽或副槽 (严格限制单物品堆叠，每次仅移入 1 个)
                else {
                    Slot mainSlot = this.slots.get(ElectricForgeBlockEntity.SLOT_MAIN);
                    Slot secondSlot = this.slots.get(ElectricForgeBlockEntity.SLOT_SECOND);

                    if (mainSlot.isActive() && !mainSlot.hasItem()) {
                        ItemStack single = itemstack1.split(1);
                        mainSlot.set(single);
                    } else if (secondSlot.isActive() && !secondSlot.hasItem()) {
                        ItemStack single = itemstack1.split(1);
                        secondSlot.set(single);
                    } else {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (this.blockEntity != null) {
            return this.blockEntity.stillValid(player);
        }
        return true;
    }
}
