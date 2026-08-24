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
    public void inspectTfcTags() throws Exception {
        java.io.File jarFile = new java.io.File("C:\\Users\\JuziD\\.gradle\\caches\\modules-2\\files-2.1\\maven.modrinth\\terrafirmacraft\\4.2.8\\aff8713c181fa4b190f3b018216998530c734a12\\terrafirmacraft-4.2.8.jar");
        if (jarFile.exists()) {
            try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(jarFile)) {
                var it = zip.entries();
                while (it.hasMoreElements()) {
                    var entry = it.nextElement();
                    String name = entry.getName();
                    if (name.startsWith("data/c/tags/item/") && (name.contains("sheet") || name.contains("ingot"))) {
                        System.out.println("=== Tag File: " + name + " ===");
                        try (var is = zip.getInputStream(entry)) {
                            System.out.println(new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
                        }
                    }
                }
            }
        }
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

    @Test
    public void testAllDataJsonSyntax() throws Exception {
        com.google.gson.Gson gson = new com.google.gson.Gson();
        java.nio.file.Path dataPath = java.nio.file.Paths.get("src/main/resources/data");
        Assertions.assertTrue(java.nio.file.Files.exists(dataPath), "数据包目录必须存在");

        java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger(0);
        try (java.util.stream.Stream<java.nio.file.Path> stream = java.nio.file.Files.walk(dataPath)) {
            stream.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                try {
                    String content = java.nio.file.Files.readString(p, java.nio.charset.StandardCharsets.UTF_8);
                    com.google.gson.JsonElement element = gson.fromJson(content, com.google.gson.JsonElement.class);
                    Assertions.assertNotNull(element, "JSON 内容解析不能为空: " + p);
                    count.incrementAndGet();
                } catch (Exception e) {
                    Assertions.fail("JSON 语法解析失败: " + p + ", 错误: " + e.getMessage());
                }
            });
        }
        Assertions.assertTrue(count.get() > 0, "应当校验至少 1 个 JSON 文件");
    }
}
