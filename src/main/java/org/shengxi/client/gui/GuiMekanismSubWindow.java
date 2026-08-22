package org.shengxi.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Mekanism 风格弹窗子窗口 (GuiMekanismSubWindow)
 * 用于显示侧面配置、传输自动化配置与升级管理等子菜单，支持鼠标按住标题栏自由拖拽移动。
 */
public class GuiMekanismSubWindow {

    public enum WindowType {
        SIDE_CONFIG,
        TRANSPORTER_CONFIG,
        UPGRADE_MANAGEMENT,
        SECURITY
    }

    private final WindowType type;
    private final Component title;
    private int x;
    private int y;
    private final int width;
    private final int height;
    private boolean visible = false;

    // 拖拽移动状态
    private boolean dragging = false;
    private double dragStartX;
    private double dragStartY;
    private int dragStartWindowX;
    private int dragStartWindowY;

    public GuiMekanismSubWindow(WindowType type, Component title, int width, int height) {
        this.type = type;
        this.title = title;
        this.width = width;
        this.height = height;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if (!visible) {
            this.dragging = false;
        }
    }

    public void toggleVisible() {
        setVisible(!this.visible);
    }

    public WindowType getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY) {
        if (!visible) return;

        // 1. 外框阴影与主背景 (Mekanism 经典深色金属质感)
        graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xFF101010);
        graphics.fill(x, y, x + width, y + height, 0xFF242424);

        // 2. 顶部标题栏 (深蓝黑渐变，支持拖拽)
        boolean titleHovered = (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 16);
        graphics.fill(x + 1, y + 1, x + width - 1, y + 15, titleHovered ? 0xFF282836 : 0xFF1E1E28);
        graphics.fill(x + 1, y + 15, x + width - 1, y + 16, 0xFF00E5FF); // 青蓝分割线

        // 标题文字
        graphics.drawString(font, title, x + 6, y + 4, 0xFFE0E0E0, false);

        // 3. 关闭按钮 (X)
        int closeX = x + width - 13;
        int closeY = y + 3;
        boolean closeHovered = (mouseX >= closeX && mouseX <= closeX + 10 && mouseY >= closeY && mouseY <= closeY + 10);
        graphics.fill(closeX, closeY, closeX + 10, closeY + 10, closeHovered ? 0xFF992222 : 0xFF3E3E3E);
        graphics.drawString(font, "×", closeX + 2, closeY + 1, 0xFFFFFFFF, false);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;

        // 1. 点击关闭按钮
        int closeX = x + width - 13;
        int closeY = y + 3;
        if (mouseX >= closeX && mouseX <= closeX + 10 && mouseY >= closeY && mouseY <= closeY + 10) {
            this.visible = false;
            this.dragging = false;
            return true;
        }

        // 2. 按住顶部标题栏开始拖拽
        if (button == 0 && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 16) {
            this.dragging = true;
            this.dragStartX = mouseX;
            this.dragStartY = mouseY;
            this.dragStartWindowX = this.x;
            this.dragStartWindowY = this.y;
            return true;
        }

        // 消耗窗口区域内的点击，防止点到背后的控件
        return (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!visible || !dragging) return false;

        Minecraft mc = Minecraft.getInstance();
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        int newX = this.dragStartWindowX + (int) (mouseX - this.dragStartX);
        int newY = this.dragStartWindowY + (int) (mouseY - this.dragStartY);

        // 限制在屏幕有效区域内
        this.x = Math.max(0, Math.min(screenW - this.width, newX));
        this.y = Math.max(0, Math.min(screenH - this.height, newY));
        return true;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.dragging) {
            this.dragging = false;
            return true;
        }
        return false;
    }
}

