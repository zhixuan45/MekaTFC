package org.shengxi.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.dries007.tfc.common.component.forge.ForgeRule;
import net.dries007.tfc.common.component.forge.ForgeStep;
import net.dries007.tfc.common.component.forge.Forging;
import net.dries007.tfc.common.component.forge.ForgingCapability;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.dries007.tfc.common.recipes.TFCRecipeTypes;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.shengxi.client.gui.GuiMekanismSideTab;
import org.shengxi.client.gui.GuiMekanismSubWindow;
import org.shengxi.client.gui.button.AutoForgeChipButton;
import org.shengxi.client.gui.button.PlanTargetButton;
import org.shengxi.client.gui.button.RecipeLockButton;
import org.shengxi.client.gui.button.StepActionButton;
import org.shengxi.client.gui.button.WeldActionButton;
import org.shengxi.common.blockentity.ElectricForgeBlockEntity;
import org.shengxi.common.container.ElectricForgeMenu;
import org.shengxi.common.network.ModPackets;
import org.shengxi.common.upgrade.ForgeUpgradeType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 自动锻造机图形界面 (ElectricForgeScreen)
 * 完美结合 TFC 经典锻造界面与 Mekanism 工业级机器架构：
 * 1. 包含 TFC 经典 8 动作按钮、锻造游标、规则指示灯、历史指示灯与 Plan 图纸选择；
 * 2. 深度复刻 Mekanism 左侧侧面配置与传输配置标签页、右侧升级管理与安全锁定标签页；
 * 3. 右侧集成 Mekanism 标志性青绿色细分条纹垂直能量条 (Vertical Power Bar)；
 * 4. 内嵌支持侧面配置与升级管理的工业弹窗子窗口 (Sub-Windows)。
 */
public class ElectricForgeScreen extends AbstractContainerScreen<ElectricForgeMenu> {

    public static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("tfc", "textures/gui/anvil.png");

    private static final int PLAN_COLS = 6;
    private static final int PLAN_VISIBLE_ROWS = 3;

    // 侧边栏标签页
    private GuiMekanismSideTab sideConfigTab;
    private GuiMekanismSideTab transporterTab;
    private GuiMekanismSideTab upgradeTab;
    private GuiMekanismSideTab securityTab;

    // 弹窗子窗口 (Mekanism 风格)
    private GuiMekanismSubWindow sideConfigWindow;
    private GuiMekanismSubWindow transporterWindow;
    private GuiMekanismSubWindow upgradeWindow;

    // Plan 图纸选择覆盖面板状态
    private boolean planSelectionOpen = false;
    private int planScrollRow = 0;
    private boolean planScrollDragging = false;
    private List<AnvilRecipe> availableRecipes = new ArrayList<>();

    // 升级管理面板当前选中的升级类型
    private ForgeUpgradeType selectedUpgradeType = ForgeUpgradeType.SPEED;

    public ElectricForgeScreen(ElectricForgeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 208; // 完整适配 TFC 铁砧背景尺寸
        this.inventoryLabelY = 114; // 调整物品栏文字 y 坐标
    }

    @Override
    protected void init() {
        super.init();

        // 动态将主槽位的活跃状态与子窗口/覆盖层显隐联动，弹窗打开时彻底隐藏主槽位
        this.menu.setMainSlotsActiveSupplier(() -> !isAnyWindowVisible() && !this.planSelectionOpen);

        int left = this.leftPos;
        int top = this.topPos;
        Supplier<Boolean> isHiddenSupplier = () -> isAnyWindowVisible() || this.planSelectionOpen;

        // 1. 初始化 8 个击打动作按钮 (100% 采用 TFC 官方 buttonX / buttonY 像素级坐标)
        for (ForgeStep step : ForgeStep.values()) {
            this.addRenderableWidget(new StepActionButton(step, left, top, b -> {
                if (this.menu.blockEntity != null) {
                    ModPackets.sendToServer(new ModPackets.ForgeStepActionPayload(
                            this.menu.blockEntity.getBlockPos(),
                            step.ordinal()
                    ));
                }
            }, isHiddenSupplier));
        }

        // 2. 初始化焊接按钮 (100% 严格对齐 TFC 官方坐标 x: 21, y: 56)
        this.addRenderableWidget(new WeldActionButton(left + 21, top + 56, this.menu, b -> {
            if (this.menu.blockEntity != null) {
                ModPackets.sendToServer(new ModPackets.WeldActionPayload(
                        this.menu.blockEntity.getBlockPos()
                ));
            }
        }, isHiddenSupplier));

        // 3. 初始化 Plan 图纸目标选择按钮 (100% 严格对齐 TFC 官方坐标 x: 137, y: 56)
        this.addRenderableWidget(new PlanTargetButton(left + 137, top + 56, this.menu, b -> {
            this.planSelectionOpen = !this.planSelectionOpen;
            if (this.planSelectionOpen) {
                this.updateAvailableRecipes();
                closeSubWindows();
            }
        }, isHiddenSupplier));

        // 4. 配方锁定切换小按钮 (位于 Plan 按钮右侧 x: 157, y: 56)
        this.addRenderableWidget(new RecipeLockButton(left + 157, top + 56, this.menu, b -> {
            if (this.menu.blockEntity != null) {
                ModPackets.sendToServer(new ModPackets.ToggleRecipeLockPayload(
                        this.menu.blockEntity.getBlockPos()
                ));
            }
        }, isHiddenSupplier));

        // 5. 自动锻造切换芯片按钮 (顶部中心空白区 x: 74, y: 4，宽 28，高 14，自绘制清晰 LED 与文字)
        this.addRenderableWidget(new AutoForgeChipButton(left + 74, top + 4, this.menu, b -> {
            if (this.menu.blockEntity != null) {
                boolean nextState = !this.menu.isAutoForgeEnabled();
                ModPackets.sendToServer(new ModPackets.ToggleAutoForgePayload(
                        this.menu.blockEntity.getBlockPos(),
                        nextState
                ));
            }
        }, isHiddenSupplier));

        // ==================== Mekanism 侧边栏标签页初始化 ====================

        // 左侧上部：侧面配置标签页
        this.sideConfigTab = new GuiMekanismSideTab(
                left - 26, top + 6,
                GuiMekanismSideTab.TabSide.LEFT,
                GuiMekanismSideTab.TabIconType.SIDE_CONFIG,
                false,
                Component.translatable("gui.mekatfc.side_config.title"),
                b -> {
                    boolean next = !sideConfigWindow.isVisible();
                    closeAllWindows();
                    sideConfigWindow.setVisible(next);
                }
        );
        this.addRenderableWidget(sideConfigTab);

        // 左侧下部：传输与自动化配置标签页
        this.transporterTab = new GuiMekanismSideTab(
                left - 26, top + 34,
                GuiMekanismSideTab.TabSide.LEFT,
                GuiMekanismSideTab.TabIconType.TRANSPORTER,
                false,
                Component.translatable("gui.mekatfc.transporter_config.title"),
                b -> {
                    boolean next = !transporterWindow.isVisible();
                    closeAllWindows();
                    transporterWindow.setVisible(next);
                }
        );
        this.addRenderableWidget(transporterTab);

        // 右侧上部：升级管理标签页
        this.upgradeTab = new GuiMekanismSideTab(
                left + 176, top + 6,
                GuiMekanismSideTab.TabSide.RIGHT,
                GuiMekanismSideTab.TabIconType.UPGRADE,
                false,
                Component.translatable("gui.mekatfc.upgrade_window.title"),
                b -> {
                    boolean next = !upgradeWindow.isVisible();
                    closeAllWindows();
                    upgradeWindow.setVisible(next);
                }
        );
        this.addRenderableWidget(upgradeTab);

        // 右侧下部：安全与锁定标签页 (橙色边框)
        this.securityTab = new GuiMekanismSideTab(
                left + 176, top + 34,
                GuiMekanismSideTab.TabSide.RIGHT,
                this.menu.isRecipeLocked() ? GuiMekanismSideTab.TabIconType.SECURITY_LOCKED : GuiMekanismSideTab.TabIconType.SECURITY_UNLOCKED,
                true,
                Component.translatable("gui.mekatfc.security_tab.title"),
                b -> {
                    if (this.menu.blockEntity != null) {
                        ModPackets.sendToServer(new ModPackets.ToggleRecipeLockPayload(
                                this.menu.blockEntity.getBlockPos()
                        ));
                    }
                }
        );
        this.addRenderableWidget(securityTab);

        // ==================== Mekanism 弹窗子窗口初始化 ====================
        this.sideConfigWindow = new GuiMekanismSubWindow(
                GuiMekanismSubWindow.WindowType.SIDE_CONFIG,
                Component.translatable("gui.mekatfc.side_config.title"),
                146, 126
        );
        this.sideConfigWindow.setPosition(left + 15, top + 20);

        this.transporterWindow = new GuiMekanismSubWindow(
                GuiMekanismSubWindow.WindowType.TRANSPORTER_CONFIG,
                Component.translatable("gui.mekatfc.transporter_config.title"),
                146, 95
        );
        this.transporterWindow.setPosition(left + 15, top + 25);

        this.upgradeWindow = new GuiMekanismSubWindow(
                GuiMekanismSubWindow.WindowType.UPGRADE_MANAGEMENT,
                Component.translatable("gui.mekatfc.upgrade_window.title"),
                156, 116
        );
        this.upgradeWindow.setPosition(left + 10, top + 15);
    }

    private void closeSubWindows() {
        this.sideConfigWindow.setVisible(false);
        this.transporterWindow.setVisible(false);
        this.upgradeWindow.setVisible(false);
    }

    private void closeAllWindows() {
        closeSubWindows();
        this.planSelectionOpen = false;
    }

    private boolean isAnyWindowVisible() {
        return (sideConfigWindow != null && sideConfigWindow.isVisible())
                || (transporterWindow != null && transporterWindow.isVisible())
                || (upgradeWindow != null && upgradeWindow.isVisible());
    }

    /**
     * 更新当前工件可锻造的所有可用配方列表
     */
    private void updateAvailableRecipes() {
        this.availableRecipes.clear();
        this.planScrollRow = 0;
        this.planScrollDragging = false;
        if (this.minecraft != null && this.minecraft.level != null) {
            ItemStack mainStack = this.menu.getSlot(ElectricForgeBlockEntity.SLOT_MAIN).getItem();
            if (!mainStack.isEmpty()) {
                int tier = this.menu.getTier();
                var allRecipes = this.minecraft.level.getRecipeManager().getAllRecipesFor(TFCRecipeTypes.ANVIL.get());
                for (RecipeHolder<AnvilRecipe> holder : allRecipes) {
                    AnvilRecipe recipe = holder.value();
                    if (recipe.getInput().test(mainStack) && (tier < 0 || recipe.isCorrectTier(tier))) {
                        this.availableRecipes.add(recipe);
                    }
                }
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int left = this.leftPos;
        int top = this.topPos;

        // 1. 绘制 TFC 原版铁砧完整背景贴图 (176x208)
        graphics.blit(BACKGROUND, left, top, 0, 0, this.imageWidth, this.imageHeight);

        ItemStack mainStack = this.menu.getSlot(ElectricForgeBlockEntity.SLOT_MAIN).getItem();
        if (!mainStack.isEmpty()) {
            Forging forge = ForgingCapability.get(mainStack);
            if (forge != null) {
                int work = forge.work();
                int target = forge.target();

                // 2. 绘制锻造游标 (TFC 官方规范：绿色目标在 y+94, 红色当前进度在 y+100)
                int targetX = left + 13 + Math.max(0, Math.min(146, target));
                graphics.blit(BACKGROUND, targetX, top + 94, 181, 0, 5, 5);

                int workX = left + 13 + Math.max(0, Math.min(146, work));
                graphics.blit(BACKGROUND, workX, top + 100, 176, 0, 5, 5);

                List<ForgeStep> steps = forge.lastSteps();
                AnvilRecipe recipe = this.menu.blockEntity != null ? this.menu.blockEntity.resolveRecipe(forge) : null;

                // 3. 绘制顶部 3 个目标规则图标与状态高亮边框 (100% 严格对齐 TFC 官方 AnvilScreen)
                if (recipe != null) {
                    List<ForgeRule> rules = recipe.getRules();
                    for (int i = 0; i < rules.size() && i < 3; i++) {
                        ForgeRule rule = rules.get(i);
                        if (rule != null) {
                            int offset = i * 19;
                            graphics.blit(BACKGROUND, left + 64 + offset, top + 16, (float) rule.iconX(), (float) rule.iconY(), 10, 10, 256, 256);

                            if (forge.matches(rule)) {
                                RenderSystem.setShaderColor(0.0F, 0.6F, 0.2F, 1.0F);
                            } else {
                                RenderSystem.setShaderColor(1.0F, 0.4F, 0.0F, 1.0F);
                            }
                            graphics.blit(BACKGROUND, left + 59 + offset, top + 13, 198, rule.overlayY(), 20, 22);
                            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                        }
                    }
                }

                // 4. 绘制历史动作图标 (100% 严格对齐 TFC 官方 AnvilScreen：倒序排列从右至左)
                int histIdx = 0;
                for (ForgeStep histStep : forge.lastSteps()) {
                    if (histIdx >= 3) break;
                    graphics.blit(BACKGROUND, left + 99 - histIdx * 19, top + 34, 16, 16,
                            (float) histStep.iconX(), (float) (histStep.iconY() - 16), 16, 16, 256, 256);
                    histIdx++;
                }
            }
        }

        // 5. 绘制 Mekanism 经典垂直能量条 (Vertical Power Bar，位于右上侧 x: 167, y: 7，宽 4，高 52)
        int energyStored = this.menu.getEnergyStored();
        int maxEnergy = this.menu.getMaxEnergyStored();
        float energyPercent = maxEnergy > 0 ? (float) energyStored / (float) maxEnergy : 0.0f;
        int barHeight = Math.round(energyPercent * 50);

        int barX = left + 167;
        int barY = top + 7;

        // 能量槽背景
        graphics.fill(barX - 1, barY - 1, barX + 5, barY + 51, 0xFF1B1B1B);
        graphics.fill(barX, barY, barX + 4, barY + 50, 0xFF353535);

        // 绘制青绿色分格能量填充
        if (barHeight > 0) {
            int fillTop = barY + 50 - barHeight;
            graphics.fill(barX, fillTop, barX + 4, barY + 50, 0xFF00E5FF);
            for (int lineY = barY + 50; lineY >= fillTop; lineY -= 2) {
                graphics.fill(barX, lineY, barX + 4, lineY + 1, 0xFF0099AA);
            }
        }

        // 7. 渲染 Plan 图纸选择覆盖面板
        if (this.planSelectionOpen) {
            renderPlanSelection(graphics, mouseX, mouseY);
        }

        // 8. 渲染弹窗子窗口
        renderSubWindows(graphics, mouseX, mouseY);
    }

    /**
     * 绘制 Plan 图纸选择覆盖层
     */
    private void renderPlanSelection(GuiGraphics graphics, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        // 面板背景与边框 (深色现代工业风)
        graphics.fill(left + 8, top + 18, left + 168, top + 112, 0xEE1E1E1E);
        graphics.fill(left + 9, top + 19, left + 167, top + 111, 0xFF2B2B2B);

        graphics.drawString(this.font, Component.translatable("gui.mekatfc.electric_forge.select_plan"), left + 14, top + 22, 0xFFE0E0E0, false);

        // 右上角 X 关闭按钮 (x: 154, y: 20，宽 12，高 12)
        int closeBtnX = left + 154;
        int closeBtnY = top + 20;
        boolean closeHovered = (mouseX >= closeBtnX && mouseX <= closeBtnX + 12 && mouseY >= closeBtnY && mouseY <= closeBtnY + 12);
        graphics.fill(closeBtnX, closeBtnY, closeBtnX + 12, closeBtnY + 12, closeHovered ? 0xFFCC2222 : 0xFF3E3E3E);
        graphics.drawCenteredString(this.font, "✕", closeBtnX + 6, closeBtnY + 2, 0xFFFFFFFF);

        if (availableRecipes.isEmpty()) {
            graphics.drawString(this.font, Component.translatable("gui.mekatfc.electric_forge.no_plans"), left + 14, top + 45, 0xFFAAAAAA, false);
            return;
        }

        int totalRows = (int) Math.ceil((double) availableRecipes.size() / PLAN_COLS);
        int startIndex = planScrollRow * PLAN_COLS;

        for (int i = 0; i < PLAN_COLS * PLAN_VISIBLE_ROWS; i++) {
            int recipeIndex = startIndex + i;
            if (recipeIndex >= availableRecipes.size()) break;

            AnvilRecipe recipe = availableRecipes.get(recipeIndex);
            int row = i / PLAN_COLS;
            int col = i % PLAN_COLS;
            int itemX = left + 14 + col * 22;
            int itemY = top + 36 + row * 22;

            // 槽位底框与悬停高亮
            boolean hovered = (mouseX >= itemX && mouseX <= itemX + 18 && mouseY >= itemY && mouseY <= itemY + 18);
            graphics.fill(itemX, itemY, itemX + 18, itemY + 18, hovered ? 0xFF4A4A4A : 0xFF353535);

            if (this.minecraft != null && this.minecraft.level != null) {
                ItemStack result = recipe.getResultItem(this.minecraft.level.registryAccess());
                graphics.renderItem(result, itemX + 1, itemY + 1);
            }
        }

        // 绘制滚动条
        if (totalRows > PLAN_VISIBLE_ROWS) {
            int scrollTrackX = left + 154;
            int scrollTrackY = top + 36;
            int scrollTrackHeight = 62;

            graphics.fill(scrollTrackX, scrollTrackY, scrollTrackX + 6, scrollTrackY + scrollTrackHeight, 0xFF1A1A1A);

            int maxScroll = totalRows - PLAN_VISIBLE_ROWS;
            int thumbHeight = Math.max(12, scrollTrackHeight * PLAN_VISIBLE_ROWS / totalRows);
            int thumbY = scrollTrackY + (scrollTrackHeight - thumbHeight) * planScrollRow / maxScroll;

            graphics.fill(scrollTrackX, thumbY, scrollTrackX + 6, thumbY + thumbHeight, 0xFF555555);
        }
    }

    /**
     * 绘制 Mekanism 风格弹窗子窗口
     */
    private void renderSubWindows(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.sideConfigWindow.isVisible()) {
            this.sideConfigWindow.render(graphics, this.font, mouseX, mouseY);
            renderSideConfigContent(graphics, mouseX, mouseY);
        }

        if (this.transporterWindow.isVisible()) {
            this.transporterWindow.render(graphics, this.font, mouseX, mouseY);
            renderTransporterContent(graphics, mouseX, mouseY);
        }

        if (this.upgradeWindow.isVisible()) {
            this.upgradeWindow.render(graphics, this.font, mouseX, mouseY);
            renderUpgradeContent(graphics, mouseX, mouseY);
        }
    }

    private void renderSideConfigContent(GuiGraphics graphics, int mouseX, int mouseY) {
        int winX = this.sideConfigWindow.getX();
        int winY = this.sideConfigWindow.getY();

        graphics.drawString(this.font, "Side IO Config", winX + 8, winY + 20, 0xFFAAAAAA, false);

        int[][] layout = {
                {-1, 1, -1}, // Top: UP (1)
                {4, 0, 5},   // Middle: WEST(4), DOWN(0), EAST(5)
                {-1, 2, 3}   // Bottom: NORTH(2), SOUTH(3)
        };
        String[][] labels = {
                {"", "UP", ""},
                {"W", "DN", "E"},
                {"", "N", "S"}
        };

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                int sideIdx = layout[r][c];
                if (sideIdx >= 0) {
                    int btnX = winX + 42 + c * 22;
                    int btnY = winY + 34 + r * 22;
                    int config = this.menu.blockEntity != null ? this.menu.blockEntity.getSideConfig(Direction.values()[sideIdx]) : 0;

                    int color = switch (config) {
                        case 0 -> 0xFF00AA00; // INPUT (绿色)
                        case 1 -> 0xFFAA0000; // OUTPUT (红色)
                        case 2 -> 0xFF0099FF; // ENERGY_ONLY (蓝色)
                        default -> 0xFF555555; // DISABLED (灰色)
                    };

                    graphics.fill(btnX, btnY, btnX + 20, btnY + 20, 0xFF101010);
                    graphics.fill(btnX + 1, btnY + 1, btnX + 19, btnY + 19, color);
                    graphics.drawCenteredString(this.font, labels[r][c], btnX + 10, btnY + 6, 0xFFFFFFFF);
                }
            }
        }
    }

    private void renderTransporterContent(GuiGraphics graphics, int mouseX, int mouseY) {
        int winX = this.transporterWindow.getX();
        int winY = this.transporterWindow.getY();

        graphics.drawString(this.font, "Auto-Eject to Output Face:", winX + 8, winY + 22, 0xFFAAAAAA, false);

        boolean eject = this.menu.blockEntity != null && this.menu.blockEntity.isAutoEject();
        int btnX = winX + 14;
        int btnY = winY + 38;

        graphics.fill(btnX, btnY, btnX + 118, btnY + 20, 0xFF101010);
        graphics.fill(btnX + 1, btnY + 1, btnX + 117, btnY + 19, eject ? 0xFF008800 : 0xFF444444);
        graphics.drawCenteredString(this.font, eject ? "AUTO-EJECT: ON" : "AUTO-EJECT: OFF", btnX + 59, btnY + 6, 0xFFFFFFFF);
    }

    private void renderUpgradeContent(GuiGraphics graphics, int mouseX, int mouseY) {
        int winX = this.upgradeWindow.getX();
        int winY = this.upgradeWindow.getY();

        // 1. 左侧升级类型列表 (4个)
        ForgeUpgradeType[] types = ForgeUpgradeType.values();
        for (int i = 0; i < types.length; i++) {
            ForgeUpgradeType type = types[i];
            int itemY = winY + 20 + i * 22;
            boolean isSelected = (type == this.selectedUpgradeType);
            boolean hovered = (mouseX >= winX + 6 && mouseX <= winX + 68 && mouseY >= itemY && mouseY <= itemY + 20);

            int count = this.menu.getUpgradeCount(type);
            graphics.fill(winX + 6, itemY, winX + 68, itemY + 20, isSelected ? 0xFF007788 : (hovered ? 0xFF3E3E3E : 0xFF2A2A2A));

            ItemStack iconStack = type.createStack(1);
            if (!iconStack.isEmpty()) {
                graphics.renderItem(iconStack, winX + 8, itemY + 2);
            }
            graphics.drawString(this.font, count + "/" + type.getMaxCount(), winX + 28, itemY + 6, 0xFFFFFFFF, false);
        }

        // 2. 右侧详情与操作面板
        if (this.selectedUpgradeType != null) {
            graphics.drawString(this.font, this.selectedUpgradeType.getTitle(), winX + 74, winY + 20, 0xFF00FFC8, false);

            int installed = this.menu.getUpgradeCount(this.selectedUpgradeType);
            if (installed > 0) {
                int btn1X = winX + 74;
                int btn1Y = winY + 65;
                graphics.fill(btn1X, btn1Y, btn1X + 36, btn1Y + 16, 0xFF101010);
                graphics.fill(btn1X + 1, btn1Y + 1, btn1X + 35, btn1Y + 15, 0xFF4A4A4A);
                graphics.drawCenteredString(this.font, "-1", btn1X + 18, btn1Y + 4, 0xFFFFFFFF);

                int btnAllX = winX + 114;
                int btnAllY = winY + 65;
                graphics.fill(btnAllX, btnAllY, btnAllX + 36, btnAllY + 16, 0xFF101010);
                graphics.fill(btnAllX + 1, btnAllY + 1, btnAllX + 35, btnAllY + 15, 0xFF662222);
                graphics.drawCenteredString(this.font, "ALL", btnAllX + 18, btnAllY + 4, 0xFFFFFFFF);
            }

            // 升级安装进度条
            int ticks = this.menu.getUpgradeTicks();
            if (ticks > 0) {
                int barW = Math.min(68, (ticks * 68) / ElectricForgeBlockEntity.UPGRADE_TICKS_REQUIRED);
                graphics.fill(winX + 74, winY + 86, winX + 142, winY + 90, 0xFF101010);
                graphics.fill(winX + 74, winY + 86, winX + 74 + barW, winY + 90, 0xFF00FFC8);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!isAnyWindowVisible() && !this.planSelectionOpen) {
            super.renderLabels(graphics, mouseX, mouseY);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        // Mekanism 侧边栏悬停提示
        if (this.sideConfigTab != null) this.sideConfigTab.renderCustomTooltip(graphics, mouseX, mouseY);
        if (this.transporterTab != null) this.transporterTab.renderCustomTooltip(graphics, mouseX, mouseY);
        if (this.upgradeTab != null) this.upgradeTab.renderCustomTooltip(graphics, mouseX, mouseY);
        if (this.securityTab != null) this.securityTab.renderCustomTooltip(graphics, mouseX, mouseY);

        // 垂直能量条悬停提示
        int barX = this.leftPos + 167;
        int barY = this.topPos + 7;
        if (mouseX >= barX && mouseX <= barX + 4 && mouseY >= barY && mouseY <= barY + 50) {
            Component energyTip = Component.translatable("gui.mekatfc.electric_forge.energy",
                    String.format("%,d", this.menu.getEnergyStored()),
                    String.format("%,d", this.menu.getMaxEnergyStored()));
            graphics.renderTooltip(this.font, energyTip, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 1. 优先处理弹窗子窗口点击
        if (this.sideConfigWindow.isVisible() && this.sideConfigWindow.mouseClicked(mouseX, mouseY, button)) {
            int winX = this.sideConfigWindow.getX();
            int winY = this.sideConfigWindow.getY();
            int[][] layout = {
                    {-1, 1, -1},
                    {4, 0, 5},
                    {-1, 2, 3}
            };
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    int sideIdx = layout[r][c];
                    if (sideIdx >= 0) {
                        int btnX = winX + 42 + c * 22;
                        int btnY = winY + 34 + r * 22;
                        if (mouseX >= btnX && mouseX <= btnX + 20 && mouseY >= btnY && mouseY <= btnY + 20) {
                            if (this.menu.blockEntity != null) {
                                ModPackets.sendToServer(new ModPackets.UpdateSideConfigPayload(
                                        this.menu.blockEntity.getBlockPos(),
                                        sideIdx,
                                        false
                                ));
                            }
                            return true;
                        }
                    }
                }
            }
            return true;
        }

        if (this.transporterWindow.isVisible() && this.transporterWindow.mouseClicked(mouseX, mouseY, button)) {
            int winX = this.transporterWindow.getX();
            int winY = this.transporterWindow.getY();
            int btnX = winX + 14;
            int btnY = winY + 38;
            if (mouseX >= btnX && mouseX <= btnX + 118 && mouseY >= btnY && mouseY <= btnY + 20) {
                if (this.menu.blockEntity != null) {
                    ModPackets.sendToServer(new ModPackets.UpdateSideConfigPayload(
                            this.menu.blockEntity.getBlockPos(),
                            0,
                            true
                    ));
                }
                return true;
            }
            return true;
        }

        if (this.upgradeWindow.isVisible() && this.upgradeWindow.mouseClicked(mouseX, mouseY, button)) {
            int winX = this.upgradeWindow.getX();
            int winY = this.upgradeWindow.getY();

            // 检查左侧类型切换
            ForgeUpgradeType[] types = ForgeUpgradeType.values();
            for (int i = 0; i < types.length; i++) {
                int itemY = winY + 20 + i * 22;
                if (mouseX >= winX + 6 && mouseX <= winX + 68 && mouseY >= itemY && mouseY <= itemY + 20) {
                    this.selectedUpgradeType = types[i];
                    return true;
                }
            }

            // 检查卸载按钮
            if (this.selectedUpgradeType != null && this.menu.getUpgradeCount(this.selectedUpgradeType) > 0) {
                int btn1X = winX + 74;
                int btn1Y = winY + 65;
                if (mouseX >= btn1X && mouseX <= btn1X + 36 && mouseY >= btn1Y && mouseY <= btn1Y + 16) {
                    if (this.menu.blockEntity != null) {
                        ModPackets.sendToServer(new ModPackets.UninstallUpgradePayload(
                                this.menu.blockEntity.getBlockPos(),
                                this.selectedUpgradeType.ordinal(),
                                false
                        ));
                    }
                    return true;
                }

                int btnAllX = winX + 114;
                int btnAllY = winY + 65;
                if (mouseX >= btnAllX && mouseX <= btnAllX + 36 && mouseY >= btnAllY && mouseY <= btnAllY + 16) {
                    if (this.menu.blockEntity != null) {
                        ModPackets.sendToServer(new ModPackets.UninstallUpgradePayload(
                                this.menu.blockEntity.getBlockPos(),
                                this.selectedUpgradeType.ordinal(),
                                true
                        ));
                    }
                    return true;
                }
            }
            return true;
        }

        // 2. 处理 Plan 图纸选择层点击与选择
        if (this.planSelectionOpen) {
            int left = this.leftPos;
            int top = this.topPos;

            // 检查是否点击右上角 X 关闭按钮
            int closeBtnX = left + 154;
            int closeBtnY = top + 20;
            if (mouseX >= closeBtnX && mouseX <= closeBtnX + 12 && mouseY >= closeBtnY && mouseY <= closeBtnY + 12) {
                this.planSelectionOpen = false;
                return true;
            }

            int totalRows = (int) Math.ceil((double) availableRecipes.size() / PLAN_COLS);
            int startIndex = planScrollRow * PLAN_COLS;

            for (int i = 0; i < PLAN_COLS * PLAN_VISIBLE_ROWS; i++) {
                int recipeIndex = startIndex + i;
                if (recipeIndex >= availableRecipes.size()) break;

                int row = i / PLAN_COLS;
                int col = i % PLAN_COLS;
                int itemX = left + 14 + col * 22;
                int itemY = top + 36 + row * 22;

                if (mouseX >= itemX && mouseX <= itemX + 18 && mouseY >= itemY && mouseY <= itemY + 18) {
                    AnvilRecipe selected = availableRecipes.get(recipeIndex);
                    if (this.menu.blockEntity != null && this.minecraft != null && this.minecraft.level != null) {
                        var holders = this.minecraft.level.getRecipeManager().getAllRecipesFor(TFCRecipeTypes.ANVIL.get());
                        for (var h : holders) {
                            if (h.value() == selected) {
                                ModPackets.sendToServer(new ModPackets.SelectRecipePlanPayload(
                                        this.menu.blockEntity.getBlockPos(),
                                        h.id().toString()
                                ));
                                break;
                            }
                        }
                    }
                    this.planSelectionOpen = false;
                    return true;
                }
            }

            // 滚动条按住
            if (totalRows > PLAN_VISIBLE_ROWS) {
                int scrollTrackX = left + 154;
                int scrollTrackY = top + 36;
                if (mouseX >= scrollTrackX && mouseX <= scrollTrackX + 6 && mouseY >= scrollTrackY && mouseY <= scrollTrackY + 62) {
                    this.planScrollDragging = true;
                    return true;
                }
            }

            // 点击面板外部关闭 Plan 面板
            if (mouseX < left + 8 || mouseX > left + 168 || mouseY < top + 18 || mouseY > top + 112) {
                this.planSelectionOpen = false;
            }
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.sideConfigWindow.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        if (this.transporterWindow.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        if (this.upgradeWindow.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;

        if (this.planScrollDragging) {
            int top = this.topPos;
            int totalRows = (int) Math.ceil((double) availableRecipes.size() / PLAN_COLS);
            int maxScroll = Math.max(0, totalRows - PLAN_VISIBLE_ROWS);
            if (maxScroll > 0) {
                float relativeY = (float) (mouseY - (top + 36)) / 62.0f;
                this.planScrollRow = Math.max(0, Math.min(maxScroll, Math.round(relativeY * maxScroll)));
            }
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.planScrollDragging = false;
        if (this.sideConfigWindow.mouseReleased(mouseX, mouseY, button)) return true;
        if (this.transporterWindow.mouseReleased(mouseX, mouseY, button)) return true;
        if (this.upgradeWindow.mouseReleased(mouseX, mouseY, button)) return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.planSelectionOpen) {
            int totalRows = (int) Math.ceil((double) availableRecipes.size() / PLAN_COLS);
            int maxScroll = Math.max(0, totalRows - PLAN_VISIBLE_ROWS);
            if (maxScroll > 0) {
                if (scrollY > 0) {
                    this.planScrollRow = Math.max(0, this.planScrollRow - 1);
                } else if (scrollY < 0) {
                    this.planScrollRow = Math.min(maxScroll, this.planScrollRow + 1);
                }
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
