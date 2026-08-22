package org.shengxi.common.upgrade;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.shengxi.registry.ModItems;

/**
 * 自动锻造机升级类型枚举 (ForgeUpgradeType)
 * 支持 Mekanism 速度、能量、消音升级以及 MekaTFC 完美锻造升级。
 */
public enum ForgeUpgradeType {
    SPEED("speed", "gui.mekatfc.upgrade.speed", "gui.mekatfc.upgrade.speed.desc", 8),
    ENERGY("energy", "gui.mekatfc.upgrade.energy", "gui.mekatfc.upgrade.energy.desc", 8),
    MUFFLING("muffling", "gui.mekatfc.upgrade.muffling", "gui.mekatfc.upgrade.muffling.desc", 4),
    PERFECT_FORGING("perfect_forging", "gui.mekatfc.upgrade.perfect_forging", "gui.mekatfc.upgrade.perfect_forging.desc", 1);

    private final String name;
    private final String titleKey;
    private final String descKey;
    private final int maxCount;

    ForgeUpgradeType(String name, String titleKey, String descKey, int maxCount) {
        this.name = name;
        this.titleKey = titleKey;
        this.descKey = descKey;
        this.maxCount = maxCount;
    }

    public String getName() {
        return name;
    }

    public Component getTitle() {
        return Component.translatable(titleKey);
    }

    public Component getDescription() {
        return Component.translatable(descKey);
    }

    public int getMaxCount() {
        return maxCount;
    }

    /**
     * 根据物品堆解析对应的升级类型
     */
    public static ForgeUpgradeType fromItemStack(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (stack.is(ModItems.UPGRADE_PERFECT_FORGING.get())) {
            return PERFECT_FORGING;
        }
        net.minecraft.resources.ResourceLocation loc = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (loc != null && "mekanism".equals(loc.getNamespace())) {
            return switch (loc.getPath()) {
                case "upgrade_speed" -> SPEED;
                case "upgrade_energy" -> ENERGY;
                case "upgrade_muffling" -> MUFFLING;
                default -> null;
            };
        }
        return null;
    }

    /**
     * 创建对应的升级物品堆
     */
    public ItemStack createStack(int count) {
        if (count <= 0) return ItemStack.EMPTY;
        if (this == PERFECT_FORGING) {
            return new ItemStack(ModItems.UPGRADE_PERFECT_FORGING.get(), count);
        }
        net.minecraft.resources.ResourceLocation loc = new net.minecraft.resources.ResourceLocation("mekanism", "upgrade_" + this.name);
        net.minecraft.world.item.Item item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(loc);
        if (item != null && item != net.minecraft.world.item.Items.AIR) {
            return new ItemStack(item, count);
        }
        return ItemStack.EMPTY;
    }
}

