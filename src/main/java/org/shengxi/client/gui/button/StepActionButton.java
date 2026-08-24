package org.shengxi.client.gui.button;

import net.dries007.tfc.common.component.forge.ForgeStep;
import net.dries007.tfc.util.Helpers;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * 击打动作按钮 (100% 对齐 TFC 1.21 原版 AnvilStepButton)
 * 直接使用 TFC 原版 anvil.png 贴图内置的 16x16 动作图标，无缩放无失真。
 */
public class StepActionButton extends Button {

    public static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("tfc", "textures/gui/anvil.png");

    private final ForgeStep step;
    private final Supplier<Boolean> isHiddenSupplier;

    public StepActionButton(ForgeStep step, int guiLeft, int guiTop, OnPress onPress, Supplier<Boolean> isHiddenSupplier) {
        super(guiLeft + step.buttonX(), guiTop + step.buttonY(), 16, 16, Helpers.translateEnum(step), onPress, DEFAULT_NARRATION);
        this.step = step;
        this.isHiddenSupplier = isHiddenSupplier;
        this.setTooltip(Tooltip.create(Helpers.translateEnum(step)));
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHiddenSupplier != null && isHiddenSupplier.get()) {
            return;
        }
        // 100% 采用 TFC 官方 AnvilStepButton 采样逻辑：16x16 像素 1:1 绘制
        graphics.blit(BACKGROUND, this.getX(), this.getY(), (float) this.step.iconX(), (float) this.step.iconY(), 16, 16, 256, 256);
        if (this.isHovered()) {
            graphics.fill(this.getX(), this.getY(), this.getX() + 16, this.getY() + 16, 0x33FFFFFF);
        }
    }
}
