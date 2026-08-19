package org.shengxi;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import mekanism.api.MekanismAPI;
import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.common.blocks.rock.Ore;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.shengxi.condition.RecipeModeCondition;
import org.shengxi.registry.ModBlocks;
import org.shengxi.registry.ModCreativeTabs;
import org.shengxi.registry.ModFluids;
import org.shengxi.registry.ModItems;
import org.slf4j.Logger;

/**
 * MekaTFC 模组主类
 */
@Mod(Mekatfc.MODID)
public class Mekatfc {
    // 模组 ID
    public static final String MODID = "mekatfc";
    private static final Logger LOGGER = LogUtils.getLogger();

    // 注册自定义配方条件（支持根据合成模式动态过滤配方）
    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS = DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, MODID);
    public static final java.util.function.Supplier<MapCodec<RecipeModeCondition>> RECIPE_MODE_CONDITION = CONDITION_CODECS.register("recipe_mode", () -> RecipeModeCondition.CODEC);

    // 向后兼容引用
    public static final DeferredItem<Item> REDSTONE_MIXTURE = ModItems.REDSTONE_MIXTURE;
    public static final DeferredItem<Item> OSMIUM_DOUBLE_INGOT = ModItems.OSMIUM_DOUBLE_INGOT;

    public Mekatfc(IEventBus modEventBus, ModContainer modContainer, Dist dist) {
        // 注册通用初始化事件
        modEventBus.addListener(this::commonSetup);

        // 如果在客户端运行，注册客户端渲染初始化事件
        if (dist.isClient()) {
            modEventBus.addListener(this::clientSetup);
        }

        // 注册流体、方块、物品与创造模式物品栏
        ModFluids.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        // 注册配方条件
        CONDITION_CODECS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        // 注册模组配置文件
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Initializing MekaTFC Compatibility Layer...");
        LOGGER.info("Detected Mekanism modid: {}", MekanismAPI.MEKANISM_MODID);
        LOGGER.info("Detected TerraFirmaCraft modid: {}", TerraFirmaCraft.MOD_ID);
        LOGGER.info("Optional Mod [Mekanism: Tools] Loaded: {}", net.neoforged.fml.ModList.get().isLoaded("mekanismtools"));
        LOGGER.info("Optional Mod [Mekanism: Generators] Loaded: {}", net.neoforged.fml.ModList.get().isLoaded("mekanismgenerators"));
        LOGGER.info("Current MekaTFC Recipe Mode: {}", Config.recipeMode);

        // 异步任务：为所有 21 种岩石的原生锇矿石注册 TFC 探矿镐代表方块映射
        event.enqueueWork(() -> {
            LOGGER.info("Registering representative blocks for MekaTFC graded osmium ores...");
            ModBlocks.registerRepresentativeBlocks();
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Configuring MekaTFC Client Render Layers...");

        event.enqueueWork(() -> {
            // 为所有 189 种矿石方块（锇、方铅矿、沥青铀矿）配置 CutoutMipped 渲染层，
            // 使矿石 Overlay 贴图的透明通道正常混合，避免底层岩石被黑色遮挡
            String[] oreTypes = {"native_osmium", "galena", "pitchblende"};
            for (String oreType : oreTypes) {
                for (Rock rock : Rock.values()) {
                    for (Ore.Grade grade : Ore.Grade.values()) {
                        var oreBlockHolder = ModBlocks.getOre(oreType, rock, grade);
                        if (oreBlockHolder != null) {
                            ItemBlockRenderTypes.setRenderLayer(oreBlockHolder.get(), RenderType.cutoutMipped());
                        }
                    }
                }
            }

            // 为地表小矿石指示物配置 Cutout 渲染层
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SMALL_NATIVE_OSMIUM.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SMALL_GALENA.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SMALL_PITCHBLENDE.get(), RenderType.cutout());

            // 为熔融金属流体配置 Translucent 半透明渲染层
            ItemBlockRenderTypes.setRenderLayer(ModFluids.MOLTEN_OSMIUM.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_MOLTEN_OSMIUM.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.MOLTEN_LEAD.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_MOLTEN_LEAD.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.MOLTEN_URANIUM.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_MOLTEN_URANIUM.get(), RenderType.translucent());
        });

        LOGGER.info("MekaTFC Client Setup completed for user: {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("MekaTFC Server Starting...");
    }
}
