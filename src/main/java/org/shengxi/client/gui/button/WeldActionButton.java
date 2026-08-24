package org.shengxi.client.gui.button;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.shengxi.common.container.ElectricForgeMenu;

import java.util.function.Supplier;

/**
 * 1.21 新版 TFC 铁砧焊接操作按钮 (100% 对齐 TFC 原版 AnvilWeldButton)
 * 直接从 TFC 原版 anvil.png 贴图中采样焊接底框与双锭压合图标。
 */
public class WeldActionButton extends Button {

    public static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("tfc", "textures/gui/anvil.png");

    private final ElectricForgeMenu menu;
    private final Supplier<Boolean> isHiddenSupplier;

    public WeldActionButton(int x, int y, ElectricForgeMenu menu, OnPress onPress, Supplier<Boolean> isHiddenSupplier) {
        super(x, y, 18, 18, Component.translatable("gui.mekatfc.electric_forge.weld"), onPress, DEFAULT_NARRATION);
        this.menu = menu;
        this.isHiddenSupplier = isHiddenSupplier;
        this.setTooltip(Tooltip.create(Component.translatable("gui.mekatfc.electric_forge.weld")));
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHiddenSupplier != null && isHiddenSupplier.get()) {
            return;
        }

        int x = this.getX();
        int y = this.getY();
        boolean canWeld = this.menu.blockEntity != null && this.menu.blockEntity.canWeld();

        // 1. 绘制 TFC 官方按键底框 (218, 0 可按 / 218, 18 禁用)
        float vOffset = canWeld ? 0.0F : 18.0F;
        graphics.blit(BACKGROUND, x, y, 218.0F, vOffset, 18, 18, 256, 256);

        // 2. 绘制 TFC 官方双锭压合焊接图标 (236, 16)
        graphics.blit(BACKGROUND, x + 1, y + 1, 236.0F, 16.0F, 16, 16, 256, 256);

        // 3. 悬停微光
        if (this.isHovered()) {
            graphics.fill(x + 1, y + 1, x + 17, y + 17, 0x33FFFFFF);
        }
    }
}
