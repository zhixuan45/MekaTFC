package org.shengxi.registry;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.shengxi.Mekatfc;
import org.shengxi.common.container.ElectricForgeMenu;

/**
 * MekaTFC 容器菜单注册表
 */
public class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Mekatfc.MODID);

    public static final RegistryObject<MenuType<ElectricForgeMenu>> ELECTRIC_FORGE =
            MENUS.register("electric_forge", () -> IForgeMenuType.create(ElectricForgeMenu::new));

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
