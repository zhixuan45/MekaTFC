package org.shengxi;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.shengxi.config.RecipeMode;

/**
 * MekaTFC 模组配置文件
 */
@EventBusSubscriber(modid = Mekatfc.MODID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // 合成模式配置：简单 (EASY)、普通 (NORMAL)、硬核 (HARDCORE)
    private static final ModConfigSpec.EnumValue<RecipeMode> RECIPE_MODE = BUILDER
            .comment(
                    "MekaTFC 的合成模式与难度档位：",
                    "  EASY (简单模式，模组默认值)：所有配方默认开放，保留通用机械与原版快捷工作台通道。",
                    "  NORMAL (普通模式)：关闭大部分简化捷径配方（如直接工作台/冶金灌注合成钢等），强制要求结合 TFC 冶金与铁砧锻造工艺。",
                    "  HARDCORE (硬核模式)：深度硬核模式，重塑工业温标与严格能耗（预留档位）。"
            )
            .defineEnum("recipeMode", RecipeMode.EASY);

    static final ModConfigSpec SPEC = BUILDER.build();

    // 当前生效的合成模式
    public static RecipeMode recipeMode = RecipeMode.EASY;

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        recipeMode = RECIPE_MODE.get();
    }
}
