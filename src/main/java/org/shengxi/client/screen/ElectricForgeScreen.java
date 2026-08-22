package org.shengxi.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.dries007.tfc.common.capabilities.forge.ForgeRule;
import net.dries007.tfc.common.capabilities.forge.ForgeStep;
import net.dries007.tfc.common.capabilities.forge.ForgeSteps;
import net.dries007.tfc.common.capabilities.forge.Forging;
import net.dries007.tfc.common.capabilities.forge.ForgingCapability;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.dries007.tfc.common.recipes.TFCRecipeTypes;
import net.dries007.tfc.util.Helpers;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.shengxi.client.gui.GuiMekanismSideTab;
import org.shengxi.client.gui.GuiMekanismSubWindow;
import org.shengxi.common.blockentity.ElectricForgeBlockEntity;
import org.shengxi.common.container.ElectricForgeMenu;
import org.shengxi.common.network.ModPackets;


import java.util.ArrayList;
import java.util.List;

/**
 * 自动锻造机图形界面 (ElectricForgeScreen)
 * 完美结合 TFC 经典锻造界面与 Mekanism 工业级机器架构：
 * 1. 包含 TFC 经典 8 动作按钮、锻造游标、规则指示灯、历史指示灯与 Plan 图纸选择；
 * 2. 深度复刻 Mekanism 左侧侧面配置与传输配置标签页、右侧升级管理与安全锁定标签页；
 * 3. 右侧集成 Mekanism 标志性青绿色细分条纹垂直能量条 (Vertical Power Bar)；
 * 4. 内嵌支持侧面配置与升级管理的工业弹窗子窗口 (Sub-Windows)。
 */
public class ElectricForgeScreen extends AbstractContainerScreen<ElectricForgeMenu> {

    // TFC 铁砧背景贴图 (256x256)
    public static final ResourceLocation BACKGROUND = new ResourceLocation("tfc", "textures/gui/anvil.png");

    // 图纸选择面板状态
    private boolean planSelectionOpen = false;
    private final List<AnvilRecipe> availableRecipes = new ArrayList<>();

    // Mekanism 侧边栏标签页
    private GuiMekanismSideTab sideConfigTab;
    private GuiMekanismSideTab transporterTab;
    private GuiMekanismSideTab upgradeTab;
    private GuiMekanismSideTab securityTab;

    // Mekanism 子窗口
    private GuiMekanismSubWindow sideConfigWindow;
    private GuiMekanismSubWindow transporterWindow;
    private GuiMekanismSubWindow upgradeWindow;

    // 当前选中的升级类型
    private org.shengxi.common.upgrade.ForgeUpgradeType selectedUpgradeType = org.shengxi.common.upgrade.ForgeUpgradeType.SPEED;

    public ElectricForgeScreen(ElectricForgeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 208;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 115;
    }

    @Override
    protected void init() {
        super.init();

        int left = this.leftPos;
        int top = this.topPos;

        // 1. 初始化 8 个击打动作按钮 (100% 采用 TFC 原始 buttonX / buttonY 坐标与精确 32x32 纹理采样)
        for (ForgeStep step : ForgeStep.values()) {
            this.addRenderableWidget(new StepActionButton(step, left + step.buttonX(), top + step.buttonY(), b -> {
                if (this.menu.blockEntity != null) {
                    ModPackets.INSTANCE.sendToServer(new ModPackets.ForgeStepActionPacket(
                            this.menu.blockEntity.getBlockPos(),
                            step.ordinal()
                    ));
                }
            }));
        }

        // 2. 初始化 Plan 图纸目标选择按钮 (位于 x: 20, y: 28)
        this.addRenderableWidget(new PlanTargetButton(left + 20, top + 28, b -> {
            this.planSelectionOpen = !this.planSelectionOpen;
            if (this.planSelectionOpen) {
                this.updateAvailableRecipes();
                closeSubWindows();
            }
        }));


        // 3. 配方锁定切换小按钮 (位于 x: 40, y: 31)
        this.addRenderableWidget(new RecipeLockButton(left + 40, top + 31, b -> {
            if (this.menu.blockEntity != null) {
                ModPackets.INSTANCE.sendToServer(new ModPackets.ToggleRecipeLockPacket(
                        this.menu.blockEntity.getBlockPos()
                ));
            }
        }));

        // 4. 自动锻造切换芯片 (右上角空白区 x: 122, y: 6，宽 28，高 14)
        this.addRenderableWidget(Button.builder(
                Component.empty(),
                b -> {
                    if (this.menu.blockEntity != null) {
                        boolean nextState = !this.menu.isAutoForgeEnabled();
                        ModPackets.INSTANCE.sendToServer(new ModPackets.ToggleAutoForgePacket(
                                this.menu.blockEntity.getBlockPos(),
                                nextState
                        ));
                    }
                }
        ).bounds(left + 122, top + 6, 28, 14).build());

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
                        ModPackets.INSTANCE.sendToServer(new ModPackets.ToggleRecipeLockPacket(
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




    /**
     * 更新当前工件可锻造的所有可用配方列表
     */
    private void updateAvailableRecipes() {
        this.availableRecipes.clear();
        if (this.minecraft != null && this.minecraft.level != null) {
            ItemStack mainStack = this.menu.getSlot(ElectricForgeBlockEntity.SLOT_MAIN).getItem();
            if (!mainStack.isEmpty()) {
                int tier = this.menu.getTier();
                var allRecipes = this.minecraft.level.getRecipeManager().getAllRecipesFor(TFCRecipeTypes.ANVIL.get());
                for (AnvilRecipe recipe : allRecipes) {
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
                int work = forge.getWork();
                int target = forge.getWorkTarget();

                // 2. 绘制锻造游标 (TFC 原版坐标规范：绿色目标在 y+94, 红色当前进度在 y+100)
                int targetX = left + 13 + Math.max(0, Math.min(146, target));
                graphics.blit(BACKGROUND, targetX, top + 94, 181, 0, 5, 5);

                int workX = left + 13 + Math.max(0, Math.min(146, work));
                graphics.blit(BACKGROUND, workX, top + 100, 176, 0, 5, 5);

                ForgeSteps steps = forge.getSteps();
                AnvilRecipe recipe = this.menu.blockEntity != null ? this.menu.blockEntity.resolveRecipe(forge) : null;

                // 3. 绘制顶部 3 个目标规则图标与状态高亮边框 (TFC 原版算法)
                if (recipe != null) {
                    ForgeRule[] rules = recipe.getRules();
                    for (int i = 0; i < rules.length && i < 3; i++) {
                        ForgeRule rule = rules[i];
                        if (rule != null) {
                            int offset = i * 19;
                            graphics.blit(BACKGROUND, left + 64 + offset, top + 10, 10, 10,
                                    (float) rule.iconX(), (float) rule.iconY(), 32, 32, 256, 256);

                            if (rule.matches(steps)) {
                                RenderSystem.setShaderColor(0.0F, 0.6F, 0.2F, 1.0F);
                            } else {
                                RenderSystem.setShaderColor(1.0F, 0.4F, 0.0F, 1.0F);
                            }
                            graphics.blit(BACKGROUND, left + 59 + offset, top + 7, 198, rule.overlayY(), 20, 22);
                            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                        }
                    }
                }

                // 4. 绘制下方 3 个历史动作图标 (从左至右依次为：倒数第三步、倒数第二步、最后一步)
                ForgeStep[] historySteps = new ForgeStep[]{steps.thirdLast(), steps.secondLast(), steps.last()};
                for (int i = 0; i < 3; i++) {
                    ForgeStep histStep = historySteps[i];
                    if (histStep != null) {
                        int offset = i * 19;
                        graphics.blit(BACKGROUND, left + 64 + offset, top + 31, 10, 10,
                                (float) histStep.iconX(), (float) histStep.iconY(), 32, 32, 256, 256);
                    }
                }
            }
        }

        // ==================== Mekanism 经典垂直条纹能量条 (Vertical Power Bar) ====================
        int barX = left + 167;
        int barY = top + 36;
        int barWidth = 6;
        int barHeight = 56;

        // 1. 能量槽外层凹陷深色外框
        graphics.fill(barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1, 0xFF101010);
        graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF1A2624); // 深墨绿槽底

        // 2. 能量填充计算
        int energy = this.menu.getEnergyStored();
        int maxEnergy = this.menu.getMaxEnergyStored();
        int filledHeight = maxEnergy > 0 ? (int) (((double) energy / maxEnergy) * barHeight) : 0;

        // 3. Mekanism 标志性青绿条纹刻度渲染
        if (filledHeight > 0) {
            int fillStartY = barY + barHeight - filledHeight;
            for (int y = fillStartY; y < barY + barHeight; y++) {
                // 每两行交替亮暗，形成标志性的条纹光效
                int stripeColor = (y % 2 == 0) ? 0xFF00FFC8 : 0xFF00D49E;
                graphics.fill(barX, y, barX + barWidth, y + 1, stripeColor);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // 绘制原版标题
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);

        // 绘制自动开关文字 (居中在芯片按钮内部)
        boolean auto = this.menu.isAutoForgeEnabled();
        Component autoText = auto ? Component.literal("AUTO").withStyle(s -> s.withColor(0x00FF88))
                : Component.literal("MANU").withStyle(s -> s.withColor(0xAAAAAA));
        graphics.drawString(this.font, autoText, 124, 9, 0xFFFFFF, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        // 1. 渲染 Plan 目标选择面板
        if (this.planSelectionOpen) {
            this.renderPlanSelectionOverlay(graphics, mouseX, mouseY);
        }

        // 2. 渲染 Mekanism 子窗口
        if (this.sideConfigWindow.isVisible()) {
            this.renderSideConfigSubWindow(graphics, mouseX, mouseY);
        } else if (this.transporterWindow.isVisible()) {
            this.renderTransporterSubWindow(graphics, mouseX, mouseY);
        } else if (this.upgradeWindow.isVisible()) {
            this.renderUpgradeSubWindow(graphics, mouseX, mouseY);
        }

        // 3. 悬浮 Tooltip 提示
        if (!this.planSelectionOpen && !isAnyWindowVisible()) {
            this.renderTooltip(graphics, mouseX, mouseY);
            this.renderCustomTooltips(graphics, mouseX, mouseY);
        }

        // 侧边栏按钮 Tooltip
        this.sideConfigTab.renderCustomTooltip(graphics, mouseX, mouseY);
        this.transporterTab.renderCustomTooltip(graphics, mouseX, mouseY);
        this.upgradeTab.renderCustomTooltip(graphics, mouseX, mouseY);
        this.securityTab.renderCustomTooltip(graphics, mouseX, mouseY);
    }

    private boolean isAnyWindowVisible() {
        return sideConfigWindow.isVisible() || transporterWindow.isVisible() || upgradeWindow.isVisible();
    }

    /**
     * 渲染侧面配置子窗口
     */
    private void renderSideConfigSubWindow(GuiGraphics graphics, int mouseX, int mouseY) {
        this.sideConfigWindow.render(graphics, this.font, mouseX, mouseY);
        int wx = this.sideConfigWindow.getX();
        int wy = this.sideConfigWindow.getY();

        if (this.menu.blockEntity == null) return;
        ElectricForgeBlockEntity be = this.menu.blockEntity;

        // 绘制 6 个面的配置按钮 (Top, Bottom, Front, Back, Left, Right)
        Direction[] dirs = Direction.values();
        String[] dirNames = {"底 (D)", "顶 (U)", "北 (N)", "南 (S)", "西 (W)", "东 (E)"};

        for (int i = 0; i < 6; i++) {
            Direction dir = dirs[i];
            int config = be.getSideConfig(dir);
            int row = i / 3;
            int col = i % 3;
            int btnX = wx + 10 + col * 43;
            int btnY = wy + 24 + row * 34;

            int color = switch (config) {
                case 0 -> 0xFF2A52BE;
                case 1 -> 0xFFB22222;
                case 2 -> 0xFF2E8B57;
                default -> 0xFF444444;
            };
            String configName = switch (config) {
                case 0 -> "输入";
                case 1 -> "输出";
                case 2 -> "能量";
                default -> "禁用";
            };

            boolean hov = (mouseX >= btnX && mouseX <= btnX + 38 && mouseY >= btnY && mouseY <= btnY + 28);
            graphics.fill(btnX, btnY, btnX + 38, btnY + 28, hov ? 0xFF666666 : 0xFF333333);
            graphics.fill(btnX + 1, btnY + 1, btnX + 37, btnY + 27, color);

            graphics.drawString(this.font, dirNames[i], btnX + 5, btnY + 4, 0xFFFFFFFF, false);
            graphics.drawString(this.font, configName, btnX + 8, btnY + 16, 0xFFEEEEEE, false);
        }

        // 自动弹出开关按钮
        int autoEjectY = wy + 98;
        boolean ejectHovered = (mouseX >= wx + 10 && mouseX <= wx + 136 && mouseY >= autoEjectY && mouseY <= autoEjectY + 18);
        graphics.fill(wx + 10, autoEjectY, wx + 136, autoEjectY + 18, ejectHovered ? 0xFF555555 : 0xFF2A2A2A);
        graphics.fill(wx + 11, autoEjectY + 1, wx + 135, autoEjectY + 17, be.isAutoEject() ? 0xFF1B5E20 : 0xFF3E2723);

        String autoEjectText = be.isAutoEject() ? "自动弹出: 已启用" : "自动弹出: 已禁用";
        graphics.drawString(this.font, autoEjectText, wx + 25, autoEjectY + 5, 0xFFFFFFFF, false);
    }

    /**
     * 渲染传输与温控配置子窗口
     */
    private void renderTransporterSubWindow(GuiGraphics graphics, int mouseX, int mouseY) {
        this.transporterWindow.render(graphics, this.font, mouseX, mouseY);
        int wx = this.transporterWindow.getX();
        int wy = this.transporterWindow.getY();

        graphics.drawString(this.font, Component.literal("自动敲击节奏: 20 ticks / 击"), wx + 10, wy + 25, 0xFFCCCCCC, false);
        graphics.drawString(this.font, Component.literal("升温功耗: 60 FE/t (平滑电热)"), wx + 10, wy + 42, 0xFF00FFC8, false);
        graphics.drawString(this.font, Component.literal("恒温保温: 5 FE/t (超低待机)"), wx + 10, wy + 59, 0xFFFFD700, false);
        graphics.drawString(this.font, Component.literal("状态: " + (this.menu.isAutoForgeEnabled() ? "全自动流水线" : "手动锻造")), wx + 10, wy + 76, 0xFFFFFFFF, false);
    }

    /**
     * 渲染升级管理子窗口 (Mekanism 经典工业级升级弹窗)
     */
    private void renderUpgradeSubWindow(GuiGraphics graphics, int mouseX, int mouseY) {
        this.upgradeWindow.render(graphics, this.font, mouseX, mouseY);
        int wx = this.upgradeWindow.getX();
        int wy = this.upgradeWindow.getY();

        // 1. 左侧已安装升级列表背景 (x: 6~68, y: 18~72)
        graphics.fill(wx + 6, wy + 18, wx + 68, wy + 72, 0xFF141418);
        graphics.fill(wx + 6, wy + 18, wx + 68, wy + 19, 0xFF2A2A38);

        org.shengxi.common.upgrade.ForgeUpgradeType[] allTypes = org.shengxi.common.upgrade.ForgeUpgradeType.values();
        for (int i = 0; i < allTypes.length; i++) {
            org.shengxi.common.upgrade.ForgeUpgradeType type = allTypes[i];
            int count = this.menu.getUpgradeCount(type);
            int itemY = wy + 19 + i * 13;

            boolean isSelected = (this.selectedUpgradeType == type);
            boolean isHovered = (mouseX >= wx + 6 && mouseX <= wx + 68 && mouseY >= itemY && mouseY <= itemY + 12);

            if (isSelected) {
                graphics.fill(wx + 6, itemY, wx + 68, itemY + 13, 0x4400FFC8);
                graphics.fill(wx + 6, itemY, wx + 8, itemY + 13, 0xFF00FFC8); // 左侧青绿指示条
            } else if (isHovered) {
                graphics.fill(wx + 6, itemY, wx + 68, itemY + 13, 0x22FFFFFF);
            }

            int textColor = count > 0 ? 0xFFFFFFFF : 0xFF777777;
            String shortName = switch (type) {
                case SPEED -> "速度";
                case ENERGY -> "能量";
                case MUFFLING -> "消音";
                case PERFECT_FORGING -> "完美";
            };
            graphics.drawString(this.font, shortName + ": " + count + "/" + type.getMaxCount(), wx + 10, itemY + 3, textColor, false);
        }

        // 2. 中间 Inner Screen 荧光显示屏 (x: 70~128, y: 18~54)
        graphics.fill(wx + 70, wy + 18, wx + 128, wy + 54, 0xFF0A0A10);
        graphics.fill(wx + 70, wy + 18, wx + 128, wy + 19, 0xFF1E1E30);

        int curCount = this.menu.getUpgradeCount(this.selectedUpgradeType);
        graphics.drawString(this.font, this.selectedUpgradeType.getTitle(), wx + 73, wy + 21, 0xFF00FFC8, false);
        graphics.drawString(this.font, "已安装: " + curCount + " / " + this.selectedUpgradeType.getMaxCount(), wx + 73, wy + 32, 0xFFE0E0E0, false);

        String effectText = switch (this.selectedUpgradeType) {
            case SPEED -> curCount > 0 ? ("节奏: " + Math.max(2, (int)(20 / (1 + curCount * 0.5))) + "t/击") : "可加快敲击与升温";
            case ENERGY -> curCount > 0 ? ("上限: " + (100 + curCount * 50) + "k FE") : "提升储能并降耗";
            case MUFFLING -> curCount > 0 ? ("音量: -" + (curCount * 25) + "%") : "消除铁砧敲击噪音";
            case PERFECT_FORGING -> curCount > 0 ? "品质: 100% 完美" : "产物获得最高品质";
        };
        graphics.drawString(this.font, effectText, wx + 73, wy + 42, 0xFFFFD700, false);

        // 卸载按钮 (x: 70~128, y: 56~71)
        int unBtnX = wx + 70;
        int unBtnY = wy + 56;
        int unBtnW = 58;
        int unBtnH = 15;
        boolean canUninstall = curCount > 0;
        boolean unHovered = (mouseX >= unBtnX && mouseX <= unBtnX + unBtnW && mouseY >= unBtnY && mouseY <= unBtnY + unBtnH);

        int btnBg = canUninstall ? (unHovered ? 0xFF4A4A58 : 0xFF282834) : 0xFF1C1C22;
        int btnBorder = canUninstall ? (unHovered ? 0xFF00FFC8 : 0xFF3D3D50) : 0xFF282830;
        graphics.fill(unBtnX, unBtnY, unBtnX + unBtnW, unBtnY + unBtnH, btnBorder);
        graphics.fill(unBtnX + 1, unBtnY + 1, unBtnX + unBtnW - 1, unBtnY + unBtnH - 1, btnBg);

        int btnTextColor = canUninstall ? 0xFF00FFC8 : 0xFF666666;
        graphics.drawString(this.font, "卸载芯片", unBtnX + 10, unBtnY + 4, btnTextColor, false);

        if (unHovered && canUninstall) {
            List<Component> unTip = new ArrayList<>();
            unTip.add(Component.literal("卸载已安装的升级"));
            unTip.add(Component.literal("§7左键: 卸载 1 个"));
            unTip.add(Component.literal("§7Shift+左键: 卸载全部"));
            graphics.renderComponentTooltip(this.font, unTip, mouseX, mouseY);
        }

        // 3. 右侧槽位与安装读条 (x: 133, y: 19 和 y: 55)

        int inX = wx + 133;
        int inY = wy + 19;
        int outX = wx + 133;
        int outY = wy + 55;

        // 输入槽边框与底色
        graphics.fill(inX - 1, inY - 1, inX + 17, inY + 17, 0xFF101010);
        graphics.fill(inX, inY, inX + 16, inY + 16, 0xFF1E2826);

        ItemStack inStack = this.menu.getSlot(ElectricForgeBlockEntity.SLOT_UPGRADE_INPUT).getItem();
        if (!inStack.isEmpty()) {
            graphics.renderItem(inStack, inX, inY);
            graphics.renderItemDecorations(this.font, inStack, inX, inY);
        } else {
            graphics.drawString(this.font, "↑", inX + 5, inY + 4, 0x5500FFC8, false);
        }

        boolean inHovered = (mouseX >= inX && mouseX <= inX + 16 && mouseY >= inY && mouseY <= inY + 16);
        if (inHovered) {
            graphics.fill(inX, inY, inX + 16, inY + 16, 0x44FFFFFF);
            if (!inStack.isEmpty()) {
                graphics.renderTooltip(this.font, inStack, mouseX, mouseY);
            } else {
                List<Component> inTip = new ArrayList<>();
                inTip.add(Component.literal("升级安装槽"));
                inTip.add(Component.literal("§7放入升级芯片以安装到机器"));
                graphics.renderComponentTooltip(this.font, inTip, mouseX, mouseY);
            }
        }

        // 动态安装进度条 (从上至下向下填充)
        int pBarX = wx + 138;
        int pBarY = wy + 38;
        int pBarW = 6;
        int pBarH = 14;
        graphics.fill(pBarX - 1, pBarY - 1, pBarX + pBarW + 1, pBarY + pBarH + 1, 0xFF101010);
        graphics.fill(pBarX, pBarY, pBarX + pBarW, pBarY + pBarH, 0xFF18201E);

        int ticks = this.menu.getUpgradeTicks();
        if (ticks > 0) {
            int pHeight = (int) (((double) ticks / ElectricForgeBlockEntity.UPGRADE_TICKS_REQUIRED) * pBarH);
            graphics.fill(pBarX, pBarY, pBarX + pBarW, pBarY + pHeight, 0xFF00FFC8);
        }

        // 输出槽边框与底色
        graphics.fill(outX - 1, outY - 1, outX + 17, outY + 17, 0xFF101010);
        graphics.fill(outX, outY, outX + 16, outY + 16, 0xFF281E1E);

        ItemStack outStack = this.menu.getSlot(ElectricForgeBlockEntity.SLOT_UPGRADE_OUTPUT).getItem();
        if (!outStack.isEmpty()) {
            graphics.renderItem(outStack, outX, outY);
            graphics.renderItemDecorations(this.font, outStack, outX, outY);
        } else {
            graphics.drawString(this.font, "↓", outX + 5, outY + 4, 0x55FF5555, false);
        }

        boolean outHovered = (mouseX >= outX && mouseX <= outX + 16 && mouseY >= outY && mouseY <= outY + 16);
        if (outHovered) {
            graphics.fill(outX, outY, outX + 16, outY + 16, 0x44FFFFFF);
            if (!outStack.isEmpty()) {
                graphics.renderTooltip(this.font, outStack, mouseX, mouseY);
            } else {
                List<Component> outTip = new ArrayList<>();
                outTip.add(Component.literal("升级输出槽"));
                outTip.add(Component.literal("§7卸载的升级芯片将退回至此"));
                graphics.renderComponentTooltip(this.font, outTip, mouseX, mouseY);
            }
        }

        // 4. 底部支持的升级列表 (x: 6~150, y: 76~112)
        graphics.fill(wx + 6, wy + 76, wx + 150, wy + 112, 0xFF16161C);
        graphics.drawString(this.font, "支持的升级类型:", wx + 10, wy + 80, 0xFFAAAAAA, false);

        for (int i = 0; i < allTypes.length; i++) {
            org.shengxi.common.upgrade.ForgeUpgradeType type = allTypes[i];
            int supX = wx + 10 + i * 35;
            int supY = wy + 93;

            boolean supHovered = (mouseX >= supX && mouseX <= supX + 32 && mouseY >= supY && mouseY <= supY + 14);
            graphics.fill(supX, supY, supX + 32, supY + 14, supHovered ? 0xFF353545 : 0xFF22222C);

            String tag = switch (type) {
                case SPEED -> "速";
                case ENERGY -> "能";
                case MUFFLING -> "音";
                case PERFECT_FORGING -> "臻";
            };
            graphics.drawString(this.font, tag + " " + type.getMaxCount(), supX + 5, supY + 3, 0xFF00FFC8, false);

            if (supHovered) {
                List<Component> supTip = new ArrayList<>();
                supTip.add(type.getTitle());
                supTip.add(type.getDescription());
                supTip.add(Component.literal("§8最大支持: " + type.getMaxCount() + " 个"));
                graphics.renderComponentTooltip(this.font, supTip, mouseX, mouseY);
            }
        }
    }


    /**
     * 绘制 Plan 图纸目标选择弹窗覆盖层
     */
    private void renderPlanSelectionOverlay(GuiGraphics graphics, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        graphics.fill(left + 8, top + 20, left + 168, top + 116, 0xEE181818);
        graphics.fill(left + 9, top + 21, left + 167, top + 115, 0xFF242424);

        graphics.drawString(this.font, Component.translatable("gui.mekatfc.electric_forge.select_plan"), left + 14, top + 25, 0xFFE0E0E0, false);

        if (this.availableRecipes.isEmpty()) {
            graphics.drawString(this.font, Component.translatable("gui.mekatfc.electric_forge.no_plans"), left + 14, top + 48, 0xFFAAAAAA, false);
        } else {
            int startX = left + 14;
            int startY = top + 42;
            int cols = 7;

            for (int i = 0; i < this.availableRecipes.size() && i < 21; i++) {
                AnvilRecipe recipe = this.availableRecipes.get(i);
                int rCol = i % cols;
                int rRow = i / cols;
                int iconX = startX + rCol * 22;
                int iconY = startY + rRow * 22;

                boolean hovered = (mouseX >= iconX && mouseX <= iconX + 18 && mouseY >= iconY && mouseY <= iconY + 18);
                graphics.fill(iconX - 1, iconY - 1, iconX + 19, iconY + 19, hovered ? 0xFF555555 : 0xFF353535);

                if (this.minecraft != null && this.minecraft.level != null) {
                    ItemStack resultStack = recipe.getResultItem(this.minecraft.level.registryAccess());
                    graphics.renderItem(resultStack, iconX + 1, iconY + 1);
                }

                if (hovered && this.minecraft != null && this.minecraft.level != null) {
                    ItemStack resultStack = recipe.getResultItem(this.minecraft.level.registryAccess());
                    List<Component> tooltip = new ArrayList<>();
                    tooltip.add(resultStack.getHoverName());
                    tooltip.add(Component.translatable("gui.mekatfc.electric_forge.recipe_tier", recipe.getMinTier()));
                    graphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 1. 处理侧面配置子窗口点击
        if (this.sideConfigWindow.isVisible()) {
            int wx = this.sideConfigWindow.getX();
            int wy = this.sideConfigWindow.getY();
            if (this.menu.blockEntity != null) {
                // 点击 6 面配置按钮
                for (int i = 0; i < 6; i++) {
                    int row = i / 3;
                    int col = i % 3;
                    int btnX = wx + 10 + col * 43;
                    int btnY = wy + 24 + row * 34;
                    if (mouseX >= btnX && mouseX <= btnX + 38 && mouseY >= btnY && mouseY <= btnY + 28) {
                        ModPackets.INSTANCE.sendToServer(new ModPackets.UpdateSideConfigPacket(
                                this.menu.blockEntity.getBlockPos(),
                                i,
                                false
                        ));
                        return true;
                    }
                }
                // 点击自动弹出切换
                int autoEjectY = wy + 98;
                if (mouseX >= wx + 10 && mouseX <= wx + 136 && mouseY >= autoEjectY && mouseY <= autoEjectY + 18) {
                    ModPackets.INSTANCE.sendToServer(new ModPackets.UpdateSideConfigPacket(
                            this.menu.blockEntity.getBlockPos(),
                            0,
                            true
                    ));
                    return true;
                }
            }
            if (this.sideConfigWindow.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        // 2. 处理升级管理子窗口点击与槽位交互
        if (this.upgradeWindow.isVisible()) {
            int wx = this.upgradeWindow.getX();
            int wy = this.upgradeWindow.getY();

            // 点击输入槽
            int inX = wx + 133;
            int inY = wy + 19;
            if (mouseX >= inX && mouseX <= inX + 16 && mouseY >= inY && mouseY <= inY + 16) {
                Slot inSlot = this.menu.getSlot(ElectricForgeBlockEntity.SLOT_UPGRADE_INPUT);
                this.slotClicked(inSlot, inSlot.index, button, Screen.hasShiftDown() ? ClickType.QUICK_MOVE : ClickType.PICKUP);
                return true;
            }

            // 点击输出槽
            int outX = wx + 133;
            int outY = wy + 55;
            if (mouseX >= outX && mouseX <= outX + 16 && mouseY >= outY && mouseY <= outY + 16) {
                Slot outSlot = this.menu.getSlot(ElectricForgeBlockEntity.SLOT_UPGRADE_OUTPUT);
                this.slotClicked(outSlot, outSlot.index, button, Screen.hasShiftDown() ? ClickType.QUICK_MOVE : ClickType.PICKUP);
                return true;
            }

            // 点击左侧升级列表项
            org.shengxi.common.upgrade.ForgeUpgradeType[] allTypes = org.shengxi.common.upgrade.ForgeUpgradeType.values();
            for (int i = 0; i < allTypes.length; i++) {
                int itemY = wy + 19 + i * 13;
                if (mouseX >= wx + 6 && mouseX <= wx + 68 && mouseY >= itemY && mouseY <= itemY + 12) {
                    this.selectedUpgradeType = allTypes[i];
                    return true;
                }
            }

            // 点击卸载按钮
            int unBtnX = wx + 70;
            int unBtnY = wy + 56;
            int unBtnW = 58;
            int unBtnH = 15;
            if (mouseX >= unBtnX && mouseX <= unBtnX + unBtnW && mouseY >= unBtnY && mouseY <= unBtnY + unBtnH) {
                int count = this.menu.getUpgradeCount(this.selectedUpgradeType);
                if (count > 0 && this.menu.blockEntity != null) {
                    boolean removeAll = net.minecraft.client.gui.screens.Screen.hasShiftDown();
                    ModPackets.INSTANCE.sendToServer(new ModPackets.UninstallUpgradePacket(
                            this.menu.blockEntity.getBlockPos(),
                            this.selectedUpgradeType.ordinal(),
                            removeAll
                    ));
                    return true;
                }
            }

            if (this.upgradeWindow.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        // 3. 处理传输子窗口点击
        if (this.transporterWindow.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        // 4. 处理 Plan 图纸选择点击
        if (this.planSelectionOpen && button == 0) {
            int left = this.leftPos;
            int top = this.topPos;
            int startX = left + 14;
            int startY = top + 42;
            int cols = 7;

            for (int i = 0; i < this.availableRecipes.size() && i < 21; i++) {
                int rCol = i % cols;
                int rRow = i / cols;
                int iconX = startX + rCol * 22;
                int iconY = startY + rRow * 22;

                if (mouseX >= iconX && mouseX <= iconX + 18 && mouseY >= iconY && mouseY <= iconY + 18) {
                    AnvilRecipe selected = this.availableRecipes.get(i);
                    if (this.menu.blockEntity != null) {
                        ModPackets.INSTANCE.sendToServer(new ModPackets.SelectRecipePlanPacket(
                                this.menu.blockEntity.getBlockPos(),
                                selected.getId().toString()
                        ));
                    }
                    this.planSelectionOpen = false;
                    return true;
                }
            }

            if (mouseX < left + 8 || mouseX > left + 168 || mouseY < top + 20 || mouseY > top + 116) {
                this.planSelectionOpen = false;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.upgradeWindow.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        if (this.sideConfigWindow.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        if (this.transporterWindow.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.upgradeWindow.mouseReleased(mouseX, mouseY, button);
        this.sideConfigWindow.mouseReleased(mouseX, mouseY, button);
        this.transporterWindow.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    /**
     * 渲染自定义悬停 Tooltip
     */

    private void renderCustomTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        // 1. Mekanism 垂直能量条 Tooltip (x: 167 ~ 173, y: 36 ~ 92)
        if (mouseX >= left + 166 && mouseX <= left + 174 && mouseY >= top + 35 && mouseY <= top + 93) {
            List<Component> energyTooltips = new ArrayList<>();
            energyTooltips.add(Component.literal("§b储能: §f" + this.menu.getEnergyStored() + " / " + this.menu.getMaxEnergyStored() + " FE"));
            int state = this.menu.getCurrentState();
            String usage = switch (state) {
                case ElectricForgeBlockEntity.STATE_HEATING -> "§c使用中: 电热升温模式";
                case ElectricForgeBlockEntity.STATE_HOLDING -> "§e使用中: 恒温保温模式";
                case ElectricForgeBlockEntity.STATE_FORGING -> "§a使用中: 自动击打模式";
                default -> "§7待机中: 0 FE/t";
            };
            energyTooltips.add(Component.literal(usage));
            graphics.renderComponentTooltip(this.font, energyTooltips, mouseX, mouseY);
        }

        // 2. 铁砧模具槽 Tooltip (x: 129 ~ 145, y: 68 ~ 84)
        if (mouseX >= left + 129 && mouseX <= left + 145 && mouseY >= top + 68 && mouseY <= top + 84) {
            int tier = this.menu.getTier();
            List<Component> anvilTooltips = new ArrayList<>();
            if (tier < 0) {
                anvilTooltips.add(Component.translatable("gui.mekatfc.electric_forge.no_anvil"));
                anvilTooltips.add(Component.translatable("gui.mekatfc.electric_forge.no_anvil.desc").withStyle(s -> s.withColor(0xFF5555)));
            } else {
                anvilTooltips.add(Component.translatable("gui.mekatfc.electric_forge.anvil_tier", tier));
            }
            graphics.renderComponentTooltip(this.font, anvilTooltips, mouseX, mouseY);
        }
    }


    /**
     * 击打动作按钮 (100% 对齐 TFC 原版 AnvilStepButton)
     */
    private static class StepActionButton extends Button {
        private final ForgeStep step;

        StepActionButton(ForgeStep step, int x, int y, OnPress onPress) {
            super(x, y, 16, 16, Helpers.translateEnum(step), onPress, DEFAULT_NARRATION);
            this.step = step;
            this.setTooltip(Tooltip.create(Helpers.translateEnum(step)));
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.blit(BACKGROUND, this.getX(), this.getY(), 16, 16,
                    (float) this.step.iconX(), (float) this.step.iconY(), 32, 32, 256, 256);
            if (this.isHovered()) {
                graphics.fill(this.getX(), this.getY(), this.getX() + 16, this.getY() + 16, 0x33FFFFFF);
            }
        }
    }

    /**
     * Plan 目标图纸按钮 (100% 对齐 TFC 原版 AnvilPlanButton)
     */
    private class PlanTargetButton extends Button {

        PlanTargetButton(int x, int y, OnPress onPress) {
            super(x, y, 18, 18, Component.empty(), onPress, DEFAULT_NARRATION);
            this.setTooltip(Tooltip.create(Component.translatable("gui.mekatfc.electric_forge.select_plan")));
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            ItemStack mainStack = ElectricForgeScreen.this.menu.getSlot(ElectricForgeBlockEntity.SLOT_MAIN).getItem();
            Forging forge = !mainStack.isEmpty() ? ForgingCapability.get(mainStack) : null;
            AnvilRecipe recipe = (forge != null && ElectricForgeScreen.this.menu.blockEntity != null)
                    ? ElectricForgeScreen.this.menu.blockEntity.resolveRecipe(forge) : null;

            int x = this.getX();
            int y = this.getY();

            if (recipe != null && ElectricForgeScreen.this.minecraft != null && ElectricForgeScreen.this.minecraft.level != null) {
                ItemStack result = recipe.getResultItem(ElectricForgeScreen.this.minecraft.level.registryAccess());
                graphics.renderItem(result, x + 1, y + 1);
            } else {
                boolean hasWorkable = false;
                if (ElectricForgeScreen.this.minecraft != null && ElectricForgeScreen.this.minecraft.level != null && !mainStack.isEmpty()) {
                    hasWorkable = AnvilRecipe.hasAny(ElectricForgeScreen.this.minecraft.level, mainStack, ElectricForgeScreen.this.menu.getTier());
                }
                float uOffset = hasWorkable ? 236.0F : 219.0F;
                float vOffset = hasWorkable ? 0.0F : 51.0F;
                graphics.blit(BACKGROUND, x + 1, y + 1, uOffset, vOffset, 16, 16, 256, 256);
            }

            if (this.isHovered()) {
                graphics.fill(x, y, x + 18, y + 18, 0x33FFFFFF);
            }
        }
    }

    /**
     * 配方锁定按钮 (Lock / Unlock 图标切换)
     */
    private class RecipeLockButton extends Button {

        RecipeLockButton(int x, int y, OnPress onPress) {
            super(x, y, 10, 14, Component.empty(), onPress, DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean locked = ElectricForgeScreen.this.menu.isRecipeLocked();
            int x = this.getX();
            int y = this.getY();

            if (locked) {
                graphics.fill(x + 2, y + 1, x + 8, y + 6, 0xFFFFD700);
                graphics.fill(x + 3, y + 2, x + 7, y + 5, 0xFF2A2A2A);
                graphics.fill(x + 1, y + 5, x + 9, y + 13, 0xFFFFB800);
                graphics.fill(x + 4, y + 8, x + 6, y + 10, 0xFF333333);
            } else {
                graphics.fill(x + 3, y + 1, x + 9, y + 5, 0xFF888888);
                graphics.fill(x + 4, y + 2, x + 8, y + 4, 0xFF2A2A2A);
                graphics.fill(x + 1, y + 5, x + 9, y + 13, 0xFF666666);
                graphics.fill(x + 4, y + 8, x + 6, y + 10, 0xFF222222);
            }

            if (this.isHovered()) {
                graphics.fill(x, y, x + 10, y + 14, 0x33FFFFFF);
                Component tip = locked
                        ? Component.translatable("gui.mekatfc.electric_forge.recipe_locked")
                        : Component.translatable("gui.mekatfc.electric_forge.recipe_unlocked");
                graphics.renderTooltip(ElectricForgeScreen.this.font, tip, mouseX, mouseY);
            }
        }
    }
}
