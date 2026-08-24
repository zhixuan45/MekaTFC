package org.shengxi.client.gui.button;

import net.dries007.tfc.common.component.forge.Forging;
import net.dries007.tfc.common.component.forge.ForgingCapability;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.shengxi.common.blockentity.ElectricForgeBlockEntity;
import org.shengxi.common.container.ElectricForgeMenu;

import java.util.function.Supplier;

/**
 * Plan 目标图纸按钮 (100% 对齐 TFC 1.21 原版 AnvilPlanButton)
 * 直接从 TFC 原版 anvil.png 贴图中采样底框与图纸问号图标。
 */
public class PlanTargetButton extends Button {

    public static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("tfc", "textures/gui/anvil.png");

    private final ElectricForgeMenu menu;
    private final Supplier<Boolean> isHiddenSupplier;

    public PlanTargetButton(int x, int y, ElectricForgeMenu menu, OnPress onPress, Supplier<Boolean> isHiddenSupplier) {
        super(x, y, 18, 18, Component.empty(), onPress, DEFAULT_NARRATION);
        this.menu = menu;
        this.isHiddenSupplier = isHiddenSupplier;
        this.setTooltip(Tooltip.create(Component.translatable("gui.mekatfc.electric_forge.select_plan")));
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHiddenSupplier != null && isHiddenSupplier.get()) {
            return;
        }
        ItemStack mainStack = this.menu.getSlot(ElectricForgeBlockEntity.SLOT_MAIN).getItem();
        Forging forge = !mainStack.isEmpty() ? ForgingCapability.get(mainStack) : null;
        AnvilRecipe recipe = (forge != null && this.menu.blockEntity != null)
                ? this.menu.blockEntity.resolveRecipe(forge) : null;

        int x = this.getX();
        int y = this.getY();

        Minecraft mc = Minecraft.getInstance();
        if (recipe != null && mc.level != null) {
            // 已绑定配方：绘制激活底框与产物图标
            graphics.blit(BACKGROUND, x, y, 218.0F, 0.0F, 18, 18, 256, 256);
            ItemStack result = recipe.getResultItem(mc.level.registryAccess());
            graphics.renderItem(result, x + 1, y + 1);
            graphics.renderItemDecorations(mc.font, result, x + 1, y + 1);
        } else {
            // 未绑定配方：检查是否有可用配方并绘制问号图纸底图
            boolean hasWorkable = false;
            if (mc.level != null && !mainStack.isEmpty()) {
                hasWorkable = AnvilRecipe.hasAny(mc.level, mainStack, this.menu.getTier());
            }
            float vOffset = hasWorkable ? 0.0F : 18.0F;
            graphics.blit(BACKGROUND, x, y, 218.0F, vOffset, 18, 18, 256, 256);
            graphics.blit(BACKGROUND, x + 1, y + 1, 236.0F, 0.0F, 16, 16, 256, 256);
        }

        if (this.isHovered()) {
            graphics.fill(x + 1, y + 1, x + 17, y + 17, 0x33FFFFFF);
        }
    }
}
