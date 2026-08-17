package org.shengxi.registry;

import net.dries007.tfc.common.blocks.rock.Ore;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.shengxi.Mekatfc;

/**
 * MekaTFC 创造模式物品栏
 */
public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Mekatfc.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MEKATFC_TAB = CREATIVE_MODE_TABS.register("mekatfc_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.mekatfc"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ModItems.NORMAL_NATIVE_OSMIUM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                // 原料与合成中间件
                output.accept(ModItems.SMALL_NATIVE_OSMIUM.get());
                output.accept(ModItems.POOR_NATIVE_OSMIUM.get());
                output.accept(ModItems.NORMAL_NATIVE_OSMIUM.get());
                output.accept(ModItems.RICH_NATIVE_OSMIUM.get());
                output.accept(ModItems.REDSTONE_MIXTURE.get());
                output.accept(ModItems.OSMIUM_DOUBLE_INGOT.get());
                output.accept(ModItems.MOLTEN_OSMIUM_BUCKET.get());

                // 各岩石的原生锇矿石方块
                for (Rock rock : Rock.values()) {
                    for (Ore.Grade grade : Ore.Grade.values()) {
                        output.accept(ModItems.getOreItem(rock, grade).get());
                    }
                }
            }).build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
