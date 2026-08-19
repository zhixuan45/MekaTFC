package org.shengxi.registry;

import net.dries007.tfc.common.blocks.GroundcoverBlock;
import net.dries007.tfc.common.blocks.rock.Ore;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.rock.RockCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.shengxi.Mekatfc;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * MekaTFC 方块注册表
 * 注册 21 种 TFC 岩石类型对应的原生锇矿石（Native Osmium）、方铅矿（Galena）与沥青铀矿（Pitchblende）方块（各含贫瘠、普通、富集三品级，共 189 种矿石方块）、
 * 地表小矿石指示物方块以及熔融金属流体方块。
 */
public class ModBlocks {
    // 方块延迟注册器
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Mekatfc.MODID);

    // 矿石方块映射表：OreType -> Rock -> Grade -> DeferredBlock<Block>
    private static final Map<String, Map<Rock, Map<Ore.Grade, DeferredBlock<Block>>>> ALL_GRADED_ORES = new HashMap<>();

    // 原生锇矿石映射表（保持向前兼容）
    private static final Map<Rock, Map<Ore.Grade, DeferredBlock<Block>>> OSMIUM_ORES;
    // 方铅矿映射表
    private static final Map<Rock, Map<Ore.Grade, DeferredBlock<Block>>> GALENA_ORES;
    // 沥青铀矿映射表
    private static final Map<Rock, Map<Ore.Grade, DeferredBlock<Block>>> PITCHBLENDE_ORES;

    // 地表指示物小矿石方块 (Loose Ore Groundcover)
    public static final DeferredBlock<Block> SMALL_NATIVE_OSMIUM;
    public static final DeferredBlock<Block> SMALL_GALENA;
    public static final DeferredBlock<Block> SMALL_PITCHBLENDE;

    // 熔融金属流体方块
    public static final DeferredBlock<LiquidBlock> MOLTEN_OSMIUM_BLOCK;
    public static final DeferredBlock<LiquidBlock> MOLTEN_LEAD_BLOCK;
    public static final DeferredBlock<LiquidBlock> MOLTEN_URANIUM_BLOCK;

    static {
        // 注册 3 种矿物在 21 种岩石与 3 种品级下的方块
        OSMIUM_ORES = registerGradedOreType("native_osmium");
        GALENA_ORES = registerGradedOreType("galena");
        PITCHBLENDE_ORES = registerGradedOreType("pitchblende");

        // 注册地表小矿石方块（作为地表矿脉指示物）
        SMALL_NATIVE_OSMIUM = registerSmallOreGroundcover("ore/small_native_osmium", MapColor.COLOR_LIGHT_GRAY);
        SMALL_GALENA = registerSmallOreGroundcover("ore/small_galena", MapColor.COLOR_GRAY);
        SMALL_PITCHBLENDE = registerSmallOreGroundcover("ore/small_pitchblende", MapColor.COLOR_BLACK);

        // 注册熔融金属流体方块
        MOLTEN_OSMIUM_BLOCK = BLOCKS.register("metal/osmium", () -> new LiquidBlock(
                ModFluids.MOLTEN_OSMIUM.get(),
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_LIGHT_BLUE)
                        .noCollission()
                        .strength(100.0F)
                        .pushReaction(PushReaction.DESTROY)
                        .liquid()
                        .sound(SoundType.EMPTY)
        ));

        MOLTEN_LEAD_BLOCK = BLOCKS.register("metal/lead", () -> new LiquidBlock(
                ModFluids.MOLTEN_LEAD.get(),
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_GRAY)
                        .noCollission()
                        .strength(100.0F)
                        .pushReaction(PushReaction.DESTROY)
                        .liquid()
                        .sound(SoundType.EMPTY)
        ));

        MOLTEN_URANIUM_BLOCK = BLOCKS.register("metal/uranium", () -> new LiquidBlock(
                ModFluids.MOLTEN_URANIUM.get(),
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_LIGHT_GREEN)
                        .noCollission()
                        .strength(100.0F)
                        .pushReaction(PushReaction.DESTROY)
                        .liquid()
                        .sound(SoundType.EMPTY)
        ));
    }

    /**
     * 辅助方法：为指定矿石名称批量注册 21 种岩石 * 3 种品级的方块
     */
    private static Map<Rock, Map<Ore.Grade, DeferredBlock<Block>>> registerGradedOreType(String oreName) {
        Map<Rock, Map<Ore.Grade, DeferredBlock<Block>>> rockMap = new EnumMap<>(Rock.class);
        for (Rock rock : Rock.values()) {
            Map<Ore.Grade, DeferredBlock<Block>> gradeMap = new EnumMap<>(Ore.Grade.class);
            RockCategory category = rock.displayCategory().category();
            float hardness = category.hardness(6.5F);

            for (Ore.Grade grade : Ore.Grade.values()) {
                String gradeName = grade.name().toLowerCase(Locale.ROOT);
                String rockName = rock.getSerializedName();
                String blockId = "ore/" + gradeName + "_" + oreName + "/" + rockName;

                DeferredBlock<Block> oreBlock = BLOCKS.register(blockId, () -> new Block(
                        BlockBehaviour.Properties.of()
                                .mapColor(rock.color())
                                .instrument(NoteBlockInstrument.BASEDRUM)
                                .requiresCorrectToolForDrops()
                                .strength(hardness, 10.0F)
                                .sound(SoundType.STONE)
                ));
                gradeMap.put(grade, oreBlock);
            }
            rockMap.put(rock, Collections.unmodifiableMap(gradeMap));
        }
        Map<Rock, Map<Ore.Grade, DeferredBlock<Block>>> unmodifiableRockMap = Collections.unmodifiableMap(rockMap);
        ALL_GRADED_ORES.put(oreName, unmodifiableRockMap);
        return unmodifiableRockMap;
    }

    /**
     * 辅助方法：注册地表小矿石指示物方块
     */
    private static DeferredBlock<Block> registerSmallOreGroundcover(String blockId, MapColor mapColor) {
        return BLOCKS.register(blockId, () -> GroundcoverBlock.looseOre(
                BlockBehaviour.Properties.of()
                        .mapColor(mapColor)
                        .noCollission()
                        .instabreak()
                        .sound(SoundType.STONE)
        ));
    }

    /**
     * 获取指定岩石和品级的原生锇矿石方块（保持兼容）
     */
    public static DeferredBlock<Block> getOre(Rock rock, Ore.Grade grade) {
        return getOre("native_osmium", rock, grade);
    }

    /**
     * 获取指定矿物、岩石和品级的矿石方块
     */
    public static DeferredBlock<Block> getOre(String oreName, Rock rock, Ore.Grade grade) {
        Map<Rock, Map<Ore.Grade, DeferredBlock<Block>>> rockMap = ALL_GRADED_ORES.get(oreName);
        if (rockMap != null) {
            Map<Ore.Grade, DeferredBlock<Block>> gradeMap = rockMap.get(rock);
            if (gradeMap != null) {
                return gradeMap.get(grade);
            }
        }
        return null;
    }

    public static DeferredBlock<Block> getOsmiumOre(Rock rock, Ore.Grade grade) {
        return getOre("native_osmium", rock, grade);
    }

    public static DeferredBlock<Block> getGalenaOre(Rock rock, Ore.Grade grade) {
        return getOre("galena", rock, grade);
    }

    public static DeferredBlock<Block> getPitchblendeOre(Rock rock, Ore.Grade grade) {
        return getOre("pitchblende", rock, grade);
    }

    /**
     * 获取原生锇矿石映射（保持向后兼容）
     */
    public static Map<Rock, Map<Ore.Grade, DeferredBlock<Block>>> getAllGradedOres() {
        return OSMIUM_ORES;
    }

    public static Map<Rock, Map<Ore.Grade, DeferredBlock<Block>>> getGradedOres(String oreName) {
        return ALL_GRADED_ORES.getOrDefault(oreName, Collections.emptyMap());
    }

    /**
     * 为所有 21 种岩石类型的原生锇矿石、方铅矿和沥青铀矿注册探矿镐代表方块（Representative Blocks）
     * 将贫瘠 (POOR) 和富集 (RICH) 矿石映射到普通品级 (NORMAL)，
     * 使 TFC 探矿镐在地下探测时能够将同区域内的所有品级矿石合并统计数量并统一输出报告。
     */
    public static void registerRepresentativeBlocks() {
        registerRepresentativeForOre("native_osmium");
        registerRepresentativeForOre("galena");
        registerRepresentativeForOre("pitchblende");
    }

    private static void registerRepresentativeForOre(String oreName) {
        for (Rock rock : Rock.values()) {
            DeferredBlock<Block> normal = getOre(oreName, rock, Ore.Grade.NORMAL);
            DeferredBlock<Block> poor = getOre(oreName, rock, Ore.Grade.POOR);
            DeferredBlock<Block> rich = getOre(oreName, rock, Ore.Grade.RICH);
            if (normal != null && poor != null && rich != null) {
                net.dries007.tfc.common.items.PropickItem.registerRepresentative(normal.get(), poor.get(), rich.get());
            }
        }
    }

    /**
     * 注册到 Mod 事件总线
     */
    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
