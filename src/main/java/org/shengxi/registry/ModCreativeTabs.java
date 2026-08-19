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
                // 1. 原生锇矿原料与金属物品
                output.accept(ModItems.SMALL_NATIVE_OSMIUM.get());
                output.accept(ModItems.POOR_NATIVE_OSMIUM.get());
                output.accept(ModItems.NORMAL_NATIVE_OSMIUM.get());
                output.accept(ModItems.RICH_NATIVE_OSMIUM.get());
                output.accept(ModItems.OSMIUM_DOUBLE_INGOT.get());
                output.accept(ModItems.MOLTEN_OSMIUM_BUCKET.get());

                // 2. 方铅矿（铅）原料与金属物品
                output.accept(ModItems.SMALL_GALENA.get());
                output.accept(ModItems.POOR_GALENA.get());
                output.accept(ModItems.NORMAL_GALENA.get());
                output.accept(ModItems.RICH_GALENA.get());
                output.accept(ModItems.LEAD_DOUBLE_INGOT.get());
                output.accept(ModItems.MOLTEN_LEAD_BUCKET.get());

                // 3. 沥青铀矿（铀）原料与金属物品
                output.accept(ModItems.SMALL_PITCHBLENDE.get());
                output.accept(ModItems.POOR_PITCHBLENDE.get());
                output.accept(ModItems.NORMAL_PITCHBLENDE.get());
                output.accept(ModItems.RICH_PITCHBLENDE.get());
                output.accept(ModItems.URANIUM_DOUBLE_INGOT.get());
                output.accept(ModItems.MOLTEN_URANIUM_BUCKET.get());

                // 4. 特殊合成中间件
                output.accept(ModItems.REDSTONE_MIXTURE.get());

                // 5. 各岩石的矿石方块（锇、方铅矿、沥青铀矿）
                String[] oreTypes = {"native_osmium", "galena", "pitchblende"};
                for (String oreType : oreTypes) {
                    for (Rock rock : Rock.values()) {
                        for (Ore.Grade grade : Ore.Grade.values()) {
                            var item = ModItems.getOreItem(oreType, rock, grade);
                            if (item != null) {
                                output.accept(item.get());
                            }
                        }
                    }
                }
            }).build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
