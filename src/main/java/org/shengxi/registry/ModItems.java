package org.shengxi.registry;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.rock.Ore;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.items.ChiselItem;
import net.dries007.tfc.common.items.HammerItem;
import net.dries007.tfc.common.items.JavelinItem;
import net.dries007.tfc.common.items.PropickItem;
import net.dries007.tfc.common.items.ScytheItem;
import net.dries007.tfc.common.items.ToolItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.shengxi.Mekatfc;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/**
 * MekaTFC 物品注册表
 * 注册原生锇矿石、方铅矿、沥青铀矿原料物品（贫瘠、普通、富集）、小矿石方块物品、各岩石矿石的 BlockItem、
 * 红石混合物、双锭、熔融金属流体桶以及锇工具和工具头。
 */
public class ModItems {
    // 物品延迟注册器
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Mekatfc.MODID);

    // ==================== 1. 原生锇矿 (Native Osmium) ====================
    public static final RegistryObject<Item> POOR_NATIVE_OSMIUM = registerSimpleItem("ore/poor_native_osmium");
    public static final RegistryObject<Item> NORMAL_NATIVE_OSMIUM = registerSimpleItem("ore/normal_native_osmium");
    public static final RegistryObject<Item> RICH_NATIVE_OSMIUM = registerSimpleItem("ore/rich_native_osmium");
    public static final RegistryObject<BlockItem> SMALL_NATIVE_OSMIUM = registerSimpleBlockItem("ore/small_native_osmium", ModBlocks.SMALL_NATIVE_OSMIUM);

    // ==================== 2. 方铅矿 (Galena - 铅/银矿) ====================
    public static final RegistryObject<Item> POOR_GALENA = registerSimpleItem("ore/poor_galena");
    public static final RegistryObject<Item> NORMAL_GALENA = registerSimpleItem("ore/normal_galena");
    public static final RegistryObject<Item> RICH_GALENA = registerSimpleItem("ore/rich_galena");
    public static final RegistryObject<BlockItem> SMALL_GALENA = registerSimpleBlockItem("ore/small_galena", ModBlocks.SMALL_GALENA);

    // ==================== 3. 沥青铀矿 (Pitchblende - 铀矿) ====================
    public static final RegistryObject<Item> POOR_PITCHBLENDE = registerSimpleItem("ore/poor_pitchblende");
    public static final RegistryObject<Item> NORMAL_PITCHBLENDE = registerSimpleItem("ore/normal_pitchblende");
    public static final RegistryObject<Item> RICH_PITCHBLENDE = registerSimpleItem("ore/rich_pitchblende");
    public static final RegistryObject<BlockItem> SMALL_PITCHBLENDE = registerSimpleBlockItem("ore/small_pitchblende", ModBlocks.SMALL_PITCHBLENDE);

    // ==================== 4. 合成原料与双锭 ====================
    // 红石混合物：TFC 木桶配方的单一物品输入
    public static final RegistryObject<Item> REDSTONE_MIXTURE = registerSimpleItem("redstone_mixture");

    // 双锭：在 TFC 铁砧上焊接制作
    public static final RegistryObject<Item> OSMIUM_DOUBLE_INGOT = registerSimpleItem("metal/double_ingot/osmium");
    public static final RegistryObject<Item> LEAD_DOUBLE_INGOT = registerSimpleItem("metal/double_ingot/lead");
    public static final RegistryObject<Item> URANIUM_DOUBLE_INGOT = registerSimpleItem("metal/double_ingot/uranium");

    // ==================== 5. 原有 Mekanism 工具对应的锇工具头 ====================
    public static final RegistryObject<Item> OSMIUM_PICKAXE_HEAD = registerSimpleItem("metal/pickaxe_head/osmium");
    public static final RegistryObject<Item> OSMIUM_AXE_HEAD = registerSimpleItem("metal/axe_head/osmium");
    public static final RegistryObject<Item> OSMIUM_SHOVEL_HEAD = registerSimpleItem("metal/shovel_head/osmium");
    public static final RegistryObject<Item> OSMIUM_HOE_HEAD = registerSimpleItem("metal/hoe_head/osmium");
    public static final RegistryObject<Item> OSMIUM_SWORD_BLADE = registerSimpleItem("metal/sword_blade/osmium");

    // ==================== 6. 新增 TFC 专属锇工具头/部件 ====================
    public static final RegistryObject<Item> OSMIUM_SAW_BLADE = registerSimpleItem("metal/saw_blade/osmium");
    public static final RegistryObject<Item> OSMIUM_PROPICK_HEAD = registerSimpleItem("metal/propick_head/osmium");
    public static final RegistryObject<Item> OSMIUM_KNIFE_BLADE = registerSimpleItem("metal/knife_blade/osmium");
    public static final RegistryObject<Item> OSMIUM_SCYTHE_BLADE = registerSimpleItem("metal/scythe_blade/osmium");
    public static final RegistryObject<Item> OSMIUM_HAMMER_HEAD = registerSimpleItem("metal/hammer_head/osmium");
    public static final RegistryObject<Item> OSMIUM_CHISEL_HEAD = registerSimpleItem("metal/chisel_head/osmium");
    public static final RegistryObject<Item> OSMIUM_JAVELIN_HEAD = registerSimpleItem("metal/javelin_head/osmium");
    public static final RegistryObject<Item> OSMIUM_MACE_HEAD = registerSimpleItem("metal/mace_head/osmium");

    // ==================== 7. 新增 TFC 专属锇成品工具 ====================
    // 锇锯子：采伐与木工工具（使用 AxeItem 逻辑以支持伐木与木工标签）
    public static final RegistryObject<AxeItem> OSMIUM_SAW = ITEMS.register("metal/saw/osmium", () ->
            new AxeItem(ModTiers.OSMIUM, 0.5f, -3.0f, new Item.Properties()));

    // 锇探矿镐：矿脉勘探工具
    public static final RegistryObject<PropickItem> OSMIUM_PROPICK = ITEMS.register("metal/propick/osmium", () ->
            new PropickItem(ModTiers.OSMIUM, 0.5f, -2.8f, new Item.Properties()));

    // 锇小刀：切削、割草与剥皮工具
    public static final RegistryObject<Item> OSMIUM_KNIFE = ITEMS.register("metal/knife/osmium", () ->
            new ToolItem(ModTiers.OSMIUM, 0.6f, -2.0f, TFCTags.Blocks.MINEABLE_WITH_KNIFE, new Item.Properties()));

    // 锇镰刀：范围收割农作物与草类工具
    public static final RegistryObject<ScytheItem> OSMIUM_SCYTHE = ITEMS.register("metal/scythe/osmium", () ->
            new ScytheItem(ModTiers.OSMIUM, 0.7f, -3.2f, TFCTags.Blocks.MINEABLE_WITH_SCYTHE, new Item.Properties()));

    // 锇锤子：铁砧锻造与敲击工具
    public static final RegistryObject<HammerItem> OSMIUM_HAMMER = ITEMS.register("metal/hammer/osmium", () ->
            new HammerItem(ModTiers.OSMIUM, 1.0f, -3.0f, new Item.Properties()));

    // 锇凿子：雕刻与石工工具
    public static final RegistryObject<ChiselItem> OSMIUM_CHISEL = ITEMS.register("metal/chisel/osmium", () ->
            new ChiselItem(ModTiers.OSMIUM, 0.27f, 1.5f, new Item.Properties()));

    // 锇标枪：远程投掷与穿刺武器
    public static final RegistryObject<JavelinItem> OSMIUM_JAVELIN = ITEMS.register("metal/javelin/osmium", () ->
            new JavelinItem(ModTiers.OSMIUM, 0.7f, -2.6f, 2.5f, new Item.Properties(), new net.minecraft.resources.ResourceLocation("mekatfc", "textures/entity/projectiles/javelin/osmium.png")));

    // 锇锤矛：重击近战破甲武器
    public static final RegistryObject<net.dries007.tfc.common.items.MaceItem> OSMIUM_MACE = ITEMS.register("metal/mace/osmium", () ->
            new net.dries007.tfc.common.items.MaceItem(ModTiers.OSMIUM, 4, -3.4f, new Item.Properties()));

    // ==================== 8. 熔融流体桶 ====================
    public static final RegistryObject<BucketItem> MOLTEN_OSMIUM_BUCKET = registerFluidBucket("metal/bucket/osmium", ModFluids.MOLTEN_OSMIUM);
    public static final RegistryObject<BucketItem> MOLTEN_LEAD_BUCKET = registerFluidBucket("metal/bucket/lead", ModFluids.MOLTEN_LEAD);
    public static final RegistryObject<BucketItem> MOLTEN_URANIUM_BUCKET = registerFluidBucket("metal/bucket/uranium", ModFluids.MOLTEN_URANIUM);

    // 矿石方块 BlockItem 映射表：OreType -> Rock -> Grade -> RegistryObject<BlockItem>
    private static final Map<String, Map<Rock, Map<Ore.Grade, RegistryObject<BlockItem>>>> ALL_GRADED_ORE_ITEMS = new HashMap<>();

    static {
        // 注册 3 种矿石在 21 种岩石与 3 种品级下的 BlockItem
        registerGradedOreBlockItems("native_osmium");
        registerGradedOreBlockItems("galena");
        registerGradedOreBlockItems("pitchblende");
    }

    private static RegistryObject<Item> registerSimpleItem(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    private static <B extends net.minecraft.world.level.block.Block> RegistryObject<BlockItem> registerSimpleBlockItem(String name, Supplier<B> blockSupplier) {
        return ITEMS.register(name, () -> new BlockItem(blockSupplier.get(), new Item.Properties()));
    }

    /**
     * 辅助方法：注册流体桶物品
     */
    private static RegistryObject<BucketItem> registerFluidBucket(String id, Supplier<? extends net.minecraft.world.level.material.Fluid> fluidSupplier) {
        return ITEMS.register(id, () -> new BucketItem(
                fluidSupplier,
                new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
        ));
    }

    /**
     * 辅助方法：批量注册矿石 BlockItem
     */
    private static void registerGradedOreBlockItems(String oreName) {
        Map<Rock, Map<Ore.Grade, RegistryObject<BlockItem>>> rockMap = new EnumMap<>(Rock.class);
        for (Rock rock : Rock.values()) {
            Map<Ore.Grade, RegistryObject<BlockItem>> gradeItemMap = new EnumMap<>(Ore.Grade.class);
            for (Ore.Grade grade : Ore.Grade.values()) {
                String gradeName = grade.name().toLowerCase(Locale.ROOT);
                String rockName = rock.getSerializedName();
                String itemId = "ore/" + gradeName + "_" + oreName + "/" + rockName;

                var block = ModBlocks.getOre(oreName, rock, grade);
                RegistryObject<BlockItem> blockItem = registerSimpleBlockItem(itemId, block);
                gradeItemMap.put(grade, blockItem);
            }
            rockMap.put(rock, Collections.unmodifiableMap(gradeItemMap));
        }
        ALL_GRADED_ORE_ITEMS.put(oreName, Collections.unmodifiableMap(rockMap));
    }

    /**
     * 获取指定岩石和品级的原生锇矿石 BlockItem（保持向后兼容）
     */
    public static RegistryObject<BlockItem> getOreItem(Rock rock, Ore.Grade grade) {
        return getOreItem("native_osmium", rock, grade);
    }

    /**
     * 获取指定矿物、岩石和品级的矿石 BlockItem
     */
    public static RegistryObject<BlockItem> getOreItem(String oreName, Rock rock, Ore.Grade grade) {
        Map<Rock, Map<Ore.Grade, RegistryObject<BlockItem>>> rockMap = ALL_GRADED_ORE_ITEMS.get(oreName);
        if (rockMap != null) {
            Map<Ore.Grade, RegistryObject<BlockItem>> gradeMap = rockMap.get(rock);
            if (gradeMap != null) {
                return gradeMap.get(grade);
            }
        }
        return null;
    }

    /**
     * 获取指定品级的原料物品（默认原生锇）
     */
    public static RegistryObject<? extends Item> getGradeItem(Ore.Grade grade) {
        return switch (grade) {
            case POOR -> POOR_NATIVE_OSMIUM;
            case NORMAL -> NORMAL_NATIVE_OSMIUM;
            case RICH -> RICH_NATIVE_OSMIUM;
        };
    }

    /**
     * 获取指定矿物与品级的原料物品
     */
    public static RegistryObject<? extends Item> getGradeItem(String oreName, Ore.Grade grade) {
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

