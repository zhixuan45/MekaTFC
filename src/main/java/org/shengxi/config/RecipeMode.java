package org.shengxi.config;

/**
 * MekaTFC 合成难度与配方模式枚举
 */
public enum RecipeMode {
    /**
     * 简单模式（默认模式）：
     * 保持模组默认值。所有原版与简化合成配方完全开放，提供顺畅的过渡体验。
     */
    EASY("easy", "简单模式：所有配方默认开放，保留原版/通用机械工作台快捷合成通道"),

    /**
     * 普通模式：
     * 去除部分简化配方（例如直接用工作台/冶金灌注合成钢等），强制要求结合 TFC 高炉、铁砧锻造与冶金工艺。
     */
    NORMAL("normal", "普通模式：禁用捷径配方，要求遵循 TFC 冶金与金属锻造标准"),

    /**
     * 硬核模式（预留档位）：
     * 深度结合 TFC 的温度衰减、耐压损耗与工业平衡规则，进一步提升工业化门槛。
     */
    HARDCORE("hardcore", "硬核模式：深度硬核平衡，重塑机械能源与工业链");

    private final String name;
    private final String description;

    RecipeMode(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name;
    }
}
