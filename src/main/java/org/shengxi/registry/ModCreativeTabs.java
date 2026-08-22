package org.shengxi.registry;

import net.dries007.tfc.common.blocks.rock.Ore;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.shengxi.Mekatfc;

/**
 * MekaTFC 创造模式物品栏
 */
public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Mekatfc.MODID);

    public static final RegistryObject<CreativeModeTab> MEKATFC_TAB = CREATIVE_MODE_TABS.register("mekatfc_tab", () -> CreativeModeTab.builder()
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

                // 4. 原有工具对应的锇工具头
                output.accept(ModItems.OSMIUM_PICKAXE_HEAD.get());
                output.accept(ModItems.OSMIUM_AXE_HEAD.get());
                output.accept(ModItems.OSMIUM_SHOVEL_HEAD.get());
                output.accept(ModItems.OSMIUM_HOE_HEAD.get());
                output.accept(ModItems.OSMIUM_SWORD_BLADE.get());

                // 5. 新增 TFC 专属锇工具头与部件
                output.accept(ModItems.OSMIUM_SAW_BLADE.get());
                output.accept(ModItems.OSMIUM_PROPICK_HEAD.get());
                output.accept(ModItems.OSMIUM_KNIFE_BLADE.get());
                output.accept(ModItems.OSMIUM_SCYTHE_BLADE.get());
                output.accept(ModItems.OSMIUM_HAMMER_HEAD.get());
                output.accept(ModItems.OSMIUM_CHISEL_HEAD.get());
                output.accept(ModItems.OSMIUM_JAVELIN_HEAD.get());
                output.accept(ModItems.OSMIUM_MACE_HEAD.get());

                // 6. 新增 TFC 专属锇成品工具
                output.accept(ModItems.OSMIUM_SAW.get());
                output.accept(ModItems.OSMIUM_PROPICK.get());
                output.accept(ModItems.OSMIUM_KNIFE.get());
                output.accept(ModItems.OSMIUM_SCYTHE.get());
                output.accept(ModItems.OSMIUM_HAMMER.get());
                output.accept(ModItems.OSMIUM_CHISEL.get());
                output.accept(ModItems.OSMIUM_JAVELIN.get());
                output.accept(ModItems.OSMIUM_MACE.get());

                // 7. 合成原料与机器设备
                output.accept(ModItems.REDSTONE_MIXTURE.get());
                output.accept(ModItems.DIAMOND_PRECURSOR.get());
                output.accept(ModItems.ROUGH_SYNTHETIC_DIAMOND.get());
                output.accept(ModItems.ELECTRIC_FORGE.get());
                output.accept(ModItems.UPGRADE_PERFECT_FORGING.get());

                // 8. 各岩石的矿石方块（锇、方铅矿、沥青铀矿）
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

