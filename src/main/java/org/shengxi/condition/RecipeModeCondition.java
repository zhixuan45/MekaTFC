package org.shengxi.condition;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import org.shengxi.Config;
import org.shengxi.Mekatfc;
import org.shengxi.config.RecipeMode;

import java.util.Optional;

/**
 * 根据配置中的合成模式（简单/普通/硬核）判断当前配方是否启用的条件
 */
public class RecipeModeCondition implements ICondition {
    public static final ResourceLocation NAME = new ResourceLocation(Mekatfc.MODID, "recipe_mode");

    private final Optional<RecipeMode> minMode;
    private final Optional<RecipeMode> maxMode;

    public RecipeModeCondition(Optional<RecipeMode> minMode, Optional<RecipeMode> maxMode) {
        this.minMode = minMode;
        this.maxMode = maxMode;
    }

    public Optional<RecipeMode> minMode() {
        return minMode;
    }

    public Optional<RecipeMode> maxMode() {
        return maxMode;
    }

    @Override
    public ResourceLocation getID() {
        return NAME;
    }

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

    /**
     * 1.20.1 Forge 配方条件序列化器
     */
    public static class Serializer implements IConditionSerializer<RecipeModeCondition> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void write(JsonObject json, RecipeModeCondition value) {
            value.minMode.ifPresent(mode -> json.addProperty("min_mode", mode.name().toLowerCase()));
            value.maxMode.ifPresent(mode -> json.addProperty("max_mode", mode.name().toLowerCase()));
        }

        @Override
        public RecipeModeCondition read(JsonObject json) {
            Optional<RecipeMode> min = json.has("min_mode")
                    ? Optional.of(RecipeMode.valueOf(json.get("min_mode").getAsString().toUpperCase()))
                    : Optional.empty();
            Optional<RecipeMode> max = json.has("max_mode")
                    ? Optional.of(RecipeMode.valueOf(json.get("max_mode").getAsString().toUpperCase()))
                    : Optional.empty();
            return new RecipeModeCondition(min, max);
        }

        @Override
        public ResourceLocation getID() {
            return NAME;
        }
    }
}

