package org.shengxi.client.gui.button;

import net.dries007.tfc.common.component.forge.ForgeStep;
import net.dries007.tfc.util.Helpers;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * 击打动作按钮 (100% 对齐 TFC 原版 AnvilStepButton)
 */
public class StepActionButton extends Button {

    public static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("tfc", "textures/gui/anvil.png");

    private final ForgeStep step;
    private final Supplier<Boolean> isHiddenSupplier;

    public StepActionButton(ForgeStep step, int x, int y, OnPress onPress, Supplier<Boolean> isHiddenSupplier) {
        super(x, y, 16, 16, Helpers.translateEnum(step), onPress, DEFAULT_NARRATION);
        this.step = step;
        this.isHiddenSupplier = isHiddenSupplier;
        this.setTooltip(Tooltip.create(Helpers.translateEnum(step)));
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHiddenSupplier != null && isHiddenSupplier.get()) {
            return;
        }
        graphics.blit(BACKGROUND, this.getX(), this.getY(), 16, 16,
                (float) this.step.iconX(), (float) this.step.iconY(), 32, 32, 256, 256);
        if (this.isHovered()) {
            graphics.fill(this.getX(), this.getY(), this.getX() + 16, this.getY() + 16, 0x33FFFFFF);
        }
    }
}
