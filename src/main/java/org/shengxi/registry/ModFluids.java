package org.shengxi.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.shengxi.Mekatfc;

import java.util.function.Consumer;

/**
 * MekaTFC 流体注册表
 * 注册熔融锇 (Molten Osmium)、熔融铅 (Molten Lead) 与熔融铀 (Molten Uranium)
 * 的流体类型、源流体、流动流体以及流体属性配置。
 */
public class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Mekatfc.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, Mekatfc.MODID);

    // 金属流体颜色定义 (ARGB)
    public static final int MOLTEN_OSMIUM_COLOR = 0xFF8AC5D3;   // 锇：浅青蓝
    public static final int MOLTEN_LEAD_COLOR = 0xFF5B697D;     // 铅：暗铅灰蓝
    public static final int MOLTEN_URANIUM_COLOR = 0xFF439A3A;  // 铀：荧光黄绿

    // ==================== 1. 熔融锇 (Molten Osmium) ====================
    public static final DeferredHolder<FluidType, FluidType> MOLTEN_OSMIUM_TYPE = registerMetalFluidType("metal/osmium", 3000, 6000, 1500, MOLTEN_OSMIUM_COLOR);

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_OSMIUM = FLUIDS.register("metal/osmium",
            () -> new BaseFlowingFluid.Source(ModFluids.MOLTEN_OSMIUM_PROPERTIES));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_MOLTEN_OSMIUM = FLUIDS.register("metal/flowing_osmium",
            () -> new BaseFlowingFluid.Flowing(ModFluids.MOLTEN_OSMIUM_PROPERTIES));

    public static final BaseFlowingFluid.Properties MOLTEN_OSMIUM_PROPERTIES = new BaseFlowingFluid.Properties(
            MOLTEN_OSMIUM_TYPE,
            MOLTEN_OSMIUM,
            FLOWING_MOLTEN_OSMIUM
    ).block(() -> ModBlocks.MOLTEN_OSMIUM_BLOCK.get())
     .bucket(() -> ModItems.MOLTEN_OSMIUM_BUCKET.get())
     .explosionResistance(100.0F)
     .tickRate(30);

    // ==================== 2. 熔融铅 (Molten Lead) ====================
    public static final DeferredHolder<FluidType, FluidType> MOLTEN_LEAD_TYPE = registerMetalFluidType("metal/lead", 11000, 4000, 600, MOLTEN_LEAD_COLOR);

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_LEAD = FLUIDS.register("metal/lead",
            () -> new BaseFlowingFluid.Source(ModFluids.MOLTEN_LEAD_PROPERTIES));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_MOLTEN_LEAD = FLUIDS.register("metal/flowing_lead",
            () -> new BaseFlowingFluid.Flowing(ModFluids.MOLTEN_LEAD_PROPERTIES));

    public static final BaseFlowingFluid.Properties MOLTEN_LEAD_PROPERTIES = new BaseFlowingFluid.Properties(
            MOLTEN_LEAD_TYPE,
            MOLTEN_LEAD,
            FLOWING_MOLTEN_LEAD
    ).block(() -> ModBlocks.MOLTEN_LEAD_BLOCK.get())
     .bucket(() -> ModItems.MOLTEN_LEAD_BUCKET.get())
     .explosionResistance(100.0F)
     .tickRate(30);

    // ==================== 3. 熔融铀 (Molten Uranium) ====================
    public static final DeferredHolder<FluidType, FluidType> MOLTEN_URANIUM_TYPE = registerMetalFluidType("metal/uranium", 19000, 7000, 1400, MOLTEN_URANIUM_COLOR);

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_URANIUM = FLUIDS.register("metal/uranium",
            () -> new BaseFlowingFluid.Source(ModFluids.MOLTEN_URANIUM_PROPERTIES));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_MOLTEN_URANIUM = FLUIDS.register("metal/flowing_uranium",
            () -> new BaseFlowingFluid.Flowing(ModFluids.MOLTEN_URANIUM_PROPERTIES));

    public static final BaseFlowingFluid.Properties MOLTEN_URANIUM_PROPERTIES = new BaseFlowingFluid.Properties(
            MOLTEN_URANIUM_TYPE,
            MOLTEN_URANIUM,
            FLOWING_MOLTEN_URANIUM
    ).block(() -> ModBlocks.MOLTEN_URANIUM_BLOCK.get())
     .bucket(() -> ModItems.MOLTEN_URANIUM_BUCKET.get())
     .explosionResistance(100.0F)
     .tickRate(30);

    /**
     * 辅助注册金属流体类型，统一配置 TFC 熔融金属材质和着色渲染
     */
    private static DeferredHolder<FluidType, FluidType> registerMetalFluidType(String name, int density, int viscosity, int temperature, int tintColor) {
        return FLUID_TYPES.register(name, () -> new FluidType(
                FluidType.Properties.create()
                        .density(density)
                        .viscosity(viscosity)
                        .temperature(temperature)
                        .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
                        .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
                        .descriptionId("fluid.mekatfc." + name.replace('/', '.'))
        ) {
            @Override
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                consumer.accept(new IClientFluidTypeExtensions() {
                    private static final ResourceLocation STILL = ResourceLocation.fromNamespaceAndPath("tfc", "block/molten_still");
                    private static final ResourceLocation FLOW = ResourceLocation.fromNamespaceAndPath("tfc", "block/molten_flow");

                    @Override
                    public ResourceLocation getStillTexture() {
                        return STILL;
                    }

                    @Override
                    public ResourceLocation getFlowingTexture() {
                        return FLOW;
                    }

                    @Override
                    public int getTintColor() {
                        return tintColor;
                    }

                    @Override
                    public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
                        return tintColor;
                    }

                    @Override
                    public int getTintColor(FluidStack stack) {
                        return tintColor;
                    }
                });
            }
        });
    }

    public static void register(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);
    }
}
