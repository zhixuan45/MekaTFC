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
import java.util.Locale;
import java.util.Map;

/**
 * MekaTFC 方块注册表
 * 注册 21 种 TFC 岩石类型对应的原生锇矿石方块（包含贫瘠、普通、富集三种品级，共 63 种矿石方块）、
 * 地表小矿石指示物方块以及熔融锇流体方块。
 */
public class ModBlocks {
    // 方块延迟注册器
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Mekatfc.MODID);

    // 矿石方块映射表：Rock -> Grade -> DeferredBlock<Block>
    private static final Map<Rock, Map<Ore.Grade, DeferredBlock<Block>>> GRADED_ORES = new EnumMap<>(Rock.class);

    // 地表指示物小矿石方块 (Loose Ore Groundcover)
    public static final DeferredBlock<Block> SMALL_NATIVE_OSMIUM;

    // 熔融锇流体方块
    public static final DeferredBlock<LiquidBlock> MOLTEN_OSMIUM_BLOCK;

    static {
        // 静态初始化并注册 21 种岩石的 3 种品级原生锇矿石
        for (Rock rock : Rock.values()) {
            Map<Ore.Grade, DeferredBlock<Block>> gradeMap = new EnumMap<>(Ore.Grade.class);
            RockCategory category = rock.displayCategory().category();
            float hardness = category.hardness(6.5F);

            for (Ore.Grade grade : Ore.Grade.values()) {
                String gradeName = grade.name().toLowerCase(Locale.ROOT);
                String rockName = rock.getSerializedName();
                String blockId = "ore/" + gradeName + "_native_osmium/" + rockName;

                // 矿石方块继承对应岩石的地图颜色与物理属性
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
            GRADED_ORES.put(rock, Collections.unmodifiableMap(gradeMap));
        }

        // 注册地表小原生锇方块（作为地表指示物）
        SMALL_NATIVE_OSMIUM = BLOCKS.register("ore/small_native_osmium", () -> GroundcoverBlock.looseOre(
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_LIGHT_GRAY)
                        .noCollission()
                        .instabreak()
                        .sound(SoundType.STONE)
        ));

        // 注册熔融锇流体方块
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
    }

    /**
     * 获取指定岩石和品级的原生锇矿石方块
     */
    public static DeferredBlock<Block> getOre(Rock rock, Ore.Grade grade) {
        Map<Ore.Grade, DeferredBlock<Block>> map = GRADED_ORES.get(rock);
        return map != null ? map.get(grade) : null;
    }

    /**
     * 获取所有品级矿石映射
     */
    public static Map<Rock, Map<Ore.Grade, DeferredBlock<Block>>> getAllGradedOres() {
        return Collections.unmodifiableMap(GRADED_ORES);
    }

    /**
     * 注册到 Mod 事件总线
     */
    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
