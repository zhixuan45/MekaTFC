package org.shengxi.client.gui.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.shengxi.common.container.ElectricForgeMenu;

import java.util.function.Supplier;

/**
 * 配方锁定按钮 (Lock / Unlock 图标切换)
 */
public class RecipeLockButton extends Button {

    private final ElectricForgeMenu menu;
    private final Supplier<Boolean> isHiddenSupplier;

    public RecipeLockButton(int x, int y, ElectricForgeMenu menu, OnPress onPress, Supplier<Boolean> isHiddenSupplier) {
        super(x, y, 10, 14, Component.empty(), onPress, DEFAULT_NARRATION);
        this.menu = menu;
        this.isHiddenSupplier = isHiddenSupplier;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHiddenSupplier != null && isHiddenSupplier.get()) {
            return;
        }
        boolean locked = this.menu.isRecipeLocked();
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
            graphics.renderTooltip(Minecraft.getInstance().font, tip, mouseX, mouseY);
        }
    }
}
