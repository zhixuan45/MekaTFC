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
 * 注册熔融锇 (Molten Osmium) 流体类型、源流体、流动流体以及流体属性配置。
 */
public class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Mekatfc.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, Mekatfc.MODID);

    // 锇的金属流体颜色（ARGB 浅青蓝）
    public static final int MOLTEN_OSMIUM_COLOR = 0xFF8AC5D3;

    // 熔融锇流体类型定义
    public static final DeferredHolder<FluidType, FluidType> MOLTEN_OSMIUM_TYPE = FLUID_TYPES.register("metal/osmium", () -> new FluidType(
            FluidType.Properties.create()
                    .density(3000)
                    .viscosity(6000)
                    .temperature(1500)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
                    .descriptionId("fluid.mekatfc.metal.osmium")
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
                    return MOLTEN_OSMIUM_COLOR;
                }

                @Override
                public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
                    return MOLTEN_OSMIUM_COLOR;
                }

                @Override
                public int getTintColor(FluidStack stack) {
                    return MOLTEN_OSMIUM_COLOR;
                }
            });
        }
    });

    // 熔融锇源流体与流动流体
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_OSMIUM = FLUIDS.register("metal/osmium",
            () -> new BaseFlowingFluid.Source(ModFluids.MOLTEN_OSMIUM_PROPERTIES));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_MOLTEN_OSMIUM = FLUIDS.register("metal/flowing_osmium",
            () -> new BaseFlowingFluid.Flowing(ModFluids.MOLTEN_OSMIUM_PROPERTIES));

    // 流体属性绑定
    public static final BaseFlowingFluid.Properties MOLTEN_OSMIUM_PROPERTIES = new BaseFlowingFluid.Properties(
            MOLTEN_OSMIUM_TYPE,
            MOLTEN_OSMIUM,
            FLOWING_MOLTEN_OSMIUM
    ).block(() -> ModBlocks.MOLTEN_OSMIUM_BLOCK.get())
     .bucket(() -> ModItems.MOLTEN_OSMIUM_BUCKET.get())
     .explosionResistance(100.0F)
     .tickRate(30);

    public static void register(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);
    }
}
