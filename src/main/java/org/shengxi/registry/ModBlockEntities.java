package org.shengxi.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.shengxi.Mekatfc;
import org.shengxi.common.blockentity.ElectricForgeBlockEntity;

/**
 * MekaTFC 方块实体注册表
 */
public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Mekatfc.MODID);

    public static final RegistryObject<BlockEntityType<ElectricForgeBlockEntity>> ELECTRIC_FORGE =
            BLOCK_ENTITIES.register("electric_forge", () ->
                    BlockEntityType.Builder.of(ElectricForgeBlockEntity::new, ModBlocks.ELECTRIC_FORGE.get())
                            .build(null));

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
