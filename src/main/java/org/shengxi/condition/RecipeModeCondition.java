package org.shengxi.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.shengxi.Config;
import org.shengxi.config.RecipeMode;

import java.util.Optional;

/**
 * 根据配置中的合成模式（简单/普通/硬核）判断当前配方是否启用的条件
 */
public record RecipeModeCondition(Optional<RecipeMode> minMode, Optional<RecipeMode> maxMode) implements ICondition {

    public static final MapCodec<RecipeModeCondition> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.STRING.xmap(
                    s -> RecipeMode.valueOf(s.toUpperCase()),
                    RecipeMode::name
            ).optionalFieldOf("min_mode").forGetter(RecipeModeCondition::minMode),
            Codec.STRING.xmap(
                    s -> RecipeMode.valueOf(s.toUpperCase()),
                    RecipeMode::name
            ).optionalFieldOf("max_mode").forGetter(RecipeModeCondition::maxMode)
    ).apply(builder, RecipeModeCondition::new));

    @Override
    public boolean test(IContext context) {
        RecipeMode currentMode = Config.recipeMode;
        if (currentMode == null) {
            currentMode = RecipeMode.EASY;
        }

        // 检查最小模式门槛
        if (minMode.isPresent() && currentMode.compareTo(minMode.get()) < 0) {
            return false;
        }

        // 检查最大模式限制
        if (maxMode.isPresent() && currentMode.compareTo(maxMode.get()) > 0) {
            return false;
        }

        return true;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
