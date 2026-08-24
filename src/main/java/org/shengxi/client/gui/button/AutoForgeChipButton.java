package org.shengxi.client.gui.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.shengxi.common.blockentity.ElectricForgeBlockEntity;
import org.shengxi.common.container.ElectricForgeMenu;

import java.util.function.Supplier;

/**
 * 自动锻造模式切换微型芯片按钮 (AutoForgeChipButton)
 * 位于界面顶部中心 (x: 74, y: 4)，集成 LED 状态指示灯与 AUTO/MANU 文字显示。
 */
public class AutoForgeChipButton extends Button {

    private final ElectricForgeMenu menu;
    private final Supplier<Boolean> isHiddenSupplier;

    public AutoForgeChipButton(int x, int y, ElectricForgeMenu menu, OnPress onPress, Supplier<Boolean> isHiddenSupplier) {
        super(x, y, 28, 14, Component.empty(), onPress, DEFAULT_NARRATION);
        this.menu = menu;
        this.isHiddenSupplier = isHiddenSupplier;
        this.setTooltip(Tooltip.create(Component.translatable("gui.mekatfc.electric_forge.toggle_auto")));
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHiddenSupplier != null && isHiddenSupplier.get()) {
            return;
        }

        int x = this.getX();
        int y = this.getY();
        boolean autoEnabled = this.menu.isAutoForgeEnabled();
        int state = this.menu.getCurrentState();

        // 1. 工业芯片立体底座
        graphics.fill(x, y, x + 28, y + 14, 0xFF1A1A1A);
        graphics.fill(x + 1, y + 1, x + 27, y + 13, this.isHovered() ? 0xFF4A4A4A : 0xFF2E2E2E);

        // 2. LED 状态指示灯
        int ledColor = switch (state) {
            case ElectricForgeBlockEntity.STATE_HEATING -> 0xFFFF8800; // 橙色：升温中
            case ElectricForgeBlockEntity.STATE_HOLDING -> 0xFFFFD700; // 黄色：恒温保温
            case ElectricForgeBlockEntity.STATE_FORGING -> 0xFF00FF00; // 绿色：自动打铁中
            default -> autoEnabled ? 0xFF00E5FF : 0xFF666666;        // 青色：就绪待机 / 灰色：已关闭
        };
        graphics.fill(x + 3, y + 4, x + 9, y + 10, ledColor);

        // 3. 模式文本
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, autoEnabled ? "AUTO" : "MANU", x + 11, y + 3, autoEnabled ? 0xFFFFFFFF : 0xFFAAAAAA, false);

        // 4. 悬停微光
        if (this.isHovered()) {
            graphics.fill(x + 1, y + 1, x + 27, y + 13, 0x22FFFFFF);
        }
    }
}
