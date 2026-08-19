package org.shengxi.registry;

import net.dries007.tfc.common.LevelTier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

/**
 * MekaTFC 工具材质层级定义
 * 实现 TFC 的 LevelTier 接口，供 TFC 工具系统（探矿镐、小刀、镰刀等）读取金属等级与属性。
 */
public enum ModTiers implements LevelTier {
    // 锇工具层级：等级 3（与熟铁同阶），耐久度 1024，挖掘速度 8.0，攻击伤害加成 4.0，附魔能力 14
    OSMIUM(
            3,
            1024,
            8.0f,
            4.0f,
            14,
            BlockTags.INCORRECT_FOR_IRON_TOOL,
            () -> Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "ingots/osmium")))
    );

    private final int level;
    private final int uses;
    private final float speed;
    private final float attackDamageBonus;
    private final int enchantmentValue;
    private final TagKey<Block> incorrectBlocksForDrops;
    private final Supplier<Ingredient> repairIngredient;

    ModTiers(int level, int uses, float speed, float attackDamageBonus, int enchantmentValue,
             TagKey<Block> incorrectBlocksForDrops, Supplier<Ingredient> repairIngredient) {
        this.level = level;
        this.uses = uses;
        this.speed = speed;
        this.attackDamageBonus = attackDamageBonus;
        this.enchantmentValue = enchantmentValue;
        this.incorrectBlocksForDrops = incorrectBlocksForDrops;
        this.repairIngredient = repairIngredient;
    }

    /**
     * TFC 金属层级等级（例如 1=铜, 2=青铜, 3=熟铁/锇, 4=钢, 5=黑钢, 6=红/蓝钢）
     */
    @Override
    public int level() {
        return level;
    }

    @Override
    public int getUses() {
        return uses;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return attackDamageBonus;
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return incorrectBlocksForDrops;
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient.get();
    }
}
