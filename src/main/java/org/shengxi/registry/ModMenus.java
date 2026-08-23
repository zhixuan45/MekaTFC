package org.shengxi.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.shengxi.Mekatfc;
import org.shengxi.common.container.ElectricForgeMenu;

/**
 * MekaTFC 容器菜单注册表 (NeoForge 1.21.1)
 */
public class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Mekatfc.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<ElectricForgeMenu>> ELECTRIC_FORGE =
            MENUS.register("electric_forge", () -> IMenuTypeExtension.create(ElectricForgeMenu::new));

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
