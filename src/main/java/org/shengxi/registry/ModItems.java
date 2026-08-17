package org.shengxi.registry;

import net.dries007.tfc.common.blocks.rock.Ore;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.shengxi.Mekatfc;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * MekaTFC 物品注册表
 * 注册原生锇矿石物品（贫瘠、普通、富集）、小矿石方块物品、各岩石矿石的 BlockItem、
 * 红石混合物、锇双锭以及熔融锇流体桶。
 */
public class ModItems {
    // 物品延迟注册器
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Mekatfc.MODID);

    // 3 种原生锇矿石原料物品
    public static final DeferredItem<Item> POOR_NATIVE_OSMIUM = ITEMS.registerSimpleItem("ore/poor_native_osmium");
    public static final DeferredItem<Item> NORMAL_NATIVE_OSMIUM = ITEMS.registerSimpleItem("ore/normal_native_osmium");
    public static final DeferredItem<Item> RICH_NATIVE_OSMIUM = ITEMS.registerSimpleItem("ore/rich_native_osmium");

    // 地表小原生锇方块物品（既作为原料物品，也可以放置为地表指示小矿石）
    public static final DeferredItem<BlockItem> SMALL_NATIVE_OSMIUM = ITEMS.registerSimpleBlockItem("ore/small_native_osmium", ModBlocks.SMALL_NATIVE_OSMIUM);

    // 红石混合物：TFC 木桶配方的单一物品输入
    public static final DeferredItem<Item> REDSTONE_MIXTURE = ITEMS.registerSimpleItem("redstone_mixture");

    // 锇双锭：在 TFC 铁砧上焊接制作
    public static final DeferredItem<Item> OSMIUM_DOUBLE_INGOT = ITEMS.registerSimpleItem("metal/double_ingot/osmium");

    // 熔融锇流体桶
    public static final DeferredItem<BucketItem> MOLTEN_OSMIUM_BUCKET = ITEMS.register("metal/bucket/osmium", () -> new BucketItem(
            ModFluids.MOLTEN_OSMIUM.get(),
            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
    ));

    // 矿石方块 BlockItem 映射表：Rock -> Grade -> DeferredItem<BlockItem>
    private static final Map<Rock, Map<Ore.Grade, DeferredItem<BlockItem>>> GRADED_ORE_ITEMS = new EnumMap<>(Rock.class);

    static {
        // 注册 63 种矿石方块的 BlockItem
        for (Rock rock : Rock.values()) {
            Map<Ore.Grade, DeferredItem<BlockItem>> gradeItemMap = new EnumMap<>(Ore.Grade.class);
            for (Ore.Grade grade : Ore.Grade.values()) {
                String gradeName = grade.name().toLowerCase(Locale.ROOT);
                String rockName = rock.getSerializedName();
                String itemId = "ore/" + gradeName + "_native_osmium/" + rockName;

                DeferredBlock<Block> block = ModBlocks.getOre(rock, grade);
                DeferredItem<BlockItem> blockItem = ITEMS.registerSimpleBlockItem(itemId, block);
                gradeItemMap.put(grade, blockItem);
            }
            GRADED_ORE_ITEMS.put(rock, Collections.unmodifiableMap(gradeItemMap));
        }
    }

    /**
     * 获取指定岩石和品级的原生锇矿石 BlockItem
     */
    public static DeferredItem<BlockItem> getOreItem(Rock rock, Ore.Grade grade) {
        Map<Ore.Grade, DeferredItem<BlockItem>> map = GRADED_ORE_ITEMS.get(rock);
        return map != null ? map.get(grade) : null;
    }

    /**
     * 获取指定品级的原料物品
     */
    public static DeferredItem<? extends Item> getGradeItem(Ore.Grade grade) {
        return switch (grade) {
            case POOR -> POOR_NATIVE_OSMIUM;
            case NORMAL -> NORMAL_NATIVE_OSMIUM;
            case RICH -> RICH_NATIVE_OSMIUM;
        };
    }

    /**
     * 注册到 Mod 事件总线
     */
    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
