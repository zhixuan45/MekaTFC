package org.shengxi.common.util;

import net.dries007.tfc.common.capabilities.forge.ForgeRule;
import net.dries007.tfc.common.capabilities.forge.ForgeStep;
import net.dries007.tfc.common.capabilities.forge.ForgeSteps;

import java.util.*;

/**
 * 自动锻造求解器 (AutoForgeSolver)
 * 根据工件当前进度 (currentWork)、目标进度 (targetWork) 以及配方要求的规则 (rules)，
 * 通过广度优先搜索 (BFS) 计算出能够到达目标且满足所有规则的最短击打步骤序列。
 */
public class AutoForgeSolver {

    // 最大允许的搜索深度（防止极端情况下无解导致性能受损）
    private static final int MAX_DEPTH = 18;
    // 有效锻造工作进度上下限（根据 TFC 铁砧游标边界通常在 0 到 150）
    private static final int MIN_WORK = 0;
    private static final int MAX_WORK = 150;

    /**
     * 搜索状态节点
     */
    private record State(
            int work,
            ForgeStep step1, // 最新的一步 (last)
            ForgeStep step2, // 倒数第二步 (secondLast)
            ForgeStep step3  // 倒数第三步 (thirdLast)
    ) {}

    /**
     * 求解最优击打动作序列
     *
     * @param currentWork  工件当前锻造位置
     * @param targetWork   配方目标锻造位置
     * @param currentSteps 当前已执行的步骤历史
     * @param rules        配方要求的规则（通常为 3 个）
     * @return 最优步骤列表；若无法在深度限制内求解则返回空列表
     */
    public static List<ForgeStep> solve(int currentWork, int targetWork, ForgeSteps currentSteps, ForgeRule[] rules) {
        if (rules == null) {
            rules = new ForgeRule[0];
        }

        // 初始状态
        ForgeStep initStep1 = currentSteps != null ? currentSteps.last() : null;
        ForgeStep initStep2 = currentSteps != null ? currentSteps.secondLast() : null;
        ForgeStep initStep3 = currentSteps != null ? currentSteps.thirdLast() : null;

        State startState = new State(currentWork, initStep1, initStep2, initStep3);

        // 如果初始状态已经完全满足目标和规则，无需额外击打
        if (currentWork == targetWork && matchesAll(startState, rules)) {
            return Collections.emptyList();
        }

        // BFS 队列与访问集合
        Queue<Node> queue = new ArrayDeque<>();
        Set<State> visited = new HashSet<>();

        queue.add(new Node(startState, null, null, 0));
        visited.add(startState);

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            if (current.depth >= MAX_DEPTH) {
                continue;
            }

            // 遍历 TFC 的 8 种可能动作
            for (ForgeStep nextStep : ForgeStep.values()) {
                int nextWork = current.state.work + nextStep.step();

                // 限制在铁砧进度条合法范围内
                if (nextWork < MIN_WORK || nextWork > MAX_WORK) {
                    continue;
                }

                // 更新滑动窗口（最新一步为 nextStep，原 step1 变为 step2，原 step2 变为 step3）
                State nextState = new State(
                        nextWork,
                        nextStep,
                        current.state.step1,
                        current.state.step2
                );

                if (visited.add(nextState)) {
                    Node nextNode = new Node(nextState, current, nextStep, current.depth + 1);

                    // 检查是否达成目标
                    if (nextWork == targetWork && matchesAll(nextState, rules)) {
                        return reconstructPath(nextNode);
                    }

                    queue.add(nextNode);
                }
            }
        }

        // 若未找到精确解，尝试退化为“仅靠拢目标位置”的贪心求解，避免死锁
        return solveFallback(currentWork, targetWork);
    }

    /**
     * 检查当前状态是否满足所有锻造规则
     */
    private static boolean matchesAll(State state, ForgeRule[] rules) {
        ForgeSteps tempSteps = new ForgeSteps();
        if (state.step3 != null) tempSteps.addStep(state.step3);
        if (state.step2 != null) tempSteps.addStep(state.step2);
        if (state.step1 != null) tempSteps.addStep(state.step1);

        for (ForgeRule rule : rules) {
            if (rule != null && !rule.matches(tempSteps)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 重构从起点到目标状态的步骤路径
     */
    private static List<ForgeStep> reconstructPath(Node targetNode) {
        List<ForgeStep> path = new ArrayList<>();
        Node curr = targetNode;
        while (curr != null && curr.action != null) {
            path.add(curr.action);
            curr = curr.parent;
        }
        Collections.reverse(path);
        return path;
    }

    /**
     * 备用求解器：当约束过于苛刻无法在限制深度内匹配时，优先逼近目标
     */
    private static List<ForgeStep> solveFallback(int currentWork, int targetWork) {
        List<ForgeStep> path = new ArrayList<>();
        int work = currentWork;

        for (int i = 0; i < 10 && work != targetWork; i++) {
            ForgeStep bestStep = null;
            int bestDist = Math.abs(targetWork - work);

            for (ForgeStep step : ForgeStep.values()) {
                int nextWork = work + step.step();
                if (nextWork < MIN_WORK || nextWork > MAX_WORK) continue;
                int dist = Math.abs(targetWork - nextWork);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestStep = step;
                }
            }

            if (bestStep == null) break;
            path.add(bestStep);
            work += bestStep.step();
        }

        return path;
    }

    /**
     * BFS 节点结构
     */
    private static class Node {
        final State state;
        final Node parent;
        final ForgeStep action;
        final int depth;

        Node(State state, Node parent, ForgeStep action, int depth) {
            this.state = state;
            this.parent = parent;
            this.action = action;
            this.depth = depth;
        }
    }
}
