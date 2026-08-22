package org.shengxi;

import net.dries007.tfc.common.capabilities.forge.ForgeRule;
import net.dries007.tfc.common.capabilities.forge.ForgeStep;
import net.dries007.tfc.common.capabilities.forge.ForgeSteps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shengxi.common.util.AutoForgeSolver;

import java.util.List;

/**
 * 自动锻造求解器单元测试
 */
public class AutoForgeSolverTest {

    @Test
    public void testDirectHit() {
        // 当前位置 50，目标 65 (差 +15，DRAW 刚好 +15)
        int currentWork = 50;
        int targetWork = 65;
        ForgeSteps steps = new ForgeSteps();
        ForgeRule[] rules = new ForgeRule[]{ForgeRule.DRAW_LAST};

        List<ForgeStep> solution = AutoForgeSolver.solve(currentWork, targetWork, steps, rules);
        Assertions.assertFalse(solution.isEmpty(), "应该能找到解");

        // 模拟执行动作
        int work = currentWork;
        for (ForgeStep step : solution) {
            work += step.step();
            steps.addStep(step);
        }

        Assertions.assertEquals(targetWork, work, "最终工作进度应精确等于目标值");
        for (ForgeRule rule : rules) {
            Assertions.assertTrue(rule.matches(steps), "最终执行历史应满足规则: " + rule);
        }
    }

    @Test
    public void testComplexThreeRules() {
        // 当前位置 30，目标 90
        // 规则：末步 PUNCH_LAST, 次步 BEND_SECOND_LAST, 倒数第三步 DRAW_THIRD_LAST
        int currentWork = 30;
        int targetWork = 90;
        ForgeSteps steps = new ForgeSteps();
        ForgeRule[] rules = new ForgeRule[]{
                ForgeRule.PUNCH_LAST,
                ForgeRule.BEND_SECOND_LAST,
                ForgeRule.DRAW_THIRD_LAST
        };

        List<ForgeStep> solution = AutoForgeSolver.solve(currentWork, targetWork, steps, rules);
        Assertions.assertFalse(solution.isEmpty(), "应能求解复杂三步规则");

        int work = currentWork;
        for (ForgeStep step : solution) {
            work += step.step();
            steps.addStep(step);
        }

        Assertions.assertEquals(targetWork, work, "最终进度应等于目标值 90");
        for (ForgeRule rule : rules) {
            Assertions.assertTrue(rule.matches(steps), "最终执行历史应满足规则: " + rule);
        }
    }
}
