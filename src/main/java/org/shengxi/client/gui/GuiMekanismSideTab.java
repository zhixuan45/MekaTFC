package org.shengxi.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Mekanism 经典侧边栏标签页按钮 (Side Tab Button)
 * 具备左侧/右侧伸出式立体按钮外观、自定义图标、橙色/灰色主题边框与悬停高亮效果。
 */
public class GuiMekanismSideTab extends Button {

    public enum TabSide {
        LEFT,
        RIGHT
    }

    public enum TabIconType {
        SIDE_CONFIG,     // 九宫格侧面配置
        TRANSPORTER,     // 传送管道/自动化配置
        UPGRADE,         // 升级管理 (向上箭头)
        SECURITY_LOCKED, // 安全与锁定 (锁定)
        SECURITY_UNLOCKED// 安全与锁定 (解锁)
    }

    private final TabSide side;
    private final TabIconType iconType;
    private final boolean isOrangeBorder;
    private final Component tooltipTitle;

    public GuiMekanismSideTab(int x, int y, TabSide side, TabIconType iconType, boolean isOrangeBorder, Component tooltipTitle, OnPress onPress) {
        super(x, y, 26, 26, Component.empty(), onPress, DEFAULT_NARRATION);
        this.side = side;
        this.iconType = iconType;
        this.isOrangeBorder = isOrangeBorder;
        this.tooltipTitle = tooltipTitle;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = this.getX();
        int y = this.getY();

        // 1. 绘制底座外框 (Mekanism 经典圆角矩形金属底座)
        int borderColor = isOrangeBorder ? 0xFFC07020 : (this.isHovered() ? 0xFF666666 : 0xFF3E3E3E);
        int innerBorderColor = isOrangeBorder ? 0xFFE08830 : 0xFF555555;
        int bgColor = 0xFF2A2A2A;

        // 外层凸起轮廓
        graphics.fill(x, y + 1, x + 26, y + 25, borderColor);
        graphics.fill(x + 1, y, x + 25, y + 26, borderColor);

        // 内层边框
        graphics.fill(x + 1, y + 2, x + 25, y + 24, innerBorderColor);
        graphics.fill(x + 2, y + 1, x + 24, y + 25, innerBorderColor);

        // 内部深灰背景
        graphics.fill(x + 2, y + 2, x + 24, y + 24, bgColor);

        // 2. 内部按钮核心方块 (18x18 金属按键)
        int btnX = x + 4;
        int btnY = y + 4;
        graphics.fill(btnX, btnY, btnX + 18, btnY + 18, 0xFF4A4A4A);
        graphics.fill(btnX + 1, btnY + 1, btnX + 17, btnY + 17, this.isHovered() ? 0xFF606060 : 0xFF505050);

        // 3. 绘制图标
        renderTabIcon(graphics, btnX + 1, btnY + 1);

        // 4. 悬停白光叠加
        if (this.isHovered()) {
            graphics.fill(x + 1, y + 1, x + 25, y + 25, 0x22FFFFFF);
        }
    }

    private void renderTabIcon(GuiGraphics graphics, int x, int y) {
        switch (this.iconType) {
            case SIDE_CONFIG -> {
                // 九宫格方块图标 (3x3 矩阵小方块)
                for (int row = 0; row < 3; row++) {
                    for (int col = 0; col < 3; col++) {
                        int dotX = x + 3 + col * 4;
                        int dotY = y + 3 + row * 4;
                        int dotColor = (row == 1 && col == 1) ? 0xFF00FFC8 : 0xFFAAAAAA; // 中心为青色，四周为浅灰
                        graphics.fill(dotX, dotY, dotX + 3, dotY + 3, dotColor);
                    }
                }
            }
            case TRANSPORTER -> {
                // 传送管道/轨道图标 (两条竖轨 + 三根横木)
                graphics.fill(x + 4, y + 2, x + 6, y + 14, 0xFF00FFC8);
                graphics.fill(x + 10, y + 2, x + 12, y + 14, 0xFF00FFC8);
                graphics.fill(x + 6, y + 4, x + 10, y + 5, 0xFFDDDDDD);
                graphics.fill(x + 6, y + 8, x + 10, y + 9, 0xFFDDDDDD);
                graphics.fill(x + 6, y + 12, x + 10, y + 13, 0xFFDDDDDD);
            }
            case UPGRADE -> {
                // 升级图标 (粗箭头向上)
                graphics.fill(x + 7, y + 3, x + 9, y + 5, 0xFF333333);
                graphics.fill(x + 5, y + 5, x + 11, y + 7, 0xFF333333);
                graphics.fill(x + 3, y + 7, x + 13, y + 9, 0xFF333333);
                graphics.fill(x + 6, y + 9, x + 10, y + 14, 0xFF333333);

                graphics.fill(x + 7, y + 4, x + 9, y + 6, 0xFFEEEEEE);
                graphics.fill(x + 5, y + 6, x + 11, y + 8, 0xFFEEEEEE);
                graphics.fill(x + 4, y + 8, x + 12, y + 9, 0xFFEEEEEE);
                graphics.fill(x + 6, y + 9, x + 10, y + 13, 0xFF00FFC8);
            }
            case SECURITY_LOCKED -> {
                // 锁定状态 (金色锁身 + 闭合锁环)
                graphics.fill(x + 5, y + 2, x + 11, y + 7, 0xFFFFD700);
                graphics.fill(x + 6, y + 3, x + 10, y + 6, 0xFF2A2A2A);
                graphics.fill(x + 3, y + 6, x + 13, y + 14, 0xFFFFB800);
                graphics.fill(x + 7, y + 8, x + 9, y + 11, 0xFF2A2A2A);
            }
            case SECURITY_UNLOCKED -> {
                // 开锁状态 (银灰色开锁)
                graphics.fill(x + 6, y + 2, x + 12, y + 6, 0xFFCCCCCC);
                graphics.fill(x + 7, y + 3, x + 11, y + 5, 0xFF2A2A2A);
                graphics.fill(x + 3, y + 6, x + 13, y + 14, 0xFF888888);
                graphics.fill(x + 7, y + 8, x + 9, y + 11, 0xFF2A2A2A);
            }
        }
    }

    public void renderCustomTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.isHovered() && this.tooltipTitle != null) {
            graphics.renderTooltip(net.minecraft.client.Minecraft.getInstance().font, this.tooltipTitle, mouseX, mouseY);
        }
    }
}
