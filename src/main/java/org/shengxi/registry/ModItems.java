package org.shengxi.registry;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.rock.Ore;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.items.ChiselItem;
import net.dries007.tfc.common.items.HammerItem;
import net.dries007.tfc.common.items.JavelinItem;
import net.dries007.tfc.common.items.PropickItem;
import net.dries007.tfc.common.items.ScytheItem;
import net.dries007.tfc.common.items.TFCMaceItem;
import net.dries007.tfc.common.items.ToolItem;
import net.minecraft.world.item.AxeItem;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * MekaTFC 物品注册表
 * 注册原生锇矿石、方铅矿、沥青铀矿原料物品（贫瘠、普通、富集）、小矿石方块物品、各岩石矿石的 BlockItem、
 * 红石混合物、双锭、熔融金属流体桶以及锇工具和工具头。
 */
public class ModItems {
    // 物品延迟注册器
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Mekatfc.MODID);

    // ==================== 1. 原生锇矿 (Native Osmium) ====================
    public static final DeferredItem<Item> POOR_NATIVE_OSMIUM = ITEMS.registerSimpleItem("ore/poor_native_osmium");
    public static final DeferredItem<Item> NORMAL_NATIVE_OSMIUM = ITEMS.registerSimpleItem("ore/normal_native_osmium");
    public static final DeferredItem<Item> RICH_NATIVE_OSMIUM = ITEMS.registerSimpleItem("ore/rich_native_osmium");
    public static final DeferredItem<BlockItem> SMALL_NATIVE_OSMIUM = ITEMS.registerSimpleBlockItem("ore/small_native_osmium", ModBlocks.SMALL_NATIVE_OSMIUM);

    // ==================== 2. 方铅矿 (Galena - 铅/银矿) ====================
    public static final DeferredItem<Item> POOR_GALENA = ITEMS.registerSimpleItem("ore/poor_galena");
    public static final DeferredItem<Item> NORMAL_GALENA = ITEMS.registerSimpleItem("ore/normal_galena");
    public static final DeferredItem<Item> RICH_GALENA = ITEMS.registerSimpleItem("ore/rich_galena");
    public static final DeferredItem<BlockItem> SMALL_GALENA = ITEMS.registerSimpleBlockItem("ore/small_galena", ModBlocks.SMALL_GALENA);

    // ==================== 3. 沥青铀矿 (Pitchblende - 铀矿) ====================
    public static final DeferredItem<Item> POOR_PITCHBLENDE = ITEMS.registerSimpleItem("ore/poor_pitchblende");
    public static final DeferredItem<Item> NORMAL_PITCHBLENDE = ITEMS.registerSimpleItem("ore/normal_pitchblende");
    public static final DeferredItem<Item> RICH_PITCHBLENDE = ITEMS.registerSimpleItem("ore/rich_pitchblende");
    public static final DeferredItem<BlockItem> SMALL_PITCHBLENDE = ITEMS.registerSimpleBlockItem("ore/small_pitchblende", ModBlocks.SMALL_PITCHBLENDE);

    // ==================== 4. 合成原料与双锭 ====================
    // 红石混合物：TFC 木桶配方的单一物品输入
    public static final DeferredItem<Item> REDSTONE_MIXTURE = ITEMS.registerSimpleItem("redstone_mixture");

    // 双锭：在 TFC 铁砧上焊接制作
    public static final DeferredItem<Item> OSMIUM_DOUBLE_INGOT = ITEMS.registerSimpleItem("metal/double_ingot/osmium");
    public static final DeferredItem<Item> LEAD_DOUBLE_INGOT = ITEMS.registerSimpleItem("metal/double_ingot/lead");
    public static final DeferredItem<Item> URANIUM_DOUBLE_INGOT = ITEMS.registerSimpleItem("metal/double_ingot/uranium");

    // ==================== 5. 原有 Mekanism 工具对应的锇工具头 ====================
    public static final DeferredItem<Item> OSMIUM_PICKAXE_HEAD = ITEMS.registerSimpleItem("metal/pickaxe_head/osmium");
    public static final DeferredItem<Item> OSMIUM_AXE_HEAD = ITEMS.registerSimpleItem("metal/axe_head/osmium");
    public static final DeferredItem<Item> OSMIUM_SHOVEL_HEAD = ITEMS.registerSimpleItem("metal/shovel_head/osmium");
    public static final DeferredItem<Item> OSMIUM_HOE_HEAD = ITEMS.registerSimpleItem("metal/hoe_head/osmium");
    public static final DeferredItem<Item> OSMIUM_SWORD_BLADE = ITEMS.registerSimpleItem("metal/sword_blade/osmium");

    // ==================== 6. 新增 TFC 专属锇工具头/部件 ====================
    public static final DeferredItem<Item> OSMIUM_SAW_BLADE = ITEMS.registerSimpleItem("metal/saw_blade/osmium");
    public static final DeferredItem<Item> OSMIUM_PROPICK_HEAD = ITEMS.registerSimpleItem("metal/propick_head/osmium");
    public static final DeferredItem<Item> OSMIUM_KNIFE_BLADE = ITEMS.registerSimpleItem("metal/knife_blade/osmium");
    public static final DeferredItem<Item> OSMIUM_SCYTHE_BLADE = ITEMS.registerSimpleItem("metal/scythe_blade/osmium");
    public static final DeferredItem<Item> OSMIUM_HAMMER_HEAD = ITEMS.registerSimpleItem("metal/hammer_head/osmium");
    public static final DeferredItem<Item> OSMIUM_CHISEL_HEAD = ITEMS.registerSimpleItem("metal/chisel_head/osmium");
    public static final DeferredItem<Item> OSMIUM_JAVELIN_HEAD = ITEMS.registerSimpleItem("metal/javelin_head/osmium");
    public static final DeferredItem<Item> OSMIUM_MACE_HEAD = ITEMS.registerSimpleItem("metal/mace_head/osmium");

    // ==================== 7. 新增 TFC 专属锇成品工具 ====================
    // 锇锯子：采伐与木工工具（使用 AxeItem 逻辑以支持伐木与木工标签）
    public static final DeferredItem<AxeItem> OSMIUM_SAW = ITEMS.register("metal/saw/osmium", () ->
            new AxeItem(ModTiers.OSMIUM, toolProperties(0.5f, -3.0f)));

    // 锇探矿镐：矿脉勘探工具
    public static final DeferredItem<PropickItem> OSMIUM_PROPICK = ITEMS.register("metal/propick/osmium", () ->
            new PropickItem(ModTiers.OSMIUM, toolProperties(0.5f, -2.8f)));

    // 锇小刀：切削、割草与剥皮工具
    public static final DeferredItem<Item> OSMIUM_KNIFE = ITEMS.register("metal/knife/osmium", () ->
            new ToolItem(ModTiers.OSMIUM, TFCTags.Blocks.MINEABLE_WITH_KNIFE, toolProperties(0.6f, -2.0f)));

    // 锇镰刀：范围收割农作物与草类工具
    public static final DeferredItem<ScytheItem> OSMIUM_SCYTHE = ITEMS.register("metal/scythe/osmium", () ->
            new ScytheItem(ModTiers.OSMIUM, toolProperties(0.7f, -3.2f)));

    // 锇锤子：铁砧锻造与敲击工具
    public static final DeferredItem<HammerItem> OSMIUM_HAMMER = ITEMS.register("metal/hammer/osmium", () ->
            new HammerItem(ModTiers.OSMIUM, toolProperties(1.0f, -3.0f)));

    // 锇凿子：雕刻与石工工具
    public static final DeferredItem<ChiselItem> OSMIUM_CHISEL = ITEMS.register("metal/chisel/osmium", () ->
            new ChiselItem(ModTiers.OSMIUM, toolProperties(0.27f, 1.5f)));

    // 锇标枪：远程投掷与穿刺武器
    public static final DeferredItem<JavelinItem> OSMIUM_JAVELIN = ITEMS.register("metal/javelin/osmium", () ->
            new JavelinItem(ModTiers.OSMIUM, toolProperties(0.7f, -2.6f)));

    // 锇锤矛：重击近战破甲武器
    public static final DeferredItem<TFCMaceItem> OSMIUM_MACE = ITEMS.register("metal/mace/osmium", () ->
            new TFCMaceItem(toolProperties(1.3f, -3.4f).durability(ModTiers.OSMIUM.getUses())));

    // ==================== 8. 熔融流体桶 ====================
    public static final DeferredItem<BucketItem> MOLTEN_OSMIUM_BUCKET = registerFluidBucket("metal/bucket/osmium", ModFluids.MOLTEN_OSMIUM);
    public static final DeferredItem<BucketItem> MOLTEN_LEAD_BUCKET = registerFluidBucket("metal/bucket/lead", ModFluids.MOLTEN_LEAD);
    public static final DeferredItem<BucketItem> MOLTEN_URANIUM_BUCKET = registerFluidBucket("metal/bucket/uranium", ModFluids.MOLTEN_URANIUM);

    // 矿石方块 BlockItem 映射表：OreType -> Rock -> Grade -> DeferredItem<BlockItem>
    private static final Map<String, Map<Rock, Map<Ore.Grade, DeferredItem<BlockItem>>>> ALL_GRADED_ORE_ITEMS = new HashMap<>();

    static {
        // 注册 3 种矿石在 21 种岩石与 3 种品级下的 BlockItem
        registerGradedOreBlockItems("native_osmium");
        registerGradedOreBlockItems("galena");
        registerGradedOreBlockItems("pitchblende");
    }

    /**
     * 辅助方法：生成工具的 Item.Properties（附加 TFC 属性与攻击修饰符）
     */
    private static Item.Properties toolProperties(float attackDamageModifier, float attackSpeedModifier) {
        return new Item.Properties().attributes(ToolItem.productAttributes(ModTiers.OSMIUM, attackDamageModifier, attackSpeedModifier));
    }

    /**
     * 辅助方法：注册流体桶物品
     */
    private static DeferredItem<BucketItem> registerFluidBucket(String id, java.util.function.Supplier<? extends net.minecraft.world.level.material.Fluid> fluidSupplier) {
        return ITEMS.register(id, () -> new BucketItem(
                fluidSupplier.get(),
                new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
        ));
    }

    /**
     * 辅助方法：批量注册矿石 BlockItem
     */
    private static void registerGradedOreBlockItems(String oreName) {
        Map<Rock, Map<Ore.Grade, DeferredItem<BlockItem>>> rockMap = new EnumMap<>(Rock.class);
        for (Rock rock : Rock.values()) {
            Map<Ore.Grade, DeferredItem<BlockItem>> gradeItemMap = new EnumMap<>(Ore.Grade.class);
            for (Ore.Grade grade : Ore.Grade.values()) {
                String gradeName = grade.name().toLowerCase(Locale.ROOT);
                String rockName = rock.getSerializedName();
                String itemId = "ore/" + gradeName + "_" + oreName + "/" + rockName;

                DeferredBlock<Block> block = ModBlocks.getOre(oreName, rock, grade);
                DeferredItem<BlockItem> blockItem = ITEMS.registerSimpleBlockItem(itemId, block);
                gradeItemMap.put(grade, blockItem);
            }
            rockMap.put(rock, Collections.unmodifiableMap(gradeItemMap));
        }
        ALL_GRADED_ORE_ITEMS.put(oreName, Collections.unmodifiableMap(rockMap));
    }

    /**
     * 获取指定岩石和品级的原生锇矿石 BlockItem（保持向后兼容）
     */
    public static DeferredItem<BlockItem> getOreItem(Rock rock, Ore.Grade grade) {
        return getOreItem("native_osmium", rock, grade);
    }

    /**
     * 获取指定矿物、岩石和品级的矿石 BlockItem
     */
    public static DeferredItem<BlockItem> getOreItem(String oreName, Rock rock, Ore.Grade grade) {
        Map<Rock, Map<Ore.Grade, DeferredItem<BlockItem>>> rockMap = ALL_GRADED_ORE_ITEMS.get(oreName);
        if (rockMap != null) {
            Map<Ore.Grade, DeferredItem<BlockItem>> gradeMap = rockMap.get(rock);
            if (gradeMap != null) {
                return gradeMap.get(grade);
            }
        }
        return null;
    }

    /**
     * 获取指定品级的原料物品（默认原生锇）
     */
    public static DeferredItem<? extends Item> getGradeItem(Ore.Grade grade) {
        return switch (grade) {
            case POOR -> POOR_NATIVE_OSMIUM;
            case NORMAL -> NORMAL_NATIVE_OSMIUM;
            case RICH -> RICH_NATIVE_OSMIUM;
        };
    }

    /**
     * 获取指定矿物与品级的原料物品
     */
    public static DeferredItem<? extends Item> getGradeItem(String oreName, Ore.Grade grade) {
        return switch (oreName) {
            case "galena" -> switch (grade) {
                case POOR -> POOR_GALENA;
                case NORMAL -> NORMAL_GALENA;
                case RICH -> RICH_GALENA;
            };
            case "pitchblende" -> switch (grade) {
                case POOR -> POOR_PITCHBLENDE;
                case NORMAL -> NORMAL_PITCHBLENDE;
                case RICH -> RICH_PITCHBLENDE;
            };
            default -> getGradeItem(grade);
        };
    }

    /**
     * 注册到 Mod 事件总线
     */
    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
