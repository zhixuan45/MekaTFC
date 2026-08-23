package org.shengxi;

import net.dries007.tfc.common.component.forge.ForgeRule;
import net.dries007.tfc.common.component.forge.ForgeStep;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shengxi.common.util.AutoForgeSolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 自动锻造求解算法 (AutoForgeSolver) 单元测试
 */
public class AutoForgeSolverTest {

    @Test
    public void testDirectHitToTargetWithoutRules() {
        int currentWork = 50;
        int targetWork = 52; // HIT_LIGHT (+2)
        List<ForgeStep> steps = Collections.emptyList();
        List<ForgeRule> rules = Collections.emptyList();

        List<ForgeStep> path = AutoForgeSolver.solve(currentWork, targetWork, steps, rules);

        Assertions.assertFalse(path.isEmpty(), "应当能找到解");
        Assertions.assertEquals(1, path.size());
        Assertions.assertEquals(ForgeStep.PUNCH, path.get(0));
    }

    @Test
    public void testAlreadyAtTarget() {
        int currentWork = 70;
        int targetWork = 70;
        List<ForgeStep> steps = Collections.emptyList();
        List<ForgeRule> rules = Collections.emptyList();

        List<ForgeStep> path = AutoForgeSolver.solve(currentWork, targetWork, steps, rules);

        Assertions.assertTrue(path.isEmpty(), "初始已达目标时返回空步骤");
    }

    @Test
    public void testMultipleStepsWithRuleConstraints() {
        int currentWork = 30;
        int targetWork = 45;
        List<ForgeStep> steps = Collections.emptyList();
        List<ForgeRule> rules = List.of(
                ForgeRule.PUNCH_LAST,
                ForgeRule.DRAW_ANY
        );

        List<ForgeStep> path = AutoForgeSolver.solve(currentWork, targetWork, steps, rules);

        Assertions.assertFalse(path.isEmpty(), "应当求解出满足规则的序列");

        // 模拟执行路径
        int work = currentWork;
        List<ForgeStep> simSteps = new ArrayList<>();
        for (ForgeStep s : path) {
            work += s.step();
            simSteps.add(s);
        }

        Assertions.assertEquals(targetWork, work, "最终进度必须精准等于 targetWork");

        int size = simSteps.size();
        ForgeStep step1 = size > 0 ? simSteps.get(size - 1) : null;
        ForgeStep step2 = size > 1 ? simSteps.get(size - 2) : null;
        ForgeStep step3 = size > 2 ? simSteps.get(size - 3) : null;

        for (ForgeRule rule : rules) {
            Assertions.assertTrue(rule.matches(step1, step2, step3), "执行后的步骤序列必须满足规则: " + rule);
        }
    }
}
